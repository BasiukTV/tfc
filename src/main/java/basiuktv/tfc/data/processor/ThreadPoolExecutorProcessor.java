package basiuktv.tfc.data.processor;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

import basiuktv.tfc.data.fetchers.InputDataFetcher;
import basiuktv.tfc.launcher.CLIOptions;
import basiuktv.tfc.text.EnglishTermFrequencyCalculator;
import basiuktv.tfc.util.Logging;

/**
 * InputDataProcessor which processes list of work through fixed thread pool executor.
 * Works faster than single thread processing and limits memory consumption due to fixed maximum work chunk size
 * and number of threads in the pool.
 * 
 * @author Taras Basiuk
 */
public class ThreadPoolExecutorProcessor implements InputDataProcessor {

	private static final int THREAD_POOL_SIZE = 5;
	private static final int THREAD_POOL_EXECUTION_TIMEOUT_SEC = 120;

	// Combined result placeholder
	private ProcessingResult combinedResult;

	// Synchronizes updates to combinedResult
	private synchronized void updateCombinedResult(final ProcessingResult partialResult) {
		this.combinedResult = ProcessingResult.combineResults(combinedResult, partialResult);
	}

	/** Callable implementation responsible for processing single chunk of work. */
	private class ProcessingThread implements Callable<Boolean> {

		private final InputDataFetcher fetcher;
		private final CLIOptions options;

		private ProcessingThread(final InputDataFetcher fetcher, final CLIOptions options) {
			this.fetcher = Preconditions.checkNotNull(fetcher, "fetcher must not be null");
			this.options = Preconditions.checkNotNull(options, "options must not be null");
		}

		@Override
		public Boolean call() throws Exception {
			long startTime = System.currentTimeMillis();
			final ProcessingResult partialResult = new EnglishTermFrequencyCalculator()
					.splitIntoTerms(fetcher.fetchData(), options);
			if (options.isVerbose()) {
				Logging.logLatency(String.format("Calculating terms for %s", fetcher), startTime);
				startTime = System.currentTimeMillis();
			}

			updateCombinedResult(partialResult); // update combined result in thread-safe fashion
			if (options.isVerbose()) {
				Logging.logLatency(String.format("Combining results for %s", fetcher), startTime);
			}

			// We return boolean rather then ProcessingResult because later one can consume a lot of memory,
			// so we should release it now, rather then after all threads finish.
			return true;
		}
	}

	/**
	 * Processes a list of InputDataFetcher and produces combined ProcessingResult.
	 * 
	 * @param work List of allocated InputDataFetcher.
	 * @param options Parsed command line arguments.
	 * @return Combined ProcessingResult.
	 * @throws IOException when problems with fetching data occur.
	 */
	@Override
	public ProcessingResult processWork(List<InputDataFetcher> work, CLIOptions options) throws IOException {
		// Initialize combined result
		this.combinedResult = new ProcessingResult(0, 0, Optional.empty());

		// Record threads execution results
		final List<Future<Boolean>> executionSuccess = new LinkedList<Future<Boolean>>();

		// submit work to the thread pool
		final ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		work.stream().forEach(f -> {
			executionSuccess.add(pool.submit(new ProcessingThread(f, options)));
		});
		pool.shutdown(); // stop submitting work to the thread pool 

		// Wait for thread pool to finish
		try {
			pool.awaitTermination(THREAD_POOL_EXECUTION_TIMEOUT_SEC, TimeUnit.SECONDS);
		} catch (final InterruptedException e) {
			throw new RuntimeException(String.format(
					"Interrupted while waiting for thread pool to finish. Reason: %s", e.getMessage()));
		}

		// Check that no threads finished with throwing an exception
		executionSuccess.stream().forEach(ex -> {
			try {
				ex.get(); // We only record true(s), so no need to do further processing.
			} catch (InterruptedException e) {
				throw new RuntimeException(String.format(
						"One of the threads was interrupted while sleeping. Reason: %s", e.getMessage()));
			} catch (ExecutionException e) {
				throw new RuntimeException(String.format(
						"One of the threads threw an exception. Reason: %s", e.getMessage()));
			}
		});

		return combinedResult;
	}
}
