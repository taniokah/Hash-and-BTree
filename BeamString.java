// BeamString.java
// 2005.04.22 taniokah	replace char to byte.

//package raptor;

import java.io.*;
import java.util.*;

// BeamString : Store Class for Beam String.
public class BeamString implements Serializable {
	static final String CHARSET_NAME = "UTF-8";
	
	private int[] index;
	private byte[] bytes;
	private int endpos = 0;		// written point of last.
	private int count = 0;		// appennded number.
	private int average = 8;	// average word length = 7 ~ 9
	private int increase = 100000;
	private int baseline = 1000000;
	
	// constructor add by yamamoke 2005.04.26
	public BeamString(final int size, int _average) {
		average = _average;
		
		count = size;
		endpos = 0;
		index = new int[size];
		bytes = new byte[size * (average + 1)];
		Arrays.fill(index, -1);
	}
	
	
	// constructor
	public BeamString(final int size) {
		count = size;
		endpos = 0;
		index = new int[size];
		bytes = new byte[size * (average + 1)];
		Arrays.fill(index, -1);
	}
	
	// compare offset value to bytes.
	public int compareTo(int offset, final byte[] key) {
		final int b = (int)0xff & bytes[offset++];
		final int lenA = b;
		final int lenB = key.length;
		final int ret = lenA - lenB;
		final int len = ret < 0 ? lenA : lenB;
		int k = 0;
		int r = 0;
		while (k < len) {
			final int b1 = (int)0xff & bytes[offset++];
			final int b2 = (int)0xff & key[k++];
			r = b1 - b2;
			if (r != 0) {
				return r;
			}
		}
		return ret;
	}
	
	// get string from offset.
	public String getString(int offset) {
		try {
			final int b = (int)0xff & bytes[offset++];
			final int len = b;
			return new String(bytes, offset, len, CHARSET_NAME);
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// get string from index.
	public String get(final int i) {
		try {
			if (i >= index.length) {
				return null;
			}
			int offset = index[i];
			if(offset < 0) {
				return null;
			}
			final int b = (int)0xff & bytes[offset++];
			final int len = b;
			return new String(bytes, offset, len, CHARSET_NAME);
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// add string into array.
	public int add(final String str) {
		return set(count++, str);
	}
	
	// get offset of bytes from index. 2005.04.22 taniokah
	public int getOffset(final int i) {
		if (i >= index.length) {
			return -1;
		}
		final int offset = index[i];
		if(offset < 0) {
			return -1;
		}
		return offset;
	}
	
	// add bytes into array. 2005.04.22 taniokah
	public int add(final byte[] str) {
		return set(count++, str);
	}
	
	// set string at index onto array.
	public int set(final int i, final String str) {
		try {
			return set(i, str.getBytes(CHARSET_NAME));
		}
		catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	// set bytes at index onto array. 2005.04.22 taniokah
	public int set(final int i, final byte[] str) {
		final int length = str.length;
		final int index_len = index.length;
		final int dif = i - index_len;
		if (dif >= 0) {
			// expand memory of array(index).
			int size = dif;
			if (index_len + dif > baseline) {
				size += (int)((double)index_len + increase);
				//System.out.println("over baseline : index size = " + 
				//	size * 4 / 1000000 + "MB");
			}
			else {
				size += (int)((double)index_len * 1.5);
				//System.out.println("under baseline : index size = " + 
				//	size * 4 / 1000000 + "MB");
			}
			int[] _index = new int[size];
			System.arraycopy(index, 0, _index, 0, index.length);
			index = null;
			System.gc();
			index = _index;
		}
		final int bytes_len = bytes.length;
		if (endpos + length + 1 >= bytes_len) {
			int size;
			if (bytes_len + length > baseline) {
				size = (int)((double)bytes_len + increase * (average + 1));
			}
			else {
				size = (int)((double)bytes_len * 1.5);
			}
			byte[] _bytes = new byte[size];
			System.arraycopy(bytes, 0, _bytes, 0, bytes.length);
			bytes = null;
			System.gc();
			bytes = _bytes;
		}
		
		index[i] = endpos;
		bytes[endpos++] = (byte)length;
		System.arraycopy (str, 0, bytes, endpos, length);
		endpos += length;
		return i;
	}
}