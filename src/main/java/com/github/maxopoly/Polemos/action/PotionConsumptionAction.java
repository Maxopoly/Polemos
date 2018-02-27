package com.github.maxopoly.Polemos.action;

import com.github.maxopoly.Polemos.model.PlayerState;

import com.github.maxopoly.Polemos.model.Potion;

public class PotionConsumptionAction extends AbstractAction {

	private Potion potion;

	public PotionConsumptionAction(long time, PlayerState mainPlayer, Potion pot) {
		super(time, mainPlayer);
		this.potion = pot;
	}

	public Potion getPotion() {
		return potion;
	}
}
