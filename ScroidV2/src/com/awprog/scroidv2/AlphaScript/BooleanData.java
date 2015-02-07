package com.awprog.scroidv2.AlphaScript;


public class BooleanData extends Data {
	private boolean value;

	public BooleanData(boolean v, boolean isStatic) {
		super(DT.bool, isStatic);
		value = v;
	}
	
	@Override
	public String toString() {
		return value ? "True" : "False";
	}

	@Override
	public void clear() {
		value = false;
	}

	/** Accessors **/ 
	public void setValue(boolean v) {
		value = v;
	}
	public boolean getValue() {
		return value;
	}
	
	/** Returns 0 if d1 is equal to d2 **/
	public int compareTo(BooleanData d) {
		return getValue() == d.getValue() ? 0 : 1;
	}
}
