package basiuktv.tfc.util;

/**
 * Helper methods used for logging.
 * 
 * @author Taras Basiuk
 */
public class Logging {

	/**
	 * Logs an error.
	 *
	 * @param description String describing error being logged.
	 */
	public static void logError(final String description, final Exception e) {
		System.out.println(String.format("ERROR : %s REASON: %s", description, e.getMessage()));
	}

	/**
	 * Logs an info message.
	 *
	 * @param description String describing info being logged.
	 */
	public static void logInfo(final String description) {
		System.out.println(String.format("INFO : %s", description));
	}

	/**
	 * Logs latency from previously recorded time.
	 * 
	 * @param description String describing latency being logged.
	 * @param startTime Previously recorded start time.
	 */
	public static void logLatency(final String description, final long startTime) {
		System.out.println(String.format("%s : %d ms.", description, System.currentTimeMillis() - startTime));
	}

}
