package com.ss.securechat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.ss.securechat.databinding.ActivityOtpVerificationBinding;

import java.util.concurrent.TimeUnit;

public class OtpVerification extends AppCompatActivity {

    ActivityOtpVerificationBinding binding;
    FirebaseAuth auth;
    String verifyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();

        String phnNo = getIntent().getStringExtra("phnNo");
        binding.enteredMobNo.setText("Verify " + phnNo);



        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phnNo)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(OtpVerification.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(OtpVerification.this, "Check your internet connection.", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken ){
                        super.onCodeSent(verificationId, forceResendingToken);
                        Toast.makeText(OtpVerification.this, "OTP Sent.", Toast.LENGTH_SHORT).show();
                        verifyId=verificationId;


                    }


                }).build();

        PhoneAuthProvider.verifyPhoneNumber(options);

        binding.buttonverifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!binding.otp1.getText().toString().isEmpty() && !binding.otp2.getText().toString().isEmpty() && !binding.otp3.getText().toString().isEmpty() && !binding.otp4.getText().toString().isEmpty() && !binding.otp5.getText().toString().isEmpty() && !binding.otp6.getText().toString().isEmpty())
                {
                    try {
                        String otp = binding.otp1.getText().toString() + binding.otp2.getText().toString() + binding.otp3.getText().toString() + binding.otp4.getText().toString() + binding.otp5.getText().toString() + binding.otp6.getText().toString();

                        binding.pBarOTP.setVisibility(View.VISIBLE);
                        binding.buttonverifyOTP.setVisibility(View.INVISIBLE);
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verifyId, otp);
                        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                binding.pBarOTP.setVisibility(View.GONE);
                                binding.buttonverifyOTP.setVisibility(View.VISIBLE);
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(OtpVerification.this, UserProfileActivity.class);
                                    startActivity(intent);
                                    finishAffinity();
                                } else
                                    Toast.makeText(OtpVerification.this, "OTP is incorrect.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e){
                        binding.pBarOTP.setVisibility(View.GONE);
                        binding.buttonverifyOTP.setVisibility(View.VISIBLE);
                        Toast.makeText(OtpVerification.this,"Check Your internet connection.",Toast.LENGTH_SHORT).show();

                    }
                }

                else
                    Toast.makeText(OtpVerification.this, "Enter all 6 digits",Toast.LENGTH_SHORT).show();
            }
        });
        moveToNextDigitOTP();
        binding.resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phnNo)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(OtpVerification.this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {

                                Toast.makeText(OtpVerification.this, "Check internet connection.", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken ){
                                super.onCodeSent(verificationId, forceResendingToken);

                                verifyId=verificationId;
                                Toast.makeText(OtpVerification.this, "OTP resent successfully.",Toast.LENGTH_SHORT).show();
                            }

                        }).build();

                PhoneAuthProvider.verifyPhoneNumber(options);
            }
        });
    }

    // automatic next shift while entering otp
    private void moveToNextDigitOTP(){
        binding.otp1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty())
                    binding.otp2.requestFocus();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.otp2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty())
                    binding.otp3.requestFocus();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.otp3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty())
                    binding.otp4.requestFocus();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.otp4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty())
                    binding.otp5.requestFocus();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.otp5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty())
                    binding.otp6.requestFocus();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }
}