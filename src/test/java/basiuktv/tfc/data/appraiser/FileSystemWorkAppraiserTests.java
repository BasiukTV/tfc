package basiuktv.tfc.data.appraiser;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import basiuktv.tfc.data.fetchers.InputDataFetcher;
import basiuktv.tfc.launcher.CLIOptions;
import basiuktv.tfc.text.EnglishTermFrequencyCalculator;
import basiuktv.tfc.text.TermFrequencyCalculator;

/** Tests for FileSystemWorkAppraiser. */
public class FileSystemWorkAppraiserTests {

	private static final String TOP_LEVEL_DATA_FILE_PATH = "src\\test\\data\\randomterm1.txt";
	private static final String TOP_DIR_DATA_FILE_PATH = "src\\test\\data\\dir\\randomterm2.txt";
	private static final String SUB_DIR_DATA_FILE_PATH = "src\\test\\data\\dir\\subdir\\randomterm2.txt";
	private static final String NOT_TEXT_FILE_PATH = "src\\test\\data\\dir\\nottext.jpg";
	private static final String WILDCARD = "randomterm*.txt";

	private static final String TOP_DIR_PATH = "src\\test\\data\\dir";
	private static final String SUB_DIR_PATH = "src\\test\\data\\dir\\subdir";

	private static final String SMALL_FILE = "src\\test\\data\\smallrandom.txt";
	private static final String NO_SEPARATORS_FILE = "src\\test\\data\\noseparators.txt";

	@Test
	public void testConstructor() {
		exceptionCatchingConstructorWrapper(0, 100, new EnglishTermFrequencyCalculator());
		exceptionCatchingConstructorWrapper(100, 0, new EnglishTermFrequencyCalculator());
		exceptionCatchingConstructorWrapper(100, 100, null);
	}

	private void exceptionCatchingConstructorWrapper(
			final int workSize, final int nextSeparatorDistance, final TermFrequencyCalculator tfc) {
		try {
			new FileSystemWorkAppraiser(workSize, nextSeparatorDistance, tfc);
			Assert.fail("Exception throw was expected by now.");
		} catch (IllegalArgumentException | NullPointerException e) {
			// All is good
		}
	}

	/** Tests only collecting files explicitly provided as command line arguments. */
	@Test
	public void testCollectOnlyExplicitlyProvidedFiles() throws ParseException {
		final CLIOptions options = new CLIOptions(new String[]{
				"-" + CLIOptions.TERM_SHORT, "test",
				TOP_LEVEL_DATA_FILE_PATH, TOP_DIR_DATA_FILE_PATH});
		final List<File> files = FileSystemWorkAppraiser.collectInputFiles(options);
		final List<String> filePaths = files.stream().map(f -> f.getPath()).collect(Collectors.toList());
		Assert.assertTrue(filePaths.contains(TOP_LEVEL_DATA_FILE_PATH));
		Assert.assertTrue(filePaths.contains(TOP_DIR_DATA_FILE_PATH));
		Assert.assertEquals(filePaths.size(), 2);
	}

	/** Tests only collecting files in directory provided as command line argument. */
	@Test
	public void testCollectOnlyDirectoryProvidedFiles() throws ParseException {
		final CLIOptions options = new CLIOptions(new String[]{
				"-" + CLIOptions.TERM_SHORT, "test",
				"-" + CLIOptions.DIRECTORY_SHORT, SUB_DIR_PATH});
		final List<File> files = FileSystemWorkAppraiser.collectInputFiles(options);
		final List<String> filePaths = files.stream().map(f -> f.getPath()).collect(Collectors.toList());
		Assert.assertTrue(filePaths.contains(SUB_DIR_DATA_FILE_PATH));
		Assert.assertEquals(filePaths.size(), 1);
	}

	/** Tests collecting files in directory provided as a command line argument with a wildcard. */
	@Test
	public void testCollectWildCardDirectoryFiles() throws ParseException {
		final CLIOptions options = new CLIOptions(new String[]{
				"-" + CLIOptions.TERM_SHORT, "test",
				"-" + CLIOptions.DIRECTORY_SHORT, TOP_DIR_PATH,
				"-" + CLIOptions.WILDCARD_SHORT, WILDCARD});
		final List<File> files = FileSystemWorkAppraiser.collectInputFiles(options);
		final List<String> filePaths = files.stream().map(f -> f.getPath()).collect(Collectors.toList());
		Assert.assertTrue(filePaths.contains(TOP_DIR_DATA_FILE_PATH));
		Assert.assertEquals(filePaths.size(), 1);
	}

