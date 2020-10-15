package com.nlpl931.controlla2;

public interface Constants {

    // Message types sent from the BluetoothChatService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";

}

interface State{
    int STATE_NONE = 0;
    int STATE_LISTEN = 1;
    int STATE_CONNECTING = 2;
    int STATE_CONNECTED = 3;
}
