package com.example.application.marketox;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class Utils {

    public  static  final  String MESSAGE_TYPE_TEXT="TEXT";
    public  static  final  String MESSAGE_TYPE_IMAGE="IMAGE";


    public static final String AD_STATUS_AVAILABLE="AVAILABLE";
    public static final String AD_STATUS_SOLD="SOLD";



    public static  final String[] categories={
            "Mobiles",
            "Computer",
            "Electronics and Home Appliances",
            "Vehicles",
            "Furniture ",
            "Sports",
            "Books",
            "Others"

    };
    public static  final int[] categoryIcons={
            R.drawable.ic_category_mobiles,
            R.drawable.ic_category_computer,
            R.drawable.ic_category_electronics,
            R.drawable.ic_category_vehicle,
            R.drawable.ic_category_furniture,
            R.drawable.ic_category_fashion,
            R.drawable.ic_category_books,
            R.drawable.ic_category_sports,
            R.drawable.ic_category_pets,
            R.drawable.ic_category_business,
            R.drawable.ic_category_agriculture

    };
    public static  final String[] conditions={
            "New",
            "Used",
            "Refurbished"
    };







    public static  void toast(Context context,String message){
        Toast.makeText(context,message, Toast.LENGTH_SHORT).show();


    }

    public static  long getTimestamp(){
        return System.currentTimeMillis();


    }


    public static String formatTimestampDateTime(long timestamp){
        Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);
        String date= android.text.format.DateFormat.format("dd/MM/yyyy",calendar).toString();
        return date;
    }
    public static String formatTimestampDate(long timestamp){
        Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);
        String date= android.text.format.DateFormat.format("dd/MM/yyyy",calendar).toString();
        return date;
    }

    public static  void addToFavourite(Context context,String adId){

        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()==null){
            Utils.toast(context,"Your not logged in!");
        }else{
            long timestamp=Utils.getTimestamp();

            HashMap<String,Object>hashMap=new HashMap<>();
            hashMap.put("timestamp",timestamp);
            hashMap.put("adId",adId);

            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favourites").child(adId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Utils.toast(context,"Added to Favourites ");

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utils.toast(context,"Failed to add to favourites due to "+e.getMessage());

                        }
                    });

        }


    }
    public static void  removeFromFavourite(Context context,String adId){
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()==null){
            Utils.toast(context,"Your not logged in!");
        }else{
            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favourites").child(adId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Utils.toast(context,"Removed from Favourites ");


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utils.toast(context,"Failed to remove from favourites due to "+e.getMessage());

                        }
                    });
        }
    }

    public  static  String chatPath(String receiptUid,String yourUid){
        String[] arrayUids=new String[]{receiptUid,yourUid};
        Arrays.sort(arrayUids);

        String chatPath=arrayUids[0]+"_"+arrayUids[1];

        return  chatPath;


    }
    public  static  void callIntent(Context context,String phone){
        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("tel:"+Uri.encode(phone)));
        context.startActivity(intent);
    }
    public  static  void smsIntent(Context context,String phone){
        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms",Uri.encode(phone),null));
        context.startActivity(intent);

    }
    public  static  void mapIntent(Context context,double latitude,double longitude){
        Uri gmmIntentUri=Uri.parse("https://maps.google.com/maps?daddr="+latitude+","+longitude);
        Intent mapIntent=new Intent(Intent.ACTION_VIEW,gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if(mapIntent.resolveActivity(context.getPackageManager())!=null){
            context.startActivity(mapIntent);
        }else{
            Utils.toast(context,"Please install Google Maps");

        }

    }
}
