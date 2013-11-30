package com.example.dyel_net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ViewSwitcher;

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
    		  lineThroughText(view);
    	  }
    	  
      }
      
      return view;
    }
    
    public void lineThroughText(View view)
    {
    	ArrayList<TextView> text = new ArrayList<TextView>();
    	text.add((TextView) view.findViewById(R.id.cell1));
    	text.add((TextView) view.findViewById(R.id.cell2));
    	text.add((TextView) view.findViewById(R.id.cell3));
    	text.add((TextView) view.findViewById(R.id.cell4));
    	text.add((TextView) view.findViewById(R.id.cell5));
    	text.add((TextView) view.findViewById(R.id.cell0));
    	
    	for (TextView eachText : text){
    		/*TextView text = (TextView) view.findViewById(R.id.cell1);    	
        	text.setTextColor(Color.WHITE);
            //text.setBackgroundColor(Color.RED); 
            int color = Color.argb( 200, 255, 64, 64 );
            text.setBackgroundColor( color );
            text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            */
        	eachText.setTextColor(Color.WHITE);
            //text.setBackgroundColor(Color.RED); 
            int color = Color.argb( 200, 255, 64, 64 );
            eachText.setBackgroundColor( color );
            eachText.setPaintFlags(eachText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            
    	}
    	    	
    }
}