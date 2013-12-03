package com.example.dyel_net;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.widget.EditText;
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
	
	private editSet new_set;
	
	private TextView topbar;
	private String day_name = "ERROR";
	private String current_SQL = null;
	
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
	
	public String getRoutineID()
	{
		return routineID;
	}
	
	public String getDayName()
	{
		if(status == "sets" || status == "exercises"){
			return day_name;
		} else {
			return "ERROR";
		}
	}
	
	public String getTopbar()
	{
		return topbar.getText().toString();
	}
	
	public void viewRoutines(){
		String query = "SELECT routineID, name " +
				" FROM routine" +
				" WHERE username='" + app.con.username() + "'" +
				" ORDER BY lastedited DESC";
		
		app.con.readQuery(query, listView, col_head);
		
		pushBack(query);
		status = "routines";
	}
	
	public void viewWeeks(String routineName, String _routineID){
		
		routineID = _routineID;
		
		String query = "SELECT weekID, week, min(C.finished1) as finished FROM ( ( " +
					   "SELECT schedule_week.weekID, week, min(_set.finished) as finished1 FROM schedule_week " + 
					   "LEFT JOIN schedule_day on schedule_day.weekID=schedule_week.weekID  " +
					   "LEFT JOIN _set on _set.dayID=schedule_day.dayID  " +
					   "WHERE schedule_week.routineID=" + routineID + " ) " +
					   "UNION ( " +
					   "SELECT schedule_week.weekID, week, 1 as finished1 FROM schedule_week " +  
					   "WHERE schedule_week.routineID=" + routineID + ")) As C " +
					   "GROUP BY weekID";
		
		app.con.readQuery(query, listView, col_head);
		
		pushBack(query);
		status = "weeks";
	}
	
	public void viewDays(String _weekID){
		weekID = _weekID;
		
		String query = "SELECT dayID, day, name, min(finished1) as finished FROM ( " +
					   "(SELECT schedule_day.dayID, day, name, min(_set.finished) as finished1 FROM schedule_day " +
					   "INNER JOIN _set on _set.dayID=schedule_day.dayID " +
					   "WHERE routineID=" + routineID + " AND weekID=" + weekID + " AND isReal=0" +
					   "GROUP BY schedule_day.dayID " +
					   ") UNION " +
					   "(SELECT schedule_day.dayID, day, name, 1 as finished1 FROM schedule_day " +
					   "WHERE routineID=" + routineID + " AND weekID=" + weekID + 
					   ")) As A GROUP BY dayID";
		
		app.con.readQuery(query, listView, col_head);
		
		pushBack(query);
		status = "days";
	}
	
	public void viewExercises(String dID, String name){
		
		dayID = dID;
		
		String SQL = " SELECT exercise.name, count(*) As 'Sets', _set.exerciseID FROM _set " + 
					 " INNER JOIN exercise ON exercise.exerciseID = _set.exerciseID " + 
					 " WHERE _set.dayID = " + dayID + " AND isReal=0 AND isGoal=0 " + 
					 " GROUP BY _set.exerciseID";
		
		app.con.readQuery(SQL, listView, col_head);
		topbar.setText(name);
		
		day_name = name;
		
		pushBack(SQL);
		status = "exercises";
	}
	
	public void viewSets(String exercise)
	{
		
		String SQL = "SELECT setnumber, reps, weight, finished FROM _set " +
					 " INNER JOIN exercise ON exercise.exerciseID = _set.exerciseID " +
					 " WHERE dayID = " + dayID + 
					 " AND exercise.name='" + exercise + "'" +
					 " AND isReal = 0 " +
					 " ORDER BY setnumber ASC";
		
		
		app.con.readQuery(SQL, listView, col_head);
		
		topbar.setText(exercise);
		
		pushBack(SQL);
		status = "sets";
		
	}
	
	public void openAddNewSet(String...strings)
	{
		app.gotoLayout(R.layout.add_set);
		
		if(strings.length > 0){
			String exercise = strings[0];
			String exerciseID = strings[1];
			TextView exerciseTV = (TextView)app.findViewById(R.id.addset_exercise_name);
			TextView exerciseID_TV = (TextView)app.findViewById(R.id.addset_exercise_id);
			exerciseTV.setText(exercise);
			exerciseID_TV.setText(exerciseID);
			TextView setnumTV = (TextView)app.findViewById(R.id.addset_setnumber);
			setnumTV.setText(getSetNumber(exercise));				
		}
	}
	
	private String getSetNumber(String exercise)
	{		
		String setnum = null;
		String SQL = "SELECT count(*) As COUNT FROM _set INNER JOIN exercise ON exercise.exerciseID = _set.exerciseID " + 
					 "WHERE _set.dayID = " + dayID + " AND exercise.name='" + exercise + "' AND isReal=0 AND isGoal=0";
		
		String jString = app.con.readQuery(SQL);
		
		try {
			JSONObject jsonObject = new JSONObject(jString);
			JSONArray jArray = jsonObject.getJSONArray("data");
			JSONObject j = jArray.getJSONObject(0);
			setnum = j.get("COUNT").toString();
		} catch (JSONException e) {e.printStackTrace();}
		
		Integer int_setnum = Integer.parseInt(setnum) + 1;
		
		return int_setnum.toString();
	}
	
	public void addSet()
	{
		TextView exerciseID_TV = (TextView)app.findViewById(R.id.addset_exercise_id);
		TextView setnumTV = (TextView)app.findViewById(R.id.addset_setnumber);
		TextView repsTV = (TextView)app.findViewById(R.id.addset_reps);
		TextView weightTV = (TextView)app.findViewById(R.id.addset_weight);
		
		String exerciseID = exerciseID_TV.getText().toString();
		String setnum = setnumTV.getText().toString();
		String reps = repsTV.getText().toString();
		String weight = weightTV.getText().toString();
		
		if(exerciseID == "" || setnum == "" || reps == "" || weight == ""){
			app.showDialog("Missing Fields");
		} else {
			String SQL = "INSERT INTO _set(dayID, exerciseID, reps, weight, setnumber, isReal, isGoal) " +
	 				  	 "VALUES(" + dayID + "," + 
	 				  			  exerciseID + "," + 
	 				  			  reps + "," +
	 				  			  weight + "," + 
	 				  			  setnum + "," + 
	 				  			  "0,0)";
			
			app.con.writeQuery(SQL);
		}
	}
	
	public void viewHistory()
	{	
		if(status == "exercise"){
			String current_exercise = topbar.getText().toString();
			HistoryViewer.viewHistory(current_exercise, dayID, app);
		}
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
	
	public String getSQL()
	{
		return previous_SQL.peek();
	}
	
	//back button functionality for listview query menus
	//sets query to listview as the previous one
	public boolean goBack()
	{
		int pops = 1;
		
		if(pops > 0 && !previous_SQL.isEmpty())
		{
			if(current_SQL.equals((String)previous_SQL.peek()))
				pops = 2;
			
			String SQL = null;
			String tbar_text = null;
			for(int i = 0; i < pops && !previous_SQL.isEmpty(); ++i){
				SQL = previous_SQL.pop();
				tbar_text = previous_topbar.pop();
			}
			
			app.con.readQuery(SQL, listView, col_head);
			
			if(tbar_text != null)
				topbar.setText(tbar_text);
			
			app.current_layout = R.layout.routine_view;
			
			return true;
		} else {
			app.setContentView(R.layout.main_menu);
			return false;
		}
	}
	
	public void pushBack(String SQL)
	{
		previous_SQL.push(SQL);
		current_SQL = SQL;
		previous_topbar.push(topbar.getText().toString());
	}
	
	public void updateSet(String setnumber, String _setID)
	{
		setID = _setID;
		app.gotoLayout(R.layout.edit_set);
		new_set = new editSet(setnumber, _setID);
		new_set.open_editSet();
	}
	
	public void submitUpdateSet()
	{
		new_set.update_editSet();
	}
	
	private class editSet
	{
		private String setnumber = null;
		private String setID = null;
		private int list_index;
		
		public editSet(String setnum, String sID)
		{
			setnumber = setnum;
			setID = sID;
			list_index = Integer.parseInt(setnum) - 1;
		}
		
		
		public void open_editSet()
		{
			TextView exercise = (TextView)app.findViewById(R.id.editset_exercise);
			TextView setnumTV = (TextView)app.findViewById(R.id.editset_setnumber);
			EditText ET1 = (EditText)app.findViewById(R.id.editset_et1);
			EditText ET2 = (EditText)app.findViewById(R.id.editset_et2);
			TextView TV1 = (TextView)app.findViewById(R.id.editset_tv1);
			TextView TV2 = (TextView)app.findViewById(R.id.editset_tv2);
			
			setnumTV.setText(setnumber);
			
			String SQL = "SELECT exercise.name, reps, weight " + 
						 "FROM _set INNER JOIN exercise ON _set.exerciseID = exercise.exerciseID " +
						 "WHERE setID=" + setID;
			
			String jString = app.con.readQuery(SQL);
			
			try {
				JSONObject jsonObject = new JSONObject(jString);
				JSONArray jArray = jsonObject.getJSONArray("data");
				JSONObject j = jArray.getJSONObject(0);
						
				exercise.setText(j.get("name").toString());
				
				TV1.setText("Reps");
				TV2.setText("Weight");
				ET1.setText(j.get("reps").toString());
				ET2.setText(j.get("weight").toString());
				
			} catch (JSONException e) {e.printStackTrace();}
		}
		
		public void update_editSet()
		{
			EditText ET1 = (EditText)app.findViewById(R.id.editset_et1);
			EditText ET2 = (EditText)app.findViewById(R.id.editset_et2);
			EditText notes = (EditText)app.findViewById(R.id.editset_notes);
			
			String reps, weight;
			
				
				reps = ET1.getText().toString();
				weight = ET2.getText().toString();
				
				String note;
				if(notes.getText().toString().length() < 1)
					note = "NULL";
				else
					note = notes.getText().toString();
				
				String SQL = "UPDATE _set SET " +
				 		   	 "reps=" + reps + "," +
				 		     "weight=" + weight + "," + 
				 		     "notes='" + note + "'" +
				 		     "WHERE setID=" + setID;
					 				  	
				app.con.writeQuery(SQL);
				
		}
	
	}

	public void deleteSet() {
		String SQL = "DELETE _set WHERE setID=" + setID;		  	
		app.con.writeQuery(SQL);
	}
	
}
