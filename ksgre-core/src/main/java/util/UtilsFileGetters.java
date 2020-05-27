package main.java.util;

import main.java.config.Settings;
import main.java.dataType.DOMInformation;
import main.java.dataType.StringComparator;

import java.io.*;
import java.util.*;

public class UtilsFileGetters {

    /**
     * Get the absolute path of the test case.
     *
     * @param name
     * @param pathToTestSuiteUnderTest
     * @return
     */
    public static String getTestFile(String name, String pathToTestSuiteUnderTest) {

        File[] files = new File(pathToTestSuiteUnderTest).listFiles(FileFilters.javaFilesFilter);
        for(File file: files) {
            if(file.getName().contains(name)) {
                return file.getAbsolutePath();
            }
        }

        return null;
    }

    /**
     * Get all java files' name of one directory.
     *
     * @param filePath
     * @return
     */
    public static List<String> getAllJavaFilesOfDir(String filePath) {

        File[] files = new File(filePath).listFiles(FileFilters.javaFilesFilter);

        List<String> allFilesName = new ArrayList<String>();
        for(File file: files) {
            allFilesName.add(file.getName().replace(".java", ""));
        }

        Collections.sort(allFilesName, new StringComparator());
        return allFilesName;
    }

    /**
     * Auxiliary method to get the HTML file.
     *
     * @param name
     * @param beginLine
     * @param type
     * @param useExtension
     * @param folder
     * @return
     * @throws Exception
     */
    public static File getHTMLDOMfile(String name, int beginLine, String type, String useExtension, String folder)
            throws Exception {

        String p = folder + name + Settings.sep + beginLine + "-" + type + "-" + name + "-" + beginLine;

        File dir = new File(p);
        File[] listOfFiles = dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String n) {
                return (n.endsWith(Settings.HTML_EXT));
            }
        });

        if (listOfFiles == null || listOfFiles.length == 0) {
            return null;
        } else {
            return listOfFiles[0];
        }

    }

    /**
     * Auxiliary method to get the JSON file with the DOM information.
     *
     * @param name
     * @param beginLine
     * @param type
     * @param folder
     * @return
     * @throws Exception
     */
    public static DOMInformation getDOMInformationFromJsonFile(String name, int beginLine, String type, String folder)
            throws Exception {

        String p = folder.replaceAll("\\.", "\\\\") + Settings.sep + name + Settings.sep;

        File dir = new File(p);
        File[] listOfFiles = dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String n) {
                return (n.startsWith(Integer.toString(beginLine)) && n.endsWith(Settings.JSON_EXT) && n.contains(name)
                        && n.contains(type));
            }

        });

        if (listOfFiles.length == 0) {
            // ------
            // Add detailed bug information.
            // Ryan 2018-12-11
            System.out.printf("\tbeginLine: %d, name: %s, type: %s\n", beginLine, name, type);
            // ------

            throw new Exception("[LOG]\tNo JSON file retrieved");

        } else if (listOfFiles.length == 1) {

            DOMInformation obj = UtilsParser.gson.fromJson(new BufferedReader(new FileReader(listOfFiles[0])),
                    DOMInformation.class);

            return obj;

        } else {
            throw new Exception("[LOG]\tToo many files retrieved");
        }

    }
}
