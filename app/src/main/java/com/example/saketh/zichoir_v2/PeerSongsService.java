package com.example.saketh.zichoir_v2;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PeerSongsService extends Service {
    public PeerSongsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
