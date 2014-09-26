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
	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	List<SocialNetworkEntity> personSocialNetworks;
	String name;
	String email;//the same as username
	String password;
	boolean isActive;
	String hashCode;

	public PersonEntity(mat.Person person){
		personSocialNetworks = new ArrayList<SocialNetworkEntity>();
		for (int i=0; i<person.getSnNames().length; i++){
			SocialNetworkEntity sne = new SocialNetworkEntity(person.getSnNames()[i]);
			personSocialNetworks.add(sne);
		}
		this.name = person.getName();
		this.email = person.getEmail();
		this.password = person.getPassword();
		this.isActive = false;
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
	String [] snNames = new String[personSocialNetworks.size()];
	for (int i=0; i<snNames.length; i++)
		snNames[i] = personSocialNetworks.get(i).getName();
	return new Person(name, snNames, email, password);
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
}
