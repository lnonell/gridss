package au.edu.wehi.idsv.vcf;

import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

public enum VcfAttributes {
	SOMATIC_P_VALUE ("SPV", 1, VCFHeaderLineType.Float, "Somatic p-value"),
	CALLED_QUAL ("CQUAL", 1, VCFHeaderLineType.Float, "Breakpoint quality score before evidence reallocation"),
	BREAKPOINT_QUAL ("BPQUAL", 1, VCFHeaderLineType.Float, "Quality score of breakpoint evidence after evidence reallocation"),
	BREAKEND_QUAL ("BEQUAL", 1, VCFHeaderLineType.Float, "Quality score of breakend evidence after evidence reallocation"),
	//EVIDENCE_COUNT ("EC", 2, VCFHeaderLineType.Integer, "Count of  evidence supporting structural variation  (Normal,Tumour)"),
	LOG_LIKELIHOOD_RATIO ("LR", 1, VCFHeaderLineType.Float, "Log-likelihood ratio of structural variation vs reference"),
	LOG_LIKELIHOOD_RATIO_BREAKPOINT ("LRBP", 1, VCFHeaderLineType.Float, "Log-likelihood ratio of structural variation vs reference for entire breakpoint"),
	REFERENCE_COUNT_READ ("RC", 2, VCFHeaderLineType.Integer, "Count of reads mapping across this breakend (Normal,Tumour)"),
	REFERENCE_COUNT_READPAIR ("PC", 2, VCFHeaderLineType.Integer, "Count of reference read pairs spanning this breakpoint supporting the reference allele (Normal,Tumour)"),
	
	// Paired Evidence
	READPAIR_EVIDENCE_COUNT ("RPEC", 2, VCFHeaderLineType.Integer, "Count of read pair evidence supporting structural variation  (Normal,Tumour)"),
	READPAIR_LOG_LIKELIHOOD_RATIO ("RPLR", 2, VCFHeaderLineType.Float, "Log-likelihood ratio of read pair structural variation vs reference (Normal,Tumour)"),
	READPAIR_MAPPED_READPAIR ("RPRM", 2, VCFHeaderLineType.Integer, "Count of read pair evidence that maps to a remote breakend (Normal,Tumour)"),
	READPAIR_MAPQ_LOCAL_MAX ("RPMQLM", 2, VCFHeaderLineType.Integer, "Local maximum MAPQ of read pair evidence (Normal,Tumour)"),
	READPAIR_MAPQ_LOCAL_TOTAL ("RPMQLT", 2, VCFHeaderLineType.Integer, "Local total MAPQ of read pair evidence (Normal,Tumour)"),
	READPAIR_MAPQ_REMOTE_MAX ("RPMQRM", 2, VCFHeaderLineType.Integer, "Maxmimum remote MAPQ of read pair evidence (Normal,Tumour)"),
	READPAIR_MAPQ_REMOTE_TOTAL ("RPMQRT", 2, VCFHeaderLineType.Integer, "Total remote MAPQ of read pair evidence (Normal,Tumour)"),
	//READPAIR_LENGTH_LOCAL ("RPBLL", 6, VCFHeaderLineType.Integer, AggregationMethod., "Local length (in bases) of read pair evidence (Overall,Normal,Tumour, totals then maximums)"),
	//READPAIR_LENGTH_REMOTE ("RPBLR", 6, VCFHeaderLineType.Integer, AggregationMethod., "Remote length (in bases) of read pair evidence (Overall,Normal,Tumour, totals then maximums)"),
	//READPAIR_BASE_COUNT_LOCAL ("RPBCL", 6, VCFHeaderLineType.Integer, AggregationMethod., "Local number of bases read pair evidence (Overall,Normal,Tumour, totals then maximums)"),
	//READPAIR_BASE_COUNT_REMOTE ("RPBCR", 6, VCFHeaderLineType.Integer, AggregationMethod., "Remote number of bases read pair evidence (Overall,Normal,Tumour, totals then maximums)"),
	//READPAIR_BASE_QUAL_LOCAL ("RPBQL", 6, VCFHeaderLineType.Integer, "Local base quality of read pair evidence (Overall,Normal,Tumour, totals then maximums)"),
	//READPAIR_BASE_QUAL_REMOTE ("RPBQR", 6, VCFHeaderLineType.Integer, "Remote base quality of read pair evidence (Overall,Normal,Tumour, totals then maximums)"),
	
