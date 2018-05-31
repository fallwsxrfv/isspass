package jay.com.isspass;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service {
    private static final String TAG = LocationService.class.getSimpleName();
    private LocationManager mLocMngr;
    private static final int LOC_POOLING_INTERVAL = 10000;       //   ms
    private static final float LOC_DISTANCE = 1000f;            // meters
    private MyLocationListener mLocLtnrGPS;
    private MyLocationListener mLocLtnrNetwProv;

    @Override
    public IBinder onBind( Intent aIt ){
        return null;
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
        super.onStartCommand(intent,flags, startId);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
//        Log.d( TAG, "onCreate()");
        if ( mLocMngr == null ) {
            mLocMngr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        }

        //      better solution would be switch provider automatically once one is not available

        if ( mLocMngr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
     //       Log.d( TAG, "Using GPS provider");
            mLocLtnrGPS = new MyLocationListener(LocationManager.GPS_PROVIDER, this);
            try {
                mLocMngr.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        LOC_POOLING_INTERVAL,
                        LOC_DISTANCE,
                        mLocLtnrGPS);
            } catch (SecurityException ex_sec) {
     //           Log.i(TAG, "fail to request location update, ignore", ex_sec);
            } catch (Exception e) {
      //          Log.d(TAG, "GPS provider does not exist, " + e.getMessage());
            }
        } else {
      //      Log.d( TAG, "Using Network provider");
            mLocLtnrNetwProv = new MyLocationListener(LocationManager.NETWORK_PROVIDER, this);
            try {
                mLocMngr.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        LOC_POOLING_INTERVAL,
                        LOC_DISTANCE,
                        mLocLtnrNetwProv);
            } catch (SecurityException ex_sec) {
    //            Log.i(TAG, "fail to request location update, ignore", ex_sec);
            } catch (Exception e) {
    //            Log.d(TAG, "Network provider does not exist, " + e.getMessage());
            }
        }
    }


    @Override
    public  void onDestroy() {
        super.onDestroy();
        if ( mLocMngr == null)
            return;
        try {
            if ( mLocLtnrGPS != null )
                mLocMngr.removeUpdates( mLocLtnrGPS);
            if ( mLocLtnrNetwProv != null )
                mLocMngr.removeUpdates( mLocLtnrNetwProv);

        } catch ( Exception e ) {
        }

    }
}       // end class


//  https://github.com/codepath/android_guides/issues/220
//  https://www.programcreek.com/java-api-examples/?code=aumarbello/WalkGraph/WalkGraph-master/app/src/main/java/com/example/ahmed/walkgraph/notifications/LocationService.java
//  https://developer.android.com/guide/components/services.html

//  https://stackoverflow.com/questions/15615471/change-my-location-provider-in-android-when-there-is-no-proper-location-updates

//      removeUpdateds()
//  https://stackoverflow.com/questions/5505429/switching-between-network-and-gps-provider/5505581#5505581

//      mock data
//  https://developer.android.com/guide/topics/location/strategies.html#MockData