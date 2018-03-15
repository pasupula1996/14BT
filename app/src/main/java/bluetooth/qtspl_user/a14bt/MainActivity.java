package bluetooth.qtspl_user.a14bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

//import static bluetooth.qtspl_user.a14bt.MainActivity.MY_UUID;

public class MainActivity extends AppCompatActivity {

    Button PairedBT, ScanBt;
    ListView pairedListView;
    public BluetoothAdapter mBluetoothAdapter;
    ArrayAdapter<String> mAdapter;
    ArrayList<String> mArrayList = new ArrayList<String>();
    BluetoothDevice[] btArray = new BluetoothDevice[30];
    //ArrayList<BluetoothDevice> connectDevice = new ArrayList<BluetoothDevice>();
    UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PairedBT = findViewById(R.id.pairedBt);
        pairedListView = findViewById(R.id.list_view_paired);
        ScanBt = findViewById(R.id.scanBt);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        PairedBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();

                String[] names = new String[devices.size()];
                int index = 0;

                if (devices.size() > 0) {
                    for (BluetoothDevice device : devices) {
                        names[index] = device.getName();
                        index++;
                    }

                     //mAdapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,names);
                    // pairedListView.setAdapter(mAdapter);
                }
            }
        });

        ScanBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBluetoothAdapter.startDiscovery();
            }
        });


        mAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, mArrayList);
        pairedListView.setAdapter(mAdapter);

        pairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //pairDevice(btArray[i]);
                BluetoothDevice device = btArray[i];
               // BluetoothDevice device = (BluetoothDevice) pairedListView.getAdapter().getItem(i);
                ClientSocket clientSocket = new ClientSocket(device);
                Toast.makeText(getApplicationContext(), "at" + btArray[i], Toast.LENGTH_SHORT).show();
                //mBluetoothAdapter.cancelDiscovery();
                clientSocket.start();
                //sendBT();
            }
        });
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String acton = intent.getAction();
            toast("onReceive method");
            int i = 0;

            if (BluetoothDevice.ACTION_FOUND.equals(acton)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btArray[i] = device;
                i++;
                //connectDevice.add(device);
                mArrayList.add(device.getName());
                toast("found");
                mAdapter.notifyDataSetChanged();

            }


        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        super.onPause();
    }

    public void toast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void sendBT() {
        OutputStream out = null;

        String sample = "Welcome to qualtech";

        // out.write(sample.getBytes());

    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void unPairDevice(BluetoothDevice device) {
        Method method = null;
        try {
            method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object) null);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }


    private class ClientSocket extends Thread {

        private BluetoothDevice mDevice;
        private BluetoothSocket mSocket;
        private boolean mSecure;
       // private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        public ClientSocket(BluetoothDevice device) {
            mDevice = device;
            BluetoothSocket tmp = null;
            mBluetoothAdapter.cancelDiscovery();
            try {
                Log.d("BT", "BT creating RfcommSocketService");
                tmp = mDevice.createRfcommSocketToServiceRecord(MY_UUID);

            } catch (IOException e) {
                e.printStackTrace();
            }

            mSocket = tmp;
        }

        public void run() {
            //mBluetoothAdapter.cancelDiscovery();
            try {
                Log.d("BT", "BT Connecting");
                mSocket.connect();
                Log.d("BT", "Connected succesufully");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}