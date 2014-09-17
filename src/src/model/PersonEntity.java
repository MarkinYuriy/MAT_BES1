package model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

@Entity
@Table(name="Perons")
public class PersonEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	int id;
	@ManyToMany
	List<SocialNetworkEntity> personSocialNetworks = new ArrayList<SocialNetworkEntity>();

}
