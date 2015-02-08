package com.awprog.scroidv2.AlphaScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import android.os.SystemClock;
import android.util.Log;

import com.awprog.scroidv2.Project;
import com.awprog.scroidv2.StandardIOService;
import com.awprog.scroidv2.AlphaScript.Data.DT;
import com.awprog.scroidv2.AlphaScript.ErrorDialog.ScriptException;
import com.awprog.scroidv2.AlphaScript.Instructions.InstructionDefinition;
import com.awprog.scroidv2.AlphaScript.Instructions.Parameters;
import com.awprog.scroidv2.AlphaScript.StackInstructions.RunnableInstruction;

public class ScriptRunner implements LowLevelAccess {
	private static final int maxStackSize = 1000;
	
	private Stack<Context> stackContext = new Stack<Context>();
	private Context currentContext;// le contexte actuel n'est pas dans la pile de contexte
	private HashMap<String, ArrayList<StackInstructions>> compiledFiles;
	StandardIOService ioService;
	
	public ScriptRunner(HashMap<String, ArrayList<StackInstructions>> cf) {
		compiledFiles = cf;
		Instructions.setLLA(this);
	}
	public void setIOService(StandardIOService ioSrv) {
		ioService = ioSrv;
	}
	
	private volatile boolean running = false; /// boolean utilis� pour conna�tre l'�tat de l'ex�cution, et non pour le commander
	private volatile boolean end = false, pause = false, oneStep = false, stepByStepMode = false;
	private boolean waitingForInput = false;
	
	private  void reset() {
		running = false;
		end = false;
		pause = false;
		oneStep = false;
		waitingForInput = false;
		currentContext = new Context(Project.MAIN_FILE_NAME, 0, null, null);
		stackContext.clear();
		initializeTime();
	}
	public boolean isRunning() {
		return running;
	}
	public boolean isEnded() {
		return end;
	}
	
	/// Execute le script, se termine quand l'execution est interrompue
	public void run() throws ScriptException {
		reset();
		if(ioService == null) { // TODO useless
			Log.d("###", "The script runner does not have an attached I/O service");
			return;
		}
		
		running = true;

		// TODO � completer ? � v�rifier ? 
		while(true) {
			// En pause -> Attente du 'resume' si on est pas en pas-�-pas, ou de la fin de l'ex�cution
			while(pause && !oneStep && !end)
				try { Thread.sleep(5);
				} catch (InterruptedException e) {}
			
			// Fin de l'execution
			if(end)
				break;
			
			// Instruction suivante
			RunnableInstruction ri = currentContext.getNextInstruction();
			if(ri != null)
				try {
					runInstruction(ri);
				} catch (ScriptException e) {
					e.setCat("Running");
					e.setFile(currentContext.file);
					e.setLine(currentContext.line-1);
					running = false;
					throw e;
				}
			else //if()
				end = true;
				
			oneStep = false;
		}
		
		running = false;
	}
	
	// faire un pas :P
	public void nextStep() {
		if(stepByStepMode)
			oneStep = true;
	}
	
	// Activer/d�sactiver le mode pas � pas
	public void enableStepByStep() {
		stepByStepMode = true;
	}
	public void disableStepByStep() {
		stepByStepMode = false;
	}
	public void pause() {
		pause = true;
	}
	public void resume() {
		pause = false;
	}
	public void stop() {
		end = true;
		if(waitingForInput) {

		}
		/// TODO : Interrupt waiting input if existing
	}
	
