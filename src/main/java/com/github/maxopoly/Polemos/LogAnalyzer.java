package com.github.maxopoly.Polemos;

import com.github.maxopoly.Polemos.action.AbstractAction;
import com.github.maxopoly.Polemos.action.AncientIngotRepairAction;
import com.github.maxopoly.Polemos.action.DestroyPowerSource;
import com.github.maxopoly.Polemos.action.HealAction;
import com.github.maxopoly.Polemos.action.HealAction.HealReason;
import com.github.maxopoly.Polemos.action.KillAction;
import com.github.maxopoly.Polemos.action.OverloadPowerSource;
import com.github.maxopoly.Polemos.action.PearlThrowAction;
import com.github.maxopoly.Polemos.action.PlayerFireArrow;
import com.github.maxopoly.Polemos.action.PlayerHitGolem;
import com.github.maxopoly.Polemos.action.PlayerKillGolem;
import com.github.maxopoly.Polemos.action.PlayerMetaData;
import com.github.maxopoly.Polemos.action.PlayerShootGolem;
import com.github.maxopoly.Polemos.action.PotionConsumptionAction;
import com.github.maxopoly.Polemos.action.playerDamaged.GolemHitPlayerAction;
import com.github.maxopoly.Polemos.action.playerDamaged.PearlDamagePlayer;
import com.github.maxopoly.Polemos.action.playerDamaged.PlayerHitPlayerAction;
import com.github.maxopoly.Polemos.action.playerDamaged.PlayerShootPlayer;
import com.github.maxopoly.Polemos.action.playerDamaged.TntModuleDamagePlayer;
import com.github.maxopoly.Polemos.model.BattleMetaData;
import com.github.maxopoly.Polemos.model.DamageType;
import com.github.maxopoly.Polemos.model.PlayerState;
import com.github.maxopoly.Polemos.model.Potion;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;

public class LogAnalyzer {

	private static final String playerNamePattern = "([a-zA-Z0-9_]+)";
	private static final String hpPattern = "(\\(\\d{1,3}\\.\\d{1,2} hp\\))";
	private static final String damagePattern = " for (-?\\d+\\.\\d+(?:E-?\\d+)?) damage";
	private static final String damageMitigationPattern = damagePattern
			+ " \\(-?\\d+\\.\\d+(?:E-?\\d+)? mitigated, ([A-Z_]+)\\)";
	private static final String playerNameAndHpPattern = playerNamePattern + " " + hpPattern;

	private static final Pattern timeParser = Pattern.compile("(\\[\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\]) (.+)");

	private static final Pattern playerActionRegex = Pattern.compile(playerNameAndHpPattern + " ([a-zA-Z]+) (.+)");

	private static final Pattern ancientIngotRepairRegex = Pattern.compile("their (.+) with an Ancient Ingot");

	private static final Pattern playerHitPlayerRegex = Pattern.compile(playerNameAndHpPattern
			+ damageMitigationPattern);

	private static final Pattern playerHitGolemRegex = Pattern.compile("([A-z ]+) Protector " + hpPattern
			+ damageMitigationPattern);

	private static final Pattern playerKillPlayerRegex = Pattern.compile(playerNameAndHpPattern);

	private static final Pattern playerKillGolemRegex = Pattern.compile("([A-z ]+) Protector " + hpPattern);

	private static final Pattern golemHitPlayerRegex = Pattern.compile("([A-z ]+) Protector " + hpPattern + " hit "
			+ playerNameAndHpPattern + damageMitigationPattern);

	private static final Pattern potRegex = Pattern.compile("a [A-z ]+ Potion of ([A-z ]+) ([0-9])");

	private static final Pattern tntHitRegex = Pattern.compile("Module \\(TnT\\) hit " + playerNameAndHpPattern
			+ damagePattern);

	private static final Pattern overLoadPowerSourceRegex = Pattern.compile(playerNamePattern
			+ " ((?:destroyed)|(?:overloaded)) a Power Source");

	private static final Pattern pearlHitRegex = Pattern.compile("entity\\.ThrownEnderpearl\\.name hit "
			+ playerNameAndHpPattern + damageMitigationPattern);

	private static final Pattern healRegex = Pattern.compile("([0-9]+\\.[0-9]+) health from ([A-z_]+)");

