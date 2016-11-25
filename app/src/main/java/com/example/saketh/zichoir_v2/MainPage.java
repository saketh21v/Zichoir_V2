package com.example.saketh.zichoir_v2;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class MainPage extends AppCompatActivity {

    static final String ID = String.valueOf(Math.random()*10000000);
    static final String SongsFolderPath = "/sdcard/TempSongs/";

    static final String CentralServerIP = "10.6.4.246";
    static final int CentralServerPushPort = 5566;
    static final int CentralServerPullPort = 4455;

    static String[] LocalSongs;
    static PeerNode SelfPeer;

    static int NoOfPeers = 0;
    static PeerNode[] PeerNodes;

    static ArrayList<String> SongsList = new ArrayList<>();
    static HashMap<String, String> SongsIPTable = new HashMap<>();

    static AsyncTask<Void, Void, Void> UploadCatalogueTask;

    ListView lv;
    StableArrayAdapter adapter;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        lv = (ListView) findViewById(R.id.listViewSongs);

        updateCatalogueAndList();
        intent = new Intent(this, PeerSongsService.class);
        startService(intent);
        uploadAndDownloadCatalogues();
    }


    protected void updateCatalogueAndList(){
        uploadAndDownloadCatalogues();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                Intent i = new Intent(getApplicationContext(), PlayScreen.class);
                String song = adapter.getItem(position);
                i.putExtra("CurrentSong", song);
                String ip = SongsIPTable.get(song);
                Log.d("SONG", "IP : "+ ip+" Song : "+ song);
                i.putExtra("CurrentPeerIP", ip);
                startActivity(i);
            }
        });

    }

    protected void setupListView(){
        adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, SongsList);
        lv.setAdapter(adapter);
    }
    protected void updateSongsListAndIPTable(){
        for(PeerNode pn : PeerNodes){
            for(String s : pn.songNames){
                SongsList.add(s);
                SongsIPTable.put(s, pn.IP);
            }
        }
    }

    protected void uploadAndDownloadCatalogues(){
//        new UploadCatalogue().execute();
        UploadCatalogueTask = new UploadCatalogue();
        UploadCatalogueTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    protected void retrieveLocalSongNames(){
        File songsFolder = new File(SongsFolderPath);
        LocalSongs = songsFolder.list();
        String IP = "";
        SelfPeer = new PeerNode(ID, IP, LocalSongs);
    }


    private class UploadCatalogue extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            retrieveLocalSongNames();
            try {
                Socket centralServerSocket = new Socket(CentralServerIP, CentralServerPushPort);
                ObjectOutputStream oos = new ObjectOutputStream(centralServerSocket.getOutputStream());

//                oos.writeObject(ID);
//                oos.writeObject(SelfPeer.IP);
                oos.writeObject(SelfPeer.songNames);
                centralServerSocket.close();

                Log.d("IPADDR", "Object Sent");

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(), "Sent Catalogue", Toast.LENGTH_SHORT).show();
            new RetriveCatalogue().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }


    private class RetriveCatalogue extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Socket centralServerSocket = new Socket(CentralServerIP, CentralServerPullPort);
                ObjectInputStream ois = new ObjectInputStream(centralServerSocket.getInputStream());

                //Retrieving NoOfPeers
                NoOfPeers = (Integer) ois.readObject();

                //Loop to retrive the Peer Objects
                PeerNodes = new PeerNode[NoOfPeers];
                String id, ip;
                String[] songs;

                for(int i=0;i<NoOfPeers;i++){
                    id = (String) ois.readObject();
                    ip = (String) ois.readObject();
                    songs = (String[]) ois.readObject();

                    PeerNodes[i] = new PeerNode(id, ip, songs);
                }
                //Done retrieving peers
                centralServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(), "Received Catalogue", Toast.LENGTH_SHORT).show();

            //Final Setup
            updateSongsListAndIPTable();
            setupListView();
        }
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}
