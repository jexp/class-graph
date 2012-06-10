package org.neo4j.analyser.codecity;

import org.neo4j.datasource.java.analyser.ClassFileIterator;
import org.neo4j.datasource.java.analyser.RecordingInspector;
import org.neo4j.datasource.java.declaration.ClassDeclaration;
import org.neo4j.datasource.java.declaration.PackageDeclaration;
import org.neo4j.datasource.java.declaration.bean.PackageDeclarationBean;

import java.io.*;
import static java.lang.System.currentTimeMillis;
import java.util.*;

/**
 * @author Michael Hunger
 * @since 07.10.2009
 */
public class MooseWriter {
    private final Map<String, ClassDeclaration> classes;
    private Set<PackageDeclaration> namespaces;
    private Collection<PackageDeclaration> aPackages;
    private int index;

    public MooseWriter(final Map<String, ClassDeclaration> classes) {
        this.classes = classes;
        index = classes.size() + 2;
        System.out.println("max.class.idx = " + index);
        this.namespaces = extractNamespaces(classes, index);
        index += namespaces.size()+1;
        System.out.println("max.ns.idx = " + index);
        this.aPackages = extractPackages(namespaces, index);
        index += aPackages.size()+1;
        System.out.println("max.pkg.idx = " + index);
    }

    private Collection<PackageDeclaration> extractPackages(Collection<PackageDeclaration> namespaces, int startIdx) {
        Collection<PackageDeclaration> result=new HashSet<PackageDeclaration>(namespaces.size());
        for (PackageDeclaration namespace : namespaces) {
            PackageDeclaration packageDeclaration = new PackageDeclarationBean(namespace.getName(), startIdx++);
            result.add(packageDeclaration);
            while (!packageDeclaration.isRoot()) {
                final PackageDeclaration superPackage = new PackageDeclarationBean(packageDeclaration.getSuperPackage(), startIdx);
                if (!result.contains(superPackage)) {
                    result.add(superPackage);
                        startIdx++;
                }
                packageDeclaration =superPackage;
            }
        }
        return result;
    }

    private Set<PackageDeclaration> extractNamespaces(final Map<String, ClassDeclaration> classes, int startIdx) {
        final Set<PackageDeclaration> result = new HashSet<PackageDeclaration>();
        for (final ClassDeclaration declaration : classes.values()) {
            final PackageDeclarationBean packageInfo = new PackageDeclarationBean(declaration.getPackage(), startIdx);
            if (!result.contains(packageInfo)) {
                result.add(packageInfo);
                startIdx++;
            }
        }
        return result;
    }

    public static void main(final String[] args) throws Exception {
        final String file = args[0];
        final Class<?> type = args.length > 1 ? Class.forName(args[1]) : Object.class;
        long time= currentTimeMillis();
        final Map<String, ClassDeclaration> classes = new ClassParser().loadClasses(type);
        time= currentTimeMillis()-time;
        System.out.println("reading took " + time+" ms");
        time= currentTimeMillis();
        new MooseWriter(classes).writeFile(file);
        time= currentTimeMillis()-time;
        System.out.println("\nwriting took " + time+" ms");
    }

    private void writeFile(final String file) {
        try {
            final Writer os = new BufferedWriter(new FileWriter(file));
            writeHeader(os);
            writePackages(os);
            writeClasses(os,index);
            writeFooter(os);
            os.close();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private void writeClasses(Writer os, int index) throws IOException {
        System.out.println("\nWriting classes");
        for (ClassDeclaration declaration : classes.values()) {
            os.write("\t\t(FAMIX.Class (id: "+ declaration.getId()+")\n" +
                    "\t\t\t(name '"+ declaration.getSimpleName()+"')\n" +
                    "\t\t\t(belongsTo (idref: "+getPackageId(namespaces, declaration.getPackage())+"))\n" +
                    "\t\t\t(isAbstract false)\n" +
                    "\t\t\t(isInterface false)\n" +
                    "\t\t\t(packagedIn (idref: "+getPackageId(aPackages, declaration.getPackage())+")))\n");
            if (!declaration.isRoot()) {
                os.write("\t\t(FAMIX.InheritanceDefinition (id: "+(index++)+")\n" +
                    "\t\t\t(subclass (idref: "+ declaration.getId()+"))\n" +
                    "\t\t\t(superclass (idref: "+ declaration.getSuperClass().getId() +")))\n");
            }
        }
    }

    private int getPackageId(Collection<PackageDeclaration> aPackages, String pkgName) {
        for (PackageDeclaration namespace : aPackages) {
            if (namespace.getName().equals(pkgName)) return namespace.getId();
        }
        throw new IllegalArgumentException("Unknown package "+pkgName);
    }

    private void writePackages(Writer os) throws IOException {
        System.out.println("\nWriting namespaces");
        for (PackageDeclaration declaration : namespaces) {
            os.write("\t\t(FAMIX.Namespace (id: "+ declaration.getId()+")\n" +
                    "\t\t\t(name '"+toMoose(declaration.getName())+"'))\n");
        }
        System.out.println("\nWriting packages");
        for (PackageDeclaration aPackage : aPackages) {
            os.write("\t\t(FAMIX.Package (id: "+aPackage.getId()+")\n" +
                    "\t\t\t(name '"+toMoose(aPackage.getName())+"')\n" +
                    "\t\t\t(DIH "+dih(aPackage)+")\n" +
                    (aPackage.isRoot() ? "" : "\t\t\t(packagedIn (idref: "+getPackageId(aPackages,aPackage.getSuperPackage())+"))")
                    +")\n");
        }
    }

    private int dih(PackageDeclaration aPackage) {
        return aPackage.getName().split("\\.").length;
    }

    private String toMoose(String pkg) {
        return pkg.replaceAll("/","::");
    }

    private void writeFooter(Writer os) throws IOException {
        os.write(")\n" +
                "\t(LOC 1000)\n" +
                "\t(NOC "+classes.size()+")\n" +
                "\t(NOP "+ namespaces.size()+")\n" +
                "\t(sourceLanguage 'Java'))\n");
    }

    private void writeHeader(Writer os) throws IOException {
        os.write("(Moose.Model (id: 1)\n" +
                "\t(name 'test')\n" +
                "\t(entity");
    }

    static class ClassParser {
        public Map<String, ClassDeclaration> loadClasses(final Class<?> type) {
            final RecordingInspector inspector = new RecordingInspector();
            final ClassFileIterator fileIterator = new ClassFileIterator();
            final String jarFileLocation = fileIterator.getJarLocationByClass(type);
            for (final String classFileName : fileIterator.getClassFileNames(jarFileLocation)) {
                inspector.inspectClass(classFileName);
            }
            return inspector.getClasses();
        }
    }
}
