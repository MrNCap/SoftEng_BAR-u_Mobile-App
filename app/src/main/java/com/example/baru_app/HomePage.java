package com.example.baru_app;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.baru_app.DATABASE_SQL.DatabaseHelper;
import com.example.baru_app.POST_HOME.Post_Adapter;
import com.example.baru_app.POST_HOME.Post_Model;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomePage extends AppCompatActivity {
    Post_Adapter Post_Adapter;
    CollectionReference PostListRef;
    FirebaseFirestore firestoreDB;
    DocumentReference UserListRef;
    FirebaseAuth firebaseAuth;
    String userID,sql_return_barangay;
    StorageReference Address_storageRef;
    FirebaseStorage firebaseStorage;
    CircleImageView userpic_home;
    DatabaseHelper databasehelper;
    Dialog addPost,admin_addpost;
    Button addPost_btn,addPost_btn_admin;
    EditText addPost_et,admin_title_post,admin_content_post;
    TextView brgy_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.home_page);
        //FIREBASE
        firebaseStorage = FirebaseStorage.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();

        //WIDGETS
        userpic_home = findViewById(R.id.home_brgy_profile);
        brgy_name = findViewById(R.id.brgy_name);

        //DB SQL
        databasehelper = new DatabaseHelper(HomePage.this);
        sql_return_barangay = databasehelper.getBarangayCurrentUser(userID);
        databasehelper.close();
        PostListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("post");


        //Post User Setting
        addPost = new Dialog(HomePage.this);
        addPost.setContentView(R.layout.add_post);
        addPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        addPost_et = addPost.findViewById(R.id.input_post);
        addPost_btn = addPost.findViewById(R.id.addpost_btn);

        //Admin Post User Setting
        admin_addpost = new Dialog(HomePage.this);
        admin_addpost.setContentView(R.layout.add_post_admin);
        admin_addpost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        admin_title_post = admin_addpost.findViewById(R.id.input_post2);
        admin_content_post = admin_addpost.findViewById(R.id.input_post);
        addPost_btn_admin = admin_addpost.findViewById(R.id.addpost_btn);


        //USER PIC
        UserListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(userID);
        UserListRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                brgy_name.setText("Brgy. " + value.getString("barangay"));
            }
        });
        Address_storageRef = firebaseStorage.getReference().child("BARANGAY LOGO/"+sql_return_barangay+"/profile_brgy.jpg");
        Address_storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(userpic_home);
            }
        });

        //PUT POST
        post_recycleView();


        Post_Adapter.setOnItemClickListner(new Post_Adapter.OnItemClicklistener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                String id = documentSnapshot.getId();
                Intent OpenPost = new Intent(HomePage.this, com.example.baru_app.POST_HOME.OpenPost.class);
                OpenPost.putExtra("pass_ID",id);
                startActivity(OpenPost);
            }
        });



    }
    public void GoTo_Services(View view){
        startActivity(new Intent(getApplicationContext(),Services.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }
    public void GoTo_Profile(View view){
        startActivity(new Intent(getApplicationContext(),Profile.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));

    }
    public void AddPost(View view){
        UserListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(userID);
        UserListRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value.getBoolean("admin").equals(true)){
                    admin_addpost.show();
                    addPost_btn_admin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String postnew = admin_content_post.getText().toString().trim();
                            String title_post = admin_title_post.getText().toString().trim();
                            if(TextUtils.isEmpty(postnew)){
                                admin_content_post.setError("Input is required.");
                                return;
                            }
                            if(TextUtils.isEmpty(title_post)){
                                admin_title_post.setError("Input is required.");
                                return;
                            }else{
                                Date timestamp = new Date();
                                String date_string = DateFormat.getDateInstance().format(timestamp);
                                String time_string = DateFormat.getTimeInstance().format(timestamp);
                                PostListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("post");
                                Map<String, Object> newPost = new HashMap<>();
                                newPost.put("author_id", userID);
                                newPost.put("description", postnew);
                                newPost.put("timestamp",timestamp);
                                newPost.put("date", date_string);
                                newPost.put("time", time_string);
                                newPost.put("numberComment", "");
                                newPost.put("title", title_post);
                                newPost.put("admin", true);
                                PostListRef.document().set(newPost).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(HomePage.this, "Post Added", Toast.LENGTH_SHORT).show();
//                                        Snackbar snackbar = Snackbar.make(linearLayoutCompat, "Failed, Try again", Snackbar.LENGTH_SHORT);
                                        admin_addpost.dismiss();
                                    }
                                });
                            }
                        }
                    });
                }else{
                    addPost.show();
                    addPost_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String postnew = addPost_et.getText().toString().trim();
                            if(TextUtils.isEmpty(postnew)){
                                addPost_et.setError("Input is required.");
                                return;
                            }else{
                                Date timestamp = new Date();
                                String date_string = DateFormat.getDateInstance().format(timestamp);
                                String time_string = DateFormat.getTimeInstance().format(timestamp);
                                PostListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("post");
                                Map<String, Object> newPost = new HashMap<>();
                                newPost.put("author_id", userID);
                                newPost.put("description", postnew);
                                newPost.put("timestamp",timestamp);
                                newPost.put("date", date_string);
                                newPost.put("time", time_string);
                                newPost.put("numberComment", "");
                                newPost.put("title", "");
                                newPost.put("admin", false);
                                PostListRef.document().set(newPost).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(HomePage.this, "Post Added", Toast.LENGTH_SHORT).show();
                                        addPost.dismiss();
                                    }
                                });
                            }
                        }
                    });
                }///
            }
        });

    }

    public void post_recycleView(){
        Query query = PostListRef.orderBy("timestamp",Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Post_Model> Post_Options = new FirestoreRecyclerOptions.Builder<Post_Model>()
                .setQuery(query, Post_Model.class)
                .build();
        Post_Adapter = new Post_Adapter(Post_Options);
        RecyclerView post_View = findViewById(R.id.home_post_recycleView);
        post_View.setHasFixedSize(true);
        post_View.setLayoutManager(new LinearLayoutManager(this));
        post_View.setAdapter(Post_Adapter);
    }
    @Override
    protected void onStart() {
        super.onStart();
        Post_Adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Post_Adapter.stopListening();
    }
}