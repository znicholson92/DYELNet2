package com.example.dyel_net;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LinReg {

	String exer_in;
	String exer_id; 
	String data_string; 
	String dayID;
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
	
	String pull_days(String exer_id)
	{
		String temp = "SELECT dayID FROM _set WHERE exerciseID == " + exer_id + ";";
		String jString = app.con.readQuery(temp);
		
		String dayID = "";
		
		try {
			JSONObject jsonObject = new JSONObject(jString);
			JSONArray jArray = jsonObject.getJSONArray("data");
			JSONObject j = jArray.getJSONObject(0);
			dayID = (String) j.get("dayID");
		} catch (JSONException e) {e.printStackTrace();}
		
		return dayID;
	}
	
	String parse_days(String dayID)
	{
		//string.split()?
		return "ISH";
	}
	
	String grab_data(String exer_id)
	{
		String dayID = pull_days(exer_id);
		String parsed_days = parse_days(dayID);
		
		return "ish";
	}
	
	void parse_data(String data_string)
	{
		/* Parse data, push node to list */
	}
	
	void pull_data(String exercise)
	{
		//Do i need to call LinReg() constructor?
		exer_in = exercise;
		exer_id = name_to_ID(exer_in);
		data_string = grab_data(exer_id);
		parse_data(data_string);
		
		
		
		
	}

}
