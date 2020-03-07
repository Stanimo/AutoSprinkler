package com.stanimo.autosprinkler;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    ImageView mBlueStatus;
    int REQUEST_ENABLE_BT = 1;
    private static final UUID my_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    OutputStream mmOutputStream;
    BluetoothSocket mmSocket = null;
    Button btnConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bluetooth connection button
        btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initiateBT();
            }
        });
//        mmOutputStream = mmSocket.getOutputStream();

        mBlueStatus = findViewById(R.id.statusBluetooth);

        Toast.makeText(this, "Back to onCreate", Toast.LENGTH_LONG).show();

        Button btnOn = findViewById(R.id.btnOn);
        Button btnOff = findViewById(R.id.btnOff);

        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnLedOn();
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnLedOff();
            }
        });
    }

    public void initiateBT() {
        if (bluetoothAdapter == null) {
            // Display a toast informing the user that he doesn't have bluetooth
            Toast.makeText(this, "Your device doesn't support Bluetooth communication." +
                    " Exiting", Toast.LENGTH_LONG).show();
            // Exit the app
            finish();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Check if there are any paired devices:
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if (deviceHardwareAddress.equals("00:14:03:05:59:A2")) {
                    ConnectThread connectedDevice = new ConnectThread(device);
                    //MyBluetoothService btService = new MyBluetoothService(device); //TODO check out if this is the right direction.
                    connectedDevice.start(); // TODO this has been changed from run() to start() as per new information that run() make this run in the same thread
                    break;
                }
            }

        }
    }

    // Connect as a client-----------------------------------------------------------------------
   public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(my_UUID);
            } catch (IOException e) {
                Log.e("ErrorMsg", "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Toast.makeText(getApplicationContext(), "Connection through Socket established", Toast.LENGTH_LONG).show();

            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e("ErrorMsg", "Could not close the client socket", closeException);
                }
            }
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("ErrorMsg", "Could not close the client socket", e);
            }
        }

        // Manage a connection - in a different class
    }

    // Sending data
    public void turnLedOn() {
        if (mmSocket != null) {
            try {
                mmSocket.getOutputStream().write(1);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Error Turning Led On", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void turnLedOff() {
        if (mmSocket != null) {
            try {
                mmSocket.getOutputStream().write(0);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Error Turning Led Off", Toast.LENGTH_LONG).show();
            }
        }
    }

}
