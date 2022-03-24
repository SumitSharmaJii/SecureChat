package com.ss.securechat.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.hbb20.CountryCodePicker;
import com.ss.securechat.databinding.ActivityGetMobNumberBinding;

public class GetMobNumber extends AppCompatActivity {

    ActivityGetMobNumberBinding binding;
    FirebaseAuth auth;
    CountryCodePicker ccp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGetMobNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ccp = binding.ccp;

        //direct open main activity
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(GetMobNumber.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        getSupportActionBar().hide();

        //buttonclicked
        binding.buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!binding.inputMobNo.getText().toString().trim().isEmpty()) {
                    ccp.registerCarrierNumberEditText(binding.inputMobNo);
                    String phnNoWithCountryCode = ccp.getFullNumberWithPlus().trim();
                    Intent intent = new Intent(getApplicationContext(), OtpVerification.class);
                    intent.putExtra("phnNo", phnNoWithCountryCode);
                    startActivity(intent);

                } else {
                    Toast.makeText(GetMobNumber.this, "Phone number cannot be left blank.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}