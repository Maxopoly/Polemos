package com.github.maxopoly.Polemos.action.playerDamaged;

import com.github.maxopoly.Polemos.model.PlayerState;

public class GolemHitPlayerAction extends PlayerDamagedAction {

	private String golemOwner;
	private double golemHealth;

	public GolemHitPlayerAction(long time, PlayerState mainPlayer, double damage, String golemOwner, double golemHealth) {
		super(time, mainPlayer, damage);
		this.golemHealth = golemHealth;
		this.golemOwner = golemOwner;
	}

	public String getGolemOwner() {
		return golemOwner;
	}

	public double getGolemHealth() {
		return golemHealth;
	}


}
