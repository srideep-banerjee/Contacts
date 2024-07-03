package com.project.contacts;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.Objects;

public class ContactComparator extends DiffUtil.ItemCallback<Contact> {
    @Override
    public boolean areItemsTheSame(@NonNull Contact oldItem, @NonNull Contact newItem) {
        return Objects.equals(oldItem.getContact_id(), newItem.getContact_id());
    }

    @Override
    public boolean areContentsTheSame(@NonNull Contact oldItem, @NonNull Contact newItem) {
        return oldItem.equals(newItem);
    }
}
