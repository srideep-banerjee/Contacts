package com.project.contacts;

import android.graphics.Bitmap;

public class Contact {

    private String name="";
    private String ph_no="1234567890";
    private String pfp_uri=null;
    private String contact_id=null;

    public String getName() {
        return name;
    }

    public String getPh_no() {
        return ph_no;
    }

    public String getPfp_uri() {
        return pfp_uri;
    }

    public String getContact_id() {
        return contact_id;
    }

    public Contact(String name, String ph_no, String pfp_uri, String contact_id){
        this.name=name;
        this.ph_no=ph_no;
        this.pfp_uri=pfp_uri;
        this.contact_id=contact_id;
    }
}
