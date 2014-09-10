package mat;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

@Entity
public class Person implements Serializable {
	String firstName;
	String lastName;
	//String userName;
	String[] snNames; 
	String email;//the same as username
	String password;
	boolean isActive;
	String hashCode;

	public String getHashCode() {
		return hashCode;
	}
	public void setHashCode(String hashCode) {
		this.hashCode = hashCode;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	//
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	//
	public String[] getSnNames() {
		return snNames;
	}
	public void setSnNames(String[] snNames) {
		this.snNames = snNames;
	}
	//
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	//
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	//
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	
	public String getUserName() {
		return email;
	}
	
	
	
}
