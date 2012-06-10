package org.neo4j.api.core;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.data.file.FileHandler;
import org.neo4j.data.file.FileSystemVisitor;
import org.neo4j.graphdb.*;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.io.File;
import static java.util.Arrays.*;

public class SimpleTest {
    private GraphDatabaseService neo;
    private Transaction tx;
    private static final String BASE_DIR = "/Users/mh14/java/lib";

    enum Relation implements RelationshipType {
        MARRIED
    }

    @Test
    public void testSimpleNeo() {
        try {
            final Node micha = neo.createNode();
            micha.setProperty("name", "Micha");
            final Node tina = neo.createNode();
            tina.setProperty("name", "Tina");
            micha.createRelationshipTo(tina, Relation.MARRIED);
            final Traverser traverser = micha.traverse(Traverser.Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL, Relation.MARRIED, Direction.BOTH);
            for (Node node : traverser) {
                assertTrue(asList(micha, tina).contains(node));
            }
        } finally {
        }
    }

    @Before
    public void setUp() {
        dropNeoDb("simple");
        neo = new EmbeddedGraphDatabase("simple");
        tx = neo.beginTx();
    }

    private boolean dropNeoDb(final String dir) {
        final File neodb = new File(dir);
        if (!neodb.exists()) return false;
        new FileSystemVisitor(new FileHandler() {
            public void handle(final File file) {
                if (file.isFile()) file.delete();
            }

            public void upTo(final File file) {
                file.delete();
            }
        }, neodb);
        return true;
    }


    @After
    public void tearDown() {
        tx.success();
        tx.finish();
        neo.shutdown();
    }
}

