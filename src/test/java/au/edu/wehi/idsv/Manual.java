package au.edu.wehi.idsv;

import htsjdk.samtools.metrics.Header;
import htsjdk.samtools.util.CloseableIterator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.experimental.categories.Category;

import au.edu.wehi.idsv.configuration.GridssConfiguration;

/**
 * Ad-hoc debugging tests
 * @author Daniel Cameron
 *
 */
public class Manual extends TestHelper {
	/**
	 * Test our iterators are behaving correctly
	 */
	//@Test
	@Category(Hg19Tests.class)
	public void debug778sorting() throws ConfigurationException {
		ProcessingContext pc = new ProcessingContext(
			new FileSystemContext(new File("W:\\778\\idsv"), new File("W:\\778\\idsv"), 500000), Hg19Tests.findHg19Reference(), true, null,
			new ArrayList<Header>(), new GridssConfiguration());
		List<SAMEvidenceSource> samEvidence = new ArrayList<SAMEvidenceSource>();
		for (String s : new String[] {
				"W:\\778\\DNA_778_HiSeq_35nt_PE1_bt2_s_rg_cleaned.bam",
				"W:\\778\\DNA_778_IL_35nt_PE1_bt2_s_rg_cleaned.bam",
				"W:\\778\\DNA_778_IL_75nt_PE1_bt2_s_rg_cleaned.bam",
				"W:\\778\\DNA_778_PM_75nt_PE1_bt2_s_rg_cleaned.bam",
				"W:\\778\\DNA_778_PM_lane1_100nt_PE1_bt2_s_rg_cleaned.bam",
				"W:\\778\\DNA_778_PM_lane2_100nt_PE1_bt2_s_rg_cleaned.bam",
			}) {
			SAMEvidenceSource ses = new SAMEvidenceSource(pc, new File(s), 0);
			//Iterator<DirectedEvidence> it = ses.iterator(true, true, true);
			//while (it.hasNext()) {
			//	it.next();
			//}
			samEvidence.add(ses);
		}
		AssemblyEvidenceSource aes = new AssemblyEvidenceSource(pc, samEvidence, new File("W:\778\\idsv\\778.vcf.idsv.working"));
		//Iterator<SAMRecordAssemblyEvidence> it = aes.iterator(true, true);
		//while (it.hasNext()) {
		//	it.next();
		//}
		EP e = new EP(pc, new File("W:\778\\idsv\\778.vcf"), samEvidence, aes);
		Iterator<DirectedEvidence> allIt = e.getAllEvidence();
		while (allIt.hasNext()) {
			allIt.next();
		}
	}
	private static class EP extends EvidenceProcessorBase {
		public EP(ProcessingContext context, File output, List<SAMEvidenceSource> samEvidence, AssemblyEvidenceSource assemblyEvidence) {
			super(context, output, samEvidence, assemblyEvidence);
		}
		public CloseableIterator<DirectedEvidence> getAllEvidence() {
			return super.getAllEvidence(true, true, true, true, true);
		}
		@Override
		public void process(ExecutorService threadpool) {
		}
	}
}
