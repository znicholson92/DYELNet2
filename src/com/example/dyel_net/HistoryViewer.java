package com.example.dyel_net;

import java.util.Stack;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


//TODO terrible design, make methods non-static
public class HistoryViewer {
	
	private static Stack<String> previous_SQL = new Stack<String>();
	private static Stack<String> previous_topbar = new Stack<String>();
	
	public static void viewHistory(String exercise, String dayID, MainActivity app)
	{
		app.gotoLayout(R.layout.workout_history);
		LinearLayout history_col_head = (LinearLayout)app.findViewById(R.id.workout_history_col_header);
		ListView history_listview = (ListView)app.findViewById(R.id.workout_history_listView);
		TextView history_topbar = (TextView)app.findViewById(R.id.workout_history_topbar_text);
		
		String SQL = "SELECT routine.name As 'Routine', session.datetime As 'Date and Time' " + 
					 "FROM _set, exercise, session, routine, schedule_day " +
					 "WHERE _set.exerciseID = exercise.exerciseID " +
					 "AND session.sessionID = _set.sessionID " +
					 "AND routine.routineID = schedule_day.routineID " +
					 "AND schedule_day.dayID = _set.dayID " +
					 "AND exercise.name = '" + exercise + 
					 "' AND _set.isReal=1  AND session.username='" + app.con.username() + "'";
		
		if(dayID != null)
			SQL += " AND _set.dayID=" + dayID;
		
		SQL +=	" GROUP BY session.datetime";
		
		app.con.readQuery(SQL, history_listview, history_col_head);
		
		history_topbar.setText(exercise);
		
		previous_SQL.push(SQL);
	}
	
	public static void viewHistory_session(String datetime, String exercise, String dayID, MainActivity app)
	{

		LinearLayout history_col_head = (LinearLayout)app.findViewById(R.id.working_out_col_header);
		ListView history_listview = (ListView)app.findViewById(R.id.workingout_listView);
		TextView history_topbar = (TextView)app.findViewById(R.id.working_out_topbar_text);
		
		String SQL = "  SELECT _set.setnumber as 'Set', _set.reps As Reps, _set.weight As Weight FROM _set, exercise, session " +
				 	 "  WHERE _set.exerciseID = exercise.exerciseID " +
				 	 "  AND session.sessionID = _set.sessionID " + 
		 	 		 "  AND exercise.name = '" + exercise + 
				 	 "' AND _set.isReal=1 " + 
		 	 		 "' AND session.dayID=" + dayID +
		 	 		 "  AND session.datetime=" + datetime +
				 	 "  AND session.username='" + app.con.username() + "'";

		
		app.con.readQuery(SQL, history_listview, history_col_head);
		
	}
	
	public static void goBack(MainActivity app)
	{
		LinearLayout ch = (LinearLayout)app.findViewById(R.id.working_out_col_header);
		ListView l = (ListView)app.findViewById(R.id.workingout_listView);
		
		if(!previous_SQL.isEmpty()){
			String SQL = previous_SQL.pop();
			app.con.readQuery(SQL, l, ch);
		} else {
			if(!Workout.isRunning(app.workout)){
				app.workout.goBack();
			} else {
				
			}
			
		}
		
	}

}
