package basiuktv.tfc.data.fetchers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * InputDataFetcher which fetches data from local (or looks-like-local) file system.
 */
public class FileSystemDataFetcher extends InputDataFetcher {

	/**
	 * Default constructor.
	 * 
	 * @param source Path to a file.
	 * @param offset Start position of the allocated data to process.
	 * @param limit End position (exclusive) of the allocated data to process.
	 */
	public FileSystemDataFetcher(final String source, final Long offset, final Long limit) {
		super(source, offset, limit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String fetchData() throws IOException {
		try (final FileInputStream inputStream = new FileInputStream(new File(this.getSource()))) {
			final byte[] data = new byte[(int) (this.getLimit() - this.getOffset())];
			inputStream.skip(this.getOffset());
			inputStream.read(data, 0, (int) (this.getLimit() - this.getOffset()));
			return new String(data, StandardCharsets.UTF_8);
		}
	}

}
