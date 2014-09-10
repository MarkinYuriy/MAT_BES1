package mat;

public interface Response {
	//response for function matLogin
	static final int IN_ACTIVE=0;
	static final int NO_PASSWORD_MATCHING=1;
	static final int NO_REGISTRATION=3;
	/***************************/
	//response for function setProfile
	static final int PROFILE_EXISTS_ACTIVE=5;
	static final int PROFILE_EXISTS_INACTIVE=6;
	/******************************/
	//common response for functions matLogin and setProfile
	static final int OK=4;
}
