package org.neo4j.datasource.java.declaration.neo;

import org.neo4j.datasource.java.analyser.ClassInspectUtils;
import org.neo4j.datasource.java.declaration.ClassDeclaration;
import org.neo4j.datasource.java.declaration.FieldDeclaration;
import org.neo4j.datasource.java.declaration.MethodDeclaration;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.*;

public class NeoClassDeclaration implements ClassDeclaration {
    private final Node node;

    public NeoClassDeclaration(final Node node) {
        this.node = node;
    }

    @Override
    public int getId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPackage() {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        return (String) node.getProperty("name");
    }

    public Map<String, MethodDeclaration> getMethods() {
        final Iterable<Relationship> methods = node.getRelationships(ClassRelations.METHOD_OF, Direction.OUTGOING);
        Map<String, MethodDeclaration> methodInfos = new HashMap<String, MethodDeclaration>();
        for (Relationship method : methods) {
            final NeoMethodDeclaration methodInfo = new NeoMethodDeclaration(method.getEndNode());
            methodInfos.put(methodInfo.getSignature(), methodInfo);
        }
        return methodInfos;
    }

    public void addMethod(final MethodDeclaration methodDeclaration) {
        final Node methodNode = ((NeoMethodDeclaration) methodDeclaration).getNode();
        node.createRelationshipTo(methodNode, ClassRelations.METHOD_OF);
        //methodNode.createRelationshipTo(node, ClassRelations.METHOD_OF);
    }

    public void addInterface(final ClassDeclaration classDeclaration) {
        final Node interfaceNode = ((NeoClassDeclaration) classDeclaration).getNode();
        node.createRelationshipTo(interfaceNode,ClassRelations.INTERFACE_TYPE);
        // interfaceNode.createRelationshipTo(node,ClassRelations.INTERFACE_TYPE);
    }

    public void setSuperClass(final ClassDeclaration classDeclaration) {
        final Node superNode = ((NeoClassDeclaration) classDeclaration).getNode();
        node.createRelationshipTo(superNode,ClassRelations.SUPER_TYPE);
        // superNode.createRelationshipTo(node,ClassRelations.SUPER_TYPE);
    }

    public ClassDeclaration getSuperClass() {
        final Relationship superType = getNode().getSingleRelationship(ClassRelations.SUPER_TYPE, Direction.OUTGOING);
        if (superType==null) return null;
        return new NeoClassDeclaration(superType.getEndNode());
    }

    public Collection<ClassDeclaration> getAnInterfaces() {
        final Iterable<Relationship> interfaces = node.getRelationships(ClassRelations.INTERFACE_TYPE, Direction.OUTGOING);
        Collection<ClassDeclaration> result=new LinkedList<ClassDeclaration>();
        for (Relationship interfaceRelationship : interfaces) {
            result.add(new NeoClassDeclaration(interfaceRelationship.getEndNode()));
        }
        return result;
    }

    public void addField(final FieldDeclaration fieldDeclaration) {
        final Node fieldNode = ((NeoFieldDeclaration) fieldDeclaration).getNode();
        node.createRelationshipTo(fieldNode, ClassRelations.FIELD);
        //fieldNode.createRelationshipTo(node, ClassRelations.FIELD);
    }

    public Map<String, FieldDeclaration> getFields() {
        final Iterable<Relationship> fields = node.getRelationships(ClassRelations.FIELD, Direction.OUTGOING);
        Map<String, FieldDeclaration> fieldInfos = new HashMap<String, FieldDeclaration>();
        for (Relationship field : fields) {
            final NeoFieldDeclaration fieldInfo = new NeoFieldDeclaration(field.getEndNode());
            fieldInfos.put(fieldInfo.getSignature(), fieldInfo);
        }
        return fieldInfos;
    }

    public boolean isRoot() {
        return false;
    }

    public Node getNode() {
        return node;
    }

    @Override public String toString() {
        return ClassInspectUtils.getSignature(this);
    }

    @Override
    public String getSimpleName() {
        return null;
    }
}
