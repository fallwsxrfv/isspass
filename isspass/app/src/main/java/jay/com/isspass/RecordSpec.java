package jay.com.isspass;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class RecordSpec {
    private int miDuration, miRaiseTime;
    private static final String TAG = RecordSpec.class.getSimpleName();
    private DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public RecordSpec( int aiDur, int aiRaise ) {
        miDuration = aiDur;
        miRaiseTime = aiRaise;
    }
    public String getStrDur() {
        return "" + miDuration;
    }

    public String getStrRaiseTime() {
        return "" + miRaiseTime;
    }

    public String getStrRaiseTimeDate() {
        java.util.Date time = new java.util.Date((long)miRaiseTime*1000);
        return mDateFormat.format(time);
    }
}
