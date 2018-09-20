package demo;

import org.springframework.context.ApplicationEvent;

public class DepositEvent extends ApplicationEvent {

	private String bankCardID;
	
	private String depositDate;
	
	public String getBankCardID() {
		return bankCardID;
	}

	public void setBankCardID(String bankCardID) {
		this.bankCardID = bankCardID;
	}

	public String getDepositDate() {
		return depositDate;
	}

	public void setDepositDate(String depositDate) {
		this.depositDate = depositDate;
	}

	// Must has this constructor, the first parameter should be event source(which object trigger this event)
	public DepositEvent(Object source, String bankCardID, String depositDate) {
		// Must call this super class constructor first.
		super(source);
		this.bankCardID = bankCardID;
		this.depositDate = depositDate;
	}
}