	private static final Pattern locationRegex = Pattern.compile("Location: ([A-z]+)");

	private List<String> lines;
	private List<AbstractAction> actions;
	private long absoluteStartingTime;
	private int parsingStage;
	private BattleMetaData metaData;

	public LogAnalyzer(List<String> lines) {
		this.actions = new LinkedList<AbstractAction>();
		this.lines = lines;
		this.absoluteStartingTime = -1;
		this.parsingStage = 0;
	}

	public BattleMetaData getMetaData() {
		return metaData;
	}

	public void parse() {
		for (String line : lines) {
			String data;
			Matcher match = timeParser.matcher(line);
			if (!match.matches()) {
				System.out.println("Unknown line without timestamp, ignoring:");
				System.out.println(line);
				return;
			}
			String timeString = match.group(1);
			data = match.group(2);
			long absoluteTime = parseTimeOut(timeString);
			if (absoluteStartingTime == -1) {
				// no starting time set yet, so let's set it based on the first message
				absoluteStartingTime = absoluteTime;
			}
			long relativeTime = absoluteTime - absoluteStartingTime;
			if (relativeTime < 0) {
				// if the match occured at midnight, the initial timestamp will be in the old day and the later ones in
				// the
				// new day, meaning relative times are < 0
				relativeTime += (1000 * 60 * 60 * 24);
			}
			if (data.startsWith("===")) {
				parsingStage++;
				continue;
			}
			switch (parsingStage) {
				case 1:
					parseActionLine(data, relativeTime);
					break;
				case 2:
					if (data.equals("Fight Reinforced!")) {
						metaData.setAttackersWon();
					}
					break;
				case 3:
					//attackers
					String group = data.split(":")[0].trim();
					String[] members = data.split(":")[1].split(",");
					metaData.registerGroup(group, true);
					for (String member : members) {
						metaData.registerPlayer(member.trim(), group);
					}
					break;
				case 4:
					//defenders
					String groupD = data.split(":")[0].trim();
					String[] membersD = data.split(":")[1].split(",");
					metaData.registerGroup(groupD, false);
					for (String member : membersD) {
						metaData.registerPlayer(member.trim(), groupD);
					}
					break;
				case 5:
					//gear
					if (data.contains("{\"player\":\"") && data.endsWith("}")) {
						// json
						parsePlayerInfoJson(data);
						break;
					}
					System.out.println("Unknown line when expecting json");
					System.out.println(data);
				default:
					break;
			}
		}
	}

	public List<AbstractAction> getResult() {
		return actions;
	}

	private void parsePlayerInfoJson(String data) {
		JSONObject json = new JSONObject(data);
		String name = json.getString("player");
		UUID uuid = UUID.fromString(json.getString("uuid"));
		actions.add(new PlayerMetaData(name, uuid));
	}

	private void parseActionLine(String line, long relativeTime) {
		Matcher actionRegex = playerActionRegex.matcher(line);
		if (!actionRegex.matches()) {
			// Not the normal pattern that starts like "Maxopoly (5.55 hp)", so check special cases
			if (line.startsWith("TowerTnT")) {
				// this is bugged and shows wrong values. It's also duplicated through damage done by "Module (TnT)",
				// which shows the right numbers.
				// Let's ignore it
				return;
			}
			if (parseTntOptional(relativeTime, line)) {
				// recognized as tnt damage
				return;
			}
			if (parsePearlDamageOptional(relativeTime, line)) {
				// recognized as pearl damage
				return;
			}
			if (parseGolemHitPlayerOptional(relativeTime, line)) {
				// recognized as golem hit
				return;
			}
			if (parsePowerSourceOptional(relativeTime, line)) {
				// recognized as power source destruction
				return;
			}
			if (parseBattleLocationOptional(relativeTime, line)) {
				// recognized as power source destruction
				return;
			}
			System.out.println("Unknown line not matching anything known, ignoring:");
			System.out.println(line);
			return;
		}
		String playerName = actionRegex.group(1);
		String hp = actionRegex.group(2);
		PlayerState player = parsePlayerState(playerName, hp);
		if (player.isNotAPlayer()) {
			return;
		}
		String action = actionRegex.group(3);
		String data = actionRegex.group(4);
		switch (action) {
			case "hit":
				parsePlayerHitByPlayer(player, relativeTime, data, true);
				return;
			case "shot":
				parsePlayerHitByPlayer(player, relativeTime, data, false);
				return;
			case "fired":
				parsePlayerFire(player, relativeTime, data);
				return;
			case "regained":
				parsePlayerHealed(player, relativeTime, data);
				return;
			case "killed":
				parsePlayerKilledByPlayer(player, relativeTime, data);
				return;
			case "repaired":
				parseAncientIngotRepair(player, relativeTime, data);
			case "took":
			case "consumed":
				return;
		}
		System.out.println("Unknown action, ignoring " + action);
		System.out.println(line);
		return;
	}

