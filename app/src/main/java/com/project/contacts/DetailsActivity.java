package com.project.contacts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DetailsActivity extends AppCompatActivity {

    Contact c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Intent i=getIntent();
        c=new Contact(i.getStringExtra("name"),i.getStringExtra("number"),i.getStringExtra("uri"),i.getStringExtra("cid"));
        TextView name=findViewById(R.id.name);
        name.setText(c.getName());
        TextView number=findViewById(R.id.number);
        number.setText(c.getPh_no());
        if(c.getPfp_uri()!=null){
            ImageView pfp=findViewById(R.id.pfp);
            pfp.setImageURI(Uri.parse(c.getPfp_uri()));
        }
        ImageView back=findViewById(R.id.back);
        back.setOnClickListener((View)->finish());
        ImageView copy=findViewById(R.id.copy);
        copy.setOnClickListener((View v)->{
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("contact number", c.getPh_no());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(),"Phone Number Copied", Toast.LENGTH_SHORT).show();
        });
        ImageView call=findViewById(R.id.call);
        call.setOnClickListener((View v)->{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 20);
            else {
                Intent caller=new Intent(Intent.ACTION_CALL);
                caller.setData(Uri.parse("tel:"+c.getPh_no()));
                startActivity(caller);
            }
        });
        TextView edit=findViewById(R.id.edit);
        edit.setOnClickListener((View v)->{
            Intent editor=new Intent(Intent.ACTION_EDIT);
            Uri content_uri= ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,Long.parseLong(c.getContact_id()));
            editor.setData(content_uri);
            editor.putExtra("finishActivityOnSaveCompleted",true);
            startActivity(editor);
            finish();
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==20){
            if(permissions.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                Intent caller = new Intent(Intent.ACTION_CALL);
                caller.setData(Uri.parse("tel:" + c.getPh_no()));
                startActivity(caller);
            }
            else
                Toast.makeText(getApplicationContext(),"Permission denied, can't make phone call", Toast.LENGTH_LONG).show();
        }

    }
}