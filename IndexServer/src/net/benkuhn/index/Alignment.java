package net.benkuhn.index;

import java.util.Comparator;

public class Alignment {
	public static Comparator<Alignment> DESCENDING = new Comparator<Alignment>() {
		@Override
		public int compare(Alignment o1, Alignment o2) {
			int ret = o1.score - o2.score;
			return ret == 0 ? o1.name.compareTo(o2.name) : ret;
		}
	};
	
	String name;
	int score;
	int begin;
	int end;
	int offset;
	public Alignment(String name, int score, int begin, int end, int offset) {
		super();
		this.name = name;
		this.score = score;
		this.begin = begin;
		this.end = end;
	}
	public String toString() {
		return name + ":" + score + "/" + begin + "-" + end + "(" + offset + ")";
	}
}
