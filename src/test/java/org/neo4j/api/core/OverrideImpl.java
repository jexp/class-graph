package org.neo4j.api.core;

import java.lang.annotation.Annotation;

class OverrideImpl implements Override {
    public Class<? extends Annotation> annotationType() {
        return Override.class;
    }
}
