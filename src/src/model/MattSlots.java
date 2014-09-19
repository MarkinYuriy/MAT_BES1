package model;

import java.util.*;

import javax.persistence.*;

import org.hibernate.annotations.Type;

@Entity
@Table(name="MattBusySlots")
public class MattSlots {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	int id;	
	@ManyToOne(targetEntity = MattInfoEntity.class)
	@JoinColumn(name = "matt_id")
	int matt_id;
	
//	@Column(name = "CDate", columnDefinition="DATETIME")
	@Temporal(TemporalType.DATE)
	Date date;
	
	int slot_number;
	
	@Type(type="boolean")
	boolean from_social;
	

}
