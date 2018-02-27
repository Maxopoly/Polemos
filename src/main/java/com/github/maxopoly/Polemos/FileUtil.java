package com.github.maxopoly.Polemos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class FileUtil {

	public static List<String> parseFile(String path) {
		File f = new File(path);
		final List<String> lines = new LinkedList<String>();
		try {
			Files.lines(f.toPath()).forEach(new Consumer<String>() {

				@Override
				public void accept(String t) {
					lines.add(t);
				}

			});
		} catch (IOException e) {
			System.out.println("Error parsing file");
			e.printStackTrace();
		}
		;
		return lines;
	}

	public static String parseFileInJarToString(String path) {
		File file = null;
		URL res = FileUtil.class.getResource(path);
		if (res.toString().startsWith("jar:")) {
			try {
				InputStream input = FileUtil.class.getResourceAsStream(path);
				file = File.createTempFile("tempfile", ".tmp");
				OutputStream out = new FileOutputStream(file);
				int read;
				byte[] bytes = new byte[1024];

				while ((read = input.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}
				out.close();
				file.deleteOnExit();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else {
			file = new File(res.getFile());
		}

		if (file != null && !file.exists()) {
			throw new RuntimeException("Error: File " + file + " not found!");
		}
		final StringBuilder sb = new StringBuilder();
		try {
			Files.lines(file.toPath()).forEach(new Consumer<String>() {

				@Override
				public void accept(String t) {
					sb.append(t);
					sb.append("\n");
				}

			});
		} catch (IOException e) {
			System.out.println("Error parsing file");
			e.printStackTrace();
		}
		return sb.toString();
	}

	public static void saveToFile(File f, String s) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(f));
			writer.write(s);
		} catch (IOException e) {
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
			}
		}
	}

}
