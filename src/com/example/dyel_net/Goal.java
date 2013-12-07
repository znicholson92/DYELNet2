package com.example.dyel_net;

import java.util.Stack;

import org.json.JSONException;

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
	private String userID;
	private boolean running = false;

	public String dayID = null;

	private LinearLayout col_head;
	private String type;
	private String subID;
	private GoalViewer gv = null;
	
	public Goal(MainActivity a, String userID) {
		app = a;
		running = true;
		this.userID = userID; 
	}
	public void createGoal(){
		//col_head = (LinearLayout) app.findViewById(R.id.working_out_col_header);
		
				EditText goalNameET =  (EditText) app.findViewById(R.id.creategoal_goal_name);
		        EditText noteET =	   (EditText) app.findViewById(R.id.creategoal_notes);
		        EditText startDateTy = (EditText) app.findViewById(R.id.creategoal_dateofbirth_year);
		        EditText startDateTm = (EditText) app.findViewById(R.id.creategoal_dateofbirth_month);
		        EditText startDateTd = (EditText) app.findViewById(R.id.creategoal_dateofbirth_day);
		        EditText endDateTy =   (EditText) app.findViewById(R.id.creategoal_dateofbirth_year2);
		        EditText endDateTm =   (EditText) app.findViewById(R.id.creategoal_dateofbirth_month2);
		        EditText endDateTd =   (EditText) app.findViewById(R.id.creategoal_dateofbirth_day2);
		        //category
		        //completed?
		        
		        String goalName = goalNameET.getText().toString();
		        String note = noteET.getText().toString();
		        String startDate = startDateTy.getText().toString()+"-"+startDateTm.getText().toString()+"-"+startDateTd.getText().toString();
		        String endDate = endDateTy.getText().toString()+"-"+endDateTm.getText().toString()+"-"+endDateTd.getText().toString();
		        //category
		        //completed?
		        
		        
		        String SQL = "INSERT INTO  `dyel-net_main`.`goals` "
		                        +"(`username` ,`name` , `notes`, `start_date` , `goal_date`)"
		                        +" VALUES ( "
		                        +"'"+userID+"', "
		                        +"'"+goalName+"', "
		                        +"'"+note+"', "
		                        +"'"+startDate+"', "
		                        +"'"+endDate+"');";
		     
		        
		        System.out.println(SQL);
		        
		        connection con = new connection("dyel-net_admin", "teamturtle", app);
		        
		        ProgressDialog pd;
		        pd = ProgressDialog.show(app, "Loading", "Creating a goal...");
		        
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
	public void updateGoal(String originalGoalName){
		//col_head = (LinearLayout) app.findViewById(R.id.working_out_col_header);
    			//app.gotoLayout(R.layout.goal_update);
				EditText goalNameET =  (EditText) app.findViewById(R.id.goal_update_goal_name);
		        EditText noteET =	   (EditText) app.findViewById(R.id.goal_update_notes);
		        EditText startDateTy = (EditText) app.findViewById(R.id.goal_update_dateofbirth_year);
		        EditText startDateTm = (EditText) app.findViewById(R.id.goal_update_dateofbirth_month);
		        EditText startDateTd = (EditText) app.findViewById(R.id.goal_update_dateofbirth_day);
		        EditText endDateTy =   (EditText) app.findViewById(R.id.goal_update_dateofbirth_year2);
		        EditText endDateTm =   (EditText) app.findViewById(R.id.goal_update_dateofbirth_month2);
		        EditText endDateTd =   (EditText) app.findViewById(R.id.goal_update_dateofbirth_day2);
		        //category
		        //completed?
		        
		        String goalName = goalNameET.getText().toString();
		        String notes = noteET.getText().toString();
		        String startDate = startDateTy.getText().toString()+"-"+startDateTm.getText().toString()+"-"+startDateTd.getText().toString();
		        String endDate = endDateTy.getText().toString()+"-"+endDateTm.getText().toString()+"-"+endDateTd.getText().toString();
		        //category
		        //completed?
		        		          
		        String updateSQL = "UPDATE goals SET `name`='" +
		        		goalName +
		        		"', `notes`='" +
		        		notes +
		        		"', `start_date`='" +
		        		startDate +
		        		"', `goal_date`='" +
		        		endDate +
		        		"' WHERE `username` = '"+
		        		userID +
		        		"' and `name` = '" +
		        		originalGoalName +
		        		"';";
		        System.out.println(updateSQL);
		        
		        connection con = new connection("dyel-net_admin", "teamturtle", app);
		        
		        ProgressDialog pd;
		        pd = ProgressDialog.show(app, "Loading", "Updating a goal...");
		        
		        try {
		        	con.writeQuery(updateSQL);
					Thread.sleep(2500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();				}
		        
		        pd.cancel();        
		        con.logout();        
		        app.setContentView(R.layout.main_menu); 
	}
	public void deleteGoal(String goalName){		
		String deleteSQL = "DELETE FROM goals WHERE username = '" +
				userID +
				"' and name = '" +
				goalName +
				"';";
		connection con = new connection("dyel-net_admin", "teamturtle", app);
		ProgressDialog pd;
		pd = ProgressDialog.show(app, "Loading", "Deleting a goal...");

		try {
			con.writeQuery(deleteSQL);
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		pd.cancel();
		con.logout();
	}
	public void updateDeletedSubID(String goalName){
		        String updateSQL = "UPDATE goals SET `type`='none', " +
		        		"`subclassID`='' " +
		        		"WHERE `username` = '"+
		        		userID +
		        		"' and `name` = '" +
		        		goalName +
		        		"';";
		        System.out.println(updateSQL);
		        
		        connection con = new connection("dyel-net_admin", "teamturtle", app);
		        ProgressDialog pd;
		        pd = ProgressDialog.show(app, "Loading", "Updating the goal with delete userdata/set information...");
		        
		        try {
		        	con.writeQuery(updateSQL);
					Thread.sleep(2500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();				}
		        
		        pd.cancel();        
		        con.logout();        
	}

	public void cancel() {
		running = false;
	}

	public boolean isRunning() {
		return running;
	}
	
	public void viewGoal()
	{	
		GoalViewer.viewSummary(app, userID);		
	}
	public void viewGoalDetail(String goalName) throws JSONException{
		GoalViewer.viewDetail(app, userID, goalName);
		subID = GoalViewer.getSubID();
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
	public void viewSetGoals() {
		GoalSet.open_editSet(app, subID);
		//GoalSet.viewSetGoal(app, subID);
	}
	public void viewDetailWithEdit(String goalName) {
		// TODO Auto-generated method stub
		GoalViewer.viewDetailWithEdit(app, userID, goalName);
		subID = GoalViewer.getSubID();
	}
}
