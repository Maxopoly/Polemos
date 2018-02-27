package com.github.maxopoly.Polemos.action.playerDamaged;

import com.github.maxopoly.Polemos.model.PlayerState;

/**
 * A player received fall damage, because his pearl landed
 *
 */
public class PearlDamagePlayer extends PlayerDamagedAction {

	public PearlDamagePlayer(long time, PlayerState mainPlayer, double damage) {
		super(time, mainPlayer, damage);
	}

}
