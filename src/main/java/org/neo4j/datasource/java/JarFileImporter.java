package org.neo4j.datasource.java;

import org.neo4j.datasource.java.analyser.ClassFileIterator;
import org.neo4j.datasource.java.analyser.RecordingInspector;
import org.neo4j.datasource.java.declaration.neo.NeoDeclarationFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.util.FileUtils;

import java.io.File;
import java.io.IOException;

public class JarFileImporter {

    private final GraphDatabaseService graph;

    public static void main(String[] args) throws ClassNotFoundException {
        JarFileImporter importer = new JarFileImporter(args[0], true);
        importer.importJarFor(Class.forName(args[1]));
        importer.shutDown();
    }

    public JarFileImporter(String databaseLocation, boolean create) {
        if (create) {
            dropNeoDb(databaseLocation);
        }
        graph = new EmbeddedGraphDatabase(databaseLocation);
    }

    public void shutDown() {
        graph.shutdown();
    }

    public void importJarFor(Class<?> type) {
        Transaction tx = graph.beginTx();
        final RecordingInspector inspector = new RecordingInspector();
        final NeoDeclarationFactory infoFactory = new NeoDeclarationFactory(graph);
        inspector.setDeclarationFactory(infoFactory);
        long count = 0;
        final ClassFileIterator fileIterator = new ClassFileIterator();
        final String jarFileLocation = fileIterator.getJarLocationByClass(type);

        long time = System.currentTimeMillis();
        for (final String classFileName : fileIterator.getClassFileNames(jarFileLocation)) {
            inspector.inspectClass(classFileName);
            count++;
            if (count % 500 == 0) {
                tx.success();
                tx.finish();
                tx = graph.beginTx();
            }
        }
        tx.success();
        tx.finish();
        long delta = (System.currentTimeMillis() - time);
        System.out.println(count + " classes took " + delta + " ms.");
    }

    public static void dropNeoDb(final String neoStoreName) {
        File file = new File(neoStoreName);
        if (!file.exists()) return;
        try {
            FileUtils.deleteRecursively(file);
        } catch (IOException ioe) {
            throw new RuntimeException("Error deleting directory ", ioe);
        }
    }

}
