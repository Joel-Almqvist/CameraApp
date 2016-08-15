package com.example.joel.myapplication;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class VoteScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_screen);
        setTitle("Vote");

        final String challenger1 = getIntent().getStringExtra("challenger1");
        final String challenger2 = getIntent().getStringExtra("challenger2");
        final String username = getIntent().getStringExtra("username");
        final int challengeID = getIntent().getIntExtra("challengeID",-1);

        Button voteButtonUser1 = (Button) findViewById(R.id.buttonVoteUser1);
        Button voteButtonUser2 = (Button) findViewById(R.id.buttonVoteUser2);
        voteButtonUser1.setText("Vote for \n"+challenger1);
        voteButtonUser2.setText("Vote for \n"+challenger2);
        final Button voteButton1 = (Button)findViewById(R.id.buttonVoteUser1);
        voteButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            vote(getApplicationContext(),username,challengeID,challenger1);
         }
        });

        final Button voteButton2 = (Button)findViewById(R.id.buttonVoteUser2);
        voteButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vote(getApplicationContext(),username,challengeID,challenger2);
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

    public void vote(final Context context, final String username, Integer challengeID, String friendsname){
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject object = new JSONObject();
        try{
            object.put("friendsname", friendsname);
            object.put("username",username);
            object.put("challengeID", challengeID);
        }catch(JSONException e){e.printStackTrace();}
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST,
                "http://cameraapp-messagesserver.openshift.ida.liu.se/votes/vote",
                object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Toast.makeText(context, response.get("respons").toString(), Toast.LENGTH_SHORT).show();
                }catch(JSONException e){e.printStackTrace();}
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }){
        };
        queue.add(sr);
    }
}