	// Soft clipped
	SOFTCLIP_EVIDENCE_COUNT ("SCEC", 2, VCFHeaderLineType.Integer, "Count of soft clip evidence supporting structural variation  (Normal,Tumour)"),
	SOFTCLIP_LOG_LIKELIHOOD_RATIO ("SCLR", 2, VCFHeaderLineType.Float, "Log-likelihood ratio of soft clip structural variation vs reference (Normal,Tumour)"),
	SOFTCLIP_MAPPED ("SCRM", 2, VCFHeaderLineType.Integer, "Count of soft clip evidence that maps to a remote breakend (Normal,Tumour)"),
	//SOFTCLIP_MAPQ_LOCAL_MAX ("SCMQLM", 2, VCFHeaderLineType.Integer, "Local maximum MAPQ of soft clip evidence (Normal,Tumour)"),
	//SOFTCLIP_MAPQ_LOCAL_TOTAL ("SCMQLT", 2, VCFHeaderLineType.Integer, "Local total MAPQ of soft clip evidence (Normal,Tumour)"),
	SOFTCLIP_MAPQ_REMOTE_TOTAL ("SCMQRT", 2, VCFHeaderLineType.Integer, "Total MAPQ of realigned soft clips (Normal,Tumour)"),
	SOFTCLIP_MAPQ_REMOTE_MAX ("SCMQRM", 2, VCFHeaderLineType.Integer, "Maximum MAPQ of realigned soft clip (Normal,Tumour)"),
	//SOFTCLIP_LENGTH_LOCAL ("SCBLL", 6, VCFHeaderLineType.Integer, "Local length (in bases) of soft clip evidence (Overall,Normal,Tumour, totals then maximums)"),
	SOFTCLIP_LENGTH_REMOTE_TOTAL ("SCBLRT", 2, VCFHeaderLineType.Integer, "Total length (in bases) of realigned soft clips (Normal,Tumour)"),
	SOFTCLIP_LENGTH_REMOTE_MAX ("SCBLRM", 2, VCFHeaderLineType.Integer, "Maximum length (in bases) of realigned soft clips (Normal,Tumour)"),
	//SOFTCLIP_BASE_COUNT_LOCAL ("SCBCL", 6, VCFHeaderLineType.Integer, AggregationMethod., "Local number of bases soft clip evidence (Overall,Normal,Tumour, totals then maximums)"),
	//SOFTCLIP_BASE_COUNT_REMOTE ("SCBCR", 6, VCFHeaderLineType.Integer, AggregationMethod., "Remote number of bases soft clip evidence (Overall,Normal,Tumour, totals then maximums)"),
	//SOFTCLIP_BASE_QUAL_LOCAL ("SCBQL", 6, VCFHeaderLineType.Integer, AggregationMethod., "Local base quality of soft clip evidence (Overall,Normal,Tumour, totals then maximums)"),
	//SOFTCLIP_BASE_QUAL_REMOTE ("SCBQR", 6, VCFHeaderLineType.Integer, AggregationMethod., "Remote base quality of soft clip evidence (Overall,Normal,Tumour, totals then maximums)"),
	
