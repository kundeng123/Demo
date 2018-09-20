package demo;

import org.springframework.context.ApplicationEvent;

public class WithdrawEvent extends ApplicationEvent {

	private String bankCardID;
	
	private String withdrawDate;
	
	public String getBankCardID() {
		return bankCardID;
	}

	public void setBankCardID(String bankCardID) {
		this.bankCardID = bankCardID;
	}

	public String getWithdrawDate() {
		return withdrawDate;
	}

	public void setWithdrawDate(String withdrawDate) {
		this.withdrawDate = withdrawDate;
	}

	// Must has this constructor, the first parameter should be event source ( which object trigger this event).
	public WithdrawEvent(Object source, String bankCardID, String withdrawDate) {
		// Must call this super class constructor first.
		super(source);
		this.bankCardID = bankCardID;
		this.withdrawDate = withdrawDate;
	}
}