	/**
	 * Parses a timestamp in the format [19:45:26.170] into a long
	 *
	 * @return Number between 0 and 60*60*1000*24, representing the daytime
	 */
	private long parseTimeOut(String timeStamp) {
		// remove brackets
		timeStamp = timeStamp.substring(1, timeStamp.length() - 1);
		String[] times = timeStamp.split(":");
		long hours = Long.parseLong(times[0]);
		long minutes = Long.parseLong(times[1]);
		String[] secSplit = times[2].split("\\.");
		long seconds = Long.parseLong(secSplit[0]);
		long milliSeconds = Long.parseLong(secSplit[1]);
		return milliSeconds + (seconds * 1000) + (minutes * 1000 * 60) + (hours * 1000 * 60 * 60);
	}

	private static PlayerState parsePlayerState(String name, String hpString) {
		// hp string is expected to look like: '(13.22 hp)' with the part before and after the decimal possibly being
		// only one number
		hpString = hpString.split(" ")[0];
		if (hpString.startsWith("(")) {
			hpString = hpString.substring(1, hpString.length());
		}
		Float hp = Float.parseFloat(hpString);
		return new PlayerState(name, hp);
	}

	private boolean parseTntOptional(long time, String data) {
		Matcher tntMatcher = tntHitRegex.matcher(data);
		if (tntMatcher.matches()) {
			// Tnt module mounted on an inhib
			String playerHit = tntMatcher.group(1);
			String hp = tntMatcher.group(2);
			double damage = Double.parseDouble(tntMatcher.group(3));
			actions.add(new TntModuleDamagePlayer(time, parsePlayerState(playerHit, hp), damage));
			return true;
		}
		return false;
	}

	public void parseAncientIngotRepair(PlayerState player, long time, String data) {
		Matcher match = ancientIngotRepairRegex.matcher(data);
		if (!match.matches()) {
			System.out.println("Unknown ingot line, ignoring:");
			System.out.println(data);
			return;
		}
		actions.add(new AncientIngotRepairAction(time, player, match.group(1)));
	}

	private boolean parsePearlDamageOptional(long time, String data) {
		Matcher pearlMatcher = pearlHitRegex.matcher(data);
		if (pearlMatcher.matches()) {
			// Fall damage from ender pearl teleport
			String playerHit = pearlMatcher.group(1);
			String hp = pearlMatcher.group(2);
			double damage = Double.parseDouble(pearlMatcher.group(3));
			actions.add(new PearlDamagePlayer(time, parsePlayerState(playerHit, hp), damage));
			return true;
		}
		return false;
	}

	private boolean parseBattleLocationOptional(long time, String data) {
		Matcher locMatcher = locationRegex.matcher(data);
		if (locMatcher.matches()) {
			// Golem hitting a player
			String loc = locMatcher.group(1);
			metaData = new BattleMetaData(loc);
			return true;
		}
		return false;
	}

	private boolean parseGolemHitPlayerOptional(long time, String data) {
		Matcher golemMatcher = golemHitPlayerRegex.matcher(data);
		if (golemMatcher.matches()) {
			// Golem hitting a player
			String golemOwner = golemMatcher.group(1);
			String golemHp = golemMatcher.group(2);
			String playerName = golemMatcher.group(3);
			String playerHp = golemMatcher.group(4);
			double damage = Double.parseDouble(golemMatcher.group(5));
			actions.add(new GolemHitPlayerAction(time, parsePlayerState(playerName, playerHp), damage, golemOwner,
					parsePlayerState(golemOwner, golemHp).getHP()));
			return true;
		}
		return false;
	}

