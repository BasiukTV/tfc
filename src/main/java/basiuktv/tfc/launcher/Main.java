package basiuktv.tfc.launcher;

import java.io.IOException;
import java.util.List;

import basiuktv.tfc.data.appraiser.FileSystemWorkAppraiser;
import basiuktv.tfc.data.fetchers.InputDataFetcher;
import basiuktv.tfc.data.processor.ProcessingResult;
import basiuktv.tfc.data.processor.ThreadPoolExecutorProcessor;
import basiuktv.tfc.text.EnglishTermFrequencyCalculator;
import basiuktv.tfc.util.Logging;

/**
 * Prints occurrence frequency of specific term within given list of English
 * UTF-8 text documents.
 *
 * usage: java -jar tfc.jar -t TERM [OPTIONS] [FILES]
 * -d,--input-directory arg     File system directory containing input files
 *                              (sub-directories will not be inspected).
 * -h,--help                    Print this message.
 * -s,--skip-file-type-check    Skip input files type check (by content
 *                              probing). Use at your own risk.
 * -t,--term arg                Term to calculate occurence frequency for.
 * -v,--verbose                 Request additional information regarding
 *                              program execution.
 * -w,--wildcard arg            File name wildcard to be used for input file
 *                              discovery in provided directory (or current
 *                              one).
 * 
 * Usage examples :
 * java -jar tfc.jar -t term input_file1.txt input_file2.txt
 * java -jar tfc.jar -t term -d input -w *.txt
 * 
 * @author Taras Basiuk
 */
public class Main {

	public static void main(String[] args) throws IOException {

		// Parse command line arguments
		long startTime = System.currentTimeMillis();
		CLIOptions options = null;
		try {
			options = new CLIOptions(args);
		} catch (final Exception e) {
			System.out.println(String.format("ERROR encountered while parsing input arguments. %s\n", e.getMessage()));
			CLIOptions.printHelp();
			return;
		}

		// If only help was requested, print it and exit
		if (options.isHelpRequest()) {
			CLIOptions.printHelp();
			return;
		}

		if (options.isVerbose()) {
			Logging.logLatency("CLI arguments parsing", startTime);
			options.echoParsedOptions();
			startTime = System.currentTimeMillis();
		}

		// Appraise (prepare) work on input data
		List<InputDataFetcher> work = null;
		try {
			work = new FileSystemWorkAppraiser(
					FileSystemWorkAppraiser.DEFAULT_DESIRED_WORK_SIZE,
					FileSystemWorkAppraiser.DEFAULT_MAX_NEXT_SEPARATOR_DISTANCE,
					new EnglishTermFrequencyCalculator())
				.appraiseWork(options);
		} catch (final Exception e) {
			System.out.println(String.format("ERROR encountered while appraising work. %s", e.getMessage()));
			return;
		}

		if (options.isVerbose()) {
			Logging.logLatency("Work appraising", startTime);
			System.out.println("Work: " + work);
			startTime = System.currentTimeMillis();
		}

		// Process work
		ProcessingResult result = null;
		try {
			result = new ThreadPoolExecutorProcessor().processWork(work, options);
		} catch (final Exception e) {
			System.out.println(String.format("ERROR encountered while processing work. %s", e.getMessage()));
			return;
		}

		if (options.isVerbose()) {
			Logging.logLatency("Entire work processing", startTime);
			System.out.println("All matches count: " + result.getEveryTermCount().get());
		}

		// Print result
		System.out.println(String.format("%.10f", new Float(result.getTermMatchCount()) / result.getAllTermsCount()));
	}
}
