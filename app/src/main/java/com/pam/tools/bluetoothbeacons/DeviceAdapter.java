package com.pam.tools.bluetoothbeacons;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class DeviceAdapter extends RecyclerView.Adapter <DeviceAdapter.MyViewHolder> {


    public static ArrayList<DeviceDetails> deviceDetails;


    public DeviceAdapter() {

    }


    @Override
    public @NonNull MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_data,
                parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        holder.tvName.setText( deviceDetails.get(position).getName() );
        holder.tvRSSI.setText( deviceDetails.get(position).getRssi() + " dBm" );
        holder.tvAddress.setText( deviceDetails.get(position).getAddress() );
        holder.tvTimeStamp.setText( deviceDetails.get(position).getTimeStamp() );

    }

    @Override
    public int getItemCount() {
        try {
            return deviceDetails.size();

        } catch (NullPointerException e) {
            return 0;
        }
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName;
        private TextView tvRSSI;
        private TextView tvAddress;
        private TextView tvTimeStamp;


        MyViewHolder(final View view) {
            super(view);

            tvName = view.findViewById(R.id.tv_device_name);
            tvRSSI = view.findViewById(R.id.tv_device_rssi);
            tvAddress = view.findViewById(R.id.tv_device__address);
            tvTimeStamp = view.findViewById(R.id.tv_device_timestamp);


        }
    }
}