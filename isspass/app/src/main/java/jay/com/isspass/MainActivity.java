package jay.com.isspass;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button wBtnLocationNow = (Button)findViewById(R.id.btnLocationNow);
        wBtnLocationNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity( new Intent( MainActivity.this, ActyLocationNow.class));

            }
        });

        Button wBtnPassTime = (Button)findViewById(R.id.btnPassTimes);
        wBtnPassTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity( new Intent( MainActivity.this, ActyPassTime.class));

            }
        });

        Button wBtnCrew = (Button)findViewById(R.id.btnCrew );
        wBtnCrew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startActivity( new Intent( MainActivity.this, ActyCrew.class));

            }
        });

    }
}
