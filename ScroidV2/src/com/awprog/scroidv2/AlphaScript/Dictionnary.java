package com.awprog.scroidv2.AlphaScript;

import java.util.Arrays;
import java.util.Comparator;

import android.graphics.Color;

public class Dictionnary {
	public static final int CAT = 0, NAM = 1, BEF = 2, AFT = 3, RET = 4, COM = 5;
	public static final String funcAndParamsAndDoc[][] = {
			/*** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
			 * {"ctg", "function", "left param : [type] what", "right params : [type] what", "returned value : [type] what", "Description"}   *
			 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * ***/
			/*** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
			 *  [Number] MyNumber
			 *  [String] MyCharSeq
			 *  [Boolean] MyTrueOrFalse
			 *  [tabNumber with size of (1..~, ?)] myListOfNumbers
			 *  [tabString with size of (1..~, ?)] myListOfWords
			 *  [tabBoolean with size of (1..~, ?)] myListOfTOF
			 *  ([Type] MyThing, [Type] MyThing, [Type] MyThing...)
			 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * /
			
			/** Native (API lvl0 **/
				/* Create variable */ // deprecated
					// auto with set
					//"newnumber", "", "", "", ""},
					//"newstring", "", "", "", ""},
					//"newstruct", "", "", "", ""},
			
				/* Basical algorythmic structure */
					//{"label", "", "", "", ""},// deprecated
					//{"goto", "", "", "", ""},// deprecated
					{"bas", "if", 		"", "[Boolean] condition", 	"", "Run the following lines if the condition is true"},
					{"bas", "elsif", 	"", "[Boolean] condition", 	"", "Run the following lines if the condition is true and if the last \"if\" was false"},
					{"bas", "else", 	"", "", 					"", "Run the following lines if the last \"if\" or \"elsif\" was false"},
					{"bas", "endif", 	"", "", 					"", "End the conditional structure"},
					
				//	{"bas",	"for",		"[Number] incrementator",	"([Number] start, [Number] end)",	"", "Run the following lines and set the incremetator from start to end"},
				//	{"bas",	"endfor",	"",							"",									"", "End the loop"},
					{"bas", "while", 		"", "[Boolean] condition", 	"", "Run in loop the following lines as long as the condition is true"},
					{"bas", "endwhile", 	"", "", 					"", "End the loop"},
					//"dowhile", "", "", "", ""},
					//"enddowhile", "", "", "", ""},
				
				/* Comparators */
					{"cmp", "equ", "[All] value1", 				"[All] value2", 			"[Boolean] result of comparison",	"Equal : compare data of the same kind"},
					{"cmp", "neq", "[All] value1", 				"[All] value2", 			"[Boolean] result of comparison",	"Not equal : compare data of the same kind"},
					{"cmp", "lss", "[Number; String] value1", 	"[Number; String] value2", 	"[Boolean] result of comparison",	"Less : compare numbers or according to the alphabetical order for strings"},
					{"cmp", "leq", "[Number; String] value1", 	"[Number; String] value2", 	"[Boolean] result of comparison",	"Less or equal : compare numbers or according to the alphabetical order for strings"},
					{"cmp", "gtr", "[Number; String] value1", 	"[Number; String] value2", 	"[Boolean] result of comparison",	"Greater : compare numbers or according to the alphabetical order for strings"},
					{"cmp", "geq", "[Number; String] value1", 	"[Number; String] value2", 	"[Boolean] result of comparison",	"Greater or equal : compare numbers or according to the alphabetical order for strings"},
				
				/* Logical operators */
					{"lop", "and",	"[Boolean] value1", "[Boolean] value2", "[Boolean] result", 			"And"},
					{"lop", "or",	"[Boolean] value1", "[Boolean] value2", "[Boolean] result", 			"Or"},
					{"lop", "xor",	"[Boolean] value1", "[Boolean] value2", "[Boolean] result", 			"Exclusive or"},
					{"lop", "not",	"",					"[Boolean] value", 	"[Boolean] inverse of value",	"Not"},
	
				/* Variable assignements */
					{"vas", "set", 		"[All] variable", 			"[All] value", 			"", "Set the variable to a value of the same type, create the variable if it still does not exist"},
					{"vas", "icz", 		"", 						"[Number] variable", 	"", "Increaze the variable"},
					{"vas", "dcz", 		"", 						"[Number] variable", 	"", "Decreaze the variable"},
					{"vas", "setadd", 	"[Number; String] variable","[Number; All] value", 	"", "Add the value to the variable; In case of variable is a String, convert value2 to String and concatenate it to the variable"},
					{"vas", "setsub", 	"[Number] variable", 		"[Number] value", 		"", "Substract the value from the variable"},
					{"vas", "setmul", 	"[Number; String] variable","[Number] factor", 		"", "Multiply the variable by the factor; In case of string : join the string end-to-end 'factor' time"},
					{"vas", "setdiv", 	"[Number] variable", 		"[Number] divisor", 	"", "Divide the variable by the value"},
					{"vas", "setmod", 	"[Number] variable", 		"[Number] divisor", 	"", "Set the variable to the result of variable modulo value"},
					{"vas", "setpow", 	"[Number] variable", 		"[Number] exponent", 	"", "Set the variable to the result of variable pow exponent"},
					
				/* Basical operators */ 
					{"bop", "add", "[Number; All/String] value1",	"[Number; String/All] value2", 	"[Number] sum; concatenation",				"Sum; In case of value1 or value2 is a String, convert the other value to string if necessary and concatenate them"},
					{"bop", "sub", "[Number] operand1", 			"[Number] operand2", 	 		"[Number] difference",						"Difference"},
					{"bop", "mul", "[Number] factor", 				"[Number; String] value",		"[Number; String] product; concatenation",	"Product; In case of String : join the string end-to-end 'factor' time"},
					{"bop", "div", "[Number] dividend", 			"[Number] divisor", 			"[Number] quotient",						"Quotient"},
					{"bop", "mod", "[Number] dividend",				"[Number] divisor", 			"[Number] result",							"Modulo"},
					{"bop", "pow", "[Number] value", 				"[Number] exponent", 			"[Number] result",							"Pow"},
				
				/* Mathematic functions */
					{"mfc", "sqrt", "", "[Number] x",		"[Number] result", 	"Square root of x"},
					{"mfc", "cbrt", "", "[Number] x",		"[Number] result", 	"Cube root of x"},
					{"mfc", "log",  "", "[Number] x",		"[Number] result", 	"Natural logarithm of x"},
					{"mfc", "log10","", "[Number] x",		"[Number] result", 	"Decimal logarithm of x"},
					//"logn", "", "", "", ""},
					{"mfc", "fact", "", "[Number] n",		"[Number] result", 	"Factorial n"},
					{"mfc", "sin",	"", "[Number] rad",		"[Number] result", 	"Sinus of rad"},
					{"mfc", "cos",  "", "[Number] rad",		"[Number] result", 	"Cosinus of rad"},
					{"mfc", "tan",  "", "[Number] rad",		"[Number] result", 	"Tangent of rad"},
					{"mfc", "asin", "", "[Number] x",		"[Number] rad", 	"Arcsinus of x"},
					{"mfc", "acos", "", "[Number] x",		"[Number] rad", 	"Arccosinus of x"},
					{"mfc", "atan", "", "[Number] x",		"[Number] rad", 	"Arctangent of x"},
					{"mfc", "deg",  "", "[Number] rad",		"[Number] degree", 	"Convert rad to degree"},
					{"mfc", "rad",  "", "[Number] degree",	"[Number] rad", 	"Convert degree to rad"},
					//"sinh", "", "", "", ""},
					//"cosh", "", "", "", ""},
					//"tanh", "", "", "", ""},
					//"asinh", "", "", "", ""},
					//"acosh", "", "", "", ""},
					//"atanh", "", "", "", ""},
					{"mfc", "floor", "", "[Number] x",		"[Number] result",	"Floor : the nearest integer value above x"},
					{"mfc", "ceil",  "", "[Number] x", 		"[Number] result",	"Ceil : the nearest integer value below x"},
					{"mfc", "round", "", "[Number] x", 		"[Number] result",	"Rounded value of x"},
					{"mfc", "abs",	 "", "[Number] x",		"[Number] result",	"Absolute value of x"},
					{"mfc", "opp",	 "", "[Number] x",		"[Number] result",	"Opposite value of x"},
					//"sq", "", "", "", ""},
				//	{"mfc", "min",   "", "([Number] value1, [Number] value2)", "[Number] smallest value", "Returns the most negative of the two values"},
				//	{"mfc", "max",   "", "([Number] value1, [Number] value2)", "[Number] biggest value", "Returns the most positive of the two values"},
				
				/* Struct function */
					{"sfc", "getindex",		"[Struct] tab",			"[Number; Struct of Number] index; multiple index", 				"[All] value",		"Return a copy of the value at the index; in case of a structure containing other(s) structure(s), you can use a multiple index (index1, index2, ..) to access recursively to the values"},// read-only
					{"sfc", "setindex",		"[Struct] variable",	"([Number; Struct of Number] index; multiple index, [All] value)",	"",					"Set the case to value at the index; in case of a structure containing other(s) structure(s), you can use a multiple index (index1, index2, ..) to access recursively to the values"}, // write-only
					{"sfc", "clearstruct",	"",						"[Struct] variable",												"",					"Clear all the value of the variable"},
					{"sfc", "createstruct",	"[Struct] variable",	"([Number; Struct of Number] index; multiple index, [All] value)",	"",					"Create a new structure with the given dimension(s) and fill it with the given value"},
					{"sfc", "pushback",		"[Struct] variable",	"[All] value", 														"",					"Add the value at the end of the structure"},
					{"sfc", "popback",		"",						"[Struct] variable", 												"[All] value",		"Remove and return the last value of the structure"},
					{"sfc", "pushfront",	"[Struct] variable",	"[All] value", 														"",					"Add the value at the beginning of the structure"},
					{"sfc", "popfront",		"",						"[Struct] variable", 												"[All] value",		"Remove and return the first value of the structure"},
					{"sfc", "remove",		"[Struct] variable",	"[Number] index", 													"",					"Remove from the structure the value at the specified index"},
				//	{"sfc", "insert",		"[Struct] variable",	"([Number] index, [All] value", 									"",					"Insert the value in the structure at the specified index"},
					{"sfc", "size",			"",						"[Struct] tab", 													"[Number] size",	"Return the size of the struct"},
					
				/* Constants */
					{"cst", "rand",		"", "", "[Number] random value",		"Return a pseudo-random value in the range [0; 1["},
					{"cst", "pi", 		"", "", "[Number] pi", 					"\u03C0 = 3.14159263538979323..."},
					{"cst", "e", 		"", "", "[Number] e",					"e = "+Math.E+"..."},
					{"boo", "true",		"", "", "[Boolean] true",				"True"},
					{"boo", "false",	"", "", "[Boolean] false",				"False"},
					{"str", "endl",		"", "", "[String] new line character",	"End Line = \"\\n\""},
				
				/* standard I/O */
					{"i/o", "cleartext",	"", "",						"", "Clear the console"},
					{"i/o", "print", 		"", "[All] value",			"", "Convert the value to a String and print it in the console"},
					{"i/o", "println",		"", "[All] value",			"", "Convert the value to a String and print it with and new line in the console"},
					{"i/o", "readnb",		"", "[Number] variable",	"", "Read a number given by the user, create the variable if it still does not exist"},
				//	{"i/o", "readbool",	"", "[Boolean] variable",	"", "Read a boolean given by the user, create the variable if it still does not exist"},
					{"i/o", "scan",		"", "[String] variable",	"", "Read a texte given by the user, create the variable if it still does not exist"},
					{"std", "stop",		"", "",						"", "End the program"},
					{"std", "pause",	"", "",						"", "Wait the button is pressed down to continue"},
					
					
			/** Native API lvl1 string **/
			
				{"str", "substr",	"[String] source",		"([Number] start, [Number] end)", 			"[String] sub-string",						"Return a new String containing the characters from start to end-1"},
				{"str", "charat",	"[String] source",		"[Number] index", 							"[String] character",						"Return a new String containing the character at the given index"},
				{"str", "replace",	"[String] variable",	"([Number] index"+/* ; ([Number] start, [Number] end)*/", [String] replacement)",	"",	"Replace the characters at index and the nexts by the remplacement string"},
			//	{"str", "insert",	"[String] variable",	"([Number] index, [String] string)",		"",											"Insert the string before the character at the index"},
				{"str", "tolower",	"",						"[String] string",							"[String] lower",							"Returns the string converted to lower case"},
				{"str", "toupper",	"",						"[String] string",							"[String] upper",							"Return the string converted to upper case"},
				{"str", "length",	"",						"[String] string",							"[Number] size",							"Return the size of the string"},
				
			/** Native API lvl1 time **/
			
				{"tim", "sleep", "", "[Number] time",	"",							"Pause for 'time' seconds"},
				{"tim", "clock", "", "", 				"[Number] elapsed time",	"Return the Elapsed time since the beginning"},
				
			/** Native API lvl1 function **/
				{"fct", "func", "", "[String] function's name", "", "Start the definition of a new function"},
				{"fct", "endfunc", "", "", "", "End the definition of the new function"},
				{"fct", "return", "", "[All] something to return", "", "Set the data returned by the current function"},
				{"fct", "arg", "", "[Number] index of argument", "", "Return the argument at the given index"},
				{"fct", "argcount", "", "", "", "Return the count of argument given to the function"},
				{"fct", "call", "", "[String] function to call", "[All] function's return", "Call an user function"},
				
			/** Native API lvl2 graph ** /
				
				/* View managers * /
					{"std", "usegraph", "", "", "", ""},
					{"std", "usetxt", "", "", "", ""},
					{"img", "clrgraph", "", "", "", ""},
				
				/* Drawing functions * /
					{"img", "pxl", "", "", "", ""},
					{"img", "getpxl", "", "", "", ""},
					{"img", "rect", "", "", "", ""},
					{"img", "cir", "", "", "", ""},
					{"img", "ecl", "", "", "", ""},
					{"img", "poly", "", "", "", ""},
					{"img", "filledpoly", "", "", "", ""},
					{"img", "txt", "", "", "", ""},
					{"img", "img", "", "", "", ""},
					{"img", "line", "", "", "", ""},
				
				/* Input * /
					{"i/o", "getx", "", "", "", ""},
					{"i/o", "gety", "", "", "", ""},
			
					
			/** Native API lvl2 sensor ** /
				{"std", "useacc", "", "", "", ""}, // automatically called when accx, accy or accz is called for the time 
				{"std", "usegyr", "", "", "", ""},
				{"i/o", "accx", "", "", "", ""},
				{"i/o", "accy", "", "", "", ""},
				{"i/o", "accz", "", "", "", ""},
				{"i/o", "gyrx", "", "", "", ""},
				{"i/o", "gyry", "", "", "", ""},
				{"i/o", "gyrz", "", "", "", ""}
			/***/
	};
	