	private void runInstruction(StackInstructions.RunnableInstruction instruction) throws ScriptException {
		switch(instruction.type) {
			// STATIC DATA
			case DATA:
				currentContext.stackData.push(instruction.data);
				break;
				
			// VARIABLE
			case VAR:
				if(isExistingVar(instruction.varName))
					currentContext.stackData.push(getVar(instruction.varName));
				else
					currentContext.stackData.push(new NotDefData(instruction.varName));
				break;
			
			// INSTRUCTION
			case INSTR:
				final InstructionDefinition idef = instruction.instrDef;
				Data leftParams, rightParams;
	
				/// Get right parameter from the stack \\\
				if(idef.after != DT.none) {
					rightParams = currentContext.stackData.pop();
					if(!idef.isEligibleRight(rightParams))
						throw new ScriptException("Incompatible data type after \'" + instruction.instrDef.name + "\'");
				}
				else
					rightParams = null;
	
				/// Get left parameter from the stack \\\
				Parameters params = idef.getParams(); 
				if(idef.before != DT.none) {
					leftParams = currentContext.stackData.pop();
					if(!(idef.isEligibleLeft(leftParams)))
						throw new ScriptException("Incompatible data type before \'" + instruction.instrDef.name + "\'");
				}
				else
					leftParams = null;
				
				
				/// Run the function \\\
				idef.getParams().inLeft = leftParams;
				idef.getParams().inRight = rightParams;

				idef.run.run(instruction.targetLine);
				
				/// Push the returned data \\\
				if(idef.returned != DT.none)
					currentContext.stackData.push(params.out);
				// TODO ? else
				//	currentContext.stackData.push(null); // Must return null even if there is not result, 
				
				break;
			
			// LIST
			case SUBLIST:
				/// If the struct contains only one item, just push it <=> do nothing
				/// else rebuild the struct and push it
				if(instruction.sublistSize > 1) {
					ArrayList<Data> list = new ArrayList<Data>(instruction.sublistSize);
					/// Get data from the stack
					for(int i = instruction.sublistSize; --i >= 0;) {
						list.add(0, currentContext.stackData.pop());
						/* TODO (cas impossible d'apr�s la construction de la stack d'instructions)
						if(data == null)
							throw new ScriptException("Unexpected value in the struct");*/
					}
					currentContext.stackData.push(new StructData(list, true));
				}
				break;
		default:
			throw new ScriptException("Unknown error");
		}
	}
	
	/***** Fonctions accessible depuis les instructions : LowLevelAccess *****/
	
	/**** Context & Cie ****/
	/** Change le compteur ordinal :P */
	public void setNextLine(int line) {
		new Exception().printStackTrace();
		/*stackContext.peek()*/currentContext.line = line;
	}
	/** Change de contexte (appel de fonction) */
	public void pushContext(String file, int line, Data left, Data right) throws ScriptException {
		stackContext.push(currentContext);
		currentContext = new Context(file, line, left, right);
		// Vérification de la taille de la pile
		if(stackContext.size() > maxStackSize)
			throw new ScriptException("Stack overflow : the call stack is limited to a size of "+maxStackSize);
	}
	/** Donne les param�tres donn�s � la fonction actuelle */
	public Data getParameterLeft() throws ScriptException {
		if(currentContext.paramLeft == null)
			throw new ScriptException("The left parameter does not exist");
		return currentContext.paramLeft;
	}
	public Data getParameterRight() throws ScriptException {
		if(currentContext.paramRight == null)
			throw new ScriptException("The right parameter does not exist");
		return currentContext.paramRight;
	}
	/** Pour sortir d'une fonction */
	public void popContext() {
		currentContext = stackContext.pop();
		
	}
	/** Pour sortir d'une fonction et retourner une valeur */
	public void popContext(Data returnedData) {
		currentContext = stackContext.pop();
		currentContext.stackData.push(returnedData);
	}
	/** Mets fin d�finitivement � l'ex�cution */
	public void end() {
		end = true;
	}
	
	/**** Acc�s variable ****/
 	/** Cr�e une nouvelle variable (si in�xistante) dans le contexte actuelle et la retourne 
	 * @throws ScriptException */
	public Data createVar(String name, int type) throws ScriptException {
		Data newVar;
		
		if(isExistingVar(name)) {
			Data existingVar = getVar(name);
			if(existingVar.getType() == type)
				return existingVar;
			else
				throw new ScriptException("The variable \'" + name + "\' is already defined as a " + existingVar.getTypeName());
		} else {
			switch(type) {
				case DT.bool:
					newVar = new BooleanData(false, false);
					break;
				case DT.number:
					newVar = new NumberData(0.0, false);
					break;
				case DT.string:
					newVar = new StringData(new StringBuilder(""), false);
					break;
				case DT.struct:
					newVar = new StructData(false);
					break;
				default:
					newVar = null;
					break;
			}
			currentContext.localVariable.put(name, newVar);

			//Log.w("###", "createVar n:"+name+" t:"+type);
			return newVar;
		}
		
	}
	/** Indique si la variable existe dans ce contexte */
	public boolean isExistingVar(String name) {
		return currentContext.localVariable.containsKey(name);
	}
	/** Donne la r�f�rence de la variable */
	public Data getVar(String name) {
		return currentContext.localVariable.get(name);
	}
	
