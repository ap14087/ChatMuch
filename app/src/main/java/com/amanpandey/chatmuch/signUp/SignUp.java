package com.amanpandey.chatmuch.signUp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.amanpandey.chatmuch.MainActivity;
import com.amanpandey.chatmuch.R;
import com.amanpandey.chatmuch.common.NodeNames;
import com.amanpandey.chatmuch.login.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    private TextInputEditText etEmail,etName,etPassword,etConfirmPassword;
    private String email,name,password,confirmPassword;
    public  ImageView ivProfile;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference fileStorage;
    private Uri localFileUri,serverFileUri;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etEmail = findViewById(R.id.etEmail);
        etName = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        ivProfile = findViewById(R.id.ivProfile);
        progressBar = findViewById(R.id.progressBar);
        fileStorage = FirebaseStorage.getInstance().getReference();
    }

    public void pickImage(View v) {

        //Checking if the user has given permission to access external storage.
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 101);

        }
        else
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},102);

        }
    }

    //to handle select activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101){
            Log.i("REQUEST CODE IS 101 : ","We are inside on activity result");
            if(resultCode == RESULT_OK){
                Log.i("REQUEST CODE IS OK : ","We are inside on activity result");
                localFileUri = data.getData();
                ivProfile.setImageURI(localFileUri);
            }
        }
    }

    //To handle permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 102){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 101);
            }
            else {
                Toast.makeText(this, "Access permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateNameAndPhoto(){
        String strFileName = firebaseUser.getUid() + ".jpg";

        final StorageReference fileRef = fileStorage.child("images/"+ strFileName);

        progressBar.setVisibility(View.VISIBLE);
        fileRef.putFile(localFileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                progressBar.setVisibility(View.INVISIBLE);
                if(task.isSuccessful()){
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                              serverFileUri = uri;
                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(etName.getText().toString().trim())
                                    .setPhotoUri(serverFileUri)
                                    .build();

                            firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        //get current user id
                                        String userID = firebaseUser.getUid();
                                        databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);

                                        HashMap<String,String> hashMap = new HashMap<>();

                                        hashMap.put(NodeNames.NAME,etName.getText().toString().trim());
                                        hashMap.put(NodeNames.EMAIL,etEmail.getText().toString().trim());
                                        hashMap.put(NodeNames.ONLINE,"true");
                                        hashMap.put(NodeNames.PHOTO,serverFileUri.getPath());

                                        databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(getApplicationContext(), "User created successfully", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(getApplicationContext(), Login.class));
                                            }
                                        });
                                    }else{
                                        Toast.makeText(SignUp.this, "Failed to update profile" + task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });

    }

    // if user not selected profile photo
    private void updateOnlyName(){
        progressBar.setVisibility(View.VISIBLE);
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(etName.getText().toString().trim())
                .build();

        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBar.setVisibility(View.INVISIBLE);
                if(task.isSuccessful()){
                         //get current user id
                         String userID = firebaseUser.getUid();
                         databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);

                         HashMap<String,String> hashMap = new HashMap<>();

                         hashMap.put(NodeNames.NAME,etName.getText().toString().trim());
                         hashMap.put(NodeNames.EMAIL,etEmail.getText().toString().trim());
                         hashMap.put(NodeNames.ONLINE,"true");
                         hashMap.put(NodeNames.PHOTO,"");

                         progressBar.setVisibility(View.VISIBLE);
                         databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                             @Override
                             public void onComplete(@NonNull Task<Void> task) {

                                 progressBar.setVisibility(View.INVISIBLE);
                                 Toast.makeText(SignUp.this, "User created successfully", Toast.LENGTH_SHORT).show();
                                 startActivity(new Intent(getApplicationContext(), Login.class));
                             }
                         });
                     }else{
                         Toast.makeText(SignUp.this, "Failed to update profile" + task.getException(), Toast.LENGTH_SHORT).show();
                     }
            }
        });
    }

    public void btnSignupClick(View v){
        email = etEmail.getText().toString().trim();
        name = etName.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        confirmPassword = etConfirmPassword.getText().toString().trim();

        if(email.equals("")){
            etEmail.setError("Enter email");
        }else if(name.equals("")){
            etName.setError("Enter name");
        }else if(password.equals("")){
            etPassword.setError("Enter password");
        }else if(confirmPassword.equals("")){
            etConfirmPassword.setError("Enter confirm password");
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("Enter correct email");
        }else if(!password.equals(confirmPassword)){
            etConfirmPassword.setError("Password mismatch");
        }else{
            progressBar.setVisibility(View.VISIBLE);
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressBar.setVisibility(View.GONE);
                    if(task.isSuccessful()){

                        firebaseUser = firebaseAuth.getCurrentUser();

                        if(localFileUri != null){
                            updateNameAndPhoto();
                        }else {
                            updateOnlyName();
                        }
                    }else{
                        Toast.makeText(SignUp.this, "SignUp Failed : " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}