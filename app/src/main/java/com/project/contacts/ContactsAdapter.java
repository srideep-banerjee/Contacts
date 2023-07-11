package com.project.contacts;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    List<Contact> contact_data;
    int padding;

    public ContactsAdapter(List<Contact> contact_data, Context context) {
        this.contact_data = contact_data;
        padding = (int) (12 * context.getResources().getDisplayMetrics().density);
    }

    public void changeData(List<Contact> contact_data) {
        this.contact_data = contact_data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact con = contact_data.get(position);
        holder.contact_name.setText(con.getName());
        holder.contact_number.setText(con.getPh_no());
        holder.array_position.setText(position + "");
        if (con.getPfp_uri() != null) {
            holder.contact_pfp.setPadding(0, 0, 0, 0);
            holder.contact_pfp.setImageURI(Uri.parse(con.getPfp_uri()));
        } else {
            holder.contact_pfp.setPadding(padding, padding, padding, padding);
            holder.contact_pfp.setImageResource(R.drawable.profile_picture);
        }
    }

    @Override
    public int getItemCount() {
        return contact_data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView contact_pfp;
        TextView contact_name, contact_number, array_position;

        public ViewHolder(View itemView) {
            super(itemView);
            contact_pfp = itemView.findViewById(R.id.contact_pfp);
            contact_name = itemView.findViewById(R.id.contact_name);
            contact_number = itemView.findViewById(R.id.contact_number);
            array_position = itemView.findViewById(R.id.array_position);

        }
    }


}
