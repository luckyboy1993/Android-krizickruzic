package games.luckygames;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class DevicesActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        final ArrayList<String> bDevicesString;
        final ArrayList<BluetoothDevice> bDevices;
        //final ArrayAdapter<String> mArrayAdapter;
        final ListView bListDevices;


        bDevicesString = getIntent().getStringArrayListExtra("deviceName.list");
        bDevices = getIntent().getExtras().getParcelableArrayList("device.list");

        final ArrayAdapter<String> mArrayAdapter;
        mArrayAdapter = new ArrayAdapter<>(this, R.layout.simple_list_item, bDevicesString);
        bListDevices = (ListView) findViewById(R.id.bDevices);
        bListDevices.setAdapter(mArrayAdapter);

        bListDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (SocketHandler.getSocket() != null) {
                    try {
                        SocketHandler.getSocket().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                BluetoothDevice bDevice = bDevices.get(position);
                ConnectThread connectThread = new ConnectThread(bDevice);
                connectThread.start();
            }
        });


    }

    //-----------------------------------------------------------------------------------------------------------------

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("a7060765-803d-4689-91a0-9cf0b575cd32"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException en) {
                    en.printStackTrace();
                }

                return;
            }

            Intent intent=new Intent(getApplicationContext(), GameActivity.class);
            SocketHandler.setSocket(mmSocket);
            intent.putExtra("sign", "X");
            startActivity(intent);

        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}
