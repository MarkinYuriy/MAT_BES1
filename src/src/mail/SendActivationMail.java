package mail;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

public class SendActivationMail extends SendAnyMail {
	MailSender msender;
	SimpleMailMessage template;
	
	public SendActivationMail() {}
	
	public SendActivationMail(int id) {
		super(id);
	}
	


	public void setMsender(MailSender msender) {
		this.msender = msender;
	}

	public void setTemplate(SimpleMailMessage template) {
		this.template = template;
	}

	@Override
	public void sendMail(model.PersonEntity pe) {
		String link=generateLink(pe);
		//template.setTo(pe.getEmail());
		String[] emails = {"anatoly.tihonov@gmail.com", "gel_82@mail.ru"};
		
		template.setTo(emails);
		String text = "Dear "+pe.getName()+", Please follow the link below to activate your account: "+link;
		template.setText(text);
		msender.send(template);
	}
	private String generateLink(model.PersonEntity pe) {
		return "http://localhost:8080/MAT_FES/activate?hash="+pe.getHashCode()+"&user="+pe.getEmail();
	}

}
