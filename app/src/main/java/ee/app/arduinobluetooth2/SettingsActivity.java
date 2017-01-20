package ee.app.arduinobluetooth2;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static ee.app.arduinobluetooth2.management.MySQLiteHelper.typeGas;
import static ee.app.arduinobluetooth2.management.MySQLiteHelper.typeHumedad;
import static ee.app.arduinobluetooth2.management.MySQLiteHelper.typeHumo;
import static ee.app.arduinobluetooth2.management.MySQLiteHelper.typeRiego;
import static ee.app.arduinobluetooth2.management.MySQLiteHelper.typeTemperatura;
import static ee.app.arduinobluetooth2.management.MySQLiteHelper.typeVentilacion;

/**
 * Created by edgargomez on 3/21/16.
 */
public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private List<String> categories;
    private Spinner spinner;
    private CoordinatorLayout clBackground;
    private DatePicker dpReportDate;
    private Button btnFrom;
    private Button btnTo;
    private long fromTimestamp;
    private long toTimestamp;

    private boolean isFromSet;
    private boolean isToSet;
    private boolean isTypeSet;

    public SettingsActivity () {
        isFromSet = false;
        isToSet   = false;
        isTypeSet = false;
        categories = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        clBackground = (CoordinatorLayout) findViewById(R.id.clBackground);
        dpReportDate = (DatePicker) findViewById(R.id.dpReportDate);
        Calendar calendar = Calendar.getInstance();
        dpReportDate.setMaxDate(calendar.getTime().getTime());
        btnFrom = (Button) findViewById(R.id.btnFrom);
        btnTo   = (Button) findViewById(R.id.btnTo);
        spinner = (Spinner) findViewById(R.id.spinner);
        Button btnSend = (Button) findViewById(R.id.btnSendReport);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        categories.add("Temperatura");
        categories.add("Humedad");
        categories.add("Gas");
        categories.add("Humo");
        categories.add("Riego");
        categories.add("Ventilacion");
        categories.add("Iluminacion");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(0);

        btnFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int day = dpReportDate.getDayOfMonth();
                int month = dpReportDate.getMonth();
                int year = dpReportDate.getYear();

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Calendar calendar = new GregorianCalendar(year, month, day);
                String formatedDate = sdf.format(calendar.getTime());
                fromTimestamp = calendar.getTime().getTime() / 1000;

                btnFrom.setText("Desde\n" + formatedDate);
                isFromSet = true;
            }
        });

        btnTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int   day  = dpReportDate.getDayOfMonth();
                int   month= dpReportDate.getMonth();
                int   year = dpReportDate.getYear();

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Calendar calendar = new GregorianCalendar(year, month, day);
                String formatedDate = sdf.format(calendar.getTime());
                toTimestamp = (calendar.getTime().getTime() / 1000) + ((23 * 60 * 60) + (59 * 60) + (59));

                btnTo.setText("Hasta\n" + formatedDate);
                isToSet = true;
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFromSet && isToSet && isTypeSet) {
                    Pubnub pubnub = new Pubnub("pub-c-b0e7d765-b3e3-42b5-9c2c-400c8458efbf", "sub-c-63d91f54-e4d8-11e5-b661-0619f8945a4f");
                    Callback callback = new Callback() {
                        public void successCallback(String channel, Object response) {
                           Log.e("successCallback", response.toString());
                        }
                        public void errorCallback(String channel, PubnubError error) {
                            Log.e("errorCallback", error.toString());
                        }
                    };
                    String data = getJsonReport();
                    data = data.replace("\\","");
                    Log.e("Reporte", data);
                    pubnub.publish("invernadero_channel", data, callback);
                } else if (!isFromSet && isToSet) {
                    Snackbar.make(clBackground, "Define la fecha de comienzo", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else if (isFromSet && !isToSet) {
                    Snackbar.make(clBackground, "Define la fecha de final", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else if (!isFromSet && !isToSet) {
                    Snackbar.make(clBackground, "Define el rango de fecha del reporte", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else if (!isTypeSet) {
                    Snackbar.make(clBackground, "Define el reporte que quieres generar", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

//        Log.e("afds", this.getDatabasePath("statistics.db").getAbsolutePath());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        isTypeSet = true;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        isTypeSet = false;
    }

    public String getJsonReport() {
        int type = spinner.getSelectedItemPosition() + 1;

        Log.e("getJsonReport", "Valor: " + fromTimestamp);
        Log.e("getJsonReport", "Valor: " + toTimestamp);

        switch(type) {
            case typeTemperatura:
            case typeHumedad:
                return ArduinoBluetooth2.getDB().getReportOne(type, fromTimestamp, toTimestamp);
            case typeGas:
            case typeHumo:
            case typeRiego:
            case typeVentilacion:
                return ArduinoBluetooth2.getDB().getReportTwo(type, fromTimestamp, toTimestamp);
            default:
                return ArduinoBluetooth2.getDB().getReportThree(fromTimestamp, toTimestamp);
        }
    }
}
