package com.example.dyel_net;

import java.util.ArrayList;

import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;

public class display_exercises {
	
	String query_final;
	ArrayList<String> muscle_groups = new ArrayList<String>();
	String muscle_groups_str = "";
	MainActivity app;
	ListView lv = (ListView) app.findViewById(R.id.display_exercise_col_header);
	LinearLayout layout = (LinearLayout) app.findViewById(R.id.display_exercise_col_header);
	
	public display_exercises(MainActivity a)
	{
		app = a;	
	}
	
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
		query_final = "SELECT exercise.name, muscle.name from exercise inner " +
					  "join muscle ON exercise.muscleID = muscle.muscleID WHERE "
					  + muscle_groups_str;
		return;
	}
	
	String list_to_string()
	{
		String list_string = "";
		
		for (String s : muscle_groups)
		{
			list_string += "musclegroup = '" + s + "' OR ";
		}
		
		list_string = list_string.substring(0, list_string.length() - 4);
		
		return list_string;
	}
	
	void display_exercises_toggle_forearms(View v)
	{
		CheckBox temp = (CheckBox)v;
		if (temp.isChecked() == true)
		{
			string_append("forearms");
			query_combine();
			app.con.readQuery(query_final, lv, layout);

		}
		
		if (temp.isChecked() == false)
		{
			string_remove("forearms");
			query_combine();
			app.con.readQuery(query_final, lv, layout);

		}
	}
	
	void display_exercises_toggle_arms(View v)
	{
		CheckBox temp = (CheckBox)v;
		if (temp.isChecked() == true)
		{
			string_append("arms");
			query_combine();
			app.con.readQuery(query_final, lv, layout);

		}
		
		if (temp.isChecked() == false)
		{
			string_remove("arms");
			query_combine();
			app.con.readQuery(query_final, lv, layout);

		}
	}
	
	void display_exercises_toggle_chest(View v)
	{
		CheckBox temp = (CheckBox)v;
		if (temp.isChecked() == true)
		{
			string_append("chest");
			query_combine();
			app.con.readQuery(query_final, lv, layout);

		}
		
		if (temp.isChecked() == false)
		{
			string_remove("chest");
			query_combine();
			app.con.readQuery(query_final, lv, layout);

		}
	}
	
	void display_exercises_toggle_back(View v)
	{
		CheckBox temp = (CheckBox)v;
		if (temp.isChecked() == true)
		{
			string_append("back");
			query_combine();
			app.con.readQuery(query_final, lv, layout);

		}
		
		if (temp.isChecked() == false)
		{
			string_remove("back");
			query_combine();
			app.con.readQuery(query_final, lv, layout);

		}
	}
	
	void display_exercises_toggle_legs(View v)
	{
		CheckBox temp = (CheckBox)v;
		if (temp.isChecked() == true)
		{
			string_append("legs");
			query_combine();
			app.con.readQuery(query_final, lv, layout);

		}
		
		if (temp.isChecked() == false)
		{
			string_remove("legs");
			query_combine();
			app.con.readQuery(query_final, lv, layout);

		}
	}
	
	void display_exercises_toggle_shoulders(View v)
	{
		CheckBox temp = (CheckBox)v;
		if (temp.isChecked() == true)
		{
			string_append("shoulders");
			query_combine();
			app.con.readQuery(query_final, lv, layout);

		}
		
		if (temp.isChecked() == false)
		{
			string_remove("shoulders");
			query_combine();
			app.con.readQuery(query_final, lv, layout);

		}
	}

}

