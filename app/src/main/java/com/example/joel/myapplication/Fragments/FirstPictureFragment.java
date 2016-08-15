package com.example.joel.myapplication.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.joel.myapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

public class FirstPictureFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first_picture, container, false);

        ImageView imageView = (ImageView) view.findViewById(R.id.imageViewLoss);
        TextView nameView = (TextView) view.findViewById(R.id.textViewChallenger1);

        int challengeID = getActivity().getIntent().getIntExtra("challengeID",-1);
        getImage(getActivity(), challengeID, imageView, nameView);
        return view;

    }


    public void getImage (final Context context, final int challengeID,
                          final ImageView iw, final TextView nameView){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET,
                "http://cameraapp-messagesserver.openshift.ida.liu.se/image/get_first/"+
                        String.valueOf(challengeID),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String username = response.get("username").toString();
                            nameView.setText(username);



                            if(!response.getString("respons").equals("no_image")) {
                                String encodedImage = response.get("respons").toString();
                                byte[] bytearray = Base64.decode(encodedImage, Base64.URL_SAFE);
                                Bitmap bm = BitmapFactory.decodeByteArray(bytearray, 0, bytearray.length);
                                iw.setImageBitmap(bm);
                            }

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
}
