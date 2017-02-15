package basiuktv.tfc.data.fetchers;

import java.io.IOException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

/** Tests for FileSystemDataFetcher. */
public class FileSystemDataFetcherTests {

	private static final String SMALL_FILE = "src\\test\\data\\smallrandom.txt";

	/** Tests fetching whole file. */
	@Test
	public void wholeFileFetchTest() throws IOException {
		final FileSystemDataFetcher testFetcher = new FileSystemDataFetcher(
				SMALL_FILE, 0L, 48L); // "Mammoth one that but hello leapt more provident."
		Assert.assertEquals(testFetcher.fetchData(), "Mammoth one that but hello leapt more provident.");
	}

	/** Tests fetching a file part by part. */
	@Test
	public void partByPartFileFetchTest() throws IOException {
		final List<InputDataFetcher> testFetchers = Lists.newArrayList(
				new FileSystemDataFetcher(SMALL_FILE, 0L, 11L),
				new FileSystemDataFetcher(SMALL_FILE, 11L, 26L),
				new FileSystemDataFetcher(SMALL_FILE, 26L, 37L),
				new FileSystemDataFetcher(SMALL_FILE, 37L, 48L));

		final StringBuilder sb = new StringBuilder();
		for (InputDataFetcher idf : testFetchers) {
			sb.append(idf.fetchData());
		}

		Assert.assertEquals(sb.toString(), "Mammoth one that but hello leapt more provident.");
	}
}
