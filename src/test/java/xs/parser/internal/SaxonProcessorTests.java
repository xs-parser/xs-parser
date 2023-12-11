package xs.parser.internal;

import org.junit.*;

public class SaxonProcessorTests {

	@Test
	public void testSaxonIsLoaded() {
		Assert.assertTrue(SaxonProcessor.IS_SAXON_LOADED);
	}

}
