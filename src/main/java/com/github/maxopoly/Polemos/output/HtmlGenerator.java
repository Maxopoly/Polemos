package com.github.maxopoly.Polemos.output;

import com.github.maxopoly.Polemos.DataAggregator;
import com.github.maxopoly.Polemos.FileUtil;
import com.github.maxopoly.Polemos.model.BattleMetaData;
import com.github.maxopoly.Polemos.model.PlayerKill;
import com.github.maxopoly.Polemos.model.PlayerStats;
import com.github.maxopoly.Polemos.model.Potion;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.imageio.ImageIO;

public class HtmlGenerator {

	private static final DecimalFormat doubleFormat = new DecimalFormat("####.#");

	private Map<String, PlayerStats> stats;
	private List<PlayerKill> kills;
	private String entryTemplate;
	private String itemEntryTemplate;
	private BattleMetaData metaData;

	public HtmlGenerator(Map<String, PlayerStats> stats, BattleMetaData meta, List<PlayerKill> kills) {
		this.stats = stats;
		this.kills = kills;
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
			String playerEntry = generatePlayerEntry(stat, new LinkedList<PlayerStats>(stats), 0);
			playerEntry = playerEntry.replace("WHAT_KIND_IS_THIS", "player");
			playerEntry = playerEntry.replace("BANNER_BASE64", getBanner(metaData.getGroup(stat.getName())));
			playerEntry = playerEntry.replace("GROUP_ALIGNMENT", metaData.isPlayerAttacker(stat.getName()) ? "attacker"
					: "defender");
			playerEntry = playerEntry.replace("AVATAR_VISIBILITY", "block");
			playerEntry = playerEntry.replace("KILL_HOVER_TEXT",
					converMultilineTextToHover(DataAggregator.getKillBreakdown(kills, stat.getName())));
			playerEntry = playerEntry.replace("DEATH_HOVER_TEXT",
					converMultilineTextToHover(DataAggregator.getDeathBreakdown(kills, stat.getName())));
			playerEntry = playerEntry.replace("ASSIST_HOVER_TEXT",
					converMultilineTextToHover(DataAggregator.getAssistBreakdown(kills, stat.getName())));
			sb.append(playerEntry);
		}
		sb.append(genGroupEntries(DataAggregator.genGroupStatsTotal(this.stats, metaData), "group"));
		sb.append(genGroupEntries(DataAggregator.genGroupStatsAvg(this.stats, metaData), "groupAvg"));
		sb.append(genSideEntries(DataAggregator.genSideStatsTotal(this.stats, metaData), "side"));
		sb.append(genSideEntries(DataAggregator.genSideStatsAvg(this.stats, metaData), "sideAvg"));
		return htmlTemplate.replace("CONTENT_PLACEHOLDER", sb.toString());
	}

	private String genGroupEntries(Map<String, PlayerStats> stats, String tag) {
		StringBuilder sb = new StringBuilder();
		List<PlayerStats> groupStats = new LinkedList<PlayerStats>(stats.values());
		Collections.sort(groupStats, new Comparator<PlayerStats>() {

			@Override
			public int compare(PlayerStats o1, PlayerStats o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		for (PlayerStats stat : groupStats) {
			String playerEntry = generatePlayerEntry(stat, new LinkedList<PlayerStats>(groupStats),
					metaData.getPlayerCount(stat.getName()));
			playerEntry = playerEntry.replace("WHAT_KIND_IS_THIS", tag);
			playerEntry = playerEntry.replace("BANNER_BASE64", getBanner(stat.getName()));
			playerEntry = playerEntry.replace("GROUP_ALIGNMENT", metaData.isGroupAttacker(stat.getName()) ? "attacker"
					: "defender");
			playerEntry = playerEntry.replace("AVATAR_VISIBILITY", "none");
			playerEntry = playerEntry.replace("KILL_HOVER_TEXT", "");
			sb.append(playerEntry);
		}
		return sb.toString();
	}

	private String genSideEntries(PlayerStats[] sideStats, String tag) {
		StringBuilder sb = new StringBuilder();
		for (PlayerStats stat : sideStats) {
			String playerEntry = generatePlayerEntry(stat, new LinkedList<PlayerStats>(Arrays.asList(sideStats)),
					stat == sideStats[0] ? metaData.getAttackerCount() : metaData.getDefenderCount());
			playerEntry = playerEntry.replace("WHAT_KIND_IS_THIS", tag);
			playerEntry = playerEntry.replace("GROUP_ALIGNMENT", stat == sideStats[0] ? "attacker" : "defender");
			playerEntry = playerEntry.replace("BANNER_BASE64", getBanner(""));
			playerEntry = playerEntry.replace("AVATAR_VISIBILITY", "none");
			playerEntry = playerEntry.replace("KILL_HOVER_TEXT", "");
			// TODO proper banner representing sides
			sb.append(playerEntry);
		}
		return sb.toString();
	}

	private String generatePlayerEntry(PlayerStats stat, List<PlayerStats> otherStats, int memberCount) {
		if (entryTemplate == null) {
			loadEntryTemplate();
		}
		String result = entryTemplate;
		result = result.replace("ENTRY_IDENTIFIER", constructIdentifierString(stat, otherStats));
		if (stat.getUUID() != null) {
			result = result.replace("AVATAR_BASE64", "playerSkins" + stat.getUUID() + ".png");
		}
		if (memberCount == 0) {
			result = result.replace("PLAYER_NAME", stat.getName());
		} else {
			result = result.replace("PLAYER_NAME", stat.getName() + " (" + memberCount + ")");
		}
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
		result = result.replace("PEARL_COUNT", String.valueOf(stat.getPearlsThrown()));
		result = result.replace("INGOT_COUNT", String.valueOf(stat.getAncientIngotsUsed()));
		result = result.replace("MVP_POINTS", String.valueOf(stat.getMVPPoints()));
		result = result.replace("PLAYER_ITEMS_PLACEHOLDER", generateItemSection(stat));
		return result;
	}

	private String generateItemSection(PlayerStats stat) {
		String currentPart = null;
		StringBuilder sb = new StringBuilder();
		if (stat.getAncientIngotsUsed() != 0) {
			currentPart = itemEntryTemplate;
			currentPart.replace("ITEM_IMAGE_1", "images/ingot.png");
			currentPart.replace("ITEM_COUNT_1", String.valueOf(stat.getAncientIngotsUsed()));
		}
		if (stat.getPearlsThrown() != 0) {
			if (currentPart != null) {
				currentPart.replace("ITEM_IMAGE_2", "images/pearl.png");
				currentPart.replace("ITEM_COUNT_2", String.valueOf(stat.getPearlsThrown()));
				sb.append(currentPart);
				currentPart = null;
			} else {
				currentPart = itemEntryTemplate;
				currentPart.replace("ITEM_IMAGE_1", "images/pearl.png");
				currentPart.replace("ITEM_COUNT_1", String.valueOf(stat.getPearlsThrown()));
			}
		}
		for (Entry<Potion, Integer> entry : stat.getPotionsUsed().entrySet()) {
			if (currentPart != null) {
				currentPart.replace("ITEM_IMAGE_2", entry.getKey().getImagePath());
				currentPart.replace("ITEM_COUNT_2", String.valueOf(entry.getValue()));
				sb.append(currentPart);
				currentPart = null;
			} else {
				currentPart = itemEntryTemplate;
				currentPart.replace("ITEM_IMAGE_1", entry.getKey().getImagePath());
				currentPart.replace("ITEM_COUNT_1", String.valueOf(entry.getValue()));
			}
		}

		if (currentPart != null) {
			currentPart.replace("ITEM_IMAGE_2", "");
			currentPart.replace("ITEM_COUNT_2", "");
		}
		return sb.toString();

	}

	private String converMultilineTextToHover(List<String> textLines) {
		StringBuilder sb = new StringBuilder();
		sb.append(textLines.get(0));
		for (int i = 1; i < textLines.size(); i++) {
			sb.append("&#13;"); // newline in hover
			sb.append(textLines.get(i));
		}
		return sb.toString();
	}

	private String constructIdentifierString(PlayerStats stat, List<PlayerStats> otherStats) {
		StringBuilder sb = new StringBuilder();
		sb.append(stat.getName());
		sb.append("-entry-");
		List<PlayerStats> stats = otherStats;
		// first ordering is alphabetic
		Collections.sort(stats, new Comparator<PlayerStats>() {

			@Override
			public int compare(PlayerStats o1, PlayerStats o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		sb.append(getBufferedNumber(stats.indexOf(stat)));
		sb.append("-");
		// second ordering is MVP (for now reversed alphabetic as MVP is not implemented)
		Collections.sort(stats, new Comparator<PlayerStats>() {

			@Override
			public int compare(PlayerStats o2, PlayerStats o1) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		sb.append(getBufferedNumber(stats.indexOf(stat)));
		sb.append("-");
		// third ordering is kills
		Collections.sort(stats, new Comparator<PlayerStats>() {

			@Override
			public int compare(PlayerStats o1, PlayerStats o2) {
				return o2.getKills() - o1.getKills();
			}
		});
		sb.append(getBufferedNumber(stats.indexOf(stat)));
		sb.append("-");
		// fourth ordering is damage dealt
		Collections.sort(stats, new Comparator<PlayerStats>() {

			@Override
			public int compare(PlayerStats o1, PlayerStats o2) {
				return Double.compare(o2.getDamageDealt(), o1.getDamageDealt());
			}
		});
		sb.append(getBufferedNumber(stats.indexOf(stat)));
		sb.append("-");
		// fifth ordering is damage taken
		Collections.sort(stats, new Comparator<PlayerStats>() {

			@Override
			public int compare(PlayerStats o1, PlayerStats o2) {
				return Double.compare(o2.getDamageTaken(), o1.getDamageTaken());
			}
		});
		sb.append(getBufferedNumber(stats.indexOf(stat)));
		sb.append("-");
		// sixth ordering is damage taken/dealt ratio
		Collections.sort(stats, new Comparator<PlayerStats>() {

			@Override
			public int compare(PlayerStats o1, PlayerStats o2) {
				return Double.compare(o2.getDamageDealtToTakenRatio(), o1.getDamageDealtToTakenRatio());
			}
		});
		sb.append(getBufferedNumber(stats.indexOf(stat)));
		return sb.toString();
	}

	private static String getBufferedNumber(int bufferNumber) {
		return new String(new char[4 - String.valueOf(bufferNumber).length() + 1]).replace("\0", "0")
				+ String.valueOf(bufferNumber);
	}

	private String getBanner(String groupName) {
		File bannerFolder = new File("banners/");
		bannerFolder.mkdir();
		File bannerFile = new File(bannerFolder, groupName + ".png");
		String groupPath = groupName;
		if (!bannerFile.exists()) {
			// we use an image with the background color to keep spacing and everything intact
			// Dont judge me, please. I'm sorry
			groupPath = metaData.isGroupAttacker(groupName) ? "attackerBanner" : "defenderBanner";
		}
		return "banners/" + groupPath + ".png";
	}

	private void loadEntryTemplate() {
		entryTemplate = FileUtil.parseFileInJarToString("/templates/playerEntryTemplate.html");
		entryTemplate = entryTemplate.replace("DIAMOND_SWORD_IMAGE", "images/sword.png");
		entryTemplate = entryTemplate.replace("SHIELD_IMAGE", "images/shield");
		entryTemplate = entryTemplate.replace("SKULL_IMAGE", "images/skull");
		entryTemplate = entryTemplate.replace("PEARL_IMAGE", "images/pearl");
		entryTemplate = entryTemplate.replace("INGOT_IMAGE", "images/ingot");
		entryTemplate = entryTemplate.replace("MVP_IMAGE", "images/mvp");
		itemEntryTemplate = FileUtil.parseFileInJarToString("/templates/itemEntryTemplate.html");
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
