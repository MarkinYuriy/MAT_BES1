package model;

import java.util.*;

import javax.persistence.*;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name="matt_busy_slots")
public class MattSlots {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	int id;	
	
	@ManyToOne
	@JoinColumn (name="matt_id")
	@ForeignKey(name="fk_mattsinfo_mattbusyslots")
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

	public int getSlot_number() {
		return slot_number;
	}
	
}
