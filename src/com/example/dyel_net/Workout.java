package com.example.dyel_net;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.locks.Lock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/***********************WORKING OUT METHODS********************************/

public class Workout
{

	/*************PRIVATE MEMBER VARIABLES******************/
	private MainActivity app;
	private Stack<String> previous_SQL = new Stack<String>();
	private Stack<String> previous_topbar = new Stack<String>();
	private String status;
	private boolean running = false;
	private String dayID = null;
	private LinearLayout col_head;
	private String name;
	private ListView listview;
	private TextView topbar;
	private String sessionID;
	private editSet new_set;
	private String current_exercise;
	
	private ArrayList< ArrayList<Boolean> > completed = new ArrayList< ArrayList<Boolean> >();
	private ArrayList<Boolean> exCompleted;
	
	public static boolean isRunning(Workout w)
	{
		if(w == null) {
			return false;
		} else {
			return w.isRunning();
		}
	}
	
	public Workout(MainActivity a, String dID, String n)
	{
		app = a;
		dayID = dID;
		name = n;
		
		app.gotoLayout(R.layout.workingout);
		
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
	
	public void finish()
	{
		running = false;
	}
	
	public boolean isRunning()
	{
		return running;
	}
	
	public void viewSession()
	{	
		app.gotoLayout(R.layout.workingout);
		col_head = (LinearLayout)app.findViewById(R.id.working_out_col_header);
		listview = (ListView)app.findViewById(R.id.workingout_listView);
		topbar = (TextView)app.findViewById(R.id.working_out_topbar_text);
		
		String SQL = "SELECT exercise.name, count(*) As 'Sets' FROM _set " + 
					 " INNER JOIN exercise ON exercise.exerciseID = _set.exerciseID " + 
					 " WHERE _set.dayID = " + dayID + " AND isReal=0 AND isGoal=0 " + 
					 " GROUP BY _set.exerciseID";
		
		app.con.readQuery(SQL, listview, col_head);

		topbar.setText(name);
		
		pushBack(SQL);
		status = "session";
		
		if(completed.isEmpty()){
			for(int i = 0; i < listview.getChildCount(); i++)
				completed.add(new ArrayList<Boolean>());
		} else {
			for(int i = 0; i < listview.getChildCount(); i++)
			{
				boolean done = true;
				Iterator<Boolean> it = completed.get(i).iterator();
				while(it.hasNext()){
					if(it.equals(Boolean.FALSE))
						done = false;
				}
				if(done){
					LinearLayout curLL = (LinearLayout)listview.getChildAt(i);
					curLL.setBackgroundColor(0xFF4BFA37);
				}
			}
		}
	}
	
	public void viewExercise(String exercise, int index)
	{
		current_exercise = exercise;
		
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
		
		
		exCompleted = completed.get(index);
		Iterator<Boolean> it = exCompleted.iterator();
		int c = 0;
		while(it.hasNext()){
			if(it.equals(Boolean.TRUE)){
				LinearLayout curLL = (LinearLayout)listview.getChildAt(c);
				curLL.setBackgroundColor(0xFF4BFA37);
			}
			++c;
		}
		
	}
	
	public String createRealSession()
	{
		String sID = null;
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		
		String SQL = "INSERT INTO session(username, datetime, isGoal, isReal, dayID) " +
					 "VALUES('" +
					 	app.con.username() + "','" +
					    timeStamp  + "'," +
					 	"0"  + "," +
					    "1"  + "," + 
					 	dayID + ")";
		
		app.con.writeQuery(SQL);
		
		//while(app.con.working()){}
		//try { Thread.sleep(500);} 
		//catch (InterruptedException e) {e.printStackTrace();}
		
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
	
	public void addRealSet(String setnumber)
	{
		app.gotoLayout(R.layout.edit_set);
		new_set = new editSet(setnumber);
		new_set.open_editSet();
	}
	
	public void insertRealSet()
	{
		new_set.update_editSet();
	}
	
	public void viewHistory()
	{	
		if(status == "exercise"){
			HistoryViewer.viewHistory(current_exercise, dayID, app);
		}
	}
	
	private class editSet
	{
		private String exerciseID = null;
		private String setnumber = null;
		private String setID = null;
		private int list_index;
		
		public editSet(String setnum)
		{
			setnumber = setnum;
			exerciseID = get_exerciseID();
			setID = get_setID();
			list_index = Integer.parseInt(setnum) - 1;
		}
		
		private String get_exerciseID()
		{
			String ret = null;
			
			String SQL = "SELECT exerciseID FROM exercise WHERE name='" + topbar.getText().toString() + "'";
			
			String jString = app.con.readQuery(SQL);
			
			try {
				JSONObject jsonObject = new JSONObject(jString);
				JSONArray jArray = jsonObject.getJSONArray("data");
				JSONObject j = jArray.getJSONObject(0);
				ret = (String) j.get("exerciseID");
			} catch (JSONException e) {e.printStackTrace();}
			
			return ret;
		}
		
		private String get_setID()
		{
			String ret = null;
			
			String SQL = "SELECT setID FROM _set WHERE isReal=0 AND isGoal=0 " + 
						 " AND dayID=" + dayID + " AND setnumber=" + setnumber + " AND exerciseID=" + exerciseID;
			
			String jString = app.con.readQuery(SQL);
			
			try {
				JSONObject jsonObject = new JSONObject(jString);
				JSONArray jArray = jsonObject.getJSONArray("data");
				JSONObject j = jArray.getJSONObject(0);
				ret = (String) j.get("setID");
			} catch (JSONException e) {e.printStackTrace();}
			
			return ret;
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
			
			String SQL = "SELECT exercise.name, reps, weight, " + 
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
				ET1.setHint(j.get("reps").toString());
				ET2.setHint(j.get("weight").toString());
				
			} catch (JSONException e) {e.printStackTrace();}
		}
		
		public void update_editSet()
		{
			EditText ET1 = (EditText)app.findViewById(R.id.editset_et1);
			EditText ET2 = (EditText)app.findViewById(R.id.editset_et2);
			EditText notes = (EditText)app.findViewById(R.id.editset_notes);
			
			String reps, weight;
			
			if(exerciseID != null)
			{
				
				reps = ET1.getText().toString();
				weight = ET2.getText().toString();
				
				String note;
				if(notes.getText().toString().length() < 1)
					note = "NULL";
				else
					note = notes.getText().toString();
				
				String SQL = "INSERT INTO _set(sessionID, dayID, exerciseID, reps, weight, setnumber, " +
							 				  " notes, isReal, isGoal) " +
							 "VALUES(" + sessionID + "," +
							 		   	 dayID + "," + 
							 		   	 exerciseID + "," + 
							 		   	 reps + "," +
							 		     weight + "," + 
							 		   	 setnumber + "," + 
							 		     note + "," +
							 		   	 "1,0)";
							 				  	
				app.con.writeQuery(SQL);
				
				exCompleted.set(list_index, Boolean.TRUE);
				
			} else {
				return;
			}
		}
	
	}
	
	
	
	public void addSet()
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
	

	//TODO make it do two pops on first back use
	
	//back button functionality for listview query menus
	//sets query to listview as the previous one
	public boolean goBack()
	{
		Log.w("WORKOUT", "GO BACK PRE");
		if(!previous_SQL.isEmpty())
		{
			col_head = (LinearLayout)app.findViewById(R.id.working_out_col_header);
			listview = (ListView)app.findViewById(R.id.workingout_listView);
			topbar = (TextView)app.findViewById(R.id.working_out_topbar_text);
			
			Log.w("WORKOUT", "GO BACK");
			String SQL = previous_SQL.pop();
			app.con.readQuery(SQL, listview, col_head);
			Log.w("PREVIOUS TOP BAR", previous_topbar.peek());
			topbar.setText(previous_topbar.pop());
			
			if(previous_SQL.isEmpty()){
				status = "session";
			} else {
				status = "exercise";
			}
			
			return true;
		}
		else
			return false;
	}
	

	private void pushBack(String SQL)
	{
		previous_SQL.push(SQL);
		previous_topbar.push(topbar.getText().toString());
	}
}
