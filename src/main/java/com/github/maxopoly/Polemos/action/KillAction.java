package com.github.maxopoly.Polemos.action;

import com.github.maxopoly.Polemos.PlayerState;

public class KillAction extends AbstractAction {

	private PlayerState victim;

	public KillAction(long time, PlayerState killer, PlayerState victim) {
		super(time, killer);
		this.victim = victim;
	}

	public PlayerState getVictim() {
		return victim;
	}

}
