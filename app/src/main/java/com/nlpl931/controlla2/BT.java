package com.nlpl931.controlla2;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class BT extends AppCompatActivity {

    BluetoothDevice bd;
    BluetoothAdapter ba;
    Spinner sp;
    ArrayList<String> mac = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setFinishOnTouchOutside(false);
        setContentView(R.layout.activity_b_t);
        Set<BluetoothDevice> pd;
        ba = BluetoothAdapter.getDefaultAdapter();
        sp = findViewById(R.id.spinner);

        Button bn = findViewById(R.id.select);
        bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = sp.getSelectedItemPosition();
                String addr = mac.get(pos);
                Intent out = new Intent();
                out.putExtra("MAC", addr);
                setResult(RESULT_OK, out);
                finish();
                //Toast.makeText(getBaseContext(), addr, Toast.LENGTH_SHORT).show();
            }
        });
        if (ba.isEnabled()){
            pd = ba.getBondedDevices();
            ArrayList<String> list = new ArrayList<>();
            if(pd.size()>0){
                for(BluetoothDevice bd:pd)
                {
                    list.add(bd.getName());
                    mac.add(bd.getAddress());
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(), R.layout.support_simple_spinner_dropdown_item, list);
            sp.setAdapter(adapter);
        }
        else {
            Intent on = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(on, 1);
        }
    }

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
