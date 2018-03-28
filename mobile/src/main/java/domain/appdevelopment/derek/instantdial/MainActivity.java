package domain.appdevelopment.derek.instantdial;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{

    private static final int CALL_PHONE_PERMISSION = 0;
    private static final int READ_CONTACT_PERMISSION = 1;

    /**
     * Loader of the other thread of importing all contacts.
     */
    private ContactLoader cloader;
    /**
     * Button for messaging.
     */
    private Button messBt;
    /**
     * Button for calling.
     */
    private Button callBt;
    /**
     *
     */
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
    /**
     * The person identified as a contact.
     */
    private Person per;
    /**
     * The reference to the pop-up menu.
     */
    private Menu pop_up;
    /**
     * The state of the pop_up, whether for dial or email. A false is for dial, a true for email.
     */
    private boolean pop;
    /**
     * The intent for invoking the call.
     */
    private Intent callIntent;
    /**
     * The intent for sending email to a specific address.
     */
    private Intent emailIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Setting up the user input autocomplete setting.
        input = findViewById(R.id.contactView);
        adp = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item);
        input.setAdapter(adp);
        cloader = new ContactLoader();
        //Check the permission and load the contacts.
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_CONTACTS},READ_CONTACT_PERMISSION);
        else
            cloader.execute();

        //Load the call button.
        callBt = findViewById(R.id.callButton);
        callBt.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View view)
            {
                if(!PERS.containsKey(input.getText().toString()))
                {
                    Toast.makeText(MainActivity.this,"No Such Contact in System",Toast.LENGTH_SHORT).show();
                    return;
                }
                per = PERS.get(input.getText().toString());
                if(per.dial.size()==0)
                {
                    Toast.makeText(MainActivity.this,"No Plausible Dial for Such Person",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CALL_PHONE},CALL_PHONE_PERMISSION);
                else
                {
                    pop = false;
                    View vi = findViewById(R.id.pos);
                    registerForContextMenu(vi);
                    openContextMenu(findViewById(R.id.pos));
                    unregisterForContextMenu(vi);
                }
            }
        });

        //Load the messenger button.
        messBt = findViewById(R.id.heraldButton);
        messBt.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View view)
            {
                if(!PERS.containsKey(input.getText().toString()))
                {
                    Toast.makeText(MainActivity.this,"No Such Contact in System",Toast.LENGTH_SHORT).show();
                    return;
                }
                per = PERS.get(input.getText().toString());
                if(per.email.size()==0)
                {
                    Toast.makeText(MainActivity.this,"No Plausible Email for Such Person",Toast.LENGTH_SHORT).show();
                    return;
                }
                pop = true;
                View vi = findViewById(R.id.pos);
                registerForContextMenu(vi);
                openContextMenu(findViewById(R.id.pos));
                unregisterForContextMenu(vi);
            }
        });
    }

    /**
     * Create a floating context menu for either the pop-up window for dial or email selection. A reference of this menu is captured in the pop_up variable.
     * @param menu The context menu representing the options of dials or emails of this person.
     * @param view The text view this menu is associated with.
     * @param info Standard information regarding this menu.
     */
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo info)
    {
        super.onCreateContextMenu(menu,view,info);
        MenuInflater mf = getMenuInflater();
        mf.inflate(R.menu.pop_menu,menu);
        pop_up = menu;
        if(!pop)
            for(String dial: per.dial)
                pop_up.add(dial);
        else
            for(String email: per.email)
                pop_up.add(email);
    }

    /**
     * Select an item in the pop-up menu of either the possible dials or emails of a contact.
     * @param item The dial or email of the person selected by the user.
     * @return The success of such a selection.
     */
    public boolean onContextItemSelected(MenuItem item)
    {
        if(!pop)
        {
            callIntent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+item.getTitle()));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(callIntent);
        }
        else
        {
            emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{item.getTitle().toString()});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "subject");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "message");
            emailIntent.setType("message/rfc822");
            startActivity(Intent.createChooser(emailIntent, "Choose an Email client :"));
        }
        pop_up.clear();
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String[] perms, int[] results)
    {
        switch(requestCode)
        {
            case CALL_PHONE_PERMISSION:
            {
                if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)
                {
                    startActivity(callIntent);
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
