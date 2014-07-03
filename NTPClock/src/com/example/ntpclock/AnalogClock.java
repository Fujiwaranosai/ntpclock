package com.example.ntpclock;

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RemoteViews.RemoteView;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * This widget display an analogic clock with two hands for hours and
 * minutes.
 */
@RemoteView
public class AnalogClock extends View {
	
    private Drawable mHourHand;
    private Drawable mMinuteHand;
    private Drawable mSecondHand;
    private Drawable mDial;

    private int mDialWidth;
    private int mDialHeight;

    private boolean mAttached;

    private final Handler mHandler = new Handler();
    private float mMinutes;
    private float mHour;
    private float mSeconds;
    private boolean mChanged;
    
    private int mLeft;
    private int mTop;
    private int mRight;
    private int mBottom;
    
    private final Handler mClockHandler = new Handler(); // Handler for Runnable
    private boolean mShouldRun = false; // Control the Runnable whether it should keep running or not
    private Calendar mClockTime; // Main clock date time object
	
	// A Runnable to update the clock every 1 second
	private final Runnable mUpdateClock = new Runnable() {

		@Override
		public void run() {
			if(mShouldRun) {
				updateClockDisplay();
				mClockHandler.postDelayed(mUpdateClock, 1000); // 1 second
			}
		}
		
	};

    public AnalogClock(Context context) {
        this(context, null);
    }

    public AnalogClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnalogClock(Context context, AttributeSet attrs,
                       int defStyle) {
        super(context, attrs, defStyle);
        Resources r = context.getResources();
        TypedArray a =
                context.obtainStyledAttributes(
                        attrs, R.styleable.AnalogClock, defStyle, 0);

        mDial = r.getDrawable(R.drawable.clock_dial);

        mHourHand = r.getDrawable(R.drawable.clock_hand_hour_2);

        mMinuteHand = r.getDrawable(R.drawable.clock_hand_minute_2);
        
        mSecondHand = r.getDrawable(R.drawable.clock_hand_second_2);

        mClockTime = Calendar.getInstance();

        mDialWidth = mDial.getIntrinsicWidth();
        mDialHeight = mDial.getIntrinsicHeight();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        
        mRight = getRight();
        mLeft = getLeft();
        mBottom = getBottom();
        mTop = getTop();

        if (!mAttached) {
            mAttached = true;
            IntentFilter filter = new IntentFilter();

            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

            //getContext().registerReceiver(mIntentReceiver, filter, null, mHandler);
        }

        // NOTE: It's safe to do these after registering the receiver since the receiver always runs
        // in the main thread, therefore the receiver can't run before this method returns.

        // The time zone may have changed while the receiver wasn't registered, so update the Time
        mClockTime = Calendar.getInstance();

        // Make sure we update to the current time
        //onTimeChanged();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            //getContext().unregisterReceiver(mIntentReceiver);
            mAttached = false;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize =  MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize =  MeasureSpec.getSize(heightMeasureSpec);

        float hScale = 1.0f;
        float vScale = 1.0f;

        if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
            hScale = (float) widthSize / (float) mDialWidth;
        }

        if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight) {
            vScale = (float )heightSize / (float) mDialHeight;
        }

        float scale = Math.min(hScale, vScale);

        setMeasuredDimension(resolveSize((int) (mDialWidth * scale), widthMeasureSpec),
                resolveSize((int) (mDialHeight * scale), heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mChanged = true;
        mRight = getRight();
        mLeft = getLeft();
        mBottom = getBottom();
        mTop = getTop();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean changed = mChanged;
        if (changed) {
            mChanged = false;
        }

        int availableWidth = mRight - mLeft;
        int availableHeight = mBottom - mTop;

        int x = availableWidth / 2;
        int y = availableHeight / 2;

        final Drawable dial = mDial;
        int w = dial.getIntrinsicWidth();
        int h = dial.getIntrinsicHeight();

        boolean scaled = false;

        if (availableWidth < w || availableHeight < h) {
            scaled = true;
            float scale = Math.min((float) availableWidth / (float) w,
                                   (float) availableHeight / (float) h);
            canvas.save();
            canvas.scale(scale, scale, x, y);
        }

        if (changed) {
            dial.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        dial.draw(canvas);

        canvas.save();
        canvas.rotate(mHour / 12.0f * 360.0f, x, y);
        final Drawable hourHand = mHourHand;
        if (changed) {
            w = hourHand.getIntrinsicWidth();
            h = hourHand.getIntrinsicHeight();
            hourHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        hourHand.draw(canvas);
        canvas.restore();

        canvas.save();
        canvas.rotate(mMinutes / 60.0f * 360.0f, x, y);

        final Drawable minuteHand = mMinuteHand;
        if (changed) {
            w = minuteHand.getIntrinsicWidth();
            h = minuteHand.getIntrinsicHeight();
            minuteHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        minuteHand.draw(canvas);
        canvas.restore();
        
        canvas.save();
        canvas.rotate(mSeconds, x, y);
        
        final Drawable secondHand = mSecondHand;
        if(changed) {
        	w = secondHand.getIntrinsicWidth();
        	h = secondHand.getIntrinsicHeight();
        	secondHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        secondHand.draw(canvas);
        canvas.restore();

        if (scaled) {
            canvas.restore();
        }
    }

    private void onTimeChanged() {
    	mClockTime = Calendar.getInstance();

        int hour = mClockTime.get(Calendar.HOUR);
        int minute = mClockTime.get(Calendar.MINUTE);
        int second = mClockTime.get(Calendar.SECOND);

        mMinutes = minute + second / 60.0f;
        mHour = hour + mMinutes / 60.0f;
        mSeconds = 6f * second;
        mChanged = true;
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mClockTime = Calendar.getInstance(TimeZone.getTimeZone(tz));
            }

            onTimeChanged();
            
            invalidate();
        }
    };
    
    public void startUpdater() {
		mShouldRun = true;
		mClockHandler.post(mUpdateClock);
	}
	
	public void stopUpdater() {
		mShouldRun = false;
	}
	
	private void updateClockDisplay() {
		mClockTime.add(Calendar.SECOND, 1);
		updateClockHands();
	}

	private void updateClockHands() {
		int seconds = mClockTime.get(Calendar.SECOND);
    	int minutes = mClockTime.get(Calendar.MINUTE);
    	int hours = mClockTime.get(Calendar.HOUR);
    	mSeconds = 6f * seconds;
    	mMinutes = minutes + seconds / 60f;
    	mHour = hours + mMinutes / 60f;
    	invalidate();
	}
    
    public void setTime(Date clockTime) {
    	mClockTime.setTime(clockTime);
    	updateClockHands();
    }
}