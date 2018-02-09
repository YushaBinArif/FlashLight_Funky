package com.example.yousha.flashlightapp;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private final int REQ_CODE_SPEECH_INPUT = 100;
    private ImageView switchFlash, SosSignal, Wifi, batteryStat;
    private FloatingActionButton floatingActionButton;
    private CameraManager mCameraManager, sosCameraManager;
    private String mCameraId, sosCameraId;
    private ImageButton mTorchOnOffButton;
    private Boolean isTorchOn;
    private MediaPlayer mp;
    WifiManager wifiManager;
    private TextView battery_Status, battery_percent, light;
    private int SELECT_CAMERA = 0;
    SensorManager sensorManager;
    Sensor light_sensor;
    static boolean AUTO_FLASH = false;
    ActionBar actionBar;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("FlashLightActivity", "onCreate()");
        setContentView(R.layout.activity_main);
        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2c3e50")));
        //getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2c3e50")));
        mTorchOnOffButton = (ImageButton) findViewById(R.id.imgbtn);
        isTorchOn = false;
        switchFlash = (ImageView) findViewById(R.id.flash);
        SosSignal = (ImageView) findViewById(R.id.sos);
        batteryStat = (ImageView) findViewById(R.id.battery);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        light_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        //light = (TextView) findViewById(R.id.light);
        sensorManager.registerListener(this, light_sensor, SensorManager.SENSOR_DELAY_NORMAL);
        battery_percent = (TextView) findViewById(R.id.batterypercent);
        Wifi = (ImageView) findViewById(R.id.wifi);
        getBatteryStatus();
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        registerReceiver(broadcastReceiver,
//                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//        wifiManager.startScan();
//

        Boolean isFlashAvailable = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!isFlashAvailable) {

            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();
            alert.setTitle("Error !!");
            alert.setMessage("Your device doesn't support flash light!");
            alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // closing the application
                            finish();
                            System.exit(0);
                        }
                    });
            alert.show();
            return;
        }

//        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            mCameraId = mCameraManager.getCameraIdList()[1];
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }


        mTorchOnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isTorchOn) {
                        turnOffFlashLight();
                        isTorchOn = false;
                        toggleButtonImage();
                    } else {
                        turnOnFlashLight();
                        isTorchOn = true;
                        toggleButtonImage();
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void turnOnFlashLight() {

        switchLed(SELECT_CAMERA);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, true);
                playOnOffSound();
                //mTorchOnOffButton.setImageResource(R.drawable.lighton);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void turnOffFlashLight() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, false);
                playOnOffSound();
                //mTorchOnOffButton.setImageResource(R.drawable.lightoff);


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playOnOffSound() {

        //mp = MediaPlayer.create(MainActivity.this, R.raw.flash_sound);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.release();
            }
        });
        mp.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isTorchOn) {
            turnOffFlashLight();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTorchOn) {
            turnOffFlashLight();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();
        if (isTorchOn) {
            turnOnFlashLight();
        }

    }

    private void toggleButtonImage() {
        if (isTorchOn) {
            mTorchOnOffButton.setImageResource(R.drawable.switchonsmall);
        } else {
            mTorchOnOffButton.setImageResource(R.drawable.switchoffsmall);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void switchLed(int SelectCamera) {
        this.SELECT_CAMERA = SelectCamera;
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[SELECT_CAMERA];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.flash:
                if (!isTorchOn) {
                    if (SELECT_CAMERA == 0) {
                        switchLed(1);
                        Toast.makeText(MainActivity.this, "Front Flash Enabled", Toast.LENGTH_SHORT).show();

                    } else {
                        switchLed(0);
                        Toast.makeText(MainActivity.this, "Rear Flash Enabled", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(MainActivity.this, "Turn Off Flashlight Before Toggling", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.sos:
                sosLight();
                break;
            case R.id.wifi:


                if (wifiManager.isWifiEnabled()) {
                    turnOffWifi();
                } else {
                    turnOnWifi();

                }


            case R.id.battery:
                getBatteryStatus();
                break;

            case R.id.floatingActionButton:
                promptSpeechInput();
        }

    }

    private void turnOnWifi() {
        wifiManager.setWifiEnabled(true);
        Wifi.setImageResource(R.drawable.wifi);
        Toast.makeText(this, "Wifi enabled", Toast.LENGTH_SHORT).show();
    }

    private void turnOffWifi() {
        wifiManager.setWifiEnabled(false);
        Wifi.setImageResource(R.drawable.wifioff);
        Toast.makeText(this, "Wifi disabled", Toast.LENGTH_SHORT).show();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void sosLight() {
        int delay = 500;

        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {

                if (j == 1) delay = 1000;
                if (j == 2) delay = 500;
                turnOnFlashLight();
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                turnOffFlashLight();
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }

        }

    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void getBatteryStatus() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(broadcastReceiver, intentFilter);

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBatterData(intent);
//            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
//                Toast.makeText(context, "true", Toast.LENGTH_SHORT).show();
//                Toast.makeText(context,wifiManager.getScanResults().size()+ "true", Toast.LENGTH_SHORT).show();
//
//                mScanResults = wifiManager.getScanResults();
//
//                WIFI_NAMES = new ArrayList<>();
//                for (ScanResult scanResult : mScanResults)
//                {
//                    WIFI_NAMES.add(scanResult+" "+scanResult.BSSID);
//                }
//                // add your logic here
//            }

        }
    };



        private void updateBatterData(Intent intent) {

            boolean present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);
            if (present) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                if (level != -1 & scale != -1) {
                    int batteryPercent = (int) ((level / (float) scale) * 100f);
                    battery_percent.setText("Battey: "+batteryPercent + "%");

                }
                int charging_status = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                boolean isUSBCHARGE = charging_status == (BatteryManager.BATTERY_PLUGGED_USB) || charging_status == BatteryManager.BATTERY_PLUGGED_AC;
                if (!isUSBCHARGE) {
                    batteryStat.setImageResource(R.drawable.battery);
                } else {
                    batteryStat.setImageResource(R.drawable.batterycharge);
                }

            }

        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            switch (requestCode) {
                case REQ_CODE_SPEECH_INPUT: {
                    if (resultCode == RESULT_OK && null != data) {

                        ArrayList<String> result = data
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        switch (result.get(0)) {
                            case "turn on flashlight":
                                if (isTorchOn) {
                                }
                                turnOnFlashLight();
                                isTorchOn = true;
                                toggleButtonImage();
                                break;
                            case "turn off flashlight":
                                if (!isTorchOn)
                                    turnOffFlashLight();
                                isTorchOn = false;
                                toggleButtonImage();
                                break;
                            case "SOS":
                                sosLight();
                                break;
                            case "open Wi-Fi":
                                turnOnWifi();
                                break;
                            case "close Wi-Fi":
                                turnOffWifi();
                                break;

                            case "show Wi-Fi":
                                startActivity(new Intent(MainActivity.this, Main2Activity.class));
                                break;

                            case "battery":
                                break;
                        }
                    }
                    break;
                }

            }

        }


        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onSensorChanged(SensorEvent event) {

            if (AUTO_FLASH) {
                if (event.values[0] < 1) {
                    turnOnFlashLight();
                    isTorchOn = true;
                    toggleButtonImage();
                }
//                else {
//                    turnOffFlashLight();
//                    isTorchOn = false;
//                    toggleButtonImage();
//                }


            }
            else
            {}


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.custom_actionbar,menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.settings :
                startActivity(new Intent(MainActivity.this,Main2Activity.class));

        }
        return true;
    }
}



