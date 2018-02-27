package com.github.maxopoly.Polemos.model;

import com.github.maxopoly.Polemos.model.Potion.PotionType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStats {

	private static final double spamClickThreshHold = 1.5;
	private static final double critClickThreshHold = 3.0;
	private static final double maxHealPerSplashHealth = 8.0;

	private String name;
	private int hitsDealt;
	private int hitsTaken;
	private int deaths;
	private int kills;
	private int assists;
	private double damageDealt;
	private double damageTaken;
	private int spamHitsDealt;
	private int normalHitsDealt;
	private int critHitsDealt;
	private int spamHitsTaken;
	private int normalHitsTaken;
	private int critHitsTaken;
	private Map<Potion, Integer> potsUsed;
	private double healthFromSplash;
	private int pearlsThrown;
	private int ancientIngotsUsed;
	private Map<String, Integer> foodEaten;
	private UUID uuid;

	public PlayerStats(String name) {
		this.name = name;
		this.potsUsed = new HashMap<Potion, Integer>();
		this.foodEaten = new HashMap<String, Integer>();
	}

	public int getHitsDealt() {
		return hitsDealt;
	}

	public int getHitsTaken() {
		return hitsTaken;
	}

	public int getKills() {
		return kills;
	}

	public int getDeaths() {
		return deaths;
	}

	public int getAssists() {
		return assists;
	}

	public String getName() {
		return name;
	}

	public UUID getUUID() {
		return uuid;
	}

	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	public int getAncientIngotsUsed() {
		return ancientIngotsUsed;
	}

	public Map<String, Integer> getFoodEaten() {
		return new HashMap<String, Integer>(foodEaten);
	}

	public double getDamageDealtToTakenRatio() {
		return damageTaken != 0.0 ? damageDealt / damageTaken : 0.0;
	}

	public double getDamageDealt() {
		return damageDealt;
	}

	public double getDamageTaken() {
		return damageTaken;
	}

	public double getAverageDamageDealtPerHit() {
		return hitsDealt != 0 ? damageDealt / (hitsDealt) : 0.0;
	}

	public double getAverageDamageTakenPerHit() {
		return hitsTaken != 0 ? damageTaken / (hitsTaken) : 0.0;
	}

	public void addPotConsumed(Potion pot) {
		Integer count = potsUsed.get(pot);
		if (count == null) {
			count = 0;
		}
		potsUsed.put(pot, ++count);
	}

	public void addFoodEaten(String name) {
		Integer count = foodEaten.get(name);
		if (count == null) {
			count = 0;
		}
		foodEaten.put(name, ++count);
	}

	public void addAncientIngotUsed() {
		ancientIngotsUsed++;
	}

	public void addDamageDealt(double damage) {
		this.damageDealt += damage;
		hitsDealt++;
		if (damage < spamClickThreshHold) {
			spamHitsDealt++;
		} else {
			if (damage < critClickThreshHold) {
				normalHitsDealt++;
			} else {
				critHitsDealt++;
			}
		}
	}

	public void addDamageTaken(double damage) {
		this.damageTaken += damage;
		hitsTaken++;
		if (damage < spamClickThreshHold) {
			spamHitsTaken++;
		} else {
			if (damage < critClickThreshHold) {
				normalHitsTaken++;
			} else {
				critHitsTaken++;
			}
		}
	}

	public void addKill() {
		kills++;
	}

	public void addDeath() {
		deaths++;
	}

	public int getPearlsThrown() {
		return pearlsThrown;
	}

	public void addPearlThrow() {
		pearlsThrown++;
	}

	public void addHealFromSplashHealth(double heal) {
		healthFromSplash += heal;
	}

	public double getHealFromSplashHealth() {
		return healthFromSplash;
	}

	public int getSpamHitsTaken() {
		return spamHitsTaken;
	}

	public int getNormalHitsTaken() {
		return normalHitsTaken;
	}

	public int getCritHitsTaken() {
		return critHitsTaken;
	}

	public int getSpamHitsDealt() {
		return spamHitsDealt;
	}

	public int getNormalHitsDealt() {
		return normalHitsDealt;
	}

	public int getCritHitsDealt() {
		return critHitsDealt;
	}

	public double getNormalHitToCritDealtRatio() {
		if (critHitsDealt == 0) {
			return 0.0;
		}
		return critHitsDealt / ((double) critHitsDealt + (double) normalHitsDealt);
	}

	public int getPotionsUsed(Potion type) {
		Integer count = potsUsed.get(type);
		if (count == null) {
			return 0;
		}
		return count;
	}

	public Map<Potion, Integer> getPotionsUsed() {
		return new HashMap<Potion, Integer>(potsUsed);
	}

	public int getSplashHealthUsed() {
		return getPotionsUsed(new Potion(2, PotionType.HEALING, true));
	}

	public double getSplashHealEffectiveness() {
		if (getSplashHealthUsed() == 0) {
			return 0.0;
		}
		return healthFromSplash / (getSplashHealthUsed() * maxHealPerSplashHealth);
	}

}
