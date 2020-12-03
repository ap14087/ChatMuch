package com.amanpandey.chatmuch.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.amanpandey.chatmuch.MainActivity;
import com.amanpandey.chatmuch.Message;
import com.amanpandey.chatmuch.R;
import com.amanpandey.chatmuch.common.Util;
import com.amanpandey.chatmuch.password.ResetPassword;
import com.amanpandey.chatmuch.signUp.SignUp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class Login extends AppCompatActivity {

    private TextInputEditText etEmail,etPassword;
    private String email,password;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        progressBar = findViewById(R.id.progressBar);

    }

    public void tvSignUpClick(View v){
        startActivity(new Intent(getApplicationContext(), SignUp.class));
    }

    public void btnLoginClick(View v){
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();

        if(email.equals("")){
            etEmail.setError("Enter email");
        }else if(password.equals("")){
            etPassword.setError("Enter Password");
        }else {
            if (Util.connectionAvailable(this)) {
                progressBar.setVisibility(View.VISIBLE);

                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.INVISIBLE);
                        if (task.isSuccessful()) {

                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();

                        } else {
                            Toast.makeText(Login.this, "Login Failed" + task.getException()
                                    , Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else
                {
                startActivity(new Intent(Login.this, Message.class));
            }
        }
    }

    public void tvResetPasswordClick(View view){
        startActivity(new Intent(getApplicationContext(), ResetPassword.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if(firebaseUser != null){

            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    Util.updateDeviceToken(Login.this, instanceIdResult.getToken() );
                }
            });


            startActivity(new Intent(Login.this,MainActivity.class));
            finish();
        }
    }
}