	/**** Temps ****/
	private static final int DELTA_SLEEP_TIME = 1/*ms*/;
	public void sleep(long millis) {
		long startSleep = SystemClock.elapsedRealtime();
		
		while(SystemClock.elapsedRealtime() - startSleep < millis && !end /*&& !pause TODO check this */) {
			try {
				Thread.sleep(DELTA_SLEEP_TIME);
			} catch (InterruptedException e) { }
		}
	}
	private long startTime=0;
	public void initializeTime() {
		startTime = SystemClock.elapsedRealtime();
	}
	public double getElapsedTime() {
		return (double)(SystemClock.elapsedRealtime() - startTime) / 1000.0;
	}
	
	/**** Entr�e / Sortie standard ****/
	public void out(CommandOut cmd, StringBuilder data) {
		switch(cmd) {
		case WRITE:
			ioService.writeText(data);
			break;
		case CLEAR:
			ioService.writeText(null);
			break;
		}
	}
	public void in(CommandIn cmd, Data data) {
		waitingForInput = true;
		
		switch(cmd) {
		case BOOLEAN:
			((BooleanData)data).setValue(ioService.readBoolean(this));
			break;
		case NUMBER:
			((NumberData)data).setValue(ioService.readNumber(this));
			break;
		case PAUSE:
			ioService.waitButton(this);
			break;
		case STRING:
			((StringData)data).setValue(new StringBuilder(ioService.readString(this)));
			break;
		}
		
		waitingForInput = false;
	}
	
	private class Context {
		String file;
		int line;
		HashMap<String, Data> localVariable = new HashMap<String, Data>();
		
		Stack<Data> stackData = new Stack<Data>();
		StackInstructions instructions = null;
		ArrayList<StackInstructions> fileInstructions = null;
		final Data paramLeft, paramRight;
		
		Context(String _file, int _line, Data _paramLeft, Data _paramRight) {
			file = _file;
			line = _line; 
			fileInstructions = compiledFiles.get(_file);
			
			instructions = (StackInstructions) fileInstructions.get(line).clone();
			line++;
			
			paramLeft = _paramLeft;
			paramRight = _paramRight;
		}

		/// Retourne la prochaine instruction � ex�cuter, null si la fin du fichier est atteinte
		RunnableInstruction getNextInstruction() {
			/// Si la ligne est termin�e, on cherche la prochaine ligne non vide
			while(instructions.stack.isEmpty()) {
				if(line >= fileInstructions.size())
					return null;
				
				instructions = (StackInstructions) fileInstructions.get(line).clone();
				line++;
				//oneStep = false;// TODO copatibilit� changement de contexte
			}
			return instructions.stack.pop();
		}
	}

}

