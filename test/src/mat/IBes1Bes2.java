package mat;

import java.util.List;

public interface IBes1Bes2 {
	boolean setIdentity(String snUsername, String matUsername, String snName);
	List<Boolean> getSlots(String username, String [] snName, mat.MattData interval); 
}
