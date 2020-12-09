package com.example.myapplication;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.Format;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import java.net.*;
//import java.io.IOException;
import java.io.File;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.media.MediaPlayer;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private TextView Correlationlow;
    private TextView Correlationhigh;
    private TextView Correlation;
    public TextView th;
   // Button playBtn;
    private SeekBar volumeBar;
    public TextView Time;
    private Button resultButton;
    private RequestQueue mQueue;
    static //set arrays
    List<Double> corArray = new ArrayList<Double>();
    List<Double> avgCorrelationArray = new ArrayList<Double>();
    static List<Double> xlsxcorrelation = new ArrayList<Double>();
    //set Variable for keeping time range of the current/previous time window in the correlation array.
    static int iHead = 0;
    static int iTail = 0;

    MediaPlayer mpThree;

    int maxVolume=50;

    //set epoch time

    static long epoch = System.currentTimeMillis()+163000;
    static long tWinHead = epoch;
    //duration of the time window to average correlaiton
    static int durWin = 1000;//1000 msecond or 1 second
    //durWin = 3
    //threshold for feedback
    double thres_FB = 0.02;
    static double thres_FB_UP = 0.02;

    private int lenCorrArray = 0;


    /*********************************************************************/
    /***************************************/
    //average function
    public static double Average(List<Double> marks) {
        Double sum = 0.0;
        if (!marks.isEmpty()) {
            for (Double mark : marks) {
                sum = sum + mark;
            }
            return sum.doubleValue() / marks.size();
        }
        return sum;
    }
    /************************************************************************/


    /*****************************************************************************************************************/

    //correlation average function
    public static double correlationAverage() {
        Log.v("Warning", String.format("length of corrArrey: %d, iHead=%d, iTail=%d", corArray.size(), iHead, iTail));
        List<Double> headToTail = corArray.subList(iHead, iTail);
        //System.out.println((iTail-iHead+1));
        double correlationaverage = Average(headToTail);
        xlsxcorrelation.add(correlationaverage);
        //System.out.println(correlationaverage);

        return correlationaverage;

    }


    /****************************************************************************************************************/

    Thread t;
    Thread tt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Correlation = findViewById(R.id.correlation_result);
        Correlationhigh = findViewById(R.id.correlation_high);
        Correlationlow = findViewById(R.id.correlation_low);
        th = findViewById(R.id.threshold);
        Time = findViewById(R.id.timer);
        resultButton = findViewById(R.id.button_start);
        //playBtn = findViewById(R.id.playBtn);
        mQueue = Volley.newRequestQueue(this);


        /////////////////////////////////////////////////////////////////////
        mpThree = MediaPlayer.create(this, R.raw.songg);
        final MediaPlayer songThree = MediaPlayer.create(MainActivity.this, R.raw.songg);
        float log3=(float)(Math.log(maxVolume-48)/Math.log(maxVolume));
        mpThree.setVolume(log3,log3); //set volume takes two paramater
////////////////////////////////////////////////////////////////////////////////

        // Volume Bar
