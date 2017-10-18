package com.cs3235.door.doorlockandroid.https;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

public class IvleObtainIdRequest extends StringRequest {
    private static final String TOKEN_PARAMETER = "&Token=";

    public IvleObtainIdRequest(HttpManager httpManager,
                                      String authToken,
                                      Response.Listener<String> listener,
                                      Response.ErrorListener errorListener) {
        super(Request.Method.POST, httpManager.getIvleUserGetIdUrl() + TOKEN_PARAMETER + authToken, listener, errorListener);
    }
}
