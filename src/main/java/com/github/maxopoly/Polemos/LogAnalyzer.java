package com.github.maxopoly.Polemos;

import com.github.maxopoly.Polemos.action.AbstractAction;
import com.github.maxopoly.Polemos.action.HealAction;
import com.github.maxopoly.Polemos.action.HealAction.HealReason;
import com.github.maxopoly.Polemos.action.KillAction;
import com.github.maxopoly.Polemos.action.PearlThrowAction;
import com.github.maxopoly.Polemos.action.PlayerFireArrow;
import com.github.maxopoly.Polemos.action.PlayerHitPlayerAction;
import com.github.maxopoly.Polemos.action.ThrewPotAction;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogAnalyzer {

	private List<String> lines;
	private List <AbstractAction> actions;

	private static final Pattern initialRegex = Pattern
			.compile("(\\[\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\]) ([a-zA-Z0-9_]+) (\\(\\d{1,2}\\.\\d{1,2} hp\\)) ([a-zA-Z]+) (.+)");

	private static final Pattern playerHitPlayerRegex = Pattern
			.compile("([a-zA-Z0-9_]+) (\\(\\d{1,2}\\.\\d{1,2} hp\\)) for (\\d+\\.\\d+) damage \\(-?\\d+\\.\\d+ mitigated, ([A-Z_]+)\\)");

	private static final Pattern playerKillPlayerRegex = Pattern
			.compile("([a-zA-Z0-9_]+) (\\(\\d{1,2}\\.\\d{1,2} hp\\))");

	private static final Pattern potRegex = Pattern.compile("a [A-z ]+ Potion of ([A-z ]+) [0-9]");

	private static final Pattern tntHitRegex = Pattern.compile("");

	private static final Pattern healRegex = Pattern.compile("([0-9]+\\.[0-9]+) health from ([A-z_]+)");

	public LogAnalyzer(List<String> lines) {
		this.actions = new LinkedList<AbstractAction>();
		this.lines = lines;
	}

	public void parse() {
		for (String line : lines) {
			processLine(line);
		}
	}

	public List<AbstractAction> getResult() {
		return actions;
	}

	private void processLine(String line) {
		if (line.contains("{\"player\":\"") && line.endsWith("}")) {
			//json
			return;
		}
		Matcher match = initialRegex.matcher(line);
		if (!match.matches()) {
			System.out.println("Unknown line, ignoring:");
			System.out.println(line);
			return;
		}
		String playerName = match.group(2);
		String hp = match.group(3);
		PlayerState player = parsePlayerState(playerName, hp);
		if (player.isNotAPlayer()) {
			return;
		}
		String action = match.group(4);
		String data = match.group(5);
		long time = 0;
		switch (action) {
			case "hit":
				parsePlayerHitByPlayer(player, time, data);
				return;
			case "fired":
				parsePlayerFire(player, time, data);
				return;
			case "regained":
				parsePlayerHealed(player, time, data);
				return;
			case "killed":
				parsePlayerKilledByPlayer(player, time, data);
				return;
			case "took":
			case "consumed":
				return;
		}
		System.out.println("Unknown action, ignoring " + action);
		System.out.println(line);
		return;
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

	private void parsePlayerHitByPlayer(PlayerState player, long time, String data) {
		Matcher match = playerHitPlayerRegex.matcher(data);
		if (!match.matches()) {
			//System.out.println("Unknown hit line, ignoring:");
			//System.out.println(data);
			return;
		}
		PlayerState victim = parsePlayerState(match.group(1), match.group(2));
		if (victim.isNotAPlayer()) {
			return;
		}
		double damage = Double.parseDouble(match.group(3));
		PlayerHitPlayerAction.AttackType type = PlayerHitPlayerAction.AttackType.valueOf(match.group(4));
		actions.add(new PlayerHitPlayerAction(time, player, victim, damage, type));
	}

	private void parsePlayerFire(PlayerState player, long time, String data) {
		Matcher match = potRegex.matcher(data);
		if (match.matches()) {
			boolean health = match.group(1).trim().equalsIgnoreCase("Healing");
			actions.add(new ThrewPotAction(time, player, health));
			return;
		}
		switch (data.split(" ") [1]) {
			case "entity.ThrownEnderpearl.name":
				actions.add(new PearlThrowAction(time, player));
				return;
			case "Arrow":
				actions.add(new PlayerFireArrow(time, player));
				return;
			case "unknown":
				//the logs are broken, idk what this is, but sometimes the logs say stuff like "Commander_12 (13.35 hp) fired a unknown"
				return;
		}
		System.out.println("Unknown fire line, ignoring:");
		System.out.println(data);
	}

	private void parsePlayerKilledByPlayer(PlayerState player, long time, String data) {
		Matcher match = playerKillPlayerRegex.matcher(data);
		if (!match.matches()) {
			System.out.println("Unknown kill line, ignoring:");
			System.out.println(data);
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
