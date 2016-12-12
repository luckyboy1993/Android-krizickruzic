package games.luckygames;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button bBluetooth;
    private Button sMusic;
    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sMusic = (Button) findViewById(R.id.sMusic);
        mp = mp.create(MainActivity.this, R.raw.pjesma);
        mp.setLooping(true);

        if(mp.isPlaying()){
            sMusic.isActivated();
        }

        sMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mp.isPlaying()) {
                    mp.pause();
                }else {
                    mp.start();
                }
            }
        });

        bBluetooth=(Button) findViewById(R.id.bBluetooth);

        bBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getBaseContext(), BluetoothActivity.class);
                startActivity(intent);
            }
        });

    }
}
