package com.example.dyel_net;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class GoalSet {

	public static void open_editSet(MainActivity app, String setID)
	{		
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
	
	public static void viewSetGoal(MainActivity app, String setID)
	{		
		TextView exercise = (TextView)app.findViewById(R.id.goal_set_create_exercise_name);
		EditText setnumET = (EditText)app.findViewById(R.id.goal_set_create_setnumber);
		EditText ET1 = (EditText)app.findViewById(R.id.goal_set_create_reps);
		EditText ET2 = (EditText)app.findViewById(R.id.goal_set_create_weight);
		//TextView TV1 = (TextView)app.findViewById(R.id.goal_set_create_tv1);
		//TextView TV2 = (TextView)app.findViewById(R.id.goal_set_create_tv2);
		
		String SQL = "SELECT exercise.name, reps, weight, setnumber " + 
					 "FROM _set INNER JOIN exercise ON _set.exerciseID = exercise.exerciseID " +
					 "WHERE setID=" + setID;
		
		String jString = app.con.readQuery(SQL);
		
		try {
			JSONObject jsonObject = new JSONObject(jString);
			JSONArray jArray = jsonObject.getJSONArray("data");
			JSONObject j = jArray.getJSONObject(0);
					
			exercise.setText(j.get("name").toString());			
			//TV1.setText("Reps");
			//TV2.setText("Weight");
			//setnumET.setText(j.get("setnumber").toString());
			setnumET.setText("N/A");
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

	public static String createSetGoal2(MainActivity app){
		String setID = "";
		TextView exerciseID_TV = (TextView)app.findViewById(R.id.goal_set_create_exercise_id);
		TextView repsTV = (TextView)app.findViewById(R.id.goal_set_create_reps);
		TextView weightTV = (TextView)app.findViewById(R.id.goal_set_create_weight);
		
		String exerciseID = exerciseID_TV.getText().toString();
		String reps = repsTV.getText().toString();
		String weight = weightTV.getText().toString();
		
		if(exerciseID == "" || reps == "" || weight == ""){
			app.showDialog("Missing Fields");
		} else {
			String SQL = "INSERT INTO _set(exerciseID, reps, weight, isReal, isGoal) " +
	 				  	 "VALUES("+ exerciseID + "," + 
	 				  			  reps + "," +
	 				  			  weight + "," + 
	 				  			  "0,1)";			
			app.con.writeQuery(SQL);
		}
		//then receive setID from here
		String readSQL = "SELECT setID FROM _set WHERE exerciseID='"
				+ exerciseID + 
				"' AND reps='"+
				reps+
				"' AND weight='"+
				weight+
				"' AND isGoal=true";
		
		Log.w("SQL", readSQL);
		String JSONstring = app.con.readQuery(readSQL);
		JSONObject jsonObject;

		if (JSONstring.length() > 10) {
			try {
				jsonObject = new JSONObject(JSONstring);
				JSONArray jArray = jsonObject.getJSONArray("data");
				JSONObject j = jArray.getJSONObject(0);
				setID = j.get("setID").toString();

			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			return "";
		}
		return setID;
	}
	//public static void createSetGoal(final MainActivity app, String exerciseID, final String setID)
	public static void createSetGoal(final MainActivity app)
	{
		//TODO
		String setID = null;
		String exerciseID = null;
		EditText ET1 = (EditText)app.findViewById(R.id.goal_editset_et1);
		EditText ET2 = (EditText)app.findViewById(R.id.goal_editset_et2);
		EditText notes = (EditText)app.findViewById(R.id.goal_editset_notes);
		
		String reps, weight;
		if(setID != null) //exerciseID -> setID
		{
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
	}

	public static void setBrowseResult(MainActivity app, String...strings) {
		app.gotoLayout(R.layout.goal_set_create);		
		if(strings.length > 0){
			String exercise = strings[0];
			String exerciseID = strings[1];
			TextView exerciseTV = (TextView)app.findViewById(R.id.goal_set_create_exercise_name);
			TextView exerciseID_TV = (TextView)app.findViewById(R.id.goal_set_create_exercise_id);
			exerciseTV.setText(exercise);
			exerciseID_TV.setText(exerciseID);
		}
	}
}