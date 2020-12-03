package com.amanpandey.chatmuch.chats;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amanpandey.chatmuch.R;
import com.amanpandey.chatmuch.common.NodeNames;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ChatFragment extends Fragment {

    private RecyclerView rvChatList;
    private View progressBar;
    private TextView tvEmptyChatList;
    private ChatListAdapter chatListAdapter;
    private List<ChatListModel> chatListModelList;

    private DatabaseReference databaseReferenceChats,databaseReferenceUsers;
    private FirebaseUser currentUser;

    private ChildEventListener childEventListener;
    private Query query;
    private   List<String> userIds = new ArrayList<>();

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvChatList = view.findViewById(R.id.rvChat);
        tvEmptyChatList = view.findViewById(R.id.tvEmptyChatList);

        chatListModelList = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(getActivity(),chatListModelList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
       linearLayoutManager.setReverseLayout(true);
       linearLayoutManager.setStackFromEnd(true);
       rvChatList.setLayoutManager(linearLayoutManager);

       rvChatList.setAdapter(chatListAdapter);

       progressBar = view.findViewById(R.id.progressBar);
       currentUser = FirebaseAuth.getInstance().getCurrentUser();

       databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
       databaseReferenceChats = FirebaseDatabase.getInstance().getReference().child(NodeNames.CHATS).child(currentUser.getUid());

       query = databaseReferenceChats.orderByChild(NodeNames.TIME_STAMPS);

       childEventListener = new ChildEventListener() {
           @Override
           public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                updateList(snapshot,true,snapshot.getKey());
           }

           @Override
           public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
               updateList(snapshot, false, snapshot.getKey());
           }

           @Override
           public void onChildRemoved(@NonNull DataSnapshot snapshot) {

           }

           @Override
           public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       };

       query.addChildEventListener(childEventListener);

        progressBar.setVisibility(View.VISIBLE);
        tvEmptyChatList.setVisibility(View.VISIBLE);
    }

    private void updateList(DataSnapshot snapshot , boolean isNew, String userId)
    {
        progressBar.setVisibility(View.GONE);
        tvEmptyChatList.setVisibility(View.GONE);

        String lastMessage,lastMessageTime,unreadCount;

        if(snapshot.child(NodeNames.LAST_MESSAGE).getValue()!=null)
            lastMessage = snapshot.child(NodeNames.LAST_MESSAGE).getValue().toString();
        else
            lastMessage = "";

        if(snapshot.child(NodeNames.LAST_MESSAGE_TIME).getValue()!=null)
            lastMessageTime = snapshot.child(NodeNames.LAST_MESSAGE_TIME).getValue().toString();
        else
            lastMessageTime="";

        unreadCount=snapshot.child(NodeNames.UNREAD_COUNT).getValue()==null?
                "0":snapshot.child(NodeNames.UNREAD_COUNT).getValue().toString();


        databaseReferenceUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String fullName = snapshot.child(NodeNames.NAME).getValue()!=null?
                        snapshot.child(NodeNames.NAME).getValue().toString():"";

                /*String photoName = dataSnapshot.child(NodeNames.PHOTO).getValue()!=null?
                        dataSnapshot.child(NodeNames.PHOTO).getValue().toString():"";*/
                String photoName  = userId +".jpg";

                ChatListModel chatListModel = new ChatListModel(userId, fullName, photoName,unreadCount,lastMessage,lastMessageTime);

                if(isNew) {
                    chatListModelList.add(chatListModel);
                    userIds.add(userId);
                }
                else {
                    int indexOfClickedUser = userIds.indexOf(userId) ;
                    chatListModelList.set(indexOfClickedUser, chatListModel);
                }

                chatListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Failed to fetch chat list : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        query.removeEventListener(childEventListener);
    }
}