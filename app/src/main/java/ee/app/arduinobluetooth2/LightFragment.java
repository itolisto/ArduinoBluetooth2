package ee.app.arduinobluetooth2;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import ee.app.arduinobluetooth2.connection.BluetoothService;

/**
 * Created by edgargomez on 3/21/16.
 */
public class LightFragment extends Fragment {

    ////////////////////////////////////////////////////////////////////////
    //ConversaApp.getDB().messageCountForContact(id)////////////////////////
    ////////////////////////////////////////////////////////////////////////

    private RelativeLayout rlBackground;
    private SeekBar sbGraduate;
    private TextView tvIntensidad;

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

    public LightFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_light, container, false);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if (bluetoothAdapter != null && (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON || bluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON)) {
            // Initialize the BluetoothChatService to perform bluetooth connections
            mService = BluetoothService.getInstance(getView(), mHandler, bluetoothAdapter);
            // Initialize the buffer for outgoing messages
            mOutStringBuffer = new StringBuffer("");
        }

        rlBackground = (RelativeLayout) rootView.findViewById(R.id.rlBackground);
        sbGraduate = (SeekBar) rootView.findViewById(R.id.sbGraduate);
        tvIntensidad = (TextView) rootView.findViewById(R.id.tvIntensidad);

        sbGraduate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvIntensidad.setText("Intensidad: " + progress + "/" + seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setLightBulbIntensity(seekBar.getProgress());
                ArduinoBluetooth2.getDB().saveIluminacion(seekBar.getProgress());

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
        if (bluetoothAdapter != null) {

            // Performing this check in onResume() covers the case in which BT was
            // not enabled during onStart(), so we were paused to enable it...
            // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
            if (mService != null) {
                mService.setContext(getView());
                mService.setHandler(mHandler);
                mService.setAdapter(bluetoothAdapter);

                // Llamar a Arduino para preguntar por estado de ventilador

                if (mService.getState() == BluetoothService.STATE_CONNECTED) {
                    sbGraduate.setEnabled(true);
                } else {
                    sbGraduate.setEnabled(false);
                    tvIntensidad.setText("Conecta un dispositivo primero");
                }
            } else {

            }
        } else {
//            if (Build.VERSION.SDK_INT > 20) {
//                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_status_error, null));
//            } else {
//                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_status_error));
//            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setLightBulbIntensity(int value) {
        // Empezar
        ArduinoBluetooth2.getPreferences().setLightStatus(value);
        Log.e("setLightBulbIntensity", "2" + value + "#");
        sendMessage("2" + value + "#");
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
                    if (readMessage.startsWith("L")) {
                        String value = readMessage.substring(1);
                        if (value.length() > 0) {
                            try {
                                int mappedValue = Integer.getInteger(value);
                                setLightBulbIntensity(mappedValue);
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
