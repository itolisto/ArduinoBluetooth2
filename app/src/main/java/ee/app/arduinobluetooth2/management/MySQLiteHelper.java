package ee.app.arduinobluetooth2.management;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by edgargomez on 3/21/16.
 */
public class MySQLiteHelper {

    private static final String TAG = "MySQLiteHelper";
    private final Context context;
    private DatabaseHelperStatistic myDbHelperForStatistics;
    private SQLiteDatabase myDb;

    public static final int typeTemperatura = 1;
    public static final int typeHumedad = 2;

    public static final int typeGas = 3;
    public static final int typeHumo = 4;
    public static final int typeRiego = 5;
    public static final int typeVentilacion = 6;

    private static final String DATABASE_NAME1 = "statistics.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_ONE = "one";
    private static final String TABLE_TWO = "two";
    private static final String TABLE_THREE = "three";

    // Temperatura, Humedad
    private static final String TABLE_ONE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_ONE + "("
            + "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "\"value\" TEXT NOT NULL, "
            + "\"type\" INTEGER NOT NULL, "
            + "\"created_at\" INTEGER NOT NULL );";

    // Gas, Humo, Riego, Ventilacion
    private static final String TABLE_TWO_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_TWO + "("
            + "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "\"state\" INTEGER NOT NULL, "
            + "\"type\" INTEGER NOT NULL, "
            + "\"created_at\" INTEGER NOT NULL );";

    // Iluminacion
    private static final String TABLE_THREE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_THREE + "("
            + "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "\"value\" TEXT NOT NULL, "
            + "\"created_at\" INTEGER NOT NULL );";

    /************************************************************/
    /*********************OPEN/CLOSE METHODS*********************/
    /************************************************************/

    public MySQLiteHelper(Context context) {
        this.context = context;
        myDbHelperForStatistics = new DatabaseHelperStatistic(context);

        openStatisticTable();
        closeStatisticTable();
    }

    public MySQLiteHelper openStatisticTable() throws SQLException {
        myDb = myDbHelperForStatistics.getWritableDatabase();
        return this;
    }

    public void closeStatisticTable() {
        if (myDbHelperForStatistics != null) { myDbHelperForStatistics.close(); }
    }

    public boolean deleteDatabase(){
        context.deleteDatabase(DATABASE_NAME1);
        return true;
    }

    /************************************************************/
    /*********************OPERATIONS METHODS*********************/
    /************************************************************/
    public void saveTemperaturaHumedad(int type, String value) {
        ContentValues loginUser = new ContentValues();
        loginUser.put("value", value);

        switch (type) {
            case typeTemperatura:
                loginUser.put("type", typeTemperatura);
                break;
            case typeHumedad:
                loginUser.put("type", typeHumedad);
                break;
            default:
                loginUser.put("type", typeTemperatura);
                break;
        }

        loginUser.put("created_at", System.currentTimeMillis() / 1000);
        openStatisticTable();
        myDb.insert(TABLE_ONE, null, loginUser);
        closeStatisticTable();
    }

    public void saveEstado(int type, int value) {
        ContentValues loginUser = new ContentValues();
        loginUser.put("state", value);

        switch (type) {
            case typeGas:
                loginUser.put("type", typeGas);
                break;
            case typeHumo:
                loginUser.put("type", typeHumo);
                break;
            case typeRiego:
                loginUser.put("type", typeRiego);
                break;
            case typeVentilacion:
                loginUser.put("type", typeVentilacion);
                break;
            default:
                loginUser.put("type", typeVentilacion);
                break;
        }

        loginUser.put("created_at", System.currentTimeMillis() / 1000);
        openStatisticTable();
        myDb.insert(TABLE_TWO, null, loginUser);
        closeStatisticTable();
    }

    public void saveIluminacion(int state) {
        ContentValues loginUser = new ContentValues();
        loginUser.put("value", state);
        loginUser.put("created_at", System.currentTimeMillis() / 1000);
        openStatisticTable();
        myDb.insert(TABLE_THREE, null, loginUser);
        closeStatisticTable();
    }

