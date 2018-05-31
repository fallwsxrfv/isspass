package jay.com.isspass;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
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


public class ActyCrew extends Activity {
    private static final String TAG = ActyCrew.class.getSimpleName();

    private ProgressDialog mPrgDiag;
    private AsTaskRecSpec mAsTaskRead;

    private TextView mTxtTotalMember;
    private TextView mTxtCrew;


    private static String getStrURL() {
        return "http://api.open-notify.org/astros.json";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

     //   Log.d(TAG, "...onCreate()");
        setContentView(R.layout.ac_crew);

        mTxtTotalMember = (TextView) findViewById(R.id.tvwTotalMember);
        mTxtCrew = (TextView) findViewById(R.id.tvwCrew);
        TextView wTxtWeb = (TextView)findViewById(R.id.tvwWeb);

        wTxtWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://http://api.open-notify.org/"));
                startActivity(intent);
            }
        });


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

            String wStrNumber;
            StringBuffer wStrBuf = new StringBuffer();
            try {
                JSONObject json = new JSONObject(aStrRes);
                wStrNumber = json.getString("number");
                JSONArray jsonArlResponse = json.getJSONArray("people");
                for ( int i = 0;  i < jsonArlResponse.length(); i++ ) {
                    JSONObject jsonobjResponse = jsonArlResponse.getJSONObject(i);
                    String wStrName = jsonobjResponse.getString("name");
                    wStrBuf.append( wStrName + "\n" );
                }
            } catch (Exception e) {
                return;
            }
            mTxtTotalMember.setText( "Total Members: "+ wStrNumber );
            mTxtCrew.setText( wStrBuf.toString() );
        }

        @Override
        protected void onPreExecute() {
            mPrgDiag = new ProgressDialog(ActyCrew.this);
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
{"people": [{"name": "Anton Shkaplerov", "craft": "ISS"}, {"name": "Scott Tingle", "craft": "ISS"}, {"name": "Norishige Kanai", "craft": "ISS"}, {"name": "Oleg Artemyev", "craft": "ISS"}, {"name": "Andrew Feustel", "craft": "ISS"}, {"name": "Richard Arnold", "craft": "ISS"}], "number": 6, "message": "success"}
 */