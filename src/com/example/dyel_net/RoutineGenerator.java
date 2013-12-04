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
	
	private ArrayList<String> dayNames = new ArrayList<String>();
	
	LinReg regression = new LinReg(app);
	
	public RoutineGenerator(MainActivity _app){
		app = _app;
		routineHash = Long.toString(System.currentTimeMillis());
		dayNames.add("dummy");
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
		String temp = ""; //placeholder
		
		try 
		{
			generateRegression(temp); //placeholder
			JSONObject jObject = generateMainJSON();
			addToDatabase(jObject);
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		
	}
	
	private void generateRegression(String exer_in){  //takes exercise string as parameter

		regression.pull_data(exer_in); //does regression on this exercise, stores equation in hashmaps.
	}
	
	
	
	private JSONObject generateMainJSON() throws JSONException{
		
		JSONObject MainJSON = new JSONObject();
		MainJSON.put("name", routine_name);
		
		/*************ADDING WEEKS*******************/
		JSONArray jArray_week = new JSONArray();
		for(int week=0; week < weeks; week++){
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
		for(int day=0; day < 7; day++){
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
		for(int set=0; set < numberOfSets; set++){
			JSONObject jsonObject_set = new JSONObject();
			makeJSON_Set(jsonObject_set, set, day, week);
			jArray_set.put(set, jsonObject_set);
		}
		jsonObject_day.put("_set", jArray_set);
	}
	
	private void makeJSON_Set(JSONObject jsonObject_set, int set, int day, int week) throws JSONException{
		
		int exerciseID = getExerciseID(day, set);
		int setnumber = getSetNumber(day, set);
		HashMap<String, String> result = function(week, setnumber, exerciseID);
		jsonObject_set.put("exerciseID", Integer.toString(exerciseID));
		jsonObject_set.put("setnumber", Integer.toString(setnumber));
		jsonObject_set.put("reps", result.get("reps"));
		jsonObject_set.put("weight", result.get("weight"));
	}
	
	public HashMap<String, String> function(int week, int setnumber, int exerciseID){
		
		/*************ADD BOX/COX HERE*********************/
		/*example: if week=2, setnumber=3, exerciseID=10
		 * then find the number of reps and weight for the exercise corresponding to
		 * exerciseID=10 for the 3rd set of such exercise on week 2 of the routine
		*/
		return new HashMap<String, String>();
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
			String routine_name = MainJSON.getString("name");
			String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
			
			SQL = "INSERT INTO routine(name, username, lastedited) VALUES('" + 
					routine_name + "','" +
					app.con.username() + ',' +
					timeStamp + "')";
			
			app.con.writeQuery(SQL);		
			
			//get routineID
			String routineID = "TODO";
			
			JSONArray jArray_week = MainJSON.getJSONArray("schedule_week");
			
			for(int k=0; k < jArray_week.length(); k++){
				
				JSONObject jObject_week = jArray_week.getJSONObject(k);
				
				String week = jObject_week.getString("week");
				
				SQL = "INSERT INTO week(week, routineID, finished) VALUES (" +
					  week + "," + routineID + ",0)";
				
				app.con.writeQuery(SQL);
				
				//get weekID
				String weekID = "TODO";
				
				JSONArray jArray_day = jObject_week.getJSONArray("schedule_day");
				
				for(int j=0; j < jArray_day.length(); j++){
					
					JSONObject jObject_day = jArray_day.getJSONObject(j);
					
					String day = jObject_day.getString("day");
					
					String dayname = jObject_day.getString("name");
					
					SQL = "INSERT INTO day(weekID, day, name, finished) VALUES(" +
						  weekID + "," + day + "," + dayname + ",0)";
					
					app.con.writeQuery(SQL);
					
					//get dayID
					String dayID = "TODO";
					
					JSONArray jArray_set = jObject_day.getJSONArray("_set");
					
					for(int i=0; i < jArray_set.length(); i++){
					
						JSONObject jObject_set = jArray_set.getJSONObject(i);
						
						SQL = " INSERT INTO _set(dayID, exerciseID, reps, weight, setnumber, isReal, isGoal) " +
							   " VALUES(" +
								  	 dayID + "," + 
								  	 jObject_set.getString("exerciseID") + "," + 
								  	 jObject_set.getString("reps") + "," +
								  	 jObject_set.getString("weight") + "," + 
								  	 jObject_set.getString("setnumber") + "," + 
								  	 "0,0)";
						
						app.con.writeQuery(SQL);
					}
				}
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
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
