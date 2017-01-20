package ee.app.arduinobluetooth2.adapters;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

import ee.app.arduinobluetooth2.R;
import ee.app.arduinobluetooth2.connection.BluetoothService;

public class BluetoothRvAdapter extends RecyclerView.Adapter<BluetoothRvAdapter.ViewHolder>{

	private List<BluetoothDevice> mUsers = new ArrayList<>();
	private AppCompatActivity mActivity;
    private BluetoothAdapter adapter;
    private BluetoothService mService = null;

	public BluetoothRvAdapter(AppCompatActivity activity, BluetoothAdapter adapter, BluetoothService service) {
		mUsers    = new ArrayList<>();
		mActivity = activity;
        mService  = service;
        this.adapter = adapter;
	}

    @Override
    public long getItemId(int position) { return super.getItemId(position); }

    @Override
    public int getItemCount() { return (mUsers == null) ? 0 : mUsers.size(); }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        BluetoothDevice user = mUsers.get(i);

        if( user.getBondState() == BluetoothDevice.BOND_BONDED ){
            holder.ivUserImage.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_action_bound));
        } else if( user.getBondState() == BluetoothDevice.BOND_BONDING ) {
            holder.ivUserImage.setImageDrawable(null);
        } else {
            holder.ivUserImage.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_action_unbound));
        }

        holder.tvUser.setText(user.getName());
        holder.tvLastMessage.setText(user.getAddress());
    }

    public void setAdapter(BluetoothAdapter adapter) {
        this.adapter = adapter;
    }

    public void addItem(BluetoothDevice device) {
        if (!mUsers.contains(device)) {
            mUsers.add(device);
            notifyItemInserted(mUsers.size());
        }
    }

    public void removeAll() {
        mUsers.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public RelativeLayout rlBluetoothLayout;
        public CircularImageView ivUserImage;
        public TextView tvUser;
        public TextView tvLastMessage;
        public LinearLayout llIndicatorLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            this.rlBluetoothLayout = (RelativeLayout) itemView
                    .findViewById(R.id.rlBluetoothItem);

            this.ivUserImage = (CircularImageView) itemView
                    .findViewById(R.id.ivUserImage);
            // Set Border
            this.ivUserImage.setBorderColor(mActivity.getResources().getColor(R.color.colorPrimaryDark));
            this.ivUserImage.setBorderWidth(10);
            // Add Shadow with default param
            this.ivUserImage.addShadow();
            // or with custom param
            this.ivUserImage.setShadowRadius(15);
            this.ivUserImage.setShadowColor(Color.RED);

            this.tvUser = (TextView) itemView
                    .findViewById(R.id.tvDeviceName);
            this.tvLastMessage = (TextView) itemView
                    .findViewById(R.id.tvMacAddress);

            this.llIndicatorLayout = (LinearLayout) itemView
                    .findViewById(R.id.llUserIndicators);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (adapter != null && mService != null) {
                if (adapter.isDiscovering()) {
                    adapter.cancelDiscovery();
                }

                BluetoothDevice user = mUsers.get(getAdapterPosition());
                mService.connect(user, true);
            } else {
                Snackbar.make(view, "No se puede conectar al dispositivo", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }
}
