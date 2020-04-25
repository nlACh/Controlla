package com.nlpl931.controlla2;

import androidx.appcompat.app.AppCompatActivity;
import io.github.controlwear.virtual.joystick.android.JoystickView;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.Toast;


import static com.nlpl931.controlla2.State.*;
import static com.nlpl931.controlla2.Constants.*;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter ba;
    private String mConnectedDeviceName = null;
    private BluetoothService mBTService = null;

    private final String TAG = "MainActivity";
    private static final int loopInterval = 25; // In milliseconds. Will run 40 times a second.
    private boolean useBodySensor = false, canTransmit = false;
    int[][] data = new int[2][2]; // This data will be sent over to whatever device needed
    public static String str1, str2;
    private ArrayAdapter<String> text_rx, text_tx;
    private Switch arm, joy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ba = BluetoothAdapter.getDefaultAdapter();
        setup();

        Button link = findViewById(R.id.link);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(v.getContext(), BT.class);
                startActivityForResult(in, 2);
            }
        });

        ListView rx = findViewById(R.id.tv);
        ListView tx = findViewById(R.id.tv2);
        rx.setAdapter(text_rx);
        tx.setAdapter(text_tx);

        arm = findViewById(R.id.arm);
        joy = findViewById(R.id.joy);
        JoystickView head = findViewById(R.id.head);
        JoystickView motor = findViewById(R.id.motor);

        final Button finish = findViewById(R.id.finish);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Goodbye");
                finish();
            }
        });

        final Button menu = findViewById(R.id.menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pop = new PopupMenu(v.getContext(), v);
                pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.Change:
                                return true;

                            case R.id.settings:
                                Toast.makeText(getBaseContext(), "Settings", Toast.LENGTH_SHORT).show();
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                pop.inflate(R.menu.popup_menu_main);
                pop.show();
            }
        });

        final Button info = findViewById(R.id.info);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent info = new Intent(v.getContext(), Info.class);
                startActivity(info);
            }
        });

        arm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //rx.setText(R.string.label_armed);
                //rx.setText(R.string.labelUnarmed);
                canTransmit = arm.isChecked();
            }
        });

        joy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (joy.isChecked())
                {
                    joy.setText(R.string.label_Body_sensors);
                    useBodySensor = true;
                }
                else
                {
                    joy.setText(R.string.label_joy);
                    useBodySensor = false;
                }
            }
        });

        motor.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                str1 = angle +"\t"+ strength +"\t";
                data[0][0] = strength;
                data[0][1] = angle;
                //rx.setText(str1);
            }
        }, loopInterval);

        head.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                str2 = angle +"\t"+ strength;
                data[1][0] = strength;
                data[1][1] = angle;
                //tx.setText(str2);
                sender(str2);
            }
        }, loopInterval);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (mBTService != null){
            if (mBTService.getState() == STATE_NONE)
                mBTService.start();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (mBTService != null)
            mBTService.stop();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                if (resultCode == RESULT_OK)    Log.d(TAG, "Switching BT on.");
                break;

            case 2:
                if(resultCode == RESULT_OK)     connectDevice(data);
                break;
        }
    }

    private void connectDevice(Intent data){
        Bundle extras = data.getExtras();
        if (extras == null) return;
        String addr = extras.getString("MAC");
        assert addr != null;
        Log.d("Connected to: ", addr);
        BluetoothDevice device = ba.getRemoteDevice(addr);
        mBTService.connect(device);
    }

    private void setup(){
        Log.d(TAG, "Setup");
        if (!ba.isEnabled()){
            Intent on = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(on, 1);
        }
        mBTService = new BluetoothService(getBaseContext(), handler);
        text_rx = new ArrayAdapter<>(this, R.layout.messages);
        text_tx = new ArrayAdapter<>(this, R.layout.messages);
        for (int i = 0; i<2; i++){
            for (int j = 0; j<2; j++)
                data[i][j] = 0;
        }

        // Set up a timer to send data at fixed intervals.
    }

    private void sender(String msg){
        if (mBTService.getState() != STATE_CONNECTED){
            //Toast.makeText(getBaseContext(), "Not connected", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Not connected");
            return;
        }

        if (msg.length()>0){
            byte[] send = msg.getBytes();
            mBTService.write(send);
            Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
            //et.setText("");
        }
    }

    // New handler!
    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1){
                        case STATE_CONNECTED:
                            Log.d(TAG, "Connected");
                            text_rx.clear();
                            text_tx.clear();
                            //tx.setText("");
                            //rx.setText("");
                            break;

                        case STATE_CONNECTING:
                            Log.d(TAG, "Connecting");
                            //tx.setText(R.string.label_connecting);
                            break;

                        case STATE_LISTEN:
                        case STATE_NONE:
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    text_tx.add("Me: " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    text_rx.add(mConnectedDeviceName + ": " + readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getBaseContext(), "Connected to:" +mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getBaseContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    };

}
