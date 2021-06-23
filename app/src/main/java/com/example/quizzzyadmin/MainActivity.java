package com.example.quizzzyadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
private EditText email,password;
private Button login;
private FirebaseAuth firebaseAuth;
private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        login=findViewById(R.id.login);
        progressBar=findViewById(R.id.progressBar);

        firebaseAuth=FirebaseAuth.getInstance();
        final Intent intent=new Intent(this,Categoryactivity.class);


       if(firebaseAuth.getCurrentUser()!=null){
           startActivity(intent);
           finish();

           return;
       }
       login.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(email.getText().toString().isEmpty()){
                   email.setError("required");
                   return;
               }else {
                   email.setError(null);
               }
               if(password.getText().toString().isEmpty()){
                   password.setError("required");
                   return;
               }else {
                   password.setError(null);
               }
               progressBar.setVisibility(View.VISIBLE);
               firebaseAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                       .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                           @Override
                           public void onComplete(@NonNull Task<AuthResult> task) {
                               if(task.isSuccessful()){
                                   startActivity(intent);
                                   finish();
                                   Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();

                               }
                               else {
                                   Toast.makeText(MainActivity.this, "failure", Toast.LENGTH_SHORT).show();

                               }
                               progressBar.setVisibility(View.INVISIBLE);
                           }
                       });

           }
       });

    }
}
