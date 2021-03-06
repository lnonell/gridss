package au.edu.wehi.idsv.bed;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import au.edu.wehi.idsv.BreakendDirection;
import au.edu.wehi.idsv.BreakpointSummary;
import au.edu.wehi.idsv.VariantContextDirectedBreakpoint;
import au.edu.wehi.idsv.vcf.VcfInfoAttributes;
import au.edu.wehi.idsv.vcf.VcfSvConstants;
import htsjdk.samtools.SAMSequenceDictionary;

/**
 * Outputs variant calls to BEDPE
 * @author Daniel Cameron
 *
 */
public class BedpeWriter implements Closeable {
	private OutputStream os;
	private SAMSequenceDictionary dict;
	public BedpeWriter(SAMSequenceDictionary dictionary, File file) throws IOException {
		this(dictionary, new FileOutputStream(file));
	}
	public BedpeWriter(SAMSequenceDictionary dictionary, OutputStream stream) throws IOException {
		this.dict = dictionary;
		this.os = new BufferedOutputStream(stream);
	}
	public void writeHeader() throws IOException {
		os.write((
				"#" +
				"chrom1" + "\t" +
				"start1" + "\t" +
				"end1" + "\t" +
				"chrom2" + "\t" +
				"start2" + "\t" +
				"end2" + "\t" +
				"name" + "\t" +
				"score" + "\t" +
				"strand1" + "\t" +
				"strand2" + "\t" +
				VcfSvConstants.HOMOLOGY_SEQUENCE_KEY + "\t" +
				VcfInfoAttributes.BREAKPOINT_ASSEMBLY_COUNT.attribute() + "\t" +
				VcfInfoAttributes.BREAKPOINT_ASSEMBLY_COUNT_REMOTE.attribute() + "\t" +
				VcfInfoAttributes.BREAKPOINT_SPLITREAD_COUNT.attribute() + "\t" +
				//VcfAttributes.BREAKPOINT_SPLITREAD_COUNT_REMOTE.attribute() + "\t" +
				VcfInfoAttributes.BREAKPOINT_READPAIR_COUNT.attribute() + "\t" +
				VcfInfoAttributes.BREAKPOINT_ASSEMBLY_QUAL.attribute() + "\t" +
				VcfInfoAttributes.BREAKPOINT_ASSEMBLY_QUAL_REMOTE.attribute() + "\t" +
				VcfInfoAttributes.BREAKPOINT_SPLITREAD_QUAL.attribute() + "\t" +
				//VcfAttributes.BREAKPOINT_SPLITREAD_QUAL_REMOTE.attribute() + "\t" +
				VcfInfoAttributes.BREAKPOINT_READPAIR_QUAL.attribute() + "\t" +
				VcfInfoAttributes.REFERENCE_READ_COUNT.attribute() + "\t" +
				VcfInfoAttributes.REFERENCE_READPAIR_COUNT.attribute() +
				"\n").getBytes(StandardCharsets.UTF_8));
	}
	public void write(VariantContextDirectedBreakpoint variant) throws IOException {
		BreakpointSummary bp = variant.getBreakendSummary();
		StringBuilder sb = new StringBuilder();
		sb.append(dict.getSequence(bp.referenceIndex).getSequenceName());
		sb.append('\t'); sb.append(Integer.toString(bp.start - 1));
		sb.append('\t'); sb.append(Integer.toString(bp.end - 1));
		sb.append('\t'); sb.append(dict.getSequence(bp.referenceIndex2).getSequenceName());
		sb.append('\t'); sb.append(Integer.toString(bp.start2 - 1));
		sb.append('\t'); sb.append(Integer.toString(bp.end2 - 1));
		sb.append('\t'); sb.append(variant.getID());
		sb.append('\t'); sb.append(variant.getPhredScaledQual());
		sb.append('\t'); sb.append(bp.direction == BreakendDirection.Forward ? '+' : '-');
		sb.append('\t'); sb.append(bp.direction2 == BreakendDirection.Forward ? '+' : '-');
		//header.addMetaDataLine(VcfStructuralVariantHeaderLines.HOMOLOGY_LENGTH);
		// header.addMetaDataLine(VcfStructuralVariantHeaderLines.HOMOLOGY_SEQUENCE);
		sb.append('\t'); sb.append(variant.getHomologySequence());
		sb.append('\t'); sb.append(Integer.toString(variant.getBreakpointEvidenceCountLocalAssembly()));
		sb.append('\t'); sb.append(Integer.toString(variant.getBreakpointEvidenceCountRemoteAssembly()));
		sb.append('\t'); sb.append(Integer.toString(variant.getBreakpointEvidenceCountSoftClip()));
		//sb.append('\t'); sb.append(Integer.toString(variant.getBreakpointEvidenceCountRemoteSoftClip()));
		sb.append('\t'); sb.append(Integer.toString(variant.getBreakpointEvidenceCountReadPair()));

		sb.append('\t'); sb.append(Double.toString(variant.getBreakpointEvidenceQualLocalAssembly()));
		sb.append('\t'); sb.append(Double.toString(variant.getBreakpointEvidenceQualRemoteAssembly()));
		sb.append('\t'); sb.append(Double.toString(variant.getBreakpointEvidenceQualSoftClip()));
		//sb.append('\t'); sb.append(Double.toString(variant.getBreakpointEvidenceQualRemoteSoftClip()));
		sb.append('\t'); sb.append(Double.toString(variant.getBreakpointEvidenceQualReadPair()));
		sb.append('\t'); sb.append(Integer.toString(variant.getReferenceReadCount()));
		sb.append('\t'); sb.append(Integer.toString(variant.getReferenceReadPairCount()));
		sb.append('\n');
		os.write(sb.toString().getBytes(StandardCharsets.UTF_8));
	}
	@Override
	public void close() throws IOException {
		if (os != null) os.close();
		os = null;
	}
}
