package gridss.analysis;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import au.edu.wehi.idsv.IntermediateFilesTest;
import picard.analysis.SinglePassSamProgram;


public class CollectMapqMetricsTest extends IntermediateFilesTest {
	@Test
	public void should_allow_no_histogram() throws IOException {
		CollectMapqMetrics cmm = new CollectMapqMetrics();
		cmm.INPUT = new File("src/test/resources/203541.bam");
		cmm.OUTPUT = new File(testFolder.getRoot(), "mapqmetrics.txt");
		SinglePassSamProgram.makeItSo(cmm.INPUT, null, true, 0, ImmutableList.of(cmm));
		assertTrue(cmm.OUTPUT.isFile());
	}
	@Test
	public void should_generate_metrics() throws IOException {
		CollectMapqMetrics cmm = new CollectMapqMetrics();
		cmm.INPUT = new File("src/test/resources/203541.bam");
		cmm.OUTPUT = new File(testFolder.getRoot(), "mapqmetrics.txt");
		cmm.Histogram_FILE = new File(testFolder.getRoot(), "mapqhistogram.pdf");
		SinglePassSamProgram.makeItSo(cmm.INPUT, null, true, 0, ImmutableList.of(cmm));
		assertTrue(cmm.OUTPUT.isFile());
	}
	@Test
	@Ignore("R dependency")
	public void should_generate_histogram() throws IOException {
		CollectMapqMetrics cmm = new CollectMapqMetrics();
		File histogram = new File(testFolder.getRoot(), "mapqhistogram.pdf");
		cmm.INPUT = new File("src/test/resources/203541.bam");
		cmm.OUTPUT = new File(testFolder.getRoot(), "mapqmetrics.txt");
		cmm.Histogram_FILE = histogram;
		SinglePassSamProgram.makeItSo(cmm.INPUT, null, true, 0, ImmutableList.of(cmm));
		assertTrue(histogram.isFile());
	}
}
