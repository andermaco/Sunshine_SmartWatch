package com.example.android.sunshine.app.lib;

/**
 * Created by cammac on 1/02/16.
 */
public class Util {
    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }

    /**
     * Gets background color based on weatherId
     * @param weatherId
     * @return
     */
    public static int getBackgroundColor(int weatherId) {
        if (weatherId >= 200 && weatherId <= 232) {
            return R.color.storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.color.light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.color.rain;
        } else if (weatherId == 511) {
            return R.color.snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.color.rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.color.snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.color.fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.color.storm;
        } else if (weatherId == 800) {
            return R.color.clear;
        } else if (weatherId == 801) {
            return R.color.light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.color.clouds;
        }
        return -1;
    }

     public static int getFontColor(int weatherId) {
        if (weatherId >= 200 && weatherId <= 232) {
            return R.color.font_clear;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.color.font_dark;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.color.font_clear;
        } else if (weatherId == 511) {
            return R.color.font_dark;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.color.font_clear;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.color.font_dark;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.color.font_dark;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.color.font_clear;
        } else if (weatherId == 800) {
            return R.color.font_clear;
        } else if (weatherId == 801) {
            return R.color.font_dark;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.color.font_clear;
        }
        return -1;
    }

    /**
     * Gets image to animate based on weatherId
     * @param weatherId
     * @return
     */
    public static int getAnimation (int weatherId) {
         if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }

}
