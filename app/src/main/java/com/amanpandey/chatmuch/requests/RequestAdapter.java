package com.amanpandey.chatmuch.requests;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amanpandey.chatmuch.R;
import com.amanpandey.chatmuch.common.Constants;
import com.amanpandey.chatmuch.common.NodeNames;
import com.amanpandey.chatmuch.common.Util;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private Context context;
    private List<RequestModel> requestModelList;
    private DatabaseReference databaseReferenceFriendRequests,databaseReferenceChats;
    private FirebaseUser currentUser;

    public RequestAdapter(Context context, List<RequestModel> requestModelList) {
        this.context = context;
        this.requestModelList = requestModelList;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_request_layout,parent,false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {

        RequestModel requestModel = requestModelList.get(position);

        holder.tvFullName.setText(requestModel.getUserName());
        StorageReference fileref = FirebaseStorage.getInstance().getReference().child(Constants.IMAGES_FOLDER + "/" + requestModel.getPhotoName());

        fileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(holder.ivProfile);
            }
        });

        // initialing database reference
        databaseReferenceFriendRequests = FirebaseDatabase.getInstance().getReference().child(NodeNames.FRIEND_REQUESTS);
        databaseReferenceChats = FirebaseDatabase.getInstance().getReference().child(NodeNames.CHATS);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        holder.btnAcceptRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.pbDecision.setVisibility(View.VISIBLE);
                holder.btnDenyRequest.setVisibility(View.GONE);
                holder.btnAcceptRequest.setVisibility(View.GONE);

                String userId = requestModel.getUserId();
                databaseReferenceChats.child(currentUser.getUid()).child(userId)
                        .child(NodeNames.TIME_STAMPS).setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            databaseReferenceChats.child(userId).child(currentUser.getUid())
                                    .child(NodeNames.TIME_STAMPS).setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        databaseReferenceFriendRequests.child(currentUser.getUid()).child(userId)
                                                .child(NodeNames.REQUESTS_TYPE).setValue(Constants.REQUEST_STATUS_ACCEPTED).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    databaseReferenceFriendRequests.child(userId).child(currentUser.getUid())
                                                            .child(NodeNames.REQUESTS_TYPE).setValue(Constants.REQUEST_STATUS_ACCEPTED).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful())

                                                            {   String title = "Friend Request Accepted";
                                                                String message= "Friend request accepted by " + currentUser.getDisplayName();
                                                                Util.sendNotification(context, title, message, userId);

                                                                holder.pbDecision.setVisibility(View.GONE);
                                                                holder.btnDenyRequest.setVisibility(View.VISIBLE);
                                                                holder.btnAcceptRequest.setVisibility(View.VISIBLE);
                                                            }
                                                            else
                                                            {
                                                                Toast.makeText(context, "Failed to accept request : " + task.getException(), Toast.LENGTH_SHORT).show();
                                                                holder.pbDecision.setVisibility(View.GONE);
                                                                holder.btnDenyRequest.setVisibility(View.VISIBLE);
                                                                holder.btnAcceptRequest.setVisibility(View.VISIBLE);
                                                            }
                                                        }
                                                    });
                                                }
                                                else
                                                {
                                                    Toast.makeText(context, "Failed to accept request : " + task.getException(), Toast.LENGTH_SHORT).show();
                                                    holder.pbDecision.setVisibility(View.GONE);
                                                    holder.btnDenyRequest.setVisibility(View.VISIBLE);
                                                    holder.btnAcceptRequest.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        });

                                    }
                                    else
                                    {
                                        Toast.makeText(context, "Failed to accept request : " + task.getException(), Toast.LENGTH_SHORT).show();
                                        holder.pbDecision.setVisibility(View.GONE);
                                        holder.btnDenyRequest.setVisibility(View.VISIBLE);
                                        holder.btnAcceptRequest.setVisibility(View.VISIBLE);
                                    }
                                }
                            });


                        }
                        else
                        {
                            Toast.makeText(context, "Failed to accept request : " + task.getException(), Toast.LENGTH_SHORT).show();
                            holder.pbDecision.setVisibility(View.GONE);
                            holder.btnDenyRequest.setVisibility(View.VISIBLE);
                            holder.btnAcceptRequest.setVisibility(View.VISIBLE);
                        }
                    }
                });


            }
        });

        holder.btnDenyRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                holder.pbDecision.setVisibility(View.VISIBLE);
                holder.btnDenyRequest.setVisibility(View.GONE);
                holder.btnAcceptRequest.setVisibility(View.GONE);

                String userId = requestModel.getUserId();
                databaseReferenceFriendRequests.child(currentUser.getUid()).child(userId)
                        .child(NodeNames.REQUESTS_TYPE).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            databaseReferenceFriendRequests.child(userId).child(currentUser.getUid())
                                    .child(NodeNames.REQUESTS_TYPE).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        String title = "Friend Request Denied";
                                        String message= "Friend request denied by " + currentUser.getDisplayName();
                                        Util.sendNotification(context, title, message, userId);

                                        holder.pbDecision.setVisibility(View.GONE);
                                        holder.btnDenyRequest.setVisibility(View.VISIBLE);
                                        holder.btnAcceptRequest.setVisibility(View.VISIBLE);
                                    }
                                    else
                                    {
                                        Toast.makeText(context, "Failed to Deny request : " + task.getException(), Toast.LENGTH_SHORT).show();
                                        holder.pbDecision.setVisibility(View.GONE);
                                        holder.btnDenyRequest.setVisibility(View.VISIBLE);
                                        holder.btnAcceptRequest.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(context, "Failed to Deny request : " + task.getException(), Toast.LENGTH_SHORT).show();
                            holder.pbDecision.setVisibility(View.GONE);
                            holder.btnDenyRequest.setVisibility(View.VISIBLE);
                            holder.btnAcceptRequest.setVisibility(View.VISIBLE);
                        }
                    }
                });


            }
        });
    }

    @Override
    public int getItemCount() {
        return requestModelList.size();
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder {

        private TextView tvFullName;
        private ImageView ivProfile;
        private Button btnAcceptRequest,btnDenyRequest;
        private ProgressBar pbDecision;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            btnAcceptRequest = itemView.findViewById(R.id.btnAcceptRequest);
            btnDenyRequest = itemView.findViewById(R.id.btnDenyRequest);
            pbDecision = itemView.findViewById(R.id.pbDecision);
        }
    }
}
