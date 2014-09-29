package model;

import java.io.IOException;
import java.util.*;

import javax.persistence.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mail.ISendActivationMail;
import mat.*;

public class FesBes1 implements IFesBes1 {
	
	@PersistenceContext(unitName = "springHibernate", type = PersistenceContextType.EXTENDED)
	EntityManager em;

	@Autowired
	ApplicationContext ctx;

	@Autowired
	IBackConnector iBackCon;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public int setProfile(Person person) {
		int result = Response.UNKNOWN;
		if (person != null) {
			PersonEntity currentPE = getPEbyEmail(person.getEmail()); //currentPE is a personEntity with considered email from database
			if (currentPE == null) {									//currentPE not exists
				currentPE = new PersonEntity(person);
				currentPE.setHashCode(UUID.randomUUID().toString());		//create unique confirmation code for person
				em.persist(currentPE);  
				launchActivation(currentPE);								//launch activate mechanism
				result = Response.OK;
			} else {													//currentPE exists, checking activation status
				if (currentPE.isActive() == false)
					result = Response.PROFILE_EXISTS_INACTIVE;
				else
					result = Response.PROFILE_EXISTS_ACTIVE;
			}
		}
		return result;
	}



	@Override
	public int matLogin(String email, String password) {
		PersonEntity pe = getPEbyEmail(email); // looking for person in database by email
		int result;
		 if (pe == null)							//person not found
			 result = Response.NO_REGISTRATION;
		 else
			 if (pe.isActive() == false)			//person found, but not active
				 result = Response.IN_ACTIVE;
			 else									//person found and active
				 if ((pe.getPassword()).equals(password)==true)	//password correct
					 result = Response.OK;
				 else								//password not correct
					 result = Response.NO_PASSWORD_MATCHING;
		return result; 
	}

	@Override
	public Matt createMatt(MattData data, String username)  {
		/*
		 * using service IBes1Bes2, described in file beans.xml bean
		 * id="bes1bes2" property name="serviceUrl"
		 * value="http://localhost:8080/bes1bes2_service/bes1bes2_service.service"
		 * property name="serviceInterface" value="mat.IBackConnector"
		 */
	/*getting list of user Social Networks, 
	  invoking getSlots() function to get Boolean ArrayList of free/busy intervals.*/
		ArrayList<Boolean> slots= getSlotsFromSN(data, username);
		
	//creating new Matt
		mat.Matt newMatt = null;
		if (slots != null) {
			newMatt = new mat.Matt();
			newMatt.setData(data);
			newMatt.setSlots(slots);
		}
		return newMatt;
	}
	
	/* getting list of user Social Networks, 
	   invoking getSlots() function to get Boolean ArrayList of free/busy intervals.*/
	private ArrayList<Boolean> getSlotsFromSN(MattData data, String username) {
		ArrayList<Boolean> slots=null;
	//get the list of SN for the user
		Query query = em.createQuery("select p from PersonEntity p where p.email= :username");
		query.setParameter("username", username);
		PersonEntity prs=(PersonEntity) query.getSingleResult(); //?????? may be changed to getResultList() for more safety.
		List<SocialNetworkEntity> snList = prs.getPersonSocialNetworks(); //function already exists
		
	//if user have no selected SN building slots array with all false (i.e. free time intervals)
		if(snList == null || snList.isEmpty()){
			int slotsNumber = data.getnDays() * (data.getEndHour() - data.getStartHour())*(data.getTimeSlot()/60); //60 - minutes in an hour.
			slots = new ArrayList<Boolean>(Collections.nCopies(slotsNumber, false));
		}
		else { //getting slots from SN networks
			List<String> snNames = new LinkedList<String>();
			for (SocialNetworkEntity sn: snList)
				snNames.add(sn.getName());
		//	slots = (ArrayList<Boolean>)iBackCon.getSlots(username, snNames.toArray(new String[snNames.size()]), data);
					}
		return slots;
	}

