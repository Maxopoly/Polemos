package com.github.maxopoly.Polemos.action;

import com.github.maxopoly.Polemos.PlayerState;

public class PlayerHitPlayerAction extends AbstractAction {

	public enum AttackType {ENTITY_ATTACK, ENTITY_SWEEP_ATTACK, THORNS, ENTITY_EXPLOSION, PROJECTILE};
	private double damage;

	private PlayerState victim;
	private AttackType type;

	public PlayerHitPlayerAction(long time, PlayerState attacker, PlayerState victim, double damage, AttackType type) {
		super(time, attacker);
		this.victim = victim;
		this.type = type;
		this.damage = damage;
	}

	public PlayerState getAttacker() {
		return getMainPlayer();
	}

	public PlayerState getVictim() {
		return victim;
	}

	public double getDamage() {
		return damage;
	}

	public AttackType getAttackType() {
		return type;
	}

}
