package com.google.android.gms.internal;

import com.google.android.gms.ads.internal.util.client.zzb;
import com.google.android.gms.ads.purchase.InAppPurchase;

@zzhb
public class zzgl implements InAppPurchase {
    private final zzgc zzFL;

    public zzgl(zzgc com_google_android_gms_internal_zzgc) {
        this.zzFL = com_google_android_gms_internal_zzgc;
    }

    public String getProductId() {
        try {
            return this.zzFL.getProductId();
        } catch (Throwable e) {
            zzb.zzd("Could not forward getProductId to InAppPurchase", e);
            return null;
        }
    }

    public void recordPlayBillingResolution(int i) {
        try {
            this.zzFL.recordPlayBillingResolution(i);
        } catch (Throwable e) {
            zzb.zzd("Could not forward recordPlayBillingResolution to InAppPurchase", e);
        }
    }

    public void recordResolution(int i) {
        try {
            this.zzFL.recordResolution(i);
        } catch (Throwable e) {
            zzb.zzd("Could not forward recordResolution to InAppPurchase", e);
        }
    }
}
