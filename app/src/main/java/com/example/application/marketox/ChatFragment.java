package com.example.application.marketox;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.application.marketox.databinding.FragmentChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private static final String TAG = "CHATS_TAG";

    private FirebaseAuth firebaseAuth;
    private String myUid;
    private ArrayList<ModelChats> chatsArrayList;
    private AdapterChats adapterChats;

    public ChatFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();


        myUid = firebaseAuth.getUid();

        Log.d(TAG, "OnViewCreated: myUid: " + myUid);


        if (myUid != null) {
            loadChats();
        }

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String query = s.toString();
                    // FIX 2: Check if adapter is null before filtering
                    if (adapterChats != null) {
                        adapterChats.getFilter().filter(query);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    // ----------------  LOAD CHATS  ----------------
    private void loadChats() {
        chatsArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatsArrayList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String chatKey = "" + ds.getKey();

                    // FIX 3: Double check myUid is not null before calling .contains()
                    if (myUid != null && chatKey.contains(myUid)) {
                        ModelChats modelChats = new ModelChats();
                        modelChats.setChatkey(chatKey);
                        chatsArrayList.add(modelChats);
                    }
                }

                // Use getContext() or requireContext() instead of mContext
                if (getContext() != null) {
                    adapterChats = new AdapterChats(getContext(), chatsArrayList);
                    binding.chatsRv.setAdapter(adapterChats);
                    sort();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: ", error.toException());
            }
        });
    }

    private void sort() {
        new Handler().postDelayed(() -> {
            if (chatsArrayList != null && adapterChats != null) {
                Collections.sort(chatsArrayList, (model1, model2) ->
                        Long.compare(model2.getTimestamp(), model1.getTimestamp()));
                adapterChats.notifyDataSetChanged();
            }
        }, 500);
    }
}