package mat;


import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MattData implements Serializable {
	String name;//name of MATT
	int nDays;//number of days
	Date startDate;
	int startHour;
	int endHour;
	int timeSlot; //in minutes
	String password; //if null the MATT is public

	public MattData(String name, int nDays, Date startDate, int startHour, int endHour, int timeSlot, String password) {
		this.name = name;
		this.nDays = nDays;
		this.startDate = startDate;
		this.startHour = startHour;
		this.endHour = endHour;
		this.timeSlot = timeSlot;
		this.password = password;
	}

	public String getName() {
		return name;
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
	public String getPassword() {
		return password;
	}

	public Date dateFormat(){
		Date ddate = null;
		if(startDate != null){
			try {
				ddate=new SimpleDateFormat("dd/MM/yyyy").parse(startDate.toString());
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return ddate;
	}

	public String mattDataToJSON() {
		return "{"+
				'"'+"name"+'"'+":"+'"'+name+'"'+','+
				'"'+"nDays"+'"'+':'+nDays+','+
				'"'+"startDate"+'"'+':'+'"'+dateFormat()+'"'+','+
				'"'+"startHour"+'"'+':'+'"'+startHour+'"'+','+
				'"'+"endHour"+'"'+':'+'"'+endHour+'"'+','+
				'"'+"timeSlot"+'"'+':'+timeSlot+','+
				'"'+"password"+'"'+':'+'"'+password+'"'+
				"}"
				;
		}
}
