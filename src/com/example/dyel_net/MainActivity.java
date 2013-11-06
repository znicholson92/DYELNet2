package com.example.dyel_net;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Stack;

import android.R.layout;
import android.os.Bundle;
import android.app.Activity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.app.*;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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
	
	public connection con;
	public Stack<Integer> previous_layouts;
	public int current_layout;
	
	public void login(View v)
	{
		EditText un_box = (EditText) findViewById(R.id.usernameText);
		EditText pw_box = (EditText) findViewById(R.id.passwordText);

		con = new connection(un_box.getText().toString(), pw_box.getText().toString(), this);

		while(con.working())
		{
			ProgressDialog.show(this, "Loading", "Logging in...");
		}
		
		if(con.loggedin())
		{
			setContentView(R.layout.main_menu);
			TextView username_bar = (TextView) findViewById(R.id.username);
			username_bar.setText(un_box.getText().toString());
		}
		else
		{	
			AlertDialog dialog = new AlertDialog.Builder(this).create();
            dialog.setTitle("Invalid Login");
            dialog.show();
		}
		un_box.setText("");
		pw_box.setText("");
	}
	
	public void logout(View v)
	{
		con.logout();
		TextView username_bar = (TextView) findViewById(R.id.usernameText);
		username_bar.setText("");
		setContentView(R.layout.login);
	}
	
	/***************MAIN GOTO RELAY**************************/
	public void clickedListItem(View v)
	{
		TextView tv = (TextView)v;
		
	}
	
	public void gotoBack(View v)
	{
		if(!previous_layouts.isEmpty())
		{
			current_layout = previous_layouts.pop();
			setContentView(current_layout);
		}
	}
	
	/***************MAIN MENU FUNCTIONALITY******************/
	public void gotoLogin(View v)
	{
		current_layout = R.layout.login;
		setContentView(R.layout.login);
	}
	
	public void gotoUserData(View v)
	{
		previous_layouts.push(R.layout.main_menu);
		setContentView(R.layout.userdata);
		current_layout = R.layout.userdata;
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
		previous_layouts.push(R.layout.login);
		setContentView(R.layout.createuser);
		current_layout = R.layout.createuser;
	}
	
	public void gotoSettings(View v)
	{
		previous_layouts.push(R.layout.main_menu);
		setContentView(R.layout.settings);
		current_layout = R.layout.settings;
		loadUserInfo();
	}
	
	public void gotoTestApp(View v)
	{
		setContentView(R.layout.activity_main);
	}
	
	public void gotoWorkingOut_Routine(View v)
	{
		previous_layouts.push(current_layout);
		current_layout = R.layout.workingout_routine;
	}
	
	public void connectToDatabase(View v)
	{
		ListView listView = (ListView) findViewById(R.id.listView1);
		connection con1 = new connection("dyel-net_admin", "teamturtle", this);
		
        con1.readQuery("select * from muscle", listView);
        
        while(con.working())
		{
			ProgressDialog.show(this, "Loading", "Loading data...");
		}
        
	}

	
	/****************SETTINGS METHODS************************/
	public void loadUserInfo()
	{
		String JSONstring = con.readQuery("SELECT * FROM User WHERE username=" + con.username());
		JSONObject jsonObject;
		if(JSONstring.length() > 10)
		{
			try 
			{
				jsonObject = new JSONObject(JSONstring);
				JSONArray jArray = jsonObject.getJSONArray("data");
				JSONObject j = jArray.getJSONObject(0);
			
				EditText firstname = (EditText)findViewById(R.id.settings_firstname);
				EditText lastname = (EditText)findViewById(R.id.settings_lastname);
				EditText dateofbirth = (EditText)findViewById(R.id.settings_dateofbirth);
				EditText sex = (EditText)findViewById(R.id.settings_sex);
				
				firstname.setText(j.get("firstname").toString());
				lastname.setText(j.get("lastname").toString());
				dateofbirth.setText(j.get("dateofbirth").toString());
				sex.setText(j.get("sex").toString());
			
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void saveUserInfo(View v)
	{
		Button b = (Button)v;
		
		EditText firstname = (EditText)findViewById(R.id.settings_firstname);
		EditText lastname = (EditText)findViewById(R.id.settings_lastname);
		EditText dateofbirth = (EditText)findViewById(R.id.settings_dateofbirth);
		EditText sex = (EditText)findViewById(R.id.settings_sex);
		
		if(b.getText().toString() != "Cancel")
		{
			String SQL = "UPDATE user SET " + 
						 "firstname='" + firstname.getText().toString() + "'," + 
						 "lastname='" + lastname.getText().toString() + "'," + 
						 "dateofbirth='" + dateofbirth.getText().toString() + "'," + 
						 "sex='" + sex.getText().toString() + "'," + 
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
		String SQL =  "SELECT * FROM userdata WHERE " + 
					  "username='" + con.username() + "'" +
					  "AND isGoal=" + false + " " + 
					  "ORDER BY datetime DESC";
					  
		String JSONstring = con.readQuery(SQL);
		JSONObject jsonObject;
		if(JSONstring.length() > 10)
		{
			try 
			{
				jsonObject = new JSONObject(JSONstring);
				JSONArray jArray = jsonObject.getJSONArray("data");
				JSONObject j = jArray.getJSONObject(0);
			
				EditText bodyfat = (EditText)findViewById(R.id.userdata_bodyfat);
				EditText restinghr = (EditText)findViewById(R.id.userdata_restinghr);
				EditText weight = (EditText)findViewById(R.id.userdata_weight);
				EditText notes = (EditText)findViewById(R.id.userdata_notes);
				
				bodyfat.setText(j.get("bodyfat").toString());
				restinghr.setText(j.get("restinghr").toString());
				weight.setText(j.get("weight").toString());
				notes.setText(j.get("notes").toString());
			
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
			
			String SQL = "INSERT INTO userdata VALUES( " + 
						 "bodyfat='" + bodyfat.getText().toString() + "'," + 
						 "restinghr='" + restinghr.getText().toString() + "'," + 
						 "weight='" + weight.getText().toString() + "'," + 
						 "notes='" + notes.getText().toString() + "'," + 
						 "datetime=" + timeStamp + "'," +
						 "isgoal=" + false + ") " + 
						 "WHERE username='" + con.username() + "'";
					
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
        EditText sexET = (EditText) findViewById(R.id.createuser_edit_sex);

        String username = userNameET.getText().toString();
        String password = passwordET.getText().toString();
        String firstName = firstNameET.getText().toString();
        String lastName = lastNameET.getText().toString();
        String DOB = dateOfBirthETy.getText().toString()+"-"+dateOfBirthETm.getText().toString()+"-"+dateOfBirthETd.getText().toString();
        String sex = sexET.getText().toString();
        
        String SQL = "INSERT INTO  `dyel-net_main`.`user` "
                        +"(`username` , `firstname` , `lastname` , `dateofbirth` , `sex`)"
                        +"VALUES ( "
                        +"'"+username+"', "
                        +"'"+firstName+"', "
                        +"'"+lastName+"', "
                        +"'"+DOB+"', "
                        +"'"+sex+"');";
        
        String create_account_query = "CREATE USER '" + username + " '@'engr-cpanel-mysql.engr.illinois.edu' " + 
        							  "IDENTIFIED BY '" + password + "';" +
        							  "grant all privileges on dyel-net.* to '" + username + "'@'engr-cpanel-mysql.engr.illinois.edu' identified by '" + password + "';";
        
        connection con = new connection("dyel-net_admin", "teamturtle", this);
        
        try {
    		ProgressDialog.show(this, "Loading", "Creating account...");
        	con.writeQuery(create_account_query);
        	Thread.sleep(1000);
        	con.writeQuery(SQL);
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        con.logout();
        
        setContentView(R.layout.login);
        
    }
	
	
}

