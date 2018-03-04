package com.github.maxopoly.Polemos.model;

import java.util.Objects;

public class Potion {

	public enum PotionType {
		FIRE_RESISTANCE("Fire Res"), STRENGTH("Strength"), SWIFTNESS("Speed"), INVISIBILITY("Invis"), REGENERATION("Regen"),
		HEALING("Health"), POISON("Poison"), SLOWNESS("Slowness"), WEAKNESS("Weakness");

		private String prettyName;

		private PotionType(String prettyName) {
			this.prettyName = prettyName;
		}

		public String getPrettyName() {
			return prettyName;
		}

	}

	private final int level;
	private final boolean splash;
	private final PotionType type;

	public Potion(int level, PotionType type, boolean splash) {
		this.level = level;
		this.type = type;
		this.splash = splash;
	}

	public int getLevel() {
		return level;
	}

	public boolean isSplash() {
		return splash;
	}

	public PotionType getType() {
		return type;
	}

	public String getImagePath() {
		return "";
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Potion)) {
			return false;
		}
		Potion p = (Potion) o;
		return p.level == level && p.type == type && p.splash == splash;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type.ordinal(), level, splash);
	}
}
