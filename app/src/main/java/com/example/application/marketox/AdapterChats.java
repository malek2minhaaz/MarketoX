package com.example.application.marketox;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.application.marketox.databinding.RowChatsBinding;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterChats extends RecyclerView.Adapter<AdapterChats.HolderChats> implements Filterable {

    private static final String TAG = "ADAPTER_CHATS_TAG";
    private Context context;
    public ArrayList<ModelChats> chatsArrayList;
    private ArrayList<ModelChats> filterList;
    private FilterChats filter;
    private FirebaseAuth firebaseAuth;
    private String myUid;

    public AdapterChats(Context context, ArrayList<ModelChats> chatsArrayList) {
        this.context = context;
        this.chatsArrayList = chatsArrayList;
        this.filterList = chatsArrayList;
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public HolderChats onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate binding locally for each holder
        RowChatsBinding binding = RowChatsBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderChats(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderChats holder, int position) {
        ModelChats modelChat = chatsArrayList.get(position);

        loadLastMessage(modelChat, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String receiptUid = modelChat.getReceiptUid();
                if (receiptUid != null) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("receiptUid", receiptUid);
                    context.startActivity(intent);
                }
            }
        });
    }

    private void loadLastMessage(ModelChats modelChats, HolderChats holder) {
        String chatKey = modelChats.chatkey;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.child(chatKey).limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String fromUid = "" + ds.child("fromUid").getValue();
                            String message = "" + ds.child("message").getValue();
                            String messageId = "" + ds.child("messageId").getValue();
                            String messageType = "" + ds.child("messageType").getValue();
                            String toUid = "" + ds.child("toUid").getValue();

                            // Safe null check for timestamp
                            Object timestampObj = ds.child("timestamp").getValue();
                            long timestamp = (timestampObj instanceof Long) ? (long) timestampObj : 0L;

                            String formattedDate = Utils.formatTimestampDateTime(timestamp);
                            modelChats.setMessage(message);
                            modelChats.setMessageId(messageId);
                            modelChats.setMessageType(messageType);
                            modelChats.setTimestamp(timestamp);
                            modelChats.setFromUid(fromUid);
                            modelChats.setToUid(toUid);

                            holder.dateTimeTv.setText(formattedDate);

                            if (messageType.equals(Utils.MESSAGE_TYPE_TEXT)) {
                                holder.lastMessageTv.setText(message);
                            } else {
                                holder.lastMessageTv.setText("Sends Attachment");
                            }
                        }
                        loadReceiptUserInfo(modelChats, holder);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadReceiptUserInfo(ModelChats modelChats, HolderChats holder) {
        String fromUid = modelChats.getFromUid();
        String toUid = modelChats.getToUid();

        String receiptUid;
        // Use myUid.equals(fromUid) to avoid NPE if fromUid is null
        if (myUid != null && myUid.equals(fromUid)) {
            receiptUid = toUid;
        } else {
            receiptUid = fromUid;
        }

        modelChats.setReceiptUid(receiptUid);

        if (receiptUid == null || receiptUid.equals("null")) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(receiptUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = "" + snapshot.child("name").getValue();
                        String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();

                        modelChats.setName(name);
                        modelChats.setProfileImageUrl(profileImageUrl);

                        holder.nameTv.setText(name);
                        try {
                            Glide.with(context)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_person2_white)
                                    .into(holder.profileIv);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange Glide: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    public int getItemCount() {
        return chatsArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterChats(this, filterList);
        }
        return filter;
    }

    class HolderChats extends RecyclerView.ViewHolder {
        ShapeableImageView profileIv;
        TextView nameTv, lastMessageTv, dateTimeTv;

        public HolderChats(RowChatsBinding binding) {
            super(binding.getRoot());
            // Assign views from the specific binding instance
            profileIv = binding.profileIv;
            nameTv = binding.nameTv;
            lastMessageTv = binding.lastMessageTv;
            dateTimeTv = binding.dateTimeTv;
        }
    }
}