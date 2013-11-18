package com.example.dyel_net;

import java.util.Stack;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class RoutineView extends Activity {
	
	private MainActivity app;
	private Stack<String> previous_SQL = new Stack<String>();
	private Stack<String> previous_topbar = new Stack<String>();
	private String status;
	private boolean running = false;
	
	private LinearLayout col_head;
	private String routineID;
	private String weekID;
	private String dayID;
	private String setID;
	private ListView listView;
	
	private TextView topbar;
	
	public static boolean isRunning(RoutineView rv)
	{
		if(rv != null) {
			return rv.isRunning();
		} else {
			return false;
		}
	}
	
	public RoutineView(MainActivity a)
	{
		app = a;
		app.gotoLayout(R.layout.routine_view);
		running = true;
		col_head = (LinearLayout)app.findViewById(R.id.routineview_col_header);
		listView = (ListView)app.findViewById(R.id.routineview_listView);
		topbar = (TextView)app.findViewById(R.id.routineview_topbar_text);
	}
	
	public void cancel()
	{
		running = false;
	}
	
	public boolean isRunning()
	{
		return running;
	}
	
	public String getDayID()
	{
		return dayID;
	}
	
	public String getWeekID()
	{
		return weekID;
	}
	
	public String getSetID()
	{
		return setID;
	}
	
	public String getTopbar()
	{
		return topbar.getText().toString();
	}
	
	public void viewRoutines(){
		String query = "SELECT name " +
				" FROM routine" +
				" WHERE username='" + app.con.username() + "'" +
				" ORDER BY lastedited DESC";
		
		app.con.readQuery(query, listView, col_head);
		
		pushBack(query);
		status = "routines";
	}
	
	public void viewWeeks(String routineName){
		
		routineID = getRoutineID(routineName);
		
		String query = "SELECT weekID, week FROM schedule_week" +
				" WHERE routineID=" + routineID;
		
		app.con.readQuery(query, listView, col_head);
		
		pushBack(query);
		status = "weeks";
	}
	
	public void viewDays(){
		String query = "SELECT dayID, day, name FROM schedule_day " +
				" WHERE routineID=" + routineID +
				" AND weekID=" + weekID;
		
		app.con.readQuery(query, listView, col_head);
		
		pushBack(query);
		status = "days";
	}
	
	public void viewSets(String dID, String name){
		
		dayID = dID;
		
		String SQL = "SELECT exercise.name, count(*) As 'Sets' FROM _set " + 
					 " INNER JOIN exercise ON exercise.exerciseID = _set.exerciseID " + 
					 " WHERE _set.dayID = " + dayID + " AND isReal=0 AND isGoal=0 " + 
					 " GROUP BY _set.exerciseID";
		
		app.con.readQuery(SQL, listView, col_head);
		topbar.setText(name);
		
		pushBack(SQL);
		status = "sets";
	}
	
	public void viewExercise(String exercise)
	{
		
		String SQL = "SELECT setnumber, reps, weight FROM _set " +
					 " INNER JOIN exercise ON exercise.exerciseID = _set.exerciseID " +
					 " WHERE dayID = " + dayID + 
					 " AND exercise.name='" + exercise + "'" +
					 " AND isReal = 0 " +
					 " ORDER BY setnumber ASC";
		
		
		app.con.readQuery(SQL, listView, col_head);
		
		topbar.setText(exercise);
		
		pushBack(SQL);
		status = "exercises";
		
	}
	
	public String getRoutineID(String name)
	{
		String ID = null;
		
		String SQL = "SELECT routineID FROM routine WHERE username = '" + app.con.username() + "' AND name='" + name + "'";
		
		String jResult = app.con.readQuery(SQL);
		
		try {
			JSONObject jsonObject = new JSONObject(jResult);
			JSONArray jArray = jsonObject.getJSONArray("data");
			JSONObject j = jArray.getJSONObject(0);
			ID = j.getString("routineID");
		} catch (JSONException e) {e.printStackTrace();}
		
		return ID;
	}
	
	public String getStatus()
	{
		return status;
	}
	
	//back button functionality for listview query menus
	//sets query to listview as the previous one
	public boolean goBack(int pops, ListView l)
	{
		if(pops > 0 && !previous_SQL.isEmpty())
		{
			String SQL = null;
			String tbar_text = null;
			for(int i = 0; i < pops && !previous_SQL.isEmpty(); ++i){
				SQL = previous_SQL.pop();
				tbar_text = previous_topbar.pop();
			}
			
			app.con.readQuery(SQL, l, col_head);

			if(tbar_text != null)
				topbar.setText(tbar_text);
			
			return true;
		}
		else
			return false;
	}
	
	public void pushBack(String SQL)
	{
		previous_SQL.push(SQL);
		previous_topbar.push(topbar.getText().toString());
	}
}
