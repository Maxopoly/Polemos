package com.github.maxopoly.Polemos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class FileUtil {

	public static List <String> parseFile(String path) {
		File f = new File(path);
		final List <String> lines = new LinkedList<String>();
		try {
			Files.lines(f.toPath()).forEach(new Consumer<String>() {

				public void accept(String t) {
					lines.add(t);
				}

			});
		} catch (IOException e) {
			System.out.println("Error parsing file");
			e.printStackTrace();
		};
		return lines;
	}

}
