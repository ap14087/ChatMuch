package FindFriends;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amanpandey.chatmuch.R;
import com.amanpandey.chatmuch.common.Constants;
import com.amanpandey.chatmuch.common.NodeNames;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


public class FindFriendsFragment extends Fragment {

    private RecyclerView rvFindFriends;
    private FindFriendAdapter findFirendAdapter;
    private List<FindFriendModel> findFriendModelList;
    private TextView tvEmptyFriendsList;

    private DatabaseReference databaseReference , databaseReferenceFriendRequests;
    private FirebaseUser currentUser;
    private View porgressBar;

    public FindFriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFindFriends = view.findViewById(R.id.rvFindFriends);
        porgressBar = view.findViewById(R.id.progressBar);
        tvEmptyFriendsList = view.findViewById(R.id.tvEmptyFriendsList);

        rvFindFriends.setLayoutManager(new LinearLayoutManager(getActivity()));

        findFriendModelList = new ArrayList<>();
        findFirendAdapter = new FindFriendAdapter(getActivity(),findFriendModelList);
        rvFindFriends.setAdapter(findFirendAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        databaseReferenceFriendRequests = FirebaseDatabase.getInstance().getReference().child(NodeNames.FRIEND_REQUESTS).child(currentUser.getUid());

        Log.d("we are in :","fragment above progessbar");
        tvEmptyFriendsList.setVisibility(View.VISIBLE);
        porgressBar.setVisibility(View.VISIBLE);
        Log.d("we are in :","fragment Visible progessbar");
        Query query = databaseReference.orderByChild(NodeNames.NAME);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 findFriendModelList.clear();
                 for(DataSnapshot ds : dataSnapshot.getChildren()){

                     String userId = ds.getKey();

                     if(userId.equals(currentUser.getUid()))
                         return;

                     if(ds.child(NodeNames.NAME).getValue() != null)
                     {
                         String fullName = ds.child(NodeNames.NAME).getValue().toString();
                         String photoName = ds.child(NodeNames.PHOTO).getValue().toString();
                         Log.d("we are in :","fragment fetching data");
                         databaseReferenceFriendRequests.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                 if(dataSnapshot1.exists()){
                                     String requestType = dataSnapshot1.child(NodeNames.REQUESTS_TYPE).getValue().toString();
                                     if(requestType.equals(Constants.REQUEST_STATUS_SENT))
                                     {
                                         Log.d("we are in :","fragment sending data to list");
                                         findFriendModelList.add(new FindFriendModel(fullName,photoName,userId,true));
                                         findFirendAdapter.notifyDataSetChanged();

                                     }
                                 }
                                 else
                                 {

                                     findFriendModelList.add(new FindFriendModel(fullName,photoName,userId,false));
                                     findFirendAdapter.notifyDataSetChanged();

                                 }
                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError error) {
                                porgressBar.setVisibility(View.GONE);
                             }
                         });

                         tvEmptyFriendsList.setVisibility(View.GONE);
                         porgressBar.setVisibility(View.GONE);
                     }
                 }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                porgressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to Find friends : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}