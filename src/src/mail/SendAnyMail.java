package mail;

public abstract class SendAnyMail implements ISendActivationMail {
	int id;
	
	
	public SendAnyMail() {
		id=1;
	}


	public SendAnyMail(int id) {
		super();
		this.id = id;
	}

}
