package com.example.insight.activity.userActivities.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.insight.R;
import com.example.insight.entity.Exchange;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.List;

public class ExchangeRecyclerViewAdapter extends RecyclerView.Adapter<ExchangeViewHolder> {
    private List<Exchange> exchanges = new ArrayList<>();

    public void setExchanges(List<Exchange> list){
        exchanges.clear();
        exchanges.addAll(list);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExchangeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        return new ExchangeViewHolder(inflater.inflate(R.layout.exchange_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ExchangeViewHolder holder, int position) {
        try {
            holder.bind(exchanges.get(position));
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return exchanges.size();
    }
}
