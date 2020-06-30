package com.Diagnostic.Spudnik;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
class ConnectionAdapterHorizontal extends RecyclerView.Adapter<ConnectionAdapterHorizontal.ViewHolder>  {

    private ArrayList<connection> connections;
    private Context mContext;
    private vehicle myvehicle;

    ConnectionAdapterHorizontal(Context context, ArrayList<connection> connections, vehicle vehicle) {
        this.connections = connections;
        this.mContext = context;
        this.myvehicle = vehicle;
    }

    @Override
    public ConnectionAdapterHorizontal.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item_horizontal, parent, false));
    }

    @Override
    public void onBindViewHolder(ConnectionAdapterHorizontal.ViewHolder holder, int position) {
        connection currentConnection = connections.get(position);
        holder.bindTo(currentConnection);
    }

    @Override
    public int getItemCount() {
        return connections.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleText;
        private TextView mInfoText;
        private TextView voltage;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTitleText = itemView.findViewById(R.id.horizontaltitle);
            mInfoText = itemView.findViewById(R.id.horizontalnewsTitle);
            voltage = itemView.findViewById(R.id.hotizontalvoltage);
        }

        @SuppressLint("SetTextI18n")
        void bindTo(connection currentConnection){
            mTitleText.setText("Pin: " + currentConnection.getS4());
            String temp = currentConnection.getName();
            String s1 = temp.substring(0, 1).toUpperCase();
            mInfoText.setText(s1 +temp.substring(1));
            voltage.setText("6.6 mA");

        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(mContext, pinlocation.class);
            i.putExtra("myvehicle", myvehicle);
            i.putParcelableArrayListExtra("connections",myvehicle.getConnections());
            i.putExtra("myConnection",connections.get(getAdapterPosition()));
            mContext.startActivity(i);
        }
    }
}
