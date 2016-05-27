package org.twinone.rubiksolver;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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


        new Handler().post(new Runnable() {
            @Override
            public void run() {
                connectBluetooth();
            }
        });
    }


    public void connectBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Enable Bluetooth and pair", Toast.LENGTH_LONG).show();
            return;
        }
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice("98:D3:31:30:3B:D8");
        for (BluetoothDevice x : mBluetoothAdapter.getBondedDevices()) {
            Log.d("Main", "Address: " + x.getAddress());
            for (ParcelUuid uuid : x.getUuids()) {
                Log.d("Main", "parceluuid:" + uuid.toString());
            }
        }
        try {
            mBluetoothAdapter.cancelDiscovery();

            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
            socket.connect();

            mSocket = socket;

            // TODO add RobotScheduler here
            //mRobotScheduler = ...
        } catch (IOException e) {
            Log.d("Main", "Exception connecting to bluetooth: ", e);
        }
    }

    // TODO
//    public RobotScheduler getRobotScheduler() {
//        return mRobotScheduler;
//    }


}
