package com.awprog.scroidv2.AlphaScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

import android.annotation.SuppressLint;

import com.awprog.scroidv2.AlphaScript.Data.DT;
import com.awprog.scroidv2.AlphaScript.ErrorDialog.ScriptException;

@SuppressLint("DefaultLocale")
public class Instructions {
	static final HashMap<String, InstructionDefinition> instructions = new HashMap<String, InstructionDefinition>();
	
	/** Reference to the LowLeveAccess instance **/
	//static class LowLevelAccessReference { LowLevelAccess lla; };
	//private static LowLevelAccessReference mLLAR = new LowLevelAccessReference();
	static LowLevelAccess lla;
 	static void setLLA(LowLevelAccess lla) {
		Instructions.lla = lla;
	}
	
	static private void addInstruction(String name, int priority, int left, int right, int returned, RunnableFunction rf) {
		instructions.put(name, new InstructionDefinition(name, priority, left, right, returned, rf));
	}
	static public void addInlineFunction(String name, int left, int right, int returned, final String file, final int line) throws ScriptException {
		if(isExisting(name))
			throw new ScriptException("The function \""+name+"\" already exists");

		addInstruction(name, 800, left, right, returned, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				// changement de context, acc�s � la fonction au fichier et ligne d�sign�s, avec param�tres (ou pas)
				lla.pushContext(file, line, getParams().inLeft, getParams().inRight);
		}});
	}
	
	static public void addNativeInstructions() {
		/*
		//********* FUNCTION ******** /
		instructions.put("function", new InstructionDefinition("", 0, DT.none, DT.none, DT.none, new RunnableFunction() {
			@Override public void run() {
				
		}});
		*/
		/** TODO isNumber isString isBoolean isStruct **/
		/********* LABEL ******** Deprecated /
		instructions.put("label", new InstructionDefinition("", 0, DT.none, DT.string|DT.number, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				// null;
		}});*/
		/********* GOTO ******** Deprecated /
		instructions.put("goto", new InstructionDefinition("", 0, DT.none, DT.string|DT.number, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				if(getParams().inRight.getType() == DT.string)
					host.gotoLabel(((StringData) getParams().inRight).getValue());
				else
					host.gotoLabel((int) ((NumberData) getParams().inRight).getValue());
		}});*/
		/********* IF ********/
		addInstruction("if", 0, DT.none, DT.bool, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				lla.enterConditionalStructure();
				if(((BooleanData)getParams().inRight).getValue() == false)
					lla.setNextLine(param);
				else
					lla.skipConditionalStructure(); // pour les blocs suivants
		}});
		/********* ELSIF ********/
		addInstruction("elsif", 0, DT.none, DT.bool, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				if(lla.conditionalStructureSkipped())
					lla.setNextLine(param);
				else if(((BooleanData)getParams().inRight).getValue() == false)
					lla.setNextLine(param);
				else
					lla.skipConditionalStructure(); // pour les blocs suivants
		}});
		/********* ELSE ********/
		addInstruction("else", 0, DT.none, DT.none, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				if(lla.conditionalStructureSkipped())
					lla.setNextLine(param);
		}});
		/********* ENDIF ********/
		addInstruction("endif", 0, DT.none, DT.none, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				lla.endConditionalStructure();
		}});
		/********* WHILE ********/
		addInstruction("while", 0, DT.none, DT.bool, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				if(((BooleanData)getParams().inRight).getValue() == false)
					lla.setNextLine(param);
		}});
		/********* ENDWHILE ********/
		addInstruction("endwhile", 0, DT.none, DT.none, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				lla.setNextLine(param);
		}});
		/********* FOR ******** /
		instructions.put("for", new InstructionDefinition("for", 0, DT.number|DT.notdef|writable, DT.struct, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				if left type = undef -> create var, set value to start
				//else scriptexception : la variable existe d�j�
				
				left += end-start < 0 ? -1 : 1;
				if left equal or out of end range -> gotoendfor ; // remove var
		}});
		/********* ENDFOR ******** /
		instructions.put("endfor", new InstructionDefinition("endfor", 0, DT.none, DT.none, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				host.goToBeginFor();
		}});*/
		/********* TER ********/
		addInstruction("ter", 400, DT.bool, DT.struct, DT.all, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				StructData values = (StructData) getParams().inRight;
				if(values.getNbData() != 2)
					throw new ScriptException("The function \'ter\' must have exactly two right parameters contained in a structure");
				
				if(((BooleanData)getParams().inLeft).getValue())
					getParams().setReturn(values.getDataAt(0));
				else
					getParams().setReturn(values.getDataAt(1));
		}});
		

		/********* EQUAL ********/
		addInstruction("equ", 600, DT.all, DT.all, DT.bool, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				final Data left = getParams().inLeft, right = getParams().inRight;
				if(left.getType() != right.getType())
					throw new ScriptException("Incomparable data type");
				else
					getParams().setReturn(Data.compare(left, right, left.getType()) == 0);
		}});
		/********* NOT-EQUAL ********/
		addInstruction("neq", 600, DT.all, DT.all, DT.bool, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				final Data left = getParams().inLeft, right = getParams().inRight;
				if(left.getType() != right.getType())
					throw new ScriptException("Incomparable data type");
				else
					getParams().setReturn(Data.compare(left, right, left.getType()) != 0);
		}});
		/********* LESS ********/
		addInstruction("lss", 600, DT.string|DT.number, DT.string|DT.number, DT.bool, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				final Data left = getParams().inLeft, right = getParams().inRight;
				if(left.getType() != right.getType())
					throw new ScriptException("Incomparable data type");
				else
					getParams().setReturn(Data.compare(left, right, left.getType()) < 0);
		}});
		/********* LESS OR EQUAL ********/
		addInstruction("leq", 600, DT.string|DT.number, DT.string|DT.number, DT.bool, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				final Data left = getParams().inLeft, right = getParams().inRight;
				if(left.getType() != right.getType())
					throw new ScriptException("Incomparable data type");
				else
					getParams().setReturn(Data.compare(left, right, left.getType()) <= 0);
		}});
		/********* GREATER ********/
		addInstruction("gtr", 600, DT.string|DT.number, DT.string|DT.number, DT.bool, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				final Data left = getParams().inLeft, right = getParams().inRight;
				if(left.getType() != right.getType())
					throw new ScriptException("Incomparable data type");
				else
					getParams().setReturn(Data.compare(left, right, left.getType()) > 0);
		}});
		/********* GREATER OR EQUAL ********/
		addInstruction("geq", 600, DT.string|DT.number, DT.string|DT.number, DT.bool, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				final Data left = getParams().inLeft, right = getParams().inRight;
				if(left.getType() != right.getType())
					throw new ScriptException("Incomparable data type");
				else
					getParams().setReturn(Data.compare(left, right, left.getType()) >= 0);
		}});
		/********* IN *********/ 
		addInstruction("in", 600, DT.string|DT.number, DT.struct, DT.bool, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				final Data left = getParams().inLeft;
				final StructData range = (StructData) getParams().inRight;
				/// retourne vrai si right[0] <= left <= right[1]
				
				/// String
				if(left.getType() == DT.string) {
					checkParamsStruct(new int[] {DT.string,  DT.string}, range, "in");
					StringData min = (StringData) range.getDataAt(0);
					StringData max = (StringData) range.getDataAt(1);
					getParams().setReturn(Data.compare(min, left, DT.string) <= 0 && Data.compare(left, max, DT.string) <= 0);
				}
				/// Number
				else {
					checkParamsStruct(new int[] {DT.number,  DT.number}, range, "in");
					double min = ((NumberData) range.getDataAt(0)).getValue();
					double max = ((NumberData) range.getDataAt(1)).getValue();
					double value = ((NumberData) left).getValue();
					getParams().setReturn(min <= value && value <= max);	
				}
		}});

		/********* AND ********/
		addInstruction("and", 520, DT.bool, DT.bool, DT.bool, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(((BooleanData)getParams().inLeft).getValue() && ((BooleanData)getParams().inRight).getValue());
		}});
		/********* NAND ********/
		addInstruction("nand", 520, DT.bool, DT.bool, DT.bool, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(!(((BooleanData)getParams().inLeft).getValue() && ((BooleanData)getParams().inRight).getValue()));
		}});
		/********* OR ********/
		addInstruction("or", 500, DT.bool, DT.bool, DT.bool, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(((BooleanData)getParams().inLeft).getValue() || ((BooleanData)getParams().inRight).getValue());
		}});
		/********* NOR ********/
		addInstruction("nor", 500, DT.bool, DT.bool, DT.bool, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(!(((BooleanData)getParams().inLeft).getValue() || ((BooleanData)getParams().inRight).getValue()));
		}});
		/********* XOR ********/
		addInstruction("xor", 510, DT.bool, DT.bool, DT.bool, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				boolean left = ((BooleanData)getParams().inLeft).getValue();
				boolean right = ((BooleanData)getParams().inRight).getValue();
				getParams().setReturn(left && !right || !left && right);
		}});
		/********* NOT ********/
		addInstruction("not", 530, DT.none, DT.bool, DT.bool, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(((BooleanData)getParams().inRight).getValue() == false);
		}});

		/********* ADD ********/
		addInstruction("add", 700, DT.all, DT.all, DT.string|DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				int typeLeft = getParams().inLeft.getType(), typeRight = getParams().inRight.getType();
				// Concatenate string and data
				if(typeLeft == DT.string || typeRight == DT.string) {
					Data d = (typeLeft != DT.string) ? getParams().inLeft : getParams().inRight;
					StringBuilder strData = new StringBuilder();
					switch(d.getType()) {
					case DT.bool:
						strData.append(((BooleanData)d).toString());
						break;
					case DT.number:
						strData.append(((NumberData)d).toString());
						break;
					case DT.string:
						strData.append(((StringData)d).getValue());
						break;
					case DT.struct:
						strData.append(((StructData)d).toString());
						break;
					}
					
					if(typeLeft != DT.string)
						strData.append(((StringData)getParams().inRight).getValue());
					else
						strData.insert(0, ((StringData)getParams().inLeft).getValue());
					
					getParams().setReturn(strData);
				}
				// Operate with two numbers
				else if(typeLeft == DT.number || typeRight == DT.number) {
					getParams().setReturn(((NumberData)getParams().inLeft).getValue() + ((NumberData)getParams().inRight).getValue());
				}
				// Try to add incomptible datas
				else {
					throw new ScriptException("Not addable data");
				}
		}});
		/********* SUB ********/
		addInstruction("sub", 700, DT.number, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(((NumberData)getParams().inLeft).getValue() - ((NumberData)getParams().inRight).getValue());
		}});
		/********* MUL ********/
		addInstruction("mul", 710, DT.number, DT.number|DT.string, DT.number|DT.string, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				if(getParams().inRight.getType() == DT.number)
					getParams().setReturn(((NumberData)getParams().inLeft).getValue() * ((NumberData)getParams().inRight).getValue());
				else {
					final StringBuilder s = ((StringData)getParams().inRight).getValue();
					StringBuilder result = new StringBuilder("");
					for(int i = (int)((NumberData)getParams().inLeft).getValue(); i > 0; --i)
						result.append(s);
					getParams().setReturn(result);
				}
		}});
		/********* DIV ********/
		addInstruction("div", 710, DT.number, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(((NumberData)getParams().inLeft).getValue() / ((NumberData)getParams().inRight).getValue());
		}});
		/********* MOD ********/
		addInstruction("mod", 710, DT.number, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				double a = ((NumberData)getParams().inLeft).getValue();
				double b = ((NumberData)getParams().inRight).getValue();
				
				if(b <= 0.0)
					throw new ScriptException("Cannot compute modulo with value less or eqal to 0");
				else
					if(a >= 0)
						getParams().setReturn(a - b*(long)(a / b));
					else
						getParams().setReturn(a - b*(long)(a / b) + b);
		}});
		/********* POW ********/
		addInstruction("pow", 720, DT.number, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				double result = Math.pow(((NumberData)getParams().inLeft).getValue(), ((NumberData)getParams().inRight).getValue());
				if(result == Double.NaN || result == Double.NEGATIVE_INFINITY || result == Double.POSITIVE_INFINITY)
					throw new ScriptException("Pow math error");
				else
					getParams().setReturn(result);
		}});

		/********* SQRT ********/
		addInstruction("sqrt", 800, DT.none, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				double result = Math.sqrt(((NumberData)getParams().inRight).getValue());
				if(result == Double.NaN)
					throw new ScriptException("Square root math error");
				else
					getParams().setReturn(result);
		}});
		/********* HYPOT ********/
		addInstruction("hypot", 800, DT.none, DT.struct, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				StructData values = (StructData) getParams().inRight;
				checkParamsStruct(new int[] {DT.number, DT.number}, values, "hypot");
				
				double x = ((NumberData) values.getDataAt(0)).getValue();
				double y = ((NumberData) values.getDataAt(1)).getValue();
				double result = Math.hypot(x, y);
				
				getParams().setReturn(result);
		}});
		/********* CBRT ********/
		addInstruction("cbrt", 800, DT.none, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				double result = Math.cbrt(((NumberData)getParams().inRight).getValue());
				if(result == Double.NaN)
					throw new ScriptException("Cube root math error");
				else
					getParams().setReturn(result);
		}});
		/********* EXP ********/
		addInstruction("exp", 800, DT.none, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				double result = Math.exp(((NumberData)getParams().inRight).getValue());
				getParams().setReturn(result);
		}});
		/********* LOG ********/
		addInstruction("log", 800, DT.none, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				double result = Math.log(((NumberData)getParams().inRight).getValue());
				if(result == Double.NaN)
					throw new ScriptException("Natural logarithm math error");
				else
					getParams().setReturn(result);
		}});
		/********* LOG10 ********/
		addInstruction("log10", 800, DT.none, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				double result = Math.log10(((NumberData)getParams().inRight).getValue());
				if(result == Double.NaN)
					throw new ScriptException("Base 10 logarithm math error");
				else
					getParams().setReturn(result);
		}});
		/********* FACT ********/
		addInstruction("fact", 800, DT.none, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				double x = Math.floor(((NumberData)getParams().inRight).getValue());
				if(x < 0 || x > 150)
					throw new ScriptException("Factoriel math error (running with integer value from 0 to 150)");
				else {
					double result = 1;
					for(; x > 1; --x)
						result *= x;
					getParams().setReturn(result);
				}
		}});
		/********* SIN ********/
		addInstruction("sin", 800, DT.none, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(Math.sin(((NumberData)getParams().inRight).getValue()));
		}});
		/********* COS ********/
		addInstruction("cos", 800, DT.none, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(Math.cos(((NumberData)getParams().inRight).getValue()));
		}});
		/********* TAN ********/
		addInstruction("tan", 800, DT.none, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(Math.tan(((NumberData)getParams().inRight).getValue()));
		}});
		/********* ASIN ********/
		addInstruction("asin", 800, DT.none, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				double result = Math.asin(((NumberData)getParams().inRight).getValue());
				if(result == Double.NaN)
					throw new ScriptException("Arcsinus math error : value must be between -1 and 1");
				else
					getParams().setReturn(result);
		}});
		/********* ACOS ********/
		addInstruction("acos", 800, DT.none, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				double result = Math.acos(((NumberData)getParams().inRight).getValue());
				if(result == Double.NaN)
					throw new ScriptException("Arcsinus math error : value must be between -1 and 1");
				else
					getParams().setReturn(result);
		}});
		/********* ATAN ********/
		addInstruction("atan", 800, DT.none, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(Math.atan(((NumberData)getParams().inRight).getValue()));
		}});
		/********* ATAN2 ********/
		addInstruction("atan2", 800, DT.none, DT.struct, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				StructData values = (StructData) getParams().inRight;
				checkParamsStruct(new int[] {DT.number, DT.number}, values, "atan2");
				
				double x = ((NumberData) values.getDataAt(1)).getValue();
				double y = ((NumberData) values.getDataAt(0)).getValue();
				double result = Math.atan2(y, x);
				getParams().setReturn(result);
		}});
		/********* RAD ********/
		addInstruction("rad", 800, DT.none, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(Math.toRadians(((NumberData)getParams().inRight).getValue()));
		}});
		/********* DEG ********/
		addInstruction("deg", 800, DT.none, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(Math.toDegrees(((NumberData)getParams().inRight).getValue()));
		}});
		/********* FLOOR ********/
		addInstruction("floor", 800, DT.none, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(Math.floor(((NumberData)getParams().inRight).getValue()));
		}});
		/********* CEIL ********/
		addInstruction("ceil", 800, DT.none, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(Math.ceil(((NumberData)getParams().inRight).getValue()));
		}});
		/********* ROUND ********/
		addInstruction("round", 800, DT.none, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				if(((NumberData)getParams().inRight).getValue() > Long.MAX_VALUE)
					throw new ScriptException("Cannot round number greater than "+Long.MAX_VALUE);
				else if(((NumberData)getParams().inRight).getValue() < Long.MIN_VALUE)
					throw new ScriptException("Cannot round number less than "+Long.MIN_VALUE);
				else
					getParams().setReturn(Math.round(((NumberData)getParams().inRight).getValue()));
		}});
		/********* ABS ********/
		addInstruction("abs", 800, DT.none, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(Math.abs(((NumberData)getParams().inRight).getValue()));
		}});
		/********* OPP ********/
		addInstruction("opp", 800, DT.none, DT.number, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(-((NumberData)getParams().inRight).getValue());
		}});

		final Random random = new Random();
		/********* RANDOM ********/
		addInstruction("rand", 999, DT.none, DT.none, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(random.nextDouble());
		}});
		/********* SEED ********/
		addInstruction("seed", 999, DT.none, DT.number, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				random.setSeed(Double.doubleToRawLongBits(((NumberData)getParams().inRight).getValue()));
		}});
		/********* PI ********/
		addInstruction("pi", 999, DT.none, DT.none, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(Math.PI);
		}});
		/********* EXP ********/
		addInstruction("e", 999, DT.none, DT.none, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(Math.E);
		}});
		/********* TRUE ********/
		addInstruction("true", 999, DT.none, DT.none, DT.bool, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(true);
		}});
		/********* FALSE ********/
		addInstruction("false", 999, DT.none, DT.none, DT.bool, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(false);
		}});
		/********* ENDL ********/
		addInstruction("endl", 999, DT.none, DT.none, DT.string, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(new StringBuilder("\n"));
		}});

		/********* SET ********/
		addInstruction("set", 0, DT.all|DT.writable|DT.notdef, DT.all, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				if(getParams().inLeft.getType() == DT.notdef)
					getParams().inLeft = lla.createVar(((NotDefData)getParams().inLeft).getName(), getParams().inRight.getType());
				
				if(getParams().inLeft.getType() != getParams().inRight.getType())
					throw new ScriptException("Incompatible data type for assignement");
				else
					getParams().writeLeft(getParams().inRight.clone());
		}});
		/********* ICS ********/
		addInstruction("ics", 0, DT.none, DT.number|DT.writable, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().writeRight(((NumberData)getParams().inRight).getValue() + 1.0);
		}});
		/********* DCS ********/
		addInstruction("dcs", 0, DT.none, DT.number|DT.writable, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().writeRight(((NumberData)getParams().inRight).getValue() - 1.0);
		}});
		/********* SETADD ********/
		addInstruction("setadd", 0, DT.string|DT.number|DT.writable, DT.all, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				if(getParams().inLeft.getType() == DT.number) {
					if(getParams().inRight.getType() != DT.number)
						throw new ScriptException("Not addable data");
					else
						getParams().writeLeft(((NumberData)getParams().inLeft).getValue() + ((NumberData)getParams().inRight).getValue());
				}
				else {
					switch(getParams().inRight.getType()) {
					case DT.bool:
						((StringData)getParams().inLeft).getValue().append(((BooleanData)getParams().inRight).toString());
						break;
					case DT.number:
						((StringData)getParams().inLeft).getValue().append(((NumberData)getParams().inRight).toString());
						break;
					case DT.string:
						((StringData)getParams().inLeft).getValue().append(((StringData)getParams().inRight).getValue());
						break;
					case DT.struct:
						((StringData)getParams().inLeft).getValue().append(((StructData)getParams().inRight).toString());
						break;
					}
				}
		}});
		/********* SETSUB ********/
		addInstruction("setsub", 0, DT.number|DT.writable, DT.number, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().writeLeft(((NumberData)getParams().inLeft).getValue() - ((NumberData)getParams().inRight).getValue());
		}});
		/********* SETMUL ********/
		addInstruction("setmul", 0, DT.number|DT.string|DT.writable, DT.number, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				if(getParams().inRight.getType() == DT.number)
					getParams().writeLeft(((NumberData)getParams().inLeft).getValue() * ((NumberData)getParams().inRight).getValue());
				else {
					final StringBuilder s = ((StringData)getParams().inLeft).getValue();
					StringBuilder result = new StringBuilder("");
					for(int i = (int)((NumberData)getParams().inRight).getValue(); i > 0; --i)
						result.append(s);
					
					getParams().writeLeft(result);
				}
		}});
		/********* SETDIV ********/
		addInstruction("setdiv", 0, DT.number|DT.writable, DT.number, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().writeLeft(((NumberData)getParams().inLeft).getValue() / ((NumberData)getParams().inRight).getValue());
		}});
		/********* SETMOD ********/
		addInstruction("setmod", 0, DT.number|DT.writable, DT.number, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				double a = ((NumberData)getParams().inLeft).getValue();
				double b = ((NumberData)getParams().inRight).getValue();
				
				getParams().writeLeft(a % b);
		}});
		/********* SETPOW ********/
		addInstruction("setpow", 0, DT.number|DT.writable, DT.number, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				double result = Math.pow(((NumberData)getParams().inLeft).getValue(), ((NumberData)getParams().inRight).getValue());
				if(result == Double.NaN || result == Double.NEGATIVE_INFINITY || result == Double.POSITIVE_INFINITY)
					throw new ScriptException("Pow math error");
				else
					getParams().writeLeft(result);
		}});

		/********* GETINDEX ********/
		addInstruction("getindex", 940, DT.struct, DT.number|DT.struct, DT.all, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				StructData struct = (StructData) getParams().inLeft; 

				// In case of an simple index 
				if(getParams().inRight.getType() == DT.number) {
					int index = (int) ((NumberData) getParams().inRight).getValue();
					
					if(index < 0 || index >= struct.getNbData())
						throw new ScriptException("Out of bounds exception in getindex, index is "+index+", size is "+struct.getNbData());
					else
						getParams().setReturn(struct.getDataAt(index).clone());
				}
				else {
					int depth = 0;
					StructData list = (StructData) getParams().inRight;
					if(list.getNbData() == 0)
						throw new ScriptException("Missing the data of the index in setindex");
					
					do {
						Data indexData = list.getDataAt(depth);
						if(indexData.getType() != DT.number)
							throw new ScriptException("The index must be specified by a number in setindex");
						
						int index = (int) ((NumberData) indexData).getValue();
						
						// The index is out of bound of the current struct
						if(index < 0 || index >= struct.getNbData())
							throw new ScriptException("Out of bounds exception, index is "+index+", size is "+struct.getNbData());
						// the end of the index list is still not reached
						else if(depth < list.getNbData() - 1) {
							// but the data is not a structure
							if(struct.getDataAt(index).getType() != DT.struct)
								throw new ScriptException("Cannot access to a no-structure data with setindex");
							// Now seeking in this structure
							struct = (StructData) struct.getDataAt(index);
						}
						// The end of the index list is reached
						// Finnaly return the value
						else
							getParams().setReturn(struct.getDataAt(index).clone());
						
					} while((++depth) < list.getNbData());
				}
		}});
		/********* SETINDEX ********/
		addInstruction("setindex", 930, DT.struct, DT.struct, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				StructData struct = (StructData) getParams().inLeft;
				StructData right =  (StructData) getParams().inRight;
				
				checkParamsStruct(new int[]{DT.number|DT.struct, DT.all}, right, "setindex");
				
				// In case of an simple index 
				if(right.getDataAt(0).getType() == DT.number) {
					int index = (int) ((NumberData) right.getDataAt(0)).getValue();
					
					if(index < 0 || index >= struct.getNbData())
						throw new ScriptException("Out of bounds exception, index is "+index+", size is "+struct.getNbData());
					else if(struct.getDataAt(index).getType() != right.getDataAt(1).getType())
						throw new ScriptException("Incompatible data type in setindex at index "+index);
					else
						struct.getDataAt(index).copyValue(right.getDataAt(1));
				}
				else {
					StructData list = (StructData) right.getDataAt(0);
					if(list.getNbData() == 0)
						throw new ScriptException("Missing the data of the index in setindex");
					
					for(int depth = 0; depth < list.getNbData(); depth++) {
						Data indexData = list.getDataAt(depth);
						if(indexData.getType() != DT.number)
							throw new ScriptException("The index must be specified by a number in setindex");
						
						int index = (int) ((NumberData) indexData).getValue();
						
						// The index is out of bound of the current struct
						if(index < 0 || index >= struct.getNbData())
							throw new ScriptException("Out of bounds exception, index is "+index+", size is "+struct.getNbData());
						// the end of the index list is still not reached
						else if(depth < list.getNbData() - 1) {
							// but the data is not a structure
							if(struct.getDataAt(index).getType() != DT.struct)
								throw new ScriptException("Cannot access to a no-structure data with setindex");
							// Now seeking in this structure
							struct = (StructData) struct.getDataAt(index);
						}
						// The end of the index list is reached
						// But the data has not the same type of the one in the struct
						else if(struct.getDataAt(index).getType() != right.getDataAt(1).getType())
							throw new ScriptException("Incompatible data type in setindex at index "+index);
						// Finnaly write the value
						else
							struct.getDataAt(index).copyValue(right.getDataAt(1));
					}
				}
		}});
		/********* CLEARSTRUCT ********/
		addInstruction("clearstruct", 0, DT.none, DT.struct|DT.writable|DT.notdef, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				if(getParams().inRight.getType() == DT.notdef)
					getParams().inRight = lla.createVar(((NotDefData)getParams().inRight).getName(), DT.struct);

				((StructData) getParams().inRight).clear();
		}});
		/********* NEWSTRUCT ********/
		addInstruction("newstruct", 0, DT.struct|DT.writable|DT.notdef, DT.struct|DT.number, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				Data struct = getParams().inLeft;
				StructData right = (StructData) getParams().inRight;

				checkParamsStruct(new int[]{DT.number|DT.struct, DT.all}, right, "newstruct");
				
				if(struct.getType() == DT.notdef)
					getParams().inLeft = struct = lla.createVar(((NotDefData) struct).getName(), DT.struct);
				
				if(right.getDataAt(0).getType() == DT.number) {
					int size = (int) ((NumberData)right.getDataAt(0)).getValue();
					ArrayList<Data> values = new ArrayList<Data>();
					
					Data d = right.getDataAt(1);
					for(int i = 0; i < size; ++i)
						values.add(d.clone());
					
					((StructData) struct).setValue(values);
				}
				else {
					StructData list = (StructData) right.getDataAt(0);
					if(list.getNbData() == 0)
						throw new ScriptException("Missing the data of the index in newstruct");
					
					Stack<Data> parent = new Stack<Data>();
					Stack<Data> child = new Stack<Data>();
					parent.push(struct);
					
					for(int depth = 0; depth < list.getNbData(); depth++) {
						Data sizeData = list.getDataAt(depth);
						if(sizeData.getType() != DT.number)
							throw new ScriptException("The index must be specified by a number in newstruct");
						int size = (int) ((NumberData) sizeData).getValue();
						
						child.clear();
						while(!parent.empty()) {
							StructData currentStruct = (StructData) parent.pop();
							for(int i = 0; i < size; i++) {
								Data put = (depth < list.getNbData()-1) ? new StructData(true) : right.getDataAt(1).clone();
								child.push(put);
								currentStruct.pushBackData(put);
							}
						}
						Stack<Data> tmp = child;
						child = parent;
						parent = tmp;
					}
				}
		}});
		/********* SIZE ********/
		addInstruction("size", 920, DT.none, DT.struct, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(((StructData)getParams().inRight).getNbData());
		}});
		/********* EMPTY ********/
		addInstruction("empty", 920, DT.none, DT.struct, DT.bool, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(((StructData)getParams().inRight).getNbData() == 0);
		}});
		/********* PUSHBACK ********/
		addInstruction("pushback", 930, DT.struct|DT.writable|DT.notdef, DT.all, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				if(getParams().inLeft.getType() == DT.notdef)
					getParams().inLeft = lla.createVar(((NotDefData)getParams().inLeft).getName(), DT.struct);
				
				((StructData) getParams().inLeft).pushBackData(getParams().inRight.clone());
		}});
		/********* POPBACK ********/
		addInstruction("popback", 940, DT.none, DT.struct|DT.writable, DT.all, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				StructData struct = (StructData) getParams().inRight;
				int size = struct.getNbData(); 
				
				if(size == 0)
					throw new ScriptException("Cannot pop back an empty structure");
				
				getParams().setReturn(struct.getDataAt(size-1));
				
				struct.popBackData();
		}});
		/********* PUSHFRONT ********/
		addInstruction("pushfront", 930, DT.struct|DT.writable|DT.notdef, DT.all, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				if(getParams().inLeft.getType() == DT.notdef)
					getParams().inLeft = lla.createVar(((NotDefData)getParams().inLeft).getName(), DT.struct);
				
				((StructData) getParams().inLeft).pushFrontData(getParams().inRight.clone());
				
		}});
		/********* POPFRONT ********/
		addInstruction("popfront", 9400, DT.none, DT.struct|DT.writable, DT.all, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				StructData struct = (StructData) getParams().inRight;
				int size = struct.getNbData(); 
				
				if(size == 0)
					throw new ScriptException("Cannot pop front an empty structure");
				
				getParams().setReturn(struct.getDataAt(0)); 
				
				struct.popFrontData();
		}});
		/********* REMOVE ********/
		addInstruction("remove", 0, DT.struct|DT.writable, DT.number, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				StructData struct = (StructData) getParams().inLeft; 
				
				int index = (int) ((NumberData) getParams().inRight).getValue();
				
				if(index < 0 || index >= struct.getNbData())
					throw new ScriptException("Out of bounds exception in remove, index is "+index+", size is "+struct.getNbData());
				else
					struct.popData(index);
		}});
		/********* CONCAT ********/
		addInstruction("concat", 930, DT.struct|DT.writable, DT.struct, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				((StructData) getParams().inLeft).getValue().addAll(((StructData) getParams().inRight).getValue());
				//((StructData) getParams().inLeft).pushBackData(getParams().inRight.clone());
		}});
		/********* INSERT ******** /
		instructions.put("insert", new InstructionDefinition("insert", 0, DT.struct|DT.writable, DT.struct, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				StructData struct = (StructData) getParams().inLeft; 
				
				int index = (int) ((NumberData) getParams().inRight).getValue();
				
				if(index < 0 || index >= struct.getNbData())
					throw new ScriptException("Out of bounds exception in remove, index is "+index+", size is "+struct.getNbData());
				else
					struct.popData(index);
		}});*/

		/********* CLEARTEXT ********/
		addInstruction("cleartext", 0, DT.none, DT.none, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				lla.out(LowLevelAccess.CommandOut.CLEAR, null);
		}});
		/********* PRINT ********/
		addInstruction("print", 0, DT.none, DT.all, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				StringBuilder txtOut;
				
				if(getParams().inRight.getType() == DT.string)
					txtOut = ((StringData) getParams().inRight).getValue();
				else
					txtOut = new StringBuilder(getParams().inRight.toString());
				
				lla.out(LowLevelAccess.CommandOut.WRITE, txtOut);
				
		}});
		/********* PRINTLN ********/
		addInstruction("println", 0, DT.none, DT.all, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				StringBuilder txtOut;
				
				if(getParams().inRight.getType() == DT.string)
					txtOut = new StringBuilder(((StringData) getParams().inRight).getValue());
				else
					txtOut = new StringBuilder(getParams().inRight.toString());
				
				lla.out(LowLevelAccess.CommandOut.WRITE, txtOut.append("\n"));
		}});
		/********* READNB ********/
		addInstruction("readnb", 0, DT.none, DT.number|DT.writable|DT.notdef, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				if(getParams().inRight.getType() == DT.notdef)
					getParams().inRight = lla.createVar(((NotDefData)getParams().inRight).getName(), DT.number);
				
				lla.in(LowLevelAccess.CommandIn.NUMBER, getParams().inRight);
		}});
		/********* READBOOL ********/
		addInstruction("readbool", 0, DT.none, DT.bool|DT.writable|DT.notdef, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				if(getParams().inRight.getType() == DT.notdef)
					getParams().inRight = lla.createVar(((NotDefData)getParams().inRight).getName(), DT.bool);
				
				lla.in(LowLevelAccess.CommandIn.BOOLEAN, getParams().inRight);
		}});
		/********* SCAN ********/
		addInstruction("scan", 0, DT.none, DT.string|DT.writable|DT.notdef, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				if(getParams().inRight.getType() == DT.notdef)
					getParams().inRight = lla.createVar(((NotDefData)getParams().inRight).getName(), DT.string);
				
				lla.in(LowLevelAccess.CommandIn.STRING, getParams().inRight);
		}});
		/********* STOP ********/
		addInstruction("stop", 0, DT.none, DT.none, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				lla.end();
		}});
		/********* PAUSE ********/
		addInstruction("pause", 0, DT.none, DT.none, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				lla.in(LowLevelAccess.CommandIn.PAUSE, null);
		}});

		/********* SUBSTR ********/
		addInstruction("substr", 940, DT.string, DT.struct, DT.string, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				StructData sd = (StructData) getParams().inRight;
				checkParamsStruct(new int[]{DT.number, DT.number}, sd, "substr");
				
				int start = (int) ((NumberData)sd.getDataAt(0)).getValue();
				int end = (int) ((NumberData)sd.getDataAt(1)).getValue();
				final StringBuilder s = ((StringData)getParams().inLeft).getValue();
				
				if(0 > start || start > end || end > s.length())
					throw new ScriptException("Out of bounds exception in getsubstr");
				
				getParams().setReturn(new StringBuilder(s.subSequence(start, end)));
		}});
		/********* REPLACE ********/
		addInstruction("replace", 930, DT.string|DT.writable, DT.struct, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				StructData sd = (StructData) getParams().inRight;
				checkParamsStruct(new int[]{DT.number, DT.string}, sd, "substr");

				StringBuilder rempl = ((StringData)sd.getDataAt(1)).getValue();
				int start = (int) ((NumberData)sd.getDataAt(0)).getValue();
				int end = start+rempl.length();
				StringBuilder s = ((StringData)getParams().inLeft).getValue();
				try {
					s.replace(start, end, rempl.toString());// bad for performance if 'rempl' is very big
				} catch(StringIndexOutOfBoundsException e) {
					throw new ScriptException("Out of bounds exception in replace");
				}
		}});
		/********* CHARAT ********/
		addInstruction("charat", 940, DT.string, DT.number, DT.string, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				int index = (int) ((NumberData)getParams().inRight).getValue();
				StringBuilder s = ((StringData)getParams().inLeft).getValue();
				
				if(index < 0 || index >= s.length())
					throw new ScriptException("Out of bounds exception in charAt");
				
				getParams().setReturn(new StringBuilder().appendCodePoint(s.codePointAt(index)));
		}});
		/********* TOUPPER ********/
		addInstruction("toupper", 930, DT.none, DT.string, DT.string, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(new StringBuilder(((StringData)getParams().inRight).getValue().toString().toUpperCase()));
		}});
		/********* TOLOWER ********/
		addInstruction("tolower", 930, DT.none, DT.string, DT.string, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(new StringBuilder(((StringData)getParams().inRight).getValue().toString().toLowerCase()));
		}});
		/********* LENGTH ********/
		addInstruction("length", 920, DT.none, DT.string, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(((StringData)getParams().inRight).getValue().length());
		}});

		/********* SLEEP ********/
		addInstruction("sleep", 0, DT.none, DT.number, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				long timeToSleep = (long) (((NumberData)getParams().inRight).getValue() * 1000);
				if(timeToSleep <= 0)
					return;
				//if(timeToSleep > 10*1000)
				//	throw new ScriptException("Sleeping time must be less than 10 sec");
				
				lla.sleep(timeToSleep);
		}});
		/********* CLOCK ********/
		addInstruction("clock", 999, DT.none, DT.none, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(lla.getElapsedTime());
		}});
		/********* FUNC ********/
		addInstruction("func", 0, DT.none, DT.all|DT.writable, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				//(TODO) nothing / it should not be executed
		}});
		/********* FUNCL ********/
		addInstruction("funcl", 0, DT.none, DT.all|DT.writable, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				//(TODO) nothing / it should not be executed
		}});
		/********* FUNCR ********/
		addInstruction("funcr", 0, DT.none, DT.all|DT.writable, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				//(TODO) nothing / it should not be executed
		}});
		/********* FUNCLR ********/
		addInstruction("funclr", 0, DT.none, DT.all|DT.writable, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				//(TODO) nothing / it should not be executed
		}});
		
		/********* ENDFUNC ********/
		addInstruction("endfunc", 0, DT.none, DT.none, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				lla.popContext();
		}});
		/********* RETURN ********/
		addInstruction("return", 0, DT.none, DT.all, DT.none, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				lla.popContext(getParams().inRight.clone());
		}});
		/********* ARGRIGHT ********/
		addInstruction("argright", 1000, DT.none, DT.none, DT.all, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(lla.getParameterRight()/*.clone() TODO check compatibility */);
		}});
		/********* ARGLEFT ********/
		addInstruction("argleft", 1000, DT.none, DT.none, DT.number, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				getParams().setReturn(lla.getParameterLeft()/*.clone() TODO check compatibility */);
		}});
		/********* ARG ********/
		addInstruction("arg", 940, DT.none, DT.number, DT.all, new RunnableFunction() {
			@Override public void run(int param) throws ScriptException {
				int index = (int) ((NumberData) getParams().inRight).getValue();
				if(lla.getParameterRight().type != DT.struct)
					throw new ScriptException("The right argument is not a structure, it cannot be accessed with \'arg\'");
				else {
					StructData struct = (StructData) lla.getParameterRight();
					if(index < 0 || index >= struct.getNbData())
						throw new ScriptException("Out of bounds exception in \'arg\', index is "+index+", size is "+struct.getNbData());
					else
						getParams().setReturn(struct.getDataAt(index).clone());
				}
		}});
	}
	
	/** check type of the data in the struct with the model paramsType (using flag : number, string, struct, boolean) **/
	static private boolean checkParamsStruct(int[] paramsType, StructData sd, String funcName) throws ScriptException {
		int l = paramsType.length;
		if(sd.getNbData() < l)
			throw new ScriptException("Not enough parameters for " + funcName);
		else if(sd.getNbData() > l)
			throw new ScriptException("Too many parameters for " + funcName);
		
		for(int i = paramsType.length-1; i >= 0; --i)
			if((paramsType[i] & sd.getDataAt(i).getType()) == 0)
				throw new ScriptException("Parameters n�" + (i+1) + " does not match for " + funcName);
		
		return true;
	}
	
	static public boolean isExisting(String name) {
		return instructions.containsKey(name);
	}
	static public InstructionDefinition get(String name) {
		return instructions.get(name);
	}
	
	
	static class InstructionDefinition {
		String name;
		// DataType
		final public int before, returned, after;
		public final RunnableFunction run;
		/** Priority
		 * from 0 to 1000 : constants, accessor func, math func, operator, comparator, logical operators, ternary operator, terminal func (whithout returning statement)
		 * 					1000		900				800			700		600			  500				400					0
		 * 1000    : constant values
		 * 900~999 : operator
		 * 800~899 : mathematic function
		 * 700~799 : 
		 */
		public final int priority;
		
		public InstructionDefinition(String name, int priority, int dataTypeLeft, int dataTypeRight, int dataTypeReturn, RunnableFunction run) {
			before = dataTypeLeft;
			after = dataTypeRight;
			returned = dataTypeReturn;

			this.name = name;
			this.priority = priority;
			
			this.run = run;
		}

		public boolean isEligibleLeft(Data data) {
			boolean staticCompatible = ((before & DT.writable) == DT.writable) ? !data.isStatic() || data.getType() == DT.notdef : true;
			boolean typeCompatible = (data.getType() & before & DT.data_mask) != 0;
			
			//Log.i("###", ""+data.getType()+ "&" + before +"&"+ DT.data_mask+ " => " + (data.getType() & before & DT.data_mask));
			//Log.i("###", "(before & DT.writable):" + (before & DT.writable) +" !data.isStatic():"+ !data.isStatic() + " isUndef;"+(data.getType() == DT.undef));
			
			return typeCompatible && staticCompatible;
		}
		public boolean isEligibleRight(Data data) {
			boolean writeCompatible = ((after & DT.writable) == DT.writable) ? !data.isStatic() || data.getType() == DT.notdef : true;
			boolean typeCompatible = (data.getType() & after & DT.data_mask) != 0;

			//Log.i("###", ""+Integer.toBinaryString(data.getType())+ "&" + Integer.toBinaryString(after) +"&"+Integer.toBinaryString(DT.data_mask)+ " => " + (data.getType() & after & DT.data_mask));
			//Log.i("###", "(after & DT.writable):" + (after & DT.writable) +" !data.isStatic():"+ !data.isStatic() + " isUndef;"+(data.getType() == DT.undef));
			
			return typeCompatible && writeCompatible;
		}
		
		Parameters getParams() {
			return run.getParams();
		}
	}
	
	static abstract class RunnableFunction {
		static final int NO_PARAM = -1;
		abstract public void run(int param) throws ScriptException;
		
		Runnable runnable;
		private final Parameters params;
		
		public RunnableFunction() {
			params = new Parameters();
			runnable = null;
		}
		
		public Parameters getParams() {
			return params;
		}
		
		public void setParams(Data inLeft, Data inRight, Data out) {
			params.inLeft = inLeft;
			params.inRight = inRight;
			params.out = out;
		}
	}
	
	static class Parameters {
		Data inLeft, inRight, out;
		// Context, mainactivity, host, script (for in out function, jump line)
		
		public Parameters() {
			inLeft = null;
			inRight = null;
			out = null;
		}
		
		/* /!\ ne clone pas */
		public void writeLeft(Data d) {
			if(inLeft == null)
				inLeft = d.clone();
			else if(d.getType() == inLeft.getType()) {
				inLeft.copyValue(d);
			}
		}
		public void writeLeft(boolean bool) {
			if(inLeft == null)
				inLeft = new BooleanData(bool, false);
			else if(inLeft.getType() == DT.bool)
				((BooleanData)inLeft).setValue(bool);
		}
		public void writeLeft(StringBuilder str) {
			if(inLeft == null)
				inLeft = new StringData(str, false);
			else if(inLeft.getType() == DT.string)
				((StringData)inLeft).setValue(str);
		}
		public void writeLeft(double nb) {
			if(inLeft == null)
				inLeft = new NumberData(nb, false);
			else if(inLeft.getType() == DT.number)
				((NumberData)inLeft).setValue(nb);
		}
		public void writeLeft(ArrayList<Data> struct) {
			if(inLeft == null)
				inLeft = new StructData(struct, false);
			else if(inLeft.getType() == DT.struct)
				((StructData)inLeft).setValue(struct);
		}

		/* /!\ ne clone pas */
		public void writeRight(Data d) {
			if(inRight == null)
				inRight = d.clone();
			else if(d.getType() == inRight.getType()) {
				inRight.copyValue(d);
			}
		}
		public void writeRight(boolean bool) {
			if(inRight == null)
				inRight = new BooleanData(bool, false);
			else if(inRight.getType() == DT.bool)
				((BooleanData)inRight).setValue(bool);
		}
		public void writeRight(StringBuilder str) {
			if(inRight == null)
				inRight = new StringData(str, false);
			else if(inRight.getType() == DT.string)
				((StringData)inRight).setValue(str);
		}
		public void writeRight(double nb) {
			if(inRight == null)
				inRight = new NumberData(nb, false);
			else if(inRight.getType() == DT.number)
				((NumberData)inRight).setValue(nb);
		}
		public void writeRight(ArrayList<Data> struct) {
			if(inRight == null)
				inRight = new StructData(struct, false);
			else if(inRight.getType() == DT.struct)
				((StructData)inRight).setValue(struct);
		}
		
		/*/!\ ne clone pas */
		public void setReturn(Data d) {
			out = d;
		}
		public void setReturn(boolean bool) {
			out = new BooleanData(bool, true);
		}
		public void setReturn(StringBuilder str) {
			out = new StringData(str, true);
		}
		public void setReturn(double nb) {
			out = new NumberData(nb, true);
		}
		public void setReturn(ArrayList<Data> struct) {
			out = new StructData(struct, true);
		}
	}
}
