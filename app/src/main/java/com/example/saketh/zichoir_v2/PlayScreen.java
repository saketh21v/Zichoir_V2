package com.example.saketh.zichoir_v2;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class PlayScreen extends AppCompatActivity {

//    static final String ID = "#2222";
    static final int PeerServerPort = 9988;

    static String CurrentPeerIP;
    static String CurrentSong;

    static File TempSongFile = new File("/sdcard/TempS/TempSong.mp3");
    static Uri TempSongFileUri = Uri.parse(TempSongFile.getAbsolutePath());

    static MediaPlayer SongPlayer;
    static ProgressBar progressBar;

    static int prog;

    AsyncTask<Void, Void, Void> GetSongAsyncTask = new GetSongAsync();

    //UI elements
    Button button;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initializing :
        // Parse local songs and upload to catalogue.
//        localCatalogueUpload();

        // Retrieve catalogue from cloud
//        retrieveCloudCatalogue();

        Bundle extras = getIntent().getExtras();
        CurrentSong = extras.getString("CurrentSong");
        CurrentPeerIP = extras.getString("CurrentPeerIP");

        Log.d("SONG", "CurrentSong : "+CurrentSong + " CurrentIP : "+ CurrentPeerIP);

        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        String[] strings = {"10.6.12.24"};
                        Toast.makeText(PlayScreen.this, "I'm here", Toast.LENGTH_SHORT).show();
                        GetSongAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
        );

        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
//        while(prog < 90){
//            progressBar.setProgress(prog);
//        }

        handler = new Handler() {
            @Override
            public void publish(LogRecord logRecord) {
                progressBar.setProgress(prog);
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {

            }
        };
    }

    public void playSong(){
        Uri songFile = TempSongFileUri;
        try {
            Log.d("TOAST", "Playing");
            SongPlayer = new MediaPlayer();
            SongPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            SongPlayer.setDataSource(getApplicationContext(), songFile);

            SongPlayer.setOnPreparedListener(
                    new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            Log.d("PLAYING", "Playing");
                            mediaPlayer.start();
                        }
                    }
            );

            SongPlayer.prepareAsync();
            Log.d("SONG", "Playing");

            SongPlayer.setOnCompletionListener(
                    new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            mediaPlayer.release();
                            SongPlayer = null;
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected class GetSongAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Log.d("SONG", "Connecting to IP : "+ CurrentPeerIP+" Port : "+ PeerServerPort);
                Socket selectedSongSocket = new Socket(CurrentPeerIP, PeerServerPort);
                Log.d("SONG", "Connected");

                ObjectOutputStream ous = new ObjectOutputStream(selectedSongSocket.getOutputStream());
                ous.writeObject(CurrentSong);
                ous.flush();

                DataInputStream dis = new DataInputStream(selectedSongSocket.getInputStream());
                DataOutputStream dos = new DataOutputStream(new FileOutputStream(TempSongFile, false));

                Log.d("SONG", "In GetSyn");

                int max = (int) (4.66 * 1024 * 1024);
                int recv = 0;
                // Receive song and write into the tempSong file
                byte[] buffer = new byte[1024];
                int len = 1024;
                int count = 0;
                while(!(len < 1024)){
                    len = dis.read(buffer);
                    dos.write(buffer, 0, len);
                    Log.d("SONG", "count = "+count+++" len = "+len);

                    recv+=len;
                    prog = (recv/max)*100;
                    handler.publish(null);
                }
                // Socket and Stream Cleanup
                dos.flush();
                dos.close();
                selectedSongSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            Uri uri = Uri.parse("file:///sdcard/TempS/OSaiyyan.mp3");
            playSong();
            Log.d("SONG", "Exiting");
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
