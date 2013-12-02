package com.example.dyel_net;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import org.joda.time.DateTime;

public class LinReg {

	String exer_in;
	String exer_id; 
	String data_string; 
	ArrayList<String> dayIDs;
	MainActivity app;
	
	public LinReg(MainActivity a)
	{
		app = a;	
	}

	String name_to_ID(String exer_in)
	{
		String temp = "SELECT exerciseID FROM exercise WHERE name = '" + exer_in + "';";
		
		String jString = app.con.readQuery(temp);
		String ID = "";
		
		try {
			JSONObject jsonObject = new JSONObject(jString);
			JSONArray jArray = jsonObject.getJSONArray("data");
			JSONObject j = jArray.getJSONObject(0);
			ID = (String) j.get("exerciseID");
		} catch (JSONException e) {e.printStackTrace();}
		
		return ID;
	}
	
	@SuppressWarnings("null")
	private ArrayList<String> pull_days(String exer_id)
	{
		String temp = "SELECT dayID FROM session WHERE exerciseID = '" + exer_id + "' AND username " +
				"= '" + app.con.username() + "';";
		String jString = app.con.readQuery(temp);
		
		ArrayList<String> dayIDs = new ArrayList<String>();
		
		try {
			JSONObject jsonObject = new JSONObject(jString);
			JSONArray jArray = jsonObject.getJSONArray("data");
			for(int i=0; i < jArray.length(); i++){
				JSONObject j = jArray.getJSONObject(i);
				String dayID = j.get("dayID").toString();
				dayIDs.add(dayID);
			}
		} catch (JSONException e) {e.printStackTrace();}
		
		return dayIDs;
	}
	
	String parse_days(String dayID)
	{
		//string.split()?
		return "ISH";
	}
	
	ArrayList<DataNode> grab_data(ArrayList<String> dayIDs, String exer_id)
	{
		ArrayList<DataNode> nodes = new ArrayList<DataNode>();
		
		int iter = 0;
		int day_len = dayIDs.size();
		while (iter < day_len)
		{
			DataNode temp = new DataNode();
			temp.dayID = dayIDs.get(iter);
			
			String query_for_week = "SELECT week FROM schedule_week INNER JOIN schedule_day" +
					" ON schedule_week.weekID = schedule_day.weekID WHERE dayID = '" + temp.dayID + "';"; 
			
			String week_s = "";
			try {
				JSONObject jsonObject = new JSONObject(query_for_week);
				JSONArray jArray = jsonObject.getJSONArray("data");
				JSONObject j = jArray.getJSONObject(0);
				week_s = (String) j.get("week");
			} catch (JSONException e) {e.printStackTrace();}
			
			int week = Integer.parseInt(week_s);
			temp.week = week;
			
			ArrayList<String> weights = new ArrayList<String>();
			
			String get_max = "SELECT MAX(weight) AS Max, reps FROM _set WHERE dayID = '" + temp.dayID
					+ "' exerciseID = '" + exer_id + "';";

			
			
		}
		
		
		return nodes;
	}

	
	void pull_data(String exercise)
	{
		//Do i need to call LinReg() constructor?
		exer_in = exercise;
		exer_id = app.cache.getExerciseID(exercise);
		exer_id = name_to_ID(exer_in);
		dayIDs = pull_days(exer_id);
		
		ArrayList<DataNode> nodes = grab_data(dayIDs, exer_id);
		
		//have list of day_ids... 
		
		
		
		
		
		
	}

}
