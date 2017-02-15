package basiuktv.tfc.text;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

/**
 * English-specific instance of TermFrequencyCalculator.
 *
 * @author Taras Basiuk
 */
public class EnglishTermFrequencyCalculator extends TermFrequencyCalculator {

	@VisibleForTesting protected static final String TERM_SEPARATOR_CHARS = " .,:;!?()\n\t\r";
	@VisibleForTesting protected static final String TERM_TRIMMER_CHARS = "-'\"";

	private static final CharMatcher TERM_SEPARATOR = CharMatcher.anyOf(TERM_SEPARATOR_CHARS);

	private static final Splitter TERM_SPLITTER = Splitter.on(TERM_SEPARATOR)
			.omitEmptyStrings()
			.trimResults(CharMatcher.anyOf(TERM_TRIMMER_CHARS));

	/** {@inheritDoc} */
	@Override
	public Splitter getLanguageSpecificSplitter() {
		return TERM_SPLITTER;
	}

	/** {@inheritDoc} */
	@Override
	public CharMatcher getLanguageSpecificTermSeparator() {
		return TERM_SEPARATOR;
	}

}