/*
        volumeBar =  findViewById(R.id.VolumeBar);
        volumeBar.setMax (50);
        new CountDownTimer(50000, 1000) {


            public void onTick(long volume) {


                Long x = volume/1000;
                Time.setText(String.valueOf(x));
                volumeBar.setProgress(x.intValue()); // the only one to be remain and should be set as the percentage of the correlation

            }

            public void onFinish() {
                Time.setText("lowest correlation percentage!");
            }
        }.start();
//        volumeBar.setOnSeekBarChangeListener(
//                new SeekBar.OnSeekBarChangeListener() {
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                        float volumeNum = progress / 100f;
//                        mp.setVolume(volumeNum, volumeNum);
//                    }
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {
//
//                    }
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {
//
//                    }
//                }
//        );
*/



        t = new Thread() {


            @Override
            public void run() {


                while (true) {
                    try {
                        Thread.sleep(1000); //should be set as 1 later on to one millisecond
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                long epoch = System.currentTimeMillis()+163000;
//                                lenCorrArray = corArray.size();
                                Log.v("Warning", String.format("before %d", epoch));
                                queryCorr(epoch);

                                if ((epoch - tWinHead) > durWin && iTail > iHead && corArray.size() > 0) {
                                    double correaltionaverage = correlationAverage();
                                    Correlation.setText(String.valueOf(correaltionaverage));
                                    if (corArray.size() != 0 && corArray.size() % 60 == 0) {

                                        Integer start = corArray.size() - 59;
                                        Integer end = corArray.size() - 1;

                                        avgCorrelationArray = corArray.subList(start, end);
                                        thres_FB_UP = Average(avgCorrelationArray);
                                        thres_FB = thres_FB_UP;

                                    }

                                    th.setText(String.valueOf(1.5*thres_FB));
                                    Log.d("correlation", String.valueOf(correaltionaverage));
                                    Log.d("threshold", String.valueOf(thres_FB_UP));
                                    if (correaltionaverage > 1.5 * thres_FB & correaltionaverage < 1.7 * thres_FB) {
                                        //th.setText(String.valueOf(thres_FB));


                                        songThree.start();
                                        Correlationhigh.setText(String.valueOf(correaltionaverage));
                                    } else if (correaltionaverage < 0.5 * thres_FB) {

//                                        Correlation.setText(String.valueOf(correaltionaverage));
                                        Correlationlow.setText(String.valueOf(correaltionaverage));
                                    }else if (correaltionaverage > 1.7 * thres_FB & correaltionaverage < 1.9 * thres_FB) {

                                        songThree.start();
                                        Correlationlow.setText(String.valueOf(correaltionaverage));
                                    }else if (correaltionaverage > 1.9 * thres_FB & correaltionaverage < 2.1 * thres_FB) {

                                        songThree.start();
                                        Correlationlow.setText(String.valueOf(correaltionaverage));
                                    }else if (correaltionaverage > 2.1 * thres_FB ) {

                                        songThree.start();
                                        Correlationlow.setText(String.valueOf(correaltionaverage));
                                    }

                                    tWinHead = tWinHead + 1;
                                    iHead = iTail;
                                    iTail = iHead;
                                }

                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }


        };

        tt = new Thread() {


            @Override
            public void run() {


                while (true) {
                    try {
                        Thread.sleep(1000); //should be set as 1 later on to one millisecond
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                long epoch = System.currentTimeMillis()+163000;
//                                lenCorrArray = corArray.size();
//                                Log.v("Warning", String.format("before %d", lenCorrArray));
                                mpThree.start();
                                queryCorr(epoch);

//                                if ((epoch - tWinHead) > durWin && iTail > iHead && corArray.size() > 0) {
//                                    double correaltionaverage = correlationAverage();
////                                    Correlation.setText(String.valueOf(correaltionaverage));
//////                                    if (corArray.size() != 0 && corArray.size() % 60 == 0) {
////
////                                        Integer start = corArray.size() - 59;
////                                        Integer end = corArray.size() - 1;
////
////                                        avgCorrelationArray = corArray.subList(start, end);
////                                        thres_FB_UP = Average(avgCorrelationArray);
////                                        thres_FB = thres_FB_UP;
////
////                                    }
////
////                                    th.setText(String.valueOf(thres_FB));
////                                    Log.d("correlation", String.valueOf(correaltionaverage));
////                                    Log.d("threshold", String.valueOf(thres_FB_UP));
////                                    if (correaltionaverage > 1.3 * thres_FB) {
////                                        //th.setText(String.valueOf(thres_FB));
////
////
////                                        song.start();
////                                        Correlationhigh.setText(String.valueOf(correaltionaverage));
////                                    } else if (correaltionaverage < 0.1 * thres_FB) {
////
////                                        Correlation.setText(String.valueOf(correaltionaverage));
////                                        Correlationlow.setText(String.valueOf(correaltionaverage));
////                                    }
//                                    tWinHead = tWinHead + 1;
//                                    iHead = iTail;
//                                    iTail = iHead;
//                                }

                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }


        };

    }

    public void BtnClick(View view) {
        t.start();
        tt.start();
    }
    //in case of need to see the volume
/*    public void play(View view) {


        if (!mp.isPlaying()) {

            // Stopping
            mp.start();
            playBtn.setBackgroundResource(R.drawable.stop);

        } else {
            // Playing
            mp.pause();
            playBtn.setBackgroundResource(R.drawable.play);
        }

    }*/
    /***************************************************************************************************************************************************************/





    /*************************************************************************************/
    //queryfunction
    public void queryCorr(long epoch) {
        final List<Double> meanCorrArrey = new ArrayList<Double>();

        String url = "http://192.168.13.100/api/correlations/"+epoch;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        Log.v("Warning", String.format("onRequest called!!!"));
                        try {
                            JSONObject data = response.getJSONObject("data");
                            JSONObject  cross = data.getJSONObject("354708094967841");
                            JSONArray crosscorrelation= cross.getJSONArray("crosscorrelations");

                            for (int i = 0; i < crosscorrelation.length(); i++){
                                double value = crosscorrelation.getJSONObject(i).getDouble("value");
                                //System.out.println(value);
                                meanCorrArrey.add(Math.abs(value));




                            }

                            Double meanvalue =Average(meanCorrArrey);
                            corArray.add(meanvalue);
                            iTail = iTail + 1;
                            Log.v("Warning", String.format("corArray in onRequest %d ", corArray.size()));
                            //Correlation.setText(String.valueOf(meanvalue));
                            /*int size= corArray.size();
                            if (size>3){List<Double> headToTail = corArray.subList(size-2, size);
                                double correlationaverage= Average(headToTail);}



                            Correlation.setText(String.valueOf(correlationaverage));
                            //System.out.println(corArray);*/
                        } catch (JSONException e) {
                            Log.v("Warning", String.format("its an error"));

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("Warning", String.format("its second  error"));
                error.printStackTrace();
            }
        });

        mQueue.add(request);
    }
    public void export(View view){
        //generate data
        StringBuilder data = new StringBuilder();
        data.append("Time, correlation");
        for (int i=0;i<xlsxcorrelation.size();i++){

            data.append("\n"+String.valueOf(i)+","+String.valueOf(xlsxcorrelation.get(i)));
        }




        try{
            //saving the file into device
            FileOutputStream out = openFileOutput("data.csv", Context.MODE_PRIVATE);
            out.write((data.toString()).getBytes());
            out.close();

            //exporting
            Context context = getApplicationContext();
            File filelocation = new File(getFilesDir(), "data.csv");
            Uri path = FileProvider.getUriForFile(context, "com.example.myapplication.fileprovider", filelocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent, "Send mail"));
        }
        catch(Exception e){
            e.printStackTrace();
        }


    }
}







