package net.simplifiedcoding.navigationdrawerexample;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by divyanshjain on 01/12/17.
 */

public class RemoveAdapter extends RecyclerView.Adapter<RemoveAdapter.ViewHolder> {

    private List<ListItem> listItems;
    private Context context;

    public RemoveAdapter(List<ListItem> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
    }

    @Override
    public RemoveAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_remove , parent , false);

        return new RemoveAdapter.ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(final RemoveAdapter.ViewHolder holder, int position) {

        final ListItem listItem = listItems.get(position);

        holder.nameApp.setText(listItem.getNameApp());
        //holder.time.setText(listItem.getTime());
        holder.icon.setImageDrawable(listItem.getThumbnail());
        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(context, "App Unblocked", Toast.LENGTH_SHORT).show();
                Toast.makeText(context, listItem.getPackageName(), Toast.LENGTH_SHORT).show();

                String id = "NULL";
                Database entry = new Database(context);

                for (Pair<String, String> idAndPhoneNo : entry.open().getIdAndValues()) {

                    if (idAndPhoneNo.second.equals(listItem.getPackageName())) {

                        id = idAndPhoneNo.first;
                    }

                }

                entry.deleteEntry(Integer.parseInt(id));
                entry.close();

            }
        });

    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView nameApp;
        //public TextView time;
        public ImageView icon;
        public Button add;

        public ViewHolder(View itemView) {
            super(itemView);

            nameApp = (TextView) itemView.findViewById(R.id.list_item_text);
            //time = (TextView) itemView.findViewById(R.id.list_item_time);
            icon = (ImageView) itemView.findViewById(R.id.list_item_thumbnail);
            add = (Button) itemView.findViewById(R.id.list_item_button);
        }
    }
}
