package mail;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import mat.Person;

public class SendActivationMail implements ISendActivationMail {
	MailSender mailsender;
	SimpleMailMessage template;
	String link;
	@Override
	public void sendMail(Person prs) {
		link=generateLink(prs);
		template.setTo(prs.getEmail());
		String text = "Dear "+prs.getFirstName()+",<br><br>Please follow the link below to activate your account<br>"+link;
		template.setText(text);
		mailsender.send(template);
	}
	private String generateLink(Person prs) {
		return "http://localhost:8080/activate?hashCode="+prs.getHashCode()+"&user="+prs.getEmail();
	}

}
