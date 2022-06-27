package com.example.serviceexample;

import android.app.Activity;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MyService extends Service{
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    private static final String REQUEST_METHOD = "GET";
    private static final int READ_TIMEOUT = 15000;
    private static final int CONNECTION_TIMEOUT = 15000;

//    private String ticker = "MSFT";
    private String token ="c8vgk7aad3icdhue95lg"; // put your own token
//    private int result_id = 0;

    private final class ServiceHandler extends Handler{//extended handler class to tell the handler what to do when receive message
        public ServiceHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg){

            // url to get historical data
            String stringUrl ;
            String result;
            String inputLine;
            String ticker = String.valueOf(msg.obj).toUpperCase();
            int result_id = msg.arg2;

            try {

                // make GET requests
                stringUrl = "https://finnhub.io/api/v1/stock/candle?symbol="+ticker
                        +"&resolution=D&from=1625097601&to=1640995199&token="+token;
                URL myUrl = new URL(stringUrl);
                HttpURLConnection connection =(HttpURLConnection) myUrl.openConnection();

                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                connection.connect();

                // store json string from GET response

                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();

                while((inputLine = reader.readLine()) != null){
                    stringBuilder.append(inputLine);
                }

                reader.close();
                streamReader.close();

                result = stringBuilder.toString();

            } catch(IOException e) {
                e.printStackTrace();
                result = null;
                Thread.currentThread().interrupt();
            }

            // parse the json string into 'close' and 'volume' array

            JSONObject jsonObject = null;
            JSONArray jsonArrayClose = null;
            JSONArray jsonArrayVolume = null;
            JSONArray jsonArrayTime = null;

            try {
                jsonObject = new JSONObject(result);
                jsonArrayClose = jsonObject.getJSONArray("c");
                jsonArrayVolume = jsonObject.getJSONArray("v");
                jsonArrayTime = jsonObject.getJSONArray("t");
            } catch (JSONException e) {e.printStackTrace();}


            Log.v("close", String.valueOf(jsonArrayClose.length()));
            Log.v("vol", String.valueOf(jsonArrayVolume.length()));
            Log.v("vol", String.valueOf(jsonArrayTime.length()));

            try {
                for (int i = 0; i < jsonArrayClose.length(); i++) {
                    double close = jsonArrayClose.getDouble(i);
                    double volume = jsonArrayVolume.getDouble(i);
                    String time = jsonArrayTime.getString(i);
                    //logging so that we can see the operations in logcat
                    Log.v("data", i + ":, c: " + close + " v: " + volume + "t: " + time);
                    //database operations of inserting the data into the content provider
                    //persisted in sqlite db
                    ContentValues values = new ContentValues();
                    values.put(HistoricalDataProvider.CLOSE, close);
                    values.put(HistoricalDataProvider.VOLUME, volume);
                    values.put(HistoricalDataProvider.NAME, ticker);
                    values.put(HistoricalDataProvider.TIME, time);
                    //HDP.content_uri is to get the connection to the sqlite db
                    //getContentResolver() is a method from extending service
                    getContentResolver().insert(HistoricalDataProvider.CONTENT_URI, values);
                }
            } catch (JSONException e) {e.printStackTrace();}

            // broadcast message that download is complete

            Intent intent = new Intent("DOWNLOAD_COMPLETE");
            //broadcast for them to know what to do next
            //one of the java will take the intent and do certain actions
            intent.putExtra("ticker", String.valueOf(ticker));
            intent.putExtra("result", result_id);
            sendBroadcast(intent);
            Log.v("Service looper", "Done here ");
            Log.v("ticker is ", ticker);

            stopSelf(msg.arg1);

        }
    }

    @Override
    public void onCreate(){
        HandlerThread thread = new HandlerThread("Service", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }


    //when startService is called in the main (intent) is passed
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
//        ticker = intent.getStringExtra("ticker"); //putExtra then now can retrieve (key,value)
//        result_id = intent.getIntExtra("result", 0 );
        getContentResolver().delete(HistoricalDataProvider.CONTENT_URI,null,null);
        Log.v("nimama =" , String.valueOf(intent.getIntExtra("result", 0 )));
        Toast.makeText(this, "download starting", Toast.LENGTH_SHORT).show();
        int res[] = intent.getIntArrayExtra("ids");
        String tickers[] = intent.getStringArrayExtra("tickers");
        Log.v("attributes", tickers[0] + String.valueOf(res[0]));
        for(int pos = 0 ; pos < res.length ; pos++){
            if(res[pos] != 0 ){
                Message msg = serviceHandler.obtainMessage();
                msg.arg1 = startId;
                msg.arg2 = res[pos];
                msg.obj = tickers[pos];
                serviceHandler.sendMessage(msg);
            }
        }
//        Message msg = serviceHandler.obtainMessage();
//        msg.arg1 = startId;
//        msg.arg2 = intent.getIntExtra("result", 0 );
//        msg.obj = intent.getStringExtra("ticker");
//        serviceHandler.sendMessage(msg);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onDestroy(){ Toast.makeText(this, "download done", Toast.LENGTH_SHORT).show(); }
}
