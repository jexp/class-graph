package org.neo4j.datasource.java.analyser.stats;

public interface Statistics<E extends Enum<E>> {
    void reset(E token);

    void addTotal(E token);

    void inc(E token);

    String toString(E token);

    void done(E token);

    void start(E token);
}
