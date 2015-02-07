package com.awprog.scroidv2;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

// TODO catch errors
/**
 * Project -> name, directory, state loaded or not saved, files
 * index :
 * 	0 - main file - MAIN_FILE_NAME, cursor, content
 *  1 and more... - other files - name, cursor, content
 */
public class Project {
	public static final String MAIN_FILE_NAME = "/Main";
	public static final String fileExtension = ".txt";// must contain the character '.' at the beginning
	
	/** The name of the project **/
	private String mProjectName;
	/** The directory where the project is **/
	private final File mProjectDirectory;
	private boolean mContentHasChanged; // TODO ArrayList<boolean>
	private boolean mIsLoaded;
	/** The content of the files, at index 0 : the main file **/
	/** Data and file **/ 
	private ArrayList<PFile> mFile = new ArrayList<PFile>();
	
	/** Instantiates a new project, the main file must exist **/
	Project(String name, File dir) {
		mProjectName = name;
		mProjectDirectory = dir;
		
		// loads the list of project's files
		String[] list = mProjectDirectory.list(new FilenameFilter() {
			// accept if it is a file and if its name matches
			public boolean accept(File dir, String filename) {
				// correct : projectName.fileName.txt
				if(!filename.matches("\\Q" + mProjectName + "\\E" +    // starts with the project's name
											"\\.[^.]+" + 			   // contains .fileName without '.' in fileName
											"\\Q" + fileExtension + "\\E")) // ends with the file extension
					return false;

				return new File(dir, filename).isFile(); 
			}
		});
		
		mFile.add(new PFile(MAIN_FILE_NAME));
		for(int i = 0; i < list.length; i++) {
			String n = list[i].substring(list[i].lastIndexOf('.', list[i].length()-fileExtension.length()-1)+1, list[i].length()-fileExtension.length());
			mFile.add(new PFile(n));
		}
		
		mContentHasChanged = false;
		mIsLoaded = false;
	}
	/** Instantiates a new project from a string array given by @code toStringArray, the main file must exist **/
	Project(String[] sa) {
		mProjectDirectory = new File(sa[0]);
		mProjectName = sa[1];
		// Create the array list of the file name
		for(int i = 2; i < sa.length; i++)
			mFile.add(new PFile(sa[i]));

		mContentHasChanged = false;
		mIsLoaded = false;
	}
	
	/** Creates a project and its main file **/
	public static Project create(String projectName, File projectDirectory) {
		createFile(projectName + fileExtension, projectDirectory); // TODO : catch error
		
		return new Project(projectName, projectDirectory);
	}
	public String[] toStringArray() {
		String[] sa = new String[mFile.size()+2];
		sa[0] = mProjectDirectory.getAbsolutePath(); // the first index is the path of the project's directory
		sa[1] = mProjectName; // The second index is the name of the project
		
		// The other index are the file's name
		for(int i = 0; i < mFile.size(); i++)
			sa[i+2] = mFile.get(i).name;
		
		return sa;
	}

	/** Creates a file and adds it to the project, the name must not be already used **/
	public void addFile(String name) {
		// create the file
		createFile(mProjectName + "." + name + fileExtension, mProjectDirectory);// catch error
		
		// add it to the list
		mFile.add(new PFile(name, isLoaded() ? "" : null));
	}
	/** Deletes a file and removes it from the project **/
	public void deleteFile(int index) {
		// delete the file
		deleteFile(getFileFullName(index), mProjectDirectory);//catch error
		
		// delete it from the list
		mFile.remove(index);
	}
	/** Returns true if the file's content has been already loaded **/
	public boolean isLoaded() {
		return mIsLoaded;
	}
	/** Loads the file's content in the list **/
	public void load() {
		// Read each file
		for(int i = 0; i < mFile.size(); i++)
			mFile.get(i).content = readFile(getFileFullName(i), mProjectDirectory);
		
		mContentHasChanged = false;
		mIsLoaded = true;
	}
	/** Saves the file's content in the files **/
	public void save() {
		// Save each file
		for(int i = 0; i < mFile.size(); i++)
			writeFile(mFile.get(i).content, getFileFullName(i), mProjectDirectory);
		
		mContentHasChanged = false;
	}
	/** Removes the whole project and its files **/
	public void delete() {
		// Delete each file
		for(int i = 0; i < mFile.size(); i++)
			deleteFile(getFileFullName(i), mProjectDirectory); // TODO : catch error
		
		mFile.clear();
		
		mContentHasChanged = false;
		mIsLoaded = false;
	}
	/** Renames the project and each attached file **/
	public void rename(String newName) {
		// main file
		renameFile(mProjectName+fileExtension, newName+fileExtension, mProjectDirectory);
		// other files
		for(int i = 1; i < mFile.size(); i++)
			renameFile(getFileFullName(i), newName+'.'+mFile.get(i).name+fileExtension, mProjectDirectory); // TODO : catch error
		
		mProjectName = newName;
	}
	/** Makes a copy of each project's file and returns the new project, you should save the project first **/
	public Project clone(String targetName, File targetDirectory) {
		// main file
		cloneFile(mProjectName+fileExtension, targetName+fileExtension, mProjectDirectory, targetDirectory);
		// other files
		for(int i = 1; i < mFile.size(); i++)
			cloneFile(getFileFullName(i), targetName+'.'+mFile.get(i).name+fileExtension, mProjectDirectory, targetDirectory); // TODO : catch error
		
		return new Project(targetName, targetDirectory);
	}
	
