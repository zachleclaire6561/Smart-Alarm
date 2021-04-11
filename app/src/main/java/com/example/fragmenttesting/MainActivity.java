package com.example.fragmenttesting;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Toast;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.fragment.NavHostFragment;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Luminosity stuff
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorEventListener lightEventListener;
    private float maxValue;
    private double luminosity = 0;
    //
    TextToSpeech textToSpeech;

        // Alarm Fragment UI
    EditText hoursEditText;
    EditText minutesEditText;
    TextView timerText;
    TextView textTimeLeft;
    TextView recitedText;
    Button snoozeButton;

    Handler handler = new Handler();
    // Handles Alarm sound Scheduling
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.siren);
            mediaPlayer.start();
            Log.d("Handlers", "Called on main thread");

            handler.postDelayed(this, 3000);
        }
    };

    private Runnable runnableTTS = new Runnable(){
        @Override
        public void run() {
            textToSpeech.speak( "Turn The Lights On", TextToSpeech.QUEUE_FLUSH, null );
            Log.d("Handlers", "Called on main thread");

            handler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        textToSpeech = new TextToSpeech(this,  new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                //YEET
            }
        });

        textToSpeech.setLanguage(Locale.US);

        // Main Page UI Init Stuff
        setContentView(R.layout.activity_main);
        hoursEditText = (EditText) findViewById(R.id.hoursEditTextNumber);
        minutesEditText = (EditText) findViewById(R.id.minutesEditTextNumber);
        recitedText = (TextView) findViewById(R.id.recitedText);
        timerText = (TextView) findViewById(R.id.timerTextView);
        textTimeLeft = (TextView) findViewById(R.id.timeLeftTextView);
        snoozeButton = (Button) findViewById(R.id.snoozeButton);
        textTimeLeft.setVisibility(View.INVISIBLE);
        snoozeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnableCode);

                String str = "Turn The Lights On".toLowerCase();
                String user = recitedText.getText().toString().toLowerCase();
                String sen = str.length() >= user.length() ? str : user;;
                String s = str.length() >= user.length() ? user : str;

                int inc = Math.abs(str.length() - user.length());
                System.out.print(inc);
                for (int i = 0; i < s.length(); i++)
                {
                    if(sen.charAt(i) != s.charAt(i))
                        inc++;
                }
                boolean check = inc == 0 ? true : (inc / (double) str.length()) <= 0.25;

                // Make Textbox visible
                if (!check && luminosity > 75.0) {
                    handler.post(runnableTTS);
                }else{
                    handler.removeCallbacks(runnableTTS);
                }
            }
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor == null) {
            Toast.makeText(this, "The device does not have a light sensor", Toast.LENGTH_SHORT).show();
            finish();
        }
        maxValue = lightSensor.getMaximumRange();
        lightEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float value = sensorEvent.values[0];
                getSupportActionBar().setTitle("Luminosity :" + value + "lx");
                int newValue = (int) (255f * value / maxValue);
                luminosity+=newValue;
                //root.setBackgroundColor(Color.rgb(newValue, newValue, newValue));
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

    }

    public void pressed(View view) {
        textTimeLeft.setVisibility(View.VISIBLE);
        int hours = Integer.valueOf(hoursEditText.getText().toString());

        int minutes = Integer.valueOf(minutesEditText.getText().toString());

        //int minDif, hoursDif;
        Date currentTime = Calendar.getInstance().getTime();
        String t = new SimpleDateFormat("HH:mm:ss").format(currentTime);
        String time[] = t.split(":");
        System.out.println("value: " + time[0] + " " + time[1]);

        int current = (Integer.valueOf(time[1]) + 60*Integer.valueOf(time[0]));
        int alarmTime = (hours*60+minutes);
        int timeDifference = alarmTime - current;
        if (timeDifference < 0) {
            timeDifference = 24 * 60 + timeDifference;
        }
        int hoursDif = timeDifference / 60;
        int minDif = timeDifference % 60;

        int timeLeft = (hoursDif * 3600000) + (minDif * 60000);

        System.out.println("value: " + timeLeft);

        new CountDownTimer(timeLeft, 60000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Date currentTime = Calendar.getInstance().getTime();
                String t = new SimpleDateFormat("HH:mm:ss").format(currentTime);
                String time[] = t.split(":");
                int current = (Integer.valueOf(time[1]) + 60*Integer.valueOf(time[0]));
                int alarmTime = (hours*60+minutes);
                int timeDifference = alarmTime - current;
                if (timeDifference < 0) {
                    timeDifference = 24 * 60 + timeDifference;
                }
                int hoursDif = timeDifference / 60;
                int minDif = timeDifference % 60;

                int timeLeft = (hoursDif * 3600000) + (minDif * 60000);
                if (minDif <= 9)
                    timerText.setText(Integer.toString(hoursDif) + " hours  : 0" + Integer.toString(minDif) + " minutes");
                else
                    timerText.setText(Integer.toString(hoursDif) + " hours: " + Integer.toString(minDif) + " minutes");
            }

            @Override
            public void onFinish() {
                snoozeButton.setVisibility(View.VISIBLE);
                handler.post(runnableCode);
            }
        }.start();
    }
}