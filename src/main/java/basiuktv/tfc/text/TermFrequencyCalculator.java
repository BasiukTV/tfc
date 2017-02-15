package basiuktv.tfc.text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

import basiuktv.tfc.data.processor.ProcessingResult;
import basiuktv.tfc.launcher.CLIOptions;

/**
 * Contains common functionality for calculating term occurrence rate in a text.
 *
 * @author Taras Basiuk
 */
public abstract class TermFrequencyCalculator {

	/**
	 * Splits given text string into terms and records number of occurrences of a given term.
	 * Optionally, records occurrences of all the terms in the text.
	 * 
	 * @param text String to be split into terms.
	 * @param options CLIOptions containing the term in question (and verbosity setting).
	 * @return ProcessingResult
	 */
	public ProcessingResult splitIntoTerms(final String text, final CLIOptions options) {
		// Initialize fields for ProcessingResult with default values
		long allTermsCount = 0;
		long termCount = 0;
		final Map<String, Long> allTerms = new HashMap<String, Long>();

		// Split given text into terms following language specific rules. 
		for (final String s : this.getLanguageSpecificSplitter().split(text)) {
			allTermsCount++;

			// If current terms is a match, record this
			final String lowercaseS = s.toLowerCase();
			if (options.getTerm().equals(lowercaseS)) {
				termCount++;
			}

			// If this execution is verbose, record occurances of all the terms
			if (options.isVerbose()) {
				if (!allTerms.containsKey(lowercaseS)) {
					allTerms.put(lowercaseS, 1L);
					continue;
				}

				allTerms.put(lowercaseS, allTerms.get(lowercaseS) + 1);
			}
		}

		// Package and return calculated occurrences into ProcessingResult
		return new ProcessingResult(
				termCount, allTermsCount, options.isVerbose() ? Optional.of(allTerms) : Optional.empty());
	}

	/**
	 * Returns language-specific text into term splitter.
	 *
	 * @return Splitter
	 */
	public abstract Splitter getLanguageSpecificSplitter();

	/**
	 * Returns language-specific text into term separator.
	 *
	 * @return CharMatcher
	 */
	public abstract CharMatcher getLanguageSpecificTermSeparator();
}
