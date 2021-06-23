package com.example.quizzzyadmin;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class Categoryactivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference();
    private Dialog loadingdialog, categorydailog;
    private CircleImageView circleImageView;
    private EditText categoryname;
    private Button addbtn;
    private Uri image;
    private String downloadurl;
    private RecyclerView recyclerView;
    public static List<Categorymodel> list;
    private Categoryadapter categoryadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categoryactivity);


        Toolbar toolbar = findViewById(R.id.mytooll);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");

        loadingdialog = new Dialog(this);
        loadingdialog.setContentView(R.layout.loadingdialog);
        loadingdialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingdialog.setCancelable(false);
        loadingdialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));

       setcategorydialog();
        recyclerView = findViewById(R.id.recycleviewmy);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        list = new ArrayList<>();
        categoryadapter = new Categoryadapter(list, new Categoryadapter.Deletelistener() {
            @Override
            public void ondelete(final String key, final int position) {

                new AlertDialog.Builder(Categoryactivity.this,R.style.Theme_AppCompat_Light_Dialog_Alert).setTitle("delete category").
                        setMessage("are you sure you want to delete category").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadingdialog.show();
                        myref.child("Categories").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    for (String setids:list.get(position).getSets()){
                                        myref.child("SETS").child(setids).removeValue();

                                    }
                                    list.remove(position);
                                    categoryadapter.notifyDataSetChanged();
                                    loadingdialog.dismiss();

                                }else {
                                    Toast.makeText(Categoryactivity.this, "failed to delete", Toast.LENGTH_SHORT).show();
                                }
                                loadingdialog.dismiss();
                            }
                        });
                    }
                }).setNegativeButton("cancel",null)
                        .setIcon(android.R.drawable.ic_dialog_alert).show();


            }
        });
        recyclerView.setAdapter(categoryadapter);
        loadingdialog.show();
        myref.child("Categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /*String data = dataSnapshot.getValue().toString();*/
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    List<String>sets=new ArrayList<>();
                    for(DataSnapshot dataSnapshot2:dataSnapshot1.child("sets").getChildren()){
                        sets.add(dataSnapshot2.getKey());
                    }
                    list.add(new Categorymodel(dataSnapshot1.child("name").getValue().toString(),sets
                            ,
                            dataSnapshot1.child("url")
                            .getValue().toString(),dataSnapshot1.getKey()));
                }

                categoryadapter.notifyDataSetChanged();
                loadingdialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Categoryactivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                loadingdialog.dismiss();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.add) {
            categorydailog.show();
        }
        if(item.getItemId()==R.id.logout){
            new AlertDialog.Builder(Categoryactivity.this,R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle("logout").
                    setMessage("are you sure you want to logout set")
                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    loadingdialog.show();
                    FirebaseAuth.getInstance().signOut();
                    Intent intent=new Intent(Categoryactivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).setNegativeButton("cancel",null)
                    .setIcon(android.R.drawable.ic_dialog_alert).show();
        }
        return super.onOptionsItemSelected(item);
    }

   private void setcategorydialog() {
        categorydailog = new Dialog(this);
        categorydailog.setContentView(R.layout.add_category_dialog);
     categorydailog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        categorydailog.setCancelable(true);
        categorydailog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_box));


        circleImageView = categorydailog.findViewById(R.id.image1);
        categoryname = categorydailog.findViewById(R.id.categoryname);
        addbtn = categorydailog.findViewById(R.id.add1);
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent galleryintent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryintent, 101);

            }
        });
        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(categoryname.getText().toString()==null||categoryname.getText().toString().isEmpty()){
                    categoryname.setError("required");
                    return;
                }
                for(Categorymodel model:list){
                   if(categoryname.getText().toString().equals(model.getName())){
                       categoryname.setError("category name already present");
                       return;
                   }
                }
                if(image==null){
                    Toast.makeText(Categoryactivity.this, "pickimage", Toast.LENGTH_SHORT).show();
                    return;
                }
                uploaddata();
                categorydailog.dismiss();


            }
        });

    }

   @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                 image = data.getData();
                circleImageView.setImageURI(image);
            }
        }
    }
    private void  uploaddata(){
        loadingdialog.show();
        StorageReference storageReference= FirebaseStorage.getInstance().getReference();
        final StorageReference imagerefrence=storageReference.child("categories").child(image.getLastPathSegment());

         UploadTask uploadTask = imagerefrence.putFile(image);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imagerefrence.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            downloadurl=task.getResult().toString();
                            uploadcategoryname();

                        }else {
                            loadingdialog.dismiss();

                            Toast.makeText(Categoryactivity.this, "somethingwent wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                } else {
                    loadingdialog.dismiss();
                    Toast.makeText(Categoryactivity.this, "somethingwent wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadcategoryname(){

        Map<String,Object> map=new HashMap<>();
        map.put("name",categoryname.getText().toString());
        map.put("sets",0);
        map.put("url",downloadurl);
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        final String id= UUID.randomUUID().toString();
        firebaseDatabase.getReference().child("Categories").child(id).setValue(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            list.add(new Categorymodel(categoryname.getText().toString(),new ArrayList<String>(),downloadurl,id));
                            categoryadapter.notifyDataSetChanged();
                        }else {
                            Toast.makeText(Categoryactivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                        }
                        loadingdialog.dismiss();

                    }
                });


    }

}
