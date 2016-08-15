package com.example.joel.myapplication;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PostGameActivity extends AppCompatActivity {
    String firstUsername;
    String secondUsername;
    int challengeID;
    String mImageFileLocation = "";
    static final int photo_intent_id = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_game);
        challengeID = getIntent().getIntExtra("challengeID",-1);
        setTitle("View Challenge");

        TextView textViewVotes1 = (TextView) findViewById(R.id.textViewUser1Votes);
        TextView textViewVotes2 = (TextView) findViewById(R.id.textViewUser2Votes);
        getVotes(this,challengeID,textViewVotes1,textViewVotes2);

        Button newPicturePutton = (Button) findViewById(R.id.newPictureButton);
        newPicturePutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isChallengeActive(getApplicationContext(),challengeID);
            }
        });

        FragmentManager fm = getFragmentManager();
        addShowHideListener(R.id.imageButtonSwap, fm.findFragmentById(R.id.fragment_win),fm.findFragmentById(R.id.fragment_loss));

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment fragment_loss = fm.findFragmentById(R.id.fragment_loss);
        ft.hide(fragment_loss);
        ft.commit();
    }


    void addShowHideListener(int buttonId, final Fragment fragmentVariable, final Fragment fragmentVariable2) {
        final ImageButton button = (ImageButton)findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in,
                        android.R.animator.fade_out);
                if (fragmentVariable.isHidden()) {
                    ft.show(fragmentVariable);
                    ft.hide(fragmentVariable2);
                } else {
                    ft.hide(fragmentVariable);
                    ft.show(fragmentVariable2);
                }
                ft.commit();
            }
        });
    }

    private void takePhoto() {
        Intent callCameraIntent = new Intent();
        callCameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File photoFile = createImageFile();
            callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile( photoFile));
            startActivityForResult(callCameraIntent, photo_intent_id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == photo_intent_id && resultCode == RESULT_OK){
            Toast.makeText(this, "Picture taken successfully", Toast.LENGTH_SHORT).show();
            Bitmap photoCapturedBitmap = BitmapFactory.decodeFile(mImageFileLocation);
            photoCapturedBitmap = resizeBitmap(photoCapturedBitmap, 2000);

            photoCapturedBitmap = resizeBitmap(photoCapturedBitmap, 2000);
            Bitmap bm = resizeBitmap(photoCapturedBitmap, 500);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG,40,baos);

            byte[] byteImage_photo = baos.toByteArray();
            String encodedImage = Base64.encodeToString(byteImage_photo,Base64.URL_SAFE);
            String username = getIntent().getStringExtra("username");
            sendImage(this,username, challengeID, encodedImage);

            Intent postGameIntent = new Intent(getApplicationContext(), PostGameActivity.class);
            postGameIntent.putExtra("username",username);
            postGameIntent.putExtra("challengeID",challengeID);
            startActivity(postGameIntent);
        }
        else{Toast.makeText(this, "Picture not taken", Toast.LENGTH_SHORT).show();}
    }


    public File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp +"_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDirectory);
        mImageFileLocation = image.getAbsolutePath();
        return image;
    }

    public Bitmap resizeBitmap(Bitmap image, int maxSize){
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public void sendImage(final Context context, final String username, final Integer challengeID, String encodedImage){
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject object = new JSONObject();
        Toast.makeText(context, challengeID.toString(), Toast.LENGTH_SHORT).show();
        try{
            object.put("imageJSON", encodedImage);
            object.put("username",username);
            object.put("challengeID", challengeID);
        }catch(JSONException e){e.printStackTrace();}
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST,
                "http://cameraapp-messagesserver.openshift.ida.liu.se/image/post",
                object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(firstUsername.equals(username)){
                    ImageView imageViewOne = (ImageView) findViewById(R.id.imageViewLoss);
                    getImage(context,challengeID,imageViewOne);
                }else{
                    ImageView imageViewTwo = (ImageView) findViewById(R.id.imageViewWin);
                    getImage(context,challengeID,imageViewTwo);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show();
            }
        }){

        };
        queue.add(sr);
    }


    public void isChallengeActive(final Context context,final Integer challengeID){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET,
                "http://cameraapp-messagesserver.openshift.ida.liu.se/challenges/is_active/"
                +challengeID.toString(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                if (response.getBoolean("respons")) {
                    firstUsername = response.getString("user1");
                    secondUsername = response.getString("user2");
                    takePhoto();
                }else{
                    Toast.makeText(getApplicationContext(), "Can't change picture for old challenge", Toast.LENGTH_SHORT).show();
                }
                }catch (JSONException e){e.printStackTrace();}
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(sr);
    }

    public void getImage (final Context context, final int challengeID,
                          final ImageView iw){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET,
                "http://cameraapp-messagesserver.openshift.ida.liu.se/image/get_first/"+
                        String.valueOf(challengeID),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String encodedImage = response.get("respons").toString();
                            byte[] bytearray = Base64.decode(encodedImage, Base64.URL_SAFE);
                            Bitmap bm = BitmapFactory.decodeByteArray(bytearray, 0, bytearray.length);
                            Toast.makeText(context, bm.toString(), Toast.LENGTH_SHORT).show();
                            iw.setImageBitmap(bm);
                        }
                        catch(JSONException e){
                            e.printStackTrace();
                            Toast.makeText(context, "Connection to server failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(sr);
    }

    public void getVotes(final Context context,final Integer challengeID, final TextView userOneTW,
                         final TextView userTwoTW){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET,
                "http://cameraapp-messagesserver.openshift.ida.liu.se/votes/get_votes/"
                        +challengeID.toString(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    int userOneVotes = response.getInt("user_one_votes");
                    int userTwoVotes = response.getInt("user_two_votes");
                    String usernameOne = response.getString("username_1");
                    String usernameTwo = response.getString("username_2");

                    userOneTW.setText(Integer.toString(userOneVotes)+" Vote(s) "+"\n"+"for "+usernameOne);
                    userTwoTW.setText(Integer.toString(userTwoVotes)+" Vote(s) "+"\n"+"for "+usernameTwo);
                }catch (JSONException e){e.printStackTrace();}
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(sr);
    }
}

