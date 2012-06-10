package org.neo4j.api.core;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import java.lang.reflect.*;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class AnnotationTest {
    @Test
    public void testCreateAnnotation() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        final OverrideImpl override1 = new OverrideImpl();
        assertNotNull(override1);
    }

    @Test
    public void testAnnotationPropxyTest() {
        final Class<Override> annotationType = Override.class;
        final Override override = annotation(annotationType, Collections.singletonMap("default",100));
    }

    private <T extends Annotation> T annotation(final Class<T> annotationType, final Map<String, Integer> attributes) {
        return annotationType.cast(Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{annotationType}, new InvocationHandler() {
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                return attributes.get(method.getName());
            }
        }));
    }

    static class Jayway { static class RICK {} static class NICLAS {}}
    @Blub( value = {Jayway.RICK.class, Jayway.NICLAS.class} )
    @Geek( GeekType.JOHANNES )
    class Test2 {

    }
    enum GeekType {JOHANNES}

    @interface Geek {
        GeekType value();
    }
}

