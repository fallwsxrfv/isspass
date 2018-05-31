package jay.com.isspass;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;


public class MyLocationListener implements LocationListener {

    public static final String KEY_LATITUDE = "key_latitude";
    public static final String KEY_LONGITUDE = "key_longitude";

    private static final String TAG  = MyLocationListener.class.getSimpleName();
    private static final double MIN_DISTANCE = 0.050d;     // Kilometers

    private Location mLoc;
    private double mdLastLatSend, mdLastLongSend;
    private LocationService mServ;
    public MyLocationListener(String aStrProv, LocationService aServ ) {
        mServ = aServ;
        mdLastLongSend = 0d;
        mdLastLatSend = 0d;
        mLoc = new Location( aStrProv);
    }
    @Override
    public void onLocationChanged(Location loc) {
        mLoc.set( loc );
        Log.d(TAG,  "Location changed: Lat: " + loc.getLatitude() + " Lng: " + loc.getLongitude());

        double wdDist = distance( loc.getLatitude(), loc.getLongitude(), mdLastLatSend, mdLastLongSend, "K" );
        if ( wdDist > MIN_DISTANCE ) {
            mdLastLatSend = loc.getLatitude();
            mdLastLongSend = loc.getLongitude();
            procBroadcast( loc.getLatitude(), loc.getLongitude() );
        } else {
            Log.d( TAG, "min distance");
        }
    }
    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    private void procBroadcast(final double aLat, final double aLon) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ActyPassTime.BROADCAST_ACTION);
        broadcastIntent.putExtra(KEY_LATITUDE, aLat );
        broadcastIntent.putExtra(KEY_LONGITUDE, aLon );

        mServ.sendBroadcast(broadcastIntent);
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts decimal degrees to radians						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}

//  https://www.geodatasource.com/developers/java
// https://www.movable-type.co.uk/scripts/latlong.html
//https://www.gps-coordinates.net/
//  https://stackoverflow.com/questions/1513485/how-do-i-get-the-current-gps-location-programmatically-in-android