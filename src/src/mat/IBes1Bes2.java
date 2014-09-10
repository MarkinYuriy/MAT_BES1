package mat;

import java.util.List;

public interface IBes1Bes2 {
	List<Boolean> getSlots(String username, String [] snNames, mat.MattData interval);
	void setMatCalendar(String username, String [] snNames, List<mat.Matt> matts);
}
