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

import androidx.recyclerview.widget.RecyclerView;

import com.Diagnostic.Spudnik.CustomObjects.Pin;
import com.Diagnostic.Spudnik.CustomObjects.Vehicle;

import java.util.ArrayList;

/**
 * Recyclerview adapter for connection objects.
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @since dev 1.0.0
 */
class ConnectionAdapterHorizontal extends RecyclerView.Adapter<ConnectionAdapterHorizontal.ViewHolder> {

    private ArrayList<Pin> pins;
    private Context mContext;
    private Vehicle myvehicle;

    ConnectionAdapterHorizontal(Context context, ArrayList<Pin> pins, Vehicle vehicle) {
        this.pins = pins;
        this.mContext = context;
        this.myvehicle = vehicle;
    }

    @Override
    public ConnectionAdapterHorizontal.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item_horizontal, parent, false));
    }

    @Override
    public void onBindViewHolder(ConnectionAdapterHorizontal.ViewHolder holder, int position) {
        Pin currentPin = pins.get(position);
        holder.bindTo(currentPin);
    }

    @Override
    public int getItemCount() {
        return pins.size();
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
        void bindTo(Pin currentPin) {
            mTitleText.setText("Pin: " + currentPin.getS4());
            String temp = currentPin.getName();
            String s1 = temp.substring(0, 1).toUpperCase();
            mInfoText.setText(s1 + temp.substring(1));
            voltage.setText("6.6 mA");

        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(mContext, PinLocation.class);
            i.putExtra("myvehicle", myvehicle);
            i.putParcelableArrayListExtra("connections", myvehicle.getPins());
            i.putExtra("myConnection", pins.get(getAdapterPosition()));
            mContext.startActivity(i);
        }
    }
}
