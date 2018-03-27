package domain.appdevelopment.derek.instantdial;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private static final int CALL_PHONE_PERMISSION = 0;

    private Button messBt;
    private Button callBt;
    private EditText input;


    private Intent inte;

    private Intent in;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messBt = findViewById(R.id.heraldButton);
        callBt = findViewById(R.id.callButton);
        input = findViewById(R.id.phone_number);
        messBt.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View view)
            {
                inte = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+ input.getText()));
                inte.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(inte);
            }
        });
        callBt.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View view)
            {
                in = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+input.getText()));
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CALL_PHONE},CALL_PHONE_PERMISSION);
                else
                    startActivity(in);
            }
        });
//        inte = new Intent(Intent.ACTION_CALL,Uri.parse("tel:4124992655"));
//        inte.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
            }
        }
    }
}
