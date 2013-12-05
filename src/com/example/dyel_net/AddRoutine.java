package com.example.dyel_net;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class AddRoutine {
	
	private MainActivity app;
	private String status;
	public boolean running = false;
	
	private int numDays;
	private int currDay;
	private String currDayName;
	private int currExercise;
	private int numWeeks;
	private String routineName;
	private int routineID;
	private int exerciseID;
	private String exerciseName;
	private int currSet;
	private int numSets;
	private int numExercises;
	private int currWeek;
	public ArrayList<Integer> weekIDs;
	public ArrayList<Integer> dayIDs;
	
	public AddRoutine(MainActivity a)
	{
		app = a;
		running = true;
		currDay=1;
		currWeek=1;
	}
	
	public int addRoutine()
	{
		TextView nameTV = (TextView)app.findViewById(R.id.add_routine_name);
		TextView weekTV = (TextView)app.findViewById(R.id.add_routine_num_weeks);
		TextView dayTV = (TextView)app.findViewById(R.id.add_routine_num_days);

		routineName = nameTV.getText().toString();
		String weeks = weekTV.getText().toString();
		String days = dayTV.getText().toString();
		
		numDays = Integer.parseInt(days);
		numWeeks = Integer.parseInt(weeks);
		
		if(routineName.equals("") || weeks.equals("") || days.equals("")){
			app.showDialog("Missing Fields");
			return -1;
		} else {
			String SQL = "INSERT INTO routine(name, username) " +
	 				  	 "VALUES('" + routineName + "', '" + 
	 				  			  app.con.username() +
	 				  			  "')";
			
			app.con.writeQuery(SQL);
			String result = app.con.readQuery("SELECT routineID from routine WHERE name='"+routineName+"' and username='"+app.con.username()+"'");
			try {
				JSONObject jsonObject = new JSONObject(result);
				JSONArray jArray = jsonObject.getJSONArray("data");
	    		JSONObject j = jArray.getJSONObject(0);
	    		Iterator<String> iter = j.keys();
	            while (iter.hasNext()) {
	                String key = iter.next();
	                String value = (String)j.get(key);
	                if(key.contains("ID")){
	                	return j.getInt(key);
	                }
	            }
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return -1;
		}
	}
	
	public ArrayList<Integer> getWeekIDs(int id){
		routineID = id;
		weekIDs = new ArrayList<Integer>();
		String result = app.con.readQuery("SELECT weekID from schedule_week WHERE routineID="+routineID);
		try {
			JSONObject jsonObject = new JSONObject(result);
			JSONArray jArray = jsonObject.getJSONArray("data");
			for(int x=0; x<jArray.length(); x++){
	    		JSONObject j = jArray.getJSONObject(x);
	    		Iterator<String> iter = j.keys();
	            while (iter.hasNext()) {
	                String key = iter.next();
	                String value = (String)j.get(key);
	                if(key.contains("ID")){
	                	weekIDs.add(Integer.parseInt(value));
	                }
	            }
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return weekIDs;
	}
	
	public void addDay(){
		currExercise = 1;
		TextView nameTV = (TextView)app.findViewById(R.id.add_routine_day_name);
		TextView exercisesTV = (TextView)app.findViewById(R.id.add_routine_day_num_exercises);

		String name = nameTV.getText().toString();
		String ex = exercisesTV.getText().toString();
		numExercises = Integer.parseInt(ex);
		currDayName = name;
		if(name.equals("") || ex.equals("")){
			app.showDialog("Missing Fields");
		}
		else{
			dayIDs = new ArrayList<Integer>();
			for(int weekID : weekIDs){
				String SQL = "INSERT INTO schedule_day(name, routineID, weekID, day) " +
				  	 "VALUES('" + name + "', " + 
				  			  routineID + ", " +
				  			  weekID + ", " +
				  			  currDay +
				  			  ")";
				app.con.writeQuery(SQL);
			}
			String result = app.con.readQuery("SELECT dayID from schedule_day WHERE routineID="+routineID+" and day="+currDay);
			try {
				JSONObject jsonObject = new JSONObject(result);
				JSONArray jArray = jsonObject.getJSONArray("data");
				for(int x=0; x<jArray.length(); x++){
		    		JSONObject j = jArray.getJSONObject(x);
		    		Iterator<String> iter = j.keys();
		            while (iter.hasNext()) {
		                String key = iter.next();
		                String value = (String)j.get(key);
		                if(key.contains("ID")){
		                	dayIDs.add(Integer.parseInt(value));
		                }
		            }
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			app.setContentView(R.layout.add_routine_day_exercise);
			TextView header = (TextView)app.findViewById(R.id.exercise_num);
			header.setText("Day: " + currDay);
		}
	}
	
	private void nextDayOrEnd(){
		if(currDay>=numDays){
			running = false;
			app.gotoRoutineView(new View(app));
		}
		else{
			currDay++;
			app.gotoLayout(R.layout.add_routine_day);
			TextView header = (TextView)app.findViewById(R.id.day_name);
			header.setText("Day: " + currDay);
		}
	}
	
	public void addExercise(){
		currSet = 1;
		TextView nameTV = (TextView)app.findViewById(R.id.exercise_name);
		TextView exerciseIdTV = (TextView)app.findViewById(R.id.exercise_id);
		TextView numSetTV = (TextView)app.findViewById(R.id.add_routine_day_exercises_num_sets);
		
		String name = nameTV.getText().toString();
		String id = exerciseIdTV.getText().toString();
		exerciseID = Integer.parseInt(id);
		String num = numSetTV.getText().toString();
		numSets = Integer.parseInt(num);
		exerciseName = name;
		if(name.equals("") || id.equals("") || num.equals("")){
			app.showDialog("Missing Fields");
		}
		else if(numSets < 0){
			app.showDialog("Can't have that many sets, bro");
		}
		else{
			app.setContentView(R.layout.add_routine_day_exercise_set);
			TextView header = (TextView)app.findViewById(R.id.exercise_name);
			header.setText(exerciseName + " Set: " + currSet);
		}
	}
	
	public void addSet(){
		TextView repsTV = (TextView)app.findViewById(R.id.set_reps);
		TextView weightTV = (TextView)app.findViewById(R.id.set_weight);
		TextView increaseTV = (TextView)app.findViewById(R.id.weight_increase);
		
		String reps = repsTV.getText().toString();
		String weight = weightTV.getText().toString();
		int increase = Integer.parseInt(increaseTV.getText().toString());
		if(reps.equals("") || weight.equals("")){
			app.showDialog("Missing Fields");
		}
		else{
			int currWeight = Integer.parseInt(weight);
			for(int dayID : dayIDs){
				String SQL = "INSERT INTO _set(dayID, exerciseID, reps, weight, setnumber, isReal, isGoal, finished) " +
				  	 "VALUES(" + dayID + ", " + 
				  			  exerciseID + ", " +
				  			  reps + ", " +
				  			  currWeight + ", " + 
				  			  currSet + ", " +
				  			  0 + ", " +
				  			  0 + ", " +
				  			  0 +
				  			  ")";
				app.con.writeQuery(SQL);
				currWeight+=increase;
			}
			if(currSet >= numSets){
				currExercise++;
				if(currExercise > numExercises){
					nextDayOrEnd();
				}
				else{
					app.setContentView(R.layout.add_routine_day_exercise);
					TextView header = (TextView)app.findViewById(R.id.exercise_num);
					header.setText("Week: "+ currWeek + "Day: " + currDayName);
				}
			}
			else{
				currSet++;
				app.setContentView(R.layout.add_routine_day_exercise_set);
				TextView header = (TextView)app.findViewById(R.id.exercise_name);
				header.setText(exerciseName + " Set: " + currSet);
			}
		}
	}
	
	public void openAddRoutineDay(String exerciseName, String exerciseID){
		app.gotoLayout(R.layout.add_routine_day_exercise);
		
		TextView exerciseTV = (TextView)app.findViewById(R.id.exercise_name);
		TextView exerciseID_TV = (TextView)app.findViewById(R.id.exercise_id);
		exerciseTV.setText(exerciseName);
		exerciseID_TV.setText(exerciseID);
	}
	
	public int getCurrDayNum(){
		return currDay;
	}
}
