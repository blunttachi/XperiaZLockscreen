package cro.marin.xperia.locker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import cro.marin.xperia.locker.R;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;

public class Clock extends TextView {

 private TextView clock;
 private String oclock;
 
	 public Clock(final Context context, AttributeSet attrs) {
		  super(context, attrs);

			clock = (TextView) findViewById(R.id.clock);  	   
	   	    Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/SoMADigitLight.ttf");
	   	    clock.setTypeface(tf);
	   	    
              final Handler h = new Handler();
              h.post(new Runnable() {
                  @Override
                  public void run() {
                      updateTime();
                      h.postDelayed(this, 1000);
                  }
              }); 
	 }
	private void updateTime() {
		  Calendar cal = Calendar.getInstance();
          SimpleDateFormat time = new SimpleDateFormat("HH:mm");
          oclock = time.format(cal.getTime());
          clock.setText(oclock);

	}
}