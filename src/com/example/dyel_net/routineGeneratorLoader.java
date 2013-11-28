package com.example.dyel_net;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.TextView;

public class routineGeneratorLoader {

	private MainActivity app;
	
	public routineGeneratorLoader(MainActivity _app){
		app = _app;
		
	}
	
	public String getDayName(int day){
		return "day_name";
	}
	
	public void toJSON(){
		try {
			String routine_name = "routine name";
			int weeks = 5;
			JSONObject MainJSON = new JSONObject();
			MainJSON.put("name", routine_name);
			/*************ADDING WEEK*******************/
			JSONArray jArray_week = new JSONArray();
			for(int week=0; week < weeks; week++){
				JSONObject jsonObject_week = new JSONObject();
				jsonObject_week.put("week", Integer.toString(week));
				jsonObject_week.put("finished", "0");
				/*************ADDING DAY*******************/
				JSONArray jArray_day = new JSONArray();
				for(int day=0; day < 7; day++){
					String day_name = getDayName(day);
					JSONObject jsonObject_day = new JSONObject();
					jsonObject_day.put("name", day_name);
					jsonObject_day.put("day", Integer.toString(day));
					jsonObject_day.put("finished", "0");
					/*************ADDING SET*******************/
					int numberOfSets = getNumberOfSets(week, day);
					JSONArray jArray_set = new JSONArray();
					for(int set=0; set < numberOfSets; set++){
						int exerciseID = getExerciseID(week, day, set);
						int setnumber = getSetNumber(exerciseID, week, day);
						HashMap<String, String> result = function(week, setnumber, exerciseID);
						JSONObject jsonObject_set = new JSONObject();
						jsonObject_set.put("exerciseID", Integer.toString(exerciseID));
						jsonObject_set.put("setnumber", Integer.toString(setnumber));
						jsonObject_set.put("reps", result.get("reps"));
						jsonObject_set.put("weight", result.get("weight"));
						jArray_set.put(set, jsonObject_set);
					}
					jsonObject_day.put("_set", jArray_set);
					jArray_day.put(day, jsonObject_day);
				}
				jsonObject_week.put("schedule_day", jArray_day);
				jArray_week.put(week, jsonObject_week);
			}
			MainJSON.put("schedule_week", jArray_week);

		} catch (JSONException e) {e.printStackTrace();}
	}
	
	public HashMap<String, String> function(int week, int setnumber, int exerciseID){
		
		/*************ADD BOX/COX HERE*********************/
		/*example: if week=2, setnumber=3, exerciseID=10
		 * then find the number of reps and weight for the exercise corresponding to
		 * exerciseID=10 for the 3rd set of such exercise on week 2 of the routine
		*/
		return new HashMap<String, String>();
	}
	
	public int getNumberOfSets(int week, int day){
		return 0;
	}
	
	public int getExerciseID(int week, int day, int set){
		return 0;
	}
	
	public int getSetNumber(int exerciseID, int week, int day){
		return 0;
	}
	
	public void addSet(JSONObject MainJSON)
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
			String SQL = "";
			try 
			{
				SQL = "";
				JSONArray jArray = MainJSON.getJSONArray("_set");
				for(int i=0; i < jArray.length(); i++){
					JSONObject jObject = jArray.getJSONObject(i);
					SQL += " INSERT INTO _set(dayID, exerciseID, reps, weight, setnumber, isReal, isGoal) " +
						   " VALUES(" +
							  	 jObject.getString("dayID") + "," + 
							  	 jObject.getString("exerciseID") + "," + 
							  	 jObject.getString("reps") + "," +
							  	 jObject.getString("weight") + "," + 
							  	 jObject.getString("setnum") + "," + 
							  	 "0,0);";
				}
				app.con.writeQuery(SQL);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
	
}
