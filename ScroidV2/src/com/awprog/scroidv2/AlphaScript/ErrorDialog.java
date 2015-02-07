package com.awprog.scroidv2.AlphaScript;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface.OnClickListener;

public class ErrorDialog {
	private final String scriptName;
	//private ErrorDescription content;
	private final Activity ctxt;
	private final OnClickListener listener;
	
	public ErrorDialog(Activity c, String sn, OnClickListener onCloseListener) {
		scriptName = sn;
		ctxt = c;
		listener = onCloseListener;
	}
	
	public void showError(ScriptException err) {
		final AlertDialog.Builder builder = new Builder(ctxt);
		
		builder.setTitle("Fatal error - running " + scriptName);
		builder.setMessage(err.category+" :\nIn file \""+err.file+"\" at line " + err.line + " : " + err.msg);
		
		builder.setCancelable(false);
		builder.setPositiveButton("I'm going to check my script !", listener);
		
		ctxt.runOnUiThread(new Runnable() {
			public void run() {
				builder.create().show();
			}
		});
	}
	
	static public class ScriptException extends Exception {
		private static final long serialVersionUID = 1L;
		private String category;
		private String msg;
		private String file;
		private int line;// commence au numéro de ligne 1

		public ScriptException(String cat, String msg, String file, int l) {
			category = cat;
			this.msg = msg;
			line = l;
			this.file = file;
		}
		public ScriptException(String msg, String file, int l) {
			this.msg = msg;
			line = l;
			this.file = file;
		}
		public ScriptException(String msg, int l) {
			this.msg = msg;
			line = l;
		}
		public ScriptException(String msg, String file) {
			this.msg = msg;
			this.file = file;
		}
		public ScriptException(String msg) {
			this.msg = msg;
		}

		public void setLine(int l) {
			line = l;
		}
		public void setFile(String file) {
			this.file = file;
		}
		public void setCat(String cat) {
			category = cat;
		}
		public int getLine() {
			return line;
		}
		public String getFile() {
			return file;
		}
	}
}
