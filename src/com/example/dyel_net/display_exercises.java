package com.example.dyel_net;

import java.util.ArrayList;

import android.view.View;
import android.widget.CheckBox;

public class display_exercises {
	
	String query_pre = "SELECT * FROM  ";
	String query_fin;
	ArrayList<String> muscle_groups = new ArrayList<String>();
	String muscle_groups_str = "";
	
	void string_append(String s)
	{
		int len = muscle_groups.size(); 
	
		int i = 0;
		while (i < len)
		{
			String item;
			if ((item = muscle_groups.get(i)) == s)
					return; 
			i++; 
		}
		//otherwise we need to add it. 
		muscle_groups.add(s);
		String hold = list_to_string();
		muscle_groups_str = hold; 
		/* Actual string which hold the list of muscles is updated */
		return; 
	}
	
	void string_remove(String s)
	{
		int len = muscle_groups.size();
		int iter = 0;
		
		while (iter < len)
		{
			String item;
			if ((item = muscle_groups.get(iter)) == s)
			{
				muscle_groups.remove(iter);
				break;
			}
			iter++;
		}
		
		String hold = list_to_string();
		muscle_groups_str = hold;		
		return; 
	}
	
	void query_combine()
	{
		return;
	}
	
	String list_to_string()
	{
		String list_string = "";
		
		for (String s : muscle_groups)
		{
			list_string += "musclegroup = " + s + " OR ";
		}
		
		list_string = list_string.substring(0, list_string.length() - 4);
		
		return list_string;
	}
	
	void display_exercises_toggle_forearms(View v)
	{
		CheckBox temp = (CheckBox)v;
		if (temp.isChecked() == true)
		{
			//string_append("forearms");
		}
		
		if (temp.isChecked() == false)
		{
			//string_remove("forearms");
		}
	}
	
	void display_exercises_toggle_arms(View v)
	{
		CheckBox temp = (CheckBox)v;
		if (temp.isChecked() == true)
		{
			//string_append("arms");
		}
		
		if (temp.isChecked() == false)
		{
			//string_remove("arms");
		}
	}
	
	void display_exercises_toggle_chest(View v)
	{
		CheckBox temp = (CheckBox)v;
		if (temp.isChecked() == true)
		{
			//string_append("chest");
		}
		
		if (temp.isChecked() == false)
		{
			//string_remove("chest");
		}
	}
	
	void display_exercises_toggle_back(View v)
	{
		CheckBox temp = (CheckBox)v;
		if (temp.isChecked() == true)
		{
			string_append("back");
		}
		
		if (temp.isChecked() == false)
		{
			string_remove("back");
		}
	}
	
	void display_exercises_toggle_legs(View v)
	{
		CheckBox temp = (CheckBox)v;
		if (temp.isChecked() == true)
		{
			string_append("legs");
		}
		
		if (temp.isChecked() == false)
		{
			string_remove("legs");
		}
	}
	
	void display_exercises_toggle_shoulders(View v)
	{
		CheckBox temp = (CheckBox)v;
		if (temp.isChecked() == true)
		{
			string_append("shoulders");
		}
		
		if (temp.isChecked() == false)
		{
			string_remove("shoulders");
		}
	}

}

