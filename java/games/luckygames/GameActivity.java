package games.luckygames;

import android.bluetooth.BluetoothSocket;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {


    boolean permission;
    boolean end;
    int youScore,himScore;
    String sign;
    Button restart;

    ArrayList<Button> buttons = new ArrayList<>();
    private static int[] field= new int[9];
    TextView result=null;
    boolean begin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        youScore=0;
        himScore=0;
        begin=false;
        end=false;

        Bundle bun=getIntent().getExtras();
        sign=bun.getString("sign");

        if(sign!=null && sign.equals("O")) {
            permission = false;
        }

        ConnectedThread connectedThread = new ConnectedThread(SocketHandler.getSocket());
        connectedThread.start();

        if(sign.equals("O")) {
            connectedThread.write(10);
        }

        result=(TextView) findViewById(R.id.textView);
        restart=(Button) findViewById(R.id.restart);

        assignButtons(connectedThread);

        SocketHandler.getSocket();
    }

    private void restart(){
        if (end) {
            for (int i = 1; i < 10; i++) {
                setSign("", i);
                if((i-1)%2==0) buttons.get(i-1).setBackgroundColor(Color.parseColor("#97C5F1"));
                else buttons.get(i-1).setBackgroundColor(Color.parseColor("#406BD0"));
            }
            end=false;
        }
    }

    private int checkField()
    {
        if(!end) {
            //checking anywhere if there are 3 "O" in a row
            if (checkAllO()) {
                //setting result for "O" player
                setResultO();
                return 1;
            //checking anywhere if there are 3 "X" in a row
            } else if (checkAllX()){
                //setting result for "X" player
                setResultX();
                return 2;
            } else {
                //checking if all fields are filled
                boolean p=true;
                for (int i = 0; i < 9; i++) {
                    if (field[i] == 0) {
                        p = false;
                        break;
                    }
                }
                end = p;
            }
        }
        return 0;

    }

    private int setSign(String sign, int index){
        index--;
        if(field[index]!=0 && !sign.equals("")) return 0;

        if(sign!=null && sign.equals("X")) {
            field[index] = -1;
        }
        else if(sign!=null && sign.equals("O")) {
            field[index] = 1;
        }
        else field[index]=0;

        buttons.get(index).setText(sign);

        permission=!permission;

        checkField();

        return 0;

    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            int inBytes;

            while(true) {
                try{
                    DataInputStream in=new DataInputStream(mmInStream);

                    inBytes = in.readInt();
                    final int bytes=inBytes;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (bytes==0) {
                                restart();
                                permission=false;
                            }else if(bytes==10){
                                permission=true;
                            }
                            else if(sign.equals("X")){
                                setSign("O", bytes);
                            }
                            else setSign("X", bytes);

                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void write(int bytes) {
            try {
                DataOutputStream out =new DataOutputStream(mmOutStream);
                out.writeInt(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkO(int a, int b, int c){
        if(field[a]+field[b]+field[c]==3) {
            if(sign.equals("O")){
                buttons.get(a).setBackgroundColor(Color.parseColor("#AF00FF19"));
                buttons.get(b).setBackgroundColor(Color.parseColor("#AF00FF19"));
                buttons.get(c).setBackgroundColor(Color.parseColor("#AF00FF19"));
            } else {
                buttons.get(a).setBackgroundColor(Color.parseColor("#AFFF0000"));
                buttons.get(b).setBackgroundColor(Color.parseColor("#AFFF0000"));
                buttons.get(c).setBackgroundColor(Color.parseColor("#AFFF0000"));
            }
            return true;
        }
        return false;
    }

    private boolean checkX(int a, int b, int c){
        if(field[a]+field[b]+field[c]==-3) {
            if(sign.equals("X")){
                buttons.get(a).setBackgroundColor(Color.parseColor("#AF00FF19"));
                buttons.get(b).setBackgroundColor(Color.parseColor("#AF00FF19"));
                buttons.get(c).setBackgroundColor(Color.parseColor("#AF00FF19"));
            } else {
                buttons.get(a).setBackgroundColor(Color.parseColor("#AFFF0000"));
                buttons.get(b).setBackgroundColor(Color.parseColor("#AFFF0000"));
                buttons.get(c).setBackgroundColor(Color.parseColor("#AFFF0000"));
            }
            return true;
        }
        return false;
    }

    private boolean checkAllO(){

        return checkO(0, 1, 2) || checkO(3, 4, 5) || checkO(6, 7, 8)
                || checkO(0, 3, 6) || checkO(1, 4, 7) || checkO(2, 5, 8)
                || checkO(0, 4, 8) || checkO(2, 4, 6);
    }

    private boolean checkAllX(){

        return checkX(0, 1, 2) || checkX(3, 4, 5) || checkX(6, 7, 8)
                || checkX(0, 3, 6) || checkX(1, 4, 7) || checkX(2, 5, 8)
                || checkX(0, 4, 8) || checkX(2, 4, 6);
    }

    private void setResultO(){
        if (sign.equals("O")) {
            result.setText("You: " + (++youScore) + "     Him:" + (himScore));
        } else result.setText("You: " + (youScore) + "     Him:" + (++himScore));
        permission = false;
        end = true;
    }

    private void setResultX(){
        if (sign.equals("O")) {
            result.setText("You: " + (youScore) + "     Him:" + (++himScore));
        } else result.setText("You: " + (++youScore) + "     Him:" + (himScore));
        permission = false;
        end = true;
    }


    private void assignButtons(final ConnectedThread connectedThread){
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(end){
                    restart();
                    permission=true;
                    connectedThread.write(0);}
            }
        });

        buttons.add((Button) findViewById(R.id.button1));
        buttons.add((Button) findViewById(R.id.button2));
        buttons.add((Button) findViewById(R.id.button3));
        buttons.add((Button) findViewById(R.id.button4));
        buttons.add((Button) findViewById(R.id.button5));
        buttons.add((Button) findViewById(R.id.button6));
        buttons.add((Button) findViewById(R.id.button7));
        buttons.add((Button) findViewById(R.id.button8));
        buttons.add((Button) findViewById(R.id.button9));


        buttons.get(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permission) {
                    setSign(sign, 1);
                    connectedThread.write(1);
                }
            }
        });
        buttons.get(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permission) {
                    setSign(sign, 2);
                    connectedThread.write(2);
                }
            }
        });
        buttons.get(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permission) {
                    setSign(sign, 3);
                    connectedThread.write(3);
                }
            }
        });
        buttons.get(3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permission) {
                    setSign(sign, 4);
                    connectedThread.write(4);
                }
            }
        });
        buttons.get(4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permission) {
                    setSign(sign, 5);
                    connectedThread.write(5);
                }
            }
        });
        buttons.get(5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permission) {
                    setSign(sign, 6);
                    connectedThread.write(6);
                }
            }
        });
        buttons.get(6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permission) {
                    setSign(sign, 7);
                    connectedThread.write(7);
                }
            }
        });
        buttons.get(7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permission) {
                    setSign(sign, 8);
                    connectedThread.write(8);
                }
            }
        });
        buttons.get(8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permission) {
                    setSign(sign, 9);
                    connectedThread.write(9);
                }
            }
        });
    }
}
