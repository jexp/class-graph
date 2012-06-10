package org.neo4j.api.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.data.file.FileHandler;
import org.neo4j.data.file.FileSystemVisitor;
import org.neo4j.graphdb.*;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.io.File;
import java.util.Collection;
import java.util.Stack;

import static org.junit.Assert.assertEquals;
import static org.neo4j.test.NeoTestHelper.dropNeoDb;

public class NeoFileSystemTest {
    private GraphDatabaseService neo;
    private static final String BASE_DIR = "src";
    private static final File START_FILE = new File(BASE_DIR);
    private static final String NEODB = "neodb";
    private static final String SIZE = "size";
    private static final String NAME = "name";

    @Before
    public void setUp() {
        dropNeoDb(NEODB);
    }

    @Test
    public void testNoopFileSystem() {
        final File startDir = new File(BASE_DIR);
        long time = System.currentTimeMillis();
        final FileSystemVisitor fileSystemVisitor = new FileSystemVisitor(new FileHandler() {
            public void handle(final File file) {
                final String name = file.getName();
                final long length = file.length();
            }

            public void upTo(final File file) {
            }
        }, startDir);
        time = System.currentTimeMillis() - time;
        System.out.println("time = " + time + " ms.");
        System.out.println("fileSystemVisitor = " + fileSystemVisitor.getCount());
    }


    @Test
    public void testFileSystem() {
        neo = new EmbeddedGraphDatabase(NEODB);
        long time = System.currentTimeMillis();
        final FileSystemVisitor fileSystemVisitor = readFileSystem(START_FILE, neo);
        time = System.currentTimeMillis() - time;
        System.out.println("time = " + time + " ms.");
        System.out.println("fileSystemVisitor = " + fileSystemVisitor.getCount());
    }

    @Test
    public void testTimeFileSystem() {
        neo = new EmbeddedGraphDatabase(NEODB);
        long time = System.currentTimeMillis();
        final FileHandler fileHandler = new FileNodeCreator2(neo);
        final FileSystemVisitor fileSystemVisitor = new FileSystemVisitor(fileHandler, new File("/Users/mh/java/neo"));
        fileHandler.finish();
        time = System.currentTimeMillis() - time;
        System.out.println("time = " + time + " ms.");
        System.out.println("fileSystemVisitor = " + fileSystemVisitor.getCount());
    }

    private FileSystemVisitor readFileSystem(File startDir, final GraphDatabaseService db) {
        final FileNodeCreator fileHandler = new FileNodeCreator(db);
        final FileSystemVisitor fileSystemVisitor = new FileSystemVisitor(fileHandler, startDir);
        fileHandler.finish();
        return fileSystemVisitor;
    }

    @After
    public void tearDown() {
        if (neo != null) {
            neo.shutdown();
        }
    }

    @Test
    public void queryFileSystemNodes() {
        neo = new EmbeddedGraphDatabase(NEODB);
        final FileSystemVisitor fileSystemVisitor = readFileSystem(START_FILE, neo);
        System.out.println(fileSystemVisitor.getCount());
        final Node node = neo.getReferenceNode();
        final Traverser fileSizeTraverser = node.traverse(Traverser.Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
            public boolean isReturnableNode(final TraversalPosition traversalPosition) {
                final Node node = traversalPosition.currentNode();
                final Long size = (Long) node.getProperty(SIZE, 0L);
                return size > 1000;
            }
        }, Relation.CHILD, Direction.OUTGOING);

        System.out.println("node = " + node);
        final Collection<Node> allNodes = fileSizeTraverser.getAllNodes();
        assertEquals("27 big files", 27, allNodes.size());
    }

    enum Relation implements RelationshipType {
        CHILD
    }

    private static class FileNodeCreator extends FileHandler {
        private final Stack<Node> nodes = new Stack<Node>();
        private final GraphDatabaseService graph;
        int count = 0;
        private Transaction tx;

        public FileNodeCreator(final GraphDatabaseService graph) {
            this.graph = graph;
            tx = graph.beginTx();
            final Node rootNode = graph.getReferenceNode();
            rootNode.setProperty(NAME, BASE_DIR);
            rootNode.setProperty(SIZE, 0L);
            this.nodes.push(rootNode);
        }

        public void handle(final File file) {
            final Node node = graph.createNode();
            node.setProperty(NAME, file.getName());
            node.setProperty(SIZE, file.length());
            nodes.peek().createRelationshipTo(node, Relation.CHILD);
            if (file.isDirectory()) nodes.push(node);
            count++;
            if (count % 5000 == 0) {
                tx.success();
                tx.finish();
                tx = graph.beginTx();
                System.out.println("commit " + count);
            }
        }

        public void upTo(final File file) {
            nodes.pop();
        }

        public void finish() {
            tx.success();
            tx.finish();
        }
    }

    private static class FileNodeCreator2 extends FileHandler {
        private final Stack<Node> nodes = new Stack<Node>();
        private final GraphDatabaseService graph;
        int count = 0;
        private Transaction tx;

        public FileNodeCreator2(final GraphDatabaseService graph) {
            this.graph = graph;
            tx = graph.beginTx();
            final Node rootNode = graph.getReferenceNode();
            rootNode.setProperty(NAME, BASE_DIR);
            this.nodes.push(rootNode);
        }

        public void handle(final File file) {
            final Node node = graph.createNode();
            node.setProperty(NAME, file.getName());
            nodes.peek().createRelationshipTo(node, Relation.CHILD);
            if (file.isDirectory()) nodes.push(node);
            count++;
        }

        public void upTo(final File file) {
            nodes.pop();
        }

        public void finish() {
            tx.success();
            tx.finish();
        }
    }
}