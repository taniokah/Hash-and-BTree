// BeamTree.java
// 2004.09.07 taniokah
// 2004.12.27 taniokah	Append BeamString.
// 2005.02.11 taniokah	extreme improve without new String.
// 2005.02.15 taniokah	extreme improve using spiritual techniques.
// 2005.04.22 taniokah	replace char to byte.
// 2005.07.11 taniokah	add function for id to term.

//package raptor;

import java.io.*;
import java.util.*;

// BeamTree : Huge Size On-Memory Database for String using Hash and B-Tree.
public class BeamTree implements Serializable {
	
	// mode normal, bidirection, ...
	public static final int NORMAL = 0;
	public static final int BIDIRECT = 1;
	
	// Size of Hash Table (Prime)
	private static final int[] HASH_SIZE = {101, 9973, 99991, 1000003, 9999991};
	// integer number (base 256)
	private static final int CHAR_BIT = 8;//16;
	//static final int INT_MAX = Integer.MAX_VALUE;
	private static final String CHARSET_NAME = "UTF-8";
	
	private Beam hashTable = null;
	private Beam keyTable = null;
	//private HyperCash hyperCash = null;
	private int count = 0;
	private int size = 0;
	private int average = 0;
	private int mode = NORMAL;
	private int hash_size = HASH_SIZE[0];
	private int hash_length;
	
	// main (sample code)
	public static void main(String[] args) throws IOException {
		int size = 10000;
		if (args.length > 0) {
			size = Integer.parseInt(args[0]);
		}
		BeamTree tree = new BeamTree(size);
		//BeamTree tree = new BeamTree(size, 0);
		//BeamTree tree = new BeamTree(size, 9973);
		//Map tree = new HashMap(size);
		
		tree.setMode(BeamTree.BIDIRECT);
		
		int id;
		String output;
		
		// データの追加（双方向の場合はsetを利用）
		System.out.println("#データの追加");
		id = tree.set("東京", 10);
		System.out.println("id = " + id);
		id = tree.set("徳島", 32);
		System.out.println("id = " + id);
		id = tree.set("東京", 210000);		// 重複してたら登録不可
		System.out.println("id = " + id);
		id = tree.set("大阪", 210000);		// 重複してないから登録可能
		System.out.println("id = " + id);
		
		byte[] test = "0".getBytes(CHARSET_NAME);
		System.out.println(test[0]);
		id = tree.set("0", 48);				// 不思議なことが...起きなくしたよ
		System.out.println("id = " + id);
		
		System.out.println("");
		
		System.out.println("#データの取得");
		// データの取得（引数と返値の型に注意）
		id = tree.get("東京");
		System.out.println("id = " + id);
		id = tree.get("徳島");
		System.out.println("id = " + id);
		output = tree.pul(10);
		System.out.println("output = " + output);
		output = tree.pul(32);
		System.out.println("output = " + output);
		output = tree.pul(210000);		// "大阪"が取り出せるはず
		System.out.println("output = " + output);
		id = tree.get("0");
		System.out.println("id = " + id);
		output = tree.pul(48);
		System.out.println("output = " + output);
		System.out.println("");
		
		
/*		int ID;
		ID = tree.put("東京", 111);
		ID = tree.get("東京");
		System.out.println("ID = " + ID);
		
		long time1 = System.currentTimeMillis();
		for (int i = 0; i < size; i++) {
			if (i % 100000 == 0) {
				System.out.print(".");
			}
			String key = new String(i + "");
			tree.put(key, i);
			//tree.put(key, null);
		}
		
		long time2 = System.currentTimeMillis();
		System.out.println("put: " + (double)(((double)time2 - time1) / size));

//		String[] keys = tree.keySet();

		for (int i = 0; i < size; i++) {
			String key = new String(i + "");
			int id = tree.get(key);
			//System.out.println(key + "," + id);
		}
		long time3 = System.currentTimeMillis();
		System.out.println("get: " + (double)(((double)time3 - time2) / size));
*/
	}
	
	// constructor
	public BeamTree() {
		this(HASH_SIZE[0]);
	}
	
