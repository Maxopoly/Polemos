package com.github.maxopoly.Polemos.action.playerDamaged;

import com.github.maxopoly.Polemos.model.PlayerState;

public class PlayerShootPlayer extends PlayerDamagedAction {

	private PlayerState victim;

	public PlayerShootPlayer(long time, PlayerState attacker, PlayerState victim, double damage) {
		super(time, attacker, damage);
		this.victim = victim;
	}

	public PlayerState getShooter() {
		return getMainPlayer();
	}


	public PlayerState getVictim() {
		return victim;
	}

}
