package net.benkuhn.index;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//TODO make thread-safe
public class Index {
	private Lock mutex = new ReentrantLock();
	private Factory<Map<String, Set<Integer>>> factory = new Factory<Map<String,Set<Integer>>>() {
		@Override
		public Map<String, Set<Integer>> make() {
			return new HashMap<String, Set<Integer>>();
		}
	};
	public final Trie<Map<String, Set<Integer>>> idx;
	public final int ngramSize;
	public final int trieWidth;
	public final int maxLeap, minLeap, zeroOffset;
	public final Map<String, int[]> tunes;
	
	public Index(int trieWidth, int ngramSize) {
		this.trieWidth = trieWidth;
		//TODO does this work for even width?
		this.maxLeap = (trieWidth - 1)/2;
		this.minLeap = maxLeap - (trieWidth - 1);
		this.zeroOffset = -minLeap;
		this.ngramSize = ngramSize;
		this.idx = new Trie<Map<String, Set<Integer>>>(ngramSize, trieWidth);
		this.tunes = new HashMap<String, int[]>();
	}
	public Index(Trie<Map<String, Set<Integer>>> idx,
			Map<String, int[]> tunes) {
		super();
		this.idx = idx;
		this.trieWidth = idx.branches.length;
		this.ngramSize = idx.sublevels;
		this.maxLeap = (trieWidth - 1)/2;
		this.minLeap = maxLeap - (trieWidth - 1);
		this.zeroOffset = -minLeap;
		this.tunes = tunes;
	}
	
