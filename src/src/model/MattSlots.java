package model;

import java.util.*;

import javax.persistence.*;


@Entity
@Table(name="MattBusySlots")
public class MattSlots {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	int id;	
	
	@ManyToOne
	@JoinColumn (name="matt_id")
	MattInfoEntity mattInfo;
		
	@Column(name = "event_date", columnDefinition="DATE")
	@Temporal(TemporalType.DATE)
	Date date;
	
	int slot_number;

	public MattSlots(Date date, int slot_number, MattInfoEntity mattInfo) {
		this.date = date;
		this.slot_number = slot_number;
		this.mattInfo = mattInfo;
	}

	public MattSlots() {}

	public MattInfoEntity getMattInfo() {
		return mattInfo;
	}

	public void setMattInfo(MattInfoEntity mattInfo) {
		this.mattInfo = mattInfo;
	}
	
	
		

}
