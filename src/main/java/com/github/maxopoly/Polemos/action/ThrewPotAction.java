package com.github.maxopoly.Polemos.action;

import com.github.maxopoly.Polemos.PlayerState;

public class ThrewPotAction extends AbstractAction {

	private boolean healtPot;

	public ThrewPotAction(long time, PlayerState mainPlayer, boolean healtPot) {
		super(time, mainPlayer);
		this.healtPot = healtPot;
	}

	public boolean isHealthPot() {
		return healtPot;
	}

}
