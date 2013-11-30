package com.example.dyel_net;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LinReg {

	String exer_in;
	String exer_id; 
	String data_string; 
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
	
	String grab_data(String exer_id)
	{
		/* Get all _set data */
		return "ish";
	}
	
	void parse_data(String data_string)
	{
		/* Parse data, push node to list */
	}
	
	void pull_data(String exercise)
	{
		exer_in = exercise;
		exer_id = name_to_ID(exer_in);
		data_string = grab_data(exer_id);
		parse_data(data_string); 
		
		
		
	}

}
