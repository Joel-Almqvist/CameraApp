package com.example.joel.myapplication;

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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChallengesActivity extends AppCompatActivity {
    int photo_intent_id = 1;
    int challengeID;
    String mImageFileLocation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        final String username = getIntent().getStringExtra("username");
        ListView challengeList= (ListView) findViewById(R.id.challengeListView);
        getActiveChallenges(this,username, challengeList);
        final TextView userInputTV = (TextView) findViewById(R.id.userInputChallengeTextView);

        setTitle("My Active Challenges");

        final Button challengeButton = (Button) findViewById(R.id.challengeButton);
        challengeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String challengedUsername = userInputTV.getText().toString();
                if(challengedUsername.equals(username)){
                    Toast.makeText(getApplicationContext(), "Can't challenge yourself...", Toast.LENGTH_SHORT).show();
                }else {
                    checkForActiveChallenge(getApplicationContext(), username, challengedUsername);
                }
            }});
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


    public void getActiveChallenges (final Context context, final String username, final ListView ChallengesListView){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET,
                "http://cameraapp-messagesserver.openshift.ida.liu.se/challenges/all_active/"+username,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            JSONArray responseMessage = response.getJSONArray("respons");
                            JSONArray givenID = response.getJSONArray("id_list");

                            final ArrayList<String> allOldChallenges = new ArrayList<String>();
                            final ArrayList<Integer> challengesIDArray = new ArrayList<Integer>();

                            if(responseMessage != null){
                                for (int i=0; i<responseMessage.length(); i++){
                                    allOldChallenges.add(responseMessage.get(i).toString());
                                   challengesIDArray.add(Integer.parseInt(givenID.get(i).toString()));
                                }
                            }
                            final ArrayAdapter challengesAdapter = new ArrayAdapter<String>(getBaseContext(),
                                    android.R.layout.simple_list_item_1, allOldChallenges);
                            ChallengesListView.setAdapter(challengesAdapter);

                            ChallengesListView.setOnItemClickListener(
                                    new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            challengeID = challengesIDArray.get(position);
                                            checkIfImagePosted(context,username,challengeID);
                                        }
                                    });

                        }catch(JSONException e){
                            e.printStackTrace();

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Connection to server failed", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(sr);
    }

    public void checkIfImagePosted(final Context context, final String username, final int localChallengeID) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET,
                "http://cameraapp-messagesserver.openshift.ida.liu.se/image/has_posted_image/"
                        + username+"/"+Integer.toString(localChallengeID),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Boolean hasPostedPictureBefore = response.getBoolean("respons");
                            if(hasPostedPictureBefore){
                                Intent postGameIntent = new Intent(getApplicationContext(), PostGameActivity.class);
                                postGameIntent.putExtra("username",username);
                                postGameIntent.putExtra("challengeID",localChallengeID);
                                startActivity(postGameIntent);
                            }else{
                                takePhoto();
                            }
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Connection to server failed", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(sr);
    }

    public void checkForActiveChallenge(final Context context, final String username, final String friendsname) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET,
                "http://cameraapp-messagesserver.openshift.ida.liu.se/challenges/active/" + username+"/"+friendsname,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int responseChallengeID = response.getInt("challengeid");
                            if(response.getInt("challengeid") == -1){
                                challengeID = response.getInt("newchallengeid");
                                takePhoto();
                            }else{
                                Intent postGameIntent = new Intent(getApplicationContext(), PostGameActivity.class);
                                postGameIntent.putExtra("username",username);
                                postGameIntent.putExtra("challengeID",responseChallengeID);
                                startActivity(postGameIntent);
                            }
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(sr);
    }


    public void sendImage(final Context context, final String username, final Integer challengeID, String encodedImage){
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject object = new JSONObject();
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
                    Toast.makeText(context, "Image Upload", Toast.LENGTH_SHORT).show();
                Intent postGameIntent = new Intent(getApplicationContext(), PostGameActivity.class);
                postGameIntent.putExtra("username",username);
                postGameIntent.putExtra("challengeID",challengeID);
                startActivity(postGameIntent);
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
}
