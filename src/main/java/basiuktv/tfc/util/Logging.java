package basiuktv.tfc.util;

/**
 * Helper methods used for logging.
 * 
 * @author Taras Basiuk
 */
public class Logging {

	/**
	 * Logs latency from previously recorded time.
	 * 
	 * @param message String describing latency being logged.
	 * @param startTime Previously recorded start time.
	 */
	public static void logLatency(final String message, final long startTime) {
		System.out.println(String.format("%s : %d ms.", message, System.currentTimeMillis() - startTime));
	}
}
