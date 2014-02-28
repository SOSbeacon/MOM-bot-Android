package org.cnc.msrobot.utils;

import android.graphics.Bitmap;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.HONEYCOMB_MR1;
import android.support.v4.util.LruCache;

import com.nostra13.universalimageloader.cache.memory.MemoryCacheAware;

import java.util.Collection;
import java.util.HashSet;

/**
 * Lru cache
 * 
 * @author ThanhLCM
 */
public class LruBitmapCache implements MemoryCacheAware<String, Bitmap> {

    private LruCache<String, Bitmap> map;

    private final int maxSize;

    /** @param maxSize Maximum sum of the sizes (Kb) of the Bitmaps in this cache */
    public LruBitmapCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.maxSize = maxSize;
        if (map != null) {
            map.evictAll();
        }
        map = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                int size = 1;
                if (SDK_INT > HONEYCOMB_MR1) {
                    size = bitmap.getByteCount();
                } else {
                    size = bitmap.getRowBytes() * bitmap.getHeight();
                }
                // return in Kb
                return size == 0 ? 1 : size / 1024;
            }
        };
    }

    /**
     * Returns the Bitmap for {@code key} if it exists in the cache. If a Bitmap was returned, it is moved to the head
     * of the queue. This returns null if a Bitmap is not cached.
     */
    @Override
    public final Bitmap get(String key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        synchronized (this) {
            return map.get(key);
        }
    }

    /** Caches {@code Bitmap} for {@code key}. The Bitmap is moved to the head of the queue. */
    @Override
    public final boolean put(String key, Bitmap value) {
        if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }

        synchronized (this) {
            map.put(key, value);
        }

        return true;
    }

    /** Removes the entry for {@code key} if it exists. */
    @Override
    public final void remove(String key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        synchronized (this) {
            map.remove(key);
        }
    }

    @Override
    public Collection<String> keys() {
        synchronized (this) {
            return new HashSet<String>(map.snapshot().keySet());
        }
    }

    @Override
    public void clear() {
        map.evictAll();
    }

    @Override
    public synchronized final String toString() {
        return String.format("LruCache[maxSize=%d]", maxSize);
    }
}