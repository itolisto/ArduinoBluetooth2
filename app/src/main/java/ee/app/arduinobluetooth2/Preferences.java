package ee.app.arduinobluetooth2;

/**
 * Created by edgargomez on 3/21/16.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Preferences
 *
 * Holds and managed application's preferences.
 */

public class Preferences {

    // Defining SharedPreferences entries
    private static final String CONNECTION_STATUS = "CONNECTION_STATUS";
    private static final String FAN_STATUS    = "FAN_STATUS";
    private static final String WATER_STATUS  = "WATER_STATUS";
    private static final String LIGHT_STATUS  = "LIGHT_STATUS";

    private SharedPreferences sharedPreferences;

    /**
     * Gets a SharedPreferences instance that points to the default file that is
     * used by the preference framework in the given context.
     */
    public Preferences(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        setConnectionStatus(false);
        setFanStatus(false);
        setWaterStatus(false);
        setLightStatus(0);
    }

    /* ******************************************************************************** */
	/* ************************************ GETTERS *********************************** */
	/* ******************************************************************************** */
    public boolean getConnectionStatus()   { return sharedPreferences.getBoolean(CONNECTION_STATUS, false); }
    public boolean getFanStatus()   { return sharedPreferences.getBoolean(FAN_STATUS, false); }
    public boolean getWaterStatus() { return sharedPreferences.getBoolean(WATER_STATUS, false); }
    public int getLightStatus() { return sharedPreferences.getInt(LIGHT_STATUS, 0); }

    public SharedPreferences getSharedPreferences(){ return sharedPreferences; }

    public boolean cleanSharedPreferences(){
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.clear();
        return editor.commit();
    }
    /* ******************************************************************************** */
	/* ************************************ SETTERS *********************************** */
	/* ******************************************************************************** */

    public void setConnectionStatus(boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(CONNECTION_STATUS, value);
        editor.apply();
    }

    public void setFanStatus(boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(FAN_STATUS, value);
        editor.apply();
    }

    public void setWaterStatus(boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(WATER_STATUS, value);
        editor.apply();
    }

    public void setLightStatus(int value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putInt(LIGHT_STATUS, value);
        editor.apply();
    }
}