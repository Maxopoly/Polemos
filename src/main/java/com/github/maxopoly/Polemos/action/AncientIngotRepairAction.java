package com.github.maxopoly.Polemos.action;

import com.github.maxopoly.Polemos.model.PlayerState;

public class AncientIngotRepairAction extends AbstractAction {

	private String pieceRepaired;

	public AncientIngotRepairAction(long time, PlayerState mainPlayer, String pieceRepaired) {
		super(time, mainPlayer);
		this.pieceRepaired = pieceRepaired;
	}

	public String getPieceRepaired() {
		return pieceRepaired;
	}

}