	private boolean parsePowerSourceOptional(long time, String data) {
		Matcher powerMatcher = overLoadPowerSourceRegex.matcher(data);
		if (powerMatcher.matches()) {
			// Golem hitting a player
			String player = powerMatcher.group(1);
			String action = powerMatcher.group(2);
			if (action.equals("overloaded")) {
				actions.add(new OverloadPowerSource(time, new PlayerState(player, Float.NaN)));
			} else {
				actions.add(new DestroyPowerSource(time, new PlayerState(player, Float.NaN)));
			}
			return true;
		}
		return false;
	}

	private void parsePlayerHitByPlayer(PlayerState player, long time, String data, boolean melee) {
		Matcher match = playerHitPlayerRegex.matcher(data);
		if (!match.matches()) {
			// might be a golem getting hit
			Matcher golemMatch = playerHitGolemRegex.matcher(data);
			if (!golemMatch.matches()) {
				System.out.println("Unknown hit line, ignoring:");
				System.out.println(data);
				return;
			}
			String owner = golemMatch.group(1);
			String hp = golemMatch.group(2);
			double damage = Double.parseDouble(golemMatch.group(3));
			if (melee) {
				actions.add(new PlayerHitGolem(time, player, damage, owner, parsePlayerState(owner, hp).getHP()));
			} else {
				actions.add(new PlayerShootGolem(time, player, damage, owner, parsePlayerState(owner, hp).getHP()));
			}
			return;
		}
		PlayerState victim = parsePlayerState(match.group(1), match.group(2));
		if (victim.isNotAPlayer()) {
			return;
		}
		double damage = Double.parseDouble(match.group(3));
		DamageType type = DamageType.valueOf(match.group(4));
		if (melee) {
			actions.add(new PlayerHitPlayerAction(time, player, victim, damage, type));
		} else {
			actions.add(new PlayerShootPlayer(time, player, victim, damage));
		}
	}

	private void parsePlayerFire(PlayerState player, long time, String data) {
		Matcher match = potRegex.matcher(data);
		if (match.matches()) {
			Potion.PotionType potType = Potion.PotionType
					.valueOf(match.group(1).trim().replace(" ", "_").toUpperCase());
			int level = Integer.parseInt(match.group(2));
			actions.add(new PotionConsumptionAction(time, player, new Potion(level, potType, true)));
			return;
		}
		switch (data.split(" ")[1]) {
			case "entity.ThrownEnderpearl.name":
				actions.add(new PearlThrowAction(time, player));
				return;
			case "Arrow":
				actions.add(new PlayerFireArrow(time, player));
				return;
			case "unknown":
				// the logs are broken, idk what this is, but sometimes the logs say stuff like
				// "Commander_12 (13.35 hp) fired a unknown"
				return;
		}
		System.out.println("Unknown fire line, ignoring:");
		System.out.println(data);
	}

	private void parsePlayerKilledByPlayer(PlayerState player, long time, String data) {
		Matcher match = playerKillPlayerRegex.matcher(data);
		if (!match.matches()) {
			Matcher golemMatch = playerKillGolemRegex.matcher(data);
			if (!golemMatch.matches()) {
				System.out.println("Unknown kill line, ignoring:");
				System.out.println(data);
			}
			String golemOwner = golemMatch.group(1);
			actions.add(new PlayerKillGolem(time, player, golemOwner));
			return;
		}
		PlayerState victim = parsePlayerState(match.group(1), match.group(2));
		if (victim.isNotAPlayer()) {
			return;
		}
		actions.add(new KillAction(time, player, victim));
	}

	private void parsePlayerHealed(PlayerState player, long time, String data) {
		Matcher match = healRegex.matcher(data);
		if (!match.matches()) {
			System.out.println("Unknown heal line, ignoring:");
			System.out.println(data);
			return;
		}
		double amount = Float.parseFloat(match.group(1));
		HealReason reason = HealReason.valueOf(match.group(2));
		amount = Math.min(20.0 - player.getHP(), amount);
		actions.add(new HealAction(time, player, amount, reason));
	}

}
