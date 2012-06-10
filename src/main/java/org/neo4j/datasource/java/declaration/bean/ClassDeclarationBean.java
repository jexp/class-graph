package org.neo4j.datasource.java.declaration.bean;

import org.neo4j.datasource.java.analyser.ClassInspectUtils;
import org.neo4j.datasource.java.declaration.ClassDeclaration;
import org.neo4j.datasource.java.declaration.FieldDeclaration;
import org.neo4j.datasource.java.declaration.MethodDeclaration;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ClassDeclarationBean implements ClassDeclaration, Serializable {
    private final String name;
    private final Map<String, MethodDeclaration> methods = new HashMap<String, MethodDeclaration>(20);
    private ClassDeclaration superClass;
    private final Collection<ClassDeclaration> anInterfaces = new LinkedList<ClassDeclaration>();
    private Map<String, FieldDeclaration> fieldInfos = new HashMap<String, FieldDeclaration>(15);
    private int id;

    public ClassDeclarationBean(final String name) {
        if (name == null) throw new IllegalArgumentException("Name must not be null");
        this.name = ClassInspectUtils.toClassName(name);
    }

    public ClassDeclarationBean(final String name, int id) {
        this(name);
        this.id=id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlashName() {
        return ClassInspectUtils.toSlashName(name);
    }

    public Map<String, MethodDeclaration> getMethods() {
        return methods;
    }

    public void addMethod(MethodDeclaration methodDeclaration) {
        methods.put(methodDeclaration.getSignature(), methodDeclaration);
    }

    public void addInterface(final ClassDeclaration classDeclaration) {
        anInterfaces.add(classDeclaration);
    }

    public void setSuperClass(final ClassDeclaration classDeclaration) {
        this.superClass = classDeclaration;
    }

    public ClassDeclaration getSuperClass() {
        return superClass;
    }

    public Collection<ClassDeclaration> getAnInterfaces() {
        return anInterfaces;
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ClassDeclarationBean classInfo = (ClassDeclarationBean) o;

        return name.equals(classInfo.name);

    }

    public int hashCode() {
        return name.hashCode();
    }

    public void addField(final FieldDeclaration fieldDeclaration) {
        fieldInfos.put(fieldDeclaration.getName(), fieldDeclaration);
    }

    public Map<String, FieldDeclaration> getFields() {
        return fieldInfos;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder(name);
        if (superClass != null && !superClass.isRoot()) sb.append(" extends ").append(superClass);
        if (!anInterfaces.isEmpty())
            sb.append("implements ").append(anInterfaces);
        return sb.toString();
    }

    public boolean isRoot() {
        return name.equals("java.lang.Object");
    }

    @Override
    public String getPackage() {
        return name.substring(0,name.lastIndexOf("."));
    }
    public String getSimpleName() {
        return name.substring(name.lastIndexOf(".")+1);
    }
}
