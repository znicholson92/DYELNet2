package com.example.dyel_net;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

//SQLite Cache for remote MySQL
public class Cache {

	private MainActivity app;
	private SQLiteDatabase db;
	private int DATABASE_MODE = Context.MODE_PRIVATE;
	
	private boolean loaded;
	
	private connection con;
	
	public Cache(MainActivity a) {
		app = a;
		File dbFile = app.getDatabasePath("cache.db");
		if(dbFile.exists())	app.deleteDatabase("cache.db");
		db = app.openOrCreateDatabase("cache.db", DATABASE_MODE, null);
		con = new connection(app);
		createTable_exercise();
		createTable_muscle();
		createTable_muscle2exercise();
		makeRoutineCache();
	}
	
	/*****************ROUTINE GENERATOR METHODS***********************************/
	public void makeRoutineCache(){	
		db.execSQL("CREATE TABLE routine(routineHash TEXT, day INT, _set INT, setnumber INT, exerciseID INT)");
		db.execSQL("CREATE TABLE days(routineHash Text, day INT, day_name Text)");
	}
	
	public void addExercise(String routineHash, String day, int sets, String exercise_name){
		
		String exerciseID = getExerciseID(exercise_name);
		String set = Integer.toString(getSetCount(routineHash) + 1);
		for(int s=0; s < sets; s++){
			String sql = "INSERT INTO routine(routineHash, day, _set, setnumber, exerciseID) " +
					 	 "VALUES('" + routineHash + "'," + day + "," + set + "," + Integer.toString(s) + "," + exerciseID + ")";
			db.execSQL(sql);
		}
	}
	
	public void addDay(String routineHash, String day, String day_name){
		
		String sql = " INSERT INTO days(routineHash, day, day_name) " +
					 " VALUES('" + routineHash + "'," + day + ",'" + day_name + "')";
		
		db.execSQL(sql);
	}
	
	private int getSetCount(String routineHash){
		String sql = "SELECT count(*) FROM routine WHERE routineHash='" + routineHash + "'";
		Cursor cursor = db.rawQuery(sql, null);
		return cursor.getInt(0);
	}
	public String getExerciseID(String exercise_name){
		String sql = "SELECT exerciseID FROM exercise WHERE name='" + exercise_name + "'";
		Cursor cursor = db.rawQuery(sql, null);
		String exerciseID = cursor.getString(0);
		return exerciseID;
	}
	
	public int getExerciseID(String routineHash, int day, int set){
		String sql = "SELECT exerciseID FROM routine " + "" +
				     "WHERE routineHash='" + routineHash + "'" +
				     "AND day=" + day +
				     "AND _set=" + set;
		Cursor cursor = db.rawQuery(sql, null);
		return cursor.getInt(0);
	}

	public int getSetNumber(String routineHash, int day, int set){
		String sql = "SELECT setnumber FROM routine " + "" +
				     "WHERE routineHash='" + routineHash + "'" +
				     "AND day=" + day +
				     "AND _set=" + set;
		Cursor cursor = db.rawQuery(sql, null);
		return cursor.getInt(0);
	}
	
	public int getNumberOfSets(String routineHash, int day){
		String sql = "SELECT count(*) FROM routin " +
					 "WHERE routineHash='" + routineHash + "'" +
					 "AND day=" + day;
		Cursor cursor = db.rawQuery(sql, null);
		return cursor.getInt(0);
	}
	
	/*******************CREATE TABLE METHODS***************************/
	private void createTable_set(){
		
		String SQL = "CREATE TABLE _set( " +
					 "setID INT PRIMARY KEY, " +
					 "sessionID INT, " +
					 "dayID INT, " +
					 "exerciseID INT, " +
					 "reps INT, " + 
					 "weight DOUBLE, " +
					 "setnumber INT, " +
					 "notes TEXT, " +
					 "isReal INT, " +
					 "isGoal INT)";
		
		db.execSQL(SQL);
	}
	
	private void createTable_exercise(){
		
		String SQL = "CREATE TABLE exercise( " + 
					 "exerciseID INT, " +
					 "name TEXT )";
		
		db.execSQL(SQL);
	}
	
	private void createTable_muscle(){
		
		String SQL = "CREATE TABLE muscle( " +
					 "muscleID INT, " +
					 "name TEXT, " +
					 "musclegroup TEXT)";
		
		db.execSQL(SQL);
	}
	
