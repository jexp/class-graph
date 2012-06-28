package org.neo4j.datasource.java;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.datasource.java.analyser.ClassInspectUtils;
import org.neo4j.datasource.java.declaration.neo.ClassRelations;
import org.neo4j.datasource.java.declaration.neo.NeoClassDeclaration;
import org.neo4j.datasource.java.declaration.neo.TypeNodeFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.Traversal;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClassTraversalTest {
    private static final String SERIALIZABLE = Serializable.class.getName();
    private static final String OBJECT = Object.class.getName();
    private GraphDatabaseService graph;

    @Test
    public void testFindAllSerializableSubclasses() {
        final Node serializable = getType(graph, SERIALIZABLE);
        final TraversalDescription traversal = Traversal.description().relationships(ClassRelations.INTERFACE_TYPE, Direction.INCOMING);
        int count=0;
        for (Node node : traversal.traverse(serializable).nodes()) {
            count++;
        }
        assertEquals("subnodes of serializable found", 500, count);
    }

    @Test
    public void testGetObjectMethods() {
        final Node ob = getType(graph, OBJECT);
        final Iterable<Relationship> methods = ob.getRelationships(ClassRelations.METHOD_OF, Direction.BOTH);
        final Set<String> methodNames = new NeoClassDeclaration(ob).getMethods().keySet();
        assertTrue("hashCode", methodNames.contains("int hashCode()"));
    }

    private Node getType(final GraphDatabaseService graph, String name) {
        name = ClassInspectUtils.toClassName(name);
        final TypeNodeFinder nodeFinder = new TypeNodeFinder(graph);
        final Node serializable = nodeFinder.getTypeNode(name);
        assertEquals(name, serializable.getProperty("name"));
        return serializable;
    }

    @Before
    public void setUp() {
        graph = new EmbeddedGraphDatabase("classes.jar.neo.save");
    }

    @After
    public void tearDown() {
        if (graph != null)
            graph.shutdown();
    }
}
