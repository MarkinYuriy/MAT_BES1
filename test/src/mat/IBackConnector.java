package mat;

import java.io.IOException;
import java.util.List;

public interface IBackConnector {
	List<Boolean> getSlots(String username, String [] snNames,
			mat.MattData interval) ;
	void setMatCalendar(String username,String [] snNames,List<mat.Matt> matts );
	
}
