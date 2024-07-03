package com.project.contacts;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ContactViewHolder extends RecyclerView.ViewHolder {
    ImageView contact_pfp;
    TextView contact_name, contact_number, array_position;
    private final int padding;
    private Contact currentContact = null;

    public ContactViewHolder(View itemView, OnContactClick onContactClick) {
        super(itemView);
        padding = (int) (12 * itemView
                .getContext()
                .getResources()
                .getDisplayMetrics()
                .density);
        contact_pfp = itemView.findViewById(R.id.contact_pfp);
        contact_name = itemView.findViewById(R.id.contact_name);
        contact_number = itemView.findViewById(R.id.contact_number);
        array_position = itemView.findViewById(R.id.array_position);
        itemView.setOnClickListener((view)-> {
            if (currentContact != null) onContactClick.onClick(currentContact);
        });
    }

    public void bind(Contact contact) {
        currentContact = contact;
        contact_name.setText(contact != null? contact.getName() : "Placeholder");
        contact_number.setText(contact != null? contact.getPh_no() : "Placeholder");
        if (contact != null && contact.getPfp_uri() != null) {
            contact_pfp.setPadding(0, 0, 0, 0);
            contact_pfp.setImageURI(Uri.parse(contact.getPfp_uri()));
        } else {
            contact_pfp.setPadding(padding, padding, padding, padding);
            contact_pfp.setImageResource(R.drawable.profile_picture);
        }
    }
}