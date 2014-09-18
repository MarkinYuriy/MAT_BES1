package model;


import javax.persistence.*;

@Entity
@Table(name="sprSocialNetworks")
public class SocialNetworkEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	int id;	
	String name;
	
}
