package com.project.contacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;

public class ContactAdapter extends PagingDataAdapter<Contact, ContactViewHolder> {
    private final OnContactClick onContactClick;

    public ContactAdapter(@NonNull DiffUtil.ItemCallback<Contact> diffCallback, OnContactClick onContactClick) {
        super(diffCallback);
        this.onContactClick = onContactClick;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
        return new ContactViewHolder(view, onContactClick);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        holder.bind(getItem(position));
    }
}
