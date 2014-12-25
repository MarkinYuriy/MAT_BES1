package model;

import javax.persistence.*;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name="sn_calendars")
public class SnCalendarsEntity {
	//default values to define synchronization of user Matt with SN Calendars
	public static final int NOT_SYNCHRONIZED=2;	
	public static final int UPLOAD=0; //Matt uploaded to SN Calendar, i.e. matt is added to specified calendar. (setCalendar())
	public static final int DOWNLOAD=1; //User events from specified SN calendar are loaded into Matt (i.e. getSlots() invoked).
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
	
	public SnCalendarsEntity(){}

	public SnCalendarsEntity(MattInfoEntity mattInfo,
			SocialNetworkEntity social_net, int upload_download_fl,
			String calendarName) {
		super();
		this.mattInfo = mattInfo;
		this.social_net = social_net;
		this.upload_download_fl = upload_download_fl;
		this.calendarName = calendarName;
	}

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((calendarName == null) ? 0 : calendarName.hashCode());
		result = prime * result
				+ ((mattInfo == null) ? 0 : mattInfo.hashCode());
		result = prime * result
				+ ((social_net == null) ? 0 : social_net.hashCode());
		result = prime * result + upload_download_fl;
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
		SnCalendarsEntity other = (SnCalendarsEntity) obj;
		if (calendarName == null) {
			if (other.calendarName != null)
				return false;
		} else if (!calendarName.equals(other.calendarName))
			return false;
		if (mattInfo == null) {
			if (other.mattInfo != null)
				return false;
		} else if (!mattInfo.equals(other.mattInfo))
			return false;
		if (social_net == null) {
			if (other.social_net != null)
				return false;
		} else if (!social_net.equals(other.social_net))
			return false;
		if (upload_download_fl != other.upload_download_fl)
			return false;
		return true;
	}
	
	
	
	
	

}
