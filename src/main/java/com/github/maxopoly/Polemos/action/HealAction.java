package com.github.maxopoly.Polemos.action;

import com.github.maxopoly.Polemos.model.PlayerState;

public class HealAction extends AbstractAction {

	public enum HealReason {MAGIC, MAGIC_REGEN, SATIATED};

	private double amount;
	private HealReason reason;

	public HealAction(long time, PlayerState mainPlayer, double amount, HealReason reason) {
		super(time, mainPlayer);
		this.amount = amount;
		this.reason = reason;
	}

	public double getAmount() {
		return amount;
	}

	public HealReason getReason() {
		return reason;
	}

}
