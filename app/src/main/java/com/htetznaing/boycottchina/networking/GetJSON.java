package com.htetznaing.boycottchina.networking;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class GetJSON {
    private final RequestQueue requestQueue;
    private OnDone done;

    public GetJSON(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public void request(String url){
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (done!=null)
                    done.onTaskCompleted(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (done!=null)
                    done.onTaskFailure(error.getMessage());
            }
        });

        requestQueue.add(stringRequest);
    }

    public GetJSON listen(OnDone done){
        this.done = done;
        return this;
    }

    public interface OnDone{
        void onTaskCompleted(String result);
        void onTaskFailure(String error);
    }
}
