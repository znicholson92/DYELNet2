package com.example.dyel_net;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Stack;

import android.R.layout;
import android.os.Bundle;
import android.os.Parcelable;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.app.*;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity {

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		if(savedInstanceState != null)
		{
			//workout = (Workout)savedInstanceState.get("workout");
			//routineView = (RoutineView)savedInstanceState.get("routineView");
			//con = (connection)savedInstanceState.get("con");
			//cache = (Cache)savedInstanceState.get("cache");
			//previous_layouts = (Stack<Integer>)savedInstanceState.get("previous_layouts");
			//current_layout = (Integer)savedInstanceState.getInt("current_layout");
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  super.onSaveInstanceState(savedInstanceState);
	  // Save UI state changes to the savedInstanceState.
	  // This bundle will be passed to onCreate if the process is
	  // killed and restarted.
	  //savedInstanceState.putParcelable("workout", (Parcelable) workout);
	  //savedInstanceState.putParcelable("routineView", (Parcelable) routineView);
	  //savedInstanceState.putParcelable("con", (Parcelable)con);
	  //savedInstanceState.putParcelable("cache", (Parcelable)cache);
	  //savedInstanceState.putParcelable("previous_layouts", (Parcelable)previous_layouts);
	  //savedInstanceState.putInt("current_layout", (int)current_layout);
	  // etc.
	  
	}


	  

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**************MACRO CONSTANTS***************************/
	public final long DBL_CLICK_THRESHOLD = 600;  //in milliseconds
	
	/**************GLOBAL VARIABLES**************************/
	public connection con;
	Stack<Integer> previous_layouts = new Stack<Integer>();
	Integer current_layout;
	boolean locked = false;
	
	/**************PROCESS CLASSES***************************/
	public Workout workout;
	public RoutineView routineView;
	public AddRoutine addRoutine;
	public display_exercises exerciseViewer;
	public Goal goal = null;
	public Cache cache;
	public RoutineGenerator routineGenerator;
	
	public void login(View v)
	{
		TextView invalid = (TextView)findViewById(R.id.invalidlogin);
		
		ProgressDialog pd = null ;
		pd = ProgressDialog.show(this, "Loading", "Logging in...");
		
		EditText un_box = (EditText) findViewById(R.id.usernameText);
		EditText pw_box = (EditText) findViewById(R.id.passwordText);

		con = new connection(un_box.getText().toString(), pw_box.getText().toString(), this);
		
		while(con.working()){}
		
		for(int i=0; i < 5; ++i)
		{	
			if(con.loggedin())
			{
				un_box.setText("");
				pw_box.setText("");
				invalid.setVisibility(View.INVISIBLE);
				if (pd != null)
					pd.cancel();
				
				setContentView(R.layout.main_menu);
				TextView username_bar = (TextView) findViewById(R.id.username);
				username_bar.setText(un_box.getText().toString());
				cache = new Cache(this);
				loadCache();
				return;
			}
			else
			{	
	            try {Thread.sleep(500);} 
	            catch (InterruptedException e) {e.printStackTrace();}
			}
		}
		
	    
		invalid.setVisibility(View.VISIBLE);
		un_box.setText("");
		pw_box.setText("");
		if (pd != null)
			pd.cancel();
		
	}
	
	public void logout(View v)
	{
		con.logout();
		TextView username_bar = (TextView) findViewById(R.id.usernameText);
		username_bar.setText("");
		gotoLayout(R.layout.login);
	}
	
	/***************MAIN GOTO RELAY**************************/

	
	@SuppressWarnings("unused")
	public void clickedListItem(View v)
	{
		if(locked)
			return;
		
		LinearLayout L = (LinearLayout)v.getParent();
		L.setBackgroundColor(0xFF5D65F5);	//highlight row
		
		TextView tv = (TextView)v;
		
		locked = true;
		Log.w("CLI", "clicked");
		switch(current_layout)
		{
			case R.layout.workingout:
				cli_workingout(tv);
            	break;
				
			case R.layout.routine_view:
				cli_routine_view(tv);
            	break;
          
			case R.layout.workout_history:
				cli_history(tv);
				break;
				
			case R.layout.display_exercises:
				cli_display_exercises(tv);
				break;
			case R.layout.goal_view:
				cli_display_goal(tv);
				break;	
		}
		
		locked = false;
		
	}


	private long lastClickedTime = (long) 0;
	private View lastClickedItem = null;
	
	private boolean checkDoubleClick(View v)
	{
		//determine if there was a double click
		long timestamp = System.currentTimeMillis();
		if((timestamp - lastClickedTime < DBL_CLICK_THRESHOLD) && (lastClickedItem == v))
		{
			return true;
		}
		else
		{
			lastClickedItem = v;
			lastClickedTime = timestamp;
			return false;
		}
	}
	
	/****************CLICKED LIST ITEM************************/
	
	private void cli_history(TextView TV)
	{
		if(HistoryViewer.status == 1)
		{
			LinearLayout L = (LinearLayout)TV.getParent();
			TextView datetimeTV = (TextView)L.getChildAt(1);
			String datetime = datetimeTV.getText().toString();
			HistoryViewer.viewHistory_session(datetime, HistoryViewer.currentExercise, HistoryViewer.currentDayID, this);
		}	
	}
	
	private void cli_display_exercises(TextView TV)
	{
		LinearLayout LL = (LinearLayout)TV.getParent();
		TextView TV1 = (TextView) LL.getChildAt(0); //exercise name
		TextView TV2 = (TextView) LL.getChildAt(4); //exerciseID
		
		String exercise_name = TV1.getText().toString();
		String exerciseID = TV2.getText().toString();
		
		switch(exerciseViewer.getPrevLayout()){
		
			case R.layout.routine_view:
				if(addRoutine != null && addRoutine.running){
					addRoutine.openAddRoutineDay(TV1.getText().toString(), TV2.getText().toString());
				}
				else{
					routineView.openAddNewSet(TV1.getText().toString(), TV2.getText().toString());
				}
				break;
				
			case R.layout.routine_generator:
				String day = routineGenerator.getCurrentDay();
				Log.w("DAY", day);
				routineGenerator.addSet(day, exercise_name);
				restore_routine_generator();
				break;
		
		}
	}
	
	private void cli_workingout(TextView TV)
	{
		String status = workout.getStatus();
		if(status == "session")
		{
			cli_workingout_session(TV);
		}
		else if (status == "exercise")
		{
			cli_workingout_exercise(TV);
		}
	}
	
	
	@SuppressLint("UseValueOf")
	private void cli_workingout_session(TextView TV)
	{	
		LinearLayout L = (LinearLayout)TV.getParent();
		TV = (TextView)L.getChildAt(0);
		String exercise_name = TV.getText().toString();
		TV = (TextView)L.getChildAt(4);
		String exerciseID = TV.getText().toString();
		int ind = L.indexOfChild((View)TV);
		workout.viewExercise(exercise_name, exerciseID, ind);
	}
	
	@SuppressLint("UseValueOf")
	private void cli_workingout_exercise(TextView TV)
	{
		Log.w("SETID", "GOT HERE");
		LinearLayout L = (LinearLayout)TV.getParent();
		ListView LV = (ListView)L.getParent();
		int sn = LV.indexOfChild((View)L) + 1;
		TV = (TextView)L.getChildAt(4);
		String setID = TV.getText().toString();
		Log.w("SETID=", setID);
		workout.addRealSet(new Integer(sn).toString(), setID);
	}
	
	public void routineViewAdd(View V){
		String status = routineView.getStatus();
		gotoLayout(R.layout.add_routine);
	}
	
	public void routine_add(View v)
	{
		addRoutine = new AddRoutine(this);
		Button tv = (Button)v;
		Log.w("BUTTON TEXT", tv.getText().toString());
		int routineID = -1;
		if(tv.getText().toString().equals("Add") ){
			routineID = addRoutine.addRoutine();
		}
		if(routineID >= 0){
			TextView weekTV = (TextView)findViewById(R.id.add_routine_num_weeks);
			TextView dayTV = (TextView)findViewById(R.id.add_routine_num_days);
			
			int weeks = Integer.parseInt(weekTV.getText().toString());
			int days = Integer.parseInt(dayTV.getText().toString());
			for(int x=1; x<=weeks;x++){
				String query = "INSERT INTO schedule_week(week,routineID) VALUES("+x+","+routineID+")";
				con.writeQuery(query);
			}
			addRoutine.getWeekIDs(routineID);
			gotoLayout(R.layout.add_routine_day);
			TextView header = (TextView)findViewById(R.id.day_name);
			header.setText("Week: 1 Day: 1");
		}
		else{
			setContentView(R.layout.routine_view);
		}
	}
	
	public void addRoutineDayExercise(View V){
		addRoutine.addExercise();
	}
	
	public void addRoutineDayExerciseSet(View V){
		addRoutine.addSet();
	}
	
	public void addRoutineDay(View V){
		addRoutine.addDay();
	}
	
	// TODO
	private void cli_routine_view(TextView TV)
	{
		String status = routineView.getStatus();
 		
		if(status == "routines") {
			cli_routineView_routines(TV);
		}
		else if(status == "weeks"){
			cli_routineView_weeks(TV);
		}
		else if (status == "days"){
			cli_routineView_days(TV);
		}
		else if (status == "exercises"){
			cli_routineView_exercises(TV);
		}
		else if (status == "sets") {
			cli_routineView_sets(TV);
		}
	}
	
	private void cli_routineView_days(TextView TV) {
		LinearLayout LL = (LinearLayout)TV.getParent();
		TV = (TextView)LL.getChildAt(4);
		String dID = TV.getText().toString();
		TV = (TextView)LL.getChildAt(0);
		String name = TV.getText().toString();
		routineView.viewExercises(dID, name);
		workoutSliderShowStart();
	}

	private void cli_routineView_weeks(TextView TV) {
		LinearLayout LL = (LinearLayout)TV.getParent();
		TV = (TextView)LL.getChildAt(4);
		routineView.viewDays(TV.getText().toString());
		workoutSliderHideAll();
	}

	private void cli_routineView_routines(TextView TV){
		LinearLayout L = (LinearLayout)TV.getParent();
		TV = (TextView)L.getChildAt(4);
		String routineID = TV.getText().toString();
		TV = (TextView)L.getChildAt(0);
		String name = TV.getText().toString();
		Log.w("ROUTINE ID", routineID);
		routineView.viewWeeks(name, routineID);
		workoutSliderHideAll();
	}

	private void cli_display_goal(TextView TV) {
		LinearLayout L = (LinearLayout)TV.getParent();
		TV = (TextView)L.getChildAt(1);
		String goalName = TV.getText().toString();
		Log.w("GOAL NAME", goalName);
		setContentView(R.layout.goal_view_detail);
		try {
			goal.viewGoalDetail(goalName);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	private void cli_routineView_exercises(TextView TV){
		LinearLayout ll = (LinearLayout)TV.getParent();
		TV = (TextView)ll.getChildAt(1);
		routineView.viewSets(TV.getText().toString());
		workoutSliderShowStart();
	}
	
	private void cli_routineView_sets(TextView TV){
		LinearLayout L = (LinearLayout)TV.getParent();
		ListView LV = (ListView)L.getParent();
		int sn = LV.indexOfChild((View)L) + 1;
		TV = (TextView)L.getChildAt(4);
		String setID = TV.getText().toString();
		routineView.updateSet(Integer.toString(sn), setID);
	}
		
	/***************NAVIGATION FUNCTIONALITY*****************/
	public void gotoBack(View v)
	{	Log.w("BACK", "gotoBACK");
		//TODO needs work, back functionality sucks
		if(current_layout == R.layout.workingout && workout.getStatus() == "exercise")
		{
			workout.goBack();
		}
		else if(current_layout == R.layout.workout_history && workout.getStatus() == "history")
		{
			HistoryViewer.goBack(this);
		}
		else if(!previous_layouts.isEmpty())
		{
			current_layout = previous_layouts.pop();
			setContentView((int)current_layout);
		}
	}

	public void gotoLayout(int layout)
	{
		if(current_layout != null)
			previous_layouts.push(current_layout);
		
		setContentView(layout);
		current_layout = (Integer)layout;
	}
	
	public void gotoLogin(View v)
	{
		current_layout = R.layout.login;
		setContentView(R.layout.login);
	}
	
	public void gotoGoal(View v)
	{
		//gotoLayout(R.layout.create_goal);
		goal = new Goal(this, con.username());
		goal.viewGoal();
		
	}
	
	public void gotoAddRoutine(View v){
		gotoLayout(R.layout.add_routine);
	}
	
	public void gotoAddRoutine_Pre(View v){
		gotoLayout(R.layout.add_routine_pre);
	}
	
	public void gotoUserData(View v)
	{
		gotoLayout(R.layout.userdata);
		userdata_load(false);
	}
	
	public void gotoMenu(View v)
	{
		previous_layouts.clear();
		setContentView(R.layout.main_menu);
		current_layout = R.layout.main_menu;
	}
	
	public void gotoCreateUser(View v)
	{
		gotoLayout(R.layout.createuser);
	}
	
	public void gotoSettings(View v)
	{
		gotoLayout(R.layout.settings);
		loadUserInfo();
	}
	
	public void gotoWorkingOut_Routine(View v)
	{
		gotoLayout(R.layout.workingout);
	}
	
	public void gotoRoutineView(View v){
		routineView = new RoutineView(this);
		routineView.viewRoutines();
	}

	public void gotoWorkout(View v)
	{
		if(Workout.isRunning(workout)){
			workout.viewSession();
		} else {
			
		}

	}
	
	public void gotoCreateGoal(View v)
	{
		gotoLayout(R.layout.create_goal);
	}	
	
	/****************TESTING METHODS*************************/
	public void connectToDatabase(View v)
	{
        
	}

	public void gotoTestApp(View v)
	{
		gotoLayout(R.layout.activity_main);
	}

	public void gotoTestWorkout(View v)
	{	
		gotoLayout(R.layout.workingout);
		//workout = new Workout(this, "1", "Back Day");
		//workout.viewSession();
	}
	
	
	/****************WORKING OUT METHHODS********************/
	
	
	public void set_update(View v)
	{
		Button tv = (Button)v;
		String buttontext = tv.getText().toString();
		
		boolean from_workout = false;
		if(Workout.isRunning(workout)){
			from_workout = workout.isEditingSet();
		}

		if(from_workout){
			if(buttontext.equals("Update") ){
			workout.insertRealSet();
			} else if (buttontext.equals("Cancel")){
				workout.cancelInsertSet();
			} else if (buttontext.equals("Delete")){
				workout.cancelInsertSet();
		}
		
		workout.goBack();
		
		workoutSliderShowFinish();
		} else {
			if(buttontext.equals("Update") ){
				routineView.submitUpdateSet();
			} else if (buttontext.equals("Delete")){
				routineView.deleteSet();
	}
			setContentView(R.layout.routine_view);
			routineView.goBack();
			workoutSliderShowStart();
		}
	}
	
	public void set_add(View v)
	{
		Button tv = (Button)v;
		Log.w("BUTTON TEXT", tv.getText().toString());
		if(tv.getText().toString().equals("Add") ){
			routineView.addSet();
		}
		
		if(Workout.isRunning(workout))
		{
			setContentView(R.layout.workingout);
			workout.goBack();
		} 
		else if (RoutineView.isRunning(routineView)) 
		{
			setContentView(R.layout.routine_view);
			routineView.goBack();
		}
		
		workoutSliderShowFinish();
		
	}
	
	public void browse_exercises(View v)
	{
		exerciseViewer = new display_exercises(this, R.layout.routine_view);
		exerciseViewer.load();
	}
	
	public void exerciseViewerChanged(View v)
	{
		exerciseViewer.load();
	}
	
	/****************WORKOUT SLIDER METHODS******************/
	
	public void clearDones(View v){
		
		if(current_layout == R.layout.routine_view){
			
			String status = routineView.getStatus();
			String SQL = null;
					
			if(status == "weeks")
			{
				SQL = "UPDATE schedule_week SET finished=0 WHERE routineID=" + routineView.getRoutineID();
				con.writeQuery(SQL);
				
				SQL = "UPDATE schedule_daySET finished=0 WHERE routineID=" + routineView.getRoutineID();
				con.writeQuery(SQL);
				
				String INNER_SQL = "SELECT schedule_day.dayID FROM schedule_day WHERE routineID=" + routineView.getRoutineID();
				SQL = "UPDATE _set SET finished=0 WHERE _set.dayID IN(" + INNER_SQL + ")";
			}
			else if (status == "days")
			{
				SQL = "UPDATE schedule_day SET finished=0 WHERE weekID=" + routineView.getWeekID();
				con.writeQuery(SQL);
				
				String INNER_SQL = "SELECT schedule_day.dayID FROM schedule_day WHERE weekID=" + routineView.getWeekID();
				SQL = "UPDATE _set SET finished=0 WHERE _set.dayID IN(" + INNER_SQL + ")";
			}
			else if (status == "exercises")
			{
				SQL = "UPDATE _set ";
				SQL += "SET finished=0 WHERE dayID=" + routineView.getDayID();
			}
			if (status == "sets") 
			{
				String INNER_SQL = routineView.getSQL();
				INNER_SQL = "SELECT _set.setID " + INNER_SQL.substring(40);
				SQL = "UPDATE _set ";
				SQL += "SET finished=0 WHERE setID IN(" + INNER_SQL + ")";
			}
			
			con.writeQuery(SQL);
			
		}
		
	}
	
	public void workoutSliderHideAll()
	{
		findViewById(R.id.workingout_startworkout_button).setVisibility(View.INVISIBLE);
		findViewById(R.id.workingout_finishworkout_button).setVisibility(View.INVISIBLE);
		findViewById(R.id.workingout_deleteworkout_button).setVisibility(View.INVISIBLE);
	}
	
	public void workoutSliderShowStart()
	{
		findViewById(R.id.workingout_startworkout_button).setVisibility(View.VISIBLE);
		findViewById(R.id.workingout_finishworkout_button).setVisibility(View.INVISIBLE);
		findViewById(R.id.workingout_deleteworkout_button).setVisibility(View.INVISIBLE);
	}
	
	public void workoutSliderShowFinish()
	{
		findViewById(R.id.workingout_startworkout_button).setVisibility(View.INVISIBLE);
		findViewById(R.id.workingout_finishworkout_button).setVisibility(View.VISIBLE);
		findViewById(R.id.workingout_deleteworkout_button).setVisibility(View.VISIBLE);
	}
	
	public void startWorkout(View v)
	{
		if(routineView.getStatus() == "exercises" || routineView.getStatus() == "sets")
		{
			String dayID = routineView.getDayID();
			String name = routineView.getDayName();
			workout = new Workout(this, dayID, name);
			workout.viewSession();
			workoutSliderShowFinish();
		}
	}
	
	public void finishWorkout(View v)
	{
		workout.finish();
		workoutSliderHideAll();
		gotoLayout(R.layout.routine_view);
	}
	
	public void deleteWorkout(View v)
	{
		workout.cancel();
		workoutSliderHideAll();
		gotoLayout(R.layout.routine_view);
	}

	public void workout_addNewSet(View v)
	{
		routineView.openAddNewSet();
	}
	
	public void workout_viewHistory(View v)
	{
		if(Workout.isRunning(workout)){
			workout.viewHistory();
		} else {
			routineView.viewHistory();
		}
	}
	
	/****************SETTINGS METHODS************************/
	public void loadUserInfo()
	{
		EditText firstname = (EditText)findViewById(R.id.settings_firstname);
		EditText lastname = (EditText)findViewById(R.id.settings_lastname);
		EditText dateofbirth = (EditText)findViewById(R.id.settings_dateofbirth);
		RadioButton sexM = (RadioButton)findViewById(R.id.settings_sexM);
		RadioButton sexF = (RadioButton)findViewById(R.id.settings_sexF);
		
		String JSONstring = con.readQuery("SELECT * FROM user WHERE username='" + con.username() + "'");
		JSONObject jsonObject;

		if(JSONstring.length() > 10)
		{
			try 
			{
		        
				jsonObject = new JSONObject(JSONstring);
				JSONArray jArray = jsonObject.getJSONArray("data");
				JSONObject j = jArray.getJSONObject(0);
				
				firstname.setText(j.get("firstname").toString());
				lastname.setText(j.get("lastname").toString());
				dateofbirth.setText(j.get("dateofbirth").toString());
				
				if(j.get("sex").toString().equalsIgnoreCase("M"))
					sexM.setChecked(true);
				else
					sexF.setChecked(true);
			
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		LinearLayout changes_bar = (LinearLayout) findViewById(R.id.settings_changesbar);
		changes_bar.setVisibility(View.INVISIBLE);
		
		TextWatcher changebarWatcher = new TextWatcher() {
			 
			@Override
		   public void afterTextChanged(Editable s) { 
		   }
		 
		   public void beforeTextChanged(CharSequence s, int start, 
		     int count, int after) {
		   }
		 
		   public void onTextChanged(CharSequence s, int start, 
		     int before, int count) {
			   LinearLayout changes_bar = (LinearLayout) findViewById(R.id.settings_changesbar);
			   changes_bar.setVisibility(View.VISIBLE);
		   }
		  };
		  
	  firstname.addTextChangedListener(changebarWatcher);
	  lastname.addTextChangedListener(changebarWatcher);
	  dateofbirth.addTextChangedListener(changebarWatcher);
	  sexM.addTextChangedListener(changebarWatcher);
	  sexF.addTextChangedListener(changebarWatcher);
	  
	}
	
	public void saveUserInfo(View v)
	{
		Button b = (Button)v;
		
		EditText firstname = (EditText)findViewById(R.id.settings_firstname);
		EditText lastname = (EditText)findViewById(R.id.settings_lastname);
		EditText dateofbirth = (EditText)findViewById(R.id.settings_dateofbirth);
		RadioButton sexM = (RadioButton)findViewById(R.id.settings_sexM);
		RadioButton sexF = (RadioButton)findViewById(R.id.settings_sexF);
		
		if(b.getText().toString() != "Cancel")
		{
			String sex;
			if(sexM.isChecked())
				sex = "M";
			else
				sex = "F";
			
			String SQL = "UPDATE user SET " + 
						 "firstname='" + firstname.getText().toString() + "'," + 
						 "lastname='" + lastname.getText().toString() + "'," + 
						 "dateofbirth='" + dateofbirth.getText().toString() + "'," + 
						 "sex='" + sex + "'," + 
						 "WHERE username='" + con.username() + "'" ;
					
			con.writeQuery(SQL);
		}
		else
		{
			loadUserInfo();
		}
		LinearLayout changes_bar = (LinearLayout) findViewById(R.id.settings_changesbar);
		changes_bar.setVisibility(View.INVISIBLE);
	}
	
	public void changedUserInfo(View v)
	{
		LinearLayout changes_bar = (LinearLayout) findViewById(R.id.settings_changesbar);
		
		if(changes_bar.getVisibility() == View.INVISIBLE)
			changes_bar.setVisibility(View.VISIBLE);
	}
	
	/****************USER DATA METHODS***********************/
	
	//Added input parameter isGoal by Suna
	public boolean userdata_load(boolean isGoal)
	{
		EditText bodyfat = (EditText)findViewById(R.id.userdata_bodyfat);
		EditText restinghr = (EditText)findViewById(R.id.userdata_restinghr);
		EditText weight = (EditText)findViewById(R.id.userdata_weight);
		EditText notes = (EditText)findViewById(R.id.userdata_notes);
		
		if(isGoal){
			bodyfat = (EditText)findViewById(R.id.goal_userdata_bodyfat);
			restinghr = (EditText)findViewById(R.id.goal_userdata_restinghr);
			weight = (EditText)findViewById(R.id.goal_userdata_weight);
			notes = (EditText)findViewById(R.id.goal_userdata_notes);
		}	
		String SQL =  "SELECT * FROM userdata WHERE " + 
					  "username='" + con.username() + "'" +
					  //"AND isGoal=" + false + " " + 
					  "AND isGoal=" + isGoal + " " +
					  "ORDER BY datetime DESC";
		Log.w("SQL", SQL);
		String JSONstring = con.readQuery(SQL);
		JSONObject jsonObject;
		
		if(JSONstring.length() > 10)
		{
			try 
			{
				jsonObject = new JSONObject(JSONstring);
				JSONArray jArray = jsonObject.getJSONArray("data");
				JSONObject j = jArray.getJSONObject(0);
				
				bodyfat.setText(j.get("bodyfat").toString());
				restinghr.setText(j.get("restingHR").toString());
				weight.setText(j.get("weight").toString());
				notes.setText(j.get("notes").toString());
			
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}else{
			return false;
		}
		
		LinearLayout changes_bar = (LinearLayout) findViewById(R.id.userdata_changesbar);
		if(isGoal){
			changes_bar = (LinearLayout) findViewById(R.id.goal_userdata_changesbar);
		}
		changes_bar.setVisibility(View.INVISIBLE);
		
		TextWatcher watcher = new TextWatcher() {
			 
			@Override
		   public void afterTextChanged(Editable s) {
			   
		   }
		 
		   public void beforeTextChanged(CharSequence s, int start, 
		     int count, int after) {
		   }
		 
		   public void onTextChanged(CharSequence s, int start, 
		     int before, int count) {
			   LinearLayout changes_bar = (LinearLayout) findViewById(R.id.userdata_changesbar);
			   changes_bar.setVisibility(View.VISIBLE);
		   }
		  };
		  
		  if(isGoal){
			  watcher = new TextWatcher() {
					 
					@Override
				   public void afterTextChanged(Editable s) {
					   
				   }
				 
				   public void beforeTextChanged(CharSequence s, int start, 
				     int count, int after) {
				   }
				 
				   public void onTextChanged(CharSequence s, int start, 
				     int before, int count) {
					   LinearLayout changes_bar = (LinearLayout) findViewById(R.id.goal_userdata_changesbar);
					   changes_bar.setVisibility(View.VISIBLE);
					   
				   }
				  };
		  }		  
		bodyfat.addTextChangedListener(watcher);
		restinghr.addTextChangedListener(watcher);
		weight.addTextChangedListener(watcher);
		notes.addTextChangedListener(watcher);
		if(isGoal){
			changes_bar.setVisibility(View.VISIBLE);
		}
		return true;
	}
	
	public void userdata_update(View v)
	{
		Button b = (Button)v;
		
		EditText bodyfat = (EditText)findViewById(R.id.userdata_bodyfat);
		EditText restinghr = (EditText)findViewById(R.id.userdata_restinghr);
		EditText weight = (EditText)findViewById(R.id.userdata_weight);
		EditText notes = (EditText)findViewById(R.id.userdata_notes);
		
		if(b.getText().toString() != "Cancel")
		{
			String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
			Log.w("timeStamp", timeStamp);
			String SQL = "INSERT INTO userdata (username, datetime, bodyfat, restingHR, weight, notes, isGoal) " + 
						 "VALUES(" + 
						 "'" + con.username() + "'," +
						 "'" + timeStamp + "'," +
						 "'" + bodyfat.getText().toString() + "'," + 
						 "'" + restinghr.getText().toString() + "'," + 
						 "'" + weight.getText().toString() + "'," + 
						 "'" + notes.getText().toString() + "'," + 
						 false + ")"; 

			con.writeQuery(SQL);
		}
		else
		{
			userdata_load(false);
		}
		LinearLayout changes_bar = (LinearLayout) findViewById(R.id.userdata_changesbar);
		changes_bar.setVisibility(View.INVISIBLE);
	}
	
	public void userdata_changed(View v)
	{
		LinearLayout changes_bar = (LinearLayout) findViewById(R.id.userdata_changesbar);
		
		if(changes_bar.getVisibility() == View.INVISIBLE)
			changes_bar.setVisibility(View.VISIBLE);
	}
	
	public boolean check_for_exists(String username)
	{
		String sql_query = "SELECT username FROM user WHERE username = '" + username +"'";
		
		connection _con = new connection("dyel-net_admin", "teamturtle", this);
		
		String result = _con.readQuery(sql_query);
		
		if (result.length() > 3) //user exists already
			return true;
		
		return false;
	}
	
	
	/***************REGISTRATION METHODS*********************/
    public void CreateUser(View view)
    {

        EditText userNameET = (EditText) findViewById(R.id.createuser_edit_username);
        EditText passwordET = (EditText) findViewById(R.id.createuser_edit_password);                
        EditText firstNameET = (EditText) findViewById(R.id.createuser_edit_firstName);
        EditText lastNameET = (EditText) findViewById(R.id.createuser_edit_lastName);
        EditText dateOfBirthETy = (EditText) findViewById(R.id.createuser_dateofbirth_year);
        EditText dateOfBirthETm = (EditText) findViewById(R.id.createuser_dateofbirth_month);
        EditText dateOfBirthETd = (EditText) findViewById(R.id.createuser_dateofbirth_day);
        RadioButton sexM = (RadioButton) findViewById(R.id.createuser_radiobuttonM);
        RadioButton sexF = (RadioButton) findViewById(R.id.createuser_radiobuttonF);
        
        String username = userNameET.getText().toString();
        String password = passwordET.getText().toString();
        String firstName = firstNameET.getText().toString();
        String lastName = lastNameET.getText().toString();
        String DOB = dateOfBirthETy.getText().toString()+"-"+dateOfBirthETm.getText().toString()+"-"+dateOfBirthETd.getText().toString();
        String sex = null;
        
        //get sex
        if(sexM.isChecked())
        	sex = "M";
        else if (sexF.isChecked())
        	sex = "F";

        if(username == "" || password == "" || firstName == "" || lastName == "" || DOB == "" || sex == null)
        {
        	showDialog("Missing Fields");
        	return;
        }
        
        if (check_for_exists(username))
        {
        	showDialog("Username already exists.");
        	return;
        }
        
        
        String SQL = "INSERT INTO  `dyel-net_main`.`user` "
                        +"(`username` , `password`, `firstname` , `lastname` , `dateofbirth` , `sex`)"
                        +"VALUES ( "
                        +"'"+username+"', "
                        +"'"+password+"', "
                        +"'"+firstName+"', "
                        +"'"+lastName+"', "
                        +"'"+DOB+"', "
                        +"'"+sex+"');";
     
        
        connection _con = new connection("dyel-net_admin", "teamturtle", this);
        
        ProgressDialog pd;
        pd = ProgressDialog.show(this, "Loading", "Creating account...");
        
        _con.writeQuery(SQL);
        
        pd.cancel();
        
        _con.logout();
  
        setContentView(R.layout.login);
        
    }
    
    /***********************ROUTINE GENERATOR METHODS*******************************/

    public void gotoRoutineGenerator(View v){

    	gotoLayout(R.layout.routine_generator);
    	routineGenerator = new RoutineGenerator(this);

    }
    
    public void routineGenerator_add_day(View v){
    	int day = routineGenerator.getNumDays();
    	EditText dayName_Text = (EditText)findViewById(R.id.routine_generator_dayname);
    	String dayName = dayName_Text.getText().toString();
    	dayName_Text.setText("");
    	Log.w("Adding day", Integer.toString(day));
    	routineGenerator.addDay(dayName, day);
    	routineGenerator_load();
    }
    
    public void routineGenerator_addexercise(View v)
    {
    	
    	EditText rt_name = (EditText)findViewById(R.id.routine_generator_routinename);
    	EditText num_weeks = (EditText)findViewById(R.id.routine_generator_numweeks);
    	String str_numWeeks = num_weeks.getText().toString();
    	
    	if(str_numWeeks == null)
    		return;
    	
    	LinearLayout LL = (LinearLayout)v.getParent();
    	TextView dayNameTV = (TextView)LL.getChildAt(0);
    	String dayName = dayNameTV.getText().toString();
    	int dayNum = routineGenerator.getDayNum(dayName);
    	
    	Log.w("INDEX", Integer.toString(dayNum));
    	
    	routineGenerator.setCurrentDay(Integer.toString(dayNum));
    	
    	routineGenerator.setNumWeeks(str_numWeeks);
    	routineGenerator.setRoutineName(rt_name.getText().toString());
    	exerciseViewer = new display_exercises(this, R.layout.routine_generator);
    	exerciseViewer.load();
    }
    
    private int routineGenerator_listIndex(View v){
    	LinearLayout LL = (LinearLayout)v.getParent();
    	ExpandableListView parent = (ExpandableListView) LL.getParent();
    	int index = -1;
    	for(index = 0; index < parent.getChildCount(); index++){
    		LinearLayout _LL = (LinearLayout)parent.getChildAt(index);
    		if(_LL.equals(LL)){
    			return index;
    		}
    	}
    	return -1;
    }
    
    public void routineGenerator_renameday(View v)
    {
    	//TODO make simple edittext to rename the day
    }
    
    private void routineGenerator_load()
    {
    	routineGenerator.displayPlan();
    }
    
    private void restore_routine_generator(){
    	setContentView(R.layout.routine_generator);
    	current_layout = R.layout.routine_generator;
    	EditText rt_name = (EditText)findViewById(R.id.routine_generator_routinename);
    	EditText num_weeks = (EditText)findViewById(R.id.routine_generator_numweeks);
    	rt_name.setText(routineGenerator.getRoutineName());
    	num_weeks.setText(routineGenerator.getNumWeeks());
    	routineGenerator_load();
    }
    
    public void routineGenerator_handleClick(View v){
    	LinearLayout LL = (LinearLayout)v.getParent();
    	LinearLayout subLL = (LinearLayout)v;
    	ExpandableListView parent = (ExpandableListView) LL.getParent();
    	TextView dayNameTV = (TextView) subLL.getChildAt(0);
    	String dayName = dayNameTV.getText().toString();
    	
    	int index = routineGenerator.getDayNum(dayName) - 1;
    	
    	if(index > -1)
    		parent.expandGroup(index);
    }
    
    
    public void routineGenerator_generate(View v){
    	
    	EditText rt_name = (EditText)findViewById(R.id.routine_generator_routinename);
    	EditText num_weeks = (EditText)findViewById(R.id.routine_generator_numweeks);
    	String strRtName = rt_name.getText().toString();
    	String strNumWeeks = num_weeks.getText().toString();
    			
    	if(strRtName != "" && strNumWeeks != ""){
	    	ProgressDialog pd;
	        pd = ProgressDialog.show(this, "Loading", "Creating account...");
	    	routineGenerator.go(strRtName, strNumWeeks);	    	
	    	pd.cancel();
    	}
    	
    }


	/***************************************************************/
    /***********************OTHER METHODS***************************/
    /***************************************************************/
	public void showDialog(String text)
	{
		AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(text);
        dialog.show();
	}
	
	public void lockApp(long delay)
	{
		locked = true;
		try {Thread.sleep(delay);} 
		catch (InterruptedException e) {e.printStackTrace();}
		locked = false;
	}
	
	 public void loadCache()
	 {
    	if(!cache.isLoaded())
    	{
    		Thread trd = new Thread(new Runnable(){
    			@Override
    			public void run(){
    				cache.load();
    			}
    		});
    		trd.start();
    	}
	 }

    
    /***********************GOALS METHODS*******************************/
    public void viewSetGoals(View v){
    	gotoLayout(R.layout.goal_edit_set);
    	if(goal != null){
    		goal.viewSetGoals();
    	}
    }
    public void viewUserDataGoals(View v){
       	gotoLayout(R.layout.goal_userdata); 
       	userdata_load(true);  //isGoal == true
    }
    /**
     * Create a goal summary for the specific user.
     * @param v
     */
    public void createGoal(View v){
    	if(goal.isRunning()){
    		goal.createGoal();
    	}
    	goal.viewGoal();
    }
    
    /**
     * Delete the chosen goal for the specific user.
     * @param v
     */
    public void deleteGoal(View v){
    	String currentGoalName;
    	if(goal.isRunning()){
    		if(GoalViewer.status == 2){
    			currentGoalName = GoalViewer.getCurrentGoalName();    			
    			goal.deleteGoal(currentGoalName);
    		}
    	}
    	goal.viewGoal();
    }
}