package com.example.dyel_net;

import android.app.Activity;

public class RoutineView extends Activity {
	public static void getWeeks(){
		String query = "SELECT * FROM schedule_week";
	}
	
	public static void getDays(int weekID){
		String query = "SELECT * FROM schedule_days WHERE routineID=" + weekID;
	}
	
	public static void getSets(int dayID){
		String query = "SELECT * FROM _sets WHERE dayID=" + dayID;
	}
}
