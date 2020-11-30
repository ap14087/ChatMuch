package com.amanpandey.chatmuch.password;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.amanpandey.chatmuch.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class changePassword extends AppCompatActivity {

    private View progressBar;
    private TextInputEditText etPassword, etConfirmPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etPassword = findViewById(R.id.etPassword);
        progressBar = findViewById(R.id.progressBar);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
    }

    public void btnChangePasswordClick(View view){
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if(password.equals("")){
            etPassword.setError("Enter Password");
        }else if(confirmPassword.equals("")){
            etConfirmPassword.setError("Enter passsword to confirm");
        }else if(!password.equals(confirmPassword)){
            etConfirmPassword.setError("Password mismatch");
        }else{
            progressBar.setVisibility(View.VISIBLE);
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

            if(firebaseUser != null){
                firebaseUser.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()){
                            Toast.makeText(changePassword.this, "password changes successfully", Toast.LENGTH_SHORT).show();
                            finish();

                        }else{
                            Toast.makeText(changePassword.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
}