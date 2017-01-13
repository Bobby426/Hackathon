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
import android.support.v4.util.ArraySet;
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
    private Handler handler2 = new Handler();
    private Timer timer2 = new Timer();
    private Button findBtn;
    private DataSource dataSource;
    Button btn_seife;
    Button btn_ledOff;
    Button btn_ledOn;
    Button btn_hc;
    Button btn_paper;
    private ArraySet<String> beaconList = new ArraySet<>();
    private ArraySet<String> blueList = new ArraySet<>();

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
                    System.out.println("bin drin");
                    beaconList.add(device.getAddress());
                    Toast.makeText(getApplicationContext(), beacon.getBeschreibung(), Toast.LENGTH_SHORT).show();
                }
                blueList.add(device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_seife = (Button) findViewById(R.id.btn_seife);
        btn_ledOn = (Button) findViewById(R.id.btn_ledOn);
        btn_ledOff = (Button) findViewById(R.id.btn_ledOff);
        final LED blau = new LED(232);
        final LED testLED = new LED(235);

        btn_hc = (Button) findViewById(R.id.btn_hc);
        btn_paper = (Button) findViewById(R.id.btn_paper);

        btn_hc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HC_SR04 hc_modul = new HC_SR04(blau);
                hc_modul.start();
            }
        });





        btn_ledOn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View viewA){

                try{
                    testLED.LEDon();
                }catch(Throwable t){
                    t.printStackTrace();
                }

            }
        });

        btn_ledOff.setOnClickListener(new View.OnClickListener(){
            public void onClick(View viewA){
                LED testLED = new LED(235);
                try{
                    testLED.LEDOff();
                }catch(Throwable t){
                    t.printStackTrace();
                }
            }
        });

        btn_paper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HC_SR04_Paper paper_measure = new HC_SR04_Paper(testLED);
                paper_measure.start();
            }
        });


        btn_seife.setOnClickListener(new View.OnClickListener(){
            public void onClick(View viewA){
                HC_SR04Seife hc_modul = new HC_SR04Seife();
                hc_modul.start();
            }
        });







        dataSource = new DataSource(this);
        dataSource.open();
        dataSource.createBeacon("00:CD:FF:0E:6B:7E", "Kabine 1");
        Beacon beacon = dataSource.findBeacon("48:5A:B6:E1:76:58");





        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!myBluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }// Instanz eines DefaultAdapters (Zugriff auf das Bluetooth)
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

        TimerTask tt2 = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        check();
                    }
                });
            }
        };
        timer.schedule(tt,0, 10000);
        timer.schedule(tt2,0, 10000);



    }

    public void check(){
        if(beaconList.size()==0){
            new SendToDatabase().execute("1", "voll");
        }
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

        if(blueList.size()>0) {
            if (beaconList.size() > 0) {
                if (blueList.contains((beaconList.valueAt(0)))) {

                } else {
                    beaconList.clear();
                }
                blueList.clear();
            }
        }
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        myBluetoothAdapter.startDiscovery();
        registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        // }
    }


    public void exit() {
        unregisterReceiver(bReceiver);
        // finish();
        System.exit(1);
        dataSource.close();
    }
}
