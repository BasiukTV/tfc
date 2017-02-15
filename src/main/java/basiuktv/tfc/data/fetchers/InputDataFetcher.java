package basiuktv.tfc.data.fetchers;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import lombok.Getter;

/**
 * Represents allocation of a chunk of data to be processed by the program.
 *
 * @author Taras Basiuk
 */
public abstract class InputDataFetcher {

	@Getter private final String source;
	@Getter private final Long offset;
	@Getter private final Long limit;

	/**
	 * Default constructor.
	 * 
	 * @param source String representation of the source of the data.
	 * @param offset Start position of the allocated data to process.
	 * @param limit End position (exclusive) of the allocated data to process.
	 */
	public InputDataFetcher(final String source, final Long offset, final Long limit) {
		if (StringUtils.isBlank(source)) {
			throw new IllegalArgumentException("Source cannot be blank or null.");
		}

		this.source = source;
		this.limit = Preconditions.checkNotNull(limit, "Limit may not be null.");
		this.offset = Preconditions.checkNotNull(offset, "Offset may not be null.");;
	}

	/**
	 * Actually fetches allocated data from the source.
	 *
	 * @return Allocated data to process.
	 * @throws IOException When data access problem occurs.
	 */
	public abstract String fetchData() throws IOException;

	@Override
	public String toString() {
		return String.format("%s, offset: %d, limit: %d", this.source, this.offset, this.limit);
	}
}
