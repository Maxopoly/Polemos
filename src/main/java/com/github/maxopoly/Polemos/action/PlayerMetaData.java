package com.github.maxopoly.Polemos.action;

import com.github.maxopoly.Polemos.model.PlayerState;
import java.util.UUID;

public class PlayerMetaData extends AbstractAction {
	//this isnt really an action, but lets us easily carry over some additional information

	private String name;
	private UUID uuid;

	public PlayerMetaData(String name, UUID uuid) {
		super(0L, new PlayerState(name, 0f));
		this.name = name;
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public UUID getUUID() {
		return uuid;
	}


}
