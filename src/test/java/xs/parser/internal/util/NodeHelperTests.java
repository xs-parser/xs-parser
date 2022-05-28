package xs.parser.internal.util;

import org.junit.*;

public class NodeHelperTests {

	@Test
	public void testCollapse() {
		Assert.assertEquals("", NodeHelper.collapseWhitespace(""));
		Assert.assertEquals("", NodeHelper.collapseWhitespace(" "));
		Assert.assertEquals("", NodeHelper.collapseWhitespace("\r"));
		Assert.assertEquals("", NodeHelper.collapseWhitespace("\r\n"));
		Assert.assertEquals("", NodeHelper.collapseWhitespace("\r\n\t"));
		Assert.assertEquals("", NodeHelper.collapseWhitespace("\r\n\t "));
		Assert.assertEquals("", NodeHelper.collapseWhitespace("    "));
		Assert.assertEquals("ABC", NodeHelper.collapseWhitespace("ABC"));
		Assert.assertEquals("A B C", NodeHelper.collapseWhitespace("A B C"));
		Assert.assertEquals("A B C D E F", NodeHelper.collapseWhitespace("\t A  B\rC\tD\r  \t \nE F\n"));
	}

}
