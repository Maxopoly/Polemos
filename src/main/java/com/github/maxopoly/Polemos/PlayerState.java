package com.github.maxopoly.Polemos;

import java.util.HashSet;
import java.util.Set;

public class PlayerState {
	private final String name;
	private final float hp;

	private static Set<String> entities;

	static {
		entities = new HashSet<String>();
		entities.add("Zombie");
		entities.add("Spider");
		entities.add("Sheep");
		entities.add("Pig");
		entities.add("Cow");
		entities.add("Rabbit");
		entities.add("Creeper");
		entities.add("Endermite");
		entities.add("Skeleton");
	}

	public PlayerState(String name, float hp) {
		this.name = name;
		this.hp = hp;
	}

	public float getHP() {
		return hp;
	}

	public String getName() {
		return name;
	}

	public boolean isNotAPlayer() {
		return entities.contains(name);
	}
}
