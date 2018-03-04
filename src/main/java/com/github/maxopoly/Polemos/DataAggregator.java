package com.github.maxopoly.Polemos;

import com.github.maxopoly.Polemos.action.AbstractAction;
import com.github.maxopoly.Polemos.action.AncientIngotRepairAction;
import com.github.maxopoly.Polemos.action.HealAction;
import com.github.maxopoly.Polemos.action.HealAction.HealReason;
import com.github.maxopoly.Polemos.action.KillAction;
import com.github.maxopoly.Polemos.action.PearlThrowAction;
import com.github.maxopoly.Polemos.action.PlayerMetaData;
import com.github.maxopoly.Polemos.action.PotionConsumptionAction;
import com.github.maxopoly.Polemos.action.playerDamaged.PlayerHitPlayerAction;
import com.github.maxopoly.Polemos.model.BattleMetaData;
import com.github.maxopoly.Polemos.model.PlayerKill;
import com.github.maxopoly.Polemos.model.PlayerStats;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

public class DataAggregator {

	private static final DecimalFormat doubleFormat = new DecimalFormat("####.##");

	private ArrayList<AbstractAction> actions;
	private Map<String, PlayerStats> stats;
	private BattleMetaData metaData;
	private List <PlayerKill> playerKills;

	private static final long assistTimeOut = 15000;
	private static final double assistTreshhold = 0.2;

	public DataAggregator(List<AbstractAction> actions, BattleMetaData meta) {
		this.actions = new ArrayList<AbstractAction>(actions);
		this.metaData = meta;
		this.playerKills = new LinkedList<PlayerKill>();
		this.stats = new HashMap<String, PlayerStats>();
		analyseDamageDealt();
		analyseKills();
		analyseConsumables();
	}

	public Map <String, PlayerStats> getStats() {
		return stats;
	}

	public List <PlayerKill> getPlayerKills() {
		return new LinkedList<PlayerKill>(playerKills);
	}

	private void analyseKills() {
		for (int i = 0; i < actions.size(); i++) {
			AbstractAction action = actions.get(i);
			if (action instanceof KillAction) {
				KillAction kill = (KillAction) action;
				PlayerStats killerStats = getStats(kill.getMainPlayer().getName());
				PlayerStats victimStats = getStats(kill.getVictim().getName());
				killerStats.addKill();
				victimStats.addDeath();
				PlayerKill pk = reverseTrackKill(i);
				for(Entry <String, Double> entry : pk.getRelativeDamageContributions().entrySet()) {
					if(entry.getValue() > assistTreshhold && !(entry.getKey().equals(pk.getKiller()))) {
						getStats(entry.getKey()).addAssist();
					}
				}
				playerKills.add(pk);
			}
		}
	}

	private PlayerKill reverseTrackKill(int index) {
		KillAction killAction = (KillAction) actions.get(index);
		PlayerKill kill = new PlayerKill(killAction.getTime(), killAction.getVictim().getName(), killAction.getMainPlayer().getName());
		ListIterator<AbstractAction> iter = actions.listIterator(index);
		long lastHit = killAction.getTime();
		while(iter.hasPrevious()) {
			AbstractAction action = iter.previous();
			if (lastHit - action.getTime() > assistTimeOut) {
				break;
			}
			if (action instanceof PlayerHitPlayerAction) {
				PlayerHitPlayerAction hit = (PlayerHitPlayerAction) action;
				if(hit.getVictim().getName().equals(kill.getVictim())) {
					kill.addDamageContribution(hit.getAttacker().getName(), hit.getDamage());
					lastHit = hit.getTime();
				}
			}
		}
		return kill;
	}

	private void analyseDamageDealt() {
		for (AbstractAction action : actions) {
			if (action instanceof PlayerHitPlayerAction) {
				PlayerHitPlayerAction hit = (PlayerHitPlayerAction) action;
				PlayerStats attackerStats = getStats(hit.getAttacker().getName());
				PlayerStats victimStats = getStats(hit.getVictim().getName());
				attackerStats.addDamageDealt(hit.getDamage());
				victimStats.addDamageTaken(hit.getDamage());
			}
		}
	}

	private void analyseConsumables() {
		for (AbstractAction action : actions) {
			if (action instanceof PotionConsumptionAction) {
				PotionConsumptionAction pot = (PotionConsumptionAction) action;
				PlayerStats potStats = getStats(pot.getMainPlayer().getName());
				potStats.addPotConsumed(pot.getPotion());
				continue;
			}
			if (action instanceof HealAction) {
				HealAction pot = (HealAction) action;
				PlayerStats potStats = getStats(pot.getMainPlayer().getName());
				if (pot.getReason() == HealReason.MAGIC) {
					//health pot
					potStats.addHealFromSplashHealth(pot.getAmount());
				}
				continue;
			}
			if (action instanceof PearlThrowAction) {
				PearlThrowAction pearl = (PearlThrowAction) action;
				PlayerStats pearlStats = getStats(pearl.getMainPlayer().getName());
				pearlStats.addPearlThrow();
				continue;
			}
			if (action instanceof AncientIngotRepairAction) {
				getStats(action.getMainPlayer().getName()).addAncientIngotUsed();
			}
			if (action instanceof PlayerMetaData) {
				PlayerMetaData meta = (PlayerMetaData) action;
				getStats(meta.getName()).setUUID(meta.getUUID());
			}
		}
	}

