package org.neo4j.datasource.java.analyser;

import org.objectweb.asm.ClassVisitor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public class VirtualVisitor implements InvocationHandler {
    private final ClassInspector classInspector;
    private final int indent;

    public VirtualVisitor(final ClassInspector classInspector, final int indent) {
        this.classInspector = classInspector;
        this.indent = indent;
    }

    public static <T> T visitor(Class<T> visitorType, ClassInspector classInspector, final int indent) {
        return visitorType.cast(Proxy.newProxyInstance(visitorType.getClassLoader(), new Class[]{visitorType}, new VirtualVisitor(classInspector, indent)));
    }

    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (args != null)
            System.out.println(indent() + method.getName() + " " + Arrays.deepToString(args));
        if (method.getDeclaringClass().equals(ClassVisitor.class) && method.getName().equals("visit")) {
            if (args != null && args.length == 6) {
                if (args[4] instanceof String) classInspector.inspectClass(args[4].toString());
                if (args[5] instanceof String[]) {
                    for (String interfaceName : (String[]) args[5]) {
                        classInspector.inspectClass(interfaceName);
                    }
                }
            }
        }
        final Class<?> returnType = method.getReturnType();
        if (returnType.isInterface()) {
            return visitor(returnType, classInspector, indent + 1);
        }
        return null;
    }

    private String indent() {
        if (indent == 0) return "";
        final StringBuilder sb = new StringBuilder(indent * 2);
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }
}
