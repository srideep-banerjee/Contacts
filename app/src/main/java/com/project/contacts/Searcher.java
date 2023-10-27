package com.project.contacts;

import java.util.ArrayList;

public class Searcher {
    private ArrayList<Contact> data;
    private String key;

    public Searcher(ArrayList<Contact> data) {
        this.data = data;
    }

    private char getCharAt(int data_index,int string_index){
        String str=data.get(data_index).getName();
        if(str.length()-1<string_index)return '\0';
        return Character.toLowerCase(data.get(data_index).getName().charAt(string_index));
    }

    private int lower_bound(int begin,int end,int ind) {
        int lb=begin;
        int ub=end;
        while(lb<=ub) {
            int mid=(lb+ub)/2;
            if(getCharAt(mid,ind) == key.charAt(ind)) {
                if(mid-1 >= begin && getCharAt(mid-1,ind)==key.charAt(ind)) {
                    ub=mid-1;
                    continue;
                }
                return mid;
            }
            else if(getCharAt(mid,ind) < key.charAt(ind))
                lb=mid+1;
            else
                ub=mid-1;
        }
        return -1;
    }

    private int upper_bound(int begin,int end,int ind) {
        int lb=begin;
        int ub=end;
        while(lb<=ub) {
            int mid=(lb+ub)/2;
            if(getCharAt(mid,ind) == key.charAt(ind)) {
                if(mid+1 <= end && getCharAt(mid+1,ind)==key.charAt(ind)) {
                    lb=mid+1;
                    continue;
                }
                return mid;
            }
            else if(getCharAt(mid,ind) < key.charAt(ind))
                lb=mid+1;
            else
                ub=mid-1;
        }
        return -1;
    }
    public int[] search(String key){
        key=key.toLowerCase();
        this.data=data;
        this.key=key;
        int begin=0;
        int end=data.size()-1;
        for(int i=0;i<key.length();i++){
            begin=lower_bound(begin,end,i);
            if(begin==-1)return null;
            end=upper_bound(begin,end,i);
            if(end==-1) return null;
        }
        return new int[]{begin,end};

    }
}
