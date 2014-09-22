package model;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name="MattsInfo")
public class MattInfoEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "matt_id")
	int matt_id;
	@ManyToOne(targetEntity = PersonEntity.class)
	@JoinColumn(name = "person_id")
	int person_id;
	
//duplicated fields from MattData class
	@Column (name="matt_name")
	String name;//name of MATT
	String password; //if null the MATT is public
	int nDays;//number of days
	
	@Temporal(TemporalType.DATE)
	Date startDate;
	int startHour;
	int endHour;
	int timeSlot; //in minutes
	
	public MattInfoEntity(int person_id, String name, String password,
			int nDays, Date startDate, int startHour, int endHour, int timeSlot) {
		this.person_id = person_id;
		this.name = name;
		this.password = password;
		this.nDays = nDays;
		this.startDate = startDate;
		this.startHour = startHour;
		this.endHour = endHour;
		this.timeSlot = timeSlot;
	}

	public MattInfoEntity() {}
	
	
	
	
	
	
}
