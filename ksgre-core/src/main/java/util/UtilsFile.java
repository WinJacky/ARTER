package main.java.util;

import main.java.config.Settings;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.apache.commons.io.FileUtils.isSymlink;

public class UtilsFile {

    /**
     * Use current date and time as file name.
     * @param nameFormat
     *      The format of the file name, for example: "yyyyMMddHHmmss".
     * @param fileType
     *      The extension of the file.
     * @return
     *      The name of the file to be created.
     */
    public static String fileNameByDateTime(String nameFormat, String fileType) {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(nameFormat);
        String fileName = dateFormat.format(date) + fileType;
        return fileName;
    }

    public static String fileNameByDateTime(String fileType) {
        return UtilsFile.fileNameByDateTime("yyyyMMddHHmmss", fileType);
    }

    public static String readFileToString(String filePath) {
        File file = new File(filePath);
        BufferedReader reader = null;
        String fileString = "";
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                fileString += tempString;
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return fileString;
    }

    /**
     * Delete a directory recursively.
     *
     * @param directory
     * @throws IOException
     */
    public static void deleteDirectory(File directory) throws IOException {
        if(!directory.exists()) {
            return;
        }

        if(!isSymlink(directory)) {
            cleanDirectory(directory);
        }

        if(!directory.delete()) {
            String message = "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    public static String readGetTagsInfoJS(List<String> fileNames) {

        String currProjPath = System.getProperty("user.dir");

        String JSCode = "";
        for (String fileName: fileNames) {
            JSCode += UtilsFile.readFileToString(currProjPath + Settings.JSDirPath + "/" + fileName + ".js");
        }

        return JSCode;
    }

    public static String readGetEleAppendInfoJS() {

        String currProjPath = System.getProperty("user.dir");

        String JSCode = UtilsFile.readFileToString(currProjPath + Settings.JSDirPath + "/GetEleAppendInfo.js");

        return JSCode;

    }


}
