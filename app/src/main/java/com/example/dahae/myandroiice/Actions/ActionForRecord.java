package com.example.dahae.myandroiice.Actions;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;

import com.example.dahae.myandroiice.R;

import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class ActionForRecord extends Activity {

    String mRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    Calendar calendar = Calendar.getInstance();
    String FileName =
            "/IceTest_"
                    + calendar.get(Calendar.YEAR) % 100 +"."+ (calendar.get(Calendar.MONTH)+1) +"."+calendar.get(Calendar.DAY_OF_MONTH) +"_"+ calendar.get(Calendar.HOUR_OF_DAY)
                    +"."+calendar.get(Calendar.MINUTE)
                    +".m4a";

    String RECORDED_FILE= mRootPath + "/" +
            "Voice Recorder" + FileName;

    MediaRecorder recorder;

    Timer timer = new Timer();
    int count;

    Button stopRecode;
    TextView time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.action_record_voice);

        stopRecode = (Button) findViewById(R.id.Stoprecode);
        time = (TextView) findViewById(R.id.textViewSyntaxTrigger);

        startRecording();
        count = 0;
        timer.scheduleAtFixedRate( new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() //run on ui thread
                {
                    public void run()
                    {
                        String numStr2 = String.valueOf( count++);
                        time.setText(numStr2);
                    }
                });
            }
        }, 1000, 1000 );

        stopRecode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickedStop();
            }
        });

    }

    protected void onPause() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        super.onPause();
    }

    public void onClickedStop(){
        if (recorder == null)
            return ;
        recorder.stop();
        recorder.release();
        recorder = null;

        Toast.makeText(getApplicationContext(), "save Recording", Toast.LENGTH_LONG).show();
        timer.cancel();
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    private void startRecording() {

            if (recorder != null) {
                recorder.stop();
                recorder.release();
                recorder = null;
            }
            recorder = new MediaRecorder();

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

            recorder.setOutputFile(RECORDED_FILE);

            Toast.makeText(getApplicationContext(), "start Recording", Toast.LENGTH_LONG).show();
        try {
            recorder.prepare();
            recorder.start();

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
