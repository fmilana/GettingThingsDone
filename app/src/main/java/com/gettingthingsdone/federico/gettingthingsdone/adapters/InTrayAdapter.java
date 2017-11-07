package com.gettingthingsdone.federico.gettingthingsdone.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gettingthingsdone.federico.gettingthingsdone.InTrayItem;
import com.gettingthingsdone.federico.gettingthingsdone.R;

import java.util.List;

/**
 * Created by Federico on 07-Nov-17.
 */

public class InTrayAdapter extends RecyclerView.Adapter<InTrayAdapter.ViewHolder> {

    private List<InTrayItem> items;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView itemTextView;

        public ViewHolder(View cardView) {
            super(cardView);

            itemTextView = (TextView) cardView.findViewById(R.id.in_tray_text_view);
        }
    }

    public InTrayAdapter(List<InTrayItem> items) {
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_in_tray_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(cardView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        InTrayItem inTrayItem = items.get(position);
        holder.itemTextView.setText(inTrayItem.getText());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
