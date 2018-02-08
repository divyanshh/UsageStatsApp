package net.simplifiedcoding.navigationdrawerexample;

import android.graphics.drawable.Drawable;

/**
 * Created by divyanshjain on 30/11/17.
 */

public class ListItem {

    Drawable thumbnail;
    String nameApp;
    String time;
    String packageName;

    public ListItem(Drawable thumbnail, String nameApp, String time , String packageName) {
        this.thumbnail = thumbnail;
        this.nameApp = nameApp;
        this.time = time;
        this.packageName = packageName;
    }

    public Drawable getThumbnail() {
        return thumbnail;
    }

    public String getNameApp() {
        return nameApp;
    }

    public String getTime() {
        return time;
    }

    public String getPackageName() {
        return packageName;
    }
}
