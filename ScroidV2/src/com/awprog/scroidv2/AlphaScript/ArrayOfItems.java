package com.awprog.scroidv2.AlphaScript;

import java.util.ArrayList;
import java.util.Stack;

import com.awprog.scroidv2.AlphaScript.ErrorDialog.ScriptException;


/**
 * keyword/word : a~z (A~Z) 0~9
 * paranthesis : ()
 * number : - 0~9 .
 * string : "
 * comments : ' ou !
 * 
 * class main : Array of 'Line'
 * class Line : RecursiveArray of 'Item' => format with known keyword into 'Instruction'
 * class Item : {String, number, keyword, array Of 'Item' (tab)}
 * class Inst : [item][keyword][Item] <=> [item]
 */

class ArrayOfItems {
	private ArrayList<Item> list = new ArrayList<Item>();
	public boolean isInSubList = false;
	
	boolean isEmpty() { return list.isEmpty(); }
	
	private ArrayOfItems(ArrayList<Item> l) {
		list = l;
	}
	public Item get(int i) {
		return list.get(i);
	}
	public int size() {
		return list.size();
	}
	
	/** Construit une liste d'items à partir d'une ligne de script brut */
	static public ArrayOfItems create(String line) throws ScriptException {
		int cur = 0, len = line.length();
		//ArrayOfItems array = new ArrayOfItems();
		Stack<ArrayList<Item>> stackList = new Stack<ArrayList<Item>>();
		stackList.add(new ArrayList<Item>());
		
		String spaceChar = " \t\u00A0";
		String commentChar = "\'!";
		char stringDelimiterChar = '\"';
		String endWordChar = spaceChar+commentChar+stringDelimiterChar+".-(),";
		
		while(cur < len) {
			char c = Character.toLowerCase(line.charAt(cur));
			/// Ignored character \\\
			if(spaceChar.indexOf(c) != -1) {
				cur++;
			}
			/// Comments \\\
			else if(commentChar.indexOf(c) != -1) {
				break;
			}
			/// String \\\
			else if(c == stringDelimiterChar) {
				int indexEndStr = line.indexOf(stringDelimiterChar, cur+1);
				
				if(indexEndStr == -1)
					throw new ScriptException("Missing end of string");
				
				stackList.peek().add(new Item(Item.Type.STRING, line.substring(cur, indexEndStr+1)));
				cur = indexEndStr+1;
			}
			/// Number \\\
			else if(Character.isDigit(c) || c == '.' || c == '-') {
				boolean neg = false, dot = false, nbStarted = false;
				int indexEndNumber = cur;
				
				while(indexEndNumber < len) {
					c = line.charAt(indexEndNumber);
					if(Character.isDigit(c)) {
						if(!nbStarted)
							nbStarted = true;
					} else if(c == '.') {
						if(dot)
							throw new ScriptException("Number syntax error");
						dot = true;
					} else if(c == '-') {
						if(neg||dot||nbStarted)
							throw new ScriptException("Number syntax error");
						neg = true;
					} else {
						break;
					}
					indexEndNumber++;
				}
				stackList.peek().add(new Item(Item.Type.NUMBER, line.substring(cur, indexEndNumber)));
				cur = indexEndNumber;
			}
			/// Paranthesis \\\
			else if(c == '(') {
				// nouvelle sub list
				stackList.peek().add(new Item(Item.Type.SUB_LIST, "()"));
				// changement de l'array d'item cible
				stackList.add(new ArrayList<Item>());
				cur++;
			}
			else if(c == ')') {
				// parenthèse fermante inattendue
				if(stackList.size() == 1)
					throw new ScriptException("Unexpected end of parenthesis");
				
				ArrayOfItems sl = new ArrayOfItems(stackList.pop());
				// dernier élément ou structure vide
				if(sl.isEmpty())
					throw new ScriptException("Nothing before the end of the parenthesis");
				
				sl.isInSubList = true;
				
				// completion de la liste mère d'ArrayOfItems
				stackList.peek().get(stackList.peek().size()-1).sublist.add(sl);
				
				cur++;
			}
			/// Comma \\\
			else if(c == ',') {
				// virgule inattendue
				if(stackList.size() == 1)
					throw new ScriptException("Unexpected comma");
				
				ArrayOfItems sl = new ArrayOfItems(stackList.pop());
				// élément de la structure vide
				if(sl.isEmpty())
					throw new ScriptException("Nothing before the comma");
				
				sl.isInSubList = true;
				
				// completion de la liste mère d'ArrayOfItems
				stackList.peek().get(stackList.peek().size()-1).sublist.add(sl);

				// nouvelle array cible
				stackList.add(new ArrayList<Item>());
				
				cur++;
			}
			/// Word/other \\\
			else {
				int indexEnd = len;
				for(int i = endWordChar.length()-1; i >= 0; --i) {
					int index = line.indexOf(endWordChar.charAt(i), cur);
					if(index != -1 && index < indexEnd)
						indexEnd = index;
				}
				
				stackList.peek().add(new Item(Item.Type.WORD, line.substring(cur, indexEnd).toLowerCase()));
				cur = indexEnd;
			}
		}
		
		if(stackList.size() != 1)
			throw new ScriptException("Missing end of parenthesis");
		
		return new ArrayOfItems(stackList.peek());
	}
	
