package org.neo4j.datasource.java.declaration.neo;

import org.neo4j.datasource.java.analyser.ClassInspectUtils;
import org.neo4j.datasource.java.declaration.FieldDeclaration;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class NeoFieldDeclaration implements FieldDeclaration {
    Node node;
    private transient String signature;

    public NeoFieldDeclaration(Node node) {
        this.node = node;
    }

    public int getAccess() {
        return (Integer)node.getProperty("access");
    }

    public String getName() {
        return (String) node.getProperty("name");
    }

    public String getType() {
        final Relationship typeRelation = node.getSingleRelationship(ClassRelations.FIELD_TYPE, Direction.OUTGOING);
        return new NeoClassDeclaration(typeRelation.getEndNode()).getName();
    }

    public String getSignature() {
        if (signature==null) signature= ClassInspectUtils.getSignature(this);
        return signature;
    }

    public Node getNode() {
        return node;
    }

    public void setSignature(final String signature) {
        this.signature = signature;
    }
}
