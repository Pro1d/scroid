package com.awprog.scroidv2.AlphaScript;

import java.util.ArrayList;

/** /!\The data contained in the arraylist must be static **/
public class StructData extends Data {
	private ArrayList<Data> values;
	
	public StructData(boolean isStatic) {
		super(DT.struct, isStatic);
		values = new ArrayList<Data>();
	}
	public StructData(ArrayList<Data> v, boolean isStatic) {
		super(DT.struct, isStatic);
		
		values = v;
		
		//toStatic();
	}
	
	/** Returns the data at the index i **/
	public Data getDataAt(int i) {
		if(i >= 0 && i < values.size())
			return values.get(i);
		return null;
	}
	/** Adds a data at the end of the list **/
	public void pushBackData(Data d) {
		values.add(d);
	}
	/** Removes a data at the end of the list **/
	public void popBackData() {
		if(!values.isEmpty())
			values.remove(values.size()-1);
	}
	/** Adds a data at the beginning of the list **/
	public void pushFrontData(Data d) {
		values.add(0, d);
	}
	/** Removes a data at the beginning of the list **/
	public void popFrontData() {
		if(!values.isEmpty())
			values.remove(0);
	}
	/** Adds a data at the given index of the list **/
	public void pushData(Data d, int index) {
		if(index >= 0)
			values.add(index > values.size() ? values.size() : index, d);
	}
	/** Removes a data at the given index of the list **/
	public void popData(int index) {
		if(index >= 0 && index < values.size())
			values.remove(index);
	}

	/** Returns the number of datas in the list **/
	public int getNbData() {
		return values.size();
	}
	
	/** Compares himself with another struct data;, returns 0 if they have the same content, false otherwise **/
	public int compareTo(StructData struct) {
		final int len = getNbData();
		
		if(len != struct.getNbData())
			return 1;
		
		for(int i = 0; i < len; ++i) {
			Data d1 = getDataAt(i), d2 = struct.getDataAt(i);
			
			if(d1.getType() != d2.getType())
				return 1;
			else if(Data.compare(d1, d2, d1.getType()) != 0)
				return 1;
		}
		return 0;
	}
	
	/** Clears fully the list **/
	@Override public void clear() {
		values.clear();
	}
	
	@Override
	public String toString() {
		String sout = "";
		int size = values.size();
		for(int i = 0; i < size; ++i)
			sout += values.get(i).toString() + ((i < size-1) ? " ":"");
		
		return "[" + sout + "]";
	}
	
	public void setValue(ArrayList<Data> struct) {
		values = struct;
	}
	public ArrayList<Data> getValue() {
		return values;
	}
	
	/** Remplace the not static Data of the ArrayList by new static data ** /
	private void toStatic() {
		for(int index = 0; index < values.size(); index++) {
			Data d = values.get(index);
			
			// Remplace the values of this sub-struct
			if(d.getType() == DT.struct) {
				((StructData)d).toStatic();
			}
			// Remplace the not static datas by new static datas
			else if(!d.isStatic()) {
				switch(d.getType()) {
				case DT.bool:
					values.set(index, new BooleanData(((BooleanData)d).getValue(), true));
					break;
				case DT.number:
					values.set(index, new NumberData(((NumberData)d).getValue(), true));
					break;
				case DT.string:
					values.set(index, new StringData(((StringData)d).getValue(), true));
					break;
				}
			}
		}
	}*/
	
	/** Clone the ArrayList and its content **/
	public ArrayList<Data> cloneValues() {
		ArrayList<Data> list = new ArrayList<Data>();
		for(Data d : values) {
			switch(d.getType()) {
			case DT.struct:
				list.add(new StructData(((StructData)d).cloneValues(), true));
				break;
			case DT.string:
				list.add(new StringData(new StringBuilder(((StringData)d).getValue()), true));
				break;
			case DT.number:
				list.add(new NumberData(((NumberData)d).getValue(), true));
				break;
			case DT.bool:
				list.add(new BooleanData(((BooleanData)d).getValue(), true));
				break;
			}
		}
		return list;
	}
}
