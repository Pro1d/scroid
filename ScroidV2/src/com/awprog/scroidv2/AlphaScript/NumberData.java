package com.awprog.scroidv2.AlphaScript;

import java.text.DecimalFormat;

public class NumberData extends Data {
	private double value;
	
	public NumberData(double v, boolean isStatic) {
		super(DT.number, isStatic);
		value = v;
	}
	
	/** Accessors **/ 
	public void setValue(double v) {
		value = v;
	}
	public double getValue() {
		return value;
	}

	@Override
	public String toString() {
		DecimalFormat df = new DecimalFormat("0.##########");
		return df.format(value);
		/*double round = value;//((double) Math.round(value * 10.0)) / 10.0;
		if((int) round == round)
			return String.valueOf((int)round);
		else
			return String.valueOf(round);*/
	}
	/** Reinitiliaze the value **/
	@Override public void clear() {
		value = 0.0;
	}
	
	public int compareTo(NumberData d) {
		return Double.compare(getValue(), d.getValue());
	}
}
