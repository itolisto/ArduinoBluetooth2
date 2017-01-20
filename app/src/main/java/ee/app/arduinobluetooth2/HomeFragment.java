package ee.app.arduinobluetooth2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.HashSet;
import java.util.Set;

import ee.app.arduinobluetooth2.adapters.BluetoothRvAdapter;
import ee.app.arduinobluetooth2.connection.BluetoothService;

public class HomeFragment extends Fragment {

    private FloatingActionButton btnInit;
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    public static BluetoothRvAdapter mUserListAdapter;
    private RecyclerView rvAvailableDevices;
    private RelativeLayout rlNoDevicesView;
    private BluetoothService mChatService;

    private static final int BLUETOOTH_REQUEST_CODE = 100;

    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
        pairedDevices = new HashSet<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        rlNoDevicesView = (RelativeLayout) rootView.findViewById(R.id.rlNoDeviceConnected);
        rvAvailableDevices = (RecyclerView) rootView.findViewById(R.id.rvAvailableDevices);
        btnInit = (FloatingActionButton) rootView.findViewById(R.id.fabInit);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        rvAvailableDevices.setLayoutManager(new LinearLayoutManager(getContext()));
        mChatService = BluetoothService.getInstance(getView(), mHandler, bluetoothAdapter);

        // Initialize the BluetoothChatService to perform bluetooth connections
        if (bluetoothAdapter != null && (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON || bluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON)) {
            mUserListAdapter   = new BluetoothRvAdapter((AppCompatActivity) getActivity(), bluetoothAdapter, mChatService);
        } else {
            mUserListAdapter   = new BluetoothRvAdapter((AppCompatActivity) getActivity(), null, null);
        }

        rvAvailableDevices.setAdapter(mUserListAdapter);
        rvAvailableDevices.setItemAnimator(new DefaultItemAnimator());

        onClickListeners();

        // Register the BroadcastReceiver
        if (bluetoothAdapter != null) {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            getActivity().registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothAdapter != null)
            getActivity().unregisterReceiver(mReceiver);
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                reloadData(device);
            }
        }
    };

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

    @Override
    public void onResume() {
        super.onResume();

        if(bluetoothAdapter != null) {

            if (mChatService != null) {
                mChatService.setContext(getView());
                mChatService.setHandler(mHandler);
                mChatService.setAdapter(bluetoothAdapter);
            }

            if (bluetoothAdapter.isEnabled()) {
                if (mChatService.getState() == BluetoothService.STATE_CONNECTED) {
                    if (Build.VERSION.SDK_INT > 20) {
                        btnInit.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_bound, null));
                    } else {
                        btnInit.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_bound));
                    }
                } else {
                    if (Build.VERSION.SDK_INT > 20) {
                        btnInit.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_search, null));
                    } else {
                        btnInit.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_search));
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT > 20) {
                    btnInit.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_on,null));
                } else {
                    btnInit.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_on));
                }
            }
        } else {
            if (Build.VERSION.SDK_INT > 20) {
                btnInit.setImageDrawable(getResources().getDrawable(R.drawable.ic_status_error,null));
            } else {
                btnInit.setImageDrawable(getResources().getDrawable(R.drawable.ic_status_error));
            }
        }

        if (mUserListAdapter.getItemCount() == 0) {
            rlNoDevicesView.setVisibility(View.VISIBLE);
            rvAvailableDevices.setVisibility(View.GONE);
        } else {
            rlNoDevicesView.setVisibility(View.GONE);
            rvAvailableDevices.setVisibility(View.VISIBLE);
        }
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

    private void onClickListeners() {
        btnInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothAdapter == null) {
                    Snackbar.make(view, "Dispositivo no soporta Bluetooth", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {

                    if (mChatService.getState() == BluetoothService.STATE_CONNECTED) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("¿Seguro que desea desconectar el dispositivo?")
                                .setCancelable(false)
                                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        mChatService.stop();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();

                        return;
                    }

                    if (bluetoothAdapter.isEnabled()) {
                        if (bluetoothAdapter.isDiscovering()) {
                            Snackbar.make(view, "Espera a que se termine de encontrar otros dispositivos", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        } else {
                            if (bluetoothAdapter.startDiscovery()) {
                                mUserListAdapter.setAdapter(bluetoothAdapter);
                                // Limpiar
                                mUserListAdapter.removeAll();

                                pairedDevices = bluetoothAdapter.getBondedDevices();
                                // If there are paired devices
                                if (pairedDevices.size() > 0) {

                                    for (BluetoothDevice device : pairedDevices) {
                                        mUserListAdapter.addItem(device);
                                    }

                                    // Show view
                                    rlNoDevicesView.setVisibility(View.GONE);
                                    rvAvailableDevices.setVisibility(View.VISIBLE);
                                }

                                Snackbar.make(view, "Buscando dispositivos...", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            } else {
                                Snackbar.make(view, "Error al buscar dispositivos", Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                            }
                        }
                    } else {
                        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(turnOn, BLUETOOTH_REQUEST_CODE);
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BLUETOOTH_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Snackbar.make(this.getView(), "Encendido", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                } else {
                    Snackbar.make(this.getView(), "No se ha encendido", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
                break;
            default:
                break;
        }
    }

    private void reloadData(BluetoothDevice device) {
        mUserListAdapter.addItem(device);

        if (mUserListAdapter.getItemCount() > 0) {
            rlNoDevicesView.setVisibility(View.GONE);
            rvAvailableDevices.setVisibility(View.VISIBLE);
        }
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
                            Snackbar.make(getView(), "Dispositivo conectado", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                            if (Build.VERSION.SDK_INT > 20) {
                                btnInit.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_bound,null));
                            } else {
                                btnInit.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_bound));
                            }

                            ArduinoBluetooth2.getPreferences().setConnectionStatus(true);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Snackbar.make(getView(), "Conectando", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            if (Build.VERSION.SDK_INT > 20) {
                                btnInit.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_search,null));
                            } else {
                                btnInit.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_search));
                            }

                            ArduinoBluetooth2.getPreferences().setConnectionStatus(false);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    break;
                case Constants.MESSAGE_READ:
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    break;
                case Constants.MESSAGE_TOAST:
                    break;
            }
        }
    };
}
