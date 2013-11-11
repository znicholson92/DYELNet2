package com.example.dyel_net;

import java.util.Stack;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

@SuppressLint("CutPasteId")
public class Goal {
	/************* PRIVATE MEMBER VARIABLES ******************/
	private MainActivity app;
	private Stack<String> previous_SQL = new Stack<String>();
	private String status;
	private boolean running = false;

	public String dayID = null;

	private LinearLayout col_head;

	public Goal(MainActivity a) {
		app = a;
		running = true;
		//col_head = (LinearLayout) app.findViewById(R.id.working_out_col_header);
		
		EditText userNameET =  (EditText) app.findViewById(R.id.creategoal_goal_name);
        EditText noteET =	   (EditText) app.findViewById(R.id.creategoal_notes);
        EditText startDateTy = (EditText) app.findViewById(R.id.creategoal_dateofbirth_year);
        EditText startDateTm = (EditText) app.findViewById(R.id.creategoal_dateofbirth_month);
        EditText startDateTd = (EditText) app.findViewById(R.id.creategoal_dateofbirth_day);
        EditText endDateTy =   (EditText) app.findViewById(R.id.creategoal_dateofbirth_year2);
        EditText endDateTm =   (EditText) app.findViewById(R.id.creategoal_dateofbirth_month2);
        EditText endDateTd =   (EditText) app.findViewById(R.id.creategoal_dateofbirth_day2);
        //category
        //completed?
        
        String userName = userNameET.getText().toString();
        String note = noteET.getText().toString();
        String startDate = startDateTy.getText().toString()+"-"+startDateTm.getText().toString()+"-"+startDateTd.getText().toString();
        String endDate = endDateTy.getText().toString()+"-"+endDateTm.getText().toString()+"-"+endDateTd.getText().toString();
        //category
        //completed?
        
        
        String SQL = "INSERT INTO  `dyel-net_main`.`goals` "
                        +"(`username` , `note`, `start_date` , `end_date`)"
                        +"VALUES ( "
                        +"'"+userName+"', "
                        +"'"+note+"', "
                        +"'"+startDate+"', "
                        +"'"+endDate+"');";
     
        
        connection con = new connection("dyel-net_admin", "teamturtle", app);
        
        ProgressDialog pd;
        pd = ProgressDialog.show(app, "Loading", "Creating account...");
        
        try {
        	con.writeQuery(SQL);
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        pd.cancel();        
        con.logout();        
        app.setContentView(R.layout.main_menu); 
	}

	public void cancel() {
		running = false;
	}

	public boolean isRunning() {
		return running;
	}

	public void viewGoal(String name) {
		
		/***TODO***/
		/***Just copied from Workout.java ***/
		String SQL = "SELECT exercise.name, count(*) As 'Sets' FROM _set "
				+ " INNER JOIN exercise ON exercise.exerciseID = _set.exerciseID WHERE _set.dayID = "
				+ dayID + " GROUP BY _set.exerciseID";

		ListView l = (ListView) app.findViewById(R.id.workingout_listView);
		app.con.readQuery(SQL, l, col_head);

		TextView t = (TextView) app.findViewById(R.id.working_out_topbar_text);
		t.setText(name);

		pushBack(SQL);
		status = "session";

	}

	// back button functionality for listview query menus
	// sets query to listview as the previous one
	public boolean goBack(int pops, ListView l) {
		if (pops > 0 && !previous_SQL.isEmpty()) {
			String SQL = null;
			for (int i = 0; i < pops && !previous_SQL.isEmpty(); ++i)
				SQL = previous_SQL.pop();

			app.con.readQuery(SQL, l, col_head);

			return true;
		} else
			return false;
	}
	public void pushBack(String SQL)
	{
		previous_SQL.push(SQL);
	}
}