	private Map<Date, LinkedList<Integer>> slotsBoolToMap (
			ArrayList<Boolean> slots, MattData data) {
		Map<Date, LinkedList<Integer> > result = new TreeMap<Date, LinkedList<Integer> >();
		if(slots != null && !slots.isEmpty() && data!= null){
			int size = slots.size();
			int numberOfSlotsPerDay = data.getnDays() * (data.getEndHour()-data.getStartHour());
			HashMap<Integer, Date> dates=new HashMap<Integer, Date>();
			Calendar calendar = new GregorianCalendar();
			for (int i=0; i<size; i++){
				if(slots.get(i)){ //returns true if slot value is 1 i.e. busy.
				//	int curr = i+1; //because i begins with zero
					int dayNumber = i/numberOfSlotsPerDay; //because division returns the number of past days
				    if(!dates.containsKey(dayNumber)){
				    	calendar.setTime(data.getStartDate());
						calendar.add(Calendar.DATE, dayNumber);
						dates.put(dayNumber, calendar.getTime());
				    }
					LinkedList<Integer> slotNums = result.get(calendar.getTime());
					if (slotNums != null){
						slotNums.add(i);
						result.replace(calendar.getTime(), slotNums);
					}
					else {
						slotNums = new LinkedList<Integer>();
						slotNums.add(i);
						result.put(calendar.getTime(), slotNums);
					}				
					
				}
			}
		}
		
		return result;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	@Override
	public boolean saveMatt(Matt mattOld, Matt mattNew, String username) {
		boolean result = false;
		if (mattNew != null && mattOld != null && username != null) {
		//determine which slots were selected by user, rearrange the slots into Map<Date, slot_num>  
			ArrayList<Boolean> user_slots = compareSlotMarks(mattOld.getSlots(), mattNew.getSlots());
			Map<Date, LinkedList<Integer> > boolSlots_toSlotNums = slotsBoolToMap(user_slots, mattNew.getData()); 
		//checking if the user have no matt with the same name as newMatt
			//determine person_id by username
			Query query = em.createQuery("select p.person_id from Persons p where p.email= :username");
			/*query = em.createQuery("select m from MattsInfo m join m.person_id p "
					+ "where m.matt_name = :mattName and p.email= :username");*/
			query.setParameter("username", username);
			int prs_id = (int) query.getSingleResult();
			query = em.createQuery("select m from MattsInfo m "
					+ "where m.matt_name = :mattName and m.person_id= :person_id");
			query.setParameter("mattName", mattNew.getData().getName());
			query.setParameter("person_id", prs_id);
			List<MattInfoEntity> matt_with_theSame_name= query.getResultList();
		//saving to DB if newMatt name unique for the user
			if (matt_with_theSame_name == null || matt_with_theSame_name.isEmpty()){
				MattData data = mattNew.getData();
				MattInfoEntity mattInfo = new MattInfoEntity(prs_id, data.getName(), data.getPassword(), 
						data.getnDays(), data.getStartDate(), data.getStartHour(), 
						data.getEndHour(), data.getTimeSlot());
				em.persist(mattInfo);
				if (!boolSlots_toSlotNums.isEmpty()){ //Map isEmpty if no user selection
					for(Map.Entry<Date, LinkedList<Integer>> entry: boolSlots_toSlotNums.entrySet()){
					//	MattSlots mattSlots = new MattSlots(entry.getKey(), entry.getValue());
					}
				}
				result = true;
			}
				
		}
		return result;
	}
	/*getting Persons Id*/
	private int getPersonId(String username){ 
		Query query = em.createQuery("select p.id from Persons p where p.email= :username"); 
		query.setParameter("username", username);
		PersonEntity prs=(PersonEntity) query.getSingleResult(); //	Getting person from DB
		return prs.getId();
	}
	
	
	
	
	@Override
	public Matt getMatt(String mattName, String username) {
		Matt resMatt = new Matt();
		int personId = getPersonId(username);
		Query query = em.createQuery("select m from MattInfoEntity m where p.id= :personId and p.matt_name= :name"); 
		query.setParameter("personId", personId);
		query.setParameter("name", mattName);
		MattInfoEntity entity = (MattInfoEntity) query.getSingleResult();
		MattData mattData = new MattData(entity.getName(), entity.getnDays(), entity.getStartDate(), 
				entity.getStartHour(), entity.getEndHour(), entity.getTimeSlot(), entity.getPassword());
		
		ArrayList<Boolean> slotsFromSn=getSlotsFromSN(mattData, username);
		ArrayList<Boolean> slotsFromDB=getSlotsFromDB(mattName, entity.getMatt_id()); //From Sasha
		
			
		ArrayList<Boolean> resSlotsList = null;
		boolean result; // result variable for merging slots
		for (int i = 0; i < slotsFromDB.size(); i++) {
			result = slotsFromDB.get(i) || slotsFromSn.get(i); // merging slots
			resSlotsList.add(result); // adding result slots to the result slots
									
		}
		resMatt.setData(mattData);
		resMatt.setSlots(resSlotsList);
		return resMatt;
	}
	
	private ArrayList<Boolean> getSlotsFromDB(String mattName, int matt_id) {
		// TODO Auto-generated method stub
		return null;
	}



	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	@Override
	public boolean removeMatt(String mattName, String username) {
		boolean result=false;
		Matt resMatt = new Matt();
			
			resMatt=getMatt(mattName, username);
			if (resMatt!=null){
				Query query_delete=em.createQuery("DELETE FROM Matt WHERE x.mattName=?1 and x.mattName=?2");
				query_delete.setParameter(1, mattName);
				query_delete.setParameter(2, username);
				result=true;
			}
		return result;
	}

	private ArrayList<Boolean> compareSlotMarks(ArrayList<Boolean> oldSlots,
			ArrayList<Boolean> newSlots) {
		int size = oldSlots.size();
		ArrayList<Boolean> result = new ArrayList<Boolean>();
		for (int i = 0; i < size; i++) {
			if (oldSlots.get(i) && newSlots.get(i)) //building new slots array with user-marked intervals.
				result.add(false);
			else
				result.add(true);
		}
		return result;
	}


	@Override
	public String[] getMattNames(String username) {
		String str = "Select m.mattName from MattInfo where m.userName = '"+username+"'";
		Query query = em.createQuery(str); //sending query
		ArrayList<String> listOfNames = (ArrayList<String>) query.getResultList();	//getting result list
		String[] resultArr = new String[listOfNames.size()];	
		resultArr = listOfNames.toArray(resultArr);
		return resultArr;
	}

	@Override
	public Person getProfile(String email) {
		PersonEntity pe = getPEbyEmail(email);
		return pe.toPerson();
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void setActive(String email, String hashcode) {
		PersonEntity pe = getPEbyEmail(email);
		if (pe.getHashCode().equals(hashcode))
			pe.setActive(true);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public int updateProfile(Person person) {
		int result = Response.UNKNOWN;
		if (person != null) {
			String email = person.getEmail();
			PersonEntity pe = getPEbyEmail(email);
			result = Response.NO_REGISTRATION;
			if (pe != null) {
				List<SocialNetworkEntity> personSocialNetworks = new ArrayList<SocialNetworkEntity>();
				for (int i=0; i<person.getSnNames().length; i++){
					SocialNetworkEntity sne = new SocialNetworkEntity(person.getSnNames()[i]);
					personSocialNetworks.add(sne);
				}
				pe.setPersonSocialNetworks(personSocialNetworks);
				result = Response.OK;
			}
		}
		return result;
	}
	//****COMMON SERVING PRIVATE FUNCTIONS****
	private PersonEntity getPEbyEmail(String email) {
		PersonEntity result = null;
		try {
			Query query = em.createQuery("SELECT pe FROM PersonEntity pe WHERE pe.email=?1");
			query.setParameter(1, email);
			result = (PersonEntity) query.getSingleResult();
		} catch (Exception e) {
		}
		return result;
	}

	private void launchActivation(PersonEntity pe) {
		ISendActivationMail sender = (ISendActivationMail) ctx.getBean("sender");
		sender.sendMail(pe);
	}
	
	private List<SocialNetworkEntity> getSocialNetworksByEmail(String email){
		return getPEbyEmail(email).getPersonSocialNetworks();
	}

}