package com.crawljax.core.plugin;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

public class ScreenShotPlugin implements OnNewStatePlugin {

	public static String DIR1 = "ksg-examples/screenshot/";
	public static String DIR2 = "ksg-examples/html/";
	public static String DIR3 = "ksg-examples/keyContent/";

	static {
		mkdirs(DIR1, DIR2, DIR3);
	}

	@Override
	public void onNewState(CrawlSession session) {
		String path1 = DIR1 + session.getCurrentState().getName() + ".png";
		String path2 = DIR2 + session.getCurrentState().getName() + ".html";
		String path3 = DIR3 + session.getCurrentState().getName() + ".txt";
		try {
			session.getBrowser().saveScreenShot(new File(path1));
			writeToFile(path2, session.getCurrentState().getDom());
		} catch (CrawljaxException e) {
			e.printStackTrace();
		}
	}

	private void writeToFile(String path, String content){
		File file = null;
		FileWriter writer = null;
		StringReader reader = null;
		try {
			file = new File(path);
			writer = new FileWriter(file);
			reader = new StringReader(content);
			int c = -1;
			while ((c = reader.read()) != -1) {
				writer.write(c);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void mkdirs(String...paths) {
		for (String path: paths) {
			File directory = new File(path);
			if (!directory.isDirectory()) {
				directory.mkdirs();
			}
		}
	}

}
