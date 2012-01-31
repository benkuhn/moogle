package net.benkuhn.index.test;

import net.benkuhn.index.Listener;
import junit.framework.TestCase;

public class ListenerTest extends TestCase {
	public void testArrays() {
		String text = "1 1 2 3 5 8 13 21 34 55";
		int[] check = {1, 1, 2, 3, 5, 8, 13, 21, 34, 55};
		int[] result = Listener.parseArray(text);
		for(int i = 0; i < check.length; i++) {
			assertEquals(check[i], result[i]);
		}
		assertEquals(check.length, result.length);
	}
}
