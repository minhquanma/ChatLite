package com.mmq.chatlite;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;

/**
 * Created by mmq on 05/10/2017.
 */

public class RoomAdapter extends ArrayAdapter<Rooms> {

    private Context context;
    private ArrayList<Rooms> list;
    private int resource;
    private ViewHolder viewHolder;

    // View lookup cache
    private static class ViewHolder {
        TextView name;
    }

    public RoomAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<Rooms> list) {
        super(context, resource, list);
        this.context = context;
        this.resource = resource;
        this.list = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // Get the data item for this position
        Rooms room = list.get(position);

        // Inflate
        convertView = LayoutInflater.from(context).inflate(resource, parent, false);

        // Mapping view
        TextView roomName = convertView.findViewById(R.id.txtViewRName);
        ImageView imageViewRoom = convertView.findViewById(R.id.imageViewRoom);
        EmojiTextView lastMessage = convertView.findViewById(R.id.txtViewRLastMsg);
        TextView time = convertView.findViewById(R.id.txtViewTime);

        if (URLUtil.isValidUrl(room.getAvatar())) {
            Picasso.with(getContext())
                    .load(room.getAvatar())
                    .into(imageViewRoom);
        }

        // Binding data
        roomName.setText(room.getName());

        if (room.isRead == false) {
            roomName.setTypeface(Typeface.DEFAULT_BOLD);
        } else if (room.isRead == true) {
            roomName.setTypeface(Typeface.DEFAULT);
        }

        lastMessage.setText(room.getLastMessage());
        time.setText(room.getSentTime());

        // Fill the view
        return convertView;
    }
}
