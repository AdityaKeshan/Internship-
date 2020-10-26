package com.work.mainactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;
import com.mukesh.OtpView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    EditText phone;
    OtpView code;
    CountryCodePicker ccp;
    TextView time;
    String codesent,cod;
    Button login,send;
    SharedPreferences shared;
    static boolean verified;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("users");

    static HashMap<String,Integer> cre;
    AlertDialog.Builder builder;
    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }
    public void logo(View v)
    {
        try {
            cod = code.getText().toString();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codesent, cod);
            signInWithPhoneAuthCredential(credential);
        }
        catch (Exception e)
        {
            Log.i("The error:",e.getMessage());
        }
    }
    public void clickStartVerification(View v){
        String z=phone.getText().toString();
        if(z==null)
        {
            Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show();
        }
        else
        {
            if(z.charAt(0)=='+' ) {
                if(z.length()==13 || z.length()==16) {
                    if (time.getVisibility() != View.VISIBLE) {
                        getOtp(z);
                        time.setVisibility(View.VISIBLE);
                        CountDownTimer countDownTimer = new CountDownTimer(1000, 1000) {

                            @Override
                            public void onTick(long millis) {

                                time.setText("Enter OTP manually in " + millis / 1000 + " seconds");

                            }

                            @Override
                            public void onFinish() {
                                code.setVisibility(View.VISIBLE);
                                login.setVisibility(View.VISIBLE);
                                time.setVisibility(View.INVISIBLE);
                            }
                        }.start();
                    } else {
                        Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            else
            {   if(z.length()==10 || z.length()==13) {
                String m = ccp.getSelectedCountryCode();
                Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
                if (time.getVisibility() != View.VISIBLE) {
                    getOtp("+91" + z);
                    time.setVisibility(View.VISIBLE);
                    CountDownTimer countDownTimer = new CountDownTimer(1000, 1000) {

                        @Override
                        public void onTick(long millis) {

                            time.setText("Enter OTP manually in " + millis / 1000 + " seconds");

                        }

                        @Override
                        public void onFinish() {
                            code.setVisibility(View.VISIBLE);
                            login.setVisibility(View.VISIBLE);
                            time.setVisibility(View.INVISIBLE);
                        }
                    }.start();
                } else {
                    Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show();
                }
            }
            }

        }
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                            start();

                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid

                                Toast.makeText(MainActivity.this, "Login UnSuccessful", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
    private void getOtp(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }
    void maker()
    {
        builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet!").setMessage("Connect to Wifi or Mobile data").setCancelable(false).setPositiveButton("DONE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!isConnected())
                {
                    maker();

                }
                else{
                    creator();
                }

            }
        });
        builder.create();
        builder.show();
    }
    void creator()
    {
        try {
            mAuth = FirebaseAuth.getInstance();
            shared = getApplicationContext().getSharedPreferences("com.work.mainactivity", Context.MODE_PRIVATE);
            phone = findViewById(R.id.phone1);
            code = findViewById(R.id.otp_view);
            time = findViewById(R.id.time);
            login = findViewById(R.id.login);
            try {
                verified = shared.getBoolean("V", false);
            } catch (Exception e) {
                Log.i("The error in saving is:", e.getMessage());
            }
            if (verified) {
                start();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error in creator", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cre=new HashMap<>();
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        if (isConnected() == false) {
            maker();
        }
        else
        {
            creator();
        }


        myRef.setValue("Hello, World!");
    }
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            Toast.makeText(MainActivity.this, "Verification successful", Toast.LENGTH_SHORT).show();

            start();

        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            Toast.makeText(MainActivity.this, "Code has been sent", Toast.LENGTH_SHORT).show();
            Log.i("The s value:", s);
            codesent = s;
        }
    };
    void start()
    {
        Intent I=new Intent(MainActivity.this,RegisterActivity.class);
        if(shared.getBoolean("V",false)!=true) {
            shared.edit().putString("ph", phone.getText().toString()).commit();
        }
        startActivity(I);
    }
}