package com.mmq.chatlite;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jon on 11/5/2017.
 */

public class ContactAdapter extends ArrayAdapter<Users> {

    private Context context;
    private ArrayList<Users> list;
    private int resource;

    public ContactAdapter(@NonNull Context context, int resource, @NonNull List<Users> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.list = (ArrayList<Users>) objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // Inflate
        convertView = LayoutInflater.from(context).inflate(resource, parent, false);

        // Get the data item for this position
        Users user = list.get(position);

        // Mapping view
        TextView txtViewContactName = convertView.findViewById(R.id.txtViewContactName);
        ImageView imageViewContact = convertView.findViewById(R.id.imageViewContact);

        // Binding data
        txtViewContactName.setText(user.getDisplayName());

        if (URLUtil.isValidUrl(user.getAvatar())) {
            Picasso.with(getContext())
                    .load(user.getAvatar())
                    .into(imageViewContact);
        }

        return convertView;
    }
}
