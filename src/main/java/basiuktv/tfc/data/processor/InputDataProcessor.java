package basiuktv.tfc.data.processor;

import java.io.IOException;
import java.util.List;

import basiuktv.tfc.data.fetchers.InputDataFetcher;
import basiuktv.tfc.launcher.CLIOptions;

/**
 * Describes an object capable of processing list of InputDataFetcher and producing the combined ProcessingResult.
 * 
 * @author Taras Basiuk
 */
public interface InputDataProcessor {

	/**
	 * Processes a list of InputDataFetcher and produces combined ProcessingResult.
	 * 
	 * @param work List of allocated InputDataFetcher.
	 * @param options Parsed command line arguments.
	 * @return Combined ProcessingResult.
	 * @throws IOException when problems with fetching data occur.
	 */
	public abstract ProcessingResult processWork(
			final List<InputDataFetcher> work,
			final CLIOptions options) throws IOException;
}
