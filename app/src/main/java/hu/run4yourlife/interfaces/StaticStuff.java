package hu.run4yourlife.interfaces;

import hu.run4yourlife.RunningService;

/**
 * Dani
 */
public class StaticStuff {
    public static int LOCATION_DELAY_METERS = 2;
    public static String WeatherApiKey = null;

    public static String NOTIF_CHANNEL_ID = "notification";

    public static String RUNDB_NAME = "FUTASOK";

    public static String RUN_EXTRA_ID_NAME = "ID";
    public static Float RED_ZONE = 4.0f;
    public static Float ORANGE_ZONE = 2.5f;
    public static int  LOCATION_DELAY_MILLIS = 2000;
    public static RunningService.GPSCoordinate cachedCoord = null;
    public static RecommendedTime recommendedTime = null;
}
