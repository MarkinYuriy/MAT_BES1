package mat;

import java.io.Serializable;
import java.util.List;

public class Person implements Serializable{
	 String name;
	 //String userName;
	 String[] snNames; 
	 String email;//the same as username
	 String password;
	 
	 

	 public Person(String name, String[] snNames, String email, String password) {
	  super();
	  this.name = name;
	  this.snNames = snNames;
	  this.email = email;
	  this.password = password;
	 }
	 
	 public String getName() {return name;}
	 public void setName(String name) {this.name = name;}
	 
	 
	 public String[] getSnNames() {return snNames;}
	 public void setSnNames(String[] snNames) {this.snNames = snNames;}
	 
	 public String getEmail() {return email;}
	 public void setEmail(String email) {this.email = email;}
	 
	 public String getPassword() {return password;}
	 public void setPassword(String password) {this.password = password;}
	 
	 
	 // 
	 public String getUserName() {return email;}
}
