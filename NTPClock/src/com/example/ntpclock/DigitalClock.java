package com.example.ntpclock;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

public class DigitalClock {

	private TextView mClockTextView; // Text view representing the clock
	private boolean mShouldRun = false; // Control the Runnable whether it should keep running or not
	private Calendar mClockTime; // Main clock date time object
	
	private final Handler mHandler = new Handler(); // Handler for Runnable
	
	// A Runnable to update the clock every 1 second
	private final Runnable mUpdateClock = new Runnable() {

		@Override
		public void run() {
			if(mShouldRun) {
				updateClockDisplay();
				mHandler.postDelayed(mUpdateClock, 1000); // 1 second
			}
		}
		
	};
	
	public DigitalClock(View clockTextView) {
		mClockTextView = (TextView) clockTextView;
		mClockTime = Calendar.getInstance();
	}
	
	public void startUpdater() {
		mShouldRun = true;
		mHandler.post(mUpdateClock);
	}
	
	public void stopUpdater() {
		mShouldRun = false;
	}
	
	private void updateClockDisplay() {
		mClockTime.add(Calendar.SECOND, 1);
		SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
		mClockTextView.setText("Digital clock: " + dateFormat.format(mClockTime.getTime()));
	}
	
	public void setTime(Date clockTime) {
		mClockTime.setTime(clockTime);
	}
}
