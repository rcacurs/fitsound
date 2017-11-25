package lv.edi.fitsound;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsConnectionListener;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsSubscription;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleDevice;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.spotify.sdk.android.player.Metadata;
import com.squareup.picasso.Picasso;

import java.util.Vector;


public class MainActivity extends Activity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback, Player.OperationCallback
{
    // TODO: Replace with your client ID

    private static final String CLIENT_ID = "ab3cf19326c44d15be7e8c80d351f849";
    private static final String TAG="FitSound";
    public static final String URI_CONNECTEDDEVICES = "suunto://MDS/ConnectedDevices";
    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";
    public static final String SCHEME_PREFIX = "suunto://";
    long previousTimetamp = 0;
    long currentTimestamp = 0;
    // Sensor subscription
    static private String URI_SERVICE = "/Sample/JumpCounter/JumpCount";//"/Sample/EdiJunction";//"/Meas/Acc/26";//"/Meas/IMU/13";//"/Meas/Acc/13";
    static private String URI_SERVICE2 = "/Meas/IMU6/26";
    String connectedSensorSerial;
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "yourcustomprotocol://callback";

    private Player mPlayer;

    private static final int REQUEST_CODE = 1337;

    private int current_SPM = 0;

//    private String playlist_70_90 =

    TextView songTitleLable;
    TextView songAlbumLable;
    TextView songArtistLable;
    // BleClient singleton
    static private RxBleClient mBleClient;
    private Mds mMds;
    RxBleDevice bleDevice;

    private MdsSubscription mdsSubscription;
    private MdsSubscription mdsSubscription2;
    ImageView albumArt;
    EditText testBPM;

    Vector<String> songTitles = new Vector();


    @Override           // Pārveido orģinālās klases funkcijas.
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The only thing that's different is we added the 5 lines below.
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        songTitleLable = (TextView)findViewById(R.id.track_label);
        songArtistLable = (TextView)findViewById(R.id.artist_label);
        songAlbumLable = (TextView)findViewById(R.id.album_label);
        albumArt = (ImageView)findViewById(R.id.album_art);

        // ble

        mMds = Mds.builder().build(this);

