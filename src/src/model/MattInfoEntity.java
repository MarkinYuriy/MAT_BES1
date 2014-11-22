package model;

import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name="MattsInfo")
public class MattInfoEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "matt_id")
	int matt_id;
	
	@ManyToOne (fetch=FetchType.LAZY) /*(targetEntity = PersonEntity.class)*/
	@JoinColumn(name = "person_id")
	@ForeignKey(name = "fk_mattsinfo_person_id")
	PersonEntity personEntity;
	
	
	@OneToMany(targetEntity=MattSlots.class, mappedBy = "mattInfo", cascade = CascadeType.ALL, orphanRemoval=true)
	@OnDelete(action=OnDeleteAction.CASCADE)
	List<MattSlots> slots;
	
	@OneToMany(targetEntity=NotificationEntity.class, mappedBy="mattInfo", cascade=CascadeType.ALL)
	List<NotificationEntity> notifications;
	
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
	
	public MattInfoEntity(String name, String password, int nDays, Date startDate, 
				int startHour, int endHour, int timeSlot, PersonEntity personEntity) {
		this.name = name;
		this.password = password;
		this.nDays = nDays;
		this.startDate = startDate;
		this.startHour = startHour;
		this.endHour = endHour;
		this.timeSlot = timeSlot;
		this.personEntity = personEntity;
	}

	public MattInfoEntity() {}

	public int getMatt_id() {
		return matt_id;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public int getnDays() {
		return nDays;
	}

	public Date getStartDate() {
		return startDate;
	}

	public int getStartHour() {
		return startHour;
	}

	public int getEndHour() {
		return endHour;
	}

	public int getTimeSlot() {
		return timeSlot;
	}

	public PersonEntity getPersonEntity() {
		return personEntity;
	}

	public void setPersonEntity(PersonEntity person) {
		this.personEntity = person;
	}

	public List<MattSlots> getSlots() {
		return slots;
	}

	public void setSlots(List<MattSlots> slots) {
		this.slots = slots;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((personEntity == null) ? 0 : personEntity.hashCode());
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
		MattInfoEntity other = (MattInfoEntity) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (personEntity == null) {
			if (other.personEntity != null)
				return false;
		} else if (!personEntity.equals(other.personEntity))
			return false;
		return true;
	}
	
	
	
	
	
	
}