	private void unindex(String id) {
		int[] notes = tunes.remove(id);
		if (notes == null) {
			return;
		}
		int i = 0;
		for (int[] ngram : new NGrams(notes)) {
			Map<String, Set<Integer>> m = idx.get(ngram);
			assert(m != null);
			Set<Integer> ints = m.get(id);
			assert(ints.remove(i));
			if (ints.isEmpty()) {
				m.remove(id);
			}
			i++;
		}
	}
	//Mess with the array so that none of the intervals will land outside the
	//trie width
	private void sanitize(String id, int[] notes) {
		for (int i = 0; i < notes.length - 1; i++) {
			if (notes[i+1] - notes[i] < minLeap) {
				System.out.println(id + ": warning: jump below minLeap: "
						+ notes[i] + " to " + notes[i+1]);
				notes[i+1] = notes[i] + minLeap;
			}
			if (notes[i+1] - notes[i] > maxLeap) {
				System.out.println(id + ": warning: jump above maxLeap: "
						+ notes[i] + " to " + notes[i+1]);
				notes[i+1] = notes[i] + maxLeap;
			}
		}
	}
	public void add(String id, int[] notes) {
		mutex.lock();
		unindex(id);
		int i = 0;
		sanitize(id, notes);
		for (int[] ngram : new NGrams(notes)) {
			Map<String, Set<Integer>> m = idx.getWithDefault(ngram, factory);
			Set<Integer> ints;
			if (m.containsKey(id)) {
				ints = m.get(id);
			}
			else {
				ints = new TreeSet<Integer>();
				m.put(id, ints);
			}
			ints.add(i);
			i++;
		}
		tunes.put(id, notes);
		mutex.unlock();
	}
	public void del(String id) {
		mutex.lock();
		unindex(id);
		mutex.unlock();
	}
	public Map<String, Set<Integer>> searchNGram(int[] notes) {
		int[] ngram = new NGrams(notes).iterator().next();
		return idx.get(ngram);
	}
	public Map<String, Match> getPreliminaryMatches(int[] notes) {
		mutex.lock();
		sanitize("query", notes);
		int queryLoc = -1;
		int len = notes.length - ngramSize;
		Map<String, Match> matches = new HashMap<String, Match>();
		/* Divide the query string up into n-grams, then find all the tunes
		 * that match each n-gram.
		 */
		for (int[] ngram : new NGrams(notes)) {
			queryLoc++;
			Map<String, Set<Integer>> m = idx.get(ngram);
			if (m == null) continue;
			for (Entry<String, Set<Integer>> entry : m.entrySet()) {
				String id = entry.getKey();
				Match match = matches.get(id);
				int[] tune;
				if (match == null) {
					match = new Match(id, len, tunes.get(id));
					matches.put(id, match);
				}
				tune = match.tune;
				Set<Integer> hits = entry.getValue();
				for (int tuneLoc : hits) {
					match.put(notes[queryLoc] - tune[tuneLoc], queryLoc, tuneLoc);
				}
			}
		}
		mutex.unlock();
		return matches;
	}
	public void search(int[] notes, PrintStream out) {
		SortedSet<Alignment> ret = new TreeSet<Alignment>(Alignment.DESCENDING);
		Map<String, Match> matches = getPreliminaryMatches(notes);
		for (Entry<String, Match> entry : matches.entrySet()) {
			Match match = entry.getValue();
			//make all the n-gram hits vote on what the offset is between the
			//query string and the tune string
			int offset = match.computeOffset();
			//locate the areas that need approximate matching
			//god DAMN this logic is complicated
			Alignment align = this.align(offset, match);
			ret.add(align);
		}
		for (Alignment align : ret) {
			out.println(align.name + ':' + align.score);
		}
		out.println();
	}
	public Match getMatch(String id, int[] notes) {
		Map<String, Match> matches = getPreliminaryMatches(notes);
		Match match = matches.get(id);
		return match;
	}
	public Alignment align(int offset, Match match) {
		int matchBegin = -1, matchEnd = -1;
		SortedSet<Integer>[] hits = match.hits.get(offset);
		// new algo!
		int queryPos = 0, tunePos = 0;
		int lastHit = 0, lastQueryHitEnd = 0, lastHitEnd = 0;
		int score = 0;
		//TODO figure out where 2-point gap penalty comes from...
		while (queryPos < match.len) {
			if (queryPos < lastQueryHitEnd) {
				//if we're inside a perfect match from a previous hit,
				//we only care about the next perfect match if it allows us
				//to extend the current string of hits.
				int nextHit = -1;
				if (hits[queryPos] != null) {
					nextHit = hits[queryPos].tailSet(tunePos).first();
				}
				if (nextHit == lastHit + 1) {
					lastHit = nextHit;
					lastHitEnd = lastHit + ngramSize;
					lastQueryHitEnd = queryPos + ngramSize;
				}
				queryPos++;
				continue;
			}
			else {
				int nextHit;
				//we're outside a perfect match, so we need to look for the next
				//one and penalize the score.
				if (hits[queryPos] == null) {
					//TODO this double-counts the following
					//a b c d q e
					//a b c d f f f e
					//results in score +1 from q and +3 from f.
					//maybe advancing lastHitEnd by 1 would fix it?
					nextHit = -1;
				}
				else {
					nextHit = hits[queryPos].tailSet(tunePos).first();
					if (nextHit < lastHitEnd) {
						nextHit = -1;
					}
				}
				if (nextHit == -1) {
					score++;
					queryPos++;
					continue;
				}
				//TODO compensate if next hit is way far away
				if (lastHitEnd > 0) { //only penalize if we've already had an exact hit
					score += nextHit - lastHitEnd;
				}
				lastHit = nextHit;
				lastHitEnd = lastHit + ngramSize;
				lastQueryHitEnd = queryPos + ngramSize;
				queryPos++;
				continue;
			}
		}
		// if there are unmatched queries at the end of the string, they haven't been penalized yet
		// the actual penalty is score += queryLength - lastQueryHitEnd but queryLength is a constant
		// so we can ignore it
		score -= lastQueryHitEnd;
		return new Alignment(match.name, score, matchBegin, matchEnd, offset);
	}
	public class NGrams implements Iterable<int[]> {
		int[] notes;
		public NGrams(int[] notes) {
			this.notes = notes;
		}
		public Iterator<int[]> iterator() {
			return new Iterator<int[]>() {
				int i = 0;
				@Override
				public boolean hasNext() {
					// each ngram actually takes ngramSize + 1 elements,
					// because we turn them into intervals first
					// so we have i, i+1, ..., i+ngramSize, and the last must
					// be < notes.length (P.S.: I hate counting bugs)
					return (i + ngramSize < notes.length);
				}
				@Override
				public int[] next() {
					int[] ret = new int[ngramSize];
					for (int k = 0; k < ngramSize; k++) {
						int val = notes[i+k+1] - notes[i+k] + zeroOffset;
						//TODO enforce these preconditions while indexing score
						assert(0 <= val);
						assert(val <= trieWidth);
						ret[k] = val;
					}
					i += 1;
					return ret;
				}
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	}
}
