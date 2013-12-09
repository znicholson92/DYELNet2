package com.example.dyel_net;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class RoutineGenerator {

	private MainActivity app;
	private String routineHash;
	
	private String routine_name = null;
	private int weeks = 0;
	
	private String curDay;
	String ex_id_global;
	
	private ArrayList<String> dayNames = new ArrayList<String>();
	
	LinReg regression;
	
	public RoutineGenerator(MainActivity _app){
		app = _app;
		routineHash = Long.toString(System.currentTimeMillis());
		dayNames.add("dummy");
		regression = new LinReg(app);
	}
	
	private String getDayName(int day){
		return dayNames.get(day);
	}
	
	public int getDayNum(String dayName){
		int index = dayNames.indexOf(dayName);
		Log.w("Index of", dayName + "= " + Integer.toString(index));
		return index;
	}
	
	public String getRoutineName(){
		return routine_name;
	}
	
	public void setRoutineName(String rn){
		routine_name = rn;
	}
	
	public String getNumWeeks(){
		
		if(weeks == 0){
			return null;
		} else {
			return Integer.toString(weeks);
		}
	}
	
	public void setNumWeeks(String _weeks){
		if(_weeks != ""){
			weeks = Integer.parseInt(_weeks);
		}
	}
	
	public void setCurrentDay(String _day){
		curDay = _day;
	}
	
	public String getCurrentDay(){
		return curDay;
	}
	
	public int getNumDays(){
		return dayNames.size();
	}
	
	public void go(String rtName, String numWeeks){
		
		routine_name = rtName;
		weeks = Integer.parseInt(numWeeks);
		
		try 
		{
			JSONObject jObject = generateMainJSON();
			addToDatabase(jObject);
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		
	}
	

	
	
	
	private JSONObject generateMainJSON() throws JSONException{
		
		JSONObject MainJSON = new JSONObject();
		MainJSON.put("name", routine_name);
		
		/*************ADDING WEEKS*******************/
		JSONArray jArray_week = new JSONArray();
		for(int week=1; week < weeks; week++){
			JSONObject jsonObject_week = new JSONObject();
			makeJSON_week(jsonObject_week, week);
			jArray_week.put(week, jsonObject_week);
		}
		MainJSON.put("schedule_week", jArray_week);

		return MainJSON;
	}
	
	private void makeJSON_week(JSONObject jsonObject_week, int week) throws JSONException{
		
		jsonObject_week.put("week", Integer.toString(week));
		/*************ADDING DAYS*******************/
		JSONArray jArray_day = new JSONArray();
		for(int day=1; day < dayNames.size(); day++){
			JSONObject jsonObject_day = new JSONObject();
			makeJSON_day(jsonObject_day, day, week);
			jArray_day.put(day, jsonObject_day);
		}
		jsonObject_week.put("schedule_day", jArray_day);
	}
	
	private void makeJSON_day(JSONObject jsonObject_day, int day, int week) throws JSONException{
		
		String day_name = getDayName(day);
		jsonObject_day.put("name", day_name);
		jsonObject_day.put("day", Integer.toString(day));
		
		/*************ADDING SETS*******************/
		int numberOfSets = getNumberOfSets(day);
		JSONArray jArray_set = new JSONArray();
		for(int set=1; set <= numberOfSets; set++){
			JSONObject jsonObject_set = new JSONObject();
			makeJSON_Set(jsonObject_set, set, day, week);
			jArray_set.put(set, jsonObject_set);
		}
		
		jsonObject_day.put("_set", jArray_set);
	}
	
	private void makeJSON_Set(JSONObject jsonObject_set, int set, int day, int week) throws JSONException{
		
		int exerciseID = getExerciseID(day, set);
		ex_id_global = Integer.toString(exerciseID);
		int setnumber = getSetNumber(day, set);
		HashMap<String, String> result = function(week, setnumber, exerciseID);
		jsonObject_set.put("exerciseID", Integer.toString(exerciseID));
		jsonObject_set.put("setnumber", Integer.toString(setnumber));
		jsonObject_set.put("reps", result.get("reps"));
		jsonObject_set.put("weight", result.get("weight"));
	}
	
	public HashMap<Integer, int[]> get_scheme()
	{
		HashMap<Integer, int[]> ret_map = new HashMap<Integer, int[]>();
		int iter = 1;
		int rem;
		
		while (iter < (weeks + 1))
		{
			rem = iter % 4; 
			int set_rep[] = new int[2];
			
			if (rem == 1)
			{ 
				ret_map.put(iter, new int[]{4, 12});
			}
			
			if (rem == 2)
			{
				ret_map.put(iter, new int[]{4, 8});
			}
			
			if (rem == 3)
			{
				ret_map.put(iter, new int[]{4, 5});
			}
			
			if (rem == 0)
			{
				//designates 10, 8, 5, 3, 1 week
				ret_map.put(iter, new int[]{4, -1});
			}
			
			iter++;
		}
		
		return ret_map;
	}
	
	public HashMap<String, String> function(int week, int setnumber, int exerciseID){
		
		/* Get equation params */
		String strExID = Integer.toString(exerciseID);
		regression.pull_data(strExID);
		float b0 = regression.hm1.get(strExID);
		float b1 = regression.hm2.get(strExID);
		
		/* Approx 1rm (adjusted) at given week */
		float y = (float) (b0 + (b1 * Math.log(week)));
		
		HashMap<Integer, int[]> scheme = get_scheme();
		
		int[] set_rep = scheme.get(week);
		double weight = 0; 
		int num_reps = set_rep[1];
		
		if (set_rep[1] == 12)
		{
			if (setnumber == 1)
				weight = (.55 * y);
			if (setnumber == 2)
				weight = (.60 * y);
			if (setnumber == 3 || setnumber == 4)
				weight = (.65 * y);
		}
		
		if (set_rep[1] == 8)
		{
			if (setnumber == 1)
				weight = (.60 * y);
			if (setnumber == 2)
				weight = (.65 * y);
			if (setnumber == 3)
				weight = (.70 * y);
			if (setnumber == 4)
				weight = (.75 * y);
		}
		
		if (set_rep[1] == 5)
		{
			if (setnumber == 1)
				weight = (.55 * y);
			if (setnumber == 2)
				weight = (.65 * y);
			if (setnumber == 3)
				weight = (.75 * y);
			if (setnumber == 4)
				weight = (.85 * y);
		}
		
		if (set_rep[1] == -1)
		{
			if (setnumber == 1)
			{
				weight = (.5 * y);
				num_reps = 8;
			}
			if (setnumber == 2)
			{
				weight = (.7 * y);
				num_reps = 5;
			}
			if (setnumber == 3)
			{
				weight = (.85 * y);
				num_reps = 3;
			}
			if (setnumber == 4)
			{
				weight = (1.025 * y);
				num_reps = 1;
			}
		}
		
		String weight_str = Double.toString(weight);
		String reps_str = Integer.toString(num_reps);
		
		HashMap<String, String> ret = new HashMap<String, String>();
		ret.put("reps", reps_str);
		ret.put("weight", weight_str);
		
		return ret;
	}
	
	private int getNumberOfSets(int day){
		return app.cache.getNumberOfSets(routineHash, day);
	}
	
	private int getExerciseID(int day, int set){
		return app.cache.getExerciseID(routineHash, day, set);
	}
	
	private int getSetNumber(int day, int set){
		return app.cache.getSetNumber(routineHash, day, set);
	}
	
	private void addToDatabase(JSONObject MainJSON)
	{
		String SQL = "";
		try 
		{
			Log.w("MAIN JSON", MainJSON.toString());
			String routine_name = MainJSON.getString("name");
			String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
			
			SQL = "INSERT INTO routine(name, username, lastedited) VALUES('" + 
					routine_name + "','" +
					app.con.username() + "','" +
					timeStamp + "')";
			
			app.con.writeQuery(SQL);		
			
			//get routineID
			String routineID = getRoutineID(routine_name, timeStamp);
			
			JSONArray jArray_week = MainJSON.getJSONArray("schedule_week");
			
			for(int k=1; k < jArray_week.length(); k++){
				
				JSONObject jObject_week = jArray_week.getJSONObject(k);
				
				if(jObject_week == null)
					continue;
				
				String week = jObject_week.getString("week");
				
				SQL = "INSERT INTO schedule_week(week, routineID) VALUES (" +
					  week + "," + routineID + ")";
				
				app.con.writeQuery(SQL);
				
				//get weekID
				String weekID = getWeekID(routineID, week);
				
				JSONArray jArray_day = jObject_week.getJSONArray("schedule_day");
				
				for(int j=1; j < jArray_day.length(); j++){
					
					JSONObject jObject_day = jArray_day.getJSONObject(j);
					
					if(jObject_day == null)
						continue;
					
					String day = jObject_day.getString("day");
					
					String dayname = jObject_day.getString("name");
					
					SQL = "INSERT INTO schedule_day(routineID, weekID, day, name) VALUES(" +
						  routineID + "," + weekID + "," + day + ",'" + dayname + "')";
					
					app.con.writeQuery(SQL);
					
					//get dayID
					String dayID = getDayID(weekID, day);
					
					JSONArray jArray_set = jObject_day.getJSONArray("_set");
					
					for(int i=1; i < jArray_set.length(); i++){
					
						JSONObject jObject_set = jArray_set.getJSONObject(i);
						
						if(jObject_set == null)
							continue;
						
						SQL = " INSERT INTO _set(dayID, exerciseID, reps, weight, setnumber, isReal, isGoal, finished) " +
							   " VALUES(" +
								  	 dayID + "," + 
								  	 jObject_set.getString("exerciseID") + "," + 
								  	 jObject_set.getString("reps") + "," +
								  	 jObject_set.getString("weight") + "," + 
								  	 jObject_set.getString("setnumber") + "," + 
								  	 "0,0,0)";
						
						app.con.writeQuery(SQL);
					}
				}
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public String getRoutineID(String rtName, String timeStamp) throws JSONException
	{
		String SQL = " SELECT routineID FROM routine WHERE name='" + rtName + "' AND lastedited='" + timeStamp + "'";
		String jString = app.con.readQuery(SQL);
		JSONObject jObject = new JSONObject(jString);
		JSONArray jArray = jObject.getJSONArray("data");
		JSONObject j = jArray.getJSONObject(0);
		String result = j.get("routineID").toString();
		return result;
	}
	
	public String getWeekID(String rtID, String week) throws JSONException
	{
		String SQL = " SELECT weekID FROM schedule_week WHERE routineID=" + rtID + " AND week=" + week;
		String jString = app.con.readQuery(SQL);
		JSONObject jObject = new JSONObject(jString);
		JSONArray jArray = jObject.getJSONArray("data");
		JSONObject j = jArray.getJSONObject(0);
		String result = j.get("weekID").toString();
		return result;
	}
	
	public String getDayID(String weekID, String day) throws JSONException
	{
		String jString = "NULL";
		while(jString == "NULL"){
			String SQL = " SELECT dayID FROM schedule_day WHERE weekID=" + weekID + " AND day=" + day;
			jString = app.con.readQuery(SQL);
		}
		JSONObject jObject = new JSONObject(jString);
		JSONArray jArray = jObject.getJSONArray("data");
		JSONObject j = jArray.getJSONObject(0);
		String result = j.get("dayID").toString();
		return result;
	}
	
	public void addSet(String day, String exercise_name){
		
		app.cache.addExercise(routineHash, day, 4, exercise_name);
	
	}
	
	public void addDay(String _dayName, int _day){
		
		dayNames.add(_dayName);
		app.cache.addDay(routineHash, Integer.toString(_day), _dayName);
	
	}
	
	public void displayPlan(){
		ExpandableListView listview = (ExpandableListView)app.findViewById(R.id.routine_generator_listview);
		app.cache.displayRoutineGeneratorPlan(routineHash, listview);
	}
	
}
