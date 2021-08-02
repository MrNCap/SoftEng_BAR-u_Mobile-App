package com.example.baru_app.HISTORY_TRANSACTION;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.baru_app.DATABASE_SQL.DatabaseHelper;
import com.example.baru_app.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class History_Adapter extends FirestoreRecyclerAdapter<com.example.baru_app.HISTORY_TRANSACTION.Histoy_Model, History_Adapter.PostHolder> {
    FirebaseFirestore firestoreDB;
    DocumentReference PostListRef;
    CollectionReference commentCounter;
    FirebaseAuth firebaseAuth;
    String userID,sql_return_barangay,postID;
    StorageReference Address_storageRef,BarangayProfile;
    FirebaseStorage firebaseStorage;
    DatabaseHelper databasehelper;
    OnItemClicklistener listener;

    public History_Adapter(@NonNull FirestoreRecyclerOptions<com.example.baru_app.HISTORY_TRANSACTION.Histoy_Model> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull PostHolder holder, int position, @NonNull com.example.baru_app.HISTORY_TRANSACTION.Histoy_Model model) {
        postID = model.getDocID();
        firebaseStorage = FirebaseStorage.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();

        //DB SQL
        databasehelper = new DatabaseHelper(holder.itemView.getContext());
        sql_return_barangay = databasehelper.getBarangayCurrentUser(userID);
        databasehelper.close();



        holder.Transaction_Date.setText(model.getStatus());
        if(model.getType().equals("certificate_residency")){
            holder.Transaction_Title.setText("Certificate of Residency");
        }else if(model.getType().equals("certificate_indigency")){
            holder.Transaction_Title.setText("Certificate of Indigency");
        }else if(model.getType().equals("barangay_id_replacement")){
            holder.Transaction_Title.setText("Barangay ID [REPLACEMENT]");
        }else if(model.getType().equals("barangay_id")){
            holder.Transaction_Title.setText("Barangay ID");
        }else if(model.getType().equals("barangay_clearance")){
            holder.Transaction_Title.setText("Barangay Clearance");
        }else if(model.getType().equals("barangay_certificate")){
            holder.Transaction_Title.setText("Barangay Certificate");
        }

        if(model.getStatus().equals("Pending")){
            holder.Status_Logo.setBackgroundResource(R.drawable.icon_pending);
        }else if(model.getStatus().equals("In-Progress")){
            holder.Status_Logo.setBackgroundResource(R.drawable.icon_in_progress);
        }else if(model.getStatus().equals("Pickup")){
            holder.Status_Logo.setBackgroundResource(R.drawable.icon_ready_pickup);
        }else if(model.getStatus().equals("Completed")){
            holder.Status_Logo.setBackgroundResource(R.drawable.check_icon);
        }else{
            //failed
            holder.Status_Logo.setBackgroundResource(R.drawable.x_icon);
        }


    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_list,parent, false);
        return new PostHolder(view);
    }

    class PostHolder extends RecyclerView.ViewHolder{
        TextView Transaction_Title,Transaction_Date;
        ImageView Status_Logo;
        public PostHolder(@NonNull View itemView) {
            super(itemView);
            Transaction_Title = itemView.findViewById(R.id.transaction_name);
            Transaction_Date = itemView.findViewById(R.id.transaction_date);
            Status_Logo = itemView.findViewById(R.id. logo_transaction);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position),position);
                    }
                }
            });

        }


    }
    public interface OnItemClicklistener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);


    }
    public void setOnItemClickListner(OnItemClicklistener listener){
        this.listener = listener;
    }
}
