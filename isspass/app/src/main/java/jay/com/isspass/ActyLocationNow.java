package jay.com.isspass;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class ActyLocationNow extends Activity {
    private static final String TAG = ActyLocationNow.class.getSimpleName();

    private ProgressDialog mPrgDiag;
    private AsTaskRecSpec mAsTaskRead;

    private TextView mTxtLocNowLat, mTxtLocNowLong;
    private TextView mTxtLastUpdate;


    private static String getStrURL() {
        return "http://api.open-notify.org/iss-now.json";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

   //     Log.d(TAG, "...onCreate()");
        setContentView(R.layout.ac_location_now);


        mTxtLocNowLat = (TextView) findViewById(R.id.tvwLocNowLat);
        mTxtLocNowLong = (TextView) findViewById(R.id.tvwLocNowLong);
        mTxtLastUpdate = (TextView) findViewById(R.id.tvwUpdate);

    }

    @Override
    public void onResume() {
        super.onResume();
        mAsTaskRead = new AsTaskRecSpec(getStrURL());
        mAsTaskRead.execute();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private class AsTaskRecSpec extends AsyncTask<Void, Void, String> {
        private String mStrUrl;

        public AsTaskRecSpec(String aStrURL) {
            mStrUrl = aStrURL;
        }

        @Override
        protected String doInBackground(Void... v) {
            String result = "";
            HttpURLConnection urlConnection = null;
            URL url;
            try {
                url = new URL(mStrUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() != 200) {
//                    Log.d( TAG, "responsecode=" + urlConnection.getResponseCode() );
                    return null;
                }
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                if (in == null)
                    return null;
                result = convertInputStreamToString(in);

            } catch (Exception e) {
                Log.e(TAG, "background e=" + e.getMessage());
                return null;

            } finally {
                urlConnection.disconnect();
            }
            return result;
        }


        @Override
        protected void onPostExecute(String aStrRes) {
            if (mPrgDiag == null) {
                //     Log.d(TAG, "mPrgDiag null");
                return;
            }
            if (mPrgDiag.isShowing()) {
                mPrgDiag.dismiss();
                mPrgDiag = null;
            }

            if ( aStrRes == null ) {
                return;
            }

            String wStrLat;
            String wStrLon;
            int wiTS = 0;
            try {
                JSONObject json = new JSONObject(aStrRes);
                String wStrTS = json.getString("timestamp");
                try {
                    wiTS = Integer.parseInt(wStrTS);
                } catch (Exception e) {
                    Log.e(TAG, "int error");
                }
                JSONObject jsIssPos = json.getJSONObject("iss_position");
                wStrLat = jsIssPos.getString("latitude");
                wStrLon = jsIssPos.getString("longitude");
            } catch (Exception e) {
                return;
            }

            mTxtLocNowLat.setText( "Lat.: " + wStrLat );
            mTxtLocNowLong.setText( "Long: " + wStrLon );
            DateFormat wDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            java.util.Date time = new java.util.Date((long)wiTS*1000);
            mTxtLastUpdate.setText( wDateFormat.format(time));
        }

        @Override
        protected void onPreExecute() {
            mPrgDiag = new ProgressDialog(ActyLocationNow.this);
            mPrgDiag.setMessage("Processing");
            mPrgDiag.show();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }


        private String convertInputStreamToString(InputStream inputStream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while ((line = bufferedReader.readLine()) != null)
                result += line;

            inputStream.close();
            return result;

        }

    }           // end class AsTask

}
/*
{"message": "success", "timestamp": 1525228928, "iss_position": {"longitude": "85.6784", "latitude": "51.3619"}}
 */