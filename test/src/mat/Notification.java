package mat;

import java.io.Serializable;

public class Notification implements Serializable{
	public String username;
	public String tableName;
	public Notification(){}
	public Notification (String userName, String tableName){
		this.username=userName;
		this.tableName=tableName;
	}
}
