package org.neo4j.datasource.java.declaration.neo;


import org.neo4j.datasource.java.analyser.ClassInspectUtils;
import org.neo4j.datasource.java.declaration.ClassDeclaration;
import org.neo4j.datasource.java.declaration.DeclarationFactory;
import org.neo4j.datasource.java.declaration.FieldDeclaration;
import org.neo4j.datasource.java.declaration.MethodDeclaration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.util.Collection;

public class NeoDeclarationFactory implements DeclarationFactory {
    private final GraphDatabaseService graph;
    private TypeNodeFinder typeNodeFinder;

    public NeoDeclarationFactory(final GraphDatabaseService graph) {
        this.graph = graph;
        typeNodeFinder = new TypeNodeFinder(graph);
    }

    public ClassDeclaration createClassInfo(final String name) {
        final Node typeNode = typeNodeFinder.getOrCreateTypeNode(name);
        return new NeoClassDeclaration(typeNode);
    }

    public MethodDeclaration createMethodInfo(final int access, final String name, final Collection<String> params, final String returnType) {
        final Node methodNode = graph.createNode();
        methodNode.setProperty("access", access);
        methodNode.setProperty("name", name);
        System.out.println("returnType = " + returnType);
        final Node returnTypeNode = typeNodeFinder.getOrCreateTypeNode(returnType);
        methodNode.createRelationshipTo(returnTypeNode, ClassRelations.RETURN_TYPE);
        if (ClassInspectUtils.isArrayType(returnType)) {
            methodNode.createRelationshipTo(returnTypeNode, ClassRelations.TYPE_ARRAY);
        }
        for (String paramName : params) {
            System.out.println("paramName = " + paramName);
            final Node paramNode = typeNodeFinder.getOrCreateTypeNode(paramName);
            // todo paramNodes, array params
            methodNode.createRelationshipTo(paramNode, ClassRelations.PARAM_TYPE);
        }
        final NeoMethodDeclaration methodInfo = new NeoMethodDeclaration(methodNode);
        methodInfo.setSignature(returnType + " " + name + "(" + params + ")");
        return methodInfo;
    }

    public FieldDeclaration createFieldInfo(final int access, final String name, final String typeName) {
        final Node fieldNode = graph.createNode();
        fieldNode.setProperty("access", access);
        fieldNode.setProperty("name", name);
        System.out.println("typeName = " + typeName);
        final Node typeNode = typeNodeFinder.getOrCreateTypeNode(typeName);
        fieldNode.createRelationshipTo(typeNode, ClassRelations.FIELD_TYPE);
        if (ClassInspectUtils.isArrayType(typeName))
            fieldNode.createRelationshipTo(typeNode, ClassRelations.TYPE_ARRAY);

        final NeoFieldDeclaration fieldInfo = new NeoFieldDeclaration(fieldNode);
        fieldInfo.setSignature(typeName+" "+name);
        return fieldInfo;
    }

    @Override public String toString() {
        return "NeoInfoFactory: "+typeNodeFinder.toString();
    }
}
