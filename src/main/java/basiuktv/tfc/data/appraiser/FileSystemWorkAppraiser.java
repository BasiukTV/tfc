package basiuktv.tfc.data.appraiser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import basiuktv.tfc.data.fetchers.FileSystemDataFetcher;
import basiuktv.tfc.data.fetchers.InputDataFetcher;
import basiuktv.tfc.launcher.CLIOptions;
import basiuktv.tfc.text.TermFrequencyCalculator;

/**
 * Checks input files and directories and splits them into list of roughly equal InputDataFetcher to be processed.
 * 
 * @author Taras Basiuk
 */
public class FileSystemWorkAppraiser {

	private static final Set<String> SUPPORTED_FILE_TYPES = Sets.newHashSet("text/plain");

	/** Default desired size of one chunk of work. */
	public static final int DEFAULT_DESIRED_WORK_SIZE = 50 * 1024 * 1024; // 50 MB

	/** Default max number of characters in which we expect to find at least one term separator. */
	public static final int DEFAULT_MAX_NEXT_SEPARATOR_DISTANCE = 100;

	private final int desiredWorkSize;
	private final int maxNextSeparatorDistance;
	private final TermFrequencyCalculator termFrequencyCalculator;

	/**
	 * Default constructor.
	 *
	 * @param desiredWorkSize Desired one chunk of work size.
	 * @param maxNextSeparatorDistance Max number of characters in which we expect to find at least one term separator.
	 * @param termFrequencyCalculator Language-specific TermFrequencyCalculator.
	 */
	public FileSystemWorkAppraiser(
			final int desiredWorkSize,
			final int maxNextSeparatorDistance,
			final TermFrequencyCalculator termFrequencyCalculator) {
		Preconditions.checkArgument(desiredWorkSize > 0, "desiredWorkSize must be positive.");
		Preconditions.checkArgument(maxNextSeparatorDistance > 0, "maxNextSeparatorDistance must be positive.");
		this.desiredWorkSize = desiredWorkSize;
		this.maxNextSeparatorDistance = maxNextSeparatorDistance;
		this.termFrequencyCalculator = Preconditions.checkNotNull(
				termFrequencyCalculator, "maxNextSeparatorDistance must not be null.");
	}

	/**
	 * Checks input files and directories and splits them into list of roughly equal InputDataFetcher to be processed.
	 *
	 * @param cliOptions Parsed CLI arguments.
	 * @return List of roughly equal in size InputDataFetcher.
	 */
	public List<InputDataFetcher> appraiseWork(final CLIOptions cliOptions) {
		final List<File> files = FileSystemWorkAppraiser.collectInputFiles(cliOptions);
		final List<InputDataFetcher> result = new LinkedList<InputDataFetcher>();
		files.stream().forEach(f -> {
			long offset = 0;
			long workSize = f.length();

			try (final FileInputStream inputStream = new FileInputStream(f)) {
				final byte[] separatorRange = new byte[this.maxNextSeparatorDistance];
				// While remaining file doesn't fit into this.desiredWorkSize + this.maxNextSeparatorDistance
				while (offset + this.desiredWorkSize + this.maxNextSeparatorDistance < workSize) {
					inputStream.skip(this.desiredWorkSize);
					inputStream.read(separatorRange, 0, this.maxNextSeparatorDistance);
					// Find next index of term-separating character for given language
					final int separatorIndex = this.termFrequencyCalculator.getLanguageSpecificTermSeparator()
							.indexIn(new String(separatorRange, Charsets.UTF_8));
					if (separatorIndex == -1) {
						throw new RuntimeException(String.format(
								"Didn't find a term separator within %d characters of %s file.",
								this.maxNextSeparatorDistance,
								f.getPath()));
					}

					// Record this chunk
					long limit = offset + this.desiredWorkSize + separatorIndex;
					result.add(new FileSystemDataFetcher(f.getPath(), offset, limit));
					offset = limit;
					inputStream.skip(separatorIndex - this.maxNextSeparatorDistance);
				}
			} catch (IOException e) {
				throw new RuntimeException(
						String.format("Error occurred while splitting file %s into smaller segments. Cause: %s",
								f.getPath(), e.getMessage()));
			}

			// Add tail segment of work on the file
			result.add(new FileSystemDataFetcher(f.getPath(), offset, workSize));
		});

		return result;
	}

	/**
	 * Extracts, discovers and checks input data files from parsed command line options.
	 * 
	 * @param cliOptions CLIOptions constructed from command line arguments.
	 * @return List of files to extract input data from.
	 */
	@VisibleForTesting
	protected static List<File> collectInputFiles(final CLIOptions cliOptions) {
		final List<File> files = new LinkedList<File>();

		// Go one level deep into provided directory, collect files in it, apply wildcard if provided.
		if (cliOptions.getDirectory().isPresent()) {
			FileUtils.listFiles(
					new File(cliOptions.getDirectory().get()),
					new WildcardFileFilter(cliOptions.getWildcard().orElse("*")),
					null /* Do not recursively inspect directories */ )
				.stream().forEach(f -> files.add(f));
		}

		// Check that files explicitly provided in command line arguments actually exist, if so, collect them.
		cliOptions.getAdditionalFiles().stream().forEach(s -> {
			final File f = new File(s);
			if (!f.exists()) {
				throw new IllegalArgumentException(String.format("%s file doesn't appear to exist.", f));
			}

			files.add(f);
		});

		// Check that collected files are of supported types (by content probing).
		if (!cliOptions.isSkipTypeCheck()) {
			files.stream().forEach(f -> {
				try {
					final String type = Files.probeContentType(f.toPath());
					if (!SUPPORTED_FILE_TYPES.contains(type)) {
						throw new IllegalArgumentException(String.format(
								"%s is a file of unsupported type %s. Supported types: %s",
								f.getPath(),
								type,
								SUPPORTED_FILE_TYPES));
					}
				} catch (IOException e) {
					throw new RuntimeException(String.format(
							"Content type probing for the file %s failed. Reason: %s", f.getPath(), e.getMessage()));
				}
			});
		}

		return files;
	}
}
