package defpackage;

import android.os.Bundle;
import com.slidingmenu.lib.BuildConfig;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Locale;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParserException;

public final class ez {
    public static Bundle a(String str, String str2) throws IOException, XmlPullParserException, ParserConfigurationException, SAXException {
        HashMap hashMap = new HashMap();
        hashMap.put("language", Locale.getDefault().getLanguage());
        hashMap.put("userId", str);
        hashMap.put("sessionId", str2);
        String a = cr.a("https://soap.izly.fr/Service.asmx", "Service", "MoneyInCbCbList", "Service/MoneyInCbCbList", BuildConfig.VERSION_NAME, hashMap);
        XMLReader xMLReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        bd bdVar = new bd();
        xMLReader.setContentHandler(bdVar);
        xMLReader.parse(new InputSource(new StringReader(a)));
        Bundle bundle = new Bundle();
        if (bdVar.a != null) {
            bundle.putParcelable("fr.smoney.android.izly.extras.serverError", bdVar.a);
        }
        if (bdVar.b != null) {
            bundle.putParcelable("fr.smoney.android.izly.extras.moneyInCbCbListData", bdVar.b);
        }
        return bundle;
    }
}
