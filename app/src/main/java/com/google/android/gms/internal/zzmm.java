package com.google.android.gms.internal;

import android.support.v4.util.ArrayMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

public class zzmm<E> extends AbstractSet<E> {
    private final ArrayMap<E, E> zzanZ;

    public zzmm() {
        this.zzanZ = new ArrayMap();
    }

    public zzmm(int i) {
        this.zzanZ = new ArrayMap(i);
    }

    public zzmm(Collection<E> collection) {
        this(collection.size());
        addAll(collection);
    }

    public boolean add(E e) {
        if (this.zzanZ.containsKey(e)) {
            return false;
        }
        this.zzanZ.put(e, e);
        return true;
    }

    public boolean addAll(Collection<? extends E> collection) {
        return collection instanceof zzmm ? zza((zzmm) collection) : super.addAll(collection);
    }

    public void clear() {
        this.zzanZ.clear();
    }

    public boolean contains(Object obj) {
        return this.zzanZ.containsKey(obj);
    }

    public Iterator<E> iterator() {
        return this.zzanZ.keySet().iterator();
    }

    public boolean remove(Object obj) {
        if (!this.zzanZ.containsKey(obj)) {
            return false;
        }
        this.zzanZ.remove(obj);
        return true;
    }

    public int size() {
        return this.zzanZ.size();
    }

    public boolean zza(zzmm<? extends E> com_google_android_gms_internal_zzmm__extends_E) {
        int size = size();
        this.zzanZ.putAll(com_google_android_gms_internal_zzmm__extends_E.zzanZ);
        return size() > size;
    }
}
