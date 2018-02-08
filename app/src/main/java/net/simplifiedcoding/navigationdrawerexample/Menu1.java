package net.simplifiedcoding.navigationdrawerexample;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class Menu1 extends Fragment {

    Button on;
    Button off;

    TextView totalTimeView;
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

        View v = inflater.inflate(R.layout.fragment_menu_1, container, false);
        totalTimeView = (TextView) v.findViewById(R.id.textView);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        on = (Button) v.findViewById(R.id.button3);
        off = (Button) v.findViewById(R.id.button);

        on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getActivity(), "button clicked", Toast.LENGTH_SHORT).show();
                getActivity().startService(new Intent(getActivity() , MyService.class));
            }
        });

        off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().stopService(new Intent(getActivity() , MyService.class));
            }
        });

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        listItems = new ArrayList<>();

        //Check if permission enabled
        if (UStats.getUsageStatsList(getActivity()).isEmpty()){
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }

        //backgroundServiceOn();

        /*statsBtn = (Button) findViewById(R.id.stats_btn);
        statsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UStats.printCurrentUsageStatus(MainActivity.this);

            }
        });*/

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
        getActivity().setTitle("Daily Usage");
    }

    public void backgroundServiceOn() {

        Toast.makeText(getActivity(), "start", Toast.LENGTH_SHORT).show();
        getActivity().startService(new Intent(getActivity() , MyService.class));

    }

    public void backgroundServiceOff(View view) {

        //stopService(new Intent(getContext() , MyService.class));

    }

    private void loadRecyclerViewData() throws PackageManager.NameNotFoundException {

        UsageStatsManager usm = getUsageStatsManager(getActivity());
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        //calendar.add(Calendar.YEAR, -1);

        //long days = TimeUnit.MILLISECONDS.toDays(endTime);
        //long startTime = endTime - TimeUnit.DAYS.toMillis(days);

        long startTime = calendar.getTimeInMillis(); // - 86400000;
        long days = TimeUnit.MILLISECONDS.toDays(startTime);  // day starts at 5.30
        startTime = TimeUnit.DAYS.toMillis(days) - 86400000; //+ 66600000; // so add this no to get to other day

        Log.d(TAG, "Range start:" + dateFormat.format(startTime));
        Log.d(TAG, "Range end:" + dateFormat.format(endTime));

        List<UsageStats> usageStatsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
        //List<String> appNames = new ArrayList<String>();

        long totalTime = 0;



            for (UsageStats u : usageStatsList) {

                try {

                if ((u.getTotalTimeInForeground() > 0) && !u.getPackageName().equals("com.google.android.deskclock") && (!u.getPackageName().equals("com.google.android.googlequicksearchbox"))) {
                    Log.d(TAG, "Pkg: " + u.getPackageName() + "\t" + "ForegroundTime: "
                            + u.getTotalTimeInForeground());


                    Drawable icon;
                    PackageManager packageManager = getContext().getPackageManager();
                    icon = packageManager.getApplicationIcon(u.getPackageName());
                    ListItem listItem = new ListItem(icon, getAppNameFromPkgName(getActivity(), u.getPackageName()), getDurationBreakdown(u.getTotalTimeInForeground()), u.getPackageName());
                    listItems.add(listItem);

                    totalTime = totalTime + u.getTotalTimeInForeground();

                }
            } catch (PackageManager.NameNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }

        adapter = new NoButtonAdapter(listItems, getActivity());
        recyclerView.setAdapter(adapter);
        totalTimeView.setText("" + getDurationBreakdown(totalTime));
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
