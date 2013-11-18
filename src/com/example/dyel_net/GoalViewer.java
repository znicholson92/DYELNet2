package com.example.dyel_net;

import java.util.Stack;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


public class GoalViewer {
	
	private static Stack<String> previous_SQL = new Stack<String>();
	private static Stack<String> previous_topbar = new Stack<String>();
	
	public static int status = 0;
	public static String currentDayID = null;
	public static String currentExercise = null;
	
	public static void viewDetail(MainActivity app, String userID)
	{
		status = 1;
		
		app.gotoLayout(R.layout.goal_view);
		LinearLayout goal_view_col_head = (LinearLayout)app.findViewById(R.id.goal_view_col_header);
		ListView goal_view_listview = (ListView)app.findViewById(R.id.goal_view_listView);
		TextView goal_view_topbar = (TextView)app.findViewById(R.id.goal_view_topbar_text);
		
		String SQL = "select name as 'Goal Name' , start_date as 'Date(Start)' , goal_date as 'Date(Last)' from goals where username = 'testuser'";
					 
		app.con.readQuery(SQL, goal_view_listview, goal_view_col_head);		
		goal_view_topbar.setText("Suna Debugging");		
		
		previous_SQL.push(SQL);
	}
	
	//when it is double clicked for details
/*	public static void viewHistory_session(String datetime, String exercise, String dayID, MainActivity app)
	{
		status = 2;
		
		LinearLayout history_col_head = (LinearLayout)app.findViewById(R.id.working_out_col_header);
		ListView history_listview = (ListView)app.findViewById(R.id.workingout_listView);
		TextView history_topbar = (TextView)app.findViewById(R.id.working_out_topbar_text);
		
		String SQL = "  SELECT _set.setnumber as 'Set', _set.reps As Reps, _set.weight As Weight FROM _set, exercise, session " +
				 	 "  WHERE _set.exerciseID = exercise.exerciseID " +
				 	 "  AND session.sessionID = _set.sessionID " + 
		 	 		 "  AND exercise.name = '" + exercise + 
				 	 "' AND _set.isReal=1 " + 
		 	 		 "  AND session.dayID=" + dayID +
		 	 		 "  AND session.datetime='" + datetime +
				 	 "' AND session.username='" + app.con.username() + "'";

		
		app.con.readQuery(SQL, history_listview, history_col_head);
		
	}*/
	
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

}
