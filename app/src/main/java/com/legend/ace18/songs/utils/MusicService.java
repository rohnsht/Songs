package com.legend.ace18.songs.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.legend.ace18.songs.MainActivity;
import com.legend.ace18.songs.R;
import com.legend.ace18.songs.model.Songs;

import java.util.List;
import java.util.Random;

/**
 * Created by rohan on 7/19/15.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener {

    private MediaPlayer player;
    private List<Songs> songsList;
    private int songIndex;

    public static Boolean isShuffle = false;
    public static int isRepeat = 0;
    public static Boolean isMusicSet = false;
    private Boolean isPausedOnCall = false;
    private String title, artist;
    private int totalDuration;

    private MusicServiceListener musicServiceListener;
    private final IBinder musicBind = new MusicBinder();
    private NotificationManager mNotificationManager;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
        initMusicPlayer();
    }

    private void initMusicPlayer() {
        //set listeners
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setMusicServiceListener(MusicServiceListener musicServiceListener) {
        this.musicServiceListener = musicServiceListener;
    }

    public void setList(List<Songs> songsList) {
        this.songsList = songsList;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                switch(state){
                    case TelephonyManager.CALL_STATE_IDLE:
                        if(player != null){
                            if(isPausedOnCall){
                                player.start();
                                isPausedOnCall = false;
                            }
                        }
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        if(player != null && player.isPlaying()){
                            player.pause();
                            isPausedOnCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        if(player != null && player.isPlaying()){
                            player.pause();
                            isPausedOnCall = true;
                        }
                        break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        return START_STICKY;
    }

    public void playSong(int songIndex) {
        this.songIndex = songIndex;
        Songs songs = songsList.get(songIndex);
        title = songs.getTitle();
        artist = songs.getArtist();
        totalDuration = songs.getDuration();
        isMusicSet = true;
        player.reset();
        try {
            player.setDataSource(songs.getPath());
            player.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pauseSong() {
        if (player.isPlaying()) {
            player.pause();
        }
    }

    public void resumeSong() {
        if (!player.isPlaying())
            player.start();
    }

    public void playNext() {
        if (isShuffle) {
            int newSong = songIndex;
            while (newSong == songIndex) {
                newSong = new Random().nextInt(songsList.size());
            }
            songIndex = newSong;
        } else if (isRepeat == 0) {
            songIndex++;
            if (songIndex >= songsList.size()) {
                musicServiceListener.onStopMusic();
                isMusicSet = false;
                player.stop();
                return;
            }
        } else if (isRepeat == 1) {
            songIndex++;
            if (songIndex >= songsList.size()) songIndex = 0;
        }
        playSong(songIndex);
    }

    public void playPrev() {
        songIndex--;
        if (songIndex < 0) songIndex = songsList.size() - 1;
        playSong(songIndex);
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }


    public Songs getSongDetails() {
        return songsList.get(songIndex);
    }

    public int getDuration() {
        if (player != null) return totalDuration;
        return 0;
    }

    public void seekTo(int time) {
        player.seekTo(time);
    }

    public int getCurrentPosition() {
        if (player != null)
            return player.getCurrentPosition();
        return 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        if (player.getCurrentPosition() > 0) {
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer player, int i, int i1) {
        Log.d("MUSIC PLAYER", "Playback Error");
        player.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        musicServiceListener.onPlayMusic(songsList.get(songIndex));
        setNotifications();
    }

    private void setNotifications() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(artist)
                        .setOngoing(true);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        if (mNotificationManager != null)
            mNotificationManager.cancelAll();
        player.release();
        isMusicSet = false;
    }

    //binder
    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public interface MusicServiceListener {
        void onPlayMusic(Songs songs);
        void onStopMusic();
    }

}
