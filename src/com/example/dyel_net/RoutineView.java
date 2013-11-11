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
	private String status;
	private boolean running = false;
	
	private LinearLayout col_head;
	private String routineID;
	private String weekID;
	private String dayID;
	private String setID;
	private ListView listView;
	
	public RoutineView(MainActivity a)
	{
		app = a;
		running = true;
		col_head = (LinearLayout)app.findViewById(R.id.routineview_col_header);
		listView = (ListView)app.findViewById(R.id.routineview_listView);
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
		String query = "SELECT type, name " +
				" FROM routine" +
				" WHERE username=" + app.con.username();
		
		app.con.readQuery(query, listView, col_head);
		
		try {
			TimeUnit.SECONDS.sleep(2);
			String JSONstring = app.con.readQuery("SELECT routineID FROM routine WHERE username=" + app.con.username() + " ORDER BY routineID DESC");
			JSONObject jsonObject = new JSONObject(JSONstring);
			JSONArray jArray = jsonObject.getJSONArray("data");
			JSONObject j = jArray.getJSONObject(0);
			routineID = j.getString("routineID");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		pushBack(query);
		status = "routines";
	}
	
	public void viewWeeks(){
		String query = "SELECT week FROM schedule_week" +
				" WHERE routineID=" + routineID;
		
		app.con.readQuery(query, listView, col_head);
		
		try {
			TimeUnit.SECONDS.sleep(2);
			String JSONstring = app.con.readQuery("SELECT weekID FROM schedule_week WHERE routineID=" + routineID + " ORDER BY weekID DESC");
			JSONObject jsonObject = new JSONObject(JSONstring);
			JSONArray jArray = jsonObject.getJSONArray("data");
			JSONObject j = jArray.getJSONObject(0);
			weekID = j.getString("weekID");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		pushBack(query);
		status = "weeks";
	}
	
	public void viewDays(){
		String query = "SELECT * FROM schedule_day " +
				" WHERE routineID=" + routineID +
				" AND weekID=" + weekID;
		
		app.con.readQuery(query, listView, col_head);
		
		try {
			TimeUnit.SECONDS.sleep(2);
			String JSONstring = app.con.readQuery("SELECT dayID FROM schedule_day WHERE routineID=" + routineID + " AND weekID=" + weekID + " ORDER BY dayID DESC");
			JSONObject jsonObject = new JSONObject(JSONstring);
			JSONArray jArray = jsonObject.getJSONArray("data");
			JSONObject j = jArray.getJSONObject(0);
			dayID = j.getString("dayID");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		pushBack(query);
		status = "days";
	}
	
	public void viewSets(){
		String query = "SELECT * FROM _set " +
				"WHERE dayID=" + dayID;
		
		app.con.readQuery(query, listView, col_head);
		
		try {
			TimeUnit.SECONDS.sleep(2);
			String JSONstring = app.con.readQuery("SELECT setID FROM _set WHERE dayID=" + dayID + " ORDER BY setID DESC");
			JSONObject jsonObject = new JSONObject(JSONstring);
			JSONArray jArray = jsonObject.getJSONArray("data");
			JSONObject j = jArray.getJSONObject(0);
			setID = j.getString("setID");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
			
			app.con.readQuery(SQL, l, col_head);
			
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
