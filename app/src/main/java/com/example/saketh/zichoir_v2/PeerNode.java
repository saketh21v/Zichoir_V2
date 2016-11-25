package com.example.saketh.zichoir_v2;

import java.io.Serializable;

/**
 * Created by Saketh on 25-11-2016.
 */

public class PeerNode implements Serializable{

    public final String ID;
    public final String IP;
    public final String[] songNames;

    public PeerNode(String id, String ip, String[] songNames) {
        ID = id;
        IP = ip;
        this.songNames = songNames;
    }
}
