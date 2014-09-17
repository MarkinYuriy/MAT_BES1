package model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

@Entity
@Table(name="MattBusySlots")
public class MattSlots {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	int id;
	@ManyToMany
	List<SocialNetworkEntity> mattSocialNetworks = new ArrayList<SocialNetworkEntity>();

	

}
