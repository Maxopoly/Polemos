package com.github.maxopoly.Polemos.action;

import com.github.maxopoly.Polemos.model.PlayerState;

public abstract class AbstractAction {

	private PlayerState mainPlayer;
	private final long time;

	public AbstractAction(long time, PlayerState mainPlayer) {
		this.mainPlayer = mainPlayer;
		this.time = time;
	}

	public PlayerState getMainPlayer() {
		return mainPlayer;
	}

	public long getTime() {
		return time;
	}
}
