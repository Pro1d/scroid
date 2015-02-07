package com.awprog.scroidv2.AlphaScript;


public class StringData extends Data {
	private StringBuilder value;
	/* (ne clone pas) */ 
	public StringData(StringBuilder v, boolean isStatic) {
		super(DT.string, isStatic);
		value = v; 
	}

	/** Accessors (ne clone pas) **/ 
	public void setValue(StringBuilder v) {
		value = v;
	}
	public StringBuilder getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "\"" + value.toString() + "\"";
	}
	
	/** Reinitiliaze the value **/
	@Override public void clear() {
		value.delete(0, value.length());
	}

	/** Compares with another StringData **/
	public int compareTo(StringData d) {
		int i = 0;
		final StringBuilder s1 = d.getValue();
		final StringBuilder s2 = getValue();
		final int l1 = s1.length();
		final int l2 = s2.length();
		while(i < l1 && i < l2) {
			int diff = s1.charAt(i)-s2.charAt(i);
			if(diff < 0)
				return -1;
			else if(diff > 0)
				return 1;
			i++;
		}
		return l1-l2;
	}
}
