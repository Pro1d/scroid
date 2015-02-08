package com.awprog.scroidv2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.awprog.scroidv2.AlphaScript.Dictionnary;

public class DocView {
	private Context ctxt;
	private AlertDialog dialog;
	private Documentation data;
	
	public DocView(Context c) {
		ctxt = c;
		data = new Documentation();
		
		createView();
	}
	
	public void createView() {
		AlertDialog.Builder ad = new AlertDialog.Builder(ctxt);
		
		ad.setTitle("Documentation");
		ad.setNeutralButton("Close", null);

		ExpandableListAdapter adapter = new ExpandableListAdapter() {
			public void unregisterDataSetObserver(DataSetObserver observer) { }
			public void registerDataSetObserver(DataSetObserver observer) { }
			public void onGroupExpanded(int groupPosition) { }
			public void onGroupCollapsed(int groupPosition) { }
			public boolean isEmpty() { return false; }
			public boolean isChildSelectable(int groupPosition, int childPosition) { return false; }
			public boolean hasStableIds() { return false; }
			public boolean areAllItemsEnabled() { return true; }
			public long getCombinedGroupId(long groupId) { return groupId; }
			public long getCombinedChildId(long groupId, long childId) { return 0; }
			
			public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
				TextView v = (TextView) LayoutInflater.from(ctxt).inflate(android.R.layout.simple_expandable_list_item_1, null);
				
				v.setText(data.getCategory(groupPosition).name);
				
				return v;
			}
			public long getGroupId(int groupPosition) {
				return groupPosition;
			}
			public int getGroupCount() {
				return data.getNbCategory();
			}
			public Object getGroup(int groupPosition) {
				return data.getCategory(groupPosition);
			}
			
			public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
				Documentation.Category.Function f = data.getCategory(groupPosition).getFunc(childPosition);
				if(f.getView() == null) {
					/*try {
						itemViewParser.setInput(null);
					} catch (XmlPullParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
					View v = LayoutInflater.from(ctxt).inflate(R.layout.item_doc, null);//LayoutInflater.from(ctxt).inflate(itemViewParser, null);

					// TextView BEFORE
					if(f.getBefore().length() == 0)
						v.findViewById(R.id.tv_before_item_doc).setVisibility(View.GONE);
					else
						((TextView) v.findViewById(R.id.tv_before_item_doc)).setText("("+f.getBefore()+")");
					
					// TextView NAME
					((TextView) v.findViewById(R.id.tv_name_item_doc)).setText(f.getName().toUpperCase(Locale.getDefault()));

					// TextView AFTER
					if(f.getAfter().length() == 0)
						v.findViewById(R.id.tv_after_item_doc).setVisibility(View.GONE);
					else
						((TextView) v.findViewById(R.id.tv_after_item_doc)).setText("("+f.getAfter()+")");

					// TextView COMMENTARY
					if(f.getCommentary().length() == 0)
						v.findViewById(R.id.tv_comm_item_doc).setVisibility(View.GONE);
					else
						((TextView) v.findViewById(R.id.tv_comm_item_doc)).setText(f.getCommentary());

					// TextView RETURN
					if(f.getReturn().length() == 0)
						v.findViewById(R.id.tv_return_item_doc).setVisibility(View.GONE);
					else
						((TextView) v.findViewById(R.id.tv_return_item_doc)).setText("Return : " + f.getReturn());
					
					f.setView(v);
				}
				
				return f.getView();
			}
			public long getChildId(int groupPosition, int childPosition) {
				return 0;
			}
			public int getChildrenCount(int groupPosition) {
				return data.getCategory(groupPosition).getNbFunc();
			}
			public Object getChild(int groupPosition, int childPosition) {
				return data.getCategory(groupPosition).getFunc(childPosition);
			}
		};
		
		ExpandableListView expListView = new ExpandableListView(ctxt);
        expListView.setAdapter(adapter);
        expListView.setDivider(null);
        
        ad.setView(expListView);
		
		dialog = ad.create();
	}
	
	public void show() {
		dialog.show();
	}
	
	private class Documentation {
		Category[] data;
		
		public Documentation() {
			HashMap<String, Category> hash =  new HashMap<String, Category>();
			for(int i = 0; i < (Dictionnary.funcAndParamsAndDoc).length; ++i)
			{
				String k = Dictionnary.funcAndParamsAndDoc[i][0];
				if(!hash.containsKey(k))
					hash.put(k, new Category(k));
				
				hash.get(k).addFunc(Dictionnary.funcAndParamsAndDoc[i]);
			}
			Object[] array = hash.values().toArray();
			data = new Category[array.length];
			for(int i = data.length-1; i >= 0; --i)
				data[i] = (Category) array[i];
		}
		
		int getNbCategory() {
			return data.length;
		}
		
		Category getCategory(int i) {
			return data[i];
		}

		class Category {
			String name;
			private ArrayList<Function> functions = new ArrayList<Function>();
			
			public Category(String s) {
				if(s.compareTo("i/o") == 0)
					name = "Input / Output";
				else if(s.compareTo("bop") == 0)
					name = "Basic operator";
				else if(s.compareTo("boo") == 0)
					name = "Boolean";
				else if(s.compareTo("str") == 0)
					name = "String";
				else if(s.compareTo("mfc") == 0)
					name = "Mathematics";
				else if(s.compareTo("lop") == 0)
					name = "Logical operator";
				else if(s.compareTo("sfc") == 0)
					name = "Structure";
				else if(s.compareTo("std") == 0)
					name = "Standard";
				else if(s.compareTo("cmp") == 0)
					name = "Comparator";
				else if(s.compareTo("cst") == 0)
					name = "Constant";
				else if(s.compareTo("vas") == 0)
					name = "Variable";
				else if(s.compareTo("bas") == 0)
					name = "Condition and loop";
				else if(s.compareTo("tim") == 0)
					name = "Time";
				else
					name = "[#???#]";
			}
			
			void addFunc(String s[]) {
				functions.add(new Function(s));
			}

			int getNbFunc() {
				return functions.size();
			}
			Function getFunc(int i) {
				return functions.get(i);
			}
			
			class Function {
				private String[] data;
				private View view;
				
				Function(String s[]) {
					data = s;
				}
				String getName() {
					return data[Dictionnary.NAM];
				}
				String getBefore() {
					return data[Dictionnary.BEF];
				}
				String getAfter() {
					return data[Dictionnary.AFT];
				}
				String getReturn() {
					return data[Dictionnary.RET];
				}
				String getCommentary() {
					return data[Dictionnary.COM];
				}
				View getView() {
					return view;
				}
				void setView(View v) {
					view = v;
				}
			}
		}
	}
	
	/**
	 AlertDialog.Builder ad = new AlertDialog.Builder(Main.this);
				ad.setTitle(getResources().getString(R.string.menu_groupe));
				ad.setCancelable(true);
		        final AlertDialog dialog = ad.create();
				
		        // Create the list
				final String NAME = "NAME", IS_EVEN = "IS_EVEN";
				List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
		        List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
		        
		        for (int i = 0; i < 5; i++) {
		            Map<String, String> curGroupMap = new HashMap<String, String>();
		            groupData.add(curGroupMap);
		            curGroupMap.put(NAME, ""+(1+i)+(i==0?"�re":"�me")+" ann�e");
		            curGroupMap.put(IS_EVEN, "Ann�e/PO");
		            
		            List<Map<String, String>> children = new ArrayList<Map<String, String>>();
			        for (int j = 0; j < mListGroups[i].length; j++) {
		                Map<String, String> curChildMap = new HashMap<String, String>();
		                children.add(curChildMap);
		                curChildMap.put(NAME, mListGroups[i][j]);
		                //curChildMap.put(IS_EVEN, "Wesh z\'y va ! choisi ce pegrou ci c� celui que c� ke tu ve mat�");
		            }
		            childData.add(children);
		        }
		        
		        // Set up our adapter
		        ExpandableListAdapter adapter = new SimpleExpandableListAdapter(Main.this,
		                groupData, android.R.layout.simple_expandable_list_item_1,
		                new String[] {NAME, IS_EVEN}, new int[] { android.R.id.text1, android.R.id.text2 },
		                childData, android.R.layout.simple_expandable_list_item_2,
		                new String[] {NAME, IS_EVEN}, new int[] { android.R.id.text1, android.R.id.text2 });
		        
		        // Create the list view
		        ExpandableListView expListView = new ExpandableListView(Main.this);
		        expListView.setAdapter(adapter);
		        if(mDesiredYearNumero != -1)
		        	expListView.expandGroup(mDesiredYearNumero);
		        expListView.setOnChildClickListener(new OnChildClickListener() {
					public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
						mDesiredYearNumero = groupPosition;
						mDesiredGroupNumero = childPosition;
						mDesiredGroupId = mListGroupId[groupPosition][childPosition];
						
		    			SeekDataAsync(true, false, false);
		    			setGroupTexts();
		    			dialog.cancel();
		    			
		    			return true;
				}});
		        
		        dialog.setView(expListView);
				dialog.show();
	 */
}
