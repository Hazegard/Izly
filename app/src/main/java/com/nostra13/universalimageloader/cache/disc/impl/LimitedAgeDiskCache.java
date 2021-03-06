package com.nostra13.universalimageloader.cache.disc.impl;

import android.graphics.Bitmap;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DefaultConfigurationFactory;
import com.nostra13.universalimageloader.utils.IoUtils.CopyListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LimitedAgeDiskCache extends BaseDiskCache {
    private final Map<File, Long> loadingDates;
    private final long maxFileAge;

    public LimitedAgeDiskCache(File file, long j) {
        this(file, null, DefaultConfigurationFactory.createFileNameGenerator(), j);
    }

    public LimitedAgeDiskCache(File file, File file2, long j) {
        this(file, file2, DefaultConfigurationFactory.createFileNameGenerator(), j);
    }

    public LimitedAgeDiskCache(File file, File file2, FileNameGenerator fileNameGenerator, long j) {
        super(file, file2, fileNameGenerator);
        this.loadingDates = Collections.synchronizedMap(new HashMap());
        this.maxFileAge = 1000 * j;
    }

    private void rememberUsage(String str) {
        File file = getFile(str);
        long currentTimeMillis = System.currentTimeMillis();
        file.setLastModified(currentTimeMillis);
        this.loadingDates.put(file, Long.valueOf(currentTimeMillis));
    }

    public void clear() {
        super.clear();
        this.loadingDates.clear();
    }

    public File get(String str) {
        File file = super.get(str);
        if (file != null && file.exists()) {
            Object obj;
            Long valueOf;
            Long l = (Long) this.loadingDates.get(file);
            if (l == null) {
                obj = null;
                valueOf = Long.valueOf(file.lastModified());
            } else {
                valueOf = l;
                int i = 1;
            }
            if (System.currentTimeMillis() - valueOf.longValue() > this.maxFileAge) {
                file.delete();
                this.loadingDates.remove(file);
            } else if (obj == null) {
                this.loadingDates.put(file, valueOf);
                return file;
            }
        }
        return file;
    }

    public boolean remove(String str) {
        this.loadingDates.remove(getFile(str));
        return super.remove(str);
    }

    public boolean save(String str, Bitmap bitmap) throws IOException {
        boolean save = super.save(str, bitmap);
        rememberUsage(str);
        return save;
    }

    public boolean save(String str, InputStream inputStream, CopyListener copyListener) throws IOException {
        boolean save = super.save(str, inputStream, copyListener);
        rememberUsage(str);
        return save;
    }
}
