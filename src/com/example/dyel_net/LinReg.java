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
	
	ArrayList<DataNode> nodes = new ArrayList<DataNode>();
	
	public LinReg(MainActivity a)
	{
		app = a;	
	}

	String name_to_ID(String exer_in)
	{
		String temp = "SELECT exerciseID FROM exercise WHERE name == " + exer_in + ";";
		
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
		String temp = "SELECT dayID FROM _set WHERE exerciseID == " + exer_id + ";";
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
	
	ArrayList<String> grab_data(String exer_id)
	{
		ArrayList<String> dayIDs = pull_days(exer_id);
		return dayIDs;
	}

	
	void pull_data(String exercise)
	{
		//Do i need to call LinReg() constructor?
		exer_in = exercise;
		exer_id = app.cache.getExerciseID(exercise);
		exer_id = name_to_ID(exer_in);
		dayIDs = grab_data(exer_id);
		//have list of day_ids... 
		
		
		
		
		
		
	}

}
