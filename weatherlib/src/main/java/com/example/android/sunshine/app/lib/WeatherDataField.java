package com.example.android.sunshine.app.lib;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.wearable.DataMap;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class WeatherDataField implements Parcelable {
    public static final String PATH_RESPONSE = "/weather-data";
    public static final String PATH_REQUEST = "/weather-data-request";
    public static final String MESSAGE_PATH = "/sunshine/update";

    private String short_desc;
    private Integer max;
    private Integer min;
    private Integer weather_id;

    public WeatherDataField() {}

    public WeatherDataField(DataMap dataMap) {
        setShort_desc(dataMap.getString("short_desc"));
        setMax(dataMap.getInt("max"));
        setMin(dataMap.getInt("min"));
        setWeather_id(dataMap.getInt("weather_id"));
    }

    public String getShort_desc() {
        return short_desc;
    }

    public void setShort_desc(String short_desc) {
        this.short_desc = short_desc;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getWeather_id() {
        return weather_id;
    }

    public void setWeather_id(Integer weather_id) {
        this.weather_id = weather_id;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        WeatherDataField rhs = (WeatherDataField) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(short_desc, rhs.short_desc).append(max, rhs.max).append(min, rhs.min)
                .append(weather_id, rhs.weather_id)
                .isEquals();
    }
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).
                append(short_desc).
                append(max).
                append(min).
                append(weather_id).
                toHashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

     @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(short_desc);
        if (max == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(max);
        }
        if (min == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(min);
        }
        if (weather_id == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(weather_id);
        }
    }

    protected WeatherDataField(Parcel in) {
        short_desc = in.readString();
        max = in.readByte() == 0x00 ? null : in.readInt();
        min = in.readByte() == 0x00 ? null : in.readInt();
        weather_id = in.readByte() == 0x00 ? null : in.readInt();
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<WeatherDataField> CREATOR = new Parcelable.Creator<WeatherDataField>() {
        @Override
        public WeatherDataField createFromParcel(Parcel in) {
            return new WeatherDataField(in);
        }

        @Override
        public WeatherDataField[] newArray(int size) {
            return new WeatherDataField[size];
        }
    };

}
