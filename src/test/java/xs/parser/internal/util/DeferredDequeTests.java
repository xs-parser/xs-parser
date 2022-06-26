package xs.parser.internal.util;

import java.util.*;
import org.junit.*;

public class DeferredDequeTests {

	@Test
	public void testAddFirst() {
		final Deque<String> def = new DeferredArrayDeque<>();
		def.addFirst("123");
		def.addFirst("234");
		Assert.assertEquals(2, def.size());
		Assert.assertFalse(def.isEmpty());
		Assert.assertEquals("234", def.getFirst());
		Assert.assertEquals("123", def.getLast());
		Assert.assertEquals("234", def.removeFirst());
		Assert.assertEquals(1, def.size());
		Assert.assertEquals("123", def.getFirst());
		Assert.assertEquals("123", def.removeFirst());
		Assert.assertTrue(def.isEmpty());
	}

}
