package com.github.maxopoly.Polemos.action.playerDamaged;

import com.github.maxopoly.Polemos.model.PlayerState;

import com.github.maxopoly.Polemos.action.AbstractAction;

public abstract class PlayerDamagedAction extends AbstractAction{

	private double damage;

	public PlayerDamagedAction(long time, PlayerState mainPlayer, double damage) {
		super(time, mainPlayer);
		this.damage = damage;
	}

	public double getDamage() {
		return damage;
	}

}
