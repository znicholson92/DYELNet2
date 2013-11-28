package com.example.dyel_net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class connection
{
	private boolean loggedin;
	private boolean working;
	private boolean success;
	private String username;
	private String password;
	private String result = "NULL";
	private MainActivity app;
	private ListView list;
	private LinearLayout col_header;
	private boolean lock;
	
	//constructor, represents the user logging in, tests the connection and either returns success or fails and logs out
	public connection(String un, String pw, MainActivity _app)
	{
		username = un;
		password = pw;
		app = _app;
		working = true;
		try
		{
			_connection task = new _connection();
			task.execute("test", "");
			Thread.sleep(2500);
			if(success)
				loggedin = true;
			else
				loggedin = false;
		} catch (InterruptedException e) {
			loggedin = false;
			e.printStackTrace();
		}
		finally
		{
			working = false;
		}
	}
	
	public connection(MainActivity _app)
	{
		username = "dyel-net_admin";
		password = "team turtle";
		app = _app;
	}
	
	//gets the boolean value of whether or not the async task is running
	public boolean working()
	{
		return working;
	}
	
	//gets the boolean value of whether or not the user is logged in
	public boolean loggedin()
	{
		return loggedin;
	}
	
	//gets the logged in username
	public String username()
	{
		return username;
	}
	
	//send a transaction and write the results to the listview
	public void readQuery(String SQL, ListView l, LinearLayout ch)
	{
		_connection task = new _connection();
		list = l;
		col_header = ch;
		Log.w("CONNECTION", "READING QUERY");
		task.execute("read", SQL, "update");
	}

	//send a transaction and return the results as a JSON string
	public String readQuery(String SQL)
	{
		result = "NULL";
		_connection task = new _connection();
		task.execute("read", SQL, "NO");
		try {Thread.sleep(1000);} 
		catch (InterruptedException e) {e.printStackTrace();}
		while(working){}
		long ti = System.currentTimeMillis();
		long timer = (long)0;
		
		while(result.length() < 5 && timer < (long)20000){
			timer = System.currentTimeMillis() - ti;
		}
		return result;
	}
	
	
	//TODO make it return boolean value if successful or not
	//send a transaction that modifies the database and does not return anything
	public void writeQuery(String SQL)
	{
		_connection task = new _connection();
		task.execute("write", SQL);
		while(working){}
	}
	
	//logs out the current user
	public void logout()
	{
		list = null;
		username = "";
		password = "";
		result = "";
		loggedin = false;
		success = false;
	}
	
	//async task to communicate with MySQL
	private class _connection  extends AsyncTask<String, Void, Boolean>
	{
		private ArrayList<HashMap<String, String>> tableList = new ArrayList<HashMap<String, String>>();
		
		//executes async task, relays to the appropriate method
		@Override
		protected Boolean doInBackground(String... params) 
		{	
			working = true;
			result = "NULL";
			Log.w("SQL", params[1]);
			if(params[0] == "read")
			{
				result = ReadQuery(params[1]);
				if(params[2] == "update"){
					return true;
				} else {
					working = false;
	        		return false;
				}
		    }
			else if(params[0] == "write")
			{
				WriteQuery(params[1]);
				working = false;
				return false;
		    }
		    else if(params[0] == "test")
		    {
		    	success = testConnection();
		    	String SQL = "select * from user where username ='"+ username + "' AND password='"+password;
		    	if(ReadQuery(SQL).length() < 10){
		    		success = false;
		    	}
		    	working = false;
		    	return false;
		    }
		        
			return false; 
		}
	
		//handles the post execution. If "update" is true, write the data to the bound ListView
		@Override
		protected void onPostExecute(Boolean update) 
		{ 
	        if(update)	//true if we want to update the listview
	        {
	        	String col0 = null;
	        	try{
	        		
	        		JSONObject jsonObject = new JSONObject(result);
	        		JSONArray jArray = jsonObject.getJSONArray("data");
	        		
	        		ArrayList<TextView> Columns = new ArrayList<TextView>();
	        		//TODO fix exception here from history viewer
	        		Log.w("COL HEADER CHILD COUNT", Integer.toString(col_header.getChildCount()));
	        		for(int i = 0; i < col_header.getChildCount(); i++)
	        		{
	        			Columns.add((TextView) col_header.getChildAt(i));
	        		}
	        		
	        		//parse JSON string
	            	for(int i=0; i < jArray.length(); i++) {
	            		HashMap<String, String> map = new HashMap<String, String>();
	                	JSONObject j = jArray.getJSONObject(i);
	               
	                	@SuppressWarnings("unchecked")
						Iterator<String> iter = j.keys();
	                	int col= 0;
	                    while (iter.hasNext()) {
	                        String key = iter.next();
	                        String value = (String)j.get(key);
	                        if(key.contains("ID")){
	                        	Columns.get(4).setText(key);
	                        } else if (key.equals("finished")) {
	                        	col0 = key;
	                        	value = (String)j.get(key);
	                        	if(value.equals("1"))
	                        		value = "DONE";
	                        	else
	                        		value = "";
	                        } else {
	                        	Columns.get(col).setText(key);
	                        	col++;
	                        }
	                        map.put(key, value);
	                    }
	                	tableList.add(map);
	            	}
	            	
	        		SimpleAdapter myAdapter = 
	        				new SimpleAdapter(app, 
	        								  tableList, 
	        								  R.layout.my_list_item,
	        								  new String[] {Columns.get(0).getText().toString(), 
	        												Columns.get(1).getText().toString(), 
	        												Columns.get(2).getText().toString(), 
	        												Columns.get(3).getText().toString(), 
	        												Columns.get(4).getText().toString(),
	        												col0}, 
	        								  new int[] {R.id.cell1, R.id.cell2, R.id.cell3, R.id.cell4, R.id.cell5, R.id.cell0});
	        		
	        		list.setAdapter(myAdapter);
	        		
	            	
	        	} catch (JSONException e) {
	            	Log.e("JSONException", "Error: " + e.toString());
	        		}
	        }
	        Log.w("CONNECTION", "WORKING = FALSE");
	        working = false;
	        this.cancel(false);
    	}
	
		//sends a transaction to MySQL, returns the result as a JSON string
		private String ReadQuery(String SQL)
		{
			try {
				String host = "http://web.engr.illinois.edu/~dyel-net/readquery.php";
				List<BasicNameValuePair> nvps = null;
	        	HttpParams httpParameters = new BasicHttpParams();
	        
	        	int timeoutConnection = 20000;
	        	HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
	        	int timeoutSocket = 20000;
	        	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	
	        	HttpClient httpclient = new DefaultHttpClient(httpParameters);
	        	HttpPost httpPost = new HttpPost(host);
	        	
	        	nvps = new ArrayList<BasicNameValuePair>();  
	        	nvps.add(new BasicNameValuePair("user", "dyel-net_admin" ));
	        	nvps.add(new BasicNameValuePair("pw", "teamturtle" ));
	        	nvps.add(new BasicNameValuePair("sql", SQL));
	        	
	        	httpPost.setEntity(new UrlEncodedFormEntity(nvps));
	        	HttpResponse response = httpclient.execute(httpPost);
	        	String htmlresponse = EntityUtils.toString(response.getEntity());
	        	
				return htmlresponse;
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
        	return "ERROR";
        
		}
	
		//sends a transaction to MySQL
		private Boolean WriteQuery(String SQL)
		{		        
		try {
			String host = "http://web.engr.illinois.edu/~dyel-net/writequery.php";
			List<BasicNameValuePair> nvps = null;
			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = 20000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 20000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			HttpClient httpclient = new DefaultHttpClient(httpParameters);
			HttpPost httpPost = new HttpPost(host);
			nvps = new ArrayList<BasicNameValuePair>();  
			nvps.add(new BasicNameValuePair("user", "dyel-net_admin" ));
			nvps.add(new BasicNameValuePair("pw", "teamturtle" ));
			nvps.add(new BasicNameValuePair("sql", SQL));
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			httpclient.execute(httpPost);
			return true;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	      
		}
		
		//tests the connection to MySQL using the given username and password
		private boolean testConnection()
		{	
			try {
				String host = "http://web.engr.illinois.edu/~dyel-net/testconnection.php";
				List<BasicNameValuePair> nvps = null;
				HttpParams httpParameters = new BasicHttpParams();
        
				int timeoutConnection = 20000;
				HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
				int timeoutSocket = 20000;
				HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

				HttpClient httpclient = new DefaultHttpClient(httpParameters);
				HttpPost httpPost = new HttpPost(host);
				
				nvps = new ArrayList<BasicNameValuePair>();  
				nvps.add(new BasicNameValuePair("user", "dyel-net_admin" ));
				nvps.add(new BasicNameValuePair("pw", "teamturtle" ));
				
				httpPost.setEntity(new UrlEncodedFormEntity(nvps));
				HttpResponse response = httpclient.execute(httpPost);
				String htmlresponse = EntityUtils.toString(response.getEntity());
				
				if(htmlresponse.length() < 10)
					return true;
				else
					return false;
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// 	TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				
			}
			return false;
		}
	}
}