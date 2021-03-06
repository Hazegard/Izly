package com.ad4screen.sdk.service.modules.d;

import android.content.Context;
import android.text.TextUtils;

import com.ad4screen.sdk.Log;
import com.ad4screen.sdk.common.e.c;
import com.ad4screen.sdk.common.h;
import com.ad4screen.sdk.d.d;
import com.ad4screen.sdk.d.d.b;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class f extends c {
    private final String c = "com.ad4screen.sdk.service.modules.common.TrackPushTask";
    private final String d = "content";
    private Context e;
    private String f;
    private String g;

    public f(Context context, String str) {
        super(context);
        this.e = context;
        this.f = str;
    }

    protected void a(String str) {
        Log.debug("TrackPushTask|Successfully tracked push");
        d.a(this.e).e(b.TrackPushWebservice);
    }

    protected void a(Throwable th) {
        Log.error("TrackPushTask|Push Tracking failed, will be retried later..");
    }

    protected boolean a() {
        i();
        j();
        if (TextUtils.isEmpty(this.f)) {
            Log.debug("TrackId is null, cannot send track push");
            return false;
        } else if (com.ad4screen.sdk.d.b.a(this.e).c() == null) {
            Log.warn("TrackPushTask|No SharedId, not tracking push");
            return false;
        } else if (d.a(this.e).c(b.TrackPushWebservice)) {
            try {
                JSONObject jSONObject = new JSONObject();
                JSONObject jSONObject2 = new JSONObject();
                JSONArray jSONArray = new JSONArray();
                if (this.f.contains("#")) {
                    jSONObject2.put("bid", this.f.split("#")[1]);
                    this.f = this.f.split("#")[0];
                }
                jSONObject2.put("trackId", this.f);
                jSONObject2.put("date", h.a());
                jSONObject2.put("ruuid", h.b());
                Log.debug("TrackPushTask", jSONObject2);
                jSONArray.put(jSONObject2);
                jSONObject.put("pushes", jSONArray);
                this.g = jSONObject.toString();
                return true;
            } catch (Throwable e) {
                Log.error("Push|Could not build message to send to Ad4Screen", e);
                return false;
            }
        } else {
            Log.debug("Service interruption on TrackPushTask");
            return false;
        }
    }

    public c b(c cVar) {
        f fVar = (f) cVar;
        try {
            JSONObject jSONObject = new JSONObject(d());
            JSONArray jSONArray = new JSONObject(fVar.d()).getJSONArray("pushes");
            JSONArray jSONArray2 = jSONObject.getJSONArray("pushes");
            for (int i = 0; i < jSONArray.length(); i++) {
                jSONArray2.put(jSONArray.get(i));
            }
            this.g = jSONObject.toString();
        } catch (Throwable e) {
            Log.internal("Failed to merge " + c(), e);
        } catch (Throwable e2) {
            Log.internal("Failed to merge " + c(), e2);
        }
        return this;
    }

    protected String c() {
        return b.TrackPushWebservice.toString();
    }

    protected String d() {
        return this.g;
    }

    public c e(String str) throws JSONException {
        super.e(str);
        JSONObject jSONObject = new JSONObject(str).getJSONObject("com.ad4screen.sdk.service.modules.common.TrackPushTask");
        if (!jSONObject.isNull("content")) {
            this.g = jSONObject.getString("content");
        }
        return this;
    }

    protected String e() {
        return d.a(this.e).a(b.TrackPushWebservice);
    }

    public /* synthetic */ Object fromJSON(String str) throws JSONException {
        return e(str);
    }

    public String getClassKey() {
        return "com.ad4screen.sdk.service.modules.common.TrackPushTask";
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject toJSON = super.toJSON();
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("content", this.g);
        toJSON.put("com.ad4screen.sdk.service.modules.common.TrackPushTask", jSONObject);
        return toJSON;
    }
}
