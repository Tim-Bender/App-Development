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

class ConnectionAdapter extends RecyclerView.Adapter<ConnectionAdapter.ViewHolder>  {

    private ArrayList<connection> connections;
    private Context mContext;
    private vehicle myvehicle;

    ConnectionAdapter(Context context, ArrayList<connection> connections,vehicle vehicle) {
        this.connections = connections;
        this.mContext = context;
        this.myvehicle = vehicle;
    }

    @Override
    public ConnectionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ConnectionAdapter.ViewHolder holder, int position) {
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

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTitleText = itemView.findViewById(R.id.title);
            mInfoText = itemView.findViewById(R.id.newsTitle);
        }

        @SuppressLint("SetTextI18n")
        void bindTo(connection currentConnection){
            mTitleText.setText("Pin: " + currentConnection.getS4());
            String temp = currentConnection.getName();
            String s1 = temp.substring(0, 1).toUpperCase();
            mInfoText.setText(s1 +temp.substring(1));

        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(mContext.getApplicationContext(), Pindiagnostic.class);
            i.putExtra("myvehicle", myvehicle);
            i.putParcelableArrayListExtra("connections",myvehicle.getConnections());
            i.putExtra("loc",getAdapterPosition());
            i.putParcelableArrayListExtra("uniqueconnections",new ArrayList<>(connections));
            mContext.startActivity(i);
        }
    }
}
