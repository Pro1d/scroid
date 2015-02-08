package com.awprog.scroidv2;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

public class Main extends Activity {
	ListView list;
	ProjectManager projManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);

		//fichier.add("Create a new file");
		projManager = new ProjectManager();

		list = (ListView) this.findViewById(R.id.lv_main);
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
				if(index > 0) {
					Intent intent = new Intent(Main.this, EditorActivity.class);
					intent.putExtra("project", projManager.getProject(index-1).toStringArray());
					intent.putExtra("scriptName", projManager.getProject(index-1).getProjectName()); // TODO remove
					Main.this.startActivity(intent);
				}
				else {
					// Affichage dialogue de demande de nom de fichier
					AlertDialog.Builder ad = new AlertDialog.Builder(Main.this);
					//ad.setIcon(android.R.drawable.ic_input_add);
					ad.setTitle("New Project");
					FrameLayout layout = new FrameLayout(Main.this);
					final EditText text = new EditText(Main.this);
					
					layout.addView(text);
					ad.setView(layout);
	
					ad.setNegativeButton("Cancel", null);
					ad.setPositiveButton("Create", new android.content.DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int arg1) {
							projManager.createNewProject(text.getText().toString().replace('.', ' '));
							updateList();
						}
					});
	
					ad.show();
				}
			}
		});
		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View view, final int index, long arg3) {
				if(index > 0) {
					final CharSequence[] items = {"Delete", "Rename", "Clone"};
					
					final String projName = projManager.getProject(index-1).getProjectName();
					AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
					builder.setTitle("Project \"" + projName + "\"");
					builder.setItems(items, new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int item) {
					    	// Bo�te de dialogue
							AlertDialog.Builder ad = new AlertDialog.Builder(Main.this);
							FrameLayout layout = new FrameLayout(Main.this);
							final EditText text = new EditText(Main.this);
							layout.addView(text);
							ad.setNegativeButton("Cancel", null);
							
					    	switch(item) {
					    	case 0: // SUPPRIMER FICHIER
								//ad.setIcon(android.R.drawable.ic_menu_delete);
								ad.setTitle("Delete \"" + projName + "\"");
								ad.setMessage("Are you sure ?");
								ad.setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int arg1) {
										projManager.getProject(index-1).delete();
							    		updateList();
									}
								});
								ad.show();
					    		break;
					    	case 1: // RENOMMER
								//ad.setIcon(android.R.drawable.ic_menu_edit);
								ad.setTitle("Rename \"" + projName + "\"");
								ad.setView(layout);
								ad.setPositiveButton("Rename", new android.content.DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int arg1) {
										projManager.getProject(index-1).rename(text.getText().toString().replace('.', ' '));
										updateList();
									}
								});
								ad.show();
					    		break;
					    	case 2: // CLONER
								//ad.setIcon(android.R.drawable.ic_menu_set_as);
								ad.setTitle("Clone \"" + projName + "\"");
								ad.setView(layout);
								ad.setPositiveButton("Clone", new android.content.DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int arg1) {
										projManager.getProject(index-1).clone(text.getText().toString().replace('.', ' '), projManager.getRootDirectory());
										updateList();
									}
								});
								ad.show();
					    		break;
					    	default:
					    		break;
					    	}
					        //Toast.makeText(Main.this, items[item], Toast.LENGTH_SHORT).show();
					    }
					});
					builder.create().show();
				}
				return true;
			}
		});

		myAdapter adpt = new myAdapter(this);
		list.setAdapter(adpt);
		
		/* v�rification si texte de secours enregistr�
		 * affichage boite de dialogue
		 * suppression si refus� ou apr�s r�cup�ration
		 */
	}

	@Override
	public void onResume() {
		super.onResume();
		updateList();
	}

	void updateList() {
		projManager.loadList();
		projManager.log();
		
		myAdapter adpt = new myAdapter(this);
		list.setAdapter(adpt);
		
	}
	
	public class myAdapter extends BaseAdapter {
		private LayoutInflater myInflater;

		public myAdapter(Context c) {
			this.myInflater = LayoutInflater.from(c);
		}
		public int getCount() {
			return projManager.getNbProject()+1;
		}
		public Object getItem(int index) {
			if(index == 0)
				return "Create a new project";
			return projManager.getProject(index-1).getProjectName();
		}
		public long getItemId(int position) {
			return position;
		}
		
		public class ViewHolder { TextView text; }
		
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {
				convertView = myInflater.inflate(R.layout.list_main, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.tv_list);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.text.setText((String)getItem(position));

			return convertView;
		}

	}
	
	
	private class ProjectManager {
		static final String DirectoryName = "Scroid";
		private ArrayList<Project> mProjects = new ArrayList<Project>();
		private File mRootDirectory = null;
		
		public ProjectManager() {
			if (Environment.getExternalStorageState().compareTo(Environment.MEDIA_MOUNTED) == 0) {
				mRootDirectory = new File(Environment.getExternalStorageDirectory(), DirectoryName);
				
				if (!mRootDirectory.exists())
					mRootDirectory.mkdir();
			}
		}
		
		public File getRootDirectory() {
			return mRootDirectory;
		}

		public void createNewProject(String projectName) {
			Project.create(projectName, mRootDirectory);
		}
		public Project getProject(int index) {
			return mProjects.get(index);
		}
		public int getNbProject() {
			return mProjects.size();
		}
		
		public void loadList() {
			mProjects.clear();
			
			// list of main files
			String s[] = mRootDirectory.list(new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					// must have the correct file extension
					if(!filename.endsWith(Project.fileExtension))
						return false;
					// must not contain a dot 
					if(filename.lastIndexOf('.', filename.length()-Project.fileExtension.length()-1) != -1)
						return false;
					// must be a file
					return new File(dir, filename).isFile(); 
				}
			});
			
			// fill the array list
			for (int i = 0; i < s.length; i++)
				mProjects.add(new Project(s[i].substring(0, s[i].lastIndexOf('.')), mRootDirectory));
			
			// Sort the array list alphabeticaly
			Collections.sort(mProjects, new Comparator<Project>() {
				public int compare(Project lhs, Project rhs) {
					return lhs.getProjectName().compareToIgnoreCase(rhs.getProjectName());
				}
			});
		}
		void log() {
			Log.i("###", this.mRootDirectory.getAbsolutePath());
			for(int i = 0; i < this.mProjects.size(); i++) {
				Log.i("###", "#" + this.mProjects.get(i).getProjectName());
				for(int j = 1; j < this.mProjects.get(i).getNbFiles(); j++)
					Log.i("###", "-> " + this.mProjects.get(i).getFileName(j));
			}
		}
	}
}
/*<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
<item android:state_pressed="true" android:color="#FF508050" />
<item android:state_focused="true" android:color="#FF505050" />
<item android:color="#FF353535" />
</selector>
*/