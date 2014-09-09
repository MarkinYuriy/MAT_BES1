package mat;

public interface IFesBes1 {
	boolean setProfile(mat.Person person);
	boolean matLogin(String username, String password);
	boolean snLogin(String username, String snName); 
	mat.Matt createMatt(mat.MattData data, String username);
	mat.Matt getMatt(String mattName, String username);
	boolean saveMatt(mat.Matt mattOld,mat.Matt mattNew,String username );
	boolean removeMatt(String mattName, String username);
}
