package net.simplifiedcoding.navigationdrawerexample;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by divyanshjain on 30/11/17.
 */

public class NoButtonAdapter extends RecyclerView.Adapter<NoButtonAdapter.ViewHolder> {

    private List<ListItem> listItems;
    private Context context;

    public NoButtonAdapter(List<ListItem> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_no_button , parent , false);

        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final ListItem listItem = listItems.get(position);

        holder.nameApp.setText(listItem.getNameApp());
        holder.time.setText(listItem.getTime());
        holder.icon.setImageDrawable(listItem.getThumbnail());

    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView nameApp;
        public TextView time;
        public ImageView icon;

        public ViewHolder(View itemView) {
            super(itemView);

            nameApp = (TextView) itemView.findViewById(R.id.list_item_text);
            time = (TextView) itemView.findViewById(R.id.list_item_time);
            icon = (ImageView) itemView.findViewById(R.id.list_item_thumbnail);
        }
    }
}
