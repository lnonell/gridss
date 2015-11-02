package au.edu.wehi.idsv;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileHeader.SortOrder;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.fastq.FastqWriterFactory;
import htsjdk.samtools.filter.AggregateFilter;
import htsjdk.samtools.filter.DuplicateReadFilter;
import htsjdk.samtools.filter.FailsVendorReadQualityFilter;
import htsjdk.samtools.filter.FilteringIterator;
import htsjdk.samtools.filter.SamRecordFilter;
import htsjdk.samtools.filter.SecondaryOrSupplementaryFilter;
import htsjdk.samtools.metrics.Header;
import htsjdk.samtools.metrics.MetricBase;
import htsjdk.samtools.metrics.MetricsFile;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.Log;
import htsjdk.variant.variantcontext.writer.Options;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFHeader;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import au.edu.wehi.idsv.configuration.AssemblyConfiguration;
import au.edu.wehi.idsv.configuration.GridssConfiguration;
import au.edu.wehi.idsv.configuration.RealignmentConfiguration;
import au.edu.wehi.idsv.configuration.SoftClipConfiguration;
import au.edu.wehi.idsv.configuration.VariantCallingConfiguration;
import au.edu.wehi.idsv.picard.ReferenceLookup;
import au.edu.wehi.idsv.picard.SynchronousReferenceLookupAdapter;
import au.edu.wehi.idsv.picard.TwoBitBufferedReferenceSequenceFile;
import au.edu.wehi.idsv.util.AutoClosingIterator;
import au.edu.wehi.idsv.vcf.VcfConstants;
import au.edu.wehi.idsv.visualisation.BufferTracker;
import au.edu.wehi.idsv.visualisation.TrackedBuffer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Processing context for the given record
 * @author Daniel Cameron
 *
 */
