package com.awprog.scroidv2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.awprog.scroidv2.AlphaScript.ColorEditText;

public class EditorActivity extends Activity {
	//private String mScriptName;
	//private final File mScroidDir = new File(Environment.getExternalStorageDirectory(), "Scroid");
	private Project mProject;
	private int mCurrentFile = 0;
	
	/** Alert dialog : confirm exit and save **/
	private AlertDialog.Builder mADConfirmClose = null;
	
	/** Alert dialog : Documention (expendable listview) **/
	private DocView mDocDialog;
	
	/** Views **/
	private ColorEditText mEditCodeView;
	private TextView mPositionIndicator;
	private ImageButton mDelFileButton;
	private ImageButton mPrevFileButton;
	private ImageButton mNextFileButton;
	private ImageButton mRenameFileButton;
	private TextView mTitleText;
	private int lastPosition[] = {-1,-1};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_editeur);
		

		mEditCodeView = (ColorEditText) findViewById(R.id.et_editeur);
		mPositionIndicator = (TextView) findViewById(R.id.tv_editeur_line);
		setPositionIndicator(0, 0);
		
		/** Image buttons in the left-side tool box **/
		// Button to save
		((ImageButton)findViewById(R.id.ib_editeur_save)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				saveScript();
			}
		});
		// Button to launch the script
		((ImageButton)findViewById(R.id.ib_editeur_run)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				saveScript();
				Intent intent = new Intent(EditorActivity.this, RunnerActivity.class);
				intent.putExtra("project", mProject.toStringArray());
				EditorActivity.this.startActivityForResult(intent, 1);
			}
		});
		// Button which opens the documentation view
		((ImageButton)findViewById(R.id.ib_editeur_doc)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(mDocDialog == null)
					mDocDialog = new DocView(EditorActivity.this);
				
				mDocDialog.show();
			}
		});
		// Button to add a new file in the project
		((ImageButton)findViewById(R.id.ib_editeur_addfile)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder ad = new AlertDialog.Builder(EditorActivity.this);
				FrameLayout layout = new FrameLayout(EditorActivity.this);
				final EditText text = new EditText(EditorActivity.this);
				layout.addView(text);
				ad.setView(layout);
				
				ad.setTitle("Add file");
				ad.setNegativeButton("Cancel", null);
				ad.setPositiveButton("Add", new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int arg1) {
						mProject.addFile(text.getText().toString().replace('.', ' '));
						// Save the last current file in editor
						storeCurrentFile();
						setCurrentFile(mProject.getNbFiles()-1);
						updateButtonEnabled();
					}
				});
				ad.show();
			}
		});
		// Button to add a new file in the project
		(mRenameFileButton = (ImageButton)findViewById(R.id.ib_editeur_renamefile)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(mCurrentFile == 0)
					return;
				
				AlertDialog.Builder ad = new AlertDialog.Builder(EditorActivity.this);
				FrameLayout layout = new FrameLayout(EditorActivity.this);
				final EditText text = new EditText(EditorActivity.this);
				layout.addView(text);
				ad.setView(layout);
				
				ad.setTitle("Rename file");
				ad.setNegativeButton("Cancel", null);
				ad.setPositiveButton("Rename", new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int arg1) {
						mProject.setFileName(mCurrentFile, text.getText().toString().replace('.', ' '));
						updateTitle();
					}
				});
				ad.show();
			}
		});
		// Button to show the previous file
		(mPrevFileButton=(ImageButton)findViewById(R.id.ib_editeur_title_previous)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				storeCurrentFile();
				changeCurrentFile(PREV_FILE);
			}
		});
		// Button to show the next file
		(mNextFileButton=(ImageButton)findViewById(R.id.ib_editeur_title_next)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				storeCurrentFile();
				changeCurrentFile(NEXT_FILE);
			}
		});
		// Button to delete the current file
		(mDelFileButton = (ImageButton)findViewById(R.id.ib_editeur_delfile)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(mCurrentFile == 0)
					return;
				AlertDialog.Builder ad = new AlertDialog.Builder(EditorActivity.this);
				FrameLayout layout = new FrameLayout(EditorActivity.this);
				final EditText text = new EditText(EditorActivity.this);
				layout.addView(text);
				ad.setTitle("Delete \"" + mProject.getFileName(mCurrentFile) + "\"");
				ad.setMessage("Are you sure ?");
				ad.setNegativeButton("Cancel", null);
				ad.setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int arg1) {
						mProject.deleteFile(mCurrentFile);
						setCurrentFile(mCurrentFile-1);
						updateButtonEnabled();
					}
				});
				ad.show();
			}
		});
		mDelFileButton.setEnabled(false);
		// Button equivalent to the back physical button 
		((ImageButton)findViewById(R.id.ib_editeur_close)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditorActivity.this.onBackPressed();
			}
		});
		
		//mScriptName = getIntent().getStringExtra("scriptName");
		
		mProject = new Project(getIntent().getStringArrayExtra("project"));
		/*Log.i("###", "open project : " + project.getProjectName());
		for(int j = 0; j < project.getNbFile(); j++)
			Log.i("###", "file"+j + " : " + project.getFileName(j+1));*/
		mTitleText = (TextView) findViewById(R.id.tv_titre_editeur);
		updateTitle();
		updateButtonEnabled();
		
		loadScript();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//super.onActivityResult(requestCode, resultCode, data);
		// ResultCode = 0 <=> SUCCES, 1 erreur localis�e, 2 autre erreur
		if(requestCode == 1 && resultCode == 1) {
			setCurrentFile(mProject.getFileIndex(data.getExtras().getString("error_file")));
			mEditCodeView.selectLine(data.getExtras().getInt("error_line"));
			// not useful to store current file (saved when launch exectutor's activity)
		}
	}
	@Override
	public void onBackPressed() {
		//super.onBackPressed();
		storeCurrentFile();
		if(mProject.contentHasChanged())//if(mEditCodeView.scriptHasChanged())
			confirmSaveAndQuit();
		else
			this.finish();
	}
	@Override
	protected void onPause() {
		super.onPause();
		
		saveScript(); // TODO epic fail -> see ConfirmSaveAndQuit's close button
	}
	/** Show a confirmation message and quit **/
	private void confirmSaveAndQuit() {
		/** Create the dialog box if it's not already done **/
		if(mADConfirmClose == null) {
			mADConfirmClose = new AlertDialog.Builder(EditorActivity.this);
			mADConfirmClose.setCancelable(true);
			mADConfirmClose.setIcon(android.R.drawable.ic_dialog_alert);
			mADConfirmClose.setTitle("Are you sure ?");
			mADConfirmClose.setMessage("Do you really want to close this script ? The changes will not be saved");
			mADConfirmClose.setPositiveButton("Close", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					EditorActivity.this.finish();// TODO epic fail -> see onPause()
				}
			});
			mADConfirmClose.setNeutralButton("Save and Close", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					saveScript();
					EditorActivity.this.finish();
				}
			});
			mADConfirmClose.setNegativeButton("Cancel", null);
		}
		
		mADConfirmClose.show();
	}
	/** Change the text which indicates the cursor position **/
	public void setPositionIndicator(int line, int column) {
		if(line != lastPosition[0] || column != lastPosition[1]) {
			lastPosition[0] = line;
			lastPosition[1] = column;
			mPositionIndicator.setText(""+line + " : "+column);
		}
	}
	/** Sets the title with the name of the current file **/
	public void updateTitle() {
		mTitleText.setText(mProject.getProjectName()+" : "+mProject.getFileName(mCurrentFile));
	}
	/** Sets the state of the buttons **/
	public void updateButtonEnabled() {
		mPrevFileButton.setEnabled(mProject.getNbFiles() > 1);
		mNextFileButton.setEnabled(mProject.getNbFiles() > 1);
		mDelFileButton.setEnabled(mCurrentFile != 0);
		mRenameFileButton.setEnabled(mCurrentFile != 0);
	}
	private void loadScript() {
		// Load the project (file content)
		mProject.load();
		// Open the main file in the editor
		mEditCodeView.setText(mProject.getFileContent(0));
		mCurrentFile = 0;
	}
	private void saveScript() {
		// Save the current file
		storeCurrentFile();
		// Save the whole project
		mProject.save();
	}
	static final int PREV_FILE = -1, NEXT_FILE = 1;
	/** Changes the current file, go to the file at pos current+'dir'; save the current file before **/
	private void changeCurrentFile(int dir) {
		setCurrentFile((mCurrentFile + dir + mProject.getNbFiles()) % mProject.getNbFiles());
	}
	/** Sets the current file in the editor **/
	private void setCurrentFile(int file) {
		mCurrentFile = file;
		onFileChanged();
	}
	/** Called when the selected file is changed **/
	private void onFileChanged() {
		// Change text
		mEditCodeView.setText(mProject.getFileContent(mCurrentFile));
		
		// Reset the cursor position indicator
		mEditCodeView.setSelection(mProject.getFileCursorPosition(mCurrentFile));
		
		// Update the title and the buttons of the view
		updateTitle();
		updateButtonEnabled();
	}
	/** Copy the content of the editor in the current project's file **/
	private void storeCurrentFile() {
		mProject.setFileContent(mCurrentFile, mEditCodeView.getText().toString());
		mProject.setFileCursorPosition(mCurrentFile, mEditCodeView.getSelectionStart());
	}
}
