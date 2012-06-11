package org.neo4j.datasource.java.declaration.bean;

import org.neo4j.datasource.java.analyser.ClassInspectUtils;
import org.neo4j.datasource.java.declaration.MethodDeclaration;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.io.Serializable;

public class MethodDeclarationBean implements MethodDeclaration, Serializable {
    private final String name;
    private final Collection<String> params;
    private final Collection<String> exceptions;
    private final String returnType;
    private final int access;

    public MethodDeclarationBean(int access, String name, String returnType, final Collection<String> params, Collection<String> exceptions) {
        this.access = access;
        this.returnType = returnType;
        this.name = name;
        this.params = params;
        this.exceptions = exceptions;
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
    }

    public int getAccess() {
        return access;
    }

    public Collection<String> getParams() {
        return params;
    }

    @Override
    public Collection<String> getExceptions() {
        return exceptions;
    }

    @Override public String toString() {
        return Modifier.toString(access) + " " + getSignature() + "\n";
    }

    public String getSignature() {
        return ClassInspectUtils.getSignature(this);
    }
}
