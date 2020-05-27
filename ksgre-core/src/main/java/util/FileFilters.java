package main.java.util;

import java.io.File;
import java.io.FilenameFilter;

public class FileFilters {

    public static FilenameFilter javaFilesFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".java");
        }
    };
}
