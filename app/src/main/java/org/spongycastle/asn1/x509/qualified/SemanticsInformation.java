package org.spongycastle.asn1.x509.qualified;

import java.util.Enumeration;
import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1EncodableVector;
import org.spongycastle.asn1.ASN1Object;
import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.DERObjectIdentifier;
import org.spongycastle.asn1.DERSequence;
import org.spongycastle.asn1.x509.GeneralName;

public class SemanticsInformation extends ASN1Object {
    private GeneralName[] nameRegistrationAuthorities;
    private ASN1ObjectIdentifier semanticsIdentifier;

    public SemanticsInformation(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this.semanticsIdentifier = aSN1ObjectIdentifier;
        this.nameRegistrationAuthorities = null;
    }

    public SemanticsInformation(ASN1ObjectIdentifier aSN1ObjectIdentifier, GeneralName[] generalNameArr) {
        this.semanticsIdentifier = aSN1ObjectIdentifier;
        this.nameRegistrationAuthorities = generalNameArr;
    }

    private SemanticsInformation(ASN1Sequence aSN1Sequence) {
        Enumeration objects = aSN1Sequence.getObjects();
        if (aSN1Sequence.size() <= 0) {
            throw new IllegalArgumentException("no objects in SemanticsInformation");
        }
        Object nextElement = objects.nextElement();
        if (nextElement instanceof ASN1ObjectIdentifier) {
            this.semanticsIdentifier = DERObjectIdentifier.getInstance(nextElement);
            nextElement = objects.hasMoreElements() ? objects.nextElement() : null;
        }
        if (nextElement != null) {
            ASN1Sequence instance = ASN1Sequence.getInstance(nextElement);
            this.nameRegistrationAuthorities = new GeneralName[instance.size()];
            for (int i = 0; i < instance.size(); i++) {
                this.nameRegistrationAuthorities[i] = GeneralName.getInstance(instance.getObjectAt(i));
            }
        }
    }

    public SemanticsInformation(GeneralName[] generalNameArr) {
        this.semanticsIdentifier = null;
        this.nameRegistrationAuthorities = generalNameArr;
    }

    public static SemanticsInformation getInstance(Object obj) {
        return obj instanceof SemanticsInformation ? (SemanticsInformation) obj : obj != null ? new SemanticsInformation(ASN1Sequence.getInstance(obj)) : null;
    }

    public GeneralName[] getNameRegistrationAuthorities() {
        return this.nameRegistrationAuthorities;
    }

    public ASN1ObjectIdentifier getSemanticsIdentifier() {
        return this.semanticsIdentifier;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        if (this.semanticsIdentifier != null) {
            aSN1EncodableVector.add(this.semanticsIdentifier);
        }
        if (this.nameRegistrationAuthorities != null) {
            ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
            for (ASN1Encodable add : this.nameRegistrationAuthorities) {
                aSN1EncodableVector2.add(add);
            }
            aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector2));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
