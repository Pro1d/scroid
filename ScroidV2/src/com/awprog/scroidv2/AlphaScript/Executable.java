package com.awprog.scroidv2.AlphaScript;

import java.util.ArrayList;
import java.util.HashMap;

/** Contient les données pseudo compilées de chaque fichier d'un projet,
 *  */
public class Executable {
	String mProjectName;
	HashMap<String, File> mData = new HashMap<String, File>();
	
	/*void createFile(String name) {
		mData.put(name, new File());
	}*/
	
	static class File {
		private ArrayList<StackInstructions> linesOfInstructions = new ArrayList<StackInstructions>();
		StackInstructions getLine(int line) {
			return linesOfInstructions.get(line);
		}
		void addLine(StackInstructions line) {
			linesOfInstructions.add(line);
		}
	}
}
