package org.neo4j.datasource.java.declaration.neo;

import org.neo4j.datasource.java.analyser.stats.DefaultStatistics;
import org.neo4j.datasource.java.analyser.stats.Statistics;
import org.neo4j.datasource.java.analyser.ClassInspectUtils;
import org.neo4j.graphdb.*;
import org.neo4j.kernel.Traversal;

import static org.neo4j.datasource.java.declaration.neo.TypeNodeFinder.StatTokens.method;
import static org.neo4j.datasource.java.declaration.neo.TypeNodeFinder.StatTokens.create;

import java.util.Iterator;

public class TypeNodeFinder {
    NodeCache nodeCache = new MapNodeCache();
    private final GraphDatabaseService graph;
    private Node typesNode;

    enum StatTokens {method, create}
    Statistics<StatTokens> statistics= // new NullStatistics<StatTokens>();
    new DefaultStatistics<StatTokens>(StatTokens.class);

    public TypeNodeFinder(final GraphDatabaseService graph) {
        this.graph = graph;
        // caching of main secondary node for types
        this.typesNode = getTypesNode();
    }

    public Node getOrCreateTypeNode(String typeName) {
        typeName = ClassInspectUtils.toSlashName(typeName);
        typeName = ClassInspectUtils.arrayToClassName(typeName);
        // typeName = typeName.intern();
        statistics.start(method);
        statistics.reset(create);
        try {
            final Node cachedNode = nodeCache.getCachedNode(typeName);
            if (cachedNode != null) {
                return cachedNode;
            }
            statistics.start(create);
            // TODO no lookup during construction -> too expensive
            // parallelize only with concurrent cache and node removal on duplication
            // final Node typeNode = getTypeNode(typesNode, typeName);
            // if (typeNode != null) return typeNode;
            final Node newTypeNode = createTypeNode(typesNode, typeName);
            nodeCache.cacheNode(typeName, newTypeNode);
            return newTypeNode;
        } finally {
            statistics.done(create);
            statistics.done(method);
        }
    }

    public String toString() {
        return statistics.toString(method)+ statistics.toString(create)+ " cache "+nodeCache;
    }

    public Node createTypeNode(final Node typesNode, final String typeName) {
        final Node typeNode = graph.createNode();
        typeNode.setProperty("name", typeName);
        typesNode.createRelationshipTo(typeNode, ClassRelations.TYPE);
        return typeNode;
    }

    public Node getTypeNode(String typeName) {
        final String slashName = ClassInspectUtils.toSlashName(typeName);
        for (Relationship relationship : typesNode.getRelationships(ClassRelations.TYPE, Direction.OUTGOING)) {
            final Node typeNode = relationship.getEndNode();
            if (typeNode.hasProperty("name") && typeNode.getProperty("name").equals(slashName)) {
                return typeNode;
            }
        }
        return null;
    }

    public Node getTypesNode() {
        final Node referenceNode = graph.getReferenceNode();
        final Relationship typesRelation = referenceNode.getSingleRelationship(ClassRelations.ALL_TYPES, Direction.OUTGOING);
        final Node typesNode;
        if (typesRelation == null) {
            typesNode = graph.createNode();
            typesNode.setProperty("name", "types");
            referenceNode.createRelationshipTo(typesNode, ClassRelations.ALL_TYPES);
        } else {
            typesNode = typesRelation.getEndNode();
        }
        return typesNode;
    }
    // todo get package node
}
