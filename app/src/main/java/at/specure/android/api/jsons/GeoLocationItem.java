package at.specure.android.api.jsons;

public class GeoLocationItem {

    public final long tstamp;
    public final long tstampNano;
    public final double geo_lat;
    public final double geo_long;
    public final float accuracy;
    public final double altitude;
    public final float bearing;
    public final float speed;
    public final String provider;

    public GeoLocationItem(final long tstamp, final double geo_lat, final double geo_long, final float accuracy,
                           final double altitude, final float bearing, final float speed, final String provider) {
        this.tstamp = tstamp;
        this.tstampNano = System.nanoTime();
        this.geo_lat = geo_lat;
        this.geo_long = geo_long;
        this.accuracy = accuracy;
        this.altitude = altitude;
        this.bearing = bearing;
        this.speed = speed;
        this.provider = provider;
    }

}
