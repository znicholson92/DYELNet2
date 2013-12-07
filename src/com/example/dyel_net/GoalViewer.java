package com.example.dyel_net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


public class GoalViewer {
	
	/**SQL attribute names **/
	private static final String SQLcategory = "goal_category";
	private static final String SQLgoalName = "name";
	private static final String SQLstartDate = "start_date";
	private static final String SQLendDate = "goal_date";
	private static final String SQLnotes = "notes";
	private static final String SQLcomplete = "completed";
	private static final String SQLtype = "type";
	private static final String SQLsubID = "subclassID";
	/************************/
	
	private static Stack<String> previous_SQL = new Stack<String>();
	private static Stack<String> previous_topbar = new Stack<String>();
	
	public static final String complete = "'Completed?'"; 
	public static int status = 0;
	public static String currentDayID = null;
	public static String currentExercise = null;
	private static String subID = null;
	private static String type = null;
	
	/**
	 * Show a list view of goals for the specific user
	 * @param app
	 * @param userID
	 */
	public static void viewSummary(MainActivity app, String userID)
	{
		status = 1;
		
		app.gotoLayout(R.layout.goal_view);
		LinearLayout goal_view_col_head = (LinearLayout)app.findViewById(R.id.goal_view_col_header);
		ListView goal_view_listview = (ListView)app.findViewById(R.id.goal_view_listView);
		TextView goal_view_topbar = (TextView)app.findViewById(R.id.goal_view_topbar_text);
		
		//String SQL = "select name as 'Goal Name' , start_date as 'Date(Start)' , goal_date as 'Date(Last)' from goals where username = 'testuser'";
		//String SQL = "select name as 'Goal Name' , start_date as 'Date(Start)' , goal_date as 'Date(Last)' from goals where username = 'testuser' ORDER BY completed ASC";
		String SQL = "select name as 'Goal Name' , completed as " +
				complete + " from goals where username = '" +
						userID +
						"' ORDER BY completed ASC";
		
		//app.con.readQuery(SQL, goal_view_listview, goal_view_col_head);		
		
		//Expired goals
		String queryResult = app.con.readQuery(SQL);		
		//System.out.println(queryResult);
		writeQueryResult(app, queryResult, goal_view_col_head, goal_view_listview);
		goal_view_topbar.setText("Goal View");	
		
		previous_SQL.push(SQL);
	}
	
	/**
	 * Show the detail of a goal when it is clicked from the list view of goals
	 * 
	 * @param app
	 * @param userID
	 * @param goalName
	 * @throws JSONException
	 */
	public static void viewDetail(MainActivity app, String userID,
			String goalName) throws JSONException {
		status = 2;
		app.gotoLayout(R.layout.goal_view_detail);
		TextView goalNameTV = (TextView)app.findViewById(R.id.goal_view_detail_goalName);
		TextView categoryTV = (TextView)app.findViewById(R.id.goal_view_detail_category);
		TextView startDateTV = (TextView)app.findViewById(R.id.goal_view_detail_startDate);
		TextView endDateTV = (TextView)app.findViewById(R.id.goal_view_detail_endDate);
		TextView notesTV = (TextView)app.findViewById(R.id.goal_view_detail_notes);
		TextView completeTV = (TextView)app.findViewById(R.id.goal_view_detail_completed);
		
		String SQL = "select * from goals " +
					"where username = '"+
					userID + "'" +
					" and name = '" +
					goalName + "'";
		
		String queryResult = app.con.readQuery(SQL);
		//System.out.println(queryResult);
		JSONObject jsonObject = new JSONObject(queryResult);
		JSONArray jArray = jsonObject.getJSONArray("data");
		
		//parse JSON string
    	for(int i=0; i < jArray.length(); i++) {
    		JSONObject j = jArray.getJSONObject(i);
       
        	@SuppressWarnings("unchecked")
			Iterator<String> iter = j.keys();
        	while (iter.hasNext()) {
                String key = iter.next();
                String value = (String)j.get(key);
                if (key.equals(SQLgoalName)) {                	
                	goalNameTV.setText(value);
                }else if (key.equals(SQLcategory)) {                	
                	categoryTV.setText(value);
                }else if (key.equals(SQLstartDate)) {                	
                	startDateTV.setText(value);
                }else if (key.equals(SQLendDate)) {                	
                	endDateTV.setText(value);
                }else if (key.equals(SQLnotes)) {                	
                	notesTV.setText(value);
                }else if (key.equals(SQLcomplete)) {
                	if(value == "1"){
                		completeTV.setText("yes");
                	}else {
                		completeTV.setText("no");
                	}
                }else if (key.equals(SQLtype)) {                	
                	type = value;
                	//String goalType = value;
                }else if (key.equals(SQLsubID)) { 
                	subID = value;
                	//String goalSubID = value;
                }
                
            }
    	}
	}
	
