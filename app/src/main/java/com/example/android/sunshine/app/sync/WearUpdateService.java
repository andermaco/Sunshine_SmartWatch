package com.example.android.sunshine.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.example.android.sunshine.app.MainActivity;
import com.example.android.sunshine.app.lib.WeatherDataField;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Timer;
import java.util.TimerTask;

public class WearUpdateService extends Service implements ConnectionCallbacks, OnConnectionFailedListener {

    private static final String LOG_TAG = WearUpdateService.class.getSimpleName();
    public static final long NOTIFY_INTERVAL = 10 * 1000; // 10 seconds
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    WeatherDataField weather;
    String path;
    public static Boolean serviceRunning = false;
    private final IBinder binder = new MyBinder();

    public WearUpdateService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "onStartCommand");
        weather = (WeatherDataField) intent.getExtras().get("weatherObj");
        path = (String) intent.getExtras().get("path");;
        // schedule task
        mTimer.scheduleAtFixedRate(new UpdateWeatherTask(), 5, NOTIFY_INTERVAL);
        serviceRunning = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "onCreate");
        if(mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public WeatherDataField getWeather() {
        return weather;
    }

    public void setWeather(WeatherDataField weather) {
        Log.i(LOG_TAG, "setWeather: " + weather.getMax() + " " + weather.getMin());
        this.weather = weather;
    }

    @Override
    public void onDestroy() {
        serviceRunning = false;
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

    class UpdateWeatherTask extends TimerTask {
        private final String LOG_TAG = UpdateWeatherTask.class.getSimpleName();

        @Override
        public void run() {
            Log.i(LOG_TAG, "UpdateWeatherTask.run");

            if (!MainActivity.googleApiClient.isConnected()) {
                MainActivity.googleApiClient.connect();
            }
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi
                        .getConnectedNodes(MainActivity.googleApiClient).await();
            Log.i(LOG_TAG, "nodes: " + nodes.getNodes());
            for (Node node : nodes.getNodes()) {
                DataMap dataMap = new DataMap();
                dataMap.putString("short_desc", weather.getShort_desc());
                dataMap.putInt("max", weather.getMax());
                dataMap.putInt("min", weather.getMin());
                dataMap.putInt("weather_id", weather.getWeather_id());
                dataMap.putLong("Time", System.currentTimeMillis());
                byte[] rawdata = dataMap.toByteArray();
                Wearable.MessageApi.sendMessage(MainActivity.googleApiClient, node.getId(),
                        WeatherDataField.MESSAGE_PATH, rawdata).await();
            }
        }

    }

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    public class MyBinder extends Binder {
        public WearUpdateService getService() {
            return WearUpdateService.this;
        }
    }

}
