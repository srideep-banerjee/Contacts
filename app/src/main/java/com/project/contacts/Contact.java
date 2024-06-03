package com.project.contacts;

public class Contact {

    private final String name;
    private final String ph_no;
    private final String pfp_uri;
    private final String contact_id;

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
