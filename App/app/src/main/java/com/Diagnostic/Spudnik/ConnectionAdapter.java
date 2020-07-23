package com.Diagnostic.Spudnik;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Recyclerview adapter for connection objects
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @see androidx.recyclerview.widget.RecyclerView.Adapter
 * @see selectpin
 * @since dev 1.0.0
 */

class ConnectionAdapter extends RecyclerView.Adapter<ConnectionAdapter.ViewHolder> {

    /**
     * Our arraylist of connection objects that we will be building cards out of
     */
    private ArrayList<connection> connections;
    /**
     * Context of the activity on the main UI thread
     */
    private Context mContext;
    /**
     * vehicle object, we will need some methods from it
     */
    private vehicle myvehicle;

    ConnectionAdapter(@NonNull Context context, @NonNull ArrayList<connection> connections, @NonNull vehicle vehicle) {
        this.connections = connections;
        this.mContext = context;
        this.myvehicle = vehicle;
    }

    @Override //inflates views from our layout file R.layut.list_item....
    public ConnectionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false));
    }

    @Override //holds the views that are inflated. binds the view to a position
    public void onBindViewHolder(ConnectionAdapter.ViewHolder holder, int position) {
        connection currentConnection = connections.get(position);
        holder.bindTo(currentConnection);
    }

    @Override //for getting the size of the list
    public int getItemCount() {
        return connections.size();
    }

    /**
     * This class will hold the views...
     *
     * @author timothy.bender
     * @version dev 1.0.0
     * @since dev 1.0.0
     */
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
        void bindTo(connection currentConnection) {
            mTitleText.setText("Pin: " + currentConnection.getS4());
            String temp = currentConnection.getName(); //capitalize it
            String s1 = temp.substring(0, 1).toUpperCase();
            mInfoText.setText(s1 + temp.substring(1));

        }

        /**
         * Click redirect. Implementation of the View.OnClickListener. Send users to the pindiagnostic activity
         *
         * @param v View
         */
        @Override
        public void onClick(View v) {
            Intent i = new Intent(mContext.getApplicationContext(), Pindiagnostic.class);
            i.putExtra("myvehicle", myvehicle);
            i.putParcelableArrayListExtra("connections", myvehicle.getConnections());
            i.putExtra("loc", getAdapterPosition());
            i.putParcelableArrayListExtra("uniqueconnections", new ArrayList<>(connections));
            mContext.startActivity(i);
        }
    }
}
