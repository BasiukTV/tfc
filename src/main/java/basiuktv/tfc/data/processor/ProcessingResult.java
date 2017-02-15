package basiuktv.tfc.data.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.Data;

/**
 * Represents result (in most cases partial) of term frequency calculation.
 *
 * @author Taras Basiuk
 */
@Data
public class ProcessingResult {

	private final long termMatchCount;
	private final long allTermsCount;
	private final Optional<Map<String, Long>> everyTermCount;

	/**
	 * Combines two processing results into one.
	 *
	 * @param r1 First ProcessingResult
	 * @param r2 Second ProcessingResult
	 * @return Combined ProcessingResult
	 */
	public static ProcessingResult combineResults(final ProcessingResult r1, final ProcessingResult r2) {
		Optional<Map<String, Long>> combinedEveryMatchCount = Optional.empty();

		// If either of results contains map of every match count, put smaller map into the larger one.
		if (r1.getEveryTermCount().isPresent() || r2.getEveryTermCount().isPresent()) {
			final Map<String, Long> m1 = r1.getEveryTermCount().orElse(new HashMap<String, Long>());
			final Map<String, Long> m2 = r2.getEveryTermCount().orElse(new HashMap<String, Long>());
			final Map<String, Long> larger = m1.size() > m2.size() ? m1 : m2;
			final Map<String, Long> smaller = m1.size() > m2.size() ? m2 : m1;
			for (final Map.Entry<String, Long> e : smaller.entrySet()) {
				if (larger.containsKey(e.getKey())) {
					larger.put(e.getKey(), larger.get(e.getKey()) + e.getValue());
					continue;
				}

				larger.put(e.getKey(), e.getValue());
			}

			combinedEveryMatchCount = Optional.of(larger);
		}

		return new ProcessingResult(
				r1.getTermMatchCount() + r2.getTermMatchCount(),
				r1.getAllTermsCount() + r2.getAllTermsCount(),
				combinedEveryMatchCount);
	}
}