	// constructor
	public BeamTree(final int size) {
		this(size, (size >= 0 && size < HASH_SIZE[1]) ? HASH_SIZE[0] : 
								(size < HASH_SIZE[2]) ? HASH_SIZE[1] : 
								(size < HASH_SIZE[3]) ? HASH_SIZE[2] : 
								(size < HASH_SIZE[4]) ? HASH_SIZE[3] : 
								HASH_SIZE[4]);
	}
	
	// constructor
	public BeamTree(int size, final int hash_size) {
		this.hash_size = hash_size;
		hash_length = (int)Math.pow(hash_size, 0.2);
		//hash_length = hash_length > 2 ? hash_length : 3;
		hash_length = hash_length > 4 ? hash_length : 5;
		//hash_length = hash_length < 16 ? hash_length : 15;
		if (hash_size > 0) {
			size = size > hash_size ? size : hash_size;
			hashTable = new Beam(size, hash_size);
		}
		else {
			hashTable = new Beam(size, 0);
		}
		hashTable.fill();
		this.size = size;
	}
	
	// constructor add by yamamoke 2005.04.26
	public BeamTree(int size, final int hash_size, int average) {
		this.hash_size = hash_size;
		hash_length = (int)Math.pow(hash_size, 0.2);
		hash_length = hash_length > 2 ? hash_length : 3;
		//hash_length = hash_length < 16 ? hash_length : 15;
		if (hash_size > 0) {
			size = size > hash_size ? size : hash_size;
			hashTable = new Beam(size, hash_size, average);
		}
		else {
			hashTable = new Beam(size, 0);
		}
		hashTable.fill();
		this.size = size;
		this.average = average;
	}
	
	// set mode.
	public boolean setMode(int mode) {
		this.mode = mode;
		if (hash_size > 0) {
			size = size > hash_size ? size : hash_size;
			switch (mode) {
				case BIDIRECT: 
				keyTable = new Beam(size, hash_size, average);
				break;
				default: 
				break;
			}
		}
		else {
			hashTable = new Beam(size, 0);
			switch (mode) {
				case BIDIRECT: 
				keyTable = new Beam(size, 0);
				break;
				default: 
				break;
			}
		}
		
		return true;
	}
	
