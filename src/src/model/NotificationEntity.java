package model;
import javax.persistence.*;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

@Entity
@Table(name="notifications")
public class NotificationEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="note_id")
	long note_id;
	
	@ManyToOne
	@JoinColumn(name="matt_id")
	@ForeignKey(name="fk_mattsinfo_notifications")
	MattInfoEntity mattInfo;
	
	@Index(name="ind_usr_email")
	String guest_email;
	
	Boolean checked_fl=false;
	
	public NotificationEntity(){}

	public NotificationEntity(MattInfoEntity mattinfo, String guest_email){
		this.guest_email = guest_email;
		this.mattInfo = mattinfo;
	}
	
	public long getNote_id() {
		return note_id;
	}
	public void setNote_id(long note_id) {
		this.note_id = note_id;
	}
	public MattInfoEntity getMattInfo() {
		return mattInfo;
	}
	public void setMattInfo(MattInfoEntity mattInfo) {
		this.mattInfo = mattInfo;
	}
	public String getGuest_email() {
		return guest_email;
	}
	public void setGuest_email(String guest_email) {
		this.guest_email = guest_email;
	}
	public Boolean getChecked_fl() {
		return checked_fl;
	}
	public void setChecked_fl(Boolean checked_fl) {
		this.checked_fl = checked_fl;
	}
	
	
}
