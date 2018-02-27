package com.github.maxopoly.Polemos.action;

import com.github.maxopoly.Polemos.model.PlayerState;

public class PlayerKillGolem extends AbstractAction {

	private String golemOwner;

	public PlayerKillGolem(long time, PlayerState mainPlayer, String golemOwner) {
		super(time, mainPlayer);
		this.golemOwner = golemOwner;
	}

	public String getGolemOwner() {
		return golemOwner;
	}

}
