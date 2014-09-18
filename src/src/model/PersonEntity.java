package model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import mat.Person;

@Entity
@Table(name="Persons")
public class PersonEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="person_id")
	int id;
	@ManyToMany
	List<SocialNetworkEntity> personSocialNetworks;
	String firstName;
	String lastName;
	String[] snNames; //list of calendar origins
	String email;//the same as username
	String password;
	boolean isActive;
	String hashCode;

public PersonEntity(mat.Person person){
	this.firstName = person.getFirstName();
	this.lastName = person.getLastName();
	this.snNames = person.getSnNames();
	this.email = person.getEmail();
	this.password = person.getPassword();
	this.isActive = person.isActive();
	this.hashCode = person.getHashCode();
}
public PersonEntity(){}

public int getId() {
	return id;
}
public List<SocialNetworkEntity> getPersonSocialNetworks() {
	return personSocialNetworks;
}
public void setPersonSocialNetworks(
		List<SocialNetworkEntity> personSocialNetworks) {
	this.personSocialNetworks = personSocialNetworks;
}
public String getFirstName() {
	return firstName;
}
public void setFirstName(String firstName) {
	this.firstName = firstName;
}
public String getLastName() {
	return lastName;
}
public void setLastName(String lastName) {
	this.lastName = lastName;
}
public String[] getSnNames() {
	return snNames;
}
public void setSnNames(String[] snNames) {
	this.snNames = snNames;
}
public String getEmail() {
	return email;
}
public void setEmail(String email) {
	this.email = email;
}
public String getPassword() {
	return password;
}
public void setPassword(String password) {
	this.password = password;
}
public boolean isActive() {
	return isActive;
}
public void setActive(boolean isActive) {
	this.isActive = isActive;
}
public String getHashCode() {
	return hashCode;
}
public void setHashCode(String hashCode) {
	this.hashCode = hashCode;
}
public Person toPerson(){
	return new Person(firstName, lastName, snNames, email, password, isActive, hashCode);
}
}
