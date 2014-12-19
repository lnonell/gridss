package au.edu.wehi.idsv;


/**
 * Breakpoint evidence including at least one base of sequence at the breakpoint location 
 * @author Daniel Cameron
 *
 */
public interface DirectedBreakpoint extends DirectedEvidence {
	/**
	 * Phred-scaled quality score of breakpoint
	 * @return
	 */
	double getBreakpointQual();
	public BreakpointSummary getBreakendSummary();
	int getRemoteMapq();
	int getRemoteBaseLength();
	int getRemoteBaseCount();
	int getRemoteMaxBaseQual();
	int getRemoteTotalBaseQual();
	/**
	 * Sequence of known untemplated bases (inserted bases not matching the reference)
	 * @return known untemplated bases, empty string if no bases can be identified
	 */
	String getUntemplatedSequence();
}
