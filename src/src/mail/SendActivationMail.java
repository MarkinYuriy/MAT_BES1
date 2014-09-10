package mail;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import mat.Person;

public class SendActivationMail implements ISendActivationMail {
	MailSender mailsender;
	SimpleMailMessage template;
	String code;
	@Override
	public void sendMail(Person prs) {
		code=generateCode(prs);
		template.setTo(prs.getEmail());
		String text = "Dear "+prs.getFirstName()+",<br><br>Please follow the link below to activate your account<br>"+code;
		template.setText(text);
		mailsender.send(template);
	}
	private String generateCode(Person prs) {
		// TODO Auto-generated method stub
		return null;
	}

}
