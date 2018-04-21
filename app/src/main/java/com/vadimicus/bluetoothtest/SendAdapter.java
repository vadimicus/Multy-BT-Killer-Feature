package com.vadimicus.bluetoothtest;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vadimicus.bluetoothtest.utils.OnReceiverClick;
import com.vadimicus.bluetoothtest.utils.Receiver;

import java.util.ArrayList;

/**
 * Created by vadimicus on 05.04.2018.
 */

public class SendAdapter extends RecyclerView.Adapter<SendAdapter.ViewHolder> {

    public static ArrayList<Receiver> mDataset;
    public static OnReceiverClick onReceiverClick;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView, tvAmount;
        public ViewHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.tvItem);
            tvAmount = (TextView) view.findViewById(R.id.tvAmount);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = getAdapterPosition();
                    if (onReceiverClick!= null){
                        onReceiverClick.clicked(mDataset.get(position));
                    }
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SendAdapter(ArrayList<Receiver> myDataset, OnReceiverClick onReceiverClick) {
        mDataset = myDataset;
        this.onReceiverClick = onReceiverClick;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SendAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        // set the view's size, margins, paddings and layout parameters



        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(mDataset.get(position).getUserCode());
        if (mDataset.get(position).getAmount() != 0) {
            holder.tvAmount.setText(String.valueOf(mDataset.get(position).getAmount()));
        }else{
            holder.tvAmount.setText("No Amount");
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
