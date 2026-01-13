package net.kdt.pojavlaunch.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import net.kdt.pojavlaunch.MainActivity;
import net.kdt.pojavlaunch.R;

/** Full Rewrite: The background core that keeps Quattro running */
public class GameService extends Service {
    public static final String ACTION_STOP_GAME = "net.quattro.STOP_GAME";
    private static final String CHANNEL_ID = "Quattro_Runtime_Channel";
    private static final int NOTIF_ID = 101;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP_GAME.equals(intent.getAction())) {
            stopSelf();
            android.os.Process.killProcess(android.os.Process.myPid());
            return START_NOT_STICKY;
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Terminate Button for the Notification
        Intent stopIntent = new Intent(this, GameService.class);
        stopIntent.setAction(ACTION_STOP_GAME);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Quattro Game Engine")
                .setContentText("Minecraft is currently active")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Terminate Game", stopPendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(NOTIF_ID, notification);
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Quattro Execution Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
