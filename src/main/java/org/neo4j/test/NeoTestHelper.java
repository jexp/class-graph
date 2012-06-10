package org.neo4j.test;

import org.neo4j.kernel.impl.util.FileUtils;

import java.io.File;
import java.io.IOException;

public class NeoTestHelper {
    public static void dropNeoDb(final String neoStoreName) {
        try {
            FileUtils.deleteRecursively(new File(neoStoreName));
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }
}
