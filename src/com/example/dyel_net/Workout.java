package com.example.dyel_net;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/***********************WORKING OUT METHODS********************************/

public class Workout
{

	/*************PRIVATE MEMBER VARIABLES******************/
	private MainActivity app;
	private Stack<String> previous_SQL = new Stack<String>();
	private String status;
	private boolean running = false;
	private String dayID = null;
	private LinearLayout col_head;
	private String name;
	private ListView listview;
	private TextView topbar;
	private String sessionID;
	
	public Workout(MainActivity a, String dID, String n)
	{
		app = a;
		dayID = dID;
		name = n;
		
		running = true;
		col_head = (LinearLayout)app.findViewById(R.id.working_out_col_header);
		listview = (ListView)app.findViewById(R.id.workingout_listView);
		topbar = (TextView)app.findViewById(R.id.working_out_topbar_text);
	
		sessionID = createRealSession();
	}
	
	public void cancel()
	{
		String SQL = "DELETE FROM _set WHERE sessionID=" + sessionID;
		app.con.writeQuery(SQL);
		
		SQL = "DELETE FROM session WHERE sessionID="+ sessionID;
		app.con.writeQuery(SQL);
		
		running = false;	
	}
	
	public boolean isRunning()
	{
		return running;
	}
	
	public void viewSession()
	{
		app.gotoLayout(R.layout.workingout);
		
		String SQL = "SELECT exercise.name, count(*) As 'Sets' FROM _set " + 
					 " INNER JOIN exercise ON exercise.exerciseID = _set.exerciseID WHERE _set.dayID = " + dayID +
					 " GROUP BY _set.exerciseID";
		
		app.con.readQuery(SQL, listview, col_head);

		topbar.setText(name);
		
		pushBack(SQL);
		status = "session";
		
	}
	
	public void viewExercise(String exercise)
	{
		String SQL = "SELECT setnumber, reps, weight FROM _set " +
					 " INNER JOIN exercise ON exercise.exerciseID = _set.exerciseID " +
					 " WHERE dayID = " + dayID + 
					 " AND exercise.name='" + exercise + "'" +
					 " AND isReal = 0 " +
					 " ORDER BY setnumber ASC";
		
		
		app.con.readQuery(SQL, listview, col_head);
		
		topbar.setText(exercise);
		
		pushBack(SQL);
		status = "exercise";
		
	}
	
	public String createRealSession()
	{
		String sID = null;
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		
		String SQL = "INSERT INTO session(username, datetime, notes, isGoal, isReal) " +
					 "VALUES('" +
					 	app.con.username() + "','" +
					    timeStamp  + "','" +
					 	"0"  + "','" +
					    "1"  + "')";
		
		app.con.writeQuery(SQL);
		
		while(app.con.working()){}
		try { Thread.sleep(500);} 
		catch (InterruptedException e) {e.printStackTrace();}
		
		SQL = "SELECT sessionID FROM session WHERE isReal=1 AND username='" + app.con.username() + "' ORDER BY datetime DESC";
		String jString = app.con.readQuery(SQL);
		
		try {
			JSONObject jsonObject = new JSONObject(jString);
			JSONArray jArray = jsonObject.getJSONArray("data");
			JSONObject j = jArray.getJSONObject(0);
			sID = (String) j.get("sessionID");
		} catch (JSONException e) {e.printStackTrace();}
		
		return sID;
	}
	
	public void editSet(LinearLayout L)
	{
		
	}
	
	public void insertSet()
	{
		
	}
	
	
	
	public String getSQL()
	{
		return previous_SQL.peek();
	}
	
	public String getStatus()
	{
		return status;
	}
	
	//back button functionality for listview query menus
	//sets query to listview as the previous one
	public boolean goBack()
	{
		if(!previous_SQL.isEmpty())
		{
			String SQL = previous_SQL.pop();
			app.con.readQuery(SQL, listview, col_head);
			topbar.setText(name);
			return true;
		}
		else
			return false;
	}
	
	private void pushBack(String SQL)
	{
		previous_SQL.push(SQL);
	}
}