    public String getReportOne(int type, long from, long to) {
        int dataType;
        String name;

        switch (type) {
            case typeTemperatura:
                name = "Temperatura";
                dataType = type;
                break;
            case typeHumedad:
                name = "Humedad";
                dataType = type;
                break;
            default:
                name = "Error";
                dataType = 0;
                break;
        }

        if (dataType == 0)
            return "";

        String query = "SELECT _id, value, created_at FROM " + TABLE_ONE
                + " WHERE type = " + dataType + " AND created_at >= " + from
                + " AND created_at <= " + to + " ORDER BY created_at DESC LIMIT 100";

        openStatisticTable();
        Cursor cursor = myDb.rawQuery(query, new String[]{});
        cursor.moveToFirst();

        String json = "\"" + name + "\":[";
        String data = "";

        while (!cursor.isAfterLast()) {
            String temp = "";
            temp = temp.concat("{");
            temp = temp.concat("\"_id\":" + "\"" + String.valueOf(cursor.getInt(0)) + "\"");
            temp = temp.concat(",");
            temp = temp.concat("\"value\":" + "\"" + cursor.getString(1) + "\"");
            temp = temp.concat(",");
            temp = temp.concat("\"created\":" + "\"" + String.valueOf(cursor.getInt(2)) + "\"");
            temp = temp.concat("}");
            if (cursor.moveToNext())
                temp = temp.concat(",");

            data = data.concat(temp);
        }

        json = json.concat(data);
        json = json.concat("]");

        // make sure to close the cursor
        cursor.close();
        closeStatisticTable();

        return json;
    }

    public String getReportTwo(int type, long from, long to) {
        int dataType;
        String name;

        switch (type) {
            case typeGas:
                name = "Gas";
                dataType = type;
                break;
            case typeHumo:
                name = "Humo";
                dataType = type;
                break;
            case typeRiego:
                name = "Riego";
                dataType = type;
                break;
            case typeVentilacion:
                name = "Ventilacion";
                dataType = type;
                break;
            default:
                name = "Error";
                dataType = 0;
                break;
        }

        if (dataType == 0)
            return "";

        String query = "SELECT _id, state, created_at FROM " + TABLE_TWO
                + " WHERE type = " + dataType + " AND created_at >= " + from
                + " AND created_at <= " + to + " ORDER BY created_at DESC LIMIT 100";

        openStatisticTable();
        Cursor cursor = myDb.rawQuery(query, new String[]{});
        cursor.moveToFirst();

        String json = "\"" + name + "\":[";
        String data = "";

        while (!cursor.isAfterLast()) {
            String temp = "";
            temp = temp.concat("{");
            temp = temp.concat("\"_id\":" + "\"" + String.valueOf(cursor.getInt(0)) + "\"");
            temp = temp.concat(",");
            temp = temp.concat("\"state\":" + "\"" + String.valueOf(cursor.getInt(1)) + "\"");
            temp = temp.concat(",");
            temp = temp.concat("\"created\":" + "\"" + String.valueOf(cursor.getInt(2)) + "\"");
            temp = temp.concat("}");
            if (cursor.moveToNext())
                temp = temp.concat(",");

            data = data.concat(temp);
        }

        json = json.concat(data);
        json = json.concat("]");

        // make sure to close the cursor
        cursor.close();
        closeStatisticTable();

        return json;
    }

    public String getReportThree(long from, long to) {
        String query = "SELECT _id, value, created_at FROM " + TABLE_THREE
                + " WHERE created_at >= " + from
                + " AND created_at <= " + to + " ORDER BY created_at DESC LIMIT 100";

        openStatisticTable();
        Cursor cursor = myDb.rawQuery(query, new String[]{});
        cursor.moveToFirst();

        String json = "\"Iluminacion\":[";
        String data = "";

        while (!cursor.isAfterLast()) {
            String temp = "";
            temp = temp.concat("{");
            temp = temp.concat("\"_id\":" + "\"" + String.valueOf(cursor.getInt(0)) + "\"");
            temp = temp.concat(",");
            temp = temp.concat("\"value\":" + "\"" + cursor.getString(1) + "\"");
            temp = temp.concat(",");
            temp = temp.concat("\"created\":" + "\"" + String.valueOf(cursor.getInt(2)) + "\"");
            temp = temp.concat("}");
            if (cursor.moveToNext())
                temp = temp.concat(",");

            data = data.concat(temp);
        }

        json = json.concat(data);
        json = json.concat("]");

        // make sure to close the cursor
        cursor.close();
        closeStatisticTable();

        return json;
    }

    // Opcion 1
    //getDate(82233213123L, "dd/MM/yyyy hh:mm:ss.SSS")
    // Opcion 2
    // SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    // String dateString = formatter.format(new Date(dateInMillis)));
    /**
     * Return date in specified format.
     * @param milliSeconds Date in milliseconds
     * @param dateFormat Date format
     * @return String representing date in specified format
     */
    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }



    /************************************************************/
    /*******************CREATE/UPGRADE METHODS*******************/
    /************************************************************/

    private static class DatabaseHelperStatistic extends SQLiteOpenHelper {

        DatabaseHelperStatistic(Context context) {
            super(context, DATABASE_NAME1, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_ONE_CREATE);
            db.execSQL(TABLE_TWO_CREATE);
            db.execSQL(TABLE_THREE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.e(TAG, "Upgrading database STATISTICS from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ONE_CREATE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TWO_CREATE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_THREE_CREATE);
            onCreate(db);
        }
    }
}
