package com.example.ntpclock;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import com.example.ntpclock.TimerClock.OnFinished;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	@Override
	public void onBackPressed() {
		this.finish();
		super.onBackPressed();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		private class GetNTPTime extends AsyncTask<String, Integer, Date> {

			@Override
			protected Date doInBackground(String... params) {
				String url = params[0];
				long nowAsPerDeviceTimeZone = 0;
				SntpClient sntpClient = new SntpClient();
				
				if(sntpClient.requestTime(url, 10000)) {
					nowAsPerDeviceTimeZone = sntpClient.getNtpTime();
					// We do not need to get UTC time zone so I comment the following lines
					/*Calendar cal = Calendar.getInstance();
					TimeZone timeZoneInDevice = cal.getTimeZone();
					int diffTimeZone = timeZoneInDevice.getOffset(System.currentTimeMillis());
					nowAsPerDeviceTimeZone -= diffTimeZone;*/
				}
				return new Date(nowAsPerDeviceTimeZone);
			}
		}
		
		// Handler for NTP Sync
		private final Handler mHandler = new Handler();
		
		// Runnable to update every 10 minutes = 600 seconds (600,000 milliseconds)
		private final Runnable mNTPSync = new Runnable() {

			@Override
			public void run() {
				try {
					Date ntpTime;
					ntpTime = getUTCTime();
					mDigitalClock.setTime(ntpTime);
					mAnalogClock.setTime(ntpTime);
					
					// Update last sync message on text view status
					Calendar cal = Calendar.getInstance();
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					statusTextView.setText("Last synced time: " + dateFormat.format(cal.getTime()));
					
					int timer = 600000; // Timer in milliseconds: 10 minutes = 600 seconds = 600,000 milliseconds
					mHandler.postDelayed(mNTPSync, timer);
					mTimerClock.setStartTime(timer/1000);
					mTimerClock.startUpdater();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
			
		};
		
		private DigitalClock mDigitalClock;
		private AnalogClock mAnalogClock;
		private TimerClock mTimerClock;
		private TextView statusTextView; // Text View to update last sync date time
		private Button syncButton; // Button View to manually sync clock time with NTP time
		
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			
			TextView clockTextView = (TextView) rootView.findViewById(R.id.digital_clock);
			mDigitalClock = new DigitalClock(clockTextView);
			mDigitalClock.startUpdater();
			
			TextView timerTextView = (TextView) rootView.findViewById(R.id.timer_clock);
			mTimerClock = new TimerClock(timerTextView);
			mTimerClock.setOnFinishedCallback(new OnFinished() {
				@Override
				public void run() {
					// Remove current callback and restart synchronizing new time
					mHandler.removeCallbacks(mNTPSync);
					mHandler.post(mNTPSync);
				}
			});
			
			mAnalogClock = (AnalogClock) rootView.findViewById(R.id.analog_clock);
			mAnalogClock.startUpdater();
			
			// Start the NTP sync handler
			mHandler.post(mNTPSync);
			
			statusTextView = (TextView) rootView.findViewById(R.id.status);
			
			syncButton = (Button) rootView.findViewById(R.id.sync);
			syncButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// Remove current callback and restart synchronizing new time
					mHandler.removeCallbacks(mNTPSync);
					mHandler.post(mNTPSync);
				}
			});
			
			return rootView;
		}
		
		public Date getUTCTime() throws InterruptedException, ExecutionException {
			Date ntpTime = new GetNTPTime().execute("0.ubuntu.pool.ntp.org").get();
			return ntpTime;
		}
	}

}
