package com.example.dyel_net;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;

public class display_exercises {
	
	String query_final;
	ArrayList<String> muscle_groups = new ArrayList<String>();
	String muscle_groups_str = "";
	MainActivity app;
	ListView lv;
	LinearLayout ch;
	
	public display_exercises(MainActivity a)
	{
		app = a;	
		app.gotoLayout(R.layout.display_exercises);
		lv = (ListView) app.findViewById(R.id.display_exercises_listview);
		ch = (LinearLayout) app.findViewById(R.id.display_exercise_col_header);
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
			String item = muscle_groups.get(iter);
			if (item == s)
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
		query_final = "SELECT DISTINCT exercise.exerciseID, exercise.name AS 'Name', muscle.musclegroup As 'Muscle Group' from exercise " +
					  "INNER JOIN muscle2exercise ON muscle2exercise.exerciseID = exercise.exerciseID " + 
					  "INNER JOIN muscle ON muscle2exercise.muscleID = muscle.muscleID " +
					  "WHERE "
					  + muscle_groups_str +
					  " ORDER BY muscle.musclegroup ASC";
		return;
	}
	
	String list_to_string()
	{
		String list_string = "";
		
		for (String s : muscle_groups)
		{
			list_string += "musclegroup = '" + s + "' OR ";
		}

		if(list_string.length() > 4)
			list_string = list_string.substring(0, list_string.length() - 4);
		
		return list_string;
	}
	
	private void toggle_forearms(CheckBox temp)
	{
		if (temp.isChecked() == true)
		{
			string_append("Forearms");
			//query_combine();
		}
		
		if (temp.isChecked() == false)
		{
			string_remove("Forearms");
			//query_combine();
		}
	}
	
	private void toggle_arms(CheckBox temp)
	{
		if (temp.isChecked() == true)
		{
			string_append("Arms");
		}
		
		if (temp.isChecked() == false)
		{
			string_remove("Arms");
		}
	}
	
	private void toggle_chest(CheckBox temp)
	{
		if (temp.isChecked() == true)
		{
			string_append("Chest");
		}
		
		if (temp.isChecked() == false)
		{
			string_remove("Chest");
		}
	}
	
	private void toggle_back(CheckBox temp)
	{
		if (temp.isChecked() == true)
		{
			string_append("Back");
		}
		
		if (temp.isChecked() == false)
		{
			string_remove("Back");
		}
	}
	
	private void toggle_legs(CheckBox temp)
	{
		if (temp.isChecked() == true) {
			string_append("Legs");
		} else {
			string_remove("Legs");
		}
	}
	
	private void toggle_shoulders(CheckBox temp)
	{
		if (temp.isChecked() == true) {
			string_append("Shoulders");
		} else {
			string_remove("Shoulders");
		}
	}
	
	public void load()
	{
		Log.w("DISP EXERCISES", "LOAD");
		CheckBox checkArms = (CheckBox)app.findViewById(R.id.CheckBox_Arms);
		CheckBox checkForearms = (CheckBox)app.findViewById(R.id.CheckBox_Forearms);
		CheckBox checkChest = (CheckBox)app.findViewById(R.id.CheckBox_Chest);
		CheckBox checkShoulders = (CheckBox)app.findViewById(R.id.CheckBox_Shoulders);
		CheckBox checkLegs = (CheckBox)app.findViewById(R.id.CheckBox_Legs);
		CheckBox checkBack = (CheckBox)app.findViewById(R.id.CheckBox_Back);
		
		toggle_arms(checkArms);
		toggle_forearms(checkForearms);
		toggle_chest(checkChest);
		toggle_shoulders(checkShoulders);
		toggle_legs(checkLegs);
		toggle_back(checkBack);
		
		query_combine();
		
		if(muscle_groups_str.length() > 0 )
			app.cache.Query(query_final, lv, ch);
		
		//if(query_final != "" && app.cache.isLoaded()){
		//app.cache.Query(query_final, lv, ch);
		//} else {
		//	app.loadCache();
		//}
	}

}

