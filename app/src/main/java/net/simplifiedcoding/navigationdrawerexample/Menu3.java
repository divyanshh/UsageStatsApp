package net.simplifiedcoding.navigationdrawerexample;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Belal on 18/09/16.
 */


public class Menu3 extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<ListItem> listItems;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("M-d-yyyy HH:mm:ss");
    public static final String TAG = UStats.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments

        View v = inflater.inflate(R.layout.fragment_menu_3, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        listItems = new ArrayList<>();

        try {
            loadRecyclerViewData();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Unblock Apps");
    }

    private void loadRecyclerViewData() throws PackageManager.NameNotFoundException {

        UsageStatsManager usm = getUsageStatsManager(getActivity());
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        //calendar.add(Calendar.YEAR, -1);

        //long days = TimeUnit.MILLISECONDS.toDays(endTime);
        //long startTime = endTime - TimeUnit.DAYS.toMillis(days);

        long startTime = calendar.getTimeInMillis() - 86400000;

        Log.d(TAG, "Range start:" + dateFormat.format(startTime));
        Log.d(TAG, "Range end:" + dateFormat.format(endTime));

        List<UsageStats> usageStatsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
        //List<String> appNames = new ArrayList<String>();

        long totalTime = 0;

            for (UsageStats u : usageStatsList) {

                try{

                if ((u.getTotalTimeInForeground() > 0) && !u.getPackageName().equals("com.google.android.deskclock") && (!u.getPackageName().equals("com.google.android.googlequicksearchbox"))) {
                    Log.d(TAG, "Pkg: " + u.getPackageName() + "\t" + "ForegroundTime: "
                            + u.getTotalTimeInForeground());

                    Database database = new Database(getActivity());
                    final List<String> blockedApps = database.open().getValues();
                    database.close();

                    String currentApp = u.getPackageName();

                    if (matches(currentApp, blockedApps)) {

                        Drawable icon;
                        PackageManager packageManager = getContext().getPackageManager();
                        icon = packageManager.getApplicationIcon(u.getPackageName());
                        ListItem listItem = new ListItem(icon, getAppNameFromPkgName(getActivity(), u.getPackageName()), getDurationBreakdown(u.getTotalTimeInForeground()), u.getPackageName());
                        listItems.add(listItem);

                        totalTime = totalTime + u.getTotalTimeInForeground();

                    } else continue;

                }
            } catch  (PackageManager.NameNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }

        adapter = new RemoveAdapter(listItems, getActivity());
        recyclerView.setAdapter(adapter);
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

    @SuppressWarnings("ResourceType")
    private static UsageStatsManager getUsageStatsManager(Context context){
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService("usagestats");
        return usm;
    }

    public static String getAppNameFromPkgName(Context context, String Packagename) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo info = packageManager.getApplicationInfo(Packagename, PackageManager.GET_META_DATA);
            String appName = (String) packageManager.getApplicationLabel(info);
            return appName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
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
}
