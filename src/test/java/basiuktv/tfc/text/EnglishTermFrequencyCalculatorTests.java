package basiuktv.tfc.text;

import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import basiuktv.tfc.data.processor.ProcessingResult;
import basiuktv.tfc.launcher.CLIOptions;

/** Tests for EnglishTermFrequencyCalculator. */
public class EnglishTermFrequencyCalculatorTests {

	private static final EnglishTermFrequencyCalculator TEST_CALCULATOR = new EnglishTermFrequencyCalculator();

	private static final String TEST_TERM = "term";

	/** Tests common English term separators. */
	@Test
	public void testTermsSeparation() throws ParseException {
		final CLIOptions options = new CLIOptions(new String[]{"-" + CLIOptions.TERM_SHORT, TEST_TERM, "input.txt"});
		final ProcessingResult result = TEST_CALCULATOR.splitIntoTerms(
				String.format(" %1$s.%1$s,%1$s:%1$s;%1$s!%1$s?%1$s(%1$s)%1$s\n%1$s\t%1$s\r", TEST_TERM), options);
		Assert.assertEquals(result.getTermMatchCount(), 11L);
		Assert.assertEquals(result.getAllTermsCount(), 11L);
		Assert.assertEquals(EnglishTermFrequencyCalculator.TERM_SEPARATOR_CHARS.length(), 12L,
				"EnglishTermFrequencyCalculator.TERM_SEPARATORS was updated, but this test was not.");
	}

	/**
	 * Tests term trimming.
	 * Basically we want splitting "'don't'" text result in one term "don't", not "don" and "t".
	 */
	@Test
	public void testTermsTrimming() throws ParseException {
		final String singleQuote = "don't";
		CLIOptions options = new CLIOptions(new String[]{"-" + CLIOptions.TERM_SHORT, singleQuote, "input.txt"});
		ProcessingResult result = TEST_CALCULATOR.splitIntoTerms(
				String.format("'%s'", singleQuote), options);
		Assert.assertEquals(result.getTermMatchCount(), 1L);
		Assert.assertEquals(result.getAllTermsCount(), 1L);

		final String doubleQuote = "don\"t";
		options = new CLIOptions(new String[]{"-" + CLIOptions.TERM_SHORT, doubleQuote, "input.txt"});
		result = TEST_CALCULATOR.splitIntoTerms(
				String.format("\"%s\"", doubleQuote), options);
		Assert.assertEquals(result.getTermMatchCount(), 1L);
		Assert.assertEquals(result.getAllTermsCount(), 1L);

		final String dash = "one-two";
		options = new CLIOptions(new String[]{"-" + CLIOptions.TERM_SHORT, dash, "input.txt"});
		result = TEST_CALCULATOR.splitIntoTerms(
				String.format("-%s-", dash), options);
		Assert.assertEquals(result.getTermMatchCount(), 1L);
		Assert.assertEquals(result.getAllTermsCount(), 1L);

		Assert.assertEquals(EnglishTermFrequencyCalculator.TERM_TRIMMER_CHARS.length(), 3L,
				"EnglishTermFrequencyCalculator.TERM_TRIMMER was updated, but this test was not.");
	}

	/** Tests that terms created by prefixing/suffixing a term don't count to be an original term. */
	@Test
	public void testAddedToTermCalculation() throws ParseException {
		final CLIOptions options = new CLIOptions(new String[]{"-" + CLIOptions.TERM_SHORT, TEST_TERM, "input.txt"});
		final ProcessingResult result = TEST_CALCULATOR.splitIntoTerms(
				String.format("%s %s %s", "not" + TEST_TERM, TEST_TERM, TEST_TERM + "not"), options);
		Assert.assertEquals(result.getTermMatchCount(), 1L);
		Assert.assertEquals(result.getAllTermsCount(), 3L);
	}

	/** Tests verbose all terms occurrence recording. */
	@Test
	public void testVerboseTermsOccurrenceRecording() throws ParseException {
		final CLIOptions options = new CLIOptions(
				new String[]{"-" + CLIOptions.TERM_SHORT, TEST_TERM, "-" + CLIOptions.VERBOSE_SHORT, "input.txt"});
		final ProcessingResult result = TEST_CALCULATOR.splitIntoTerms(
				String.format("%1$s %2$s %2$s %3$s %3$s %3$s", "one", "two", "three"), options);
		Assert.assertEquals(result.getEveryTermCount().get().get("one").longValue(), 1L);
		Assert.assertEquals(result.getEveryTermCount().get().get("two").longValue(), 2L);
		Assert.assertEquals(result.getEveryTermCount().get().get("three").longValue(), 3L);
	}
}
