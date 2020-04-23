package com.nlpl931.controlla2;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import androidx.annotation.Nullable;

public class BluetoothService extends Service {
    final int handlerState = 0;
    Handler BTin;
    private BluetoothAdapter ba=null;

    private ConnectingThread mConnectingThread;
    private ConnectedThread mConnectedThread;

    private boolean stopThread;
    private static final UUID uid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private StringBuilder dataString = new StringBuilder();

    public BluetoothService() {
        Log.d("BT SERVICE","SERVICE CREATED");
        stopThread = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("BT SERVICE", "SERVICE STARTED");
        BTin = new Handler(){
            public void handleMessage(android.os.Message msg){
                Log.d("DEBUG","handle Message");
                if (msg.what == handlerState){
                    String readMessage = (String) msg.obj;
                    dataString.append(readMessage);

                    Log.d("RECORDED", dataString.toString());
                }
                dataString.delete(0, dataString.length());
            }
        };
        ba = BluetoothAdapter.getDefaultAdapter();
        chkBTstate();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        BTin.removeCallbacksAndMessages(null);
        stopThread = true;
        if (mConnectedThread != null)   mConnectedThread.closeStreams();
        if (mConnectingThread != null)  mConnectingThread.closeSocket();
        Log.d("SERVICE","onDestroy");
    }

    public void chkBTstate(){
        if (ba == null) stopSelf();
        else if (ba.isEnabled()){
            try {
                BluetoothDevice device = ba.getRemoteDevice(MainActivity.MAC);
                mConnectingThread = new ConnectingThread(device);
                mConnectingThread.start();
            }catch (IllegalArgumentException e){
                Log.d("SERVICE", "PROBLEM WITH MAC");
                stopSelf();
            }
        }else stopSelf();
    }

    private class ConnectingThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectingThread(BluetoothDevice device){
            Log.d("DBG BT", "IN CONNECTING THREAD");
            mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = mmDevice.createRfcommSocketToServiceRecord(uid);
            }catch (IOException e){
                stopSelf();
            }
            mmSocket = tmp;
        }

        @Override
        public void run(){
            super.run();
            ba.cancelDiscovery();
            try {
                mmSocket.connect();
                mConnectedThread = new ConnectedThread(mmSocket);
                mConnectedThread.start();
                mConnectedThread.write("x");
            }catch (IOException e){
                try{
                    mmSocket.close();
                    stopSelf();
                }catch (IOException e2){
                    stopSelf();
                }
            }catch (IllegalStateException e){
                stopSelf();
            }
        }

        public void closeSocket(){
            try{
                mmSocket.close();
            }catch (IOException e2){
                stopSelf();
            }
        }
    }
    private class ConnectedThread extends Thread{
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try{
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }catch (IOException e){
                stopSelf();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[256];
            int bytes;
            while(true && !stopThread){
                try{
                    bytes = mmInStream.read(buffer);
                    String readmsg = new String(buffer, 0, bytes);
                    BTin.obtainMessage(handlerState, bytes, -1, readmsg).sendToTarget();
                }catch (IOException e){
                    stopSelf();
                    break;
                }
            }
        }

        public void write(String input){
            byte[] msgBuffer = input.getBytes();
            try{
                mmOutStream.write(msgBuffer);
            }catch (IOException e){
                stopSelf();
            }
        }

        public void closeStreams(){
            try{
                mmInStream.close();
                mmOutStream.close();
            }catch (IOException e){
                stopSelf();
            }
        }
    }
}


