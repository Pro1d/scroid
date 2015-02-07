package com.awprog.scroidv2.AlphaScript;

public class NotDefData extends Data {
	private final String name;
	
	protected NotDefData(String name) {
		super(DT.notdef, true);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "<" + name + ">";
	}

	@Override
	public void clear() {
		
	}

}