	/** Tests collecting files provided explicitly and provided as a directory in command line arguments. */
	@Test
	public void testCollectExplicitlyAndDirectoryProvidedFiles() throws ParseException {
		final CLIOptions options = new CLIOptions(new String[]{
				"-" + CLIOptions.TERM_SHORT, "test",
				"-" + CLIOptions.DIRECTORY_SHORT, SUB_DIR_PATH,
				TOP_LEVEL_DATA_FILE_PATH});
		final List<File> files = FileSystemWorkAppraiser.collectInputFiles(options);
		final List<String> filePaths = files.stream().map(f -> f.getPath()).collect(Collectors.toList());
		Assert.assertTrue(filePaths.contains(TOP_LEVEL_DATA_FILE_PATH));
		Assert.assertTrue(filePaths.contains(SUB_DIR_DATA_FILE_PATH));
		Assert.assertEquals(filePaths.size(), 2);
	}

	/** Tests collecting explicitly provided not existing file. */
	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testExplicitlyProvidedNotExistingFile() throws ParseException {
		final CLIOptions options = new CLIOptions(new String[]{
				"-" + CLIOptions.TERM_SHORT, "test",
				"thisfiledoesntexist.log"});
		FileSystemWorkAppraiser.collectInputFiles(options);
		Assert.fail("IllegalArgumentException was expected by now.");
	}

	/** Tests collecting non-text file. */
	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testCollectingNonTextFile() throws ParseException {
		final CLIOptions options = new CLIOptions(new String[]{
				"-" + CLIOptions.TERM_SHORT, "test",
				NOT_TEXT_FILE_PATH});
		FileSystemWorkAppraiser.collectInputFiles(options);
		Assert.fail("IllegalArgumentException was expected by now.");
	}

	/** Tests splitting small file into even smaller chunks. */
	@Test
	public void testAppraiseWorkSplitting() throws ParseException {
		final FileSystemWorkAppraiser testAppraiser =
				new FileSystemWorkAppraiser(10, 10, new EnglishTermFrequencyCalculator());
		final CLIOptions options = new CLIOptions(new String[]{
				"-" + CLIOptions.TERM_SHORT, "test",
				SMALL_FILE}); // "Mammoth one that but hello leapt more provident."
		final List<InputDataFetcher> resultingFetchers = testAppraiser.appraiseWork(options);
		// 0 -> 'Mammoth one' -> 11 -> ' that but hello' -> 26 -> ' leapt more' -> 37 -> ' provident.' -> 48
		Assert.assertEquals(resultingFetchers.get(0).getOffset(), new Long(0));
		Assert.assertEquals(resultingFetchers.get(1).getOffset(), new Long(11));
		Assert.assertEquals(resultingFetchers.get(2).getOffset(), new Long(26));
		Assert.assertEquals(resultingFetchers.get(3).getOffset(), new Long(37));
		Assert.assertEquals(resultingFetchers.get(0).getLimit(), new Long(11));
		Assert.assertEquals(resultingFetchers.get(1).getLimit(), new Long(26));
		Assert.assertEquals(resultingFetchers.get(2).getLimit(), new Long(37));
		Assert.assertEquals(resultingFetchers.get(3).getLimit(), new Long(48));
	}

	/** Tests splitting small file with no term separators. */
	@Test(expectedExceptions={RuntimeException.class})
	public void testSplittingNoSeparators() throws ParseException {
		final FileSystemWorkAppraiser testAppraiser =
				new FileSystemWorkAppraiser(10, 10, new EnglishTermFrequencyCalculator());
		final CLIOptions options = new CLIOptions(new String[]{
				"-" + CLIOptions.TERM_SHORT, "test",
				NO_SEPARATORS_FILE}); // "Mammothonethatbuthelloleaptmoreprovident."
		testAppraiser.appraiseWork(options);
		Assert.fail("RuntimeException was expected.");
	}
}
