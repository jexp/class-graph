package org.neo4j.datasource.java.declaration.bean;

import org.neo4j.datasource.java.analyser.ClassInspectUtils;
import org.neo4j.datasource.java.declaration.FieldDeclaration;

import java.lang.reflect.Modifier;
import java.io.Serializable;

public class FieldDeclarationBean implements FieldDeclaration, Serializable {
    private final int access;
    private final String name;
    private final String type;

    public FieldDeclarationBean(final int access, final String name, final String type) {
        this.access = access;
        this.name = name;
        this.type = type;
    }

    public int getAccess() {
        return access;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override public String toString() {
        return Modifier.toString(access)+" "+ getSignature()+"\n";
    }

    public String getSignature() {
        return ClassInspectUtils.getSignature(this);
    }
}
