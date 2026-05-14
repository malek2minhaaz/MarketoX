package com.example.application.marketox;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.application.marketox.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;

    private static final String TAG = "CHAT_TAG";

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private String receiptUid = "";
    private String myUid = "";
    private String chatPath = "";

    private Uri imageUri = null;

    private ArrayList<ModelChat> chatArrayList;
    private AdapterChat adapterChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        receiptUid = getIntent().getStringExtra("receiptUid");
        myUid = firebaseAuth.getUid();

        chatPath = Utils.chatPath(receiptUid, myUid);

        Log.d(TAG, "receiptUid: " + receiptUid);
        Log.d(TAG, "myUid: " + myUid);
        Log.d(TAG, "chatPath: " + chatPath);

        // Setup RecyclerView
        chatArrayList = new ArrayList<>();
        adapterChat = new AdapterChat(this, chatArrayList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        binding.chatRv.setLayoutManager(layoutManager);
        binding.chatRv.setAdapter(adapterChat);

        loadReceiptDetails();
        loadMessages();

        binding.toolbarBackBtn.setOnClickListener(v -> finish());
        binding.attachFab.setOnClickListener(v -> imagePickDialog());
        binding.sendBtn.setOnClickListener(v -> validateData());
    }

    // 🔹 Load Receiver Info
    private void loadReceiptDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(receiptUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            String name = "" + snapshot.child("name").getValue();
                            String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();

                            binding.toolbarTitleTv.setText(name);

                            Glide.with(ChatActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_person2_white)
                                    .into(binding.toolbarProfileIv);

                        } catch (Exception e) {
                            Log.e(TAG, "loadReceiptDetails: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // 🔹 Load Messages (FIXED)
    private void loadMessages() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");

        ref.child(chatPath)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Log.d("CHAT_DEBUG", "Snapshot exists: " + snapshot.exists());

                        chatArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            try {
                                ModelChat modelChat = ds.getValue(ModelChat.class);

                                if (modelChat != null) {
                                    Log.d("CHAT_DEBUG", "Message: " + modelChat.getMessage());
                                    chatArrayList.add(modelChat);
                                } else {
                                    Log.d("CHAT_DEBUG", "ModelChat NULL");
                                }

                            } catch (Exception e) {
                                Log.e(TAG, "loadMessages error: ", e);
                            }
                        }

                        adapterChat.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // 🔹 Validate Message
    private void validateData() {
        String message = binding.messageEt.getText().toString().trim();
        long timestamp = System.currentTimeMillis();

        if (message.isEmpty()) {
            Utils.toast(this, "Enter message");
        } else {
            sendMessage(Utils.MESSAGE_TYPE_TEXT, message, timestamp);
        }
    }

    // 🔹 Send Message (FIXED TIMESTAMP)
    private void sendMessage(String messageType, String message, long timestamp) {

        progressDialog.setMessage("Sending...");
        progressDialog.show();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        String keyId = ref.push().getKey();

        HashMap<String, Object> map = new HashMap<>();
        map.put("messageId", keyId);
        map.put("message", message);
        map.put("messageType", messageType);
        map.put("fromUid", myUid);
        map.put("toUid", receiptUid);
        map.put("timestamp", timestamp); // ✅ FIXED

        ref.child(chatPath).child(keyId)
                .setValue(map)
                .addOnSuccessListener(unused -> {
                    binding.messageEt.setText("");
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Utils.toast(ChatActivity.this, "Failed: " + e.getMessage());
                });
    }

    // 🔹 Image Picker
    private void imagePickDialog() {
        PopupMenu menu = new PopupMenu(this, binding.attachFab);

        menu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        menu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

        menu.show();

        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA});
            } else {
                pickImageGallery();
            }
            return false;
        });
    }

    private void pickImageGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            imageUri = result.getData().getData();
                            uploadImage();
                        }
                    });

    private final ActivityResultLauncher<String[]> requestCameraPermissions =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> pickImageCamera());

    private void pickImageCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "TEMP");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            uploadImage();
                        }
                    });

    private void uploadImage() {

        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("ChatImages/" + timestamp);

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            sendMessage(Utils.MESSAGE_TYPE_IMAGE, uri.toString(), timestamp);
                        }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Utils.toast(this, e.getMessage());
                });
    }
}