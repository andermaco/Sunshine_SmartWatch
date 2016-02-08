/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.example.android.sunshine.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.example.android.sunshine.app.lib.Util;
import com.example.android.sunshine.app.lib.WeatherDataField;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class SunshineWatchFace extends CanvasWatchFaceService implements ConnectionCallbacks,
        OnConnectionFailedListener {
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
    private static final Typeface BOLD_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;
    private static final String LOG_TAG = SunshineWatchFace.class.getSimpleName();

    public static GoogleApiClient googleApiClient;

    @Override
    public Engine onCreateEngine() {
        googleApiClient = new GoogleApiClient.Builder(this)
        .addApi(Wearable.API)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();
        return new Engine();
    }

    @Override
    public void onDestroy() {
        if (null != googleApiClient && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private static class EngineHandler extends Handler {
        private final WeakReference<SunshineWatchFace.Engine> mWeakReference;

        public EngineHandler(SunshineWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            SunshineWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        final Handler mUpdateTimeHandler = new EngineHandler(this);
        boolean mRegisteredTimeZoneReceiver = false;
        Paint mBackgroundPaint;
        Paint mTextPaint;
        Paint mDatePaint;
        Paint mTemperaturePaint;
        boolean mAmbient;
        Time mTime;
        DisplayMetrics metrics;

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };
        int mTapCount;

        float mXOffset;
        float mXOffset_date;
        float mYOffset;
        float mYOffset_date;
        float mXOffset_image;
        float mXOffset_image_anim = 0;
        float mYOffset_image_anim = 0;
//        float XOFFSET_IMAGE_ANIM, YOFFSET_IMAGE_ANIM;
        float mYOffset_image;
        float mXOffset_temperature_max;
        float mXOffset_temperature_min;
        float mYOffset_temperature;
        String mMax = null;
        String mMin = null;
        String mWeather_id, mGrade;
        int mDrawableResource, mBackgroundColorResource, mFontColorResource;
        boolean isRound;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            // Request weather data
            new SendToDataLayerThread(WeatherDataField.PATH_REQUEST, null);

            setWatchFaceStyle(new WatchFaceStyle.Builder(SunshineWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());

            Resources resources = SunshineWatchFace.this.getResources();
            mYOffset = resources.getDimension(R.dimen.digital_y_offset_time);
            mYOffset_date = resources.getDimension(R.dimen.digital_y_offset_date);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(resources.getColor(R.color.background));

            mTextPaint = new Paint();
            mTextPaint = createTextPaint(resources.getColor(R.color.digital_text));
            mDatePaint = new Paint();
            mDatePaint = createTextPaint(resources.getColor(R.color.digital_text));
            mTemperaturePaint = new Paint();
            mTemperaturePaint = createTextPaint(resources.getColor(R.color.digital_text));

            mWeather_id = getResources().getString(R.string.weather_id);
            mGrade = getResources().getString(R.string.grace);
            mDrawableResource = -1;
            mBackgroundColorResource = resources.getColor(R.color.black);
            mFontColorResource = resources.getColor(R.color.white);
            mTime = new Time();

            // Register EventBus
            EventBus.getDefault().register(this);



        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            EventBus.getDefault().unregister(this);
            super.onDestroy();
        }




        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();


            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            SunshineWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            SunshineWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = SunshineWatchFace.this.getResources();
            isRound = insets.isRound();
            if (!isRound) mXOffset = resources.getDimension(R.dimen.digital_x_offset_time);
            if (!isRound) mXOffset_date = resources.getDimension(R.dimen.digital_x_offset_date);
            mYOffset = resources.getDimension(isRound
                    ? R.dimen.digital_y_offset_time_round : R.dimen.digital_y_offset_time);
            mYOffset_date = resources.getDimension(isRound
                    ? R.dimen.digital_y_offset_date_round : R.dimen.digital_y_offset_date);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);
            mXOffset_image = resources.getDimension(isRound
                    ? R.dimen.image_x_offset_round : R.dimen.image_x_offset);
            mYOffset_image = resources.getDimension(isRound
                    ? R.dimen.image_y_offset_round : R.dimen.image_y_offset);
            float textDateSize = resources.getDimension(isRound ? R.dimen.digital_text_date_size_round :
                    R.dimen.digital_text_date_size);
            mTextPaint.setTextSize(textSize);
            mDatePaint.setTextSize(textDateSize);

            mXOffset_temperature_max = resources.getDimension(isRound
                    ? R.dimen.max_temperature_x_offset_round : R.dimen.max_temperature_x_offset);
            if (!isRound) mXOffset_temperature_min = resources
                    .getDimension(R.dimen.min_temperature_x_offset);
            mYOffset_temperature = resources.getDimension(isRound
                    ? R.dimen.temperature_y_offset_round : R.dimen.temperature_y_offset);
            mTemperaturePaint.setTextSize(resources.getDimension(isRound
                    ? R.dimen.temperature_text_size_round : R.dimen.temperature_text_size));
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            boolean burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
            mTextPaint.setTypeface(burnInProtection ? NORMAL_TYPEFACE : BOLD_TYPEFACE);

        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;

                if (mLowBitAmbient) {
                    updateWatchStyle();
                }

                mXOffset_image_anim = 0;
                mYOffset_image_anim = 0;
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void updateWatchStyle(){
            if (mAmbient){

                mTextPaint.setAntiAlias(false);
                mDatePaint.setAntiAlias(false);
            } else {
                mTextPaint.setAntiAlias(true);
                mDatePaint.setAntiAlias(true);
                mTemperaturePaint.setAntiAlias(true);
            }
        }


        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            Resources resources = SunshineWatchFace.this.getResources();
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    mTapCount++;
                    mBackgroundPaint.setColor(resources.getColor(mTapCount % 2 == 0 ?
                            R.color.background : R.color.background2));
                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            boolean mInvalidate = false;

            // Draw the background.
            if (isInAmbientMode()) {
                canvas.drawColor(Color.BLACK);
                Log.i(LOG_TAG, "ambientMode");

                // Paint time
                mTime.setToNow();
                String time = String.format("%d:%02d", mTime.hour, mTime.minute);
                mTextPaint.setColor(getResources().getColor(R.color.white));
                canvas.drawText(time, mXOffset, mYOffset, mTextPaint);

                // Paint date
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("c MMM d");
                mDatePaint.setColor(getResources().getColor(R.color.white));
                canvas.drawText(simpleDateFormat.format(calendar.getTime()), mXOffset_date,
                        mYOffset_date, mDatePaint);

            } else {
                canvas.drawColor(mBackgroundColorResource);
                Log.i(LOG_TAG, "Not ambientMode");

                // Paint time
                mTime.setToNow();
                String time = String.format("%d:%02d", mTime.hour, mTime.minute);
                mTextPaint.setColor(mFontColorResource);
                if (isRound) {
                    mXOffset = (bounds.width() / 2) - (mTextPaint.measureText(time) / 2);
                }
                canvas.drawText(time, mXOffset, mYOffset, mTextPaint);

                // Paint date
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("c MMM d");
                mDatePaint.setColor(mFontColorResource);
                String date = simpleDateFormat.format(calendar.getTime());
                if (isRound) {
                    mXOffset_date = (bounds.width() / 2) - (mDatePaint.measureText(date) / 2);
                }
                canvas.drawText(date, mXOffset_date, mYOffset_date, mDatePaint);

                // Paint mMax
                mTemperaturePaint.setColor(mFontColorResource);
                if (mMax != null) {
                    String max = mMax.concat(mGrade);
                    if (isRound) mXOffset_temperature_max = (bounds.width() / 2)
                            - getResources().getDimension(R.dimen.max_temperature_x_offset_round)
                            - mTemperaturePaint.measureText(max);
                    canvas.drawText(max, mXOffset_temperature_max, mYOffset_temperature
                            , mTemperaturePaint);
                }
                // Paint mMin
                if (mMin != null) {
                    if (isRound) mXOffset_temperature_min = (bounds.width() / 2) +
                            getResources().getDimension(R.dimen.min_temperature_x_offset_round);
                    canvas.drawText(mMin.concat(mGrade), mXOffset_temperature_min, mYOffset_temperature
                            , mTemperaturePaint);
                }

                // Init animation bounds.
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), mDrawableResource);
                if (bitmap != null) {

                    if (mXOffset_image_anim == 0 || mYOffset_image_anim == 0) {
                        if (isRound) {
                             mXOffset_image_anim =  (bounds.width() / 2) - (bitmap.getWidth() / 2);
                            mYOffset_image_anim = bounds.height();
                        } else {
                             mXOffset_image_anim = bounds.width();
                             mYOffset_image_anim = bounds.height();
                        }
                    }
                    if (isRound && mYOffset_image_anim > mYOffset_image) {
                        mYOffset_image_anim -= getResources()
                                .getInteger(R.integer.animation_y_offset_round);
                        mInvalidate = true;
                    } else if (!isRound &&  (mXOffset_image_anim > mXOffset_image
                            && mYOffset_image_anim > mYOffset_image)) {
                        mYOffset_image_anim -= getResources().getInteger(R.integer.animation_y_offset);
                        mXOffset_image_anim -= getResources().getInteger(R.integer.animation_x_offset);
                        mInvalidate = true;
                    }

                    // Paint image
                    if (mDrawableResource != -1) {
                        canvas.drawBitmap(bitmap, mXOffset_image_anim, mYOffset_image_anim, null);
                    }

                    if (mInvalidate) invalidate();
                }
            }
         }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        /**
         * Handle weather updates
         * @param weatherDataField
         */
        public void onEvent (final WeatherDataField weatherDataField) {
            if (weatherDataField.getMax() != null) {
                Log.i(LOG_TAG, "Got new mMax: " + weatherDataField.getMax());
                mMax = weatherDataField.getMax().toString();
            }
            if (weatherDataField.getMin() != null) {
                Log.i(LOG_TAG, "Got new mMin: " + weatherDataField.getMin());
               mMin = weatherDataField.getMin().toString();
            }
            if (weatherDataField.getWeather_id() != null) {
                Log.i(LOG_TAG, "Got mWeather_id: " + weatherDataField.getWeather_id());
                mDrawableResource = Util.getArtResourceForWeatherCondition(weatherDataField
                        .getWeather_id());
                mBackgroundColorResource = getResources().getColor(Util.getBackgroundColor(weatherDataField.getWeather_id()));
                mFontColorResource = getResources().getColor(Util.getFontColor(weatherDataField.getWeather_id()));
            }
        }



    }

    class SendToDataLayerThread extends Thread {
        String path;
        DataMap dataMap;

        // Constructor for sending data objects to the data layer
        SendToDataLayerThread(String p, DataMap data) {
            path = p;
            dataMap = data;
        }

        public void run() {
            // Construct a DataRequest and send over the data layer
            PutDataMapRequest putDMR = PutDataMapRequest.create(path);
            putDMR.getDataMap().putAll(dataMap);
            PutDataRequest request = putDMR.asPutDataRequest();

            DataApi.DataItemResult result = Wearable.DataApi
                    .putDataItem(SunshineWatchFace.googleApiClient, request).await();
            if (result.getStatus().isSuccess()) {
                Log.v(LOG_TAG, "DataMap: " + dataMap + " sent successfully to data layer ");
            }
            else {
                // Log an error
                Log.v(LOG_TAG, "ERROR: failed to send DataMap to data layer");
            }
        }
    }

}
