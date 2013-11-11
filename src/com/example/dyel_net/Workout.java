package com.example.dyel_net;

import java.util.Stack;

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
	
	public String dayID = null;
	
	private LinearLayout col_head;
	
	public Workout(MainActivity a)
	{
		app = a;
		running = true;
		col_head = (LinearLayout)app.findViewById(R.id.working_out_col_header);
	}
	
	public void cancel()
	{
		running = false;
	}
	
	public boolean isRunning()
	{
		return running;
	}
	
	public void viewSession(String name)
	{
		String SQL = "SELECT exercise.name, count(*) As 'Sets' FROM _set " + 
					 " INNER JOIN exercise ON exercise.exerciseID = _set.exerciseID WHERE _set.dayID = " + dayID +
					 " GROUP BY _set.exerciseID";
		
		ListView l = (ListView)app.findViewById(R.id.workingout_listView);
		app.con.readQuery(SQL, l, col_head);
		
		TextView t = (TextView)app.findViewById(R.id.working_out_topbar_text);
		t.setText(name);
		
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
		
		ListView l = (ListView)app.findViewById(R.id.workingout_listView);
		app.con.readQuery(SQL, l, col_head);
		
		TextView t = (TextView)app.findViewById(R.id.working_out_topbar_text);
		t.setText(exercise);
		
		pushBack(SQL);
		status = "exercise";
		
	}
	
	public void editSet(LinearLayout L)
	{
		
	}
	
	public void insertSet()
	{
		
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
