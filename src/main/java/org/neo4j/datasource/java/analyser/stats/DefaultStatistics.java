package org.neo4j.datasource.java.analyser.stats;

import java.util.EnumMap;
import java.util.Map;

public class DefaultStatistics<E extends Enum<E>> implements Statistics<E> {
    private final Map<E, TimeInfo> infos;

    public DefaultStatistics(final Class<E> tokenType) {
        infos = new EnumMap<E, TimeInfo>(tokenType);
        for (E token : tokenType.getEnumConstants()) {
            infos.put(token, new TimeInfo(token.name()));
        }
    }

    public void reset(final E token) {
        infos.get(token).reset();
    }

    public void addTotal(final E token) {
        infos.get(token).addTotal();
    }

    public void inc(final E token) {
        infos.get(token).inc();
    }

    public String toString(final E token) {
        return infos.get(token).toString();
    }

    public void done(final E token) {
        final TimeInfo timeInfo = infos.get(token);
        if (timeInfo.addTotal() != -1) {
            timeInfo.inc();
        }
    }

    static class TimeInfo {
        private long start;
        private int count;
        private long delta;
        private long totalInMillis;

        private final String token;
        private static final int NANOSECONDS = 1000 * 1000;

        public TimeInfo(final String token) {
            this.token = token;
        }

        public void start() {
            start = System.nanoTime();
        }

        public void reset() {
            start = -1;
        }

        public long addTotal() {
            delta=-1;
            if (start >= 0) {
                delta = (System.nanoTime() - start) / NANOSECONDS;
                totalInMillis += delta;
            }
            return delta;
        }

        public void inc() {
            count++;
        }

        public String toString() {
            return String.format("%s: last %d ms total: %d ms / %d = %.2f ms ",
                    token, delta, totalInMillis, count, (float) totalInMillis / count);
        }
    }

    public void start(final E token) {
        infos.get(token).start();
    }
}
