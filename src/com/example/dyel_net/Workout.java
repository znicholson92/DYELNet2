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

import android.os.AsyncTask;
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
	private String workout_name;
	private ListView listview;
	private TextView topbar;
	private String sessionID;
	private editSet new_set;
	private String current_exercise;
	private String current_SQL;
	private String current_exerciseID = null;
	
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
		workout_name = n;
		
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
		
		//display each distinct exercise with the number of sets of each
		String SQL = " SELECT exercise.name As 'Name', count(*) As 'Sets', exerciseID FROM _set " + 
					 " INNER JOIN exercise ON exercise.exerciseID = _set.exerciseID " + 
					 " WHERE _set.dayID = " + dayID + " AND isReal=0 AND isGoal=0 " + 
					 " GROUP BY _set.exerciseID";
		
		app.con.readQuery(SQL, listview, col_head);

		//set topbar to the current workout name
		topbar.setText(workout_name);
		
		//sets the status variable to session because we are viewing a session
		status = "session";
		
	    //push current state's info the  previous_SQL stack for back functionality
		pushBack(SQL); 
		
		//start asynchronous task that checks if each exercise was completed
		//initializes the needed data structures if not initialized yet
		completedHandler task = new completedHandler();
		task.execute();

	}
	
	//asynchronous task that checks if each exercise was completed
	//initializes the needed data structures if not initialized yet
	private class completedHandler extends AsyncTask<String, Void, Boolean>
	{

		//background task waits until the listview is set with children
		//waits until child count is nonzero and then delays another second because
		//all children are not added instantly
		@Override
		protected Boolean doInBackground(String... arg0) {
			while(listview.getChildCount() == 0) {}
			try {Thread.sleep(1000);} 
			catch (InterruptedException e) {e.printStackTrace();}
			return true;
		}
		
		//initialize the 'completed' data structure is uninitialized
		//otherwise check if each exercise was completed
		@Override
		protected void onPostExecute(Boolean result){
			if(completed.isEmpty()){
				for(int i = 0; i < listview.getChildCount(); i++){
					LinearLayout tempLL = (LinearLayout)listview.getChildAt(i);
					TextView tempTV = (TextView)tempLL.getChildAt(0);
					int setcount = Integer.parseInt(tempTV.getText().toString());
					completed.add(new ArrayList<Boolean>(setcount));
					for(int j=0; j < setcount; j++)
						completed.get(i).add(j, false);
				}
			} else {
				for(int i = 0; i < listview.getChildCount(); i++)
				{//TODO figure out why this is not working
					boolean done = true;
					ArrayList<Boolean> curExCompleted = completed.get(i);
					for(int j=0; j < curExCompleted.size(); j++){
						if(!curExCompleted.get(j)){
							done = false;
							break;
						}
					}
					if(done){
						LinearLayout curLL = (LinearLayout)listview.getChildAt(i);
						curLL.setBackgroundColor(0xFF4BFA37);
					}
				}
			}
		}
	}
	
	public void viewExercise(String exercise_name, String exerciseID, int index)
	{
		current_exercise = exercise_name;
		current_exerciseID = exerciseID;
		
		String SQL = " SELECT setnumber, reps, weight, setID, finished FROM _set " +
					 " INNER JOIN exercise ON exercise.exerciseID = _set.exerciseID " +
					 " WHERE dayID = " + dayID + 
					 " AND exercise.name='" + exercise_name + "'" +
					 " AND isReal = 0 " +
					 " ORDER BY setnumber ASC";
		
		
		app.con.readQuery(SQL, listview, col_head);
		
		topbar.setText(exercise_name);
		
		pushBack(SQL);
		status = "exercise";
		
		exCompleted = completed.get(index);
		
		exCompletedHandler task = new exCompletedHandler();
		task.execute();
		
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
	
	public void addRealSet(String setnumber, String setID)
	{
		app.gotoLayout(R.layout.edit_set);
		new_set = new editSet(setnumber, current_exerciseID, setID);
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
		
		public editSet(String setnum, String eID, String sID)
		{
			setnumber = setnum;
			exerciseID = eID;
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
				Thread trd = new Thread(new Runnable(){
	    			@Override
	    			public void run(){
	    				connection con2 = new connection(app);
	    				con2.writeQuery("UPDATE _set SET finished = 1 WHERE setID ='" + setID + "'");
	    			}
	    		});
	    		trd.start();
				
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
				
				exCompleted.add(list_index, Boolean.TRUE);
				
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
		if(!previous_SQL.isEmpty())
		{
			app.setContentView(R.layout.workingout);
			app.current_layout = R.layout.workingout;
			
			col_head = (LinearLayout)app.findViewById(R.id.working_out_col_header);
			listview = (ListView)app.findViewById(R.id.workingout_listView);
			topbar = (TextView)app.findViewById(R.id.working_out_topbar_text);
	
			String SQL;
			if(app.current_layout == R.layout.edit_set || app.current_layout == R.layout.add_set){
				SQL = previous_SQL.pop();
				app.con.readQuery(SQL, listview, col_head);
				topbar.setText(previous_topbar.pop());
				status = "exercise";
				exCompletedHandler task = new exCompletedHandler();
				task.execute();
			} else {
				SQL = previous_SQL.pop();
				app.con.readQuery(SQL, listview, col_head);
				topbar.setText(previous_topbar.pop());
				status = "session";
				app.current_layout = R.layout.workingout;
			}
			
			return true;
		}
		else
			return false;
	}
	
	private class exCompletedHandler extends AsyncTask<Void, Void, Boolean>
	{

		@Override
		protected Boolean doInBackground(Void... arg0) {
			while(listview.getChildCount() == 0){}
			try {Thread.sleep(1000);} 
			catch (InterruptedException e) {e.printStackTrace();}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result){
			Log.w("exCompleted", Integer.toString(exCompleted.size()));
			for(int i=0; i < exCompleted.size(); i++){Log.w("COMPLETED", "LOOPING");
				if(exCompleted.get(i).equals(Boolean.TRUE)){
					LinearLayout curLL = (LinearLayout)listview.getChildAt(i);
					curLL.setBackgroundColor(0xFF4BFA37);Log.w("COMPLETED", "SET BACKGROUND");
				}
			}
		}
	}

	private void pushBack(String SQL)
	{
		previous_SQL.push(SQL);
		current_SQL = SQL;
		previous_topbar.push(topbar.getText().toString());
	}
}
