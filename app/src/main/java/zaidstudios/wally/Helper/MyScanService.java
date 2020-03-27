package zaidstudios.wally.Helper;

import android.app.Service;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.IBinder;
import android.widget.Toast;

import java.io.File;

public class MyScanService extends Service implements MediaScannerConnection.MediaScannerConnectionClient{
    String path;
    private String mFilename;
    private String mMimetype;
    private MediaScannerConnection mConn;
    public MyScanService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //we have some options for service

        if(intent != null){
            path = intent.getStringExtra("path");
        }
        File file = new File(path);



        this.mFilename = file.getAbsolutePath();
        mConn = new MediaScannerConnection(this, this);
        mConn.connect();


        //Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
        //start sticky means service will be explicity started and stopped
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //stopping the player when service is destroyed
        //Toast.makeText(this, "Service Stopped!", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onMediaScannerConnected() {
        mConn.scanFile(mFilename, mMimetype);
    }

    @Override
    public void onScanCompleted(String s, Uri uri) {
        mConn.disconnect();
        //Toast.makeText(this, "scan completed!", Toast.LENGTH_SHORT).show();
    }
}
