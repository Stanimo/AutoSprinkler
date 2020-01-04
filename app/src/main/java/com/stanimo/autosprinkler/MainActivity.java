package com.stanimo.autosprinkler;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    int REQUEST_ENABLE_BT = 1;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (bluetoothAdapter == null) {
            // Display a toast informing the user that he doesn't have bluetooth
            Toast.makeText(this, "Your device doesn't support Bluetooth communication." +
                    " Exiting", Toast.LENGTH_LONG).show();
            finish();
        } else {
            bluetoothConnection();
        }
    }

    public void bluetoothConnection() {
        // Check if bluetooth isn't enabled, if so pop up a BT enabling dialog.
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Check if there are desired paired devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }

        // Register for broadcast when the device is discovered
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, intentFilter);

        Toast.makeText(this, "finished onCreate", Toast.LENGTH_SHORT).show();


    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice object and its'
                // info from the Intent.
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = mDevice.getName();
                String deviceHardwareAdress = mDevice.getAddress();
            }
        }
    };

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread (BluetoothDevice device) {
            // Use a temporary object to that is later assigned to mmSocket because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with given BluetoothDevice.
                // MY_UUID is the apps UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch(IOException e) {
                Log.e("ErrorLog", "Sockets' create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discover because it otherwise slows down the connection
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect the remote device through the socket. This call blocks until it succeeds
                // or throws an exception
                mmSocket.connect();
                } catch (IOException connectException) {
                // Unable to connect; close the socket and return
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e("ErrorLog", "Could not close the socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with connection in a separate
            // thread
            // manageMyConnectedSocket(mmSocket); //TODO
        }

        // Closes the socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("ErrorLog", "Could not close the client socket", e);
            }
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // ACTION_FOUND receiver should be unregistered
        unregisterReceiver(receiver);
    }
}
