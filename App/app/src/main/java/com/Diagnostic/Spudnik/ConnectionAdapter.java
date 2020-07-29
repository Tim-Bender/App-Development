/*
 *
 *  Copyright (c) 2020, Spudnik LLc <https://www.spudnik.com/>
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are not permitted in any form.
 *
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION, DEATH, or SERIOUS INJURY or DAMAGE)
 *  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

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

import com.Diagnostic.Spudnik.CustomObjects.Connection;
import com.Diagnostic.Spudnik.CustomObjects.vehicle;

import java.util.ArrayList;

/**
 * Recyclerview adapter for connection objects
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @see androidx.recyclerview.widget.RecyclerView.Adapter
 * @see SelectPin
 * @since dev 1.0.0
 */

class ConnectionAdapter extends RecyclerView.Adapter<ConnectionAdapter.ViewHolder> {

    /**
     * Our arraylist of connection objects that we will be building cards out of
     */
    private ArrayList<Connection> Connections;
    /**
     * Context of the activity on the main UI thread
     */
    private Context mContext;
    /**
     * vehicle object, we will need some methods from it
     */
    private vehicle myvehicle;

    ConnectionAdapter(@NonNull Context context, @NonNull ArrayList<Connection> Connections, @NonNull vehicle vehicle) {
        this.Connections = Connections;
        this.mContext = context;
        this.myvehicle = vehicle;
    }

    @Override //inflates views from our layout file R.layut.list_item....
    public ConnectionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false));
    }

    @Override //holds the views that are inflated. binds the view to a position
    public void onBindViewHolder(ConnectionAdapter.ViewHolder holder, int position) {
        Connection currentConnection = Connections.get(position);
        holder.bindTo(currentConnection);
    }

    @Override //for getting the size of the list
    public int getItemCount() {
        return Connections.size();
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
        void bindTo(Connection currentConnection) {
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
            Intent i = new Intent(mContext.getApplicationContext(), PinDiagnostic.class);
            i.putExtra("myvehicle", myvehicle);
            i.putParcelableArrayListExtra("connections", myvehicle.getConnections());
            i.putExtra("loc", getAdapterPosition());
            i.putParcelableArrayListExtra("uniqueconnections", new ArrayList<>(Connections));
            mContext.startActivity(i);
        }
    }
}
