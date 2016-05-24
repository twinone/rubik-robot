package org.twinone.rubiksolver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    public static final int SIZE = 3;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new CameraFragment()).commit();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void connectBluetooth() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "you can't even bluetooth", Toast.LENGTH_LONG).show();
            return;
        }
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice("98:D3:31:30:3B:D8");
        try {
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            mBluetoothAdapter.cancelDiscovery();
            socket.connect();
            mSocket = socket;
        } catch (IOException e) {
            Toast.makeText(this, "couldn't connect, sorry.", Toast.LENGTH_LONG).show();
        }
    }

}
