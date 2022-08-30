package a.gautham.statusdownloader.Utils;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import a.gautham.statusdownloader.Models.Status;
import a.gautham.statusdownloader.R;

public class Common {

    static final int MINI_KIND = 1;
    static final int MICRO_KIND = 3;

    public static final int GRID_COUNT = 2;

    private static final String CHANNEL_NAME = "GAUTHAM";

    public static final File STATUS_DIRECTORY = new File(Environment.getExternalStorageDirectory() +
            File.separator + "WhatsApp/Media/.Statuses");

    public static String APP_DIR;

    public static void copyFile(Status status, Context context, RelativeLayout container) {

        File file = new File(Common.APP_DIR);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Snackbar.make(container, "Something went wrong", Snackbar.LENGTH_SHORT).show();
            }
        }

        String fileName;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());

        if (status.isVideo()) {
            fileName = "VID_" + currentDateTime + ".mp4";
        } else {
            fileName = "IMG_" + currentDateTime + ".jpg";
        }

        File destFile = new File(file + File.separator + fileName);

        try {

            if (status.isApi30()) {
                ContentValues values = new ContentValues();
                Uri destinationUri;

                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                if (status.isVideo()) {
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "video/*");

                } else {
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "image/*");
                }

                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/Status Downloader");
                Uri collectionUri = MediaStore.Images.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY
                );
                destinationUri = context.getContentResolver().insert(collectionUri, values);

                InputStream inputStream = context.getContentResolver().openInputStream(status.getDocumentFile().getUri());
                OutputStream outputStream = context.getContentResolver().openOutputStream(destinationUri);
                IOUtils.copy(inputStream, outputStream);
            } else {
                org.apache.commons.io.FileUtils.copyFile(status.getFile(), destFile);
                destFile.setLastModified(System.currentTimeMillis());
                new SingleMediaScanner(context, file);
            }


        } catch (IOException e) {
            e.printStackTrace();
            Log.d("copyFile", "Error: " + e.toString());
        }

    }


    private static void showNotification(Context context, RelativeLayout container, File destFile, Status status) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            makeNotificationChannel(context);
        }

        Uri data = FileProvider.getUriForFile(context, "a.gautham.statusdownloader" + ".provider", new File(destFile.getAbsolutePath()));
        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (status.isVideo()) {
            intent.setDataAndType(data, "video/*");
        } else {
            intent.setDataAndType(data, "image/*");
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(context, CHANNEL_NAME);

        notification.setSmallIcon(R.drawable.ic_file_download_black)
                .setContentTitle(destFile.getName())
                .setContentText("File Saved to" + APP_DIR)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        assert notificationManager != null;
        notificationManager.notify(new Random().nextInt(), notification.build());

        Snackbar.make(container, "Saved to " + Common.APP_DIR, Snackbar.LENGTH_LONG).show();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void makeNotificationChannel(Context context) {

        NotificationChannel channel = new NotificationChannel(Common.CHANNEL_NAME, "Saved", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setShowBadge(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
    }

}
