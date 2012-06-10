package org.neo4j.datasource.java.analyser;

import org.neo4j.datasource.java.declaration.ClassDeclaration;
import org.neo4j.datasource.java.declaration.FieldDeclaration;
import org.neo4j.datasource.java.declaration.MethodDeclaration;

import java.util.Collection;

public class ClassInspectUtils {
    public static String toSlashName(final Class<?> type) {
        return toSlashName(type.getName());
    }

    public static String toSlashName(final String typeName) {
        return typeName.replace('.','/');
    }

    public static String toClassName(String name) {
        return name.replace('/', '.');
    }

    public static String getSignature(final MethodDeclaration methodDeclaration) {
        return methodDeclaration.getReturnType() + " " + methodDeclaration.getName() + "(" + paramList(methodDeclaration.getParams()) + ")";
    }

    private static String paramList(final Collection<String> params) {
        if (params == null || params.isEmpty()) return "";
        final StringBuilder sb = new StringBuilder();
        for (String param : params) {
            sb.append(", ").append(param);
        }
        return sb.substring(2);
    }

    public static String getSignature(final FieldDeclaration fieldDeclaration) {
        return fieldDeclaration.getType()+" "+ fieldDeclaration.getName();
    }

    public static String getSignature(final ClassDeclaration classDeclaration) {
        StringBuilder sb = new StringBuilder(classDeclaration.getName());
        final ClassDeclaration superClass = classDeclaration.getSuperClass();
        if (superClass != null && !superClass.isRoot()) sb.append(" extends ").append(superClass);
        final Collection<ClassDeclaration> anInterfaces = classDeclaration.getAnInterfaces();
        if (!anInterfaces.isEmpty())
            sb.append("implements ").append(anInterfaces);
        return sb.toString();
    }

    public static String getClassPath() {
        return System.getProperty("java.class.path");
    }

    public static String arrayToClassName(String typeName) {
        if (isArrayType(typeName)) typeName=typeName.substring(0,typeName.length()-2);
        return typeName;
    }

    public static boolean isArrayType(final String typeName) {
        return typeName.endsWith("[]");
    }
}
