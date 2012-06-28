package org.neo4j.datasource.java.declaration.neo;

import org.neo4j.datasource.java.analyser.stats.DefaultStatistics;
import org.neo4j.datasource.java.analyser.stats.Statistics;
import org.neo4j.datasource.java.analyser.ClassInspectUtils;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;

import java.util.List;

import static java.util.Arrays.asList;
import static org.neo4j.datasource.java.declaration.neo.TypeNodeFinder.StatTokens.method;
import static org.neo4j.datasource.java.declaration.neo.TypeNodeFinder.StatTokens.create;

public class TypeNodeFinder {
    NodeCache packageCache = new MapNodeCache();
    NodeCache nodeCache = new MapNodeCache();
    private final GraphDatabaseService graph;
    private Node typesNode;
    private Node packagesNode;
    private Node packageTreeNode;
    private Index<Node> packageIndex;
    private Index<Node> typeIndex;

    public Node getOrCreateTypeNode(String name) {
        return getOrCreateTypeNode(name,-1);
    }

    enum StatTokens {method, create}
    Statistics<StatTokens> statistics= // new NullStatistics<StatTokens>();
    new DefaultStatistics<StatTokens>(StatTokens.class);

    public TypeNodeFinder(final GraphDatabaseService graph) {
        this.graph = graph;
        this.typesNode = getTypesNode();
        this.packagesNode = getPackagesNode();
        this.packageTreeNode = getPackageTreeNode();
        this.typeIndex = graph.index().forNodes("types");
        this.packageIndex = graph.index().forNodes("packages");
    }

    public Node getOrCreateTypeNode(String typeName, int access) {
        typeName = toStoredTypeName(typeName);
        statistics.start(method);
        statistics.reset(create);
        try {
            final Node cachedNode = nodeCache.getCachedNode(typeName);
            if (cachedNode != null) {
                return cachedNode;
            }
            statistics.start(create);
            final Node newTypeNode = createTypeNode(typeName,access);
            nodeCache.cacheNode(typeName, newTypeNode);
            return newTypeNode;
        } finally {
            statistics.done(create);
            statistics.done(method);
        }
    }

    private String toStoredTypeName(String typeName) {
        typeName = ClassInspectUtils.toClassName(typeName);
        typeName = ClassInspectUtils.arrayToClassName(typeName);
        return typeName;
    }

    public String toString() {
        return statistics.toString(method)+ statistics.toString(create)+ " cache "+nodeCache;
    }

    private Node createTypeNode(final String typeName, int access) {
        Node packageNode = getPackageForType(typeName);
        if (packageNode==null) {
            packageNode = createPackageNode(typeName);
        }
        final Node typeNode = graph.createNode();
        typeNode.setProperty("name", typeName);
        typeIndex.add(typeNode,"name",typeName);
        if (access!=-1) {
            typeNode.setProperty("access",access);
        }
        packageNode.createRelationshipTo(typeNode, ClassRelations.IN_PACKAGE);
        typesNode.createRelationshipTo(typeNode, ClassRelations.TYPE);
        return typeNode;
    }

    private Node createPackageNode(String typeName) {
        Node packageNode = graph.createNode();
        String packageName = toPackageName(typeName);
        packageNode.setProperty("name", packageName);
        packageIndex.add(packageNode,"name",packageName);
        packagesNode.createRelationshipTo(packageNode, DynamicRelationshipType.withName(packageName));
        addPackageTree(packageNode, packageName);
        return packageNode;
    }

    private void addPackageTree(Node packageNode, String packageName) {
        Node node = packageTreeNode;
        List<String> parts = asList(packageName.split("\\."));
        String pkg="";
        for (String part : parts.subList(0,parts.size())) {
            pkg += part;
            DynamicRelationshipType partType = DynamicRelationshipType.withName(part);
            Relationship rel = node.getSingleRelationship(partType, Direction.OUTGOING);
            if (rel==null) {
                Node partNode = graph.createNode();
                partNode.setProperty("name",part);
                partNode.setProperty("package",pkg);
                node.createRelationshipTo(partNode,partType);
                node = partNode;
            } else {
                node = rel.getEndNode();
            }
            pkg += ".";
        }
        String lastPart=parts.get(parts.size()-1);
        node.createRelationshipTo(packageNode,DynamicRelationshipType.withName(lastPart));
    }

    public Node getTypeNode(String typeName) {
        typeName = toStoredTypeName(typeName);
        final Node cachedNode = nodeCache.getCachedNode(typeName);
        if (cachedNode != null) return cachedNode;

        for (Relationship inPackage : getPackageForType(typeName).getRelationships(ClassRelations.TYPE, Direction.OUTGOING)) {
            final Node typeNode = inPackage.getEndNode();
            if (typeNode.hasProperty("name") && typeNode.getProperty("name").equals(typeName)) {
                return typeNode;
            }
        }
        return null;
    }

    private Node getPackageForType(String typeName) {
        String packageName = toPackageName(typeName);
        Node cachedNode = packageCache.getCachedNode(packageName);
        if (cachedNode!=null) {
            return cachedNode;
        }
        Relationship packageRel = packagesNode.getSingleRelationship(DynamicRelationshipType.withName(packageName), Direction.OUTGOING);
        if (packageRel==null) return null;
        return packageRel.getEndNode();
    }

    private String toPackageName(String typeName) {
        int idx = typeName.lastIndexOf(".");
        return idx == -1 ? "default" : typeName.substring(0, idx).replace('/', '.');
    }

    public Node getTypesNode() {
        return getCategoryNode(ClassRelations.ALL_TYPES);
    }

    public Node getPackagesNode() {
        return getCategoryNode(ClassRelations.ALL_PACKAGES);
    }

    public Node getPackageTreeNode() {
        return getCategoryNode(ClassRelations.PACKAGE_TREE);
    }

    public Node getCategoryNode(ClassRelations type) {
        final Node referenceNode = graph.getReferenceNode();
        final Relationship relationship = referenceNode.getSingleRelationship(type, Direction.OUTGOING);
        if (relationship == null) {
            final Transaction tx = graph.beginTx();
            try {
                final Node categoryNode = graph.createNode();
                categoryNode.setProperty("name", type.name());
                referenceNode.createRelationshipTo(categoryNode, type);
                tx.success();
                return categoryNode;
            } finally {
                tx.finish();
            }
        } else {
            return relationship.getEndNode();
        }
    }
}
