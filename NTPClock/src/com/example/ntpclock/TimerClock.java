package com.example.ntpclock;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

public class TimerClock {
	
	// Callback to be launched when timer reaches 0 second
	public interface OnFinished {
		public void run();
	}

	private TextView mClockTextView; // Text view representing the clock
	private Calendar mClockTime; // Main clock date time object
	
	private final Handler mHandler = new Handler(); // Handler for Runnable
	
	private OnFinished onFinishedCallback;
	
	private int mHour = 0;
	private int mMinute = 10;
	private int mSecond = 0;
	
	// A Runnable to update the clock every 1 second
	private final Runnable mUpdateClock = new Runnable() {

		@Override
		public void run() {
			updateClockDisplay();
			mHandler.postDelayed(mUpdateClock, 1000); // 1 second
		}
		
	};
	
	public TimerClock(View clockTextView) {
		mClockTextView = (TextView) clockTextView;
	}
	
	public void startUpdater() {
		mHandler.removeCallbacks(mUpdateClock);
		mClockTime = Calendar.getInstance();
		mClockTime.set(Calendar.HOUR, mHour);
		mClockTime.set(Calendar.MINUTE, mMinute);
		mClockTime.set(Calendar.SECOND, mSecond);
		mHandler.post(mUpdateClock);
	}
	
	public void setStartTime(int seconds) {
		mHour = seconds / 3600;
		mMinute = (seconds % 3600) / 60;
		mSecond = (seconds % 3600) % 60;
	}
	
	private void updateClockDisplay() {
		if(mClockTime.get(Calendar.MINUTE) == 0 && mClockTime.get(Calendar.SECOND) == 0) {
			mHandler.removeCallbacks(mUpdateClock);
			onFinishedCallback.run();
			return;
		}
		// Minus 1 second for counting down
		mClockTime.add(Calendar.SECOND, -1);
		SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
		mClockTextView.setText("Remaining time until next sync: " + dateFormat.format(mClockTime.getTime()));
	}
	
	public void setOnFinishedCallback(OnFinished callback) {
		onFinishedCallback = callback;
	}
}
