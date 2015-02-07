package com.awprog.scroidv2.AlphaScript;

import java.util.Arrays;
import java.util.Comparator;

import android.R;
import android.content.Context;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;

import com.awprog.scroidv2.EditorActivity;

public class ColorEditText extends MultiAutoCompleteTextView {
	TextWatcher mTextWatcher;
	//private boolean mScriptHasChanged;
	EditorActivity mEditor;
	
	public ColorEditText(Context context) {
		super(context);
		mEditor = (EditorActivity) context;

		init();
	}
	public ColorEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		mEditor = (EditorActivity) context;

		init();
	}
	public ColorEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mEditor = (EditorActivity) context;
		
		init();
	}
	
	@Override
	protected void onSelectionChanged(int selStart, int selEnd) {
		super.onSelectionChanged(selStart, selEnd);
		// Compute line and column position of the cursor
		if(mEditor != null) {
			int selectionEnd = ColorEditText.this.getSelectionEnd();
			String txt = ColorEditText.this.getText().toString();
			int last = txt.lastIndexOf('\n', selectionEnd-1);
			int line = 0, column = selectionEnd - last - 1;

			while((last = txt.lastIndexOf('\n', selectionEnd-1)) != -1) {
				line++;
				selectionEnd = last;
			}
			
			// Update position indicator in the toolbox of the editor's view
			mEditor.setPositionIndicator(line + 1, column);
		}
	}
	
	private void setAutoComplete() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_dropdown_item_1line, Dictionnary.getKeywordList());
		this.setAdapter(adapter);
		
		Tokenizer tokenizer = new Tokenizer() {
			public CharSequence terminateToken(CharSequence text) {
				return text;
			}
			
			public int findTokenStart(CharSequence text, int cursor) {
				String s = text.toString();
				int start = 0;
				
				for(int i = Dictionnary.carEnd.length()-1; i >= 0; --i) {
					int result = s.lastIndexOf(Dictionnary.carEnd.charAt(i), cursor-1)+1;
					if(result != -1 && result > start)
						start = result;
				}
				
				return start;
			}
			
			public int findTokenEnd(CharSequence text, int cursor) {
				String s = text.toString();
				int end = text.length()-1;
				
				for(int i = Dictionnary.carEnd.length()-1; i >= 0; --i) {
					int result = s.indexOf(Dictionnary.carEnd.charAt(i), cursor+1)-1;
					if(result != -1 && result < end)
						end = result;
				}
				
				return end;
			}
		};
		
		this.setTokenizer(tokenizer);
	}
	
	private void setTextWatcher() {
		mTextWatcher = new TextWatcher() {
			private int startChg;
			private int countChg;
			
			private int somme = 0;
			private int nb = 0;
			
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				startChg = start;
				countChg = count;
				//setScriptHasChanged(true);
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			/** Reset spannable string for the modified line(s) **/
			public void afterTextChanged(Editable s) {
				long t = SystemClock.elapsedRealtime();
				
				String str = s.toString();
				int lineStart = str.lastIndexOf('\n', startChg > 0 ? startChg-1 : startChg) + 1;
				int lineEnd = str.indexOf('\n', startChg+countChg);
				if(lineEnd == -1)
					lineEnd = str.length();
				if(lineEnd <= lineStart)
					return;

				SpannableString ss = getSpannableString(str.substring(lineStart, lineEnd));
				
				ColorEditText.this.removeTextChangedListener(mTextWatcher);
				s.replace(lineStart, lineEnd, ss);
				ColorEditText.this.addTextChangedListener(mTextWatcher);

				nb++;
				t = SystemClock.elapsedRealtime()-t;
				somme += t;
				Log.i("###", "afterTextChanged exec time : " + t + " average:" + ((double)somme/nb));

			}
		};
		this.addTextChangedListener(mTextWatcher);
	}

	private void setInputFilters() {
		InputFilter filterAutoIndent = new InputFilter() {
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				//Log.i("###", ""+dstart+" "+dend+"\n\""+dest+"\"");
				// Ajoute autant de tabulation et d'espace qu'à la ligne précédante
				if(end-start == 1 && source.charAt(start) == '\n') {
					String strDest = dest.toString();
					int lineStart = strDest.lastIndexOf('\n', dstart-1) + 1;
					
					String newStr = "\n";
					int index = lineStart;
					while(index < strDest.length()) {
						char c = strDest.charAt(index);
						if(c == ' ')
							newStr += ' ';
						else if(c == '\t')
							newStr += '\t';
						else
							break;
						
						index++;
					}
					return newStr;
				}
				return null;
			}
		};
		InputFilter filterTabulation = new InputFilter() {
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				// If the source is a tabulation
				if(end - start == 1 && source.charAt(start) == '\t') {
					// if the dest is on one line, remplace the tabulation by 3 spaces
					if(dend-dstart == 0 || dest.subSequence(dstart, dend).toString().indexOf('\n') == -1) {
						return "   ";
					}
					// the dest is on more than one line, just add tabulation at the beginning of each line
					else {
						return dest.toString().substring(dstart, dend).replaceAll("\n", "\n   ");
					}
				}
				return null;
			}
		};
		/*InputFilter filterShortcut = new InputFilter() {
			private final String shortcuts[] = {
					"+", "add",
					"*", "mul",
					"/", "div",
					"%", "mod",
					"^", "pow",
					"=", "set"
			};
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				if(end - start != 1)
					return null;
				
				char c = source.subSequence(start, end).toString().charAt(0);
				for(int i = shortcuts.length/2-1; i >= 0; i--)
					if(c == shortcuts[i*2].charAt(0))
						return shortcuts[i*2 + 1];
				
				return null;
			}
		};*/
		
		this.setFilters(new InputFilter[] {filterAutoIndent, filterTabulation/*, filterShortcut*/});
	}
	
	private void init() {
		setAutoComplete();
		setTextWatcher();
		setInputFilters();
		
		// TODO : setValidator(validator);
		//setDropDownAnchor(R.id.closeButton);
		//mScriptHasChanged = false;
	}

	/*public boolean scriptHasChanged() {
		return mScriptHasChanged;
	}
	public void setScriptHasChanged(boolean state) {
		mScriptHasChanged = state;
	}*/
	
	private static void setSpan(SpannableString colorText, int start, int end, int color, boolean bold, boolean italic) {
		colorText.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		//if(bold || italic)
		//	colorText.setSpan(new StyleSpan((bold?Typeface.BOLD:0)|(italic?Typeface.ITALIC:0)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	}
	
	private SpannableString getSpannableString(String text) {
		SpannableString colorText = new SpannableString(text);
		int index = 0;
		int len = text.length();
		
		while(index < len) {
			char c = Character.toLowerCase(text.charAt(index));
			/// Ignored character \\\
			if((new String(" \t\n")).indexOf(c) != -1) {
				index++;
			}
			/// Comments \\\
			else if(c == '\'' || c == '!') {
				int indexEndLine = text.indexOf('\n', index+1);
				if(indexEndLine == -1)
					indexEndLine = len;
				
				setSpan(colorText, index, indexEndLine, 0xFF808040, false, true);
				index = indexEndLine;
			}
			/// String \\\
			else if(c == '\"') {
				int indexEndStr = text.indexOf('\"', index+1)+1;
				int indexEndLine = text.indexOf('\n', index+1);
				if(indexEndStr == 0)
					indexEndStr = len;
				if(indexEndLine == -1)
					indexEndLine = len;
				
				int end = Math.min(indexEndStr, indexEndLine);
				setSpan(colorText, index, end, 0xFFFF2020, false, false);
				index = end;
			}
			/// Number \\\
			else if(Character.isDigit(c) || c == '.' || c == '-') {
				int indexEndNumber = index;
				
				while(indexEndNumber < len) {
					c = text.charAt(indexEndNumber);
					if(!Character.isDigit(c) && c != '.' && c != '-')
						break;
					indexEndNumber++;
				}
				
				setSpan(colorText, index, indexEndNumber, 0xFF808080, false, false);
				index = indexEndNumber;
			}
			/// Paranthesis \\\
			else if(c == '(' || c == ')' || c == ',') {
				index++;
			}
			/// Word/other \\\
			else {
				int indexEnd = text.indexOf('\n', index);
				if(indexEnd == -1)
					indexEnd = len;
				
				for(int i = Dictionnary.carEnd.length()-1; i >= 0; --i) {
					int indexOf = text.indexOf(Dictionnary.carEnd.charAt(i), index);
					if(indexOf != -1 && indexOf < indexEnd)
						indexEnd = indexOf;
				}
				String word = text.substring(index, indexEnd).toLowerCase();
				int search = Arrays.binarySearch(Dictionnary.getKeywordList(), word, new Comparator<String>() {
					public int compare(String lhs, String rhs) {
						return lhs.compareTo(rhs);
					}
				});
				if(search >= 0)
					setSpan(colorText, index, indexEnd, Dictionnary.getKeywordColor(word), true, false);
				else
					setSpan(colorText, index, indexEnd, 0xFF202020, false, true);
				
				index = indexEnd;
			}
		}
		
		return colorText;
	}
	
	public void selectLine(int line) {
		String s = getText().toString();
		int l = 0;
		int indexStartLine = 0, indexEndLine = 0;
		for(int i = 0; i < s.length(); i++) {
			if(i == s.length()-1 || s.charAt(i) == '\n') {
				l++;
				indexEndLine = i+1;
				if(l == line) {
					setSelection(indexStartLine, indexEndLine);
					break;
				}
				else
					indexStartLine = indexEndLine;
			}
		}
	}
}
