package model;

import java.util.List;

import javax.persistence.*;

@Entity
@Table(name="sprSocialNetworks")
public class SocialNetworkEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	int id;	
	String name;
	
}
