package com.example.dyel_net;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.app.*;
import android.widget.EditText;
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
	
	public void gotoLogin(View v)
	{
		setContentView(R.layout.login);
	}
	
	public void gotoMenu(View v)
	{
		setContentView(R.layout.main_menu);
	}
	
	public void gotoCreateUser(View v)
	{
		setContentView(R.layout.createuser);
	}
	
	public void gotoSettings(View v)
	{
		setContentView(R.layout.settings);
		loadUserInfo();
	}
	
	public void gotoTestApp(View v)
	{
		setContentView(R.layout.activity_main);
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
				EditText weight = (EditText)findViewById(R.id.settings_weight);
				EditText bodyfat = (EditText)findViewById(R.id.settings_bodyfat);
				
				firstname.setText(j.get("firstname").toString());
				lastname.setText(j.get("lastname").toString());
				dateofbirth.setText(j.get("dateofbirth").toString());
				sex.setText(j.get("sex").toString());
				//weight.setText(j.get("weight").toString());
				//bodyfat.setText(j.get("bodyfat").toString());
			
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void saveUserInfo(View v)
	{
		EditText firstname = (EditText)findViewById(R.id.settings_firstname);
		EditText lastname = (EditText)findViewById(R.id.settings_lastname);
		EditText dateofbirth = (EditText)findViewById(R.id.settings_dateofbirth);
		EditText sex = (EditText)findViewById(R.id.settings_sex);
		EditText weight = (EditText)findViewById(R.id.settings_weight);
		EditText bodyfat = (EditText)findViewById(R.id.settings_bodyfat);
		
		String SQL = "UPDATE user SET " + 
					 "firstname='" + firstname.getText().toString() + "'," + 
					 "lastname='" + lastname.getText().toString() + "'," + 
					 "dateofbirth='" + dateofbirth.getText().toString() + "'," + 
					 "sex='" + sex.getText().toString() + "'," + 
					 "WHERE username='" + con.username() + "'" ;
				
		con.writeQuery(SQL);
	}
}