	// Assembly
	ASSEMBLY_EVIDENCE_COUNT ("A_EC", 1, VCFHeaderLineType.Integer, "Count of breakend assembly evidence supporting structural variation"),
	ASSEMBLY_LOG_LIKELIHOOD_RATIO ("A_LR", 1, VCFHeaderLineType.Float, "Log-likelihood ratio of breakend assembly structural variation vs reference"),
	ASSEMBLY_MAPPED ("A_RM", 1, VCFHeaderLineType.Integer, "Count of breakend assembly evidence that maps to a remote breakend"),
	ASSEMBLY_MAPQ_LOCAL_MAX ("A_MQLM", 1, VCFHeaderLineType.Integer, "Maximum MAPQ of reads anchoring breakend assembly"),
	//ASSEMBLY_MAPQ_LOCAL_TOTAL ("A_MQLT", 2, VCFHeaderLineType.Integer, "Total MAPQ of reads anchoring breakend assembly (Normal,Tumour)"),
	ASSEMBLY_MAPQ_MAX ("A_MQM", 1, VCFHeaderLineType.Integer, "Maxmium MAPQ of realigned breakend assembly"),
	ASSEMBLY_MAPQ_TOTAL ("A_MQT", 1, VCFHeaderLineType.Integer, "Total MAPQ of realigned breakend assembly"),
	ASSEMBLY_LENGTH_LOCAL_MAX ("A_BLLM", 1, VCFHeaderLineType.Integer, "Length (in bases) of breakend assembly mapping to reference at assembly location"),
	//ASSEMBLY_LENGTH_LOCAL_TOTAL ("A_BLLT", 2, VCFHeaderLineType.Integer, "Length (in bases) of breakend assembly mapping to reference at assembly location (Normal,Tumour)"),
	ASSEMBLY_LENGTH_REMOTE_MAX ("A_BLRM", 1, VCFHeaderLineType.Integer, "Length of breakend in assembly"),
	//ASSEMBLY_LENGTH_REMOTE_TOTAL ("A_BLRT", 2, VCFHeaderLineType.Integer, "Remote length (in bases) of breakend assembly evidence (Overall,Normal,Tumour, totals then maximums)"),
	ASSEMBLY_BASE_COUNT ("A_BCT", "bc", 2, VCFHeaderLineType.Integer, "Number of bases in assembly by source (Normal,Tumour)"),
	ASSEMBLY_READPAIR_COUNT ("A_RP", "rp", 2, VCFHeaderLineType.Integer, "Number of read pairs contributing to assembly evidence (Normal,Tumour)"),
	ASSEMBLY_READPAIR_LENGTH_MAX ("A_RPBLM", "rl", 2, VCFHeaderLineType.Integer, "Maxmimum read length of read pairs contributing to assembly (Normal,Tumour)"),
	ASSEMBLY_SOFTCLIP_COUNT ("A_SC", "sc", 2, VCFHeaderLineType.Integer, "Number of soft clips contributing to assembly evidence (Normal,Tumour)"),
	ASSEMBLY_SOFTCLIP_CLIPLENGTH_TOTAL ("A_SCCLT", "sm", 2, VCFHeaderLineType.Integer, "Total soft clip length of assembled reads (Normal,Tumour)"),
	ASSEMBLY_SOFTCLIP_CLIPLENGTH_MAX ("A_SCCLM", "sl", 2, VCFHeaderLineType.Integer, "Maximum soft clip length of assembled reads (Normal,Tumour)"),
	ASSEMBLY_CONSENSUS ("A_AB", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, "Anomolous read consensus assembly sequence"),
	ASSEMBLY_PROGRAM ("A_AP", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, "Assembly algorithm"),
	ASSEMBLY_BREAKEND_QUALS ("A_BQ", 1, VCFHeaderLineType.String, "Assembly breakend base qualities (percent encoding of fastq phred(+33) base qualities)"),
	
	CONFIDENCE_INTERVAL_REMOTE_BREAKEND_START_POSITION_KEY ("CIRPOS", 2, VCFHeaderLineType.Integer, "Confidence interval around remote breakend POS for imprecise variants"),
	
	TRUTH_MATCHES ("TRUTH_MATCHES", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, "Variant ID of all true variants as defined in the supplied truth set"),
	TRUTH_MISREALIGN ("TRUTH_MISREALIGN", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, "Variant ID of the true variant for all variants where the remote breakend location does not match the true variant as defined in the supplied truth set");

	private final VCFInfoHeaderLine header;
	private final String tag;
	VcfAttributes(String name, String samTag, int count, VCFHeaderLineType type, String description) {
		this(new VCFInfoHeaderLine(name, count, type, description), samTag);
	}
	VcfAttributes(String name, String samTag, VCFHeaderLineCount count, VCFHeaderLineType type, String description) {
		this(new VCFInfoHeaderLine(name, count, type, description), samTag);
	}
	VcfAttributes(String name, int count, VCFHeaderLineType type, String description) {
		this(name, null, count, type, description);
	}
	VcfAttributes(String name, VCFHeaderLineCount count, VCFHeaderLineType type, String description) {
		this(name, null, count, type, description);
	}
	VcfAttributes(VCFInfoHeaderLine header, String samTag) {
		this.header = header;
		this.tag = samTag;
	}
	public VCFInfoHeaderLine infoHeader() { return header; }
	public String attribute() { return header != null ? header.getID() : null; }
	/**
	 * Gets the attribute for the given key
	 * @param key VCF info field name
	 * @return corresponding attribute, null if no idsv-specific attribute with the given name is defined
	 */
	public static VcfAttributes getAttributefromKey(String key) {
		for (VcfAttributes a : values()) {
			if (a.attribute() == key) return a;
		}
		return null;
	}
	/**
	 * SAM tag for attribute when persisted to BAM
	 * @return
	 */
	public String samTag() {
		return tag;
	}
}