	private void createTable_muscle2exercise(){
		
		String SQL = "CREATE TABLE muscle2exercise( " +
					 "muscleID INT, " +
					 "exerciseID INT)";
					
		db.execSQL(SQL);
	}

	
	public void load(){
		
		String sql, jString;
		
		sql = "SELECT * FROM exercise";
		jString = con.readQuery(sql);
		try {
			JSONObject jsonObject = new JSONObject(jString);
			JSONArray jArray = jsonObject.getJSONArray("data");
			for(int i=0; i < jArray.length(); i++){
				JSONObject j = jArray.getJSONObject(i);
				sql = "INSERT INTO exercise(exerciseID, name) VALUES(" +
					   j.get("exerciseID").toString() + ",'" + 
					   j.get("name").toString() + "')";
				db.execSQL(sql);
			}
		} catch (JSONException e) {e.printStackTrace();}

		sql = "SELECT * FROM muscle";
		jString = con.readQuery(sql);
		try {
			JSONObject jsonObject = new JSONObject(jString);
			JSONArray jArray = jsonObject.getJSONArray("data");
			for(int i=0; i < jArray.length(); i++){
				JSONObject j = jArray.getJSONObject(i);
				sql = "INSERT INTO muscle(muscleID, name, musclegroup) VALUES(" +
					   j.get("muscleID").toString() + ",'" + 
					   j.get("name").toString() + "','" +
					   j.get("musclegroup").toString() + "')";
				db.execSQL(sql);
			}
		} catch (JSONException e) {e.printStackTrace();}
		
		sql = "SELECT * FROM muscle2exercise";
		jString = con.readQuery(sql);
		try {
			JSONObject jsonObject = new JSONObject(jString);
			JSONArray jArray = jsonObject.getJSONArray("data");
			for(int i=0; i < jArray.length(); i++){
				JSONObject j = jArray.getJSONObject(i);
				sql = "INSERT INTO muscle2exercise(muscleID, exerciseID) VALUES(" +
					   j.get("muscleID").toString() + ",'" + 
					   j.get("exerciseID").toString() + "')";
				db.execSQL(sql);
			}
		} catch (JSONException e) {e.printStackTrace();}
		
		loaded = true;
		
	}

	public void Query(String sql, ListView listview, LinearLayout col_header){
		
		Log.w("CACHE", sql);
		
		ArrayList<TextView> Columns = new ArrayList<TextView>();
		for(int i = 0; i < col_header.getChildCount(); i++){
			Columns.add((TextView) col_header.getChildAt(i));
		}
		
		ArrayList<HashMap<String, String>> tableList = new ArrayList<HashMap<String, String>>();
		Cursor cursor = db.rawQuery(sql, null);
		while (cursor.moveToNext()) {
			int col = 0;
			HashMap<String, String> map = new HashMap<String, String>();
			for(int i=0; i < cursor.getColumnCount(); i++){
				String key = cursor.getColumnName(i);
		        String value = cursor.getString(i);
		        if(key.contains("ID")){
                	Columns.get(4).setText(key);
                } else {
                	Columns.get(col).setText(key);
                	col++;
                }
		        map.put(key,value);
			}
			tableList.add(map);
		}
		cursor.close();
    	
		SimpleAdapter myAdapter = 
				new SimpleAdapter(app, 
								  tableList, 
								  R.layout.my_list_item,
								  new String[] {Columns.get(0).getText().toString(), 
												Columns.get(1).getText().toString(), 
												Columns.get(2).getText().toString(), 
												Columns.get(3).getText().toString(), 
												Columns.get(4).getText().toString()}, 
								  new int[] {R.id.cell1, R.id.cell2, R.id.cell3, R.id.cell4, R.id.cell5});

		listview.setAdapter(myAdapter);
		
	}
	
	public boolean isLoaded(){
		return loaded;
	}
	

	

	
	public void displayRoutineGeneratorPlan(String routineHash, ExpandableListView listview){
		
		String sql1 =  " SELECT day, day_name FROM days WHERE routineHash='" + routineHash + "' ORDER BY day ASC";
		
		String sql2 = " SELECT routine.day, max(routine.setnumber), exercise.name FROM routine " +
					  " INNER JOIN exercise ON exercise.exerciseID = routine.exerciseID " +
					  " WHERE routineHash='" + routineHash + "' " +
					  " GROUP BY exercise.name " +
					  " ORDER BY routine.day ASC ";
		
		List<String> listDataHeader = new ArrayList<String>();
	    HashMap<String, List<String>> listDataChild = new HashMap<String, List<String>>();				

	    Cursor cursor1 = db.rawQuery(sql1, null);
		Cursor cursor2 = db.rawQuery(sql2, null);
		
		List<List<String>> subLists = new ArrayList<List<String>>();
		
		int c = 0;
		while (cursor1.moveToNext()){
			String day_name = cursor1.getString(1);
			listDataHeader.add(day_name);
			subLists.set(c, new ArrayList<String>());
			listDataChild.put(listDataHeader.get(c), subLists.get(c));
			c++;
		}
		cursor1.close();
		while (cursor2.moveToNext()) {	
			int day = cursor2.getInt(0);
			List<String> subList = subLists.get(day);
			String exercise_name = cursor2.getString(2);
			subList.add(exercise_name);
		}
		cursor2.close();
		
		ExpandableListAdapter listAdapter = new ExpandableListAdapter(app, listDataHeader, listDataChild);
		 
	    listview.setAdapter(listAdapter);

		
	}
	

	
	

}