	private static String keyword[];
	
	public static String[] getKeywordList() {
		if(keyword == null) {
			keyword = new String[funcAndParamsAndDoc.length];
			for(int i = keyword.length-1; i >= 0; --i)
				keyword[i] = funcAndParamsAndDoc[i][1];
			
			Arrays.sort(keyword, new Comparator<String>() {
				public int compare(String lhs, String rhs) {
					return lhs.compareTo(rhs);
				}
			});
		}
		return keyword;
	}
	
	private static boolean funcAndParamsAndDocIsSorted = false;
	private static final Comparator<String[]> cmp = new Comparator<String[]>() {
		public int compare(String[] lhs, String[] rhs) {
			return lhs[1].compareTo(rhs[1]);
		}
	};
	
	public static int getKeywordColor(String k) {
		if(!funcAndParamsAndDocIsSorted) {
			Arrays.sort(funcAndParamsAndDoc, cmp);
		}
		int search = Arrays.binarySearch(funcAndParamsAndDoc, new String[]{"", k,"","","",""}, cmp);
		if(search < 0)
			return Color.DKGRAY;
		/**
		 * String : rouge -> orange
		 * struct : marron
		 * boolean : bleu foncé -> cyan
		 * standard : violet foncé -> rose
		 * number : gris
		 * math : vert
		 * 
		 */
		String s = funcAndParamsAndDoc[search][0];
		if(s.compareTo("fct") == 0)
			return 0xFF0000DD; // bleu très foncé
		else if(s.compareTo("bas") == 0)
			return 0xFF0000FF; // bleu foncé
		else if(s.compareTo("lop") == 0)
			return 0xFF0028FF; // bleu
		else if(s.compareTo("cmp") == 0)
			return 0xFF0056FF; // bleu clair
		else if(s.compareTo("boo") == 0)
			return 0xFF0084FF; // cyan foncé
		
		else if(s.compareTo("i/o") == 0)
			return 0xFF800080; // violet foncé
		else if(s.compareTo("std") == 0)
			return 0xFFA000A0; // violet
		else if(s.compareTo("vas") == 0)
			return 0xFFBB00BB; // violet clair

		else if(s.compareTo("cst") == 0)
			return 0xFF004000; // vert foncé
		else if(s.compareTo("mfc") == 0)
			return 0xFF177017; // vert
		else if(s.compareTo("bop") == 0)
			return 0xFF33AA33; // vert clair
		
		else if(s.compareTo("sfc") == 0)
			return 0xFF702800; // marron
		
		else if(s.compareTo("str") == 0)
			return 0xFFFF8800; // orange
		else if(s.compareTo("tim") == 0)
			return 0xFF800080; // violet
		else if(s.compareTo("img") == 0)
			return 0xFF888888; // gris
		else
			return Color.DKGRAY;
	}
	
	public static final String carEnd = " ()\"-.,\n\t";
}
