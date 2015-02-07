package com.awprog.scroidv2;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import com.awprog.scroidv2.AlphaScript.ScriptRunner;

public class StandardIOService {
	
	public StandardIOService(ScriptRunner sr, Handler handlerOutputActivity, Handler handlerInputActivity) {
		mHandlerOutputActivity = handlerOutputActivity;
		mHandlerInputActivity = handlerInputActivity;
	}
	
	/**
	 * Commandes script :
	 * write 
	 * clear
	 * input (wait)
	 * 
	 * Commande GUI :
	 * send data (notify)
	 */
	
	/**** STANDARD INPUT ****/
	
	private Handler mHandlerInputActivity; // Created in the GUI Looper
	
	static final int NUMBER_INPUT = 0, STRING_INPUT = 1, BOOLEAN_INPUT = 2, PAUSE_INPUT = 3;
	private static final long DELAY_WAIT_INPUT = 1;
	class SyncData {
		public AtomicBoolean inputDone = new AtomicBoolean(false);
		public String s=null;
		public double n=0;
		public boolean b=false;
	}; SyncData data = new SyncData();
	
	public double readNumber(ScriptRunner srRef) {
		data.inputDone.set(false);
		Message msg = new Message();
		msg.what = NUMBER_INPUT;
		msg.obj = data;
		mHandlerInputActivity.sendMessage(msg);

		while(!srRef.isEnded() && !data.inputDone.get())
			synchronized (data) {
				try {
					data.wait(DELAY_WAIT_INPUT);
				} catch (InterruptedException e) { }
			}
		
		return data.n;
	}
	public String readString(ScriptRunner srRef) {
		data.inputDone.set(false);
		Message msg = new Message();
		msg.what = STRING_INPUT;
		msg.obj = data;
		mHandlerInputActivity.sendMessage(msg);

		while(!srRef.isEnded() && !data.inputDone.get())
			synchronized (data) {
				try {
					data.wait(DELAY_WAIT_INPUT);
				} catch (InterruptedException e) { }
			}
		
		return data.s;
	}
	public boolean readBoolean(ScriptRunner srRef) {
		data.inputDone.set(false);
		Message msg = new Message();
		msg.what = BOOLEAN_INPUT;
		msg.obj = data;
		mHandlerInputActivity.sendMessage(msg);

		while(!srRef.isEnded() && !data.inputDone.get())
			synchronized (data) {
				try {
					data.wait(DELAY_WAIT_INPUT);
				} catch (InterruptedException e) { }
			}
		
		return data.b;
	}
	public void waitButton(ScriptRunner srRef) {
		data.inputDone.set(false);
		Message msg = new Message();
		msg.what = PAUSE_INPUT;
		msg.obj = data;
		mHandlerInputActivity.sendMessage(msg);

		while(!srRef.isEnded() && !data.inputDone.get())
			synchronized (data) {
				try {
					data.wait(DELAY_WAIT_INPUT);
				} catch (InterruptedException e) { }
			}
	}
	
	
	/**** STANDARD OUTPUT ****/
	
	private Handler mHandlerOutputActivity; // Created in the GUI Looper
	private Handler mHandlerBufferTextOut = new Handler();
	private final static long delayBuffer = 50/*ms*/;
	public static final int TEXT_OUTPUT = 4;
	private volatile long mLastPushTime = SystemClock.elapsedRealtime();
	private StringBuilder mTextConsole = new StringBuilder();
	
	private Runnable mRunnableTextOut = new Runnable() {
		StringBuilder buffer = new StringBuilder();
		public void run() {
			mLastPushTime = SystemClock.elapsedRealtime();
			
			synchronized (mTextConsole) {
				buffer.delete(0, buffer.length()).append(mTextConsole);
			}
			Message msg = new Message();
			msg.what = TEXT_OUTPUT;
			msg.obj = buffer;
			
			mHandlerOutputActivity.sendMessage(msg);
		}
	};
	
	/** Envoie un texte � afficher dans la console; mettre 'null' pour effacer
	 * Doit �tre appel� depuis le thread du script */
	public void writeText(StringBuilder str) {
		// Annulation du pr�c�dant post
		mHandlerBufferTextOut.removeCallbacks(mRunnableTextOut);
		
		// Op�ration sur le texte de la console
		synchronized (mTextConsole) {
			if(str == null)
				mTextConsole.delete(0, mTextConsole.length());
			else
				mTextConsole.append(str);
		}

		// calcul du nouveau d�lai avant commit
		long delay = Math.max(delayBuffer - (SystemClock.elapsedRealtime() - mLastPushTime), 0);
		mHandlerBufferTextOut.postDelayed(mRunnableTextOut, delay);
	}
}
