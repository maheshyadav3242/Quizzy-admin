package com.example.quizzzyadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.UUID;

public class Setsactivity extends AppCompatActivity {
    private GridView gridView;
    private Dialog loadingdialog;
    private Gridviewadapeter gridviewadapeter;
    private DatabaseReference myref;
    private String categoryname;
    private List<String>sets;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setsactivity);
        Toolbar toolbar=findViewById(R.id.setstoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        categoryname=getIntent().getStringExtra("title");
        getSupportActionBar().setTitle(getIntent().getStringExtra("title"));

        loadingdialog = new Dialog(this);
        loadingdialog.setContentView(R.layout.loadingdialog);
        loadingdialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingdialog.setCancelable(false);
        loadingdialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        gridView=findViewById(R.id.gridview);

        myref=FirebaseDatabase.getInstance().getReference();
        sets=Categoryactivity.list.get(getIntent().getIntExtra("position", 0)).getSets();

         gridviewadapeter=new Gridviewadapeter(sets, getIntent().getStringExtra("title"), new Gridviewadapeter.Gridlistener() {
            @Override
            public void addset() {
                loadingdialog.show();
                final String id= UUID.randomUUID().toString();

                FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
                firebaseDatabase.getReference().child("Categories").child(getIntent().getStringExtra("key")).child("sets").child(id).setValue("SET ID")
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            sets.add(id);
                          gridviewadapeter.notifyDataSetChanged();

                        }else {
                            Toast.makeText(Setsactivity.this, "something went wrng", Toast.LENGTH_SHORT).show();
                        }
                        loadingdialog.dismiss();
                    }
                });

            }

             @Override
             public void onlongclick(final String setid,int position) {
                 new AlertDialog.Builder(Setsactivity.this,R.style.Theme_AppCompat_Light_Dialog_Alert)
                         .setTitle("delete sets"+position).
                         setMessage("are you sure you want to delete set").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         loadingdialog.show();
                         myref.child("SETS").child(setid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                             @Override
                             public void onComplete(@NonNull Task<Void> task) {
                                 if (task.isSuccessful()) {

                                     myref.child("Categories").child(Categoryactivity.list.get(getIntent().getIntExtra("position", 0)).getKey())
                                             .child("sets").child("setid").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                         @Override
                                         public void onComplete(@NonNull Task<Void> task) {
                                             if(task.isSuccessful()){
                                                 sets.remove(setid);
                                                 gridviewadapeter.notifyDataSetChanged();
                                             }else {
                                                 Toast.makeText(Setsactivity.this, "something went wrong", Toast.LENGTH_SHORT).show();

                                             }
                                             loadingdialog.dismiss();
                                         }
                                     });


                                 } else {
                                     Toast.makeText(Setsactivity.this, "somthing went wrong", Toast.LENGTH_SHORT).show();
                                     loadingdialog.dismiss();
                                 }
                             }
                         });

                     }
                 }).setNegativeButton("cancel",null)
                         .setIcon(android.R.drawable.ic_dialog_alert).show();

             }
         });
        gridView.setAdapter(gridviewadapeter);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
