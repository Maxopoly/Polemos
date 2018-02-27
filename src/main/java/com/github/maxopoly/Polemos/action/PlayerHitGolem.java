package com.github.maxopoly.Polemos.action;

import com.github.maxopoly.Polemos.model.PlayerState;

public class PlayerHitGolem extends AbstractAction {

	private double damage;
	private String golemOwner;
	private double golemHealth;

	public PlayerHitGolem(long time, PlayerState mainPlayer, double damage, String golemOwner, double golemHealth) {
		super(time, mainPlayer);
		this.damage = damage;
		this.golemOwner = golemOwner;
		this.golemHealth = golemHealth;
	}

	public String getGolemOwner() {
		return golemOwner;
	}

	public double getDamage() {
		return damage;
	}

	public double getGolemHealth() {
		return golemHealth;
	}

}
