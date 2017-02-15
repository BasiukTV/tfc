package basiuktv.tfc.data.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

import basiuktv.tfc.data.processor.ProcessingResult;

/** Tests for ProcessingResult. */
public class ProcessingResultTests {

	/** Tests combining non-verbose (without every term count map) results */
	@Test
	public void testCombinationNonVerbose() {
		final ProcessingResult r1 = new ProcessingResult(5L, 10L, Optional.empty());
		final ProcessingResult r2 = new ProcessingResult(10L, 20L, Optional.empty());

		final ProcessingResult test1 = ProcessingResult.combineResults(r1, r2);
		Assert.assertEquals(test1.getTermMatchCount(), 15L);
		Assert.assertEquals(test1.getAllTermsCount(), 30L);
		Assert.assertFalse(test1.getEveryTermCount().isPresent());

		final ProcessingResult test2 = ProcessingResult.combineResults(r2, r1);
		Assert.assertEquals(test2.getTermMatchCount(), 15L);
		Assert.assertEquals(test2.getAllTermsCount(), 30L);
		Assert.assertFalse(test2.getEveryTermCount().isPresent());
	}

	/** Tests combination of two ProcessingResult when one of them is verbose (has map of every term count). */
	@Test
	public void testCombinationOneVerbose() {
		final Map<String, Long> m = new HashMap<String, Long>();
		m.put("one", 1L);
		m.put("two", 2L);

		ProcessingResult r1 = new ProcessingResult(5L, 10L, Optional.of(m));
		ProcessingResult r2 = new ProcessingResult(10L, 20L, Optional.empty());
		final ProcessingResult test1 = ProcessingResult.combineResults(r1, r2);
		Assert.assertEquals(test1.getTermMatchCount(), 15L);
		Assert.assertEquals(test1.getAllTermsCount(), 30L);
		Assert.assertTrue(test1.getEveryTermCount().get().equals(m));

		r1 = new ProcessingResult(5L, 10L, Optional.empty());
		r2 = new ProcessingResult(10L, 20L, Optional.of(m));
		final ProcessingResult test2 = ProcessingResult.combineResults(r1, r2);
		Assert.assertEquals(test2.getTermMatchCount(), 15L);
		Assert.assertEquals(test2.getAllTermsCount(), 30L);
		Assert.assertTrue(test2.getEveryTermCount().get().equals(m));
	}

	/** Tests combination of two ProcessingResult when both of them are verbose (has map of every term count). */
	@Test
	public void testCombinationTwoVerbose() {
		final Map<String, Long> m1 = new HashMap<String, Long>();
		m1.put("one", 1L);
		m1.put("two", 2L);

		final Map<String, Long> m2 = new HashMap<String, Long>();
		m2.put("two", 2L);
		m2.put("three", 3L);

		ProcessingResult r1 = new ProcessingResult(5L, 10L, Optional.of(m1));
		ProcessingResult r2 = new ProcessingResult(10L, 20L, Optional.of(m2));
		final ProcessingResult test = ProcessingResult.combineResults(r1, r2);
		Assert.assertEquals(test.getTermMatchCount(), 15L);
		Assert.assertEquals(test.getAllTermsCount(), 30L);
		Assert.assertEquals(test.getEveryTermCount().get().get("one"), new Long(1L));
		Assert.assertEquals(test.getEveryTermCount().get().get("two"), new Long(4L));
		Assert.assertEquals(test.getEveryTermCount().get().get("three"), new Long(3L));
		Assert.assertEquals(test.getEveryTermCount().get().size(), 3);
	}
}
