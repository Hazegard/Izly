package org.spongycastle.asn1.x509;

import java.util.Enumeration;
import java.util.Vector;
import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1EncodableVector;
import org.spongycastle.asn1.ASN1Object;
import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.ASN1OctetString;
import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.ASN1TaggedObject;
import org.spongycastle.asn1.DEROctetString;
import org.spongycastle.asn1.DERSequence;
import org.spongycastle.asn1.DERTaggedObject;
import org.spongycastle.asn1.DERUTF8String;

public class IetfAttrSyntax extends ASN1Object {
    public static final int VALUE_OCTETS = 1;
    public static final int VALUE_OID = 2;
    public static final int VALUE_UTF8 = 3;
    GeneralNames policyAuthority = null;
    int valueChoice = -1;
    Vector values = new Vector();

    private IetfAttrSyntax(ASN1Sequence aSN1Sequence) {
        int i;
        if (aSN1Sequence.getObjectAt(0) instanceof ASN1TaggedObject) {
            this.policyAuthority = GeneralNames.getInstance((ASN1TaggedObject) aSN1Sequence.getObjectAt(0), false);
            i = 1;
        } else if (aSN1Sequence.size() == 2) {
            this.policyAuthority = GeneralNames.getInstance(aSN1Sequence.getObjectAt(0));
            i = 1;
        } else {
            i = 0;
        }
        if (aSN1Sequence.getObjectAt(i) instanceof ASN1Sequence) {
            Enumeration objects = ((ASN1Sequence) aSN1Sequence.getObjectAt(i)).getObjects();
            while (objects.hasMoreElements()) {
                int i2;
                ASN1Primitive aSN1Primitive = (ASN1Primitive) objects.nextElement();
                if (aSN1Primitive instanceof ASN1ObjectIdentifier) {
                    i2 = 2;
                } else if (aSN1Primitive instanceof DERUTF8String) {
                    i2 = 3;
                } else if (aSN1Primitive instanceof DEROctetString) {
                    i2 = 1;
                } else {
                    throw new IllegalArgumentException("Bad value type encoding IetfAttrSyntax");
                }
                if (this.valueChoice < 0) {
                    this.valueChoice = i2;
                }
                if (i2 != this.valueChoice) {
                    throw new IllegalArgumentException("Mix of value types in IetfAttrSyntax");
                }
                this.values.addElement(aSN1Primitive);
            }
            return;
        }
        throw new IllegalArgumentException("Non-IetfAttrSyntax encoding");
    }

    public static IetfAttrSyntax getInstance(Object obj) {
        return obj instanceof IetfAttrSyntax ? (IetfAttrSyntax) obj : obj != null ? new IetfAttrSyntax(ASN1Sequence.getInstance(obj)) : null;
    }

    public GeneralNames getPolicyAuthority() {
        return this.policyAuthority;
    }

    public int getValueType() {
        return this.valueChoice;
    }

    public Object[] getValues() {
        int i;
        if (getValueType() == 1) {
            ASN1OctetString[] aSN1OctetStringArr = new ASN1OctetString[this.values.size()];
            for (i = 0; i != aSN1OctetStringArr.length; i++) {
                aSN1OctetStringArr[i] = (ASN1OctetString) this.values.elementAt(i);
            }
            return aSN1OctetStringArr;
        } else if (getValueType() == 2) {
            ASN1ObjectIdentifier[] aSN1ObjectIdentifierArr = new ASN1ObjectIdentifier[this.values.size()];
            for (i = 0; i != aSN1ObjectIdentifierArr.length; i++) {
                aSN1ObjectIdentifierArr[i] = (ASN1ObjectIdentifier) this.values.elementAt(i);
            }
            return aSN1ObjectIdentifierArr;
        } else {
            DERUTF8String[] dERUTF8StringArr = new DERUTF8String[this.values.size()];
            for (i = 0; i != dERUTF8StringArr.length; i++) {
                dERUTF8StringArr[i] = (DERUTF8String) this.values.elementAt(i);
            }
            return dERUTF8StringArr;
        }
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        if (this.policyAuthority != null) {
            aSN1EncodableVector.add(new DERTaggedObject(0, this.policyAuthority));
        }
        ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
        Enumeration elements = this.values.elements();
        while (elements.hasMoreElements()) {
            aSN1EncodableVector2.add((ASN1Encodable) elements.nextElement());
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector2));
        return new DERSequence(aSN1EncodableVector);
    }
}
