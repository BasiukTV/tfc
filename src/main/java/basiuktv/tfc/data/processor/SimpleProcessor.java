package basiuktv.tfc.data.processor;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import basiuktv.tfc.data.fetchers.InputDataFetcher;
import basiuktv.tfc.launcher.CLIOptions;
import basiuktv.tfc.text.EnglishTermFrequencyCalculator;
import basiuktv.tfc.util.Logging;

/**
 * InputDataProcessor which processes list of work sequentially and using one thread.
 * Slow, but can be used as a good benchmark.
 * 
 * @author Taras Basiuk
 */
public class SimpleProcessor implements InputDataProcessor {

	/**
	 * Processes a list of InputDataFetcher and produces combined ProcessingResult.
	 * 
	 * @param work List of allocated InputDataFetcher.
	 * @param options Parsed command line arguments.
	 * @return Combined ProcessingResult.
	 * @throws IOException when problems with fetching data occur.
	 */
	public ProcessingResult processWork(
			final List<InputDataFetcher> work,
			final CLIOptions options) throws IOException {
		ProcessingResult combinedResult = new ProcessingResult(0, 0, Optional.empty());
		for (final InputDataFetcher fetcher : work) {
			long startTime = System.currentTimeMillis();
			final ProcessingResult partialResult = new EnglishTermFrequencyCalculator()
					.splitIntoTerms(fetcher.fetchData(), options);
			if (options.isVerbose()) {
				Logging.logLatency(String.format("Calculating terms for %s", fetcher), startTime);
				startTime = System.currentTimeMillis();
			}

			combinedResult = ProcessingResult.combineResults(combinedResult, partialResult);
			if (options.isVerbose()) {
				Logging.logLatency(String.format("Combining results for %s", fetcher), startTime);
			}
		}

		return combinedResult;
	}
}
