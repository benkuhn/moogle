package net.benkuhn.index;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

public class Match implements Comparable<Match> {
	String name;
	int[] tune;
	Map<Integer, SortedSet<Integer>[]> hits;
	/// maps offset -> number of votes for that offset
	Map<Integer, Integer> counts = new TreeMap<Integer, Integer>();
	int nhits = 0;
	int len;
	public Match(String name, int len, int[] tune) {
		this.name = name;
		this.hits = new TreeMap<Integer, SortedSet<Integer>[]>();
		this.len = len;
		this.tune = tune;
	}
	//WARNING: unsafe to put multiple things to the same num currently
	//TODO optimize
	@SuppressWarnings("unchecked")
	void put(int offset, int queryLoc, int tuneLoc) {
		SortedSet<Integer>[] subhits;
		if (hits.containsKey(offset)) {
			subhits = hits.get(offset);
		}
		else {
			subhits = new SortedSet[len];
			hits.put(offset, subhits);
		}
		if (subhits[queryLoc] == null) {
			subhits[queryLoc] = new TreeSet<Integer>();
			if (counts.containsKey(offset)) {
				counts.put(offset, counts.get(offset) + 1);
			}
			else {
				counts.put(offset, 1);
			}
		}
		if (!subhits[queryLoc].contains(tuneLoc)) {
			subhits[queryLoc].add(tuneLoc);
			nhits += 1;
		}
	}
	@Override
	public int compareTo(Match m) {
		return nhits - m.nhits;
	}
	public Alignment align(int[] query, int[] tune) {
		return null;
	}
	public int computeOffset() {
		int offset = 0;
		int maxcount = 0;
		for (Entry<Integer, Integer> e : counts.entrySet()) {
			if (e.getValue() > maxcount) {
				maxcount = e.getValue();
				offset = e.getKey();
			}
		}
		return offset;
	}
}
