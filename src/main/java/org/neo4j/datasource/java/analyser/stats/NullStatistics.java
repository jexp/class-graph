package org.neo4j.datasource.java.analyser.stats;

public class NullStatistics<E extends Enum<E>> implements Statistics<E> {
    public void reset(final Enum token) {
    }

    public void addTotal(final Enum token) {
    }

    public void inc(final Enum token) {
    }

    public String toString(final Enum token) {
        return "";
    }

    public void done(final Enum token) {
    }

    public void start(final Enum token) {
    }

    @Override public String toString() {
        return "";
    }
}
