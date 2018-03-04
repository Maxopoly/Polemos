package com.github.maxopoly.Polemos.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class PlayerKill {

	private String victim;
	private Map<String, Double> totalDamageContributions;
	private double totalDamage;
	private String lastHit;
	private long time;

	public PlayerKill(long time, String victim, String killer) {
		this.victim = victim;
		this.time = time;
		this.lastHit = killer;
		this.totalDamage = 0.0;
		this.totalDamageContributions = new HashMap<String, Double>();
	}

	public void addDamageContribution(String name, double damage) {
		Double contribution = totalDamageContributions.get(name);
		if (contribution == null) {
			contribution = 0.0;
		}
		contribution += damage;
		totalDamage += damage;
		totalDamageContributions.put(name, contribution);
	}

	public Map <String, Double> getTotalDamageContributions() {
		return new HashMap<String, Double>(totalDamageContributions);
	}

	public Map <String, Double> getRelativeDamageContributions() {
		Map<String, Double> relCon = new HashMap<String, Double>();
		for(Entry<String, Double> entry : totalDamageContributions.entrySet()) {
			relCon.put(entry.getKey(), entry.getValue() / totalDamage);
		}
		return relCon;
	}

	public double getRelativeContribution(String name) {
		Double contribution = getRelativeDamageContributions().get(name);
		if (contribution == null) {
			return 0.0;
		}
		return contribution;
	}

	public double getTotalDamageContribution(String name) {
		Double contribution = totalDamageContributions.get(name);
		if (contribution == null) {
			return 0.0;
		}
		return contribution;
	}

	public String getVictim() {
		return victim;
	}

	public String getKiller() {
		return lastHit;
	}

	public long getTime() {
		return time;
	}

}
