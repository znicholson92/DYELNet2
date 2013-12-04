package com.example.dyel_net;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LinReg {

	String exer_in;
	String exer_id; 
	String data_string; 
	ArrayList<String> dayIDs;
	MainActivity app;
	
	HashMap<String, Float> hm1;
	HashMap<String, Float> hm2;
	
	public LinReg(MainActivity a)
	{
		app = a;
		hm1 = new HashMap<String, Float>();
		hm2 = new HashMap<String, Float>();
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
	
	float get_adjusted(int max, int reps)
	{
		/* using Brzycki formula */
		int den = 37 - reps; 
		float fract = 36/den; 
		float adj = max * fract; 
		
		return adj;
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

			String jString = app.con.readQuery(query_for_week);

			String week_s = "";
			try {
				JSONObject jsonObject = new JSONObject(jString);
				JSONArray jArray = jsonObject.getJSONArray("data");
				JSONObject j = jArray.getJSONObject(0);
				week_s = (String) j.get("week");
			} catch (JSONException e) {e.printStackTrace();}
			
			int week = Integer.parseInt(week_s);
			temp.week = week;
			
			String get_max = "SELECT MAX(weight) AS Max, reps FROM _set WHERE dayID = '" + temp.dayID
					+ "' exerciseID = '" + exer_id + "';";

			String jString1 = app.con.readQuery(get_max);

			String max_s = "";
			String reps_s = "";
			try {
				JSONObject jsonObject = new JSONObject(jString1);
				JSONArray jArray = jsonObject.getJSONArray("data");
				JSONObject j = jArray.getJSONObject(0);
				max_s = (String) j.get("Max");
				reps_s = (String) j.get("reps");
			} catch (JSONException e) {e.printStackTrace();}
			
			int max = Integer.parseInt(max_s);
			int reps = Integer.parseInt(reps_s);
			
			float adjusted = get_adjusted(max, reps);
			
			temp.adjusted = (float) Math.log(adjusted);
			nodes.add(iter, temp);
			iter++;
		}
		
		return nodes;
	}
	
	float calc_xbar(ArrayList<DataNode> nodes)
	{
		int len = nodes.size();
		int iter = 0;
		float total = 0;
		
		while (iter < len)
		{
			DataNode temp = nodes.get(iter);
			total += temp.week;
			iter++;
		}
		
		float res = total/len; 
		return res;
		
	}
	
	float calc_SXX(ArrayList<DataNode> nodes)
	{
		float xbar = calc_xbar(nodes);
		int len = nodes.size();
		int iter = 0;
		float total = 0;
		
		while (iter < len)
		{
			DataNode temp = nodes.get(iter);
			float hold = (temp.week - xbar) * (temp.week - xbar);
			total += hold; 
			iter++;
		}
		
		return total; 
	}
	
	float calc_ybar(ArrayList<DataNode> nodes)
	{
		int len = nodes.size();
		int iter = 0;
		float total = 0;
		
		while (iter < len)
		{
			DataNode temp = nodes.get(iter);
			total += temp.adjusted;
			iter++;
		}
		
		float res = total/len; 
		return res;
		
	}
	
	float calc_SXY(ArrayList<DataNode> nodes)
	{
		float xbar = calc_xbar(nodes);
		float ybar = calc_ybar(nodes);
		
		int len = nodes.size();
		int iter = 0;
		float total = 0; 
		
		while (iter < len)
		{
			DataNode temp = nodes.get(iter);
			float hold = (temp.week - xbar) * (temp.adjusted - ybar);
			total += hold;
			iter++;
		}
		
		return total;
	}

	float calc_B0(ArrayList<DataNode> nodes, float B1)
	{
		float xbar = calc_xbar(nodes);
		float ybar = calc_ybar(nodes);
		
		float B0 = ybar - (B1 * xbar);
		return B0;
	}
	
	float[] calc_reg(ArrayList<DataNode> nodes)
	{
		float SXX = calc_SXX(nodes);
		float SXY = calc_SXY(nodes);
		float B1 = SXY/SXX;
		float B0 = calc_B0(nodes, B1);
		
		float RegEqn[] = new float[2];
		RegEqn[0] = B0;
		RegEqn[1] = B1; 
		
		return RegEqn;
	}
	
	
	void pull_data(String exercise)
	{
		exer_in = exercise;
		exer_id = app.cache.getExerciseID(exercise);
		exer_id = name_to_ID(exer_in);
		dayIDs = pull_days(exer_id);
		
		ArrayList<DataNode> nodes = grab_data(dayIDs, exer_id);
		float RegEqn[] = calc_reg(nodes);
		hm1.put(exer_id, RegEqn[0]);
		hm2.put(exer_id, RegEqn[1]);
		
	}

}
