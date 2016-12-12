package games.luckygames;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;

public class BroadcastService extends Service {

    private final static String TAG = "BroadcastService";
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    public static final String COUNTDOWN_BR = "your_package_name.countdown_br";
    Intent bi = new Intent(COUNTDOWN_BR);

    CountDownTimer cdt = null;

    @Override
    public void onCreate() {
        super.onCreate();

        cdt = new CountDownTimer(300000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (mBluetoothAdapter==null || !mBluetoothAdapter.isEnabled() ) {
                    cdt.cancel();
                }else {
                    bi.putExtra("countdown", millisUntilFinished);
                    sendBroadcast(bi);
                }

            }

            @Override
            public void onFinish() {

            }
        };

        cdt.start();
    }

    @Override
    public void onDestroy() {

        cdt.cancel();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}