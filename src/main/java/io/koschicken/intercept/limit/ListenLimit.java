package io.koschicken.intercept.limit;

public class ListenLimit {
    /**
     * 有效时间
     */
    private final long effectiveTime;
    /**
     * 失效时间
     */
    private volatile long invalidTime = 0;

    public ListenLimit(long effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    /**
     * 检测是否已过期
     * 检测当前是否已经超过失效时间，且如果超过，刷新时间
     *
     * @return 是否过期
     */
    public boolean expired() {
        final long now = System.currentTimeMillis();
        if (invalidTime < now) {
            invalidTime = now + effectiveTime;
            return true;
        }
        return false;
    }

    public int remaining() {
        final long now = System.currentTimeMillis();
        if (expired()) {
            return 0;
        } else {
            return (int) (invalidTime - now) / 1000;
        }
    }
}
