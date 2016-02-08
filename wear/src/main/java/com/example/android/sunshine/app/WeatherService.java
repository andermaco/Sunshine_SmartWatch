package com.example.android.sunshine.app;

import android.util.Log;

import com.example.android.sunshine.app.lib.WeatherDataField;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import de.greenrobot.event.EventBus;

/**
 * Updates wear devices on a scheduled time basis.
 */
public class WeatherService extends WearableListenerService {
    public static final String LOG_TAG = WeatherService.class.getSimpleName();

    public WeatherService() {
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent dataEvent: dataEvents) {
            Log.i(LOG_TAG, "DataEvent received " + dataEvent.getType());
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                String path = dataEvent.getDataItem().getUri().getPath();
                WeatherDataField weatherDataField = new WeatherDataField();
                if (path.equals("/weather-data")) {
                    weatherDataField.setShort_desc(dataMap.getString("short_desc"));
                    Log.i(LOG_TAG, "short_desc: " + weatherDataField.getShort_desc());
                    weatherDataField.setMax(dataMap.getInt("max"));
                    Log.i(LOG_TAG, "max: " + weatherDataField.getMax());
                    weatherDataField.setMin(dataMap.getInt("min"));
                    Log.i(LOG_TAG, "min: " + weatherDataField.getMin());
                    weatherDataField.setWeather_id(dataMap.getInt("weather_id"));
                    Log.i(LOG_TAG, "weather_id: " + weatherDataField.getWeather_id());
                }
                EventBus.getDefault().post(weatherDataField);
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(WeatherDataField.MESSAGE_PATH)) {
            byte[] data = messageEvent.getData();
            DataMap dataMap = DataMap.fromByteArray(data);
            WeatherDataField weatherDataField = new WeatherDataField(dataMap);
            EventBus.getDefault().post(weatherDataField);
        }
    }
}
