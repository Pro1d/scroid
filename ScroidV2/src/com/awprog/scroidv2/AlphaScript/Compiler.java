package com.awprog.scroidv2.AlphaScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import com.awprog.scroidv2.Project;
import com.awprog.scroidv2.AlphaScript.Data.DT;
import com.awprog.scroidv2.AlphaScript.ErrorDialog.ScriptException;

public class Compiler {
	private Project mProject;
	private boolean isBuilt = false;
	private HashMap<String, ArrayList<ArrayOfItems>> preCompiledFiles = new HashMap<String, ArrayList<ArrayOfItems>>();
	private HashMap<String, ArrayList<StackInstructions>> compiledFiles = new HashMap<String, ArrayList<StackInstructions>>();
	
	public void setProjectSource(Project p) {
		mProject = p;
	}
	public void setKeywordsList() {
		// TODO
	}
	public void clean() {
		isBuilt = false;
		preCompiledFiles.clear();
		compiledFiles.clear();
	}
	public void build() throws ScriptException {
		clean();
		
		if(!mProject.isLoaded())
			mProject.load();
		for(int i = mProject.getNbFiles(); --i >= 0;) {
			String name = mProject.getFileName(i);
			/// Script brut -> array of items
			try {
				preCompiledFiles.put(name, getArraysOfItemsFile(mProject.getFileContent(i)));
			} catch (ScriptException e) {
				e.setFile(name);
				e.setCat("File Parser");
				throw e;
			}

			/// V�rification des structures
			/// Table des fonctions
			try {
				checkAndSetUpStructure(preCompiledFiles.get(name), i == 0, name);
			} catch (ScriptException e) {
				e.setFile(name);
				e.setCat("Structure and function checking");
				throw e;
			}
		}
		
		/// ArrayOfItems -> StackInstructions
		for(int i = mProject.getNbFiles(); --i >= 0;) {
			String name = mProject.getFileName(i);
			
			try {
				compiledFiles.put(name, getStackInstructionsFile(preCompiledFiles.get(i)));
			} catch (ScriptException e) {
				e.setFile(name);
				e.setCat("Instruction stack building");
				throw e;
			}
		}
		
		// ArrayOfItems.create(line)
		isBuilt = true;
	}
	/** Retourne l'array de stack d'instruction de chaque ligne du ficher 
	 * @throws ScriptException */
	private ArrayList<StackInstructions> getStackInstructionsFile(ArrayList<ArrayOfItems> file) throws ScriptException {
		ArrayList<StackInstructions> lines = new ArrayList<StackInstructions>();
		int line = 1;
		for(ArrayOfItems array : file) {
			try {
				lines.add(StackInstructions.create(array));
			} catch (ScriptException e) {
				e.setLine(line);
				throw e;
			}
			line++;
		}
		lines.trimToSize();
		return lines;
	}
	/** Retourne l'array of items de chaque ligne du fichier  
	 * @throws ScriptException */
	private ArrayList<ArrayOfItems> getArraysOfItemsFile(String fullFile) throws ScriptException {
		ArrayList<ArrayOfItems> lines = new ArrayList<ArrayOfItems>();
		
		String strLines[] = fullFile.split("\n");
		int lineNumber = 1;
		
		for(String line : strLines) {
			try {
				lines.add(ArrayOfItems.create(line));
			} catch (ScriptException e) {
				e.setLine(lineNumber);
				throw e;
			}
			lineNumber++;
		}
		
		lines.trimToSize();
		return lines;
	}
	
