package domain.appdevelopment.derek.instantdial;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{

    private static final int CALL_PHONE_PERMISSION = 0;
    private static final int READ_CONTACT_PERMISSION = 1;

    private ContactLoader cloader;
    private Button messBt;
    private Button callBt;
    private AutoCompleteTextView input;
    /**
     * The names of persons suggested by the autocomplete according to the given user input name.
     */
    private static final List<String> AUTO_NAME = new LinkedList<>();
    /**
     * All persons matched on present user input..
     */
    private static final Map<String,Person> PERS = new HashMap<>();
    /**
     * The array adapter for the present autocomplete conditions.
     */
    private static ArrayAdapter<String> adp;




    private Intent inte;

    private Intent in;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        input = findViewById(R.id.contactView);
        adp = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item);
        input.setAdapter(adp);
        cloader = new ContactLoader();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_CONTACTS},READ_CONTACT_PERMISSION);
        else
            cloader.execute();
 //       messBt = findViewById(R.id.heraldButton);
        callBt = findViewById(R.id.callButton);


//        messBt.setOnClickListener(new Button.OnClickListener()
//        {
//            public void onClick(View view)
//            {
//                inte = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+ input.getText()));
//                inte.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(inte);
//            }
//        });
//        callBt.setOnClickListener(new Button.OnClickListener()
//        {
//            public void onClick(View view)
//            {
//                in = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+input.getText()));
//                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED)
//                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CALL_PHONE},CALL_PHONE_PERMISSION);
//                else
//                    startActivity(in);
//            }
//        });
    }

    public void onRequestPermissionsResult(int requestCode, String[] perms, int[] results)
    {
        switch(requestCode)
        {
            case CALL_PHONE_PERMISSION:
            {
                if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)
                {
                    startActivity(in);
                }
                break;
            }
            case READ_CONTACT_PERMISSION:
            {
                if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)
                    cloader.execute();
                break;
            }
        }
    }

    /**
     * Sift those persons with the name in accord with the one given in the argument,
     * and represent them in the autocomplete.
     * @param name The name of the person to be queried.
     */
    protected void fillPersons(String name)
    {
        ContentResolver res = getContentResolver();
        //Query for persons with names containing this name.
        Cursor cur = res.query(ContactsContract.Contacts.CONTENT_URI,null, ContactsContract.Contacts.DISPLAY_NAME+" like ?",new String[]{"%"+name+"%"},null);
        while(cur.moveToNext())
        {
            String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
            String dname = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            //Query the dials and emails of those persons previously filtered.
            Cursor dialCur = res.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" = ?",new String[]{id},null);
            Cursor emailCur = res.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,null, ContactsContract.CommonDataKinds.Email.CONTACT_ID+" = ?",new String[]{id},null);
            //Create a person.
            Person per = new Person();
            per.name = dname;
            while(dialCur.moveToNext())
                per.dial.add(dialCur.getString(dialCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
            while(emailCur.moveToNext())
                per.email.add(emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)));
            PERS.put(per.name,per);
            Log.e("See",per.toString());
        }
    }

    /**
     * Get and fill all the contacts into the persons.
     */
    protected void fillAllPersons()
    {
        ContentResolver res = getContentResolver();
        //Query for persons with names containing this name.
        Cursor cur = res.query(ContactsContract.Contacts.CONTENT_URI,null, null,null,null);
        while(cur.moveToNext())
        {
            String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
            String dname = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            //Query the dials and emails of those persons previously filtered.
            Cursor dialCur = res.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" = ?",new String[]{id},null);
            Cursor emailCur = res.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,null, ContactsContract.CommonDataKinds.Email.CONTACT_ID+" = ?",new String[]{id},null);
            //Create a person.
            Person per = new Person();
            per.name = dname;
            while(dialCur.moveToNext())
                per.dial.add(dialCur.getString(dialCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
            while(emailCur.moveToNext())
                per.email.add(emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)));
            PERS.put(per.name,per);
        }
    }

    /**
     * A simple representation of a person along wit his/her dials and emails.
     */
    private static class Person
    {
        /**
         * The display name of a contact stored in the android system.
         */
        String name;
        /**
         * The dials and emails of this person.
         */
        List<String> dial,email;

        {
            dial = new LinkedList<>();
            email = new LinkedList<>();
        }

        Person()
        {

        }

        public String toString()
        {
            return String.format("%s,%s,%s",name,dial,email);
        }
    }

    private class ContactLoader extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected Void doInBackground(Void... voids)
        {
            fillAllPersons();
            adp.addAll(PERS.keySet());
            return null;
        }

    }

}