	public static Map <String, PlayerStats> genGroupStatsTotal(Map <String, PlayerStats> playerStats, BattleMetaData meta) {
		Map <String, PlayerStats> groupStats = new HashMap<String, PlayerStats>();
		for(PlayerStats stat : playerStats.values()) {
			String group = meta.getGroup(stat.getName());
			if (group == null) {
				System.out.println("No group for " + stat.getName());
				continue;
			}
			PlayerStats groupStat = groupStats.get(group);
			if (groupStat == null) {
				groupStat = new PlayerStats(group);
				groupStats.put(group, groupStat);
			}
			groupStat.merge(stat);
		}
		return groupStats;
	}

	private PlayerStats getStats(String name) {
		PlayerStats killerStats = stats.get(name);
		if (killerStats == null) {
			killerStats = new PlayerStats(name);
			stats.put(name, killerStats);
		}
		return killerStats;
	}

	public static List <String> getKillBreakdown(List <PlayerKill> kills, String playerName) {
		List<String> result = new LinkedList<String>();
		for(PlayerKill kill: kills) {
			if (kill.getKiller().equals(playerName)) {
				result.add(String.format("Killed %s at %s (%s %% of damage)", kill.getVictim(), formatTime(kill.getTime()),
						doubleFormat.format(kill.getRelativeContribution(kill.getKiller())* 100)));
			}
		}
		if (result.size() == 0) {
			result.add(playerName + " didn't get any kills :(");
		}
		return result;
	}

	public static List <String> getDeathBreakdown(List <PlayerKill> kills, String playerName) {
		List<String> result = new LinkedList<String>();
		for(PlayerKill kill: kills) {
			if (kill.getVictim().equals(playerName)) {
				result.add(String.format("Killed by %s at %s", kill.getKiller(), formatTime(kill.getTime())));
			}
		}
		if (result.size() == 0) {
			result.add(playerName + " didn't die :)");
		}
		return result;
	}

	public static List <String> getAssistBreakdown(List <PlayerKill> kills, String playerName) {
		List<String> result = new LinkedList<String>();
		for(PlayerKill kill: kills) {
			if (!kill.getKiller().equals(playerName) && kill.getRelativeContribution(playerName) >=  assistTreshhold) {
				result.add(String.format("Assisted kill on %s at %s with %s damage (%s %% of damage)", kill.getVictim(), formatTime(kill.getTime()),
						doubleFormat.format(kill.getTotalDamageContribution(playerName)), doubleFormat.format(kill.getRelativeContribution(playerName)* 100)));
			}
		}
		if (result.size() == 0) {
			result.add(playerName + " didn't get any assists :(");
		}
		return result;
	}

	private static String formatTime(long time) {
		int seconds = (int) (time / 1000L);
		return String.format("%dm %ds", seconds / 60, seconds % 60);
	}

	public static Map <String, PlayerStats> genGroupStatsAvg(Map <String, PlayerStats> playerStats, BattleMetaData meta) {
		Map <String, List <PlayerStats>> collectedStats = new HashMap<String, List<PlayerStats>>();
		for(PlayerStats stat : playerStats.values()) {
			String group = meta.getGroup(stat.getName());
			if (group == null) {
				System.out.println("No group for " + stat.getName());
				continue;
			}
			List<PlayerStats> groupStat = collectedStats.get(group);
			if (groupStat == null) {
				groupStat = new LinkedList<PlayerStats>();
				collectedStats.put(group, groupStat);
			}
			groupStat.add(stat);
		}
		Map <String, PlayerStats> groupStats = new HashMap<String, PlayerStats>();
		for(Entry <String, List<PlayerStats>> entry : collectedStats.entrySet()) {
			String group = entry.getKey();
			groupStats.put(group, PlayerStats.calculateAverage(group, entry.getValue()));
		}
		return groupStats;
	}

	public static PlayerStats [] genSideStatsTotal(Map <String, PlayerStats> playerStats, BattleMetaData meta) {
		PlayerStats [] sideStats = new PlayerStats [2];
		sideStats[0] = new PlayerStats("Attacker");
		sideStats[1] = new PlayerStats("Defender");
		for(PlayerStats stat : playerStats.values()) {
			PlayerStats toAdd = meta.isPlayerAttacker(stat.getName()) ? sideStats[0] : sideStats[1];
			toAdd.merge(stat);
		}
		return sideStats;
	}

	public static PlayerStats [] genSideStatsAvg(Map <String, PlayerStats> playerStats, BattleMetaData meta) {
		List [] sideStats = new List [2];
		sideStats[0] = new LinkedList<PlayerStats>();
		sideStats[1] = new LinkedList<PlayerStats>();
		for(PlayerStats stat : playerStats.values()) {
			if(meta.isPlayerAttacker(stat.getName())) {
				sideStats [0].add(stat);
			}
			else {
				sideStats[1].add(stat);
			}
		}
		PlayerStats result [] = new PlayerStats [2];
		result[0] = PlayerStats.calculateAverage("Attacker", sideStats [0]);
		result[1] = PlayerStats.calculateAverage("Defender", sideStats [1]);
		return result;
	}
}
