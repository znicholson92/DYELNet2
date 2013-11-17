package com.example.dyel_net;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Stack;

import android.R.layout;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
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
	
	/**************PROCESS CLASSES***************************/
	public Workout workout;
	public RoutineView routineView;
	public display_exercises exerciseViewer;
	
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

	
	private void clickedListItem(View v)
	{
		LinearLayout L = (LinearLayout)v.getParent();
		L.setBackgroundColor(0xFF5D65F5);	//highlight row
		
		TextView tv = (TextView)v;	
		
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
		}
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
		TextView exerciseTV = (TextView)L.getChildAt(1);
		workout.viewExercise(exerciseTV.getText().toString());
	}
	
	@SuppressLint("UseValueOf")
	private void cli_workingout_exercise(TextView TV)
	{
		LinearLayout L = (LinearLayout)TV.getParent();
		ListView LV = (ListView)L.getParent();
		int sn = LV.indexOfChild((View)L) + 1;
		workout.addRealSet(new Integer(sn).toString());
	}
	
	// TODO
	private void cli_routine_view(TextView TV)
	{
		String status = routineView.getStatus();
 		
		if(status == "routines")
		{
			cli_routineView_routines(TV);
		}
		else if(status == "weeks"){
			cli_routineView_days(TV);
		}
		else if (status == "days"){
			cli_routineView_sets(TV);
		}
	}
	
	private void cli_routineView_sets(TextView tV) {
		routineView.viewSets();
	}

	private void cli_routineView_days(TextView tV) {
		routineView.viewDays();
	}

	private void cli_routineView_routines(TextView TV){
		LinearLayout L = (LinearLayout)TV.getParent();
		routineView.viewWeeks();
	}


		
	/***************NAVIGATION FUNCTIONALITY*****************/
	public void gotoBack(View v)
	{	Log.w("BACK", "gotoBACK");
		//TODO add conditional for if in Workout
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
		gotoLayout(R.layout.create_goal);
	}
	
	public void gotoUserData(View v)
	{
		gotoLayout(R.layout.userdata);
		userdata_load();
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
		gotoLayout(R.layout.routine_view);
		routineView.viewRoutines();
	}

	public void gotoWorkout(View v)
	{
		if(Workout.isRunning(workout)){
			workout.viewSession();
		} else {
			
		}

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
		Log.w("BUTTON TEXT", tv.getText().toString());
		if(tv.getText().toString().equals("Update") ){Log.d("SET_UPDATE", "POINT B");
			workout.insertRealSet();
		}
		
		setContentView(R.layout.workingout);
		
		findViewById(R.id.workingout_startworkout_button).setVisibility(View.INVISIBLE);
		findViewById(R.id.workingout_finishworkout_button).setVisibility(View.VISIBLE);
		findViewById(R.id.workingout_deleteworkout_button).setVisibility(View.VISIBLE);
		
		workout.goBack();
	}
	
	public void set_add(View v)
	{
		Button tv = (Button)v;
		Log.w("BUTTON TEXT", tv.getText().toString());
		if(tv.getText().toString().equals("Add") ){
			workout.addSet();
		}
		
		setContentView(R.layout.workingout);
		
		findViewById(R.id.workingout_startworkout_button).setVisibility(View.INVISIBLE);
		findViewById(R.id.workingout_finishworkout_button).setVisibility(View.VISIBLE);
		findViewById(R.id.workingout_deleteworkout_button).setVisibility(View.VISIBLE);
		
		workout.goBack();
	}
	
	public void browse_exercises(View v)
	{
		gotoLayout(R.layout.display_exercises);
		//exerciseViewer = new display_exercises(this);
		//exerciseViewer.load();
	}
	
	public void exerciseViewerChanged(View v)
	{
		//exerciseViewer.load();
	}
	
	
	/****************WORKOUT SLIDER METHODS******************/
	public void startWorkout(View v)
	{
		String dayID = "1";
		workout = new Workout(this, dayID, "Back Day");
		workout.viewSession();
		v.setVisibility(View.INVISIBLE);
		findViewById(R.id.workingout_finishworkout_button).setVisibility(View.VISIBLE);
		findViewById(R.id.workingout_deleteworkout_button).setVisibility(View.VISIBLE);
	}
	
	public void finishWorkout(View v)
	{
		workout.finish();
		v.setVisibility(View.GONE);
		findViewById(R.id.workingout_startworkout_button).setVisibility(View.VISIBLE);
		findViewById(R.id.workingout_deleteworkout_button).setVisibility(View.GONE);
		gotoLayout(R.layout.routine_view);
	}
	
	public void deleteWorkout(View v)
	{
		workout.cancel();
		v.setVisibility(View.GONE);
		findViewById(R.id.workingout_startworkout_button).setVisibility(View.VISIBLE);
		findViewById(R.id.workingout_finishworkout_button).setVisibility(View.GONE);
		gotoLayout(R.layout.routine_view);
	}

	public void workout_addNewSet(View v)
	{
		gotoLayout(R.layout.add_set);
	}
	
	public void workout_viewHistory(View v)
	{
		if(Workout.isRunning(workout)){
			workout.viewHistory();
		} else {
			
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
	public void userdata_load()
	{
		EditText bodyfat = (EditText)findViewById(R.id.userdata_bodyfat);
		EditText restinghr = (EditText)findViewById(R.id.userdata_restinghr);
		EditText weight = (EditText)findViewById(R.id.userdata_weight);
		EditText notes = (EditText)findViewById(R.id.userdata_notes);
		
		String SQL =  "SELECT * FROM userdata WHERE " + 
					  "username='" + con.username() + "'" +
					  "AND isGoal=" + false + " " + 
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

		}
		
		LinearLayout changes_bar = (LinearLayout) findViewById(R.id.userdata_changesbar);
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
		  
		bodyfat.addTextChangedListener(watcher);
		restinghr.addTextChangedListener(watcher);
		weight.addTextChangedListener(watcher);
		notes.addTextChangedListener(watcher);
		
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
			userdata_load();
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
		
		String result = con.readQuery(sql_query);
		
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
        
        if (check_for_exists(username) == true)
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
     
        
        connection con = new connection("dyel-net_admin", "teamturtle", this);
        
        ProgressDialog pd;
        pd = ProgressDialog.show(this, "Loading", "Creating account...");
        
        con.writeQuery(SQL);
		//Thread.sleep(2500);
        
        pd.cancel();
        
        con.logout();
  
        setContentView(R.layout.login);
        
    }

	/***************************************************************/
    /***********************OTHER METHODS***************************/
    /***************************************************************/
	private void showDialog(String text)
	{
		AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(text);
        dialog.show();
	}
}

