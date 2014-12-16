package model;

import java.util.*;

import javax.persistence.*;

import org.hibernate.annotations.ForeignKey;

import mat.Person;

@Entity
@Table(name="persons")
public class PersonEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="person_id")
	int id;
	
	String name;
	String email;//the same as username
	String password;
	boolean isActive;
	String hashCode;
	int timeZone;
	
	@OneToMany (mappedBy="personEntity")
	List<MattInfoEntity> mattInfo;

	public PersonEntity(mat.Person person){
		this.name = person.getName();
		this.email = person.getEmail();
		this.password = person.getPassword();
		this.timeZone=person.getTimeZone();
		this.isActive = false;
	}
	
	public PersonEntity(){}
	
	public int getId() {
		return id;
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
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
		
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PersonEntity other = (PersonEntity) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		return true;
	}

	public int getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(int timeZone) {
		this.timeZone = timeZone;
	}
	
//	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
/*	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "persons_sn", joinColumns = { 
			@JoinColumn(name = "person_id", nullable = false) }, 
			inverseJoinColumns = { @JoinColumn(name = "sn_id", 
					nullable = false) })
	@ForeignKey(name="fk_persons")
	Set<SocialNetworkEntity> personSocialNetworks;*/

}
