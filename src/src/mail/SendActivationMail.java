package mail;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import mat.Person;

public class SendActivationMail implements ISendActivationMail {
	MailSender mailsender;
	SimpleMailMessage template;
	String link;
	@Override
	public void sendMail(model.PersonEntity pe) {
		link=generateLink(pe);
		template.setTo(pe.getEmail());
		String text = "Dear "+pe.getFirstName()+",<br><br>Please follow the link below to activate your account<br>"+link;
		template.setText(text);
		mailsender.send(template);
	}
	private String generateLink(model.PersonEntity pe) {
		return "http://localhost:8080/activate?hash="+pe.getHashCode()+"&user="+pe.getEmail();
	}

}
