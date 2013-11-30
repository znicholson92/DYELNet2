package com.example.dyel_net;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

public class SpecialAdapter extends SimpleAdapter {
    private int[] colors = new int[] { 0x30FF0000, 0x300000FF };
     
    public SpecialAdapter(Context context, List<HashMap<String, String>> items, int resource, String[] from, int[] to) {
        super(context, items, resource, from, to);
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = super.getView(position, convertView, parent);
      //int colorPos = position % colors.length;
      //view.setBackgroundColor(colors[colorPos]);
      view.setBackgroundColor(colors[0]);
      Object item = getItem(position);
      String row = item.toString();
      String complete = GoalViewer.complete.substring(1, GoalViewer.complete.length()-1);
      if(row.contains(complete)){
    	  System.out.println("contains true");
    	  int startIndex = row.indexOf(complete);
    	  startIndex = startIndex + complete.length() + 1;
    	  System.out.println(row.substring(startIndex, startIndex+1));
    	  if (row.substring(startIndex, startIndex+1).contentEquals("1")){
    		  view.setBackgroundColor(colors[1]);  
    	  }
      }
      return view;
    }
}