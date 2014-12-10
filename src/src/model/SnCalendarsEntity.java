package model;

import javax.persistence.*;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name="sn_calendars")
public class SnCalendarsEntity {
	//default values to define synchronization of user Matt with SN Calendars
	public static final int NOT_SYNCHRONIZED=0;	
	public static final int UPLOAD=1; //Matt uploaded to SN Calendar, i.e. matt is added to specified calendar. (setCalendar())
	public static final int DOWNLOAD=2; //User events from specified SN calendar are loaded into Matt (i.e. getSlots() invoked).
	public static final int UPLOAD_AND_DOWNLOAD=3; 
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="sn_calendar_id")
	long sn_calendar_id;
	
	@ManyToOne
	@JoinColumn(name="matt_id")
	@ForeignKey(name="fk_matt_info_sn_calendar")
	MattInfoEntity mattInfo;
	
	@ManyToOne
	@JoinColumn(name="sn_id")
	@ForeignKey(name="fk_sn_sn_calendar")
	SocialNetworkEntity social_net;
	
	@Column(columnDefinition="TINYINT", length=1)
	int upload_download_fl;
	
	String calendarName;

	public long getSn_calendar_id() {
		return sn_calendar_id;
	}

	public void setSn_calendar_id(long sn_calendar_id) {
		this.sn_calendar_id = sn_calendar_id;
	}

	public MattInfoEntity getMattInfo() {
		return mattInfo;
	}

	public void setMattInfo(MattInfoEntity mattInfo) {
		this.mattInfo = mattInfo;
	}

	public SocialNetworkEntity getSocial_net() {
		return social_net;
	}

	public void setSocial_net(SocialNetworkEntity social_net) {
		this.social_net = social_net;
	}

	public int getUpload_download_fl() {
		return upload_download_fl;
	}

	public void setUpload_download_fl(int upload_download_fl) {
		this.upload_download_fl = upload_download_fl;
	}

	public String getCalendarName() {
		return calendarName;
	}

	public void setCalendarName(String calendarName) {
		this.calendarName = calendarName;
	}
	
	
	
	
	

}
