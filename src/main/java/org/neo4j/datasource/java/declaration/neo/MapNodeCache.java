package org.neo4j.datasource.java.declaration.neo;

import org.neo4j.graphdb.Node;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapNodeCache implements NodeCache {
    private final Map<String, Node> typeNodes = new ConcurrentHashMap<String, Node>(1000);
    private int hits;

    public Node getCachedNode(final String typeName) {
        final Node node = typeNodes.get(typeName);
        if (node != null) hits++;
        return node;
    }

    public void cacheNode(final String typeName, final Node newTypeNode) {
        typeNodes.put(typeName, newTypeNode);
    }

    @Override public String toString() {
        return "size " + typeNodes.size() + " hits " + hits;
    }
}
