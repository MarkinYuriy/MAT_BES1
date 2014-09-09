package mat;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Person implements Serializable {
	@Id
	String userName;
	List<mat.SocialNetwork> networks; 
	String email;
	String password;
	
	public String getUserName() {return userName;}
	public void setUserName(String userName) {this.userName = userName;}
	
	public List<mat.SocialNetwork> getNetworks() {return networks;}
	public void setNetworks(List<mat.SocialNetwork> networks) {this.networks = networks;}
	
	public String getEmail() {return email;}
	public void setEmail(String email) {this.email = email;}

	public String getPassword() {return password;}
	public void setPassword(String password) {this.password = password;}
	
}
