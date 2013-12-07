package com.example.dyel_net;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.widget.EditText;
import android.widget.TextView;

public class GoalSet {

	//private String exerciseID = null;
	//private String setnumber = null;
	//private String setID = null;
	//private int list_index;
	//private static MainActivity app;
	
	public static void open_editSet(MainActivity app, String setID)
	{		
		//editing_set = true;
		
		TextView exercise = (TextView)app.findViewById(R.id.goal_editset_exercise);
		TextView setnumTV = (TextView)app.findViewById(R.id.goal_editset_setnumber);
		EditText ET1 = (EditText)app.findViewById(R.id.goal_editset_et1);
		EditText ET2 = (EditText)app.findViewById(R.id.goal_editset_et2);
		TextView TV1 = (TextView)app.findViewById(R.id.goal_editset_tv1);
		TextView TV2 = (TextView)app.findViewById(R.id.goal_editset_tv2);
		
		setnumTV.setText("N/A");
		
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

	
	
	public static void updateSetGoal(final MainActivity app, final String setID)
	{
		EditText ET1 = (EditText)app.findViewById(R.id.goal_editset_et1);
		EditText ET2 = (EditText)app.findViewById(R.id.goal_editset_et2);
		EditText notes = (EditText)app.findViewById(R.id.goal_editset_notes);
		
		String reps, weight;
		if(setID != null) //exerciseID -> setID
		{
			/*Thread trd = new Thread(new Runnable(){
    			@Override
    			public void run(){
    				connection con2 = new connection(app);
    				con2.writeQuery("UPDATE _set SET finished = 1 WHERE setID ='" + setID + "'");
    			}
    		});
    		trd.start();*/
			
			reps = ET1.getText().toString();
			weight = ET2.getText().toString();
			
			String note;
			if(notes.getText().toString().length() < 1)
				note = "NULL";
			else
				note = notes.getText().toString();
			String updateSQL = 
					"UPDATE _set SET " +
					"reps='" +
					reps +
					"', weight='" +
					weight +
					"', notes='" +
					note +
					"', isReal='" +
					"0" +  //hard coded
					"', isGoal='" +
					"1' " + //hard coded
					"WHERE setID ='"+
					setID+
					"';";
			app.con.writeQuery(updateSQL);
					
		}
	}
	
	public static void deleteSetGoal(MainActivity app, String setID){		
		String deleteSQL = "DELETE FROM _set WHERE setID = '" +
				setID +
				"';";
		connection con = new connection("dyel-net_admin", "teamturtle", app);
		ProgressDialog pd;
		pd = ProgressDialog.show(app, "Loading", "Deleting a set goal...");

		try {
			con.writeQuery(deleteSQL);
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		pd.cancel();
		con.logout();
	}

	public static void createSetGoal(final MainActivity app, String exerciseID, final String setID)
	{
		EditText ET1 = (EditText)app.findViewById(R.id.goal_editset_et1);
		EditText ET2 = (EditText)app.findViewById(R.id.goal_editset_et2);
		EditText notes = (EditText)app.findViewById(R.id.goal_editset_notes);
		
		String reps, weight;
		if(setID != null) //exerciseID -> setID
		{
			/*Thread trd = new Thread(new Runnable(){
    			@Override
    			public void run(){
    				connection con2 = new connection(app);
    				con2.writeQuery("UPDATE _set SET finished = 1 WHERE setID ='" + setID + "'");
    			}
    		});
    		trd.start();*/
			
			reps = ET1.getText().toString();
			weight = ET2.getText().toString();
			
			String note;
			if(notes.getText().toString().length() < 1)
				note = "NULL";
			else
				note = notes.getText().toString();
			
			String SQL = "INSERT INTO _set(exerciseID, reps, weight, setnumber, " +
						 				  " notes, isReal, isGoal) " +
					"VALUES("
					+ exerciseID
					+ ","
					+ reps
					+ ","
					+ weight
					+ ","
					+ "0"  //trivial number for set number
					+ ","
					+ note
					+ "," + "0,1)";
	  	
			app.con.writeQuery(SQL);
					
		}
		//editing_set = false;
	}
}