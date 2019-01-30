package com.smiskol.speed_controller;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Properties;

public class ListenerService extends Service {

    public Context context = this;
    public Handler handler = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        System.out.println("Service created!");
        startListener();
    }

    public void startListener() {

        MediaSessionCompat ms = new MediaSessionCompat(getApplicationContext(), getPackageName());

        ms.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_FAST_FORWARD | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_STOP)
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1, SystemClock.elapsedRealtime())
                .build();
        ms.setPlaybackState(state);

        ms.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                KeyEvent keyEvent = (KeyEvent) mediaButtonIntent.getExtras().get(Intent.EXTRA_KEY_EVENT);
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyEvent.getKeyCode()) {
                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                            //connectSSH();
                            Toast.makeText(context, "Play/Pause", Toast.LENGTH_SHORT).show();
                            break;

                        case KeyEvent.KEYCODE_MEDIA_NEXT:
                            Toast.makeText(context, "Next", Toast.LENGTH_SHORT).show();
                            break;

                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                            Toast.makeText(context, "Previous", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                return super.onMediaButtonEvent(mediaButtonIntent);
            }
        });
        ms.setActive(true);
    }

    public void connectSSH() {
        String eonIP = "192.168.1.32"; //soon to be inputtable by user
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        try {
            JSch jsch = new JSch();
            File file = new File(getFilesDir(), "eon_id.ppk");
            jsch.addIdentity(file.getAbsolutePath());
            Session session = jsch.getSession("root", eonIP, 8022);

            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            prop.put("PreferredAuthentications", "publickey");
            session.setConfig(prop);

            session.connect();

            ChannelExec channelssh = (ChannelExec) session.openChannel("exec");
            //ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //channelssh.setOutputStream(baos);

            channelssh.setCommand("cd /data/HELLOTHERE; mkdir ITSFINALLYWORKING");
            channelssh.connect();
            channelssh.disconnect();
            //Toast.makeText(this, baos.toString(), Toast.LENGTH_SHORT).show();

            //return baos.toString();
        } catch (Exception e) {
            Toast.makeText(this, "Can't connect to EON.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        System.out.println("Service stopped");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "1")
                .setContentTitle("Bluetooth Listener Running")
                .setContentText("Tap to configure")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        notificationManager.notify(1, notification);
        startForeground(1, notification);

        return START_NOT_STICKY;
    }
}