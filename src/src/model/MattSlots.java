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
		
	@Column(name = "event_date", columnDefinition="DATETIME")
	@Temporal(TemporalType.DATE)
	Date date;
	
	int slot_number;

	public MattSlots(int matt_id, Date date, int slot_number) {
		this.date = date;
		this.slot_number = slot_number;
	}

	public MattSlots() {
		super();
	}
	
	
		

}
