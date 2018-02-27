package com.github.maxopoly.Polemos;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.UUID;
import javax.imageio.ImageIO;

public class SkinHandler {

	private static String skinFolderPath = "playerSkins/";
	private static String avatarAPI = "https://crafatar.com/renders/head/UUID";

	public static BufferedImage getSkin(UUID uuid) {
		File skinFolder = new File(skinFolderPath);
		skinFolder.mkdir();
		File skinFile = new File(skinFolder, uuid.toString() + ".png");
		if (!skinFile.exists()) {
			System.out.println("No cached skin found for " + uuid.toString() + ". Attempting to download...");
			downloadSkin(uuid, skinFile);
		}
		try {
			return ImageIO.read(skinFile);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void downloadSkin(UUID uuid, File destFile) {
		try {
			URL url = new URL(avatarAPI.replace("UUID", uuid.toString()));
			InputStream is = url.openStream();
			OutputStream os = new FileOutputStream(destFile);

			byte[] b = new byte[2048];
			int length;

			while ((length = is.read(b)) != -1) {
				os.write(b, 0, length);
			}
			is.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
