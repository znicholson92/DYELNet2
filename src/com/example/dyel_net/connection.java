package com.example.dyel_net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
	private String result;
	private MainActivity app;
	private ListView list;
	
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
	public void readQuery(String SQL, ListView l)
	{
		_connection task = new _connection();
		list = l;
		task.execute("read", SQL, "update");
	}

	//send a transaction and return the results as a JSON string
	public String readQuery(String SQL)
	{
		_connection task = new _connection();
		task.execute("read", SQL, "NO");
		return result;
	}
	
	//send a transaction that modifies the database and does not return anything
	public void writeQuery(String SQL)
	{
		_connection task = new _connection();
		task.execute("write", SQL);
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
		
		@Override
		protected Boolean doInBackground(String... params) 
		{	
			if(params[0] == "read")
			{
				result = ReadQuery(params[1]);
				if(params[2] == "update")
					return true;
	        	else
	        		return false;
		    }
			else if(params[0] == "write")
			{
				WriteQuery(params[1]);
					return false;
		    }
		    else if(params[0] == "test")
		    {
		    	success = testConnection();
		    	return false;
		    }
		        
			return false; 
		}
	
		@Override
		protected void onPostExecute(Boolean update) 
		{
	        if(update)	//true if we want to update the listview
	        {
	        	try{
	        		JSONObject jsonObject = new JSONObject(result);
	        		JSONArray jArray = jsonObject.getJSONArray("data");
	        		
	        		TextView Col1 = (TextView)app.findViewById(R.id.col1);
	        		TextView Col2 = (TextView)app.findViewById(R.id.col2);
	        		TextView Col3 = (TextView)app.findViewById(R.id.col3);
	        		TextView Col4 = (TextView)app.findViewById(R.id.col4);
	        		TextView Col5 = (TextView)app.findViewById(R.id.col5);
	        		
	        		TextView[] Columns = new TextView[] {Col1, Col2, Col3, Col4, Col5};
	        		
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
	                        map.put(key, value);
	                        Log.w(key, value);
	                        Columns[col].setText(key);
	                        col++;
	                    }
	                	tableList.add(map);
	            	}
	            	
	        		SimpleAdapter myAdapter = 
	        				new SimpleAdapter(app, 
	        								  tableList, 
	        								  R.layout.my_list_item,
	        								  new String[] {(String) Col1.getText(), (String) Col2.getText(),(String) Col3.getText() ,(String) Col4.getText(), (String) Col5.getText()}, 
	        								  new int[] {R.id.cell1, R.id.cell2, R.id.cell3, R.id.cell4, R.id.cell5});
	
	        		list.setAdapter(myAdapter);	
	
	        	
	            	
	        	} catch (JSONException e) {
	            	Log.e("JSONException", "Error: " + e.toString());
	        		}
	        	}
	        	this.cancel(false);
    	}
	
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
        	HttpResponse response;
        	nvps = new ArrayList<BasicNameValuePair>();  
        	nvps.add(new BasicNameValuePair("user", username ));
        	nvps.add(new BasicNameValuePair("pw", password ));
        	nvps.add(new BasicNameValuePair("sql", SQL));
        	httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        	response = httpclient.execute(httpPost);
        	String htmlresponse;
			htmlresponse = EntityUtils.toString(response.getEntity());
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
			nvps.add(new BasicNameValuePair("user", username ));
			nvps.add(new BasicNameValuePair("pw", password ));
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
					nvps.add(new BasicNameValuePair("user", username ));
					nvps.add(new BasicNameValuePair("pw", password ));
					httpPost.setEntity(new UrlEncodedFormEntity(nvps));
					HttpResponse response = httpclient.execute(httpPost);
					String htmlresponse;
					htmlresponse = EntityUtils.toString(response.getEntity());
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