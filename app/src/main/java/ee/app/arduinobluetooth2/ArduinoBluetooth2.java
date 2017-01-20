package ee.app.arduinobluetooth2;

import android.app.Application;
import android.support.v4.content.LocalBroadcastManager;

import ee.app.arduinobluetooth2.management.MySQLiteHelper;

/**
 * Created by edgargomez on 3/21/16.
 */
public class ArduinoBluetooth2 extends Application {

    private static ArduinoBluetooth2 sInstance;
    private MySQLiteHelper mDb;
    private Preferences mPreferences;
    public static boolean gOpenFromBackground;
    private LocalBroadcastManager mLocalBroadcastManager;

    /**
     * Called when the application is starting, before any other application objects have been created
     */
    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        gOpenFromBackground = true;
        mDb = new MySQLiteHelper(this);
        setPreferences(new Preferences(this));
        setLocalBroadcastManager(LocalBroadcastManager.getInstance(this));
    }

    /* ************************************************************************************************ */
	/* **********************************PREFERENCES/FILE/BROADCAST INIT******************************* */
	/* ************************************************************************************************ */
    public static Preferences getPreferences() {
        return sInstance.mPreferences;
    }

    public static LocalBroadcastManager getLocalBroadcastManager() {
        return sInstance.mLocalBroadcastManager;
    }

    public static MySQLiteHelper getDB() {
        return sInstance.mDb;
    }

    private void setPreferences(Preferences preferences) { mPreferences = preferences; }
    private void setLocalBroadcastManager(LocalBroadcastManager localBroadcastManager) {
        mLocalBroadcastManager = localBroadcastManager;
    }
}