package com.example.serviceexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.serviceexample.*;

public class MainActivity extends AppCompatActivity{

    private Button start, calc;
    private TextView result , result1 , result2 , result3;
    private EditText ticker , ticker1,ticker2 , ticker3;


//    Uri CONTENT_URI = Uri.parse("content://com.example.serviceexample.HistoricalDataProvider/history");
    private BroadcastReceiver myBroadcastReceiver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up layout

        setContentView(R.layout.activitymain);

        start = (Button) findViewById(R.id.start_button);
        calc = (Button) findViewById(R.id.calc_button);
        result = (TextView) findViewById(R.id.textview_result);
        result1 = (TextView) findViewById(R.id.textview_result2);
        result2 = (TextView)findViewById(R.id.textview_result3);
        result3 = (TextView)findViewById(R.id.textview_result4);
        ticker = (EditText) findViewById(R.id.edit_ticker);
        ticker1 = (EditText) findViewById(R.id.edit_ticker1);
        ticker2 = (EditText) findViewById(R.id.edit_ticker2);
        ticker3 = (EditText) findViewById(R.id.edit_ticker3);

        // start service, pass ticker info via an intent
        //button .setOnclick
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int resultids[] = {0,0,0,0,0};
                String tickers[] = new String[5];

                if(!String.valueOf(ticker.getText()).equals("")){
                    tickers[0] = String.valueOf(ticker.getText());
                    resultids[0] = R.id.textview_result;
                }
                if(!String.valueOf(ticker1.getText()).equals("")){
                    tickers[1] = String.valueOf(ticker1.getText());
                    resultids[1] = R.id.textview_result2;
                }
                if(!String.valueOf(ticker2.getText()).equals("")){
                    tickers[2] = String.valueOf(ticker2.getText());
                    resultids[2] = R.id.textview_result3;
                }
                if(!String.valueOf(ticker3.getText()).equals("")){
                    tickers[3] = String.valueOf(ticker3.getText());
                    resultids[3] = R.id.textview_result4;
                }

                Intent test = new Intent(getApplicationContext(),MyService.class);
                test.putExtra("ids", resultids);
                test.putExtra("tickers", tickers);
                startService(test);
            }
        });

        // register broadcast receiver to get informed that data is downloaded so that we can calc

        calc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result.setText("Waiting for data.. ");
                result1.setText("Waiting for data.. ");
                result2.setText("Waiting for data.. ");
                result3.setText("Waiting for data.. ");
//                myBroadcastReceiver = new MyBroadcastReceiver(new Handler(Looper.getMainLooper()));
//                registerReceiver(myBroadcastReceiver, new IntentFilter("DOWNLOAD_COMPLETE"));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        myBroadcastReceiver = new MyBroadcastReceiver(new Handler(Looper.getMainLooper()));
        registerReceiver(myBroadcastReceiver, new IntentFilter("DOWNLOAD_COMPLETE"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(myBroadcastReceiver);
    }


}