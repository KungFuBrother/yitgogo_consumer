package com.smartown.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver broadcastReceiver;
    private BluetoothAdapter bluetoothAdapter;
    private HashMap<String, BluetoothDevice> bluetoothDeviceHashMap;
    private TextView textView;
    private BluetoothDevice bondedBluetoothDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.devices);
        init();
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.startDiscovery();
            } else {
                //如果蓝牙设备不可用的话,创建一个intent对象,该对象用于启动一个Activity,提示用户启动蓝牙适配器
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(intent);
            }
        }
    }

    private void init() {
        initReceiver();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothDeviceHashMap = new HashMap<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private void initReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    bluetoothDeviceHashMap.put(bluetoothDevice.getAddress(), bluetoothDevice);
                    StringBuilder builder = new StringBuilder();
                    Iterator<BluetoothDevice> iterator = bluetoothDeviceHashMap.values().iterator();
                    while (iterator.hasNext()) {
                        BluetoothDevice device = iterator.next();
                        builder.append(device.getName() + "\n" + device.getAddress() + "\n" + device.getBondState() + "\n\n");
                        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                            bondedBluetoothDevice = device;
                            connect();
                            unregisterReceiver(broadcastReceiver);
                        }
                    }
                    textView.setText(builder.toString());
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void connect() {
        try {
            BluetoothSocket bluetoothSocket = bondedBluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            bluetoothSocket.connect();
            OutputStream mOutputStream = bluetoothSocket.getOutputStream();
            InputStream mInputStream = bluetoothSocket.getInputStream();
            mOutputStream.write("StartOnNet".getBytes());
            mOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
