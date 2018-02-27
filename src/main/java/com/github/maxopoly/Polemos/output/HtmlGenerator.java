package com.github.maxopoly.Polemos.output;

import com.github.maxopoly.Polemos.FileUtil;
import com.github.maxopoly.Polemos.SkinHandler;
import com.github.maxopoly.Polemos.model.BattleMetaData;
import com.github.maxopoly.Polemos.model.PlayerStats;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class HtmlGenerator {

	private static final DecimalFormat doubleFormat = new DecimalFormat("####.#");

	private static final int defaultColor = 0xefffff;

	private Map<String, PlayerStats> stats;
	private String entryTemplate;
	private BattleMetaData metaData;

	public HtmlGenerator(Map<String, PlayerStats> stats, BattleMetaData meta) {
		this.stats = stats;
		this.metaData = meta;
	}

	public String generate() {
		String htmlTemplate = FileUtil.parseFileInJarToString("/templates/template.html");
		String css = FileUtil.parseFileInJarToString("/templates/stylesheet.css");
		String js = FileUtil.parseFileInJarToString("/templates/scripts.js");
		htmlTemplate = htmlTemplate.replace("CSS_PLACEHOLDER", css);
		htmlTemplate = htmlTemplate.replace("JAVASCRIPT_PLACEHOLDER", js);
		List<PlayerStats> stats = new LinkedList<PlayerStats>(this.stats.values());
		StringBuilder sb = new StringBuilder();
		Collections.sort(stats, new Comparator<PlayerStats>() {

			@Override
			public int compare(PlayerStats o1, PlayerStats o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		for (PlayerStats stat : stats) {
			sb.append(generatePlayerEntry(stat));
		}
		return htmlTemplate.replace("CONTENT_PLACEHOLDER", sb.toString());
	}

	private String generatePlayerEntry(PlayerStats stat) {
		if (entryTemplate == null) {
			loadEntryTemplate();
		}
		String result = entryTemplate;
		result = result.replace("ENTRY_IDENTIFIER", constructIdentifierString(stat));
		result = result.replace("GROUP_ALIGNMENT", metaData.isPlayerAttacker(stat.getName())?"attacker":"defender");
		result = result.replace("AVATAR_BASE64", base64Encode(SkinHandler.getSkin(stat.getUUID())));
		result = result.replace("BANNER_BASE64", base64Encode(getBanner(stat)));
		result = result.replace("PLAYER_NAME", stat.getName());
		result = result.replace("KILL_COUNT", String.valueOf(stat.getKills()));
		result = result.replace("DEATH_COUNT", String.valueOf(stat.getDeaths()));
		result = result.replace("ASSIST_COUNT", String.valueOf(stat.getAssists()));
		result = result.replace("HIT_DAMAGE_DEALT", doubleFormat.format(stat.getDamageDealt()));
		result = result.replace("HIT_COUNT_DEALT", String.valueOf(stat.getHitsDealt()));
		result = result.replace("SPAMCLICK_DEALT", String.valueOf(stat.getSpamHitsDealt()));
		result = result.replace("NORMALCLICK_DEALT", String.valueOf(stat.getNormalHitsDealt()));
		result = result.replace("CRIT_DEALT", String.valueOf(stat.getCritHitsDealt()));
		result = result.replace("HIT_DAMAGE_TAKEN", doubleFormat.format(stat.getDamageTaken()));
		result = result.replace("HIT_COUNT_TAKEN", String.valueOf(stat.getHitsTaken()));
		result = result.replace("SPAMCLICK_TAKEN", String.valueOf(stat.getSpamHitsTaken()));
		result = result.replace("NORMALCLICK_TAKEN", String.valueOf(stat.getNormalHitsTaken()));
		result = result.replace("CRIT_TAKEN", String.valueOf(stat.getCritHitsTaken()));
		return result;
	}

	private String constructIdentifierString(PlayerStats stat) {
		StringBuilder sb = new StringBuilder();
		sb.append(stat.getName());
		sb.append("-entry-");
		List <PlayerStats> stats = new LinkedList<PlayerStats>(this.stats.values());
		//first ordering is alphabetic
		Collections.sort(stats, new Comparator<PlayerStats>() {

			@Override
			public int compare(PlayerStats o1, PlayerStats o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		sb.append(getBufferedNumber(stats.indexOf(stat)));
		sb.append("-");
		//second ordering is MVP (for now reversed alphabetic as MVP is not implemented)
		Collections.sort(stats, new Comparator<PlayerStats>() {

			@Override
			public int compare(PlayerStats o2, PlayerStats o1) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		sb.append(getBufferedNumber(stats.indexOf(stat)));
		sb.append("-");
		//third ordering is kills
		Collections.sort(stats, new Comparator<PlayerStats>() {

			@Override
			public int compare(PlayerStats o1, PlayerStats o2) {
				return o2.getKills() - o1.getKills();
			}
		});
		sb.append(getBufferedNumber(stats.indexOf(stat)));
		sb.append("-");
		//fourth ordering is damage dealt
		Collections.sort(stats, new Comparator<PlayerStats>() {

			@Override
			public int compare(PlayerStats o1, PlayerStats o2) {
				return Double.compare(o2.getDamageDealt(), o1.getDamageDealt());
			}
		});
		sb.append(getBufferedNumber(stats.indexOf(stat)));
		sb.append("-");
		//fifth ordering is damage taken
		Collections.sort(stats, new Comparator<PlayerStats>() {

			@Override
			public int compare(PlayerStats o1, PlayerStats o2) {
				return Double.compare(o2.getDamageTaken(), o1.getDamageTaken());
			}
		});
		sb.append(getBufferedNumber(stats.indexOf(stat)));
		sb.append("-");
		//sixth ordering is damage taken/dealt ratio
		Collections.sort(stats, new Comparator<PlayerStats>() {

			@Override
			public int compare(PlayerStats o1, PlayerStats o2) {
				return Double.compare(o2.getDamageDealtToTakenRatio(), o1.getDamageDealtToTakenRatio());
			}
		});
		sb.append(getBufferedNumber(stats.indexOf(stat)));
		sb.append("-");
		return sb.toString();
	}

	private static String getBufferedNumber(int bufferNumber) {
		return new String(new char[4 - String.valueOf(bufferNumber).length() + 1]).replace("\0", "0") + String.valueOf(bufferNumber);
	}

	private BufferedImage getBanner(PlayerStats stat) {
		String group = metaData.getGroup(stat.getName());
		File bannerFolder = new File("banners/");
		bannerFolder.mkdir();
		File bannerFile = new File(bannerFolder, group + ".png");
		if (!bannerFile.exists()) {
			//we use an image with the background color to keep spacing and everything intact
			//Dont judge me, please. I'm sorry
			BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
			for(int i = 0; i < img.getHeight(); i++) {
				for(int j = 0; j < img.getWidth(); j++) {
					img.setRGB(j, i, defaultColor);
				}
			}
			return img;
		}
		try {
			return ImageIO.read(bannerFile);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void loadEntryTemplate() {
		entryTemplate = FileUtil.parseFileInJarToString("/templates/playerEntryTemplate.html");
		entryTemplate = entryTemplate.replace("DIAMOND_SWORD_IMAGE", getEncodedImage("sword"));
		entryTemplate = entryTemplate.replace("SHIELD_IMAGE", getEncodedImage("shield"));
		entryTemplate = entryTemplate.replace("SKULL_IMAGE", getEncodedImage("skull"));
	}

	private String getEncodedImage(String name) {
		BufferedImage image;
		try {
			image = ImageIO.read(HtmlGenerator.class.getResourceAsStream("/images/" + name + ".png"));
		} catch (IOException e) {
			System.out.println("Failed to load image " + name);
			e.printStackTrace();
			return null;
		}
		return base64Encode(image);
	}

	public static String base64Encode(BufferedImage img) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, "png", bos);
			byte[] imageBytes = bos.toByteArray();
			byte[] encoded = Base64.getEncoder().encode(imageBytes);
			bos.close();
			return new String(encoded, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
