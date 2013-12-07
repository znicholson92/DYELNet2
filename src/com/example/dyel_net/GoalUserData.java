package com.example.dyel_net;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class GoalUserData {

	public static void viewUserDataGoal(final MainActivity app,
			String userdataID) {
		Boolean isGoal = true;
		EditText bodyfat = (EditText) app
				.findViewById(R.id.goal_userdata_bodyfat);
		EditText restinghr = (EditText) app
				.findViewById(R.id.goal_userdata_restinghr);
		EditText weight = (EditText) app
				.findViewById(R.id.goal_userdata_weight);
		EditText notes = (EditText) app.findViewById(R.id.goal_userdata_notes);

		String SQL = "SELECT * FROM userdata WHERE " + "username='"
				+ app.con.username() + "'" + "AND isGoal=" + isGoal + " "
				+ "ORDER BY datetime DESC";
		Log.w("SQL", SQL);
		String JSONstring = app.con.readQuery(SQL);
		JSONObject jsonObject;

		if (JSONstring.length() > 10) {
			try {
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

		} else {
			return;
		}

		LinearLayout changes_bar = (LinearLayout) app
				.findViewById(R.id.goal_userdata_changesbar);
		changes_bar.setVisibility(View.INVISIBLE);

		TextWatcher watcher = new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				LinearLayout changes_bar = (LinearLayout) app
						.findViewById(R.id.goal_userdata_changesbar);
				changes_bar.setVisibility(View.VISIBLE);
			}
		};

		bodyfat.addTextChangedListener(watcher);
		restinghr.addTextChangedListener(watcher);
		weight.addTextChangedListener(watcher);
		notes.addTextChangedListener(watcher);
		if (isGoal) {
			changes_bar.setVisibility(View.VISIBLE);
		}
		return;
	}

	public static void updateUserDataGoal(final MainActivity app, String userdataID) {
		EditText bodyfat = (EditText) app.findViewById(R.id.goal_userdata_bodyfat);
		EditText restinghr = (EditText) app.findViewById(R.id.goal_userdata_restinghr);
		EditText weight = (EditText) app.findViewById(R.id.goal_userdata_weight);
		EditText notes = (EditText) app.findViewById(R.id.goal_userdata_notes);

		String SQL = "UPDATE userdata SET bodyfat='"
				+ bodyfat.getText().toString() + "', restingHR='"
				+ restinghr.getText().toString() + "', weight='"
				+ weight.getText().toString() + "', notes='"
				+ notes.getText().toString() + "' WHERE username = '"
				+ app.con.username() + "' and userdataID='" + userdataID + "';";

		app.con.writeQuery(SQL);
		/*LinearLayout changes_bar = (LinearLayout) app
				.findViewById(R.id.userdata_changesbar);
		changes_bar.setVisibility(View.INVISIBLE);*/
	}

	public static void deleteUserDataGoal(MainActivity app, String userdataID) {
		String deleteSQL = "DELETE FROM userdata WHERE userdataID = '" +
				userdataID +
				"';";
		connection con = new connection("dyel-net_admin", "teamturtle", app);
		ProgressDialog pd;
		pd = ProgressDialog.show(app, "Loading", "Deleting a userdata goal...");

		try {
			con.writeQuery(deleteSQL);
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		pd.cancel();
		con.logout();
	}

	public static void createUserDataGoal(MainActivity app, String userdataID) {

		EditText bodyfat = (EditText) app.findViewById(R.id.goal_userdata_bodyfat);
		EditText restinghr = (EditText) app.findViewById(R.id.goal_userdata_restinghr);
		EditText weight = (EditText) app.findViewById(R.id.goal_userdata_weight);
		EditText notes = (EditText) app.findViewById(R.id.goal_userdata_notes);

		String SQL = "INSERT INTO userdata (username, bodyfat, restingHR, weight, notes, isGoal) "
				+ "VALUES("
				+ "'"
				+ app.con.username()
				+ "',"
				+ "'"
				+ bodyfat.getText().toString()
				+ "',"
				+ "'"
				+ restinghr.getText().toString()
				+ "',"
				+ "'"
				+ weight.getText().toString()
				+ "',"
				+ "'"
				+ notes.getText().toString() + "'," + true + ")";
		// userdataID?

		app.con.writeQuery(SQL);
		LinearLayout changes_bar = (LinearLayout) app
				.findViewById(R.id.userdata_changesbar);
		changes_bar.setVisibility(View.INVISIBLE);
	}
}
