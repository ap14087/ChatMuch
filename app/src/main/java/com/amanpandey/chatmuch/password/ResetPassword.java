package com.amanpandey.chatmuch.password;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import com.amanpandey.chatmuch.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;



public class ResetPassword extends AppCompatActivity {

    private TextInputEditText etEMail;
    private TextView tvMessage;
    private LinearLayout llResetPassword,llMessage;
    private Button btnRetry;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etEMail = findViewById(R.id.etEmail);
        tvMessage = findViewById(R.id.tvMessage);
        llMessage = findViewById(R.id.llMessage);
        btnRetry = findViewById(R.id.btnRetry);
        progressBar = findViewById(R.id.progressBar);
        llResetPassword = findViewById(R.id.llResetPassword);
    }

    public void btnResetPasswordClick(View view){
        String email = etEMail.getText().toString().trim();

        if(email.equals("")){
            etEMail.setError("Enter email");
        }
        else
        {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressBar.setVisibility(View.GONE);
                    llResetPassword.setVisibility(android.view.View.GONE);
                    llMessage.setVisibility(android.view.View.VISIBLE);

                    if(task.isSuccessful()){
                       tvMessage.setText("Reset Password instructions has been sent on email: "+email);
                        new CountDownTimer(60000,1000){

                            @Override
                            public void onTick(long l) {
                                btnRetry.setText("Resend in : " + String.valueOf(l/1000));
                                btnRetry.setOnClickListener(null);
                            }

                            @Override
                            public void onFinish() {
                                btnRetry.setText("Retry");

                                btnRetry.setOnClickListener(new android.view.View.OnClickListener() {
                                    @Override
                                    public void onClick(android.view.View view) {
                                        llResetPassword.setVisibility(android.view.View.VISIBLE);
                                        llMessage.setVisibility(android.view.View.GONE);
                                    }
                                });
                            }
                        }.start();
                    }else{
                        tvMessage.setText("Failed to sent email : "+task.getException());

                        btnRetry.setOnClickListener(new android.view.View.OnClickListener() {
                            @Override
                            public void onClick(android.view.View view) {
                                llResetPassword.setVisibility(android.view.View.VISIBLE);
                                llMessage.setVisibility(android.view.View.GONE);
                            }
                        });
                    }
                }
            });
        }
    }

    public void btnCloseClick(View view){
        finish();
    }
}