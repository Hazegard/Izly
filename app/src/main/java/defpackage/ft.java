package defpackage;

import android.os.Bundle;
import com.slidingmenu.lib.BuildConfig;
import fr.smoney.android.izly.SmoneyApplication;
import java.io.IOException;
import java.text.ParseException;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

public final class ft {
    public static Bundle a(String str, String str2, long j, String str3) throws IOException, JSONException, ParseException {
        String a = SmoneyApplication.c.a(str, str3);
        cq cqVar = new cq("https://rest.izly.fr/Service/PublicService.svc/rest/LeveeSupport");
        cqVar.a(BuildConfig.VERSION_NAME);
        cqVar.a("language", Locale.getDefault().getLanguage());
        cqVar.a("userId", str);
        cqVar.a("sessionId", str2);
        cqVar.a("passOTP", a);
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("supportId", j);
        cqVar.b(jSONObject.toString());
        return ak.a(cqVar.a(1));
    }
}
