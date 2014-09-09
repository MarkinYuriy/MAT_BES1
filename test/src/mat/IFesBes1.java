package mat;

public interface IFesBes1 {
	void setProfile(mat.Person person);
	boolean matLogin(String username, String password);
	boolean snLogin(String username, String snName); 
	mat.Matt createMatt(mat.MattData data, String username);	
//1 - function should receive snName.
//2 - ? function getSlots returns slots for the particular time interval (i.e. ready to use in create function) 
//		or just slots for specified dates.
//3 - ? what to do if function getSlots returns null
//4 - ? should I verify if mattData is filled appropriately (i.e. startDate, nDays etc are filled) 
//      before invoking getSlots function
	mat.Matt getMatt(String mattName, String username);
	boolean saveMatt(mat.Matt mattOld,mat.Matt mattNew,String username );
	boolean removeMatt(String mattName, String username);

}
