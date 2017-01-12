package com.example.bobby.hackathon;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter myBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ListView myListView;
    private ArrayList<String> list = new ArrayList<>();
    private ArrayAdapter<String> BTArrayAdapter;
    private Handler handler = new Handler();
    private Timer timer = new Timer();
    private Button findBtn;
    private DataSource dataSource;

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Toast.makeText(getApplicationContext(), "BroadCastRegister started", Toast.LENGTH_SHORT).show();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                //list.add(device.getAddress());
                Beacon beacon = dataSource.findBeacon(device.getAddress());
                if (beacon.getBeschreibung() != "") {
                    new SendToDatabase().execute("1", "leer");
                    Toast.makeText(getApplicationContext(), beacon.getBeschreibung(), Toast.LENGTH_SHORT).show();
                }
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataSource = new DataSource(this);
        dataSource.open();
        dataSource.createBeacon("00:CD:FF:0E:6B:7E", "Kabine 1");
        Beacon beacon = dataSource.findBeacon("48:5A:B6:E1:76:58");



        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // Instanz eines DefaultAdapters (Zugriff auf das Bluetooth)
        myListView = (ListView) findViewById(R.id.listView);
        findBtn = (Button) findViewById(R.id.button3);
        // create the arrayAdapter that contains the BTDevices, and set it to the ListView
        BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        myListView.setAdapter(BTArrayAdapter);
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        findBtn.performClick();
                    }
                });
            }
        };
        timer.schedule(tt,0, 20000);


    }


    public void off(View v) {
        myBluetoothAdapter.disable();
        Toast.makeText(getApplicationContext(), "Turned off", Toast.LENGTH_LONG).show();
    }


    public void visible(View v) {
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }

    public void on(View v) {
        if (!myBluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }
    }

    public void find(View view) {
        //if (myBluetoothAdapter.isDiscovering()) {
        // the button is pressed when it discovers, so cancel the discovery
        //myBluetoothAdapter.cancelDiscovery();

        //unregisterReceiver(bReceiver);
        //}
        //else {

        Toast.makeText(getApplicationContext(), "Find Devices", Toast.LENGTH_LONG).show();

        BTArrayAdapter.clear();
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        myBluetoothAdapter.startDiscovery();
        registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        // }
    }

    public void list(View view) {
        BTArrayAdapter.clear();
        pairedDevices = myBluetoothAdapter.getBondedDevices();
        // put it's one to the adapter
        for (BluetoothDevice device : pairedDevices)
            BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();


    }

    public void exit() {
        unregisterReceiver(bReceiver);
        // finish();
        System.exit(1);
        dataSource.close();
    }
}
