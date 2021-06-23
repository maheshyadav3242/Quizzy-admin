package com.example.quizzzyadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.UUID;

public class Addquestion extends AppCompatActivity {
    private EditText question;
    private RadioGroup options;
    private LinearLayout answers;
    private Button upload;
    private  String categoryname;
    private Dialog loadingdialog;
    /*private int setno;*/
    private int position;
    private Questionmodel questionmodel;
    private String id,setid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addquestion);
        Toolbar toolbar=findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add question");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        loadingdialog = new Dialog(this);
        loadingdialog.setContentView(R.layout.loadingdialog);
        loadingdialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingdialog.setCancelable(false);
        loadingdialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));

        question=findViewById(R.id.question1);
        options=findViewById(R.id.radiooption);
        answers=findViewById(R.id.answersid);
        upload=findViewById(R.id.button1);


        categoryname=getIntent().getStringExtra("category");
        setid=getIntent().getStringExtra("setid");
        position=getIntent().getIntExtra("position",-1);

        if(setid==null){
            finish();
            return;
        }
        if(position!=-1){
            questionmodel=Questiosactivity.list.get(position);

            setdata();
        }
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(question.getText().toString().isEmpty()){
                    question.setError("required");
                    return;
                }
              upload();
            }
        });

    }
    private void upload(){
        int correct=-1;

        for(int i=0;i<options.getChildCount();i++){
            EditText answer= (EditText) answers.getChildAt(i);
            if(answer.getText().toString().isEmpty()){
                answer.setError("required");
                return;
            }


            RadioButton radioButton= (RadioButton) options.getChildAt(i);
            if(radioButton.isChecked()){
                correct=i;
                break;
            }
        }
        if(correct==-1){
            Toast.makeText(this, "please mark correct option", Toast.LENGTH_SHORT).show();
            return;
        }

        final HashMap<String,Object>map=new HashMap<>();
        map.put("correctans",((EditText)answers.getChildAt(correct)).getText().toString());
        map.put("optiona",((EditText)answers.getChildAt(0)).getText().toString());
        map.put("optionb",((EditText)answers.getChildAt(1)).getText().toString());
        map.put("optionc",((EditText)answers.getChildAt(2)).getText().toString());
        map.put("optiond",((EditText)answers.getChildAt(3)).getText().toString());
        map.put("question",question.getText().toString());
        map.put("setid",setid);
  if(position!=-1){
    id=questionmodel.getId();
  }else {
    id=UUID.randomUUID().toString();
  }


      loadingdialog.show();
        FirebaseDatabase.getInstance().getReference().child("SETS").child(setid)
                .child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    Questionmodel questionmodel=new Questionmodel(id,map.get("question").toString(),
                            map.get("optiona").toString()
                            ,map.get("optionb").toString(),map.get("optionc").toString(),
                            map.get("optiond").toString(),
                            map.get("correctans").toString(),
                            map.get("setid").toString());

                    if(position!=-1){
                        Questiosactivity.list.set(position,questionmodel);
                    }else {
                        Questiosactivity.list.add(questionmodel);
                    }

                    finish();
                }else {
                    Toast.makeText(Addquestion.this, "something went wrong", Toast.LENGTH_SHORT).show();

                }
                loadingdialog.dismiss();

            }
        });
    }
    private  void setdata(){
        question.setText(questionmodel.getQuestion());

        ((EditText)answers.getChildAt(0)).setText(questionmodel.getA());
        ((EditText)answers.getChildAt(0)).setText(questionmodel.getB());
        ((EditText)answers.getChildAt(0)).setText(questionmodel.getC());
        ((EditText)answers.getChildAt(0)).setText(questionmodel.getD());
        for(int i=0;i<answers.getChildCount();i++){
            if(((EditText)answers.getChildAt(i)).getText().toString().equals(questionmodel.getAnswer())){
                RadioButton radioButton= (RadioButton) options.getChildAt(i);
                radioButton.setChecked(true);
                break;

            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
