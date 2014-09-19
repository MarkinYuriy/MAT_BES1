package model;


import javax.persistence.*;

@Entity
@Table(name="sprSocialNetworks")
public class SocialNetworkEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	int id;	
	String name;
	
	public SocialNetworkEntity(String name) {
		super();
		this.name = name;
	}
	public int getId() {return id;}
	public String getName() {return name;}
	
}
