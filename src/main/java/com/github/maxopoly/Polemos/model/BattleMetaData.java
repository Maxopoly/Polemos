package com.github.maxopoly.Polemos.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BattleMetaData {

	private String location;
	private List<String> attackerGroups;
	private List<String> defenderGroups;
	private Map<String, String> playerToGroup;
	private boolean attackersWon;

	public BattleMetaData(String location) {
		this.location = location;
		this.attackerGroups = new LinkedList<String>();
		this.defenderGroups = new LinkedList<String>();
		this.playerToGroup = new HashMap<String, String>();
	}

	public void registerPlayer(String name, String group) {
		playerToGroup.put(name, group);
	}

	public void registerGroup(String name, boolean isAttacker) {
		if(isAttacker) {
			attackerGroups.add(name);
		}
		else {
			defenderGroups.add(name);
		}
	}

	public int getPlayerCount(String group) {
		int count = 0;
		for(Entry<String, String> entry : playerToGroup.entrySet()) {
			if (entry.getValue().equals(group)) {
				count++;
			}
		}
		return count;
	}

	public int getAttackerCount() {
		int count = 0;
		for(Entry<String, String> entry : playerToGroup.entrySet()) {
			if (attackerGroups.contains(entry.getValue())) {
				count++;
			}
		}
		return count;
	}

	public int getDefenderCount() {
		int count = 0;
		for(Entry<String, String> entry : playerToGroup.entrySet()) {
			if (defenderGroups.contains(entry.getValue())) {
				count++;
			}
		}
		return count;
	}

	public void setAttackersWon() {
		attackersWon = true;
	}

	public String getGroup(String player) {
		return playerToGroup.get(player);
	}

	public boolean didAttackersWin() {
		return attackersWon;
	}

	public boolean isPlayerAttacker(String player) {
		return attackerGroups.contains(getGroup(player));
	}

	public boolean isGroupAttacker(String player) {
		return attackerGroups.contains(player);
	}

	public List <String> getAttackers() {
		return new LinkedList<String>(attackerGroups);
	}

	public List <String> getDefenders() {
		return new LinkedList<String>(defenderGroups);
	}

	public String getLocation() {
		return location;
	}
}