	// hashing... for String.
	private int hash(final String s) {
		try {
			return hash(s.getBytes(CHARSET_NAME));
		}
		catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	// hashing... for bytes. 2005.04.22 taniokah
	private int hash(final byte[] s) {
		long v = 0;
		final int length = s.length;
		for (int i = 0; i < hash_length && i < length; i++) {
			final int b = (int)0xff & s[i];
			v = ((v << CHAR_BIT) + b) % hash_size;
		}
		return (int)v;
	}
	
	// clear objects.
	public void clear() {
		hashTable = null;
	}
	
	// get size of table.
	public int size() {
		switch (mode) {
			case BIDIRECT: 
			return count / 2;
			//break;
			
			default: 
			return count;
			//break;
		}
	}
	
	// get key set in String.
	public String[] keySet() {
		String[] keys = new String[count];
		int index = 0;
		int pos = 0;
		while (true){
			final int offset = hashTable.getKey(pos++);
			if (offset < 0) {
				continue;
			}
			String key = hashTable.getKeyString(offset);
			if (key == null) {
				continue;
			}
			keys[index++] = key;
			if (index == count) {
				break;
			}
		}
		return keys;
	}
	
	private byte[] getBytes(int key) {
		byte[] _key = new byte[4];
		int append = 0;
		append = key >>> 24 & 0xff;
		_key[0] = (byte)append;
		append = key >>> 16 & 0xff;
		_key[1] = (byte)append;
		append = key >>> 8 & 0xff;
		_key[2] = (byte)append;
		append = key & 0xff;
		_key[3] = (byte)append;
		
		return _key;
	}
	
	// get id of key from int number.
	public int get(final int key) {
		byte[] _key = getBytes(key);
		return get(hashTable, _key);
	}
	
	// get id of key from String.
	public int get(final String key) {
		try {
			return get(hashTable, key.getBytes(CHARSET_NAME));
		}
		catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	// get id of key from bytes. 2005.04.22 taniokah
	public int get(Beam table, final byte[] key) {
		final int h = hash_size > 0 ? hash(key) : 0;
		int child = h;
		while (true) {
			final int _offset = table.getKey(child);
			if (_offset < 0) {
				child = -1;
				break;
			}
			final int cmp = table.compareTo(_offset, key);
			if (cmp == 0) {
				break;
			}
			if (cmp < 0) {
				child = table.getLeft(child);
				continue;
			}
			else {
				child = table.getRight(child);
				continue;
			}
		}
		if (child == -1) {
			return -1;
		}
		// registered already
		return table.getID(child);
	}
	
	public int put(final int key, final int id) {
		byte[] _key = getBytes(key);
		return put(hashTable, _key, id);
	}
	
	// put id for key from String.
	public int put(final String key, final int id) {
		try {
			return put(hashTable, key.getBytes(CHARSET_NAME), id);
		}
		catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	// put id for key from bytes.
	public int put(Beam table, final byte[] key, final int id) {
		boolean isLeftChild = false;
		int parent = -1;
		final int h = hash_size > 0 ? hash(key) : 0;
		int child = h;
		while (true) {
			final int _offset = table.getKey(child);
			if (_offset < 0) {
				child = -1;
				break;
			}
			final int cmp = table.compareTo(_offset, key);
			if (cmp == 0) {
				break;
			}
			parent = child;
			if (cmp < 0) {
				child = table.getLeft(child);
				isLeftChild = true;
				continue;
			}
			else {
				child = table.getRight(child);
				isLeftChild = false;
				continue;
			}
		}
		if (child != -1) {
			// registered already
			return table.getID(child);
		}
		
		// append key and id into HashTable.
		count++;
		if (hash_size > 0 && parent == -1) {
			table.setKey(h, key);
			table.setLeft(h, -1);
			table.setRight(h, child);
			table.setID(h, id);
		}
		else {
			int q = table.append(key, -1, child, id);
			if (isLeftChild) {
				table.setLeft(parent, q);
			}
			else {
				table.setRight(parent, q);
			}
		}
		return id;
	}
	
	// pul key as String from value as key.
	public String pul(final int id) {
		if (keyTable == null) {
			System.err.println("Failed to set mode for BeamTree.");
			return null;
		}
		byte[] _key = getBytes(id);
		if (_key == null || _key.length <= 0) {
			return null;
		}
		int index = get(keyTable, _key);
		if (index < 0) {
			return null;
		}
		//System.out.println("index = " + index);
		int offset = hashTable.getKey(index);
		if (offset < 0) {
			return null;
		}
		//System.out.println("offset = " + offset);
		return hashTable.getKeyString(offset);
	}
	
	// set id and key.
	public int set(final String key, final int id) {
		if (keyTable == null) {
			System.err.println("Failed to set mode for BeamTree.");
			return -1;
		}
		try {
			return set(hashTable, key.getBytes(CHARSET_NAME), id);
		}
		catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	// put id for key from bytes, and return offset.
	public int set(Beam table, final byte[] key, final int id) {
		boolean isLeftChild = false;
		int parent = -1;
		final int h = hash_size > 0 ? hash(key) : 0;
		int child = h;
		while (true) {
			final int _offset = table.getKey(child);
			if (_offset < 0) {
				child = -1;
				break;
			}
			final int cmp = table.compareTo(_offset, key);
			if (cmp == 0) {
				break;
			}
			parent = child;
			if (cmp < 0) {
				child = table.getLeft(child);
				isLeftChild = true;
				continue;
			}
			else {
				child = table.getRight(child);
				isLeftChild = false;
				continue;
			}
		}
		if (child != -1) {
			// registered already
			return table.getID(child);
		}
		
		// append key and id into HashTable.
		count++;
		if (hash_size > 0 && parent == -1) {
			table.setKey(h, key);
			table.setLeft(h, -1);
			table.setRight(h, child);
			table.setID(h, id);
			if (put(keyTable, getBytes(id), h) < 0) {
				return -1;
			}
		}
		else {
			int q = table.append(key, -1, child, id);
			if (isLeftChild) {
				table.setLeft(parent, q);
			}
			else {
				table.setRight(parent, q);
			}
			if (put(keyTable, getBytes(id), q) < 0) {
				return -1;
			}
		}
		return id;
	}
}

// Beam : Access Control Class for Beam Objects.
class Beam implements Serializable {
	final static public byte BROTHERS = 3;
	
	private BeamString keys;
	private int[] brothers;
	private int count = 0;
	private int increase = 100000;
	private int baseline = 1000000;
	
	// constructor
	public Beam(final int size, final int hash_size) {
		final int temp_size = hash_size + hash_size;
		final int beam_size = size > temp_size ? size + hash_size / 5 : temp_size;
		//System.out.println("beam_size = " + beam_size);
		keys = new BeamString(beam_size);
		brothers = new int[beam_size * BROTHERS];
		count = hash_size;
	}
	
	// constructor add by yamamoke 2005.04.26
	public Beam(final int size, final int hash_size, int average) {
		final int temp_size = hash_size + hash_size;
		final int beam_size = size > temp_size ? size + hash_size / 5 : temp_size;
		//System.out.println("beam_size = " + beam_size);
		keys = new BeamString(beam_size, average);
		brothers = new int[beam_size * BROTHERS];
		count = hash_size;
	}
	
	// fill brothers by -1.
	public synchronized void fill() {
		Arrays.fill(brothers, -1);
	}
	
	// append key into beams. 2005.04.22 taniokah
	public synchronized int append(final byte[] key, 
							final int left, final int right, final int id) {
		if (count >= brothers.length / BROTHERS) {
			// expand memory of array.
			int size;
			if (count > baseline) {
				size = (int)((double)count + increase);
			}
			else {
				size = (int)((double)count * 1.5);
			}
			int[] _brothers = new int[size * BROTHERS];
			System.arraycopy(brothers, 0, _brothers, 0, brothers.length);
			brothers = null;
			System.gc();
			brothers = _brothers;
		}
		
		final int index = keys.set(count, key);
		if (index != count) {
			return -1;
		}
		
		final int base = count * BROTHERS;
		brothers[base] = left;
		brothers[base + 1] = right;
		brothers[base + 2] = id;
		return count++;
	}
	
	// compare offset value to bytes. 2005.04.22 taniokah
	public int compareTo(final int offset, final byte[] key) {
		return keys.compareTo(offset, key);
	}
	
	// get key in String from offset.
	public String getKeyString(final int offset) {
		return keys.getString(offset);
	}
	
	// get value of key.
	public int getKey(final int index) {
		if (index < 0) {
			return -1;
		}
		int offset = keys.getOffset(index);
		if (offset < 0) {
			return -1;
		}
		return offset;
	}
	
	// get left index of key.
	public int getLeft(final int index) {
		if (index < 0) {
			return -1;
		}
		return brothers[index * BROTHERS];
	}
	
	// get right index of key.
	public int getRight(final int index) {
		if (index < 0) {
			return -1;
		}
		return brothers[index * BROTHERS + 1];
	}
	
	// get id of key.
	public int getID(final int index) {
		if (index < 0) {
			return -1;
		}
		return brothers[index * BROTHERS + 2];
	}
	
	// set value of key. 2005.04.22 taniokah
	public void setKey(final int index, final byte[] key) {
		if (index < 0) {
			return;
		}
		if (key == null) {
			keys.set(index, (byte[])null);
			return;
		}
		keys.set(index, key);
	}
	
	// set left index of key.
	public void setLeft(final int index, final int left) {
		if (index < 0) {
			return;
		}
		brothers[index * BROTHERS] = left;
	}
	
	// set right index of key.
	public void setRight(final int index, final int right) {
		if (index < 0) {
			return;
		}
		brothers[index * BROTHERS + 1] = right;
	}
	
	// set id of key.
	public void setID(final int index, final int id) {
		if (index < 0) {
			return;
		}
		brothers[index * BROTHERS + 2] = id;
	}
}
