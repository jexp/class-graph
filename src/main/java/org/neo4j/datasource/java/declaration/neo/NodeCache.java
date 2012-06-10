package org.neo4j.datasource.java.declaration.neo;

import org.neo4j.graphdb.Node;

public interface NodeCache {
    Node getCachedNode(String typeName);

    void cacheNode(String typeName, Node newTypeNode);
}
