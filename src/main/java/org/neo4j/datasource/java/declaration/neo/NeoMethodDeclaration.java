package org.neo4j.datasource.java.declaration.neo;

import org.neo4j.datasource.java.analyser.ClassInspectUtils;
import org.neo4j.datasource.java.declaration.MethodDeclaration;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.Collection;
import java.util.LinkedList;

public class NeoMethodDeclaration implements MethodDeclaration {
    private final Node node;
    private transient String signature;

    public NeoMethodDeclaration(final Node node) {
        this.node = node;
    }

    public String getName() {
        return (String) node.getProperty("name");
    }

    public String getReturnType() {
        final Relationship returnType = node.getSingleRelationship(ClassRelations.RETURN_TYPE, Direction.OUTGOING);
        return new NeoClassDeclaration(returnType.getEndNode()).getName();
    }

    public Collection<String> getParams() {
        final Iterable<Relationship> params = node.getRelationships(ClassRelations.PARAM_TYPE, Direction.OUTGOING);
        final Collection<String> result=new LinkedList<String>();
        for (final Relationship param : params) {
            result.add(new NeoClassDeclaration(param.getEndNode()).getName());
        }
        return result;
    }

    @Override
    public Collection<String> getExceptions() {
        final Iterable<Relationship> params = node.getRelationships(ClassRelations.THROWS, Direction.OUTGOING);
        final Collection<String> result=new LinkedList<String>();
        for (final Relationship param : params) {
            result.add(new NeoClassDeclaration(param.getEndNode()).getName());
        }
        return result;
    }

    public int getAccess() {
        return (Integer)node.getProperty("access");
    }

    public String getSignature() {
        if (signature==null) signature= ClassInspectUtils.getSignature(this);
        return signature;
    }

    public void setSignature(final String signature) {
        this.signature = signature;
    }

    public Node getNode() {
        return node;
    }
}