        mBleClient = RxBleClient.create(this);
//        RxBleDevice bleDevice = mBleClient.getBleDevice("0C:8C:DC:21:47:1D");//"");
        RxBleDevice bleDevice = mBleClient.getBleDevice("80:1F:02:4E:F1:70");
        mMds.connect(bleDevice.getMacAddress(), new MdsConnectionListener() {

            @Override
            public void onConnect(String s) {
                Log.d(TAG, "Connecting:" + s);
            }

            @Override
            public void onConnectionComplete(String macAddress, String serial) {
                Log.d(TAG, "CONNETED! serial - "+serial);
                connectedSensorSerial = serial;
                subscribeToSensor(connectedSensorSerial);

//                for (MyScanResult sr : mScanResArrayList) {
//                    if (sr.macAddress.equalsIgnoreCase(macAddress)) {
//                        sr.markConnected(serial);
//                        break;
//                    }
//                }
//                mScanResArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(MdsException e) {
                Log.e(TAG, "Error connecting:" + e);

            }

            @Override
            public void onDisconnect(String bleAddress) {
                Log.d(TAG, "DEVICE DISCONNECTED: " + bleAddress);
//                for (MyScanResult sr : mScanResArrayList) {
//                    if (bleAddress.equals(sr.macAddress))
//                        sr.markDisconnected();
//                }
//                mScanResArrayAdapter.notifyDataSetChanged();
            }
        });
        testBPM = (EditText) findViewById(R.id.spm_input);

    }

    @Override
    // Atgriešanās no citas aktivitātes
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        // The next 19 lines of the code are what you need to copy & paste! :)
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addNotificationCallback(MainActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    // Izsauc Spotify API
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            case kSpPlaybackNotifyMetadataChanged:
                Metadata.Track track = mPlayer.getMetadata().currentTrack;
                Log.d(TAG, track.toString());
                songTitleLable.setText(track.name);
                songArtistLable.setText(track.artistName);
                songAlbumLable.setText(track.albumName);

                Picasso.with(this).load(track.albumCoverWebUrl).into(albumArt);
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");

        // This is the line that plays a song.
//        mPlayer.playUri(null, "spotify:track:2TpxZ7JUBn3uw46aR7qd6V", 0, 0);
//        mPlayer.playUri(null, "spotify:track:29zkoUsOE50f0I3n44LjjU", 0, 0);
        mPlayer.playUri(null, "spotify:user:spotify:playlist:37i9dQZEVXcTmxDvQtsYYP", 0, 0);
        mPlayer.pause(null);
//        mPlayer.queue(null, "spotify:track:2TpxZ7JUBn3uw46aR7qd6V");
//        mPlayer.queue(null, "spotify:track:29zkoUsOE50f0I3n44LjjU");
//

    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error var1) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    public void onClickPlay(View v){
        Log.d(TAG, "PRESSED PLAY");
        mPlayer.resume(null);

    }

    public void onClickPause(View v){
        Log.d(TAG, "PRESSED PAUSE");
        mPlayer.pause(this);
    }

    public void onClickPrevious(View v){
        Log.d(TAG, "PRESSED PREVIOUS");
        mPlayer.skipToPrevious(null);
    }

    public void onClickNext(View v){
        Log.d(TAG, "PRESSED NEXT");
        mPlayer.skipToNext(null);
    }

    public void setSPM(View v){
        Log.d("MyTag","Hello");
        String valueStr = testBPM.getText().toString();
        current_SPM = Integer.parseInt(valueStr);
        Log.d(TAG, String.format("Steps per minute set to %d !", current_SPM));
    }

    @Override
    public void onError(Error error){

    }

    @Override
    public void onSuccess(){

    }

    private void subscribeToSensor(String connectedSerial) {
        // Clean up existing subscription (if there is one)
        if (mdsSubscription != null) {
//            unsubscribe();
        }

        // Build JSON doc that describes what resource and device to subscribe
        // Here we subscribe to 13 hertz accelerometer data
        StringBuilder sb = new StringBuilder();
        String strContract = sb.append("{\"Uri\": \"").append(connectedSerial).append(URI_SERVICE).append("\"}").toString();
        Log.d(TAG, strContract);


        mdsSubscription = mMds.builder().build(this).subscribe(URI_EVENTLISTENER,
                strContract, new MdsNotificationListener() {
                    @Override
                    public void onNotification(String data) {
                        Log.d(TAG, "Subscription data received: " + data);
                        BPMResponse bpmResponse = new Gson().fromJson(data, BPMResponse.class);
                        Log.d(TAG, "RECEIVED SUBSCRIPTION: "+data);

                        if (bpmResponse != null ) {
//
                            Log.d(TAG, "RESPONSE TIMESTAMP "+bpmResponse.body.timestamp);
                            currentTimestamp = bpmResponse.body.timestamp;
                            double delta = (double)currentTimestamp - previousTimetamp;
                            previousTimetamp = currentTimestamp;
                            Log.d(TAG, "RESPONSE BPM "+ 1/(delta/60000));
                        }
//                        if (accResponse != null && accResponse.body.array.length > 0) {
//
//                            Log.d(TAG, "Acc: time - " + accResponse.body.timestamp + ", " + accResponse.body.array[0].x + ", " + accResponse.body.array[0].y + ", " + accResponse.body.array[0].z);
//                        }
//                        // If UI not enabled, do it now
//                        if (sensorUI.getVisibility() == View.GONE)
//                            sensorUI.setVisibility(View.VISIBLE);
//
//                        AccDataResponse accResponse = new Gson().fromJson(data, AccDataResponse.class);
//                        if (accResponse != null && accResponse.body.array.length > 0) {
//
//                            String accStr =
//                                    String.format("%.02f, %.02f, %.02f", accResponse.body.array[0].x, accResponse.body.array[0].y, accResponse.body.array[0].z);
//
//                            ((TextView)findViewById(R.id.sensorMsg)).setText(accStr);
//                        }
                    }

                    @Override
                    public void onError(MdsException error) {
                        Log.e(TAG, "subscription onError(): ", error);
//                        unsubscribe();
                    }
                });
        sb = new StringBuilder();
        strContract = sb.append("{\"Uri\": \"").append(connectedSerial).append(URI_SERVICE2).append("\"}").toString();
//        mdsSubscription2 = mMds.builder().build(this).subscribe(URI_EVENTLISTENER,
//                strContract, new MdsNotificationListener() {
//                    @Override
//                    public void onNotification(String data) {
//                        Log.d(TAG, "Subscription data received: " + data);
//                        Imu6DataResponse accResponse = new Gson().fromJson(data, Imu6DataResponse.class);
//                        Log.d(TAG, "RECEIVED SUBSCRIPTION 2");
////                        if (accResponse != null && accResponse.body.array.length > 0) {
////
//////                            Log.d(TAG, "Acc: time - " + accResponse.body.timestamp + ", " + accResponse.body.array[0].x + ", " + accResponse.body.array[0].y + ", " + accResponse.body.array[0].z);
////                        }
////                        // If UI not enabled, do it now
////                        if (sensorUI.getVisibility() == View.GONE)
////                            sensorUI.setVisibility(View.VISIBLE);
////
////                        AccDataResponse accResponse = new Gson().fromJson(data, AccDataResponse.class);
////                        if (accResponse != null && accResponse.body.array.length > 0) {
////
////                            String accStr =
////                                    String.format("%.02f, %.02f, %.02f", accResponse.body.array[0].x, accResponse.body.array[0].y, accResponse.body.array[0].z);
////
////                            ((TextView)findViewById(R.id.sensorMsg)).setText(accStr);
////                        }
//                    }
//
//                    @Override
//                    public void onError(MdsException error) {
//                        Log.e(TAG, "subscription onError(): ", error);
////                        unsubscribe();
//                    }
//                });
    }

}
