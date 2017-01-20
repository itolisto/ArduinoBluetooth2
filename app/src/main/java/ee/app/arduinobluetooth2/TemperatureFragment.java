package ee.app.arduinobluetooth2;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;

import ee.app.arduinobluetooth2.connection.BluetoothService;

public class TemperatureFragment extends Fragment {

    private ImageView ivFan;
    private SpeedometerGauge speedometer;
    private FloatingActionButton fab;
    private TextView tvTemperature;
    private TextView tvHumedad;

    /**
     * Member object for the chat services
     */
    private BluetoothService mService = null;
    private BluetoothAdapter bluetoothAdapter;
    private OnFragmentInteractionListener mListener;
    private final String TAG = "TemperatureFragment";

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    public TemperatureFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_temperature, container, false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if (bluetoothAdapter != null && (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON || bluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON)) {
            // Initialize the BluetoothChatService to perform bluetooth connections
            mService = BluetoothService.getInstance(getView(), mHandler, bluetoothAdapter);
            // Initialize the buffer for outgoing messages
            mOutStringBuffer = new StringBuffer("");
        }

        ivFan = (ImageView) rootView.findViewById(R.id.ivFan);
        tvTemperature = (TextView) rootView.findViewById(R.id.tvValor);
        tvTemperature.setText("");
        tvHumedad     = (TextView) rootView.findViewById(R.id.tvValorHumedad);
        tvHumedad.setText("");

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mService != null) {
                    // Only if the state is STATE_NONE, do we know that we haven't started already
                    if (mService.getState() == BluetoothService.STATE_CONNECTED) {
                        // Start the Bluetooth chat services
                        if(ArduinoBluetooth2.getPreferences().getFanStatus()) {
                            ArduinoBluetooth2.getDB().saveEstado(
                                    ArduinoBluetooth2.getDB().typeVentilacion,
                                    0
                            );

                            stopFanAnimation();
                        } else {
                            ArduinoBluetooth2.getDB().saveEstado(
                                    ArduinoBluetooth2.getDB().typeVentilacion,
                                    1
                            );

                            startFanAnimation();
                        }

                        return;
                    }
                }

                // Mostrar mensaje de alerta
                sendMessage("");
            }
        });

        // Customize SpeedometerGauge
        speedometer = (SpeedometerGauge) rootView.findViewById(R.id.speedometer);

        // Add label converter
        speedometer.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });

        // configure value range and ticks
        speedometer.setMaxSpeed(75);
        speedometer.setMajorTickStep(5);
        speedometer.setMinorTicks(1);

        // Configure value range colors
        speedometer.addColoredRange(0, 15, Color.GREEN);
        speedometer.addColoredRange(15, 45, Color.YELLOW);
        speedometer.addColoredRange(45, 75, Color.RED);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (bluetoothAdapter != null && mService != null) {
            // Initialize the BluetoothChatService to perform bluetooth connections
            mService = BluetoothService.getInstance(getView(), mHandler, bluetoothAdapter);
            // Initialize the buffer for outgoing messages
            mOutStringBuffer = new StringBuffer("");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(bluetoothAdapter != null) {

            // Performing this check in onResume() covers the case in which BT was
            // not enabled during onStart(), so we were paused to enable it...
            // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
            if (mService != null) {
                mService.setContext(getView());
                mService.setHandler(mHandler);
                mService.setAdapter(bluetoothAdapter);

                // Llamar a Arduino para preguntar por estado de ventilador

                if (mService.getState() == BluetoothService.STATE_CONNECTED) {
                    if(ArduinoBluetooth2.getPreferences().getFanStatus()) {
                        if (Build.VERSION.SDK_INT > 20) {
                            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_stop, null));
                        } else {
                            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_stop));
                        }
                    } else {
                        if (Build.VERSION.SDK_INT > 20) {
                            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_wind, null));
                        } else {
                            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_wind));
                        }
                    }
                } else {
                    if (Build.VERSION.SDK_INT > 20) {
                        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_unbound, null));
                    } else {
                        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_unbound));
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT > 20) {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_status_error,null));
                } else {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_status_error));
                }
            }
        } else {
            if (Build.VERSION.SDK_INT > 20) {
                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_status_error,null));
            } else {
                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_status_error));
            }
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mService != null) {
            if (mService.getState() != BluetoothService.STATE_CONNECTED) {
                try {
                    Snackbar.make(getView(), "Aun no se ha conectado a dispositivo", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } catch (NullPointerException e) {
                    Log.e(TAG, e.getMessage());
                }
                return;
            }

            // Check that there's actually something to send
            if (message.length() > 0) {
                // Get the message bytes and tell the BluetoothChatService to write
                byte[] send = message.getBytes();
                mService.write(send);

                // Reset out string buffer to zero and clear the edit text field
                mOutStringBuffer.setLength(0);
            }
        } else {
            try {
                Snackbar.make(getView(), "Aun no se ha conectado a dispositivo", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } catch (NullPointerException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void startFanAnimation() {
        // Empezar
        ArduinoBluetooth2.getPreferences().setFanStatus(true);

        Animation rotation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        rotation.setRepeatCount(Animation.INFINITE);
        if (ivFan != null)
            ivFan.startAnimation(rotation);

        if (Build.VERSION.SDK_INT > 20) {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_stop,null));
        } else {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_stop));
        }

        sendMessage("01#");
    }

    public void stopFanAnimation() {
        // Parar
        ArduinoBluetooth2.getPreferences().setFanStatus(false);

        if (ivFan != null)
            ivFan.clearAnimation();

        if (Build.VERSION.SDK_INT > 20) {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_wind,null));
        } else {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_wind));
        }

        sendMessage("00#");
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                        case BluetoothService.STATE_CONNECTING:
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Snackbar.make(getView(), "Me: " + writeMessage, Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if (readMessage.startsWith("T")) {
                        String value = readMessage.substring(1);
                        if (value.length() > 0) {
                            try {
                                double mappedValue = (Double.valueOf(value) / 1024) * 500;
                                value = String.format("%.2f", mappedValue);
                                tvTemperature.setText(value + "C");
                                if (mappedValue <= 0) {
                                    speedometer.setSpeed(0);
                                } else {
                                    speedometer.setSpeed(mappedValue);
                                }

                                ArduinoBluetooth2.getDB().saveTemperaturaHumedad(
                                        ArduinoBluetooth2.getDB().typeTemperatura,
                                        String.valueOf(value)
                                );
                            } catch (Exception e) {}
                        }
                    } else if (readMessage.startsWith("H")) {
                        String value = readMessage.substring(1);
                        if (value.length() > 0) {
                            try {
                                int mappedValue = Integer.valueOf(value);

                                tvHumedad.setText(mappedValue + "%");

                                ArduinoBluetooth2.getDB().saveTemperaturaHumedad(
                                        ArduinoBluetooth2.getDB().typeHumedad,
                                        String.valueOf(value)
                                );
                            } catch (Exception e) {}
                        }
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    //mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    //if (null != activity) {
                    //    Toast.makeText(activity, "Connected to "
                    //            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    //}
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Snackbar.make(getView(), "" + msg.getData().getString(Constants.TOAST), Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();
                    }
                    break;
            }
        }
    };
}
