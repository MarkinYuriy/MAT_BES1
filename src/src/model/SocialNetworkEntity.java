package model;


import java.util.*;

import javax.persistence.*;

@Entity
@Table(name="social_networks")
public class SocialNetworkEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="sn_id", unique = true, nullable = false)
	int id;	
	String name;
	
//	reference to sn_calendars table
	@OneToMany(targetEntity=SnCalendarsEntity.class, mappedBy="social_net", cascade=CascadeType.ALL)
	List<SnCalendarsEntity> sn_calendars;
	
	public SocialNetworkEntity(String name) {
		super();
		this.name = name;
	}
	public int getId() {return id;}
	
	public String getName() {return name;}
	
	public SocialNetworkEntity() {}
	
	public List<SnCalendarsEntity> getSn_calendars() {
		return sn_calendars;
	}
	public void setSn_calendars(List<SnCalendarsEntity> sn_calendars) {
		this.sn_calendars = sn_calendars;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		SocialNetworkEntity other = (SocialNetworkEntity) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	/*	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "personSocialNetworks")
	@ForeignKey(name="fk_spr_networks")
	Set<PersonEntity> prs;*/
	
}
