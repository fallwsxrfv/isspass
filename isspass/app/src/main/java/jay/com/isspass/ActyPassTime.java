package jay.com.isspass;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class ActyPassTime extends Activity{
    public static final String BROADCAST_ACTION = "com.jay.broadcast.data";
    private static final String TAG = ActyPassTime.class.getSimpleName();
    private static final int K_I_REQUEST_CODE_ACCESS_LOCATION = 1;
    private static final String LOC_LAT = "latitude";
    private static final String LOC_LON = "longitude";

    private IntentFilter mIntentFilter;
    private ProgressDialog mPrgDiag;
    private AsTaskRecSpec mAsTaskRead;

    private RecyclerView mRcvlView;
    private RecyclerView.Adapter mAdapRcvl;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView mTxtCurrentLocation;
    private double mdLat, mdLon;


    private static String getStrURL( double adLat, double adLon   ) {
        return "http://api.open-notify.org/iss-pass.json?lat=" + adLat + "&lon=" + adLon;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Log.d( TAG, "...onCreate()ggggggg");
        setContentView(R.layout.recly_main);

        verifyAccessLocationPermissions();

//        Intent intent = new Intent( this, LocationService.class );
//        startService( intent );

        mTxtCurrentLocation = (TextView)findViewById(R.id.my_txtCurrentLocation);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BROADCAST_ACTION);

        mRcvlView = null;
        mLayoutManager = null;

        mRcvlView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRcvlView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRcvlView.setLayoutManager(mLayoutManager);

        // recovering the instance state
        if (savedInstanceState != null) {
            mdLat = savedInstanceState.getDouble(LOC_LAT);
            mdLon = savedInstanceState.getDouble(LOC_LON);
            mTxtCurrentLocation.setText( "Lat=" + mdLat + " " + "Lon=" + mdLon );
            procRecreateRecycler();
        }

//        procRecreateRecycler( 49.1234, 33.3 );


    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isGpsPermissionGranted() {
        return  ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void procStartService() {
        Log.d( TAG, "procStartService()");

        if ( isMyServiceRunning( LocationService.class ))
            return;
        Intent intent = new Intent( this, LocationService.class );
        startService( intent );
    }

    private void procStopService() {
  //      Log.d( TAG, "procStopService()");
        if ( !isMyServiceRunning( LocationService.class ))
            return;
        stopService( new Intent( this, LocationService.class ));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putDouble( LOC_LAT, mdLat);
        savedInstanceState.putDouble( LOC_LON, mdLon);
        super.onSaveInstanceState(savedInstanceState);
    }
    /**
     * Checks if the app has permission
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     */
    private void verifyAccessLocationPermissions() {

        final String[] PERMISSIONS_ACCESS = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        if ( isGpsPermissionGranted())
            return;

        // We don't have permission so prompt the user
        ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_ACCESS,
                K_I_REQUEST_CODE_ACCESS_LOCATION
        );
        //   this.finish();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case K_I_REQUEST_CODE_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0  &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    for ( int i = 0; i < permissions.length; i++ )
//                        Log.d( TAG, permissions[i]);
                    return;
                }
                Toast.makeText(getApplicationContext(), "Apps needs Access device location permission for to work", Toast.LENGTH_LONG).show();
                this.finish();
                return;
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (mReceiver != null && mIntentFilter != null)
                registerReceiver(mReceiver, mIntentFilter);
        } catch ( IllegalArgumentException e ) {
            Log.e( TAG, "resume/register");
            e.printStackTrace();
        }
        if ( isGpsPermissionGranted() )
            procStartService();
    }

    @Override
    protected void onPause() {
        try {
            if (mReceiver != null)
                unregisterReceiver(mReceiver);
        } catch ( IllegalArgumentException e ) {
            Log.e( TAG, "pause/register");
            e.printStackTrace();
        }
        procStopService();
        super.onPause();
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
    //        Log.d( TAG, "....Broadcast From Service");
            if (intent.getAction().equals( BROADCAST_ACTION)) {
                mdLat = intent.getDoubleExtra( MyLocationListener.KEY_LATITUDE, 40.7128 );
                mdLon = intent.getDoubleExtra( MyLocationListener.KEY_LONGITUDE, -74.0060 );
                mTxtCurrentLocation.setText( "Lat=" + mdLat + " " + "Lon=" + mdLon );
         //       Log.d( TAG, "lat =" + mdLat + "lon= " + mdLon );
                procRecreateRecycler();
            }
        }
    };

    @Override
    public void onBackPressed() {
        stopService( new Intent(this, LocationService.class));
        finish();
    }


    private void procRecreateRecycler() {
        mAsTaskRead = new AsTaskRecSpec(  getStrURL( mdLat, mdLon ));
        mAsTaskRead.execute();
    }


    private class AsTaskRecSpec extends AsyncTask<Void, Void, Boolean> {
        private ArrayList<RecordSpec> mArlRecSpec;
        private String mStrUrl;
        public AsTaskRecSpec(String aStrURL ) {
            mArlRecSpec = new ArrayList<RecordSpec>();
            mStrUrl = aStrURL;
        }

        @Override
        protected Boolean doInBackground(Void... v) {
            if ( procRestData() == null )
                return false;
            return true;
        }


        @Override
        protected void onPostExecute(Boolean aBol) {
            if (mPrgDiag == null) {
                //     Log.d(TAG, "mPrgDiag null");
                return;
            }
            if (mPrgDiag.isShowing()) {
                mPrgDiag.dismiss();
                mPrgDiag = null;
            }

            if (!aBol.booleanValue()) {
                return;
            }

            mAdapRcvl = new MyAdapter( mArlRecSpec);
            mAdapRcvl.notifyDataSetChanged();
            mRcvlView.setAdapter(mAdapRcvl);

        }

        @Override
        protected void onPreExecute() {
            mPrgDiag = new ProgressDialog(ActyPassTime.this);
            mPrgDiag.setMessage("Processing");
            mPrgDiag.show();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        private  String procRestData(){
            String result = "";
            HttpURLConnection urlConnection = null;
            URL url;
            try {
                url = new URL(mStrUrl);
                urlConnection = (HttpURLConnection)url.openConnection();
                if ( urlConnection.getResponseCode() != 200 ) {
//                    Log.d( TAG, "responsecode=" + urlConnection.getResponseCode() );
                    return null;
                }
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                if ( in == null )
                    return null;
                result = convertInputStreamToString(in);

            }  catch ( Exception e ) {
                Log.e( TAG, "background e=" + e.getMessage());
                return null;

            }  finally {
                urlConnection.disconnect();
            }

            procPopulateArrayList( result );
            return result;
        }

        private void  procPopulateArrayList ( String aStr ) {

            try {
                JSONObject json = new JSONObject(aStr);
                JSONArray jsonArlResponse = json.getJSONArray("response");
                for ( int i = 0;  i < jsonArlResponse.length(); i++ ) {
                    JSONObject jsonobjResponse = jsonArlResponse.getJSONObject( i );
                    String wStrDur = jsonobjResponse.getString( "duration" );
                    String wStrRaise = jsonobjResponse.getString( "risetime" );
                    int wiDur = 0;
                    int wiRaise = 0;
                    try {
                        wiDur = Integer.parseInt( wStrDur );
                        wiRaise = Integer.parseInt(wStrRaise );
                    } catch ( Exception e ) {
                        Log.e( TAG, "int error");
                        continue;
                    }
                    mArlRecSpec.add( new RecordSpec( wiDur, wiRaise ));
                }
            } catch ( Exception e ) {
            }


        }

        private String convertInputStreamToString(InputStream inputStream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while((line = bufferedReader.readLine()) != null)
                result += line;

            inputStream.close();
            return result;

        }

    }           // end class AsTask

}
//  https://android.jlelse.eu/local-broadcast-less-overhead-and-secure-in-android-cfa343bb05be

/*
http://api.open-notify.org/iss-pass.json?lat=39&lon=-75
{
  "message": "success",
  "request": {
    "altitude": 100,
    "datetime": 1521235290,
    "latitude": 49.0,
    "longitude": -33.0,
    "passes": 5
  },
  "response": [
    {
      "duration": 615,
      "risetime": 1521255673
    },
    {
      "duration": 621,
      "risetime": 1521261458
    },
    {
      "duration": 526,
      "risetime": 1521267344
    },
    {
      "duration": 527,
      "risetime": 1521273206
    },
    {
      "duration": 621,
      "risetime": 1521278997
    }
  ]
}
 */