public class ProcessingContext implements Closeable {
	private static final Log log = Log.getInstance(ProcessingContext.class);
	/**
	 * Buffer between chromosomes
	 * Must be greater than VariantCallingParameters.breakendMargin
	 * value this huge helps with debugging as the chromosome index and offset are immediately apparent  
	 */
	static final long LINEAR_COORDINATE_CHROMOSOME_BUFFER = 10000000000L;
	private ReferenceLookup reference;
	private final File referenceFile;
	//private final File referenceFile;
	private final SAMSequenceDictionary dictionary;
	private final LinearGenomicCoordinate linear;
	private final FileSystemContext fsContext;
	private final boolean perChr;
	private final GridssConfiguration config;
	private final List<Header> metricsHeaders;
	private final SAMFileHeader basicHeader;
	private boolean filterDuplicates = true;
	private long calculateMetricsRecordCount = Long.MAX_VALUE; 
	private AssemblyIdGenerator assemblyIdGenerator = new SequentialIdGenerator("asm");
	private BufferTracker bufferTracker = null;
	private final List<String> categories = Lists.newArrayList((String)null);
	public ProcessingContext(
			FileSystemContext fileSystemContext,
			List<Header> metricsHeaders,
			GridssConfiguration config,
			File ref, boolean perChr) {
		this(fileSystemContext, metricsHeaders, config, LoadReference(ref), ref, perChr);
		BackgroundCacheReference(ref);
	}
	private void BackgroundCacheReference(final File ref) {
		Thread thread = new Thread(() -> {
			try {
				log.debug("Loading reference genome cache on background thread.");
				this.reference = new TwoBitBufferedReferenceSequenceFile(new IndexedFastaSequenceFile(ref));
				log.debug("Reference genome cache loaded.");
			} catch (Exception e) {
				log.error(e, "Background caching of reference genome failed.");
				System.exit(1);
			}
	    });
		thread.setDaemon(true);
		thread.setName("LoadReference");
		thread.start();
	}
	@SuppressWarnings("resource")
	private static SynchronousReferenceLookupAdapter LoadReference(File ref) {
		try {
			ReferenceSequenceFile underlying = new IndexedFastaSequenceFile(ref);
			if (ref.length() > Runtime.getRuntime().maxMemory()) {
				log.error("Caching reference fasta in memory would require more than 50% of the memory allocated to the JVM. Allocate more heap memory to the JVM..");
				throw new RuntimeException("Not enough memory to cache reference fasta.");
			}
			return new SynchronousReferenceLookupAdapter(underlying);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Unabled load fasta " + ref, e);
		}
	}
	public ProcessingContext(
			FileSystemContext fileSystemContext,
			List<Header> metricsHeaders,
			GridssConfiguration config,
			ReferenceLookup reference, File ref, boolean perChr) {
		this.fsContext = fileSystemContext;
		this.metricsHeaders = metricsHeaders;
		this.config = config;
		this.perChr = perChr;
		this.referenceFile = ref;
		this.reference = reference;
		if (this.reference.getSequenceDictionary() == null) {
			throw new IllegalArgumentException("Missing sequence dictionary for reference genome. Please create using picard CreateSequenceDictionary.");
		}
		this.dictionary = new DynamicSAMSequenceDictionary(this.reference.getSequenceDictionary());
		this.linear = new PaddedLinearGenomicCoordinate(this.dictionary, LINEAR_COORDINATE_CHROMOSOME_BUFFER, true);
		this.basicHeader = new SAMFileHeader();
		this.basicHeader.setSequenceDictionary(this.reference.getSequenceDictionary());
		if (config.getVisualisation().buffers) {
			bufferTracker = new BufferTracker(new File(config.getVisualisation().directory, "gridss.buffers.csv"), config.getVisualisation().bufferTrackingItervalInSeconds);
			bufferTracker.start();
		}
	}
	/**
	 * Creates a new metrics file with appropriate headers for this context 
	 * @return MetricsFile
	 */
	public <A extends MetricBase,B extends Comparable<?>> MetricsFile<A,B> createMetricsFile() {
        final MetricsFile<A,B> file = new MetricsFile<A,B>();
        for (final Header h : metricsHeaders) {
            file.addHeader(h);
        }
        return file;
    }
	public FileSystemContext getFileSystemContext() {
		return fsContext;
	}
	/**
	 * Gets a reader for the given file
	 * @param file SAM/BAM file
	 * @return  htsjdk reader
	 */
	public SamReader getSamReader(File file) {
		return getSamReaderFactory().open(file);
	}
	public SamReaderFactory getSamReaderFactory() {
		SamReaderFactory factory = SamReaderFactory.makeDefault()
				.validationStringency(ValidationStringency.LENIENT);
				//.enable(Option.INCLUDE_SOURCE_IN_RECORDS); // don't need as we're tracking ourselves using EvidenceSource
		return factory;
	}
	public CloseableIterator<SAMRecord> getSamReaderIterator(SamReader reader) {
		return getSamReaderIterator(reader, null);
	}
	public CloseableIterator<SAMRecord> getSamReaderIterator(SamReader reader, SortOrder expectedOrder) {
		return getSamReaderIterator(reader, expectedOrder, null);
	}
	public CloseableIterator<SAMRecord> getSamReaderIterator(File reader) {
		return getSamReaderIterator(reader, null);
	}
	public CloseableIterator<SAMRecord> getSamReaderIterator(File file, SortOrder expectedOrder) {
		return getSamReaderIterator(getSamReader(file), expectedOrder, file);
	}
	private CloseableIterator<SAMRecord> getSamReaderIterator(SamReader reader, SortOrder expectedOrder, File file) {
		SAMRecordIterator rawIterator = reader.iterator();
		if (expectedOrder != null && expectedOrder != SortOrder.unsorted) {
			rawIterator.assertSorted(expectedOrder);
		}
		// wrap so we're happy to close as many times as we want
		CloseableIterator<SAMRecord> safeIterator = new AutoClosingIterator<SAMRecord>(rawIterator,  ImmutableList.<Closeable>of(reader));
		return applyCommonSAMRecordFilters(safeIterator);
	}
	public SAMFileWriterFactory getSamFileWriterFactory(boolean sorted) {
		return new SAMFileWriterFactory()
			.setTempDirectory(fsContext.getTemporaryDirectory())
			.setCreateIndex(sorted);
	}
	/**
	 * Applies filters such as duplicate removal that apply to all SAMRecord parsing
	 * @param iterator raw reads
	 * @return iterator with filtered record excluded
	 */
	public CloseableIterator<SAMRecord> applyCommonSAMRecordFilters(CloseableIterator<SAMRecord> iterator) {
		return applyCommonSAMRecordFilters(iterator, true);
	}
	/**
	 * Applies filters such as duplicate removal that apply to all SAMRecord parsing
	 * @param iterator raw reads
	 * @param filterSecondaryAlignment should secondary alignment be filtered out
	 * @return iterator with filtered record excluded
	 */
	public CloseableIterator<SAMRecord> applyCommonSAMRecordFilters(final CloseableIterator<SAMRecord> iterator, final boolean singleAlignmentPerRead) {
		List<SamRecordFilter> filters = Lists.<SamRecordFilter>newArrayList(new FailsVendorReadQualityFilter());
		if (singleAlignmentPerRead) {
			filters.add(new SecondaryOrSupplementaryFilter());
		}
		if (filterDuplicates) {
			filters.add(new DuplicateReadFilter());
		}
		return new AutoClosingIterator<SAMRecord>(new FilteringIterator(iterator, new AggregateFilter(filters)), ImmutableList.<Closeable>of(iterator));
	}
	public FastqWriterFactory getFastqWriterFactory(){
		FastqWriterFactory factory = new FastqWriterFactory();
		return factory;
	}
	public VariantContextWriterBuilder getVariantContextWriterBuilder(File output, boolean createIndex) {
		VariantContextWriterBuilder builder = new VariantContextWriterBuilder()
			.setOutputFile(output)
			.setReferenceDictionary(getReference().getSequenceDictionary());
		builder.clearOptions();
		if (createIndex) {
			builder.setOption(Options.INDEX_ON_THE_FLY);
		} else {
			builder.clearIndexCreator();
		}
		return builder;
	}
	/**
	 * Gets a VCF file ready to write variants to
	 * A header based on this processing context will have already been written to the returned writer
	 * It is the responsibility of the caller to close the returned @link {@link VariantContextWriter}
	 * @param output file
	 * @return opened output VCF stream
	 */
	public VariantContextWriter getVariantContextWriter(File file, boolean createIndex) {
		VariantContextWriterBuilder builder = getVariantContextWriterBuilder(file, createIndex);
		VariantContextWriter vcfWriter = builder.build();
		final VCFHeader vcfHeader = new VCFHeader();
		VcfConstants.addHeaders(vcfHeader);
		vcfHeader.setSequenceDictionary(getReference().getSequenceDictionary());
		vcfWriter.writeHeader(vcfHeader);
		return vcfWriter;
	}
	/**
	 * Gets a basic minimal SAM file header matching the reference sequence
	 * @return
	 */
	public SAMFileHeader getBasicSamHeader() {
		return basicHeader;
	}
	public ReferenceLookup getReference() {
		return reference;
	}
	public File getReferenceFile() {
		return referenceFile;
	}
	public SAMSequenceDictionary getDictionary() {
		return dictionary;
	}
	public LinearGenomicCoordinate getLinear() {
		return linear;
	}
	public boolean shouldProcessPerChromosome() {
		return perChr;
	}
	@Override
	public void close() throws IOException {
		if (reference != null) reference.close();
	}
	public GridssConfiguration getConfig() {
		return config;
	}
	public AssemblyConfiguration getAssemblyParameters() {
		return getConfig().getAssembly();
	}
	public SoftClipConfiguration getSoftClipParameters() {
		return getConfig().getSoftClip();
	}
	public RealignmentConfiguration getRealignmentParameters() {
		return getConfig().getRealignment();
	}
	public VariantCallingConfiguration getVariantCallingParameters() {
		return getConfig().getVariantCalling();
	}
	public boolean isFilterDuplicates() {
		return filterDuplicates;
	}
	public void setFilterDuplicates(boolean filterDuplicates) {
		this.filterDuplicates = filterDuplicates;
	}
	public long getCalculateMetricsRecordCount() {
		return calculateMetricsRecordCount;
	}
	public void setCalculateMetricsRecordCount(long calculateMetricsRecordCount) {
		this.calculateMetricsRecordCount = calculateMetricsRecordCount;
	}
	public AssemblyIdGenerator getAssemblyIdGenerator() {
		return assemblyIdGenerator;
	}
	public void setAssemblyIdGenerator(AssemblyIdGenerator generator) {
		this.assemblyIdGenerator = generator;
	}
	public void registerCategory(int category, String description) {
		if (category < 0) throw new IllegalArgumentException("Category cannot be negative");
		while (categories.size() <= category) categories.add(null);
		categories.set(category, description);
	}
	/**
	 * Number of categories registered  
	 * @return
	 */
	public int getCategoryCount() {
		return categories.size();
	}
	public void registerBuffer(String context, TrackedBuffer obj) {
		if (bufferTracker != null) {
			bufferTracker.register(context, obj);
		}
	}
}
