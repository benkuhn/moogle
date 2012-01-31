package net.benkuhn.index;

public class Trie<T> {
	Object[] branches;
	int sublevels;
	/** Makes a new Trie.
	 * 
	 * @param sublevels - number of different indices in each key.
	 * @param len - length of each array. Each key index can take any
	 * 		integer value in the interval [0, len).
	 */
	public Trie (int sublevels, int len) {
		this.sublevels = sublevels;
		this.branches = new Object[len];
	}
	/** Gets the element currently stored at an index.
	 * 
	 * @param nums - the index of the element to return
	 * @return the element currently stored at index nums, or null if
	 * that space is currently empty.
	 * @throws RuntimeException if nums is the wrong size
	 * @throws ArrayIndexOutOfBoundsException if any element of
	 * 		nums is out of bounds
	 */
	@SuppressWarnings("unchecked")
	public T get(int[] nums) {
		if (nums.length != sublevels) {
			//TODO throw a better exception
			throw new RuntimeException();
		}
		Trie<T> cur = this;
		for (int i = 0; i < sublevels - 1; i++) {
			Trie<T> next = (Trie<T>) cur.branches[nums[i]];
			if (next == null) {
				return null;
			}
			else {
				cur = next;
			}
		}
		return (T) cur.branches[nums[sublevels - 1]];
	}
	/** Gets the element currently stored at an index.
	 * 
	 * @param nums - the index
	 * @param value - the value
	 * @throws RuntimeException if nums is the wrong size
	 * @throws ArrayIndexOutOfBoundsException if any element of
	 * 		nums is out of bounds
	 */
	@SuppressWarnings("unchecked")
	public void set (int[] nums, T value) {
		if (nums.length != sublevels) {
			throw new RuntimeException();
		}
		Trie<T> cur = this;
		for (int i = 0; i < sublevels - 1; i++) {
			Trie<T> next = (Trie<T>) cur.branches[nums[i]];
			if (next == null) {
				next = new Trie<T>(sublevels - 1, branches.length);
				cur.branches[nums[i]] = next;
			}
			cur = next;
		}
		cur.branches[nums[sublevels - 1]] = value;
	}
	/** Gets the element currently stored at an index.
	 * 
	 * @param nums - the index of the element to return
	 * @param factory - creates a default element if there is no other
	 * @return the element currently stored at index nums, or factory.make() if
	 * that space is currently empty.
	 * @throws RuntimeException if nums is the wrong size
	 * @throws ArrayIndexOutOfBoundsException if any element of
	 * 		nums is out of bounds
	 */
	@SuppressWarnings("unchecked")
	public T getWithDefault (int[] nums, Factory<T> factory) {
		if (nums.length != sublevels) {
			throw new RuntimeException();
		}
		Trie<T> cur = this;
		for (int i = 0; i < sublevels - 1; i++) {
			Trie<T> next = (Trie<T>) cur.branches[nums[i]];
			if (next == null) {
				next = new Trie<T>(sublevels - 1, branches.length);
				cur.branches[nums[i]] = next;
			}
			cur = next;
		}
		T ret = (T) cur.branches[nums[sublevels - 1]];
		if (ret == null) {
			ret = factory.make();
			cur.branches[nums[sublevels - 1]] = ret;
		}
		return ret;
	}
}
