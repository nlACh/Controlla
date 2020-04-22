package com.nlpl931.controlla2;

import androidx.appcompat.app.AppCompatActivity;
import io.github.controlwear.virtual.joystick.android.JoystickView;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int loopInterval = 25; // In milliseconds. Will run 40 times a second.
    private boolean useBodySensor = false, canTransmit = false;
    int[][] data = new int[2][2]; // This data will be sent over to whatever device needed
    private TextView tv, tx;
    private Switch arm, joy;
    private JoystickView head, motor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        Button link = findViewById(R.id.link);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getBaseContext(), BT.class);
                startActivity(in);
            }
        });
        tv = findViewById(R.id.tv);
        tv.setText("HI THERE!!");
        tx = findViewById(R.id.tv2);

        arm = findViewById(R.id.arm);
        joy = findViewById(R.id.joy);
        head = findViewById(R.id.head);
        motor = findViewById(R.id.motor);

        arm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tv.setText("ARMED");
                canTransmit = true;
            }
        });

        joy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (joy.isChecked())
                {
                    joy.setText("BODY SENSORS ");
                    useBodySensor = true;
                }
                else
                {
                    joy.setText("JOYSTICKS ");
                    useBodySensor = false;
                }
            }
        });

        motor.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                tv.setText(String.valueOf(angle)+"\t"+String.valueOf(strength));
                data[0][0] = strength;
                data[0][1] = angle;
            }
        }, loopInterval);

        head.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                tx.setText(String.valueOf(angle)+"\t"+String.valueOf(strength));
                data[1][0] = strength;
                data[1][1] = angle;
            }
        }, loopInterval);
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    //  Immersive mode
    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

}
