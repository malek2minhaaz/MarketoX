package com.example.application.marketox;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.application.marketox.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {



    private ActivityMainBinding binding;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser()==null){
            startLoginOptions();

        }

        showHomeFragment();
        binding.bottomNv.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                int itemId= menuItem.getItemId();
                if(itemId == R.id.menu_home){

                    showHomeFragment();
                    return true;

                } else if (itemId == R.id.menu_chats) {
                    if(firebaseAuth.getCurrentUser()==null){
                        Utils.toast(MainActivity.this,"Please Login First");
                        startLoginOptions();
                        return  false;

                    }
                    else{

                        showChatFragment();
                        return true;

                    }


                }
                else if (itemId == R.id.menu_sell) {
                    showMyAdsFragment();
                    return true;

                    
                }
                else if (itemId == R.id.menu_my_ads) {

                    if(firebaseAuth.getCurrentUser()==null){
                        Utils.toast(MainActivity.this,"Please Login First");
                        startLoginOptions();
                        return  false;

                    }
                    else{
                        showMyAdsFragment();
                        return true;

                    }
                }
                else if (itemId == R.id.menu_account) {
                    if(firebaseAuth.getCurrentUser()==null){
                        Utils.toast(MainActivity.this,"Please Login First");
                        startLoginOptions();
                        return  false;

                    }
                    else{
                        showAccountFragment();
                        return true;


                    }


                }
                else{
                    return false;

                }

            }
        });
        binding.sellFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,AdCreateActivity.class));

            }
        });

    }

    private void showHomeFragment() {
        binding.toolbarTitleTv.setText("Home");

        //show Fragment
        HomeFragment fragment=new HomeFragment();
        FragmentTransaction transaction= getSupportFragmentManager().beginTransaction();
        transaction.replace(binding.fragmentsFl.getId(),fragment,"HomeFragment");
        transaction.commit();
    }

    private void showChatFragment(){
        binding.toolbarTitleTv.setText("Chat");
        ChatFragment fragment=new ChatFragment();
        FragmentTransaction transaction= getSupportFragmentManager().beginTransaction();
        transaction.replace(binding.fragmentsFl.getId(),fragment,"ChatFragment");
        transaction.commit();


    }

    private void showMyAdsFragment(){
        binding.toolbarTitleTv.setText("My Ads ");
        MyAdsFragment fragment=new MyAdsFragment();
        FragmentTransaction transaction= getSupportFragmentManager().beginTransaction();
        transaction.replace(binding.fragmentsFl.getId(),fragment,"MyAdsFragment");
        transaction.commit();


    }

    private void showAccountFragment(){
        binding.toolbarTitleTv.setText("Account");
        AccountFragment fragment=new AccountFragment();
        FragmentTransaction transaction= getSupportFragmentManager().beginTransaction();
        transaction.replace(binding.fragmentsFl.getId(),fragment,"AccountFragment");
        transaction.commit();

    }
    private void startLoginOptions(){
        startActivity(new Intent(this,LoginOptionActivity.class));
    }

}