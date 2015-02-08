package com.awprog.scroidv2;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.awprog.scroidv2.StandardIOService.SyncData;
import com.awprog.scroidv2.AlphaScript.Compiler;
import com.awprog.scroidv2.AlphaScript.ErrorDialog;
import com.awprog.scroidv2.AlphaScript.ErrorDialog.ScriptException;
import com.awprog.scroidv2.AlphaScript.ScriptRunner;

public class RunnerActivity extends Activity {
	/** TODO
	 * GUI
	 * interface runner
	 **/

	// bottom input box
	private EditText mEditTextInput;
	private ProgressBar mProgressBar;
	private LinearLayout mBooleanButton;
	private Button mButtonContinue;
	
	// Console
	private TextView mTextViewConsole;
	private StringBuilder mText;
	
	// The runner :P
	private ScriptRunner mRunner = null;
	private Thread mRunnerThread;
	
	Project mProject;
	
	// Error Dialog
	ErrorDialog mErrorDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_executeur);
		
		setUpInputView();
		setUpOutputView();
		mProject = new Project(getIntent().getExtras().getStringArray("project"));
		mProject.load();
		mErrorDialog = new ErrorDialog(this, mProject.getProjectName(), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				RunnerActivity.this.finish();
			}
		});
		// TODO NOW : mRunner = new ScriptRunner();
	}
	/*
	 * android:maxLines = "AN_INTEGER"

android:scrollbars = "vertical"
properties of your TextView in your layout's xml file.

Then use:

yourTextView.setMovementMethod(new ScrollingMovementMethod())

in your code.
	 */
	
	@Override
	protected void onResume() {
		super.onResume();
		// AsyncTask : 
		if(mRunner != null) {
			if(mRunner.isRunning())
				mRunner.resume();
		}
		/*else if(!mRunner.isEnded())
			mRunner.run();*/
		
		// TODO start threads
	}
	@Override
	protected void onPause() {
		super.onPause();

		if(mRunner != null) {
			if(mRunner.isRunning())
				mRunner.pause();
		}
	}
	@Override
	protected void onStop() {
		super.onStop();
		if(mRunner != null) {
			if(mRunner.isRunning()) {
				mRunner.end();
				try {
					mRunnerThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		/// Compilation et ex�cution
		if(hasFocus && mRunner == null) {
			/// Compilation du projet
			Compiler cpl = new Compiler();
			cpl.setProjectSource(mProject);
			cpl.setKeywordsList();
			try {
				cpl.build();
			} catch (ScriptException e) {
				e.printStackTrace();
				this.getIntent().putExtra("error_line", e.getLine());
				this.getIntent().putExtra("error_file", e.getFile());
				mErrorDialog.showError(e);
				return;
			}
			
			/// Cr�ation du runner
			mRunner = new ScriptRunner(cpl.getExecutable());
			mIOService = new StandardIOService(mRunner, mHandlerIO, mHandlerIO);
			mRunner.setIOService(mIOService);
			
			mRunnerThread = new Thread(new Runnable() {
				public void run() {
					try {
						mRunner.run();
					} catch (ScriptException e) {
						e.printStackTrace();
						RunnerActivity.this.getIntent().putExtra("error_line", e.getLine());
						RunnerActivity.this.getIntent().putExtra("error_file", e.getFile());
						mErrorDialog.showError(e);
					}
				}
			});
			mRunnerThread.start();
		}
	}
	
	/** Initialise les vues utilis�es pour les inputs **/
	private void setUpInputView() {
		mEditTextInput = (EditText) findViewById(R.id.et_executeur);
		mEditTextInput.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_DONE && syncDataRef != null) {
					/// String input
					if(v.getInputType() == InputType.TYPE_CLASS_TEXT) {
						syncDataRef.s = v.getText().toString();
					}
					/// Number input
					else {
						String txt = v.getText().toString();
						if(txt.length() == 0)
							return false;
						syncDataRef.n = Double.parseDouble(txt);
					}
					changeInputType(InputTypeView.INPUT_VIEW_NONE);
					syncDataRef.inputDone.set(true);
					synchronized (syncDataRef) {
						syncDataRef.notify();
					}
					syncDataRef = null;
					return true;
				}
				return false;
			}
		});
		mProgressBar = (ProgressBar) findViewById(R.id.pb_executeur);
		mButtonContinue = (Button) findViewById(R.id.b_executeur_continue);
		mButtonContinue.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				changeInputType(InputTypeView.INPUT_VIEW_NONE);
				if(syncDataRef != null) {
					syncDataRef.inputDone.set(true);
					synchronized (syncDataRef) {
						syncDataRef.notify();
					}
					syncDataRef = null;
				}
			}
		});
		mBooleanButton = (LinearLayout) findViewById(R.id.ll_boolean);
		mBooleanButton.findViewById(R.id.b_executeur_true).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				changeInputType(InputTypeView.INPUT_VIEW_NONE);
				if(syncDataRef != null) {
					syncDataRef.b = true;
					syncDataRef.inputDone.set(true);
					synchronized (syncDataRef) {
						syncDataRef.notify();
					}
					syncDataRef = null;
				}
			}
		});
		mBooleanButton.findViewById(R.id.b_executeur_false).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				changeInputType(InputTypeView.INPUT_VIEW_NONE);
				if(syncDataRef != null) {
					syncDataRef.b = false;
					syncDataRef.inputDone.set(true);
					synchronized (syncDataRef) {
						syncDataRef.notify();
					}
					syncDataRef = null;
				}
			}
		});
		
		currentVisibleInputView = mProgressBar;
	}
	private void setUpOutputView() {
		mTextViewConsole = (TextView) findViewById(R.id.tv_executeur);
	}
	
	private enum InputTypeView{INPUT_VIEW_TEXT, INPUT_VIEW_NUMBER, INPUT_VIEW_BOOLEAN, INPUT_VIEW_PAUSE, INPUT_VIEW_NONE};
	View currentVisibleInputView;
	private void changeInputType(InputTypeView type) {
		currentVisibleInputView.setVisibility(View.GONE);
		switch(type) {
		case INPUT_VIEW_TEXT:
			mEditTextInput.setInputType(InputType.TYPE_CLASS_TEXT);
			mEditTextInput.setVisibility(View.VISIBLE);
			mEditTextInput.setText("");
			currentVisibleInputView = mEditTextInput;
			break;
		case INPUT_VIEW_NUMBER:
			mEditTextInput.setInputType(InputType.TYPE_CLASS_NUMBER
										| InputType.TYPE_NUMBER_FLAG_DECIMAL
										| InputType.TYPE_NUMBER_FLAG_SIGNED);
			mEditTextInput.setVisibility(View.VISIBLE);
			mEditTextInput.setText("");
			currentVisibleInputView = mEditTextInput;
			break;
		case INPUT_VIEW_BOOLEAN:
			currentVisibleInputView = mBooleanButton;
			break;
		case INPUT_VIEW_PAUSE:
			currentVisibleInputView = mButtonContinue;
			break;
		case INPUT_VIEW_NONE:
			currentVisibleInputView = mProgressBar;
			break;
		}
		currentVisibleInputView.requestFocus();
		currentVisibleInputView.setVisibility(View.VISIBLE);
	}
	
	/** Standard IO Service **/
	private StandardIOService mIOService;
	private StandardIOService.SyncData syncDataRef;
	private Handler mHandlerIO = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case StandardIOService.TEXT_OUTPUT:
				setTextConsole((StringBuilder)msg.obj);
				break;
			case StandardIOService.BOOLEAN_INPUT:
				syncDataRef = (SyncData) msg.obj;
				changeInputType(InputTypeView.INPUT_VIEW_BOOLEAN);
				break;
			case StandardIOService.STRING_INPUT:
				syncDataRef = (SyncData) msg.obj;
				changeInputType(InputTypeView.INPUT_VIEW_TEXT);
				break;
			case StandardIOService.NUMBER_INPUT:
				syncDataRef = (SyncData) msg.obj;
				changeInputType(InputTypeView.INPUT_VIEW_NUMBER);
				break;
			case StandardIOService.PAUSE_INPUT:
				syncDataRef = (SyncData) msg.obj;
				changeInputType(InputTypeView.INPUT_VIEW_PAUSE);
				break;
			}
		};
	};
	
	private void setTextConsole(StringBuilder sb) {
		mTextViewConsole.setText(sb.toString());
	}
}