	/** Returns the name of this project **/
	public String getProjectName() {
		return mProjectName;
	}
	/** 0 : the main file ; 1,2,3... the other files **/ 
	public int getFileCursorPosition(int file) {
		return mFile.get(file).cursorPosition;
	}
	/** Sets the cursor's position of the file
	 * 0 : the main file ; 1,2,3... the other files **/
	public void setFileCursorPosition(int file, int pos) {
		mFile.get(file).cursorPosition = pos;
	}
	/** 0 : the main file ; 1,2,3... the other files **/ 
	public String getFileContent(int file) {
		return mFile.get(file).content;
	}
	/** Sets the content of the file, or null for the main file.
	 * It does not write the file;  0 : the main file ; 1,2,3... the other files **/
	public void setFileContent(int file, String content) {
		if(content.compareTo(mFile.get(file).content) == 0)
			return;
		
		mContentHasChanged = true;
		mFile.get(file).content = content;
	}
	/** Returns the name of the file; 0 : the main file (name=MAIN_FILE_NAME) ; 1,2,3... the other files **/ 
	public String getFileName(int file) {
		return mFile.get(file).name;
	}
	/** Returns the index of the file disigned by its name**/
	public int getFileIndex(String name) {
		/*if(name.equals(MAIN_FILE_NAME))
			return 0;
		else {*/
			for(int i = 0; i < mFile.size(); i++)
				if(name.equals(mFile.get(i).name))
					return i;
			return 0;
		//}
	}
	/** Returns the name of the file; 0 : the main file (name="") ; 1,2,3... the other files **/ 
	public String getFileFullName(int file) {
		return mProjectName + ((file == 0) ? "" : "." + mFile.get(file).name) + fileExtension;
	}
	/** Sets the name of the subfile given **/
	public void setFileName(int file, String newName) {
		if(file == 0) return;
		renameFile(getFileFullName(file), mProjectName+'.'+newName+fileExtension, mProjectDirectory); // TODO : catch error
		mFile.get(file).name = newName;
	}
	/** returns the number of files **/
	public int getNbFiles() {
		return mFile.size();
	}
	/** Returns true of the project's file has been changed but not saved **/
	public boolean contentHasChanged() {
		return mContentHasChanged;
	}
	/** Reads a file **/
	private static String readFile(String name, File dir) {
		File fl = openFile(name, dir);
		byte[] b = null;
		
		try {
			InputStream input = new BufferedInputStream(new FileInputStream(fl));
			b = new byte[input.available()];
			input.read(b);
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
			// TODO Toast.makeText(this, "Load error !", Toast.LENGTH_SHORT).show();
		}
		
		if(b != null)
			return new String(b);
		else
			return null;
	}
	/** Writes a String in a file **/
	private static void writeFile(String content, String name, File dir) {
		File fl = openFile(name, dir);
		byte[] b = content.getBytes();
		
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(fl));
			dos.write(b);
			dos.close();
			// TODO Toast.makeText(this, "Saved in external storage as \"Scroid/"+mScriptName+".txt\"", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			// TODO Toast.makeText(this, "Save error !", Toast.LENGTH_SHORT).show();
		}
	}
	/** returns the error message or null **/
	private static String deleteFile(String name, File dir) {
		File f = openFile(name, dir);
		if(!f.delete())
			return "Cannot delete the file \"Scroid/"+name+"\"";
		else
			return null; // "\"Scroid/"+name+"\" have been deleted";
	}
	/** returns the error message or null **/
	private static String renameFile(String name, String newName, File dir) {
		File f = openFile(name, dir);
		if(!f.renameTo(openFile(newName, dir)))
			return "Cannot rename the file \""+ name +"\"";
		else
			return null; // "\"Scroid/"+name+"\" have been renamed to \""+newName+"\"";
	}
	/** returns the error message or null **/
	private static String cloneFile(String name, String newName, File dir, File newDir) {
		File f = openFile(name, dir);
		if(!f.exists()) {
			return "The file \""+name+"\" is not existing";
		}
		
		try {
			InputStream input = new FileInputStream(f);
			OutputStream output = new FileOutputStream(openFile(newName, newDir));
			byte[] buffer = new byte[1024]; int bytesRead = 0;
			while((bytesRead = input.read(buffer)) > 0)
			    output.write(buffer, 0, bytesRead);
			//Toast.makeText(this, "\""+name+"\" have been cloned to \""+newName+"\"", Toast.LENGTH_SHORT).show();
			input.close(); output.close();
		} catch (Exception e) {
			e.printStackTrace();
			return "IO File exception";
		}
		
		return null;
	}
	/** returns the error message or null **/
	private static String createFile(String name, File dir) {
		File f = openFile(name, dir);
		if (!f.exists())
			try {
				f.createNewFile();
				return "A new file have been created in external storage";
			} catch (IOException e) {
				e.printStackTrace();
				return "Cannot create the file \"Scroid/"+name+"\"";
			}
		else
			return "This file is already existing";
	}
	private static File openFile(String name, File dir) {
		return new File(dir, name);
	}
	
	
	class PFile {
		String name;
		String content;
		int cursorPosition;
		boolean hasChanged;
		PFile(String name) {
			this.name = name;
			content = null;
			hasChanged = false;
			cursorPosition = 0;
		}
		PFile(String name, String content) {
			this.name = name;
			this.content = content;
			hasChanged = false;
			cursorPosition = 0;
		}
	}


	
}
