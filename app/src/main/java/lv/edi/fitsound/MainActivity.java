package lv.edi.fitsound;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MainActivity extends Activity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback, Player.OperationCallback
{
    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "ab3cf19326c44d15be7e8c80d351f849";
    private static final String TAG="FitSound";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "yourcustomprotocol://callback";

    private Player mPlayer;

    private static final int REQUEST_CODE = 1337;

    private int current_SPM = 0;

    private List bpmPlaylistsURI = new ArrayList();

    TextView songTitleLable;
    TextView songAlbumLable;
    TextView songArtistLable;
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

        testBPM = (EditText) findViewById(R.id.spm_input);

        bpmPlaylistsURI.add("spotify:user:11127592734:playlist:29Iv9A4NFPjpKomT5TggMy");
        bpmPlaylistsURI.add("spotify:user:11127592734:playlist:366I8N1GYX5gkc9N0p1oLY");
        bpmPlaylistsURI.add("spotify:user:11127592734:playlist:5kT3vPLgp7orzdQdJAX93w");

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
        int playlistID = 0;
        String valueStr = testBPM.getText().toString();
        current_SPM = Integer.parseInt(valueStr);
        playlistID = (current_SPM-70)/20;
        if(playlistID<0){
            playlistID = 0;
        }
        else if (playlistID>2){
            playlistID = 2;
        }
        mPlayer.playUri(null, (String) bpmPlaylistsURI.get(playlistID), 0, 0);
        Log.d(TAG, String.format("Steps per minute set to %d !", current_SPM));
    }

    @Override
    public void onError(Error error){

    }

    @Override
    public void onSuccess(){

    }

}
