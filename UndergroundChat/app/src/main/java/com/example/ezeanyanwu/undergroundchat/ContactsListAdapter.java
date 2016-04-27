package com.example.ezeanyanwu.undergroundchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ezeanyanwu on 27/04/2016.
 */

public class ContactsListAdapter extends BaseAdapter {
    Context context;
    ArrayList<SingleContactListing> data;
    private static LayoutInflater inflater = null;

    public ContactsListAdapter(Context context, ArrayList<SingleContactListing> data)
    {
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null)
        {
            view = inflater.inflate(R.layout.row, null);
        }
        TextView header = (TextView) view.findViewById(R.id.header);
        TextView subheader = (TextView) view.findViewById(R.id.subheader);
        header.setText(data.get(position).contactName);
        subheader.setText(data.get(position).presence);
        return view;
    }

}