	static class Item {
		enum Type {WORD, NUMBER, STRING, SUB_LIST};
		
		Type type;
		String data = null;
		ArrayList<ArrayOfItems> sublist = null;
		int targetLine = -1;
		
		Item(Type t, String d) {
			data = d;
			type = t;
			if(type == Type.SUB_LIST)
				sublist = new ArrayList<ArrayOfItems>();
		}
		String getString() {
			if(type != Type.STRING)
				return null;
			return data.substring(1, data.length()-2);
		}
		String getWord() {
			if(type != Type.WORD)
				return null;
			return data;
		}
		double getNumber() {
			if(type != Type.NUMBER)
				return 0.0;
			try {
				return Double.parseDouble(data);
			} catch(NumberFormatException e) {
				return 0.0;
			}
		}
	}
}
/**
public class ArrayOfItems {
	/** Returns array containing line array containing item array ** /
	static public ArrayList<ArrayList<Item>> get(String data) throws ScriptException {
		ArrayList<ArrayList<Item>> lines = splitLine(getArrayOfLines(data));
		
		for(int i = lines.size()-1; i >= 0; --i) {
			ArrayList<ArrayList<Item>> al = makeSubList(lines.get(i));
			
			if(al.size() > 1)
				throw new ScriptException("Unexpected \',\'", i+1);
			else if(!lines.get(i).isEmpty())
				throw new ScriptException("Paranthesis error", i+1);
			else
				lines.set(i, al.get(0));
		}
		
		return lines;
	}
	
	/** Separates line ** /
	static private String[] getArrayOfLines(String s) {
		return s.split("\n");
	}
	/** Splits all the lines ** /
	static private ArrayList<ArrayList<Item>> splitLine(String line[]) throws ScriptException {
		int nbLine = line.length;
		ArrayList<ArrayList<Item>> splittedLines = new ArrayList<ArrayList<Item>>();
		
		int i = 0;
		try {
			for(i = 0; i < nbLine; i++)
				splittedLines.add(splitString(line[i]));
		} catch (ScriptException e) {
			e.setLine(i+1);
			throw e;
		}
		
		return splittedLines;
	}
	/** Splits line contents in an array of items ** /
	static private ArrayList<Item> splitString(String s) throws ScriptException {
		int cur = 0, len = s.length();
		ArrayList<Item> array = new ArrayList<Item>();
		
		while(cur < len) {
			char c = Character.toLowerCase(s.charAt(cur));
			/// Ignored character \\\
			if((new String(" \t")).indexOf(c) != -1) {
				cur++;
			}
			/// Comments \\\
			else if(c == '\'' || c == '!') {
				break;
			}
			/// String \\\
			else if(c == '\"') {
				int indexEndStr = s.indexOf('\"', cur+1);
				
				if(indexEndStr == -1)
					throw new ScriptException("Missing double quote");
				
				array.add(new Item(s.substring(cur, indexEndStr+1), Item.STR));
				cur = indexEndStr+1;
			}
			/// Number \\\
			else if(Character.isDigit(c) || c == '.' || c == '-') {
				boolean neg = false, dot = false, nbStarted = false;
				int indexEndNumber = cur;
				
				while(indexEndNumber < len) {
					c = s.charAt(indexEndNumber);
					if(Character.isDigit(c)) {
						if(!nbStarted)
							nbStarted = true;
					} else if(c == '.') {
						if(dot)
							throw new ScriptException("Number syntax error");
						dot = true;
					} else if(c == '-') {
						if(neg||dot||nbStarted)
							throw new ScriptException("Number syntax error");
						neg = true;
					} else {
						break;
					}
					indexEndNumber++;
				}
				array.add(new Item(s.substring(cur, indexEndNumber), Item.NUMB));
				cur = indexEndNumber;
			}
			/// Paranthesis \\\
			else if(c == '(') {
				array.add(new Item("(", Item.PAROP));
				cur++;
			}
			else if(c == ')') {
				array.add(new Item(")", Item.PARCL));
				cur++;
			}
			/// Comma \\\
			else if(c == ',') {
				array.add(new Item(",", Item.COMMA));
				cur++;
			}
			/// Math Operators \\\ // TODO : fuck code pour chagement de mot clé
			/*else if(c == '*') {
				array.add(new Item("mul", Item.WORD));
				cur++;
			}
			else if(c == '/') {
				array.add(new Item("div", Item.WORD));
				cur++;
			}
			else if(c == '+') {
				array.add(new Item("add", Item.WORD));
				cur++;
			}
			else if(c == '-') { // TODO conflit avec signe de négation
				array.add(new Item("sub", Item.WORD));
				cur++;
			}
			else if(c == '^') {
				array.add(new Item("exp", Item.WORD));
				cur++;
			}
			else if(c == '%') {
				array.add(new Item("mod", Item.WORD));
				cur++;
			}* /
			/// Word/other \\\
			else { // if(Character.isLetter(c)) {
				int indexEnd = len;
				for(int i = Dictionnary.carEnd.length()-1; i >= 0; --i) {
					int index = s.indexOf(Dictionnary.carEnd.charAt(i), cur);
					if(index != -1 && index < indexEnd)
						indexEnd = index;
				}
				
				array.add(new Item(s.substring(cur, indexEnd).toLowerCase(), Item.WORD));
				cur = indexEnd;
			}
		}
		
		return array;
	}
	
	/** Makes sublist with paranthesis and internal commas, if the process works normally, the input line is empty ** /
	static private ArrayList<ArrayList<Item>> makeSubList(ArrayList<Item> line) throws ScriptException {
		ArrayList<ArrayList<Item>> sub = new ArrayList<ArrayList<Item>>();
		ArrayList<Item> list = new ArrayList<Item>();
		
		while(!line.isEmpty()) {
			Item i = line.get(0);
			line.remove(0);
			
			if(list == null)
				list = new ArrayList<Item>();
			
			if(i.type == Item.PAROP) {
				i.type = Item.SUBLIST;
				i.data = null;
				i.subList = makeSubList(line);
				list.add(i);
			}
			else if(i.type == Item.PARCL) {
				if(list.isEmpty())
					throw new ScriptException("Missing data before \')\'");
				
				sub.add(list);
				return sub;
			}
			else if(i.type == Item.COMMA) {
				if(list.isEmpty())
					throw new ScriptException("Missing data before \',\'");
				sub.add(list);
				list = null;
			}
			else {
				list.add(i);
			}
		}

		if(list != null)
			sub.add(list);
		return sub;
	}
	*/
/*
	static public class Item {
		public final static int STR = 0, NUMB = 1, WORD = 2, PAROP = 3, PARCL = 4, COMMA = 5, SUBLIST = 6;
		
		public int type;
		public String data;
		
		public ArrayList<ArrayList<Item>> subList = null; 
		
		Item(String s, int t) {
			type = t;
			data = s;
		}
	}
}*/
