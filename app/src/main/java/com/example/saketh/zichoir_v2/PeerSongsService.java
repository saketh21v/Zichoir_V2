package com.example.saketh.zichoir_v2;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerSongsService extends Service {

    //Constants
    static final int PeerServerPort = 9988;
    static final String SongsFolderPath = "/sdcard/TempSongs/";
    final static int BufferSize = 1024;

    // Server details
    static ServerSocket PeerSocketServer;
    static Socket PeerSocket;

    static String CurrentSongName;

    static DataOutputStream dos;
    static BufferedInputStream bis;

    static ObjectInputStream ois;

    public PeerSongsService() {
        new StartPeerServer().execute();
    }

    private class StartPeerServer extends AsyncTask<Object, Object, Void> {

        @Override
        protected Void doInBackground(Object... voids) {
            try {
                PeerSocketServer = new ServerSocket(PeerServerPort);

                // Infinite loop Server
                while(true){
                    Log.d("PEER", "Waiting...");
                    PeerSocket = PeerSocketServer.accept();
                    Log.d("PEER", "Accepted connection from IP : "+ PeerSocket.getInetAddress().getHostAddress());
                    ois = new ObjectInputStream(PeerSocket.getInputStream());
                    dos = new DataOutputStream(PeerSocket.getOutputStream());

                    CurrentSongName = (String) ois.readObject();
                    File songFile = new File(SongsFolderPath+CurrentSongName);
                    bis = new BufferedInputStream(new FileInputStream(songFile));

                    int fileSize = (int) songFile.length();

                    byte[] b = new byte[fileSize];

                    bis.read(b);
                    dos.write(b, 0, fileSize);

                    dos.flush();
//                dos.close();
                    bis.close();
                    PeerSocket.close();
                }

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
            Toast.makeText(getApplicationContext(), "Completed Asynctask from Server", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
