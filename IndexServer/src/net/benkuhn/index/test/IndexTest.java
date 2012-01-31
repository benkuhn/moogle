package net.benkuhn.index.test;

import java.util.Map;

import net.benkuhn.index.Alignment;
import net.benkuhn.index.Index;
import net.benkuhn.index.Match;

import junit.framework.TestCase;

public class IndexTest extends TestCase {
	Index index;
	String id1 = "salutation";
	int[] notes1 = {31, 33, 35, 36, 31, 28, 26, 24, 26, 28, 26, 19, 19,
			21, 23, 24, 21, 19, 17, 16, 17, 19, 14, 26, 28, 29, 33, 21,
			23, 24, 23, 26, 26, 28, 29, 31, 28, 28, 26, 24, 24, 31, 33,
			35, 36, 29, 28, 29, 31, 33, 31, 33, 35, 26, 29, 31, 33, 35,
			36, 33, 35, 31, 33, 29, 31, 26, 26, 28, 29, 28, 26, 28, 24,
			21, 23, 24, 23, 31, 31, 33, 35, 33, 31, 33, 36, 31, 28, 26,
			24, 24};
	int[] search = {31, 33, 35, 36, 31, 28, 26, 24, 26, 28, 26};
	String id2 = "other";
	int[] notes2 = {21, 22, 24, 26, 24, 21, 24, 29, 31, 33, 31, 33, 36,
			29, 29, 28, 26, 28, 29, 26, 24, 21, 17, 21, 19, 19, 21, 19,
			21, 22, 24, 26, 24, 21, 24, 29, 31, 33, 31, 33, 36, 29, 29,
			28, 26, 28, 29, 26, 24, 21, 19, 21, 17, 17, 19, 17, 29, 31,
			33, 31, 33, 34, 36, 36, 24, 26, 24, 26, 28, 29, 24, 26, 28,
			29, 26, 24, 21, 17, 21, 19, 19, 21, 19, 29, 31, 33, 31, 33,
			34, 36, 36, 24, 26, 24, 26, 28, 29, 24, 26, 28, 29, 26, 24,
			21, 19, 21, 17, 17, 19, 17};
	protected void setUp() throws Exception {
		super.setUp();
		
		index = new Index(40, 4);
		index.add(id1, notes1);
		index.add(id2, notes2);
	}
	
	public void testIndex() {
		assertNotNull(index.searchNGram(new int[] {1, 3, 5, 6, 1}));
	}
	
	public void testSearch() {
		index.add(id1, notes1);
		Map<String, Match> results1 = index.getPreliminaryMatches(search);
		assertTrue(results1.containsKey(id1));
		Match m = results1.get(id1);
		Alignment a = index.align(m.computeOffset(), m);
		System.out.print(a);
	}
}
