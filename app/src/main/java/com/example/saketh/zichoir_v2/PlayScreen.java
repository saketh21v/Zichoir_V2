package com.example.saketh.zichoir_v2;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class PlayScreen extends AppCompatActivity {

    static final String ID = "#2222";
    static final int PeerServerPort = 9988;

    static String SongsFolderPath = "/sdcard/TempSongs/";
    static String[] LocalSongs;

    static final String CentralServerIP = "10.6.4.246";
    static final int CentralServerPort = 4455;

    static int NoOfPeers = 0;
    static PeerNode[] PeerNodes;

    static PeerNode CurrentPeer;
    static String CurrentSong;

    static File TempSongFile = new File("/sdcard/TempSong.mp3");
    static Uri TempSongFileUri = Uri.parse(TempSongFile.getAbsolutePath());

    static MediaPlayer SongPlayer = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initializing :
        // Parse local songs and upload to catalogue.
        localCatalogueUpload();

        // Retrieve catalogue from cloud
        retrieveCloudCatalogue();
    }

    protected void retrieveLocalSongNames(){
        File songsFolder = new File(SongsFolderPath);
        LocalSongs = songsFolder.list();
    }

    protected void localCatalogueUpload(){
        retrieveLocalSongNames();
    }

    protected void retrieveCloudCatalogue(){
        // initial socket setup to connect to central server
        try {
            Socket centralServerSocket = new Socket(CentralServerIP, CentralServerPort);
            ObjectInputStream ois = new ObjectInputStream(centralServerSocket.getInputStream());

            //Retrieving NoOfPeers
            NoOfPeers = (Integer) ois.readObject();

            //Loop to retrive the Peer Objects
            PeerNodes = new PeerNode[NoOfPeers];
            for(int i=0;i<NoOfPeers;i++){
                PeerNodes[i] = (PeerNode) ois.readObject();
            }
            //Done retrieving peers
            centralServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void getAndPlaySelectedSong(){
        // Setup of the Peer socket
        try {
            Socket selectedSongSocket = new Socket(CurrentPeer.IP, PeerServerPort);

            ObjectOutputStream ous = new ObjectOutputStream(selectedSongSocket.getOutputStream());
            ous.writeObject(CurrentSong);
            ous.flush();

            DataInputStream dis = new DataInputStream(selectedSongSocket.getInputStream());
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(TempSongFile, false));

            // Receive song and write into the tempSong file
            byte[] buffer = new byte[1024];
            int len = 0;
            while(len > -1){
                len = dis.read(buffer);
                dos.write(buffer, 0, len);
            }
            // Socket and Stream Cleanup
            dos.flush();
            dos.close();
            selectedSongSocket.close();

            //Setup the player and start playing.
            SongPlayer.setDataSource(getApplicationContext(), TempSongFileUri);
            SongPlayer.prepare();
            SongPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
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
