package ee.app.arduinobluetooth2;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.plattysoft.leonids.ParticleSystem;

import ee.app.arduinobluetooth2.connection.BluetoothService;

public class FumeFragment extends Fragment {

    ////////////////////////////////////////////////////////////////////////
    //ConversaApp.getDB().messageCountForContact(id)////////////////////////
    ////////////////////////////////////////////////////////////////////////

    private RelativeLayout rlBackground;
    private FloatingActionButton fab;
    private TextView tvIsRaining;
    private ParticleSystem rainAnimation;

    /**
     * Member object for the chat services
     */
    private BluetoothService mService = null;
    private BluetoothAdapter bluetoothAdapter;
    private OnFragmentInteractionListener mListener;
    private final String TAG = "FumeFragment";

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    public FumeFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_fume, container, false);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if (bluetoothAdapter != null && (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON || bluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON)) {
            // Initialize the BluetoothChatService to perform bluetooth connections
            mService = BluetoothService.getInstance(getView(), mHandler, bluetoothAdapter);
            // Initialize the buffer for outgoing messages
            mOutStringBuffer = new StringBuffer("");
        }

        rlBackground = (RelativeLayout) rootView.findViewById(R.id.rlBackground);
        tvIsRaining = (TextView) rootView.findViewById(R.id.tvValor);
        tvIsRaining.setText("");
        rainAnimation = new ParticleSystem(getActivity(), 60, R.drawable.ic_object_drop, 9000);
        rainAnimation.setSpeedByComponentsRange(0f, 0f, 0.05f, 0.1f);
        rainAnimation.setAcceleration(0.00005f, 90);

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mService != null) {
                    // Only if the state is STATE_NONE, do we know that we haven't started already
                    if (mService.getState() == BluetoothService.STATE_CONNECTED) {
                        // Start the Bluetooth chat services
                        if (ArduinoBluetooth2.getPreferences().getWaterStatus()) {
                            stopRainAnimation();
                        } else {
                            startRainAnimation();
                        }
                        return;
                    }
                }

                // Mostrar mensaje de alerta
                sendMessage("");
            }
        });

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
                    if(ArduinoBluetooth2.getPreferences().getWaterStatus()) {
                        if (Build.VERSION.SDK_INT > 20) {
                            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_stop, null));
                        } else {
                            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_stop));
                        }
                    } else {
                        if (Build.VERSION.SDK_INT > 20) {
                            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_water, null));
                        } else {
                            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_water));
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (rainAnimation != null)
            rainAnimation.cancel();
    }

    public void startRainAnimation() {
        // Empezar
        ArduinoBluetooth2.getPreferences().setWaterStatus(true);
        ArduinoBluetooth2.getDB().saveEstado(
                ArduinoBluetooth2.getDB().typeRiego,
                1
        );

        if (Build.VERSION.SDK_INT > 20) {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_stop,null));
        } else {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_stop));
        }

        if (rainAnimation != null)
            rainAnimation.emitWithGravity(rlBackground, Gravity.TOP, 8);

        sendMessage("11#");
    }

    public void stopRainAnimation() {
        // Empezar
        ArduinoBluetooth2.getPreferences().setWaterStatus(false);
        ArduinoBluetooth2.getDB().saveEstado(
                ArduinoBluetooth2.getDB().typeRiego,
                0
        );

        if (Build.VERSION.SDK_INT > 20) {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_water,null));
        } else {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_water));
        }

        if (rainAnimation != null)
            rainAnimation.cancel();

        sendMessage("10#");
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
                    if (readMessage.startsWith("R")) {
                        String value = readMessage.substring(1);
                        if (value.length() == 1) {
                            boolean status = (value.equalsIgnoreCase("1")) ? false : true;
                            if (status) {
                                if(!ArduinoBluetooth2.getPreferences().getWaterStatus()) {
                                    tvIsRaining.setText("Si");
                                    startRainAnimation();
                                }
                            } else {
                                if(ArduinoBluetooth2.getPreferences().getWaterStatus()) {
                                    tvIsRaining.setText("No");
                                    stopRainAnimation();
                                }
                            }
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
