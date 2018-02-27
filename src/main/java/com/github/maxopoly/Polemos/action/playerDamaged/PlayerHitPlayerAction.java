package com.github.maxopoly.Polemos.action.playerDamaged;

import com.github.maxopoly.Polemos.model.PlayerState;

import com.github.maxopoly.Polemos.model.DamageType;

/**
 * A player hit another player melee
 *
 */
public class PlayerHitPlayerAction extends PlayerDamagedAction {

	private PlayerState victim;
	private DamageType type;

	public PlayerHitPlayerAction(long time, PlayerState attacker, PlayerState victim, double damage, DamageType type) {
		super(time, attacker, damage);
		this.victim = victim;
		this.type = type;
	}

	public PlayerState getAttacker() {
		return getMainPlayer();
	}

	public PlayerState getVictim() {
		return victim;
	}

	public DamageType getAttackType() {
		return type;
	}

}
