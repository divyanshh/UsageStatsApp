package net.simplifiedcoding.navigationdrawerexample;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by divyanshjain on 30/11/17.
 */

public class MyService extends Service {

    private android.os.Handler handler = new android.os.Handler();
    private Timer timer = new Timer();
    private Database database;
    private Context context;
    NotificationManager nMN;
    private static int NOTIFICATION_ID = 123456;
    public static final String TAG = UStats.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("M-d-yyyy HH:mm:ss");


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // Let it continue running until it is stopped.
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

        List apps = new ArrayList<>();
        final String[] activityOnTop = {null};

        PackageManager packageManager = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> appList = packageManager.queryIntentActivities(mainIntent, 0);
        Collections.sort(appList, new ResolveInfo.DisplayNameComparator(packageManager));
        List<PackageInfo> packs = packageManager.getInstalledPackages(0);
        for(int i=0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            ApplicationInfo a = p.applicationInfo;
            // skip system apps if they shall not be included
            if((a.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                continue;
            }
            apps.add(p.packageName);
        }


        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        showOnNotification();

                    }
                });
            }
        } , 0 , 10000);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        printForegroundTask();

                    }
                });
            }
        } , 0 , 500);


        //Intent lockIntent = new Intent(mContext, LockScreen.class);
        //lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //mContext.startActivity(lockIntent);

        //return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    private void printForegroundTask() {
        String currentApp = "NULL";
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }

        Log.e("CURRENT APP", "Current App in foreground is: " + currentApp);

        database = new Database(this);
        final List<String> blockedApps = database.open().getValues();
        database.close();

        if (matches (currentApp , blockedApps)) {

            Intent startHomescreen=new Intent(Intent.ACTION_MAIN);
            startHomescreen.addCategory(Intent.CATEGORY_HOME);
            startHomescreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //startHomescreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(startHomescreen);

        }
    }

    private boolean matches(String currentForegroundApp, List<String> blockedApps) {
        for (String blocked : blockedApps) {
            Log.d("Blocked app" , blocked);
            if (currentForegroundApp.equals(blocked)) {
                return true;
            }
        }
        return false;
    }

    private void showOnNotification() {

        // TODO Auto-generated method stub

        UsageStatsManager usm = getUsageStatsManager(this);
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        //calendar.add(Calendar.YEAR, -1);

        //long days = TimeUnit.MILLISECONDS.toDays(endTime);
        //long startTime = endTime - TimeUnit.DAYS.toMillis(days);

        long startTime = calendar.getTimeInMillis(); // - 86400000;
        long days = TimeUnit.MILLISECONDS.toDays(startTime);  // day starts at 5.30
        startTime = TimeUnit.DAYS.toMillis(days);// + 66600000; // so add this no to get to other day

        Log.d(TAG, "Range start:" + dateFormat.format(startTime));
        Log.d(TAG, "Range end:" + dateFormat.format(endTime));

        List<UsageStats> usageStatsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        long totalTime = 0;

        for (UsageStats u : usageStatsList) {

            try {

                if ((u.getTotalTimeInForeground() > 0) && !u.getPackageName().equals("com.google.android.deskclock") && (!u.getPackageName().equals("com.google.android.googlequicksearchbox"))) {
                    Log.d(TAG, "Pkg: " + u.getPackageName() + "\t" + "ForegroundTime: "
                            + u.getTotalTimeInForeground());

                    Drawable icon;
                    PackageManager packageManager = getPackageManager();
                    icon = packageManager.getApplicationIcon(u.getPackageName());

                    totalTime = totalTime + u.getTotalTimeInForeground();

                }
            } catch (PackageManager.NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        /*NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this);
        builder.setContentTitle("Use-Less App");
        builder.setContentText("Total Time = " +totalTime);

        Notification notification = builder.build();
        NotificationManagerCompat.from(this).notify(0,notification);*/

        }

        showForegroundNotification(getDurationBreakdown(totalTime));
    }

        @SuppressWarnings("ResourceType")
        private static UsageStatsManager getUsageStatsManager (Context context){
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService("usagestats");
            return usm;
        }

    private void showForegroundNotification(String contentText) {
        // Create intent that will bring our app to the front, as if it was tapped in the app
        // launcher
        Intent showTaskIntent = new Intent(getApplicationContext(), MainActivity.class);
        showTaskIntent.setAction(Intent.ACTION_MAIN);
        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                showTaskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("Use-Less")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_menu_manage)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    public static String getDurationBreakdown(long millis)
    {
        if(millis < 0)
        {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);

        if (hours > 0) {
            sb.append(hours);
            sb.append(" Hr ");
        }
        sb.append(minutes);
        sb.append(" Min ");

        return(sb.toString());
    }

    public Context getContext() {
        return context;
    }
}
