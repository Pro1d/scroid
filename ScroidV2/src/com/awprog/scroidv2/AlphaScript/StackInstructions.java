package com.awprog.scroidv2.AlphaScript;

import java.util.Stack;

import com.awprog.scroidv2.AlphaScript.ArrayOfItems.Item;
import com.awprog.scroidv2.AlphaScript.Data.DT;
import com.awprog.scroidv2.AlphaScript.ErrorDialog.ScriptException;
import com.awprog.scroidv2.AlphaScript.Instructions.InstructionDefinition;

public class StackInstructions {
	/** Pile d'instruction **/ 
	Stack<RunnableInstruction> stack;
	
	public boolean isEmpty() {
		return stack.empty();
	}
	@SuppressWarnings("unchecked")
	public StackInstructions(StackInstructions si) {
		stack = (Stack<RunnableInstruction>) si.stack.clone();
	}
	public StackInstructions() {
		stack = new Stack<RunnableInstruction>();
	}
	/** Clone cette pile d'instructions */
	@Override
	protected Object clone() {
		return new StackInstructions(this);
	}
	
	/** Construit la pile d'isntruction à partir d'une ligne formatée */
	static class NoReccObj { int start, end; ArrayOfItems array; public NoReccObj(ArrayOfItems a, int s, int e) {start = s; end = e; array = a;}}
	static public StackInstructions create(ArrayOfItems line) throws ScriptException {
		// Objet cible à construire
		StackInstructions si = new StackInstructions();
		/// Stack utilisée pour éviter d'utiliser la réccursivité
		Stack<NoReccObj> noRecc = new Stack<NoReccObj>();
		
		noRecc.push(new NoReccObj(line, 0, line.size()));
		
		while(!noRecc.isEmpty()) {
			NoReccObj o = noRecc.pop();
			int posPrMin = getMinPriority(o.array, o.start, o.end);
			RunnableInstruction ri = new RunnableInstruction();
			
			// pas de fonction à executer
			if(posPrMin == -1) {
				if(o.end - o.start > 1)
					throw new ScriptException("Unexpected data from "+o.array.get(o.start).data+" to "+o.array.get(o.end-1).data);// Bad message for two-words line
				else {
					Item i = o.array.get(o.start);
					switch(i.type) {
						case NUMBER:
							ri.type = RunnableInstruction.Type.DATA;
							ri.data = new NumberData(i.getNumber(), true);
							si.stack.push(ri);
							break;
						case STRING:
							ri.type = RunnableInstruction.Type.DATA;
							ri.data = new StringData(new StringBuilder(i.getString()), true);
							si.stack.push(ri);
							break;
						case SUB_LIST:
							ri.type = RunnableInstruction.Type.SUBLIST;
							ri.sublistSize = i.sublist.size();
							si.stack.push(ri);
							for(ArrayOfItems itemList : i.sublist)
								noRecc.push(new NoReccObj(itemList, 0, itemList.size()));
							break;
						case WORD:
							ri.type = RunnableInstruction.Type.VAR;
							ri.varName = i.getWord();
							si.stack.push(ri);
							break;
					}
				}
			}
			else {
				Item ifct = o.array.get(posPrMin);
				
				/// Instruction \\\
				ri.type = RunnableInstruction.Type.INSTR;
				ri.instrDef = Instructions.get(ifct.getWord());
				ri.targetLine = ifct.targetLine; // target line pour les les structures conditionnelles et les boucles
				si.stack.push(ri);
				
				/// Absence de valeur retournée inattendue \\\
				// TODO check compatibility with manual func 
				if(ri.instrDef.returned == DT.none && si.stack.size() > 1)
					throw new ScriptException("The function \""+ifct.getWord()+"\" does not return data, but it is required here");
				
				/// Présence de données à gauche \\\ 
				if(posPrMin - o.start >= 1) { 
					if(Instructions.get(ifct.data).before == DT.none)
						throw new ScriptException("The function \""+ifct.getWord()+"\" does not take left paramater. Unexpected data before \'" + ifct.getWord() + "\'");
					else
						noRecc.push(new NoReccObj(o.array, o.start, posPrMin));//ri.left = getRunInstr(line, start, posPrMin);
				}
				/// Absence de données à gauche inattendue \\\
				else if(Instructions.get(ifct.data).before != Data.DT.none)
					throw new ScriptException("The function \""+ifct.getWord()+"\" must have left paramater. Missing data before \'" + ifct.getWord() + "\'");
				
				/// Présence de données à droite \\\
				if(o.end - posPrMin - 1 >= 1) {
					if(Instructions.get(ifct.data).after == DT.none)
						throw new ScriptException("The function \""+ifct.getWord()+"\" does not take right paramater. Unexpected data after \'" + ifct.getWord() + "\'");
					else
						noRecc.push(new NoReccObj(o.array, posPrMin+1, o.end));//ri.right = getRunInstr(line, posPrMin+1, end);
				}
				/// Absence de données à droite inattendue \\\
				else if(Instructions.get(ifct.data).after != DT.none)
					throw new ScriptException("The function \""+ifct.getWord()+"\" must have right paramater. Missing data after \'" + ifct.getWord() + "\'");
				
			}
		}
		
		si.stack.trimToSize();
		
		return si;
	}
	
	/** Returns the position of the function with the lowest priority (ignoring each one between parathesis), returns -1 if there are not function  (data only or nothing) **/
	private static int getMinPriority(ArrayOfItems line, int start, int end) {
		int priority = -1, pos = -1;
		boolean found = false;
		
		for(int i = start; i < end; ++i) {
			Item item = line.get(i);
			//Log.i("###", "getMinPr i:"+i+" t:" + item.type+"("+ArrayOfItems.Item.WORD+")" + " d:"+item.data);
			if(item.type == ArrayOfItems.Item.Type.WORD && /*TODO fonction*/Instructions.isExisting(item.data)) {
				int curPr = Instructions.get(item.data).priority;
				if(!found || curPr <= priority) { // '<=' in case of instr with the same priority : the lowest priority for the last instr
					found = true;
					pos = i;
					priority = curPr;
				}
			}
		}
		//Log.i("###", "getMinPr result: "+ ((pos!=-1)?" d:"+line.get(pos).data:"")+" pr:"+priority+ " pos:"+pos);
		
		return pos;
	}
	
	static class RunnableInstruction {
		enum Type { INSTR, SUBLIST, VAR, DATA, UNKNOWN, NOTHING }
		
		Type type = Type.UNKNOWN;
		
		// INSTR - the function to be executed
		InstructionDefinition instrDef = null;
		// valeur relative du saut du compteur ordinal pour les structures (if, while, ...)
		int targetLine = -1;
		//RunnableInstr left = null, right = null;
		
		// SUBLIST - a list of instruction (in case of struct)
		int sublistSize = 0;
		
		// DATA - a static data
		Data data = null;
		
		// VAR - an undefined/still unknwon variable
		String varName = null;
	}
}
