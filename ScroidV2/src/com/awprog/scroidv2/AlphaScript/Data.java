package com.awprog.scroidv2.AlphaScript;


public abstract class Data {
	/** DataType **/
	public class DT {
		static final int string = 0x01,
						 number = 0x02,
						 bool = 0x04,
						 struct = 0x08,
						 error = 0x10,
						 notdef = 0x20,
						 all = string|number|bool|struct,
						 data_mask = 0x3F,
						 none = 0x00,
						 writable = 0x80;
		static final int cmp_equ = 0x01,
						 cmp_neq = 0x02,
						 cmp_lss = 0x04,
						 cmp_leq = 0x08,
						 cmp_gtr = 0x10,
						 cmp_geq = 0x20,
						 cmp_equal = cmp_equ|cmp_neq,
						 cmp_strict_dif = cmp_lss|cmp_gtr,
						 cmp_all = cmp_equal|cmp_strict_dif|cmp_leq|cmp_geq,
						 cmp_none = 0x00;
	}
	
	final protected int type;
	final private boolean isStatic;
	
	protected Data(int type, boolean isStatic) {
		this.type = type;
		this.isStatic = isStatic;
	}

	public int getType() {
		return type;
	}
	public String getTypeName() {
		switch(type) {
		case DT.string:
			return "String";
		case DT.bool:
			return "Boolean";
		case DT.number:
			return "Number";
		case DT.error:
			return "Error";
		case DT.notdef:
			return "Undefined data";
		default:
			return "#Unknown data type#";
		}
	}
	public boolean isStatic() {
		return isStatic;
	}
	
	/** Abstract methods **/
	abstract public String toString();
	abstract public void clear();
	//abstract public int compareTo();
	/** Clone the data with its value **/
	public Data clone() {
		switch(type) {
		case DT.bool:
			return new BooleanData(((BooleanData) this).getValue(), true);
		case DT.number:
			return new NumberData(((NumberData) this).getValue(), true);
		case DT.string:
			return new StringData(new StringBuilder(((StringData) this).getValue()), true);
		case DT.struct:
			return new StructData(((StructData) this).cloneValues(), true);
		default:
			return null;
		}
	}
	public void copyValue(Data d) {
		if(d.type != type)
			return;
		switch(type) {
		case DT.number:
			((NumberData)this).setValue(((NumberData)d).getValue());
			break;
		case DT.string:
			((StringData)this).setValue(((StringData)d).getValue());
			break;
		case DT.struct:
			((StructData)this).setValue(((StructData)d).cloneValues());
			break;
		case DT.bool:
			((BooleanData)this).setValue(((BooleanData)d).getValue());
			break;
		}
	
	}
	
	/** Compares two data, their type must be the one which is specified **/
	public static int compare(Data d1, Data d2, int type) {
		switch(type) {
		case DT.bool:
			return ((BooleanData)d1).compareTo((BooleanData)d2);
		case DT.number:
			return ((NumberData)d1).compareTo((NumberData)d2);
		case DT.string:
			return ((StringData)d1).compareTo((StringData)d2);
		case DT.struct:
			return ((StructData)d1).compareTo((StructData)d2);
		default :
			return 0;
		}
	}
}
