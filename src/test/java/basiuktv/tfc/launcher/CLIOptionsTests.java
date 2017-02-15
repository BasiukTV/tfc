package basiuktv.tfc.launcher;

import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Tests for CLIOptions. */
public class CLIOptionsTests {

	private final static String TEST_TERM = "test";
	private final static String TEST_DIRECTORY = "input";
	private final static String TEST_WILDCARD = "*.txt";
	private final static String TEST_FILE_0 = "file0.txt";
	private final static String TEST_FILE_1 = "file1.txt";

	/** Tests parsing minimally required amount of CLI arguments passed. */
	@Test
	public void testMinimalArgumentsParsing() throws ParseException {
		final String[] testArgs = {"-" + CLIOptions.TERM_SHORT, TEST_TERM, TEST_FILE_0};

		final CLIOptions testOptions = new CLIOptions(testArgs);
		Assert.assertEquals(testOptions.getTerm(), TEST_TERM);
		Assert.assertFalse(testOptions.isHelpRequest());
		Assert.assertFalse(testOptions.getDirectory().isPresent());
		Assert.assertFalse(testOptions.getWildcard().isPresent());
		Assert.assertFalse(testOptions.isSkipTypeCheck());
		Assert.assertFalse(testOptions.isVerbose());
		Assert.assertEquals(testOptions.getAdditionalFiles().get(0), TEST_FILE_0);
	}

	/** Tests parsing all supported (non-help) CLI arguments passed via a short flag. */
	@Test
	public void testCLIShortFlagsArgumentsParsing() throws ParseException {
		final String[] testArgs = {
				"-" + CLIOptions.TERM_SHORT, TEST_TERM,
				"-" + CLIOptions.DIRECTORY_SHORT, TEST_DIRECTORY,
				"-" + CLIOptions.WILDCARD_SHORT, TEST_WILDCARD,
				"-" + CLIOptions.SKIP_TYPE_CHECK_SHORT,
				"-" + CLIOptions.VERBOSE_SHORT,
				TEST_FILE_0, TEST_FILE_1};

		final CLIOptions testOptions = new CLIOptions(testArgs);
		Assert.assertEquals(testOptions.getTerm(), TEST_TERM);
		Assert.assertEquals(testOptions.getDirectory().get(), TEST_DIRECTORY);
		Assert.assertEquals(testOptions.getWildcard().get(), TEST_WILDCARD);
		Assert.assertTrue(testOptions.isSkipTypeCheck());
		Assert.assertTrue(testOptions.isVerbose());
		Assert.assertEquals(testOptions.getAdditionalFiles().get(0), TEST_FILE_0);
		Assert.assertEquals(testOptions.getAdditionalFiles().get(1), TEST_FILE_1);
	}

	/** Tests parsing all supported (non-help) CLI arguments passed via a long flag. */
	@Test
	public void testCLILongFlagsArgumentsParsing() throws ParseException {
		final String[] testArgs = {
				"--" + CLIOptions.TERM_LONG, TEST_TERM,
				"--" + CLIOptions.DIRECTORY_LONG, TEST_DIRECTORY,
				"--" + CLIOptions.WILDCARD_LONG, TEST_WILDCARD,
				"--" + CLIOptions.SKIP_TYPE_CHECK_LONG,
				"--" + CLIOptions.VERBOSE_LONG,
				TEST_FILE_0, TEST_FILE_1};

		final CLIOptions testOptions = new CLIOptions(testArgs);
		Assert.assertEquals(testOptions.getTerm(), TEST_TERM);
		Assert.assertEquals(testOptions.getDirectory().get(), TEST_DIRECTORY);
		Assert.assertEquals(testOptions.getWildcard().get(), TEST_WILDCARD);
		Assert.assertTrue(testOptions.isSkipTypeCheck());
		Assert.assertTrue(testOptions.isVerbose());
		Assert.assertEquals(testOptions.getAdditionalFiles().get(0), TEST_FILE_0);
		Assert.assertEquals(testOptions.getAdditionalFiles().get(1), TEST_FILE_1);
	}

	/** Tests that missing term CLI argument causes an IllegalArgumentException being thrown. */
	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testMissingTermArgumentCausesException() throws ParseException {
		final String[] testArgs = {
				"-" + CLIOptions.DIRECTORY_SHORT, TEST_DIRECTORY,
				"-" + CLIOptions.WILDCARD_SHORT, TEST_WILDCARD,
				"-" + CLIOptions.SKIP_TYPE_CHECK_SHORT,
				"-" + CLIOptions.VERBOSE_SHORT,
				TEST_FILE_0, TEST_FILE_1};
		new CLIOptions(testArgs);
	}

	/** Tests that missing any sort of input data sources causes an IllegalArgumentException being thrown. */
	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testMissingInputDataSourcesCausesException() throws ParseException {
		final String[] testArgs = {
				"-" + CLIOptions.TERM_SHORT, TEST_TERM,
				"-" + CLIOptions.SKIP_TYPE_CHECK_SHORT,
				"-" + CLIOptions.VERBOSE_SHORT};
		new CLIOptions(testArgs);
	}

	/** Tests that requesting to print help message, prevents missing arguments related exceptions. */
	@Test
	public void testHelpRequestIgnoresExceptions() throws ParseException {
		final String[] testArgs = {"-" + CLIOptions.HELP_SHORT};
		final CLIOptions testOptions = new CLIOptions(testArgs);
		Assert.assertTrue(testOptions.isHelpRequest());
	}

	/** Tests that wildcard argument sets directory to current one, if it's not otherwise provided. */
	@Test
	public void testMissingDirectoryWithWildcard() throws ParseException {
		final String[] testArgs = {
				"-" + CLIOptions.TERM_SHORT, TEST_TERM,
				"-" + CLIOptions.WILDCARD_SHORT, TEST_WILDCARD};

		final CLIOptions testOptions = new CLIOptions(testArgs);
		Assert.assertEquals(testOptions.getDirectory().get(), ".");
	}
}
