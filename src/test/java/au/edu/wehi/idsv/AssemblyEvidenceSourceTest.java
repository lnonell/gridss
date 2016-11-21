package au.edu.wehi.idsv;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import au.edu.wehi.idsv.configuration.GridssConfiguration;
import au.edu.wehi.idsv.picard.ReferenceLookup;
import au.edu.wehi.idsv.picard.SynchronousReferenceLookupAdapter;
import au.edu.wehi.idsv.vcf.VcfFilter;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.fastq.FastqRecord;
import htsjdk.samtools.metrics.Header;
import htsjdk.samtools.metrics.StringHeader;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;

public class AssemblyEvidenceSourceTest extends IntermediateFilesTest {
	private File assemblyFile;
	@Before
	public void setup() throws IOException {
		super.setup();
		assemblyFile = new File(super.testFolder.getRoot(), "breakend.bam");
	}
	@Test
	public void should_write_breakend_bam() throws IOException {
		createInput(RP(0, 1, 2, 1));
		SAMEvidenceSource ses = new SAMEvidenceSource(getCommandlineContext(), input, 0);
		AssemblyEvidenceSource aes = new AssemblyEvidenceSource(getCommandlineContext(), ImmutableList.of(ses), assemblyFile);
		aes.assembleBreakends();
		getFastqRecords(aes);
	}
	@Test
	public void debruijn_should_generate_bam() throws IOException {
		createInput(
				withSequence("AATTAATCGCAAGAGCGGGTTGTATTCGACGCCAAGTCAGCTGAAGCACCATTACCCGATCAAAACATATCAGAAATGATTGACGTATCACAAGCCGGA", Read(0, 1, "1M98S")),
				withSequence("AATTAATCGCAAGAGCGGGTTGTATTCGACGCCAAGTCAGCTGAAGCACCATTACCCGATCAAAACATATCAGAAATGATTGACGTATCACAAGCCGGAT", Read(0, 1, "1M99S"))
				);
		ProcessingContext pc = getCommandlineContext();
		SAMEvidenceSource ses = new SAMEvidenceSource(pc, input, 0);
		AssemblyEvidenceSource aes = new AssemblyEvidenceSource(pc, ImmutableList.of(ses), assemblyFile);
		aes.assembleBreakends();
		
		assertEquals(1, getRecords(assemblyFile).size());
		assertEquals("AATTAATCGCAAGAGCGGGTTGTATTCGACGCCAAGTCAGCTGAAGCACCATTACCCGATCAAAACATATCAGAAATGATTGACGTATCACAAGCCGGAT", S(getRecords(assemblyFile).get(0).getReadBases()));
	}
	@Test
	public void iterator_should_return_in_chr_order() throws IOException {
		createInput(
				withSequence("AATTAATCGCAAGAGCGGGTTGTATTCGACGCCAAGTCAGCTGAAGCACCATTACCCGATCAAAACATATCAGAAATGATTGACGTATCACAAGCCGGA", Read(0, 1, "1M98S")),
				withSequence("AATTAATCGCAAGAGCGGGTTGTATTCGACGCCAAGTCAGCTGAAGCACCATTACCCGATCAAAACATATCAGAAATGATTGACGTATCACAAGCCGGAT", Read(0, 1, "1M99S")),
				withSequence("AATTAATCGCAAGAGCGGGTTGTATTCGACGCCAAGTCAGCTGAAGCACCATTACCCGATCAAAACATATCAGAAATGATTGACGTATCACAAGCCGGA", Read(1, 1, "1M98S")),
				withSequence("AATTAATCGCAAGAGCGGGTTGTATTCGACGCCAAGTCAGCTGAAGCACCATTACCCGATCAAAACATATCAGAAATGATTGACGTATCACAAGCCGGAT", Read(1, 1, "1M99S")),
				withSequence("AATTAATCGCAAGAGCGGGTTGTATTCGACGCCAAGTCAGCTGAAGCACCATTACCCGATCAAAACATATCAGAAATGATTGACGTATCACAAGCCGGA", Read(2, 1, "1M98S")),
				withSequence("AATTAATCGCAAGAGCGGGTTGTATTCGACGCCAAGTCAGCTGAAGCACCATTACCCGATCAAAACATATCAGAAATGATTGACGTATCACAAGCCGGAT", Read(2, 1, "1M99S")),
				withSequence("AATTAATCGCAAGAGCGGGTTGTATTCGACGCCAAGTCAGCTGAAGCACCATTACCCGATCAAAACATATCAGAAATGATTGACGTATCACAAGCCGGA", Read(3, 1, "1M98S")),
				withSequence("AATTAATCGCAAGAGCGGGTTGTATTCGACGCCAAGTCAGCTGAAGCACCATTACCCGATCAAAACATATCAGAAATGATTGACGTATCACAAGCCGGAT", Read(3, 1, "1M99S"))
				);
		ProcessingContext pc = getCommandlineContext();
		//pc.getRealignmentParameters().requireRealignment = false;
		SAMEvidenceSource ses = new SAMEvidenceSource(pc, input, 0);
		AssemblyEvidenceSource aes = new AssemblyEvidenceSource(pc, ImmutableList.of(ses), assemblyFile);
		aes.assembleBreakends();
		List<DirectedEvidence> list = Lists.newArrayList(aes.iterator());
		assertEquals(4,  list.size());
		for (int i = 0; i <= 3; i++) {
			assertEquals(i, list.get(i).getBreakendSummary().referenceIndex);
		}
	}
	@Test
	public void should_not_write_filtered_assemblies() throws IOException {
		createInput(
				OEA(0, 1, "100M", true)
				);
		ProcessingContext pc = getCommandlineContext();
		pc.getAssemblyParameters().writeFiltered = false;
		SAMEvidenceSource ses = new SAMEvidenceSource(pc, input, 0);
		AssemblyEvidenceSource aes = new AssemblyEvidenceSource(pc, ImmutableList.of(ses), assemblyFile);
		aes.assembleBreakends();
		List<DirectedEvidence> contigs = Lists.newArrayList(aes.iterator());
		assertEquals(0, contigs.size());
	}
	@Test
	@Category(Hg19Tests.class)
	public void should_generate_indel_assemblies() throws IOException {
		createInput(new File("src/test/resources/inss.bam"));
		List<Header> headers = Lists.newArrayList();
		headers.add(new StringHeader("TestHeader"));
		File ref = Hg19Tests.findHg19Reference();
		IndexedFastaSequenceFile indexed = new IndexedFastaSequenceFile(ref);
		ReferenceLookup lookup = new SynchronousReferenceLookupAdapter(indexed);
		ProcessingContext pc = new ProcessingContext(
				new FileSystemContext(testFolder.getRoot(), 500000), ref, false, lookup,
				headers,
				new GridssConfiguration(getDefaultConfig(), testFolder.getRoot()));
		pc.registerCategory(0, "Normal");
		SAMEvidenceSource ses = new SAMEvidenceSource(pc, input, 0);
		AssemblyEvidenceSource aes = new AssemblyEvidenceSource(pc, ImmutableList.of(ses), assemblyFile);
		aes.assembleBreakends();
		List<DirectedEvidence> contigs = Lists.newArrayList(aes.iterator());
		assertEquals(2, contigs.size());
	}
	@Test
	public void should_filter_fully_reference_assemblies() {
		SAMRecord r = AssemblyFactory.createAnchoredBreakend(
				getContext(), AES(), BWD, null,
				0, 1, 2, B("AA"), B("AA"));
		assertTrue(AES().shouldFilterAssembly(r));
	}
	@Test
	public void should_filter_single_read_assemblies() {
		MockDirectedEvidence ev = new MockDirectedEvidence(new BreakendSummary(0, FWD, 1, 1, 2));
		SAMRecord r = AssemblyFactory.createAnchoredBreakend(getContext(), AES(), FWD, Lists.newArrayList(ev), 0, 1, 1, B("AA"), B("AA"));
		assertTrue(AES().shouldFilterAssembly(r));
	}
	@Test
	public void should_filter_mate_anchored_assembly_shorter_than_read_length() {
		ArrayList<DirectedEvidence> support = Lists.<DirectedEvidence>newArrayList(NRRP(OEA(0, 1, "4M", true)));
		SAMRecord e = AssemblyFactory.createUnanchoredBreakend(
				getContext(), AES(), new BreakendSummary(0, FWD, 5, 5, 300), support,
				B("AAA"), B("AAA"), new int[] { 2, 0 });
		assertTrue(AES().shouldFilterAssembly(e));
	}
	@Test
	public void read_length_filter_should_not_apply_to_anchored_breakend_assembly() {
		ArrayList<DirectedEvidence> support = Lists.<DirectedEvidence>newArrayList(SCE(FWD, Read(0, 1, "1M2S")),
				NRRP(OEA(0, 1, "3M", true)),
				NRRP(OEA(0, 1, "4M", true)));
		SAMRecord e = AssemblyFactory.createAnchoredBreakend(
				getContext(), AES(), FWD, support,
				0, 1, 1, B("AAA"), B("AAA"));
		assertFalse(AES().shouldFilterAssembly(e));
	}
	@Test
	public void soft_clip_size_filter_should_not_apply_to_unanchored_assembly() {
		ArrayList<DirectedEvidence> support = Lists.<DirectedEvidence>newArrayList(
				NRRP(OEA(0, 1, "3M", false)),
				NRRP(OEA(0, 1, "4M", false)));
		SAMRecord e = AssemblyFactory.createUnanchoredBreakend(
				getContext(), AES(), new BreakendSummary(0, FWD, 5, 5, 300), support,
				B("AAAAAA"), B("AAAAAA"), new int[] { 2, 0 });
		assertFalse(AES().shouldFilterAssembly(e));
	}
	@Test
	public void should_filter_if_no_breakpoint_assembly() {
		ArrayList<DirectedEvidence> support = Lists.<DirectedEvidence>newArrayList(
				SCE(BWD, Read(0, 1, "1S1M")),
				NRRP(OEA(0, 1, "3M", false)),
				NRRP(OEA(0, 1, "4M", false)));
		SAMRecord e = AssemblyFactory.createAnchoredBreakend(
				getContext(), AES(), BWD, support,
				0, 1, 2, B("AA"), B("AA"));
		// reference assembly
		assertTrue(AES().shouldFilterAssembly(e));
	}
	@Test
	public void should_not_apply_breakend_filter_to_unanchored_assembly() {
		ArrayList<DirectedEvidence> support = Lists.<DirectedEvidence>newArrayList(NRRP(SES(100, 100), DP(0, 1, "1M", true, 0, 5, "1M", false)));
		SAMRecord e = AssemblyFactory.createUnanchoredBreakend(
				getContext(), AES(), new BreakendSummary(0, FWD, 1, 1, 300), support,
				B("AA"), B("AA"), new int[] { 2, 0});
		assertFalse(AES().shouldFilterAssembly(e));
	}
	@Test
	public void should_filter_too_few_reads() {
		ProcessingContext pc = getContext();
		pc.getAssemblyParameters().minReads = 3;
		SAMRecord e = AssemblyFactory.createAnchoredBreakend(pc, AES(), BWD, null, 0, 1, 5, B("AACGTG"), B("AACGTG"));
		assertTrue(AES().shouldFilterAssembly(e));
		
		ArrayList<DirectedEvidence> support = Lists.<DirectedEvidence>newArrayList(
				SCE(BreakendDirection.Forward, withQual(new byte[] { 5,5,5,5,5,5 }, withSequence("AACGTG", Read(0, 1, "1M5S")))));
		e = AssemblyFactory.createAnchoredBreakend(
				getContext(), AES(), BWD, support,
				0, 1, 5, B("AACGTG"), B("AACGTG"));
		assertTrue(AES().shouldFilterAssembly(e));
		
		support = Lists.<DirectedEvidence>newArrayList(
				SCE(BreakendDirection.Forward, withQual(new byte[] { 5,5,5,5,5,5 }, withSequence("AACGTG", Read(0, 1, "1M5S")))),
				NRRP(OEA(0, 1, "4M", false)));
		e = AssemblyFactory.createAnchoredBreakend(
				pc, AES(), BWD, support,
				0, 1, 5, B("AACGTG"), B("AACGTG"));
		assertTrue(AES().shouldFilterAssembly(e));
		
		support = Lists.<DirectedEvidence>newArrayList(
				SCE(BreakendDirection.Forward, withQual(new byte[] { 5,5,5,5,5,5 }, withSequence("AACGTG", Read(0, 1, "1M5S")))),
				NRRP(OEA(0, 1, "3M", false)),
				NRRP(OEA(0, 1, "5M", false)));
		e = AssemblyFactory.createAnchoredBreakend(
				pc, AES(), BWD, support,
				0, 1, 5, B("AACGTG"), B("AACGTG"));
		assertFalse(AES().shouldFilterAssembly(e));
	}
	@Test
	public void should_filter_reference_breakend() {
		List<DirectedEvidence> evidence = Lists.newArrayList();
		evidence.add(SCE(FWD, Read(0, 5, "5M5S")));
		//evidence.add(SCE(FWD, Read(0, 5, "5M6S")));
		//evidence.add(SCE(FWD, Read(0, 5, "5M7S")));
		SAMRecord e = AssemblyFactory.createAnchoredBreakend(getContext(), AES(), FWD, evidence,
				0, 10, 5, B("AAAAAAAAAA"), B("AAAAAAAAAA"));
		assertTrue(AES().shouldFilterAssembly(e));
	}
	@Test
	public void should_filter_reference_breakpoint() {
		List<DirectedEvidence> evidence = Lists.newArrayList();
		evidence.add(SCE(FWD, Read(0, 5, "5M5S")));
		evidence.add(SCE(FWD, Read(0, 5, "5M6S")));
		evidence.add(SCE(FWD, Read(0, 5, "5M7S")));
		SAMRecord e = AssemblyFactory.createAnchoredBreakend(getContext(), AES(), FWD, evidence,
				0, 10, 5, B("AAAAAAAAAA"), B("AAAAAAAAAA"));
		assertTrue(AES().shouldFilterAssembly(e));
	}
}
