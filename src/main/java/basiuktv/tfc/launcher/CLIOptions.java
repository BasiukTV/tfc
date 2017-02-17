package basiuktv.tfc.launcher;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import lombok.Getter;

/**
 * Used to parse arguments passed to the program through the command line.
 * 
 * @author Taras Basiuk
 */
public class CLIOptions {

	/** Short flag for the argument expected by the program to print the usage information and halt. */
	public static final String HELP_SHORT = "h";
	/** Long flag for the argument expected by the program to print the usage information and halt. */
	public static final String HELP_LONG = "help";

	/** Short flag for the argument indicating the term for which the occurrence frequency is being calculated. */
	public static final String TERM_SHORT = "t";
	/** Long flag for the argument indicating the term for which the occurrence frequency is being calculated. */
	public static final String TERM_LONG = "term";

	/** Short flag for the argument indicating the directory path in which the input text files are located. */
	public static final String DIRECTORY_SHORT = "d";
	/** Long flag for the argument indicating the directory path in which the input text files are located. */
	public static final String DIRECTORY_LONG = "input-directory";

	/**
	 * Short flag for the argument indicating the wildcard to be used to filter the input text files
	 * in the given (or current) directory.
	 */
	public static final String WILDCARD_SHORT = "w";
	/**
	 * Long flag for the argument indicating the wildcard to be used to filter the input text files
	 * in the given (or current) directory.
	 */
	public static final String WILDCARD_LONG = "wildcard";

	/** Short flag for the argument expected by the program to skip the input file type check. */
	public static final String SKIP_TYPE_CHECK_SHORT = "s";
	/** Long flag for the argument expected by the program to skip the input file type check. */
	public static final String SKIP_TYPE_CHECK_LONG = "skip-file-type-check";

	/** Short flag for the argument expected by the program to be verbose. */
	public static final String VERBOSE_SHORT = "v";
	/** Long flag for the argument expected by the program to be verbose. */
	public static final String VERBOSE_LONG = "verbose";

	private static final String HELP_MESSAGE_USAGE_PREFIX = "java -jar tfc.jar";
	private static final String HELP_MESSAGE_USAGE_SUFIX = " -t TERM [OPTIONS] [FILES]";
	private static final String HELP_MESSAGE_HEADER =
			"\nPrints occurrence frequency of specific term within given list of English UTF-8 text documents.\n\n";

	private static final String USAGE_EXAMPLES = String.format(
			"\nUsage examples :"
			+ "\n%1$s -%2$s term input_file1.txt input_file2.txt"
			+ "\n%1$s -%2$s term -%3$s input -%4$s *.txt",
			HELP_MESSAGE_USAGE_PREFIX, TERM_SHORT, DIRECTORY_SHORT, WILDCARD_SHORT);

	private static final Options OPTIONS = new Options();

	static {{
		// Compile all accepted/expected by the program CLI arguments below
		OPTIONS.addOption(HELP_SHORT, HELP_LONG, false,
				"Print this message.");
		OPTIONS.addOption(TERM_SHORT, TERM_LONG, true,
				"Term to calculate occurence frequency for.");
		OPTIONS.addOption(DIRECTORY_SHORT, DIRECTORY_LONG, true,
				"File system directory containing input files (sub-directories will not be inspected).");
		OPTIONS.addOption(WILDCARD_SHORT, WILDCARD_LONG, true,
				"File name wildcard to be used for input file discovery in provided directory (or current one).");
		OPTIONS.addOption(SKIP_TYPE_CHECK_SHORT, SKIP_TYPE_CHECK_LONG, false,
				"Skip input files type check (by content probing). Use at your own risk.");
		OPTIONS.addOption(VERBOSE_SHORT, VERBOSE_LONG, false,
				"Request additional information regarding program execution.");
	}}

	@Getter private boolean helpRequest;
	@Getter private String term;
	@Getter private Optional<String> directory;
	@Getter private Optional<String> wildcard;
	@Getter private boolean skipTypeCheck;
	@Getter private boolean verbose;
	@Getter private List<String> additionalFiles;

	/**
	 * Constructs CLIOptions instance.
	 *
	 * @param args Command line arguments program is being executed with.
	 * @throws ParseException If invalid arguments are provided by the caller.
	 */
	public CLIOptions(final String[] args) throws ParseException {
		// Initialize fields with default values
		this.helpRequest = false;
		this.directory = Optional.empty();
		this.wildcard = Optional.empty();
		this.skipTypeCheck = false;
		this.verbose = false;
		this.additionalFiles = new LinkedList<String>();

		final CommandLineParser parser = new DefaultParser();
		CommandLine line = parser.parse(OPTIONS, args); // throws ParseException

		if (line.hasOption(HELP_SHORT)) {
			this.helpRequest = true;
			return; // No need to continue parsing of just help message is requested.
		}

		if (!line.hasOption(TERM_SHORT)) {
			throw new IllegalArgumentException("Term argument is missing.");
		}

		this.term = line.getOptionValue(TERM_SHORT);

		if (!line.hasOption(DIRECTORY_SHORT) && !line.hasOption(WILDCARD_SHORT) && line.getArgList().isEmpty()) {
			throw new IllegalArgumentException(
					"At least one of either directory or wildcard or file(s) needed as input.\n");
		}

		if (line.hasOption(DIRECTORY_SHORT)) {
			this.directory = Optional.of(line.getOptionValue(DIRECTORY_SHORT));
		}

		if (line.hasOption(WILDCARD_SHORT)) {
			this.wildcard = Optional.of(line.getOptionValue(WILDCARD_SHORT));
			if (!this.directory.isPresent()) {
				// If input directory not specified by the user, set it to current directory
				this.directory = Optional.of(".");
			}
		}

		this.skipTypeCheck = line.hasOption(SKIP_TYPE_CHECK_SHORT);
		this.verbose = line.hasOption(VERBOSE_SHORT);

		// Unparsed arguments at this point assumed to be explicitly provided input files.
		this.additionalFiles = line.getArgList();
	}

	/** Prints the help message to the stdin. */
	public static void printHelp() {
		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(
				HELP_MESSAGE_USAGE_PREFIX + HELP_MESSAGE_USAGE_SUFIX,
				HELP_MESSAGE_HEADER,
				OPTIONS,
				USAGE_EXAMPLES, // Use manually prepared usage examples as help message footer
				false /* Do not auto-generate usage examples */);
	}

	@Override
	public String toString() {
		return String.format(
				"Following input arguments are in effect :\n"
				+ "Term : %s\n"
				+ "Directory : %s\n"
				+ "Wildcard : %s\n"
				+ "Skip file type check : %s\n"
				+ "Be verbose : %s\n"
				+ "Additional files : %s",
				this.getTerm(),
				this.getDirectory().orElse("NONE"),
				this.getWildcard().orElse("NONE"),
				this.isSkipTypeCheck(),
				this.isVerbose(),
				this.getAdditionalFiles().isEmpty() ? "NONE" : this.getAdditionalFiles());
	}
}
