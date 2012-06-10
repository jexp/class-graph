package org.neo4j.datasource.java.declaration;

import java.util.Collection;
import java.util.Map;

public interface PackageDeclaration {
    int getId();
    String getName();

    String getSuperPackage();

    boolean isRoot();
}