/*package com.example.dyel_net;

import android.view.View;
import android.widget.CheckBox;
import android.os.Bundle;

public class display_exercises {
	
	String query_pre = "SELECT * FROM  ";
	String query_fin;
	ArrayList<String> muscle_groups; 
	String muscle_groups_str = "";
	
	void string_append(string s)
	{
		int len = muscle_groups.length; 
		String check_string = string + " ,";
		
		int i = 0;
		while (i < len){
			if (muscle_groups[i] == check_string)
					return; 
			i++; 
		}
		
		//otherwise we need to add it. 
		muscle_groups.add(check_string);
		query_fin = query_pre + 
		return; 
	}
	
	void string_remove(string s)
	{
		
		
		return; 
	}
	
	void query_combine()
	{
		return;
	}
	
	void list_to_string()
	{
		
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
	
	void display_exercises_toggle_arms()
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
	
	void display_exercises_toggle_chest()
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
	
	void display_exercises_toggle_back()
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
	
	void display_exercises_toggle_legs()
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
	
	void display_exercises_toggle_shoulders()
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
*/