	/**
 	 * v�rifie la coh�rance des structures conditionnelles, boucles et fonction
	 * et calcule les sauts relatifs pour le compteur ordinal (deltaPointerCounter)
	 * ajoute les fonctions d�clar�es dans la table des fonctions
	 * @throws ScriptException */
	private enum ScopeType { IF, ELSIF, ELSE, WHILE, FUNC, MAIN }
	private static class ScopeObj {
		ScopeType type;
		int line;
		/// Pour les d�clarations de fonctions ; { return pr�sent(s), struct if compl�ement return, 
		boolean hasSomeReturn = false, fullReturning = false, returnInScope = false, left = false, right = false;
		String name = null;
		public ScopeObj(ScopeType t, int l){type=t;line=l;}}
	private static final String IF = "if", ELSIF = "elsif", ELSE = "else", ENDIF = "endif", 
								WHILE = "while", ENDWHILE = "while", // BREAK, CONTINUE
								FUNC = "func", FUNCR = "funcr", FUNCL = "funcl", FUNCLR = "funclr",
								ENDFUNC = "endfunc", RETURN = "return";
	private void checkAndSetUpStructure(ArrayList<ArrayOfItems> file, boolean isMainFile, String fileName) throws ScriptException {
		Stack<ScopeObj> scopeStack = new Stack<ScopeObj>();
		
		scopeStack.push(new ScopeObj(ScopeType.MAIN, 0));
		
		int line = 1;
		for(ArrayOfItems array : file) {
			
			if(array.isEmpty()) continue;
			
			ArrayOfItems.Item first = array.get(0);
			if(first.type != ArrayOfItems.Item.Type.WORD) continue;
			
			if(first.getWord().equals(FUNC)) {
				 if(scopeStack.size() > 1)
					 throw new ScriptException("Cannot declare a function here", line);
				 if(isMainFile)
					 throw new ScriptException("Cannot declare a function in the main file", line);

				 if(array.size() != 2 || array.get(1).type != ArrayOfItems.Item.Type.WORD)
					 throw new ScriptException("The keyword \""+FUNC+"\" must be followed by the function's name (and nothing more)", line);

				 scopeStack.push(new ScopeObj(ScopeType.FUNC, line-1));
				 scopeStack.peek().name = array.get(1).getWord();
			}
			else if(first.getWord().equals(FUNCL)) {
				 if(scopeStack.size() > 1)
					 throw new ScriptException("Cannot declare a function here", line);
				 if(isMainFile)
					 throw new ScriptException("Cannot declare a function in the main file", line);

				 if(array.size() != 2 || array.get(1).type != ArrayOfItems.Item.Type.WORD)
					 throw new ScriptException("The keyword \""+FUNCL+"\" must be followed by the function's name (and nothing more)", line);

				 scopeStack.push(new ScopeObj(ScopeType.FUNC, line-1));
				 scopeStack.peek().name = array.get(1).getWord();
				 scopeStack.peek().left = true;
				 
			}
			else if(first.getWord().equals(FUNCR)) {
				 if(scopeStack.size() > 1)
					 throw new ScriptException("Cannot declare a function here", line);
				 if(isMainFile)
					 throw new ScriptException("Cannot declare a function in the main file", line);

				 if(array.size() != 2 || array.get(1).type != ArrayOfItems.Item.Type.WORD)
					 throw new ScriptException("The keyword \""+FUNCR+"\" must be followed by the function's name (and nothing more)", line);

				 scopeStack.push(new ScopeObj(ScopeType.FUNC, line-1));
				 scopeStack.peek().name = array.get(1).getWord();
				 scopeStack.peek().right = true;
			}
			else if(first.getWord().equals(FUNCLR)) {
				 if(scopeStack.size() > 1)
					 throw new ScriptException("Cannot declare a function here", line);
				 if(isMainFile)
					 throw new ScriptException("Cannot declare a function in the main file", line);

				 if(array.size() != 2 || array.get(1).type != ArrayOfItems.Item.Type.WORD)
					 throw new ScriptException("The keyword \""+FUNC+"\" must be followed by the function's name (and nothing more)", line);

				 scopeStack.push(new ScopeObj(ScopeType.FUNC, line-1));
				 scopeStack.peek().name = array.get(1).getWord();
				 scopeStack.peek().left = true;
				 scopeStack.peek().right = true;
			}
			else if(first.getWord().equals(ENDFUNC)) {
				 if(scopeStack.peek().type != ScopeType.FUNC)
					 throw new ScriptException("Unexpected end of function", line);

				 ScopeObj so = scopeStack.pop();
				 
				 if(!so.returnInScope && so.hasSomeReturn) 
				 	throw new ScriptException("A function must return something in all cases or never", so.line);//	   must have return either in all case or never [ERROR /!\]
				 else
					 addInlineFunction(so.name, so.left, so.right, so.returnInScope, fileName, so.line);
				 	/*TODO Add function table (name, left, right, returnInScope, so.line, fileName)*/;
					 
			}
			else if(!isMainFile && scopeStack.size() == 1) { 
				 if(scopeStack.size() > 1)
					 throw new ScriptException("Secondary files cannot have instructions out of a function", line);
				// -> ligne d'instruction hors d'une fonction pour un fichier secondaire
				
			}
			else if(first.getWord().equals(RETURN)) {
				if(isMainFile)
					throw new ScriptException("Cannot return anything in the main file", line);
				scopeStack.peek().hasSomeReturn = true;
				scopeStack.peek().returnInScope = true;
				switch(scopeStack.peek().type) {
				case IF: case FUNC:
					scopeStack.peek().fullReturning = true; // temporary true for 'if'
					break;
				default:
					break;
				}
			}
			else if(first.getWord().equals(IF)) {
				 scopeStack.push(new ScopeObj(ScopeType.IF, line-1));
			}
			else if(first.getWord().equals(ELSIF)) {
				 if(scopeStack.peek().type != ScopeType.IF || scopeStack.peek().type != ScopeType.ELSIF)
					 throw new ScriptException("Unexpected \""+ELSIF+"\"", line);
				 
				 ScopeObj so = scopeStack.pop();
				 // Set the target line of the last 'if' or 'elsif' in case of 'false' condition 
				 file.get(so.line).get(0).targetLine = line-1;
				 
				 scopeStack.push(new ScopeObj(ScopeType.ELSIF, line-1));
				 // on trasmet le fullReturning s'il est toujours vrai
				 if(so.returnInScope && so.fullReturning)
					 scopeStack.peek().fullReturning = true;
				 // on fait h�riter le hasSomeReturn s'il existe 
				 if(so.hasSomeReturn)
					 scopeStack.peek().hasSomeReturn = true;
			}
			else if(first.getWord().equals(ELSE)) {
				 if(scopeStack.peek().type != ScopeType.IF || scopeStack.peek().type != ScopeType.ELSIF)
					 throw new ScriptException("Unexpected \""+ELSE+"\"", line);

				 ScopeObj so = scopeStack.pop();
				 // Set the target line of the last 'if' or 'elsif' in case of 'false' condition 
				 file.get(so.line).get(0).targetLine = line-1;
				 
				 scopeStack.push(new ScopeObj(ScopeType.ELSE, line-1));
				 // on trasmet le fullReturning s'il est toujours vrai
				 if(so.returnInScope && so.fullReturning)
					 scopeStack.peek().fullReturning = true;
				 // on fait h�riter le hasSomeReturn s'il est vrai 
				 if(so.hasSomeReturn)
					 scopeStack.peek().hasSomeReturn = true;
			}
			else if(first.getWord().equals(ENDIF)) {
				 if(scopeStack.peek().type != ScopeType.IF || scopeStack.peek().type != ScopeType.ELSIF || scopeStack.peek().type != ScopeType.ELSE)
					 throw new ScriptException("Unexpected \""+ENDIF+"\"", line);
				 
				 ScopeObj so = scopeStack.pop();
				 // Set the target line of the last 'if' or 'elsif' in case of 'false' condition 
				 if(so.type != ScopeType.ELSE)
					 file.get(so.line).get(0).targetLine = line-1;
				 
				 // on commmunique le fullReturning au bloc parent s'il est toujours vrai
				 if(so.returnInScope && so.fullReturning)
					 scopeStack.peek().returnInScope = true;
				 // on fait h�riter le hasSomeReturn s'il est vrai
				 if(so.hasSomeReturn)
					 scopeStack.peek().hasSomeReturn = true;
			}
			else if(first.getWord().equals(WHILE)) {
				 scopeStack.push(new ScopeObj(ScopeType.WHILE, line-1));
			}
			else if(first.getWord().equals(ENDWHILE)) {
				 if(scopeStack.peek().type != ScopeType.WHILE)
					 throw new ScriptException("Unexpected \""+ENDWHILE+"\"", line);
				 
				 ScopeObj so = scopeStack.pop();
				 // Set the target line of the corresponding 'while' in case of 'false' condition 
				 file.get(so.line).get(0).targetLine = (line-1) + 1;
				 // Set the target line to the corresponding 'while'
				 first.targetLine = so.line-1;
				 
				 // on fait h�riter le hasSomeReturn s'il est vrai
				 if(so.hasSomeReturn)
					 scopeStack.peek().hasSomeReturn = true;
			}
				
			line++;
		}
		if(scopeStack.size() > 1)
			throw new ScriptException("Uncomplete structure (missing end of "+scopeStack.peek().type.toString()+")", line-1);
		
	}
	 
	/**
	 * @throws ScriptException
	 */
	private void addInlineFunction(String name, boolean left, boolean right, boolean _return, String file, int line) throws ScriptException {
		Instructions.addInlineFunction(name, left?DT.all:DT.none, right?DT.all:DT.none, _return?DT.all:DT.none, file, line);
	}
	
	public boolean isBuiltSuccessfully() {
		return isBuilt;
	}
	public HashMap<String, ArrayList<StackInstructions>> getExecutable() {
		return compiledFiles;
	}
}
