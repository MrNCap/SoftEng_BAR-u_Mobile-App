package com.example.baru_app.POST_HOME;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.baru_app.DATABASE_SQL.DatabaseHelper;
import com.example.baru_app.HomePage;
import com.example.baru_app.POST_HOME.COMMENT.Comment_Adapter;
import com.example.baru_app.POST_HOME.COMMENT.Comment_Model;
import com.example.baru_app.Profile;
import com.example.baru_app.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OpenPost extends AppCompatActivity {
    FirebaseFirestore firestoreDB;
    DocumentReference PostListRef,UserRef;
    FirebaseAuth firebaseAuth;
    String userID,sql_return_barangay,pass_ID;
    StorageReference Address_storageRef,BarangayProfile;
    FirebaseStorage firebaseStorage;
    DatabaseHelper databasehelper;
    CollectionReference CommentListRef;
    DocumentReference UserCommentListRef,Delete_UserCommentListRef;
    Comment_Adapter comment_adapter;
    EditText commentBox;
    Dialog openCommentSetting,editComment,deleteComment;
    Button edit_comment_setting,delete_comment_setting,update_comment,delete_comment,cancel_delete_comment;
    TextView editAuthor,editDate,editTime;
    EditText editComment_user;
    String comment_id;
    TextView Post_Title,Post_Date,Post_Description,Post_Time,Post_Author;
    ImageView Post_Profile,Post_Setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_post_activity);

        firebaseStorage = FirebaseStorage.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();

        //DB SQL
        databasehelper = new DatabaseHelper(OpenPost.this);
        sql_return_barangay = databasehelper.getBarangayCurrentUser(userID);
        databasehelper.close();
        // WIDGETS
        Post_Author = findViewById(R.id.tvPostAuthor);
        Post_Title = findViewById(R.id.tv_postTitle);
        Post_Date = findViewById(R.id. tvPostDate);
        Post_Time = findViewById(R.id. tvPostTime);
        Post_Description = findViewById(R.id. tvPostContent);
        Post_Profile = findViewById(R.id. profileImage);
        Post_Setting = findViewById(R.id.settings_post);
        pass_ID = getIntent().getStringExtra("pass_ID");
        commentBox = findViewById(R.id.etCommentBox);

        //Comment User Setting
        openCommentSetting = new Dialog(OpenPost.this);
        openCommentSetting.setContentView(R.layout.comment_settings_popout);
        openCommentSetting.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        edit_comment_setting = openCommentSetting.findViewById(R.id.edit_comment_setting);
        delete_comment_setting = openCommentSetting.findViewById(R.id.delete_comment_setting);
            //DeleteComment User dialog
            deleteComment = new Dialog(OpenPost.this);
            deleteComment.setContentView(R.layout.delete_comment_dialog);
            deleteComment.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                //widgets
                delete_comment = deleteComment.findViewById(R.id.accept_delete_btn);
                cancel_delete_comment = deleteComment.findViewById(R.id.decline_delete_btn);
            //Comment User dialog
            editComment = new Dialog(OpenPost.this);
            editComment.setContentView(R.layout.edit_comment);
            editComment.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                //widgets
                editAuthor  = editComment.findViewById(R.id.edit_author);
                editDate  = editComment.findViewById(R.id.edit_date);
                editTime  = editComment.findViewById(R.id.edit_time);
                editComment_user = editComment.findViewById(R.id.commentBox_edit);
                update_comment = editComment.findViewById(R.id.update_comment_btn);

        CommentListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("post").document(pass_ID).collection("comment");
        PostListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("post").document(pass_ID);
        PostListRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable  DocumentSnapshot value_post, @Nullable  FirebaseFirestoreException error) {
                        String Author_ID = value_post.getString("author_id");
                        UserRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(Author_ID);
                        UserRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable  DocumentSnapshot value, @Nullable  FirebaseFirestoreException error) {
                                if(userID.equals(Author_ID)){
                                    Post_Setting.setVisibility(View.VISIBLE);
                                    Post_Setting.setClickable(true);
                                }

                                if(value.getBoolean("admin") == false){
                                    Post_Title.setText(value.getString("firstName") +" " + value.getString("lastName") );
                                    Post_Author.setText("Brgy. "+value.getString("barangay"));
                                    Post_Date.setText(value_post.getString("date"));
                                    Post_Time.setText(value_post.getString("time"));
                                    //USER PIC
                                    Address_storageRef = firebaseStorage.getReference().child("PROFILE/users/" +  userID + "/user_profile");
                                    Address_storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(Post_Profile);
                                        }
                                    });
                                }else{
                                    Post_Title.setText(value_post.getString("title"));
                                    Post_Author.setText("Brgy. "+value.getString("barangay"));
                                    Post_Description.setText(value_post.getString("description"));
                                    Post_Date.setText(value_post.getString("date"));
                                    Post_Time.setText(value_post.getString("time"));
                                    BarangayProfile = firebaseStorage.getReference().child("BARANGAY LOGO/"+sql_return_barangay+"/profile_brgy.jpg");
                                    BarangayProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(Post_Profile);
                                        }
                                    });
                                }

                            }
                        });

            }
        });
        comment_recycleView();
        //USER EDIT COMMENT
        comment_adapter.setOnLongClickListener(new Comment_Adapter.OnLongClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                comment_id = documentSnapshot.getId();
                openCommentSetting.show();
                UserCommentListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("post").document(pass_ID).collection("comment").document(comment_id);
                UserCommentListRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable  FirebaseFirestoreException error) {
                        if(value.getString("author_id").equals(userID)){
                            edit_comment_setting.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    openCommentSetting.dismiss();
                                    editComment.show();
                                        //show comment data
                                    UserRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(value.getString("author_id"));
                                    UserRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable DocumentSnapshot value_user, @Nullable FirebaseFirestoreException error) {
                                            editAuthor.setText(value_user.getString("firstName") +" " + value_user.getString("lastName"));
                                        }
                                    });

                                        editComment_user.setText(value.getString("comment"));
                                        editDate.setText(value.getString("date"));
                                        editTime.setText(value.getString("time"));

                                        //CLICK UPDATE BTN
                                        update_comment.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            String comment = editComment_user.getText().toString().trim();
                                            if(TextUtils.isEmpty(comment)){
                                                editComment_user.setError("Type something...");
                                                return;
                                            }else{
                                                editComment.dismiss();
                                                openCommentSetting.dismiss();
                                                UserCommentListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("post").document(pass_ID).collection("comment").document(comment_id);
                                                Map<String, Object> newComment = new HashMap<>();
                                                newComment.put("comment", comment);
                                                UserCommentListRef.update(newComment).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {

                                                        Toast.makeText(OpenPost.this, "Comment Updated", Toast.LENGTH_SHORT).show();

                                                    }
                                                });
                                            }

                                        }
                                    });
                                    }
                                    });//end Update Btn

                        }

                    }
                });

            }
        });
        delete_comment_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCommentSetting.dismiss();
                deleteComment.show();

                delete_comment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteComment.dismiss();
                        UserCommentListRef = firestoreDB.collection("barangays")
                                .document(sql_return_barangay).collection("post").document(pass_ID).collection("comment").document(comment_id);
                        UserCommentListRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Toast.makeText(OpenPost.this, pass_ID + "," + comment_id, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                cancel_delete_comment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteComment.dismiss();

                    }
                });
            }
        });

    }
    public void AddComment (View view){
        String comment = commentBox.getText().toString().trim();
        if(TextUtils.isEmpty(comment)){
            commentBox.setError("Type something...");
            return;
        }else{
            if(pass_ID != null){
                Date timestamp = new Date();
                String date_string = DateFormat.getDateInstance().format(timestamp);
                String time_string = DateFormat.getTimeInstance().format(timestamp);
                CommentListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("post").document(pass_ID).collection("comment");
                Map<String, Object> newComment = new HashMap<>();
                newComment.put("author_id", userID);
                newComment.put("comment", comment);
                newComment.put("timestamp",timestamp);
                newComment.put("date", date_string);
                newComment.put("time", time_string);
                newComment.put("postID", pass_ID);
                CommentListRef.add(newComment).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        commentBox.setText("");
                    }
                });
            }
        }
    }
    public void GoBack_Home(View view){
        startActivity(new Intent(getApplicationContext(), HomePage.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }
    public void comment_recycleView(){
        Query query = CommentListRef.orderBy("timestamp",Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Comment_Model> comment_options = new FirestoreRecyclerOptions.Builder<Comment_Model>()
                .setQuery(query, Comment_Model.class)
                .build();
        comment_adapter = new Comment_Adapter(comment_options);
        RecyclerView comment_view = findViewById(R.id.commentList);
        comment_view.setHasFixedSize(true);
        comment_view.setLayoutManager(new LinearLayoutManager(this));
        comment_view.setAdapter(comment_adapter);
    }
    @Override
    protected void onStart() {
        super.onStart();
        comment_adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        comment_adapter.stopListening();
    }
}