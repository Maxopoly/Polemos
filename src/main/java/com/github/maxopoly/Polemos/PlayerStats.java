package com.github.maxopoly.Polemos;

public class PlayerStats {

	private static final double spamClickThreshHold = 1.5;
	private static final double critClickThreshHold = 3.0;
	private static final double maxHealPerSplashHealth = 8.0;

	private String name;
	private int hitsDealt;
	private int hitsTaken;
	private int deaths;
	private int kills;
	private double damageDealt;
	private double damageTaken;
	private int spamHitsDealt;
	private int normalHitsDealt;
	private int critHitsDealt;
	private int spamHitsTaken;
	private int normalHitsTaken;
	private int critHitsTaken;
	private int buffsUsed;
	private int splashHealthUsed;
	private double healthFromSplash;
	private int pearlsThrown;


	public PlayerStats(String name) {
		this.name = name;
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

	public String getName() {
		return name;
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

	public void addDamageDealt(double damage) {
		this.damageDealt += damage;
		hitsDealt++;
		if (damage < spamClickThreshHold) {
			spamHitsDealt++;
		}
		else {
			if (damage < critClickThreshHold) {
				normalHitsDealt++;
			}
			else {
				critHitsDealt++;
			}
		}
	}

	public void addDamageTaken(double damage) {
		this.damageTaken += damage;
		hitsTaken++;
		if (damage < spamClickThreshHold) {
			spamHitsTaken++;
		}
		else {
			if (damage < critClickThreshHold) {
				normalHitsTaken++;
			}
			else {
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

	public void addBuffUsed() {
		buffsUsed++;
	}

	public void addSplashHealthUsed() {
		splashHealthUsed++;
	}

	public void addHealFromSplashHealth(double heal) {
		healthFromSplash += heal;
	}

	public int getBuffsUsed() {
		return buffsUsed;
	}

	public int getSplashHealthUsed() {
		return splashHealthUsed;
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

	public double getSplashHealEffectiveness() {
		if (splashHealthUsed == 0) {
			return 0.0;
		}
		return healthFromSplash / (splashHealthUsed * maxHealPerSplashHealth);
	}

}
