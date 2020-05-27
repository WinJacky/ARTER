package main.java.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class StreamGobbler extends Thread {

    private static Logger log = LoggerFactory.getLogger(StreamGobbler.class);

    InputStream is;
    String type;
    OutputStream os;

    StreamGobbler(InputStream is, String type) {
        this(is, type, null);
    }

    StreamGobbler(InputStream is, String type, OutputStream redirect) {
        this.is = is;
        this.type = type;
        this.os = redirect;
    }

    public void run() {

        try {
            PrintWriter pw = null;
            if(os != null) {
                pw = new PrintWriter(os);
            }

            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while((line=br.readLine()) != null) {
                if(pw != null) {
//                    pw.println(line);
                }
//                log.info(type + " > " + line);
            }
            if(pw != null)
                pw.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