/*
public class ScriptExec {
	final ScriptHost host;
	
	public ScriptExec(ScriptHost h) {
		host = h;
	}
	static final int FINISHED = 0, PAUSED = 1, ABORTED = 2, CALL_DONE = 3;
	public int run(StackInstr si) throws ScriptException {
		RunnableInstr ri;
		
		// TODO : interrupt if the host requiere
		while((ri = si.getNextInstruction()) != null) {
			
			switch(ri.type) {
			// STATIC DATA
			case RunnableInstr.t_data:
				si.pushData(ri.data);
				break;
				
			// VARIABLE
			case RunnableInstr.t_var:
				if(host.varIsExisting(ri.varName))
					si.pushData(host.getVar(ri.varName));
				else
					si.pushData(new NotDefData(ri.varName));
				break;
			
			// INSTRUCTION
			case RunnableInstr.t_instr:
				final InstructionDefinition idef = ri.instrDef;
				Data leftParams, rightParams;

				/// Get right parameter from the stack \\\
				if(idef.after != DT.none) {
					rightParams = si.popData();
					if(rightParams == null)
						throw new ScriptException("Missing data type after \'" + ri.instrDef.name + "\'");
					else if(!idef.isEligibleRight(rightParams))
						throw new ScriptException("Incompatible data type after \'" + ri.instrDef.name + "\'");
				}
				else
					rightParams = null;

				/// Get left parameter from the stack \\\
				Parameters params = idef.getParams(); 
				if(idef.before != DT.none) {
					leftParams = si.popData();
					if(leftParams == null)
						throw new ScriptException("Missing data type before \'" + ri.instrDef.name + "\'");
					else if(!(idef.isEligibleLeft(leftParams)))
						throw new ScriptException("Incompatible data type before \'" + ri.instrDef.name + "\'");
				}
				else
					leftParams = null;
				
				
				/// Run the function \\\
				idef.getParams().inLeft = leftParams;
				idef.getParams().inRight = rightParams;
				// TODO : case of call
				idef.run.run();
				
				/// Push the returned data \\\
				if(idef.returned != DT.none)
					si.pushData(params.out);
				else
					si.pushData(null); // Must return null even if there is not result, 
				
				break;
			
			// LIST
			case RunnableInstr.t_riList:
				ArrayList<Data> list = new ArrayList<Data>();
				/// Get data from the stack
				for(int i = ri.runInstrList.size(); i > 0; i--) {
					Data data = si.popData();
					if(data == null)
						throw new ScriptException("Unexpected value in the struct");
					list.add(0, data);//** /*  /* ///* /* / � ajouter � la fin (push) pour la nouvelle stack d'instruction
				}
				si.pushData(new StructData(list, true));
				break;
			}
		}
		
		return FINISHED;
	}
	
	public StackInstr getStackInstr(RunnableInstr instr) throws ScriptException {
		return new StackInstr(instr);
	}
	/*
	public Data run(RunnableInstr instr) throws ScriptException {
		// TODO : really possible ? when run from left or right params, can't be null because the quantity paramas is already checked
		//Log.w("###", "run called");
		if(instr == null) {
			Log.e("###", "instr = null");
			return null;
		}
		
		switch(instr.type) {
			// STATIC DATA
			case RunnableInstr.t_data:
				return instr.data;

			// VARIABLE
			case RunnableInstr.t_var:
				if(host.varIsExisting(instr.varName))
					return host.getVar(instr.varName);
				else
					return new NotDefData(instr.varName);
			
			// INSTRUCTION
			case RunnableInstr.t_instr:
				final InstructionDefinition idef = instr.instrDef;
				Data leftParams, rightParams;

				/// Sets parameters \\\
				Parameters params = idef.getParams(); 
				if(idef.before != DT.none) {
					leftParams = run(instr.left);
					if(leftParams == null)
						throw new ScriptException("Missing data type before \'" + instr.instrDef.name + "\'");
					else if(!(idef.isEligibleLeft(leftParams)))
						throw new ScriptException("Incompatible data type before \'" + instr.instrDef.name + "\'");
				}
				else
					leftParams = null;
				
				if(idef.after != DT.none) {
					rightParams = run(instr.right);
					if(rightParams == null)
						throw new ScriptException("Missing data type after \'" + instr.instrDef.name + "\'");
					else if(!idef.isEligibleRight(rightParams))
						throw new ScriptException("Incompatible data type after \'" + instr.instrDef.name + "\'");
				}
				else
					rightParams = null;
				
				
				/// Runs the function \\\
				idef.getParams().inLeft = leftParams;
				idef.getParams().inRight = rightParams;
				idef.run.run();
				
				/// Returns the received data (or not) \\\
				if(idef.returned != DT.none)
					return params.out;
				else
					return null;
			
			// LIST
			case RunnableInstr.t_riList:
				if(instr.runInstrList.size() == 0)
					throw new ScriptException("Valid data not found between the paranthesis");
				else if(instr.runInstrList.size() == 1) // A simple StructData is not a StructData => extract data
					return run(instr.runInstrList.get(0));
				else {
					ArrayList<Data> list = new ArrayList<Data>();
					for(RunnableInstr ri : instr.runInstrList) {
						Data data = run(ri);
						if(data == null)
							throw new ScriptException("Unexpected value in the struct");
						list.add(data);
					}
					return new StructData(list, true);
				}
			default:
				return null;
		}
	}* /

	public static void checkStructure(ArrayList<RunnableInstr> riList) throws ScriptException {
		checkStructure(riList, 0, FILE);
	}
	
	static final int FILE = 0, IF = 1, ELSIF = 2, ELSE = 3, WHILE = 4, FUNC = 5;
	//static String str[] = {""+FILE, /*Dictionnary get(if), * / }; // -> str[WHILE] = "while"
	private static int checkStructure(ArrayList<RunnableInstr> riList, int line, int from) throws ScriptException {
		int size = riList.size();
		for(int i = line; i < size;) {
			RunnableInstr ri = riList.get(i);
			if(ri.type == RunnableInstr.t_instr) {
				
				if	   (ri.instrDef.name.compareTo("if") == 0) {
					i = checkStructure(riList, i+1, IF);
				}
				else if(ri.instrDef.name.compareTo("elsif") == 0) {
					if(from == IF || from == ELSIF)
						return checkStructure(riList, i+1, ELSIF);
					else
						throw new ScriptException("Unexpected 'elsif'", i);
				}
				else if(ri.instrDef.name.compareTo("else") == 0) {
					if(from == IF || from == ELSIF)
						return checkStructure(riList, i+1, ELSE);
					else
						throw new ScriptException("Unexpected 'else'", i);
				}
				else if(ri.instrDef.name.compareTo("endif") == 0) {
					if(from == IF || from == ELSIF || from == ELSE)
						return i+1;
					else
						throw new ScriptException("Unexpected 'endif'", i);
				}
				else if(ri.instrDef.name.compareTo("while") == 0) {
					i = checkStructure(riList, i+1, WHILE);
				}
				else if(ri.instrDef.name.compareTo("endwhile") == 0) {
					if(from == WHILE)
						return i+1;
					else
						throw new ScriptException("Unexpected 'endwhile'", i);
				}
				else if(ri.instrDef.name.compareTo("func") == 0) {
					// cannot declare a function in a coditionnal structure or a loop,
					// but it would be fun (change the function's reference dynamicaly !!!)
					if(from != FILE)
						i = checkStructure(riList, i+1, FUNC);
					else
						throw new ScriptException("Unexpected 'func'", i);
				}
				else if(ri.instrDef.name.compareTo("endfunc") == 0) {
					if(from == FUNC)
						return i+1;
					else
						throw new ScriptException("Unexpected 'endfunc'", i);
				}
				else {
					i++;
				}
			}
			else {
				i++;
			}
		}
		
		return size; // could occur only if 'from' is set to FILE
	}
	
	/** Contain a ReccInstructions formated to a stack ** /
	class StackInstr {
		/** Stack of instructions;
		 * will be filled after the initialization of the StackInstr object
		 * will only pop the instruction (after each instruction's execution)
		 * /
		private LinkedList<RunnableInstr> instr = new LinkedList<RunnableInstr>();
		/** Stack of returned data ;
		 * each instruction will push its result into this,
		 * the instruction's params will be pop from this
		 * by starting by the left param and next the right one 
		 * /
		Stack<Data> data = new Stack<Data>();
		
		public StackInstr(RunnableInstr ri) throws ScriptException {
			pushInstructions(ri);
		}
		
		private void pushInstructions(RunnableInstr ri) throws ScriptException {
			switch(ri.type) {
			// STATIC DATA & VARIABLE
			case RunnableInstr.t_data:
			case RunnableInstr.t_var:
				instr.add(ri);
				break;
				
			// INSTRUCTION
			case RunnableInstr.t_instr:
				final InstructionDefinition idef = ri.instrDef;
				if(idef.before != DT.none)
					pushInstructions(ri.left);
				if(idef.after != DT.none)
					pushInstructions(ri.right);
				
				instr.add(ri);
				break;
			
			// LIST
			case RunnableInstr.t_riList:
				if(ri.runInstrList.size() == 0)
					throw new ScriptException("Valid data not found between the paranthesis");
				else if(ri.runInstrList.size() == 1) // A simple StructData is not a StructData => extract data
					pushInstructions(ri.runInstrList.get(0));
				else {
					for(RunnableInstr subri : ri.runInstrList) {
						pushInstructions(subri);
					}
					instr.add(ri);
				}
				break;
			default:
				break;
		}
		}
		
		public RunnableInstr getNextInstruction() {
			return instr.poll();
		}
		
		public Data popData() {
			return data.pop();
		}
		public void pushData(Data d) {
			data.push(d);
		}
	}
}
*/