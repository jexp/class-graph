package org.neo4j.data.file;

import java.io.FileFilter;
import java.io.File;

public class FileSystemVisitor implements FileFilter {
    int count=0;
    private final FileHandler fileHandler;

    public FileSystemVisitor(final FileHandler fileHandler, File startFile) {
        this.fileHandler = fileHandler;
        startFile.listFiles(this);
    }

    public boolean accept(final File file) {
        fileHandler.handle(file);
        count++;
        if (file.isDirectory()) {
            fileHandler.downFrom(file);
            file.listFiles(this);
            fileHandler.upTo(file);
        }
        return true;
    }

    public int getCount() {
        return count;
    }
}
