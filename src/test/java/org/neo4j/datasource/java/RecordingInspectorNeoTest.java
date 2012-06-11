package org.neo4j.datasource.java;

import org.junit.After;
import org.junit.Test;
import org.neo4j.datasource.java.analyser.ClassFileIterator;
import org.neo4j.datasource.java.analyser.RecordingInspector;
import org.neo4j.datasource.java.declaration.ClassDeclaration;
import org.neo4j.datasource.java.declaration.neo.NeoDeclarationFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import javax.swing.text.JTextComponentBeanInfo;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.neo4j.test.NeoTestHelper.dropNeoDb;

public class RecordingInspectorNeoTest {
    private GraphDatabaseService graph;

    @Test
    public void testIterateDtJar() {
        final String neoStoreName = "dt.jar.neo";
        dropNeoDb(neoStoreName);
        graph = new EmbeddedGraphDatabase(neoStoreName);
        final Transaction tx = graph.beginTx();
        final ClassFileIterator fileIterator = new ClassFileIterator();
        final String jarFileLocation = fileIterator.getJarLocationByClass(JTextComponentBeanInfo.class);
        final RecordingInspector inspector = new RecordingInspector();
        inspector.setDeclarationFactory(new NeoDeclarationFactory(graph));
        long count = 0;
        for (final String classFileName : fileIterator.getClassFileNames(jarFileLocation)) {
            inspector.inspectClass(classFileName);
            count++;
        }
        tx.success();
        tx.finish();
        assertTrue("Files in dt.jar", count > 40);
    }

    @Test
    public void testIterateClassesJar() throws IOException {
        // /Volumes/ramdisk/
        final String neoStoreName = "classes.jar.neo";
        Class<?> type = Object.class;
        dropNeoDb(neoStoreName);
        graph = new EmbeddedGraphDatabase(neoStoreName);
        Transaction tx = graph.beginTx();
        final RecordingInspector inspector = new RecordingInspector();
        final NeoDeclarationFactory infoFactory = new NeoDeclarationFactory(graph);
        inspector.setDeclarationFactory(infoFactory);
        long count = 0;
        final ClassFileIterator fileIterator = new ClassFileIterator();
        final String jarFileLocation = fileIterator.getJarLocationByClass(type);

        long time = System.currentTimeMillis();
        for (final String classFileName : fileIterator.getClassFileNames(jarFileLocation)) {
            final ClassDeclaration classDeclaration = inspector.inspectClass(classFileName);
            // System.out.println(classFileName+" -> "+classInfo);
            count++;
            if (count % 500 == 0) {
                tx.success();
                tx.finish();
                tx = graph.beginTx();

                long delta = (System.currentTimeMillis() - time);
                System.out.println("count = " + count + " took " + delta + " ms.");
                System.out.println("infoFactory status: " + infoFactory.toString());
                time = System.currentTimeMillis();
            }
        }
        tx.success();
        tx.finish();
        assertTrue("Files in classes.jar", count>1900);
    }

    /*
without signature caching and with field/method relationships in both directions
classFileName = java/lang/Object.class
count = 500 took 8397 ms.
count = 1000 took 8039 ms.
count = 1500 took 14713 ms.
count = 2000 took 19289 ms.
count = 2500 took 22172 ms.
count = 3000 took 26939 ms.
count = 3500 took 27085 ms.
count = 4000 took 39363 ms.
count = 4500 took 24075 ms.
count = 5000 took 28837 ms.
count = 5500 took 49173 ms.
count = 6000 took 44835 ms.
count = 6500 took 52506 ms.
count = 7000 took 52505 ms.
count = 7500 took 53512 ms.
count = 8000 took 54138 ms.
count = 8500 took 60008 ms.
    */
    @After
    public void tearDown() {
        if (graph != null)
            graph.shutdown();
    }
}