	/**
	 * Auxiliary function to help rendering goal list view.
	 * Uses SpecialAdapter.java
	 * 
	 * @param app
	 * @param result
	 * @param col_header
	 * @param list
	 */
	private static void writeQueryResult(MainActivity app, String result, LinearLayout col_header, ListView list){
		ArrayList<HashMap<String, String>> tableList = new ArrayList<HashMap<String, String>>();
		String col0 = null;
    	try{
    		
    		JSONObject jsonObject = new JSONObject(result);
    		JSONArray jArray = jsonObject.getJSONArray("data");
    		
    		ArrayList<TextView> Columns = new ArrayList<TextView>();
    		//TODO fix exception here from history viewer
    		Log.w("COL HEADER CHILD COUNT", Integer.toString(col_header.getChildCount()));
    		for(int i = 0; i < col_header.getChildCount(); i++)
    		{
    			Columns.add((TextView) col_header.getChildAt(i));
    		}
    		
    		//parse JSON string
        	for(int i=0; i < jArray.length(); i++) {
        		HashMap<String, String> map = new HashMap<String, String>();
            	JSONObject j = jArray.getJSONObject(i);
           
            	@SuppressWarnings("unchecked")
				Iterator<String> iter = j.keys();
            	int col= 0;
                while (iter.hasNext()) {
                    String key = iter.next();
                    String value = (String)j.get(key);
                    
                    //change 0,1 value to v
                    
                    if(key.contains("Completed?") && value.contains("1")){
                    	value = "v";
                    } else if(key.contains("Completed?") && value.contains("0")){
                    	value = " ";
                    }
                    
                    if(key.contains("ID")){
                    	Columns.get(4).setText(key);
                    } else if (key.equals("finished")) {
                    	col0 = key;
                    	value = (String)j.get(key);
                    	if(value.equals("1"))
                    		value = "DONE";
                    	else
                    		value = "";
                    } else {
                    	Columns.get(col).setText(key);
                    	col++;
                    }
                    map.put(key, value);
                }
            	tableList.add(map);
        	}
        	
    		/*SimpleAdapter myAdapter = 
    				new SimpleAdapter(app, */
        	
        	SpecialAdapter myAdapter = 
    				new SpecialAdapter(app, 
    								  tableList, 
    								  R.layout.my_list_item,
    								  new String[] {Columns.get(0).getText().toString(), 
    												Columns.get(1).getText().toString(), 
    												Columns.get(2).getText().toString(), 
    												Columns.get(3).getText().toString(), 
    												Columns.get(4).getText().toString(),
    												col0}, 
    								  new int[] {R.id.cell1, R.id.cell2, R.id.cell3, R.id.cell4, R.id.cell5, R.id.cell0});
    		
    		list.setAdapter(myAdapter);
    		
        	
		} catch (JSONException e) {
			Log.e("JSONException", "Error: " + e.toString());
		}
	}
	
	/**
	 * When clicked "Back" Button, it goes back to the previous screen.
	 * Not yet implemented.
	 * 
	 * @param app
	 */
	public static void goBack(MainActivity app)
	{
		LinearLayout ch = (LinearLayout)app.findViewById(R.id.goal_view_col_header);
		ListView l = (ListView)app.findViewById(R.id.goal_view_listView);
		
		if(!previous_SQL.isEmpty()){
			String SQL = previous_SQL.pop();
			app.con.readQuery(SQL, l, ch);
		} else {
			//needs to be fixed
			/*if(!Workout.isRunning(app.workout)){
				app.workout.goBack();
			} else {
				
			}*/
		}		
	}
	
	

	
	public static String getType() {
		return type;
	}
	public static String getSubID() {
		return subID;
	}
}

