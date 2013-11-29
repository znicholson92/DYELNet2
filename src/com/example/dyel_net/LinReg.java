package com.example.dyel_net;

import java.util.ArrayList;

public class LinReg {

	String exer_in;
	String exer_id; 
	String data_string; 
	
	ArrayList<DataNode> nodes = new ArrayList<DataNode>();

	String name_to_ID(String exer_in)
	{
		/* Query for the ID given the actual string */
		return "ish";
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
