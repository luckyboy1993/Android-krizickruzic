package games.luckygames;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class BluetoothActivity extends AppCompatActivity {

    private Button bPaired;
    private Button bScan;
    private Button bVisibility;
    private Button bOnOff;

    private TextView vVisibility;

    private ProgressDialog mProgressDig;

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private ArrayList<BluetoothDevice> bDevices = new ArrayList<>();
    private ArrayList<String> bDevicesString= new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        bPaired=(Button) findViewById(R.id.bPaired);
        bScan=(Button) findViewById(R.id.bScan);
        bVisibility=(Button) findViewById(R.id.bVisibility);
        vVisibility=(TextView) findViewById(R.id.vVisibility);
        bOnOff=(Button) findViewById(R.id.bOnOff);



        mProgressDig = new ProgressDialog(this);
        mProgressDig.setMessage("Scaninng");
        mProgressDig.setCancelable(false);
        mProgressDig.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener(){
            @Override
        public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();

                mBluetoothAdapter.cancelDiscovery();
            }
        });

        if (mBluetoothAdapter==null || !mBluetoothAdapter.isEnabled() ) {
            BluetoothOff();
        }else {
            mBluetoothAdapter.enable();
            BluetoothOn();
        }



        bPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bDevices.clear();
                bDevicesString.clear();

                final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                if (pairedDevices == null || pairedDevices.size() == 0) {
                    showToast("No paired devices found");
                } else {

                    for (BluetoothDevice device : pairedDevices) {
                        bDevices.add(device);
                        bDevicesString.add(device.getName());
                    }

                    Intent intent = new Intent(getBaseContext(), DevicesActivity.class);
                    intent.putParcelableArrayListExtra("device.list", bDevices);
                    intent.putExtra("deviceName.list", bDevicesString);
                    startActivity(intent);
                }
            }
        });

        bScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bDevices.clear();
                bDevicesString.clear();

                showToast("scan");
                mBluetoothAdapter.startDiscovery();

            }
        });

        bVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!vVisibility.getText().equals("")){
                    stopService(new Intent(getApplicationContext(), BroadcastService.class));
                    vVisibility.setText("");
                }else {
                    Intent discIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(discIntent);

                    do{}while(BluetoothAdapter.getDefaultAdapter().getScanMode()!= BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);

                    startService(new Intent(BluetoothActivity.this, BroadcastService.class));
                }



            }
        });

        bOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mBluetoothAdapter==null || !mBluetoothAdapter.isEnabled() ) {

                    bOnOff.setEnabled(false);
                    mBluetoothAdapter.enable();

                    do{}while(!mBluetoothAdapter.isEnabled());

                    BluetoothOn();

                } else {

                    mBluetoothAdapter.disable();

                    BluetoothOff();
                }


            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

    }


    //-------------------------------------------------------------------------------------------------------------

    private void BluetoothOn(){

        bOnOff.setEnabled(true);
        bVisibility.setEnabled(true);
        bScan.setEnabled(true);
        bPaired.setEnabled(true);
        bOnOff.setText("Bluetooth On");

        AcceptThread acceptThread=new AcceptThread();
        acceptThread.start();
    }

    private void BluetoothOff(){

        bOnOff.setText("Bluetooth Off");
        bVisibility.setEnabled(false);
        bScan.setEnabled(false);
        bPaired.setEnabled(false);
    }

    //-------------------------------------------------------------------------------------------------------------

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mProgressDig.show();
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                mProgressDig.dismiss();
                if(bDevices == null || bDevices.size() == 0){
                    showToast("No devices nearby");
                } else {
                    Intent newIntent = new Intent(getBaseContext(), DevicesActivity.class);
                    newIntent.putParcelableArrayListExtra("device.list", bDevices);
                    newIntent.putExtra("deviceName.list", bDevicesString);
                    startActivity(newIntent);
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bDevices.add(device);
                bDevicesString.add(device.getName());

                showToast("Found " + device.getName());
            }
        }
    };

    //--------------------------------------------------------------------------------------------------------------------

    private class AcceptThread extends Thread {
        BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("iksoks", UUID.fromString("a7060765-803d-4689-91a0-9cf0b575cd32"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmServerSocket = tmp;

        }

        public void run() {

            BluetoothSocket socket = null;

            while (mBluetoothAdapter.isEnabled()) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }


                if (socket != null) {
                    final BluetoothSocket socketBluetooth = socket;


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {


                            AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothActivity.this);
                            builder.setMessage("Do you want to connect?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                                            SocketHandler.setSocket(socketBluetooth);
                                            intent.putExtra("sign", "O");
                                            startActivity(intent);
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            return;
                                        }
                                    })
                                    .show();
                        }
                    });

                }
            }
        }
    }

    //---------------------------------------------------------------------------

    public void showToast(String message){
        Toast.makeText(BluetoothActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    //------------------------------------------------------------------------------


    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateGUI(intent); // or whatever method used to update your GUI fields
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(br, new IntentFilter(BroadcastService.COUNTDOWN_BR));
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);


    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(br);

    }

    @Override
    public void onStop() {
        try {
            unregisterReceiver(br);
        } catch (Exception e) {
            // Receiver was probably already stopped in onPause()
        }
        super.onStop();
    }

    @Override
    public void onDestroy(){
        stopService(new Intent(this, BroadcastService.class));
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void updateGUI(Intent intent) {
        if (intent.getExtras() != null) {
            long millisUntilFinished = intent.getLongExtra("countdown", 0);
            vVisibility.setText(String.valueOf(millisUntilFinished/1000));
        } else {
            vVisibility.setText("");
        }
    }
}
