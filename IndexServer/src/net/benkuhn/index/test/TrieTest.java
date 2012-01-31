package net.benkuhn.index.test;
import java.util.Set;
import java.util.TreeSet;

import net.benkuhn.index.Factory;
import net.benkuhn.index.Trie;

import junit.framework.TestCase;


public class TrieTest extends TestCase {
	
	Trie<Set<String>> trie;
	protected void setUp() throws Exception {
		super.setUp();
		
		trie = new Trie<Set<String>>(4, 31);
	}
	
	public void test() {
		int[] k1 = {1, 1, 1, 2};
		int[] k2 = {1, 15, 12, 3};
		Set<String> v2 = new TreeSet<String>();
		int[] k3 = {30, 12, 1, 8};
		int[] k4 = {0, 0, 0, 0};
		Set<String> v4 = new TreeSet<String>();
		int[] k5 = {30, 30, 30, 30};
		Set<String> v5 = new TreeSet<String>();
		assertNull(trie.get(k1));
		assertNull(trie.get(k2));
		assertNull(trie.get(k3));
		assertNull(trie.get(k4));
		assertNull(trie.get(k5));
		trie.set(k2, v2);
		assertEquals(v2, trie.get(k2));
		trie.set(k4, v4);
		assertEquals(v4, trie.get(k4));
		trie.set(k5, v5);
		assertEquals(v5, trie.get(k5));
		trie.getWithDefault(k3, new Factory<Set<String>>() {
			public Set<String> make() {
				return new TreeSet<String>();
			}
		}).add("foobar");
		assertNotNull(trie.get(k3));
		assertTrue(trie.get(k3).contains("foobar"));
	}
}
