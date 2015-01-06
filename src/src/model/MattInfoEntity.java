package model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name="matt_info")
public class MattInfoEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "matt_id")
	int matt_id;
	
	@ManyToOne //(fetch=FetchType.EAGER)
	@JoinColumn(name = "person_id")
	@ForeignKey(name = "fk_mattsinfo_person_id")
	PersonEntity personEntity;
	
	
	@OneToMany(targetEntity=MattSlots.class, mappedBy = "mattInfo", cascade = CascadeType.ALL/*, orphanRemoval=true*/)
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

//storing upload-download to SN Calendar properties
	@OneToMany(targetEntity=SnCalendarsEntity.class, mappedBy="mattInfo", cascade=CascadeType.ALL)
	List<SnCalendarsEntity> sncalendars;
		
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


	public List<SnCalendarsEntity> getSncalendars() {
		return sncalendars;
	}

	public void setSncalendars(List<SnCalendarsEntity> sncalendars) {
		this.sncalendars = sncalendars;
	}

	public void setNotifications(List<NotificationEntity> notifications) {
		this.notifications = notifications;
	}

	public List<NotificationEntity> getNotifications() {
		return notifications;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setnDays(int nDays) {
		this.nDays = nDays;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}

	public void setEndHour(int endHour) {
		this.endHour = endHour;
	}

	public void setTimeSlot(int timeSlot) {
		this.timeSlot = timeSlot;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + matt_id;
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
		if (matt_id != other.matt_id)
			return false;
		return true;
	}
	
	
	
	
	
	
}
