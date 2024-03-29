package com.example.a125_final;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import cn.leancloud.AVUser;

public class LeaderboardActivity extends AppCompatActivity {

    private int score;

    private final String appid = "4MyF250OP26euKGPTFQxc0pE-MdYXbMMI";

    private final String appkey = "Qb17eoMfxfi4LpjinPUTCFS1";

    private AVUser user = new AVUser();

    private String token;

    private final String url = "https://Qb17eoMf.api.lncldglobal.com/1.1/leaderboard/users/self/statistics";

    private final String boardUrl = "https://Qb17eoMf.api.lncldglobal.com/1.1/leaderboard/leaderboards/" +
            "Flip125_Leaderboard/ranks?maxResultsCount=50&includeUser=username";

    private RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        queue = Volley.newRequestQueue(this);
        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0);
        user.setUsername(intent.getStringExtra("username"));
        user.setPassword(intent.getStringExtra("password"));
        token = AVUser.getCurrentUser().getSessionToken();
    }

    @Override
    public void onResume() {
        super.onResume();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You got: " + score + "!");
        builder.setTitle("Congratulations!");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        updateScore();
        postLeaderBoard();
    }

    /**
     * update the user's new score passed from the main activity.
     */
    private void updateScore() {
        JSONArray ja = new JSONArray();
        JSONObject js = new JSONObject();
        try {
            js.put("statisticName", "Flip125_Leaderboard");
            js.put("statisticValue", score);
            ja.put(js);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST, url, ja, result -> { },
                error -> {
            System.out.println(error);
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("X-LC-Id", appid);
                params.put("X-LC-Key", appkey);
                params.put("X-LC-Session", token);
                params.put("Content-Type", "application/json");
                return params;
            }
            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        queue.add(request);
    }

    /**
     * should post the leaderboard with a chunked view(under construction).
     */
    private void postLeaderBoard() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, boardUrl, null, r -> {
            try {
                JSONArray array = r.getJSONArray("results");
                //TextView leaderboard =  findViewById(R.id.leaderboard);
                //parent.removeAllViews();
                LinearLayout parent = findViewById(R.id.boardlayout);
                parent.removeAllViews();
                for (int i = -1; i < array.length(); i++) {
                    View chunk = getLayoutInflater().inflate(R.layout.chunk_leaderboard, parent, false);
                    TextView usernameText = chunk.findViewById(R.id.uesrname);
                    TextView rankText = chunk.findViewById(R.id.rank);
                    TextView scoreText = chunk.findViewById(R.id.score);
                    if (i == -1) {
                        rankText.setText("Rank");
                        usernameText.setText("Username");
                        scoreText.setText("Score");
                        parent.addView(chunk);
                    } else {
                        JSONObject result = array.getJSONObject(i);
                        int rank = result.getInt("rank");
                        int score = result.getInt("statisticValue");
                        JSONObject user = result.getJSONObject("user");
                        String username = user.getString("username");
                        rankText.setText(Integer.toString(rank + 1));
                        usernameText.setText(username);
                        scoreText.setText(Integer.toString(score));
                        parent.addView(chunk);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            /* do some thing*/
            /* Only care about the username, score and their rank, I didn't do the rest when signing in.
            *sample result:
            * {
                "results": [
                {
                  "user": {
                    "objectId": "5ab9f63910b03545c0f529df",
                    "__type": "Pointer",
                    "username": "Superman",
                    "avatar_url": "http://example.com/avatar.png",
                    "className": "_User"
                  },
                  "rank": 0,
                  "statisticName": "wins",
                  "statisticValue": 42,
                  "statistics": [{
                    "statisticName": "world",
                    "statisticValue": null,
                    "version": null,
                  }]
                }, {
                  "user": {},
                  "rank": 1,
                } ...
                ]
              }
             */
        }, error -> {
            System.out.println("error in getboard");
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("X-LC-Id", appid);
                params.put("X-LC-Key", appkey);
                return params;
            }
        };
        queue.add(request);
    }
}
