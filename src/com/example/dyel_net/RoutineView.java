package com.example.dyel_net;

import java.util.Stack;

import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class RoutineView extends Activity {
	
	private MainActivity app;
	private Stack<String> previous_SQL = new Stack<String>();
	private String status;
	private boolean running = false;
	
	public RoutineView(MainActivity a)
	{
		app = a;
		running = true;
	}
	
	public void cancel()
	{
		running = false;
	}
	
	public boolean isRunning()
	{
		return running;
	}
	
	public void viewRoutines(){
		String query = "SELECT routineID, type, name " +
				" FROM routine" +
				" WHERE username=" + app.con.username();
		
		ListView l = (ListView)app.findViewById(R.id.routineview_listView);
		LinearLayout ch = (LinearLayout)app.findViewById(routineview_col_header);
		app.con.readQuery(query, l, ch);
		
		pushBack(query);
		status = "routines";
	}
	
	public void viewWeeks(String routineID){
		String query = "SELECT week FROM schedule_week" +
				" WHERE routineID=" + routineID;
		
		ListView l = (ListView)app.findViewById(R.id.routineview_listView);
		LinearLayout ch = (LinearLayout)app.findViewById(routineview_col_header);
		app.con.readQuery(query, l, ch);
		
		pushBack(query);
		status = "weeks";
	}
	
	public void viewDays(String routineID, String weekID){
		String query = "SELECT * FROM schedule_day " +
				" WHERE routineID=" + routineID +
				" AND weekID=" + weekID;
		
		ListView l = (ListView)app.findViewById(R.id.routineview_listView);
		LinearLayout ch = (LinearLayout)app.findViewById(routineview_col_header);
		app.con.readQuery(query, l, ch);
		
		pushBack(query);
		status = "days";
	}
	
	public void viewSets(String dayID){
		String query = "SELECT * FROM _set " +
				"WHERE dayID=" + dayID;
		
		ListView l = (ListView)app.findViewById(R.id.routineview_listView);
		LinearLayout ch = (LinearLayout)app.findViewById(routineview_col_header);
		app.con.readQuery(query, l, ch);
		
		pushBack(query);
		status = "sets";
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
			for(int i = 0; i < pops && !previous_SQL.isEmpty(); ++i)
				SQL = previous_SQL.pop();
			
			app.con.readQuery(SQL, l);
			
			return true;
		}
		else
			return false;
	}
	
	public void pushBack(String SQL)
	{
		previous_SQL.push(SQL);
	}
}
