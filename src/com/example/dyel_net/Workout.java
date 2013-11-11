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
	private String dayID = null;
	private LinearLayout col_head;
	private String name;
	private ListView listview;
	private TextView topbar;
	
	public Workout(MainActivity a, String dID, String n)
	{
		app = a;
		running = true;
		col_head = (LinearLayout)app.findViewById(R.id.working_out_col_header);
		dayID = dID;
		name = n;
		listview = (ListView)app.findViewById(R.id.workingout_listView);
		topbar = (TextView)app.findViewById(R.id.working_out_topbar_text);
	}
	
	public void cancel()
	{
		running = false;
	}
	
	public boolean isRunning()
	{
		return running;
	}
	
	private void viewSession()
	{
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
			String SQL = null;
			SQL = previous_SQL.pop();
			
			app.con.readQuery(SQL, listview, col_head);
			
			topbar.setText(name);
			
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
