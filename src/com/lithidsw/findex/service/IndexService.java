package com.lithidsw.findex.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.lithidsw.findex.R;
import com.lithidsw.findex.MainActivity;
import com.lithidsw.findex.utils.C;
import com.lithidsw.findex.utils.FileWalker;
import com.lithidsw.findex.widget.WidgetUtils;

public class IndexService extends Service {

    Context context;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        start();
        return (START_NOT_STICKY);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    private void start() {
        final Thread thread = new Thread() {
            @Override
            public void run() {
                new FileWalker(context).main();
                stopSelf();
                WidgetUtils.update(context);
                sendBroadcast(new Intent(C.ACTION_INDEX_COMPLETE));
            }
        };
        thread.start();
        fg(context.getResources().getString(R.string.index_running));
    }

    private void fg(String message) {
        Bitmap bit = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_launcher);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(bit)
                .setProgress(0, 0, true)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(message);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                0, intent, 0);
        mBuilder.setContentIntent(resultPendingIntent);
        startForeground(1, mBuilder.build());
    }
}
