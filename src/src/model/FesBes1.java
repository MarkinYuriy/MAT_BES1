package model;

import java.util.*;

import javax.persistence.*;

import org.hibernate.ejb.criteria.expression.SizeOfCollectionExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mail.ISendActivationMail;
import mat.*;

public class FesBes1 implements IFesBes1 {
	static final int MIN_PER_HOUR=60;
	
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
			if (currentPE == null) {								//currentPE not exists
				currentPE = new PersonEntity(person);
				Set<SocialNetworkEntity> personSocialNetworks = getSocialNetworks(person.getSnNames());
				currentPE.setPersonSocialNetworks(personSocialNetworks);	//setting user SN
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

//populating user SN list
	@SuppressWarnings("unchecked")
	@Transactional (readOnly=false, propagation=Propagation.REQUIRED)
	private Set<SocialNetworkEntity> getSocialNetworks(String[] snNames) {
		List<SocialNetworkEntity> snFromDB;
		//List<SocialNetworkEntity> personSocialNetworks = new ArrayList<SocialNetworkEntity>();
		Set<SocialNetworkEntity> personSocialNetworks=new HashSet<SocialNetworkEntity>();
		for(String snName: snNames){
		//getting SN from DB
			snFromDB= em.createQuery("select sn from SocialNetworkEntity sn where sn.name= :snName").
					setParameter("snName", snName).getResultList();
			if (snFromDB != null && !snFromDB.isEmpty())
				personSocialNetworks.addAll(snFromDB);
			else {
				SocialNetworkEntity newSN = new SocialNetworkEntity(snName);
				personSocialNetworks.add(newSN);
				em.persist(newSN);
			}
		}
		return personSocialNetworks;
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
	public int ifEmailExistsInDB(String email){
		PersonEntity pe = getPEbyEmail(email); // looking for person in database by email
		int result;
		if (pe != null){						//person not found
			if (pe.isActive() == false)			//person found, but not active
				 result = Response.IN_ACTIVE;
			else result=Response.OK;
		} 
		else result = Response.NO_REGISTRATION;
		
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
		mat.Matt newMatt = null;
		if (data != null && username != null){
	//determine person_id by username
		PersonEntity prs = getPEbyEmail(username);
	//checking if there is no Matt with this name for this user
		Query query = em.createQuery("select m from MattInfoEntity m "
				+ "where m.name = :mattName and m.personEntity= :person");
		query.setParameter("mattName", data.getName());
		query.setParameter("person", prs);
		int isMattNameDuplicated = query.getResultList().size();
		if (isMattNameDuplicated == 0){	
	/*getting list of user Social Networks, 
	  invoking getSlots() function to get Boolean ArrayList of free/busy intervals.*/
		ArrayList<Boolean> slots= getSlotsFromSN(data, username);
	//creating new Matt
	if (slots != null) {
			newMatt = new mat.Matt();
			newMatt.setData(data);
			newMatt.setSlots(slots);
		}
		}
		}
		return newMatt;
	}
	
	/* getting list of user Social Networks, 
	   invoking getSlots() function to get Boolean ArrayList of free/busy intervals.*/
	private ArrayList<Boolean> getSlotsFromSN(MattData data, String username) {
		ArrayList<Boolean> slots=null;
	//get the list of SN for the user
		PersonEntity prs = getPEbyEmail(username);
		Set<SocialNetworkEntity> snList = prs.getPersonSocialNetworks(); //PersonSocialNetworks is the field of class PersonEntity
		
	//if user have no selected SN building slots array with all false (i.e. free time intervals)
		if(snList == null || snList.isEmpty()){
			int numberOfSlotsPerDay=data.getEndHour()-data.getStartHour();
			int slotsNumber = numberOfSlotsPerDay * data.getnDays() * FesBes1.MIN_PER_HOUR/data.getTimeSlot();
			slots = new ArrayList<Boolean>(Collections.nCopies(slotsNumber, false));
		}
		else { //getting slots from SN networks
			List<String> snNames = new LinkedList<String>();
			for (SocialNetworkEntity sn: snList)
				snNames.add(sn.getName());
			slots = (ArrayList<Boolean>)iBackCon.getSlots(prs.getEmail(), snNames.toArray(new String[snNames.size()]), data); //prs.getEmail() = username
					}
		return slots;
	}

	private Map<Date, LinkedList<Integer>> slotsBoolToMap (
			ArrayList<Boolean> slots, MattData data) {
		Map<Date, LinkedList<Integer> > result = new TreeMap<Date, LinkedList<Integer> >();
		if(slots != null && !slots.isEmpty() && data!= null){
			int size = slots.size();
			int numberOfSlotsPerDay=data.getEndHour()-data.getStartHour();
			HashMap<Integer, Date> dates=new HashMap<Integer, Date>();
			Calendar calendar = new GregorianCalendar();
			if (numberOfSlotsPerDay > 0){
				for (int i=0; i<size; i++){
					if(slots.get(i).booleanValue()){ //returns true if slot value is true i.e. busy.
						int dayNumber = i/numberOfSlotsPerDay; //because division returns the number of past days
					    if(!dates.containsKey(dayNumber)){
					    	calendar.setTime(data.getStartDate());
							calendar.add(Calendar.DATE, dayNumber);
							dates.put(dayNumber, calendar.getTime());
					    }
						LinkedList<Integer> slotNums = result.get(calendar.getTime());
						if (slotNums != null){
							slotNums.add(i);
							result.replace(calendar.getTime(), slotNums); //change "replace" to "put", as replace appeared only since 1.8
						}
						else {
							slotNums = new LinkedList<Integer>();
							slotNums.add(i);
							result.put(calendar.getTime(), slotNums);
						}				
						
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
		//determine person_id by username
			PersonEntity prs = getPEbyEmail(username);
							
		//saving to DB if newMatt name unique for the user
				//determine which slots were selected by user, rearrange the slots into Map<Date, slot_num> 
				Set<SocialNetworkEntity> snList = prs.getPersonSocialNetworks();
				ArrayList<Boolean> user_slots;
				if(snList!=null && !snList.isEmpty())
					user_slots = compareSlotMarks(mattOld.getSlots(), mattNew.getSlots());
				else 
					user_slots = mattNew.getSlots();
				Map<Date, LinkedList<Integer> > boolSlots_toSlotNums = slotsBoolToMap(user_slots, mattNew.getData());
			//creating MattInfoEntity
				MattData data = mattNew.getData();
				MattInfoEntity mattInfo = new MattInfoEntity(data.getName(), data.getPassword(), 
						data.getnDays(), data.getStartDate(), data.getStartHour(), 
						data.getEndHour(), data.getTimeSlot(), prs);
				List<MattSlots> mattSlots = new ArrayList<MattSlots>();
				if (!boolSlots_toSlotNums.isEmpty()){ //Map isEmpty if no user selection
					for(Map.Entry<Date, LinkedList<Integer>> entry: boolSlots_toSlotNums.entrySet()){
						LinkedList<Integer> slotsByDate = entry.getValue();
					//creating list of separate MattSlots 
						for(int slot_num : slotsByDate){
							mattSlots.add(new MattSlots(entry.getKey(), slot_num, mattInfo));
						}	
					}
				}
				mattInfo.setSlots(mattSlots);
				em.persist(mattInfo); //saving MattInfoEntity to the DB.
				/*if (em.getFlushMode() != FlushModeType.AUTO) //manually flushing the changes to DB
					em.flush();
				result = true;
			//updating Mat Calendars in SN 
				if(snList!=null && !snList.isEmpty())
					updateMatCalendarInSN(username, prs.getPersonSocialNetworks(), prs.getId());*/
			}
				
		return result;
	}
	
	
	
	@Override
	public void updateMatCalendarInSN(String username, String snName) { //updating Mat Calendars in SN
		if (username != null && snName != null){
			PersonEntity prs = getPEbyEmail(username);
		//getting String[] of SN names
			String [] snNames = {snName};
		
		//building list of actual MATTs	for current user
		// 1 - creating Query to select all actual for today Matt names for this user
		// using native SQL for mySQL server, because JPQL currently doesn't support required DATE operations
			/*Query query = em.createNativeQuery("select * from test.MattsInfo where person_id=" + prs.getId() +
					" and date_add(startDate, interval nDays day) > curdate()", MattInfoEntity.class);
			List<MattInfoEntity> mattEntities = query.getResultList();*/
		//alternative flow
			//1. select all MATTs for the user
			Query query = em.createQuery("select m from MattInfoEntity m where m.personEntity= :person");
			query.setParameter("person", prs);
			List<MattInfoEntity> allMattEntities=query.getResultList();
			List<Matt> actualUserMatts = new LinkedList<Matt>();
			if(allMattEntities != null && !allMattEntities.isEmpty()){
				Calendar today = new GregorianCalendar();
				today = Calendar.getInstance();
				for(MattInfoEntity entity:allMattEntities){
					today.add(Calendar.DATE, -entity.getnDays());
					if (entity.getStartDate().compareTo(today.getTime()) >= 0)
						actualUserMatts.add(getMattFromMattEntity(entity, username));
				}
			}
		
		// 2 - building MATTs from MattEntities
			/*List<Matt> actualUserMatts = new LinkedList<Matt>();
			for(MattInfoEntity entity : mattEntities)
				actualUserMatts.add(getMattFromMattEntity(entity, username));	*/
			
			iBackCon.setMatCalendar(username, snNames, actualUserMatts);
		}
	}



	private Matt getMattFromMattEntity(MattInfoEntity entity, String username) {
		Matt matt = new Matt();
		if (entity != null){
			MattData mattData = new MattData(entity.getName(), entity.getnDays(), entity.getStartDate(), 
					entity.getStartHour(), entity.getEndHour(), entity.getTimeSlot(), entity.getPassword());
			
			ArrayList<Boolean> slotsFromSn=getSlotsFromSN(mattData, username);
			ArrayList<Boolean> slotsFromDB=getSlotsFromDB(entity); 
			ArrayList<Boolean> resSlotsList = new ArrayList<Boolean>();
			boolean result; // result variable for merging slots
			int size = slotsFromDB.size();
			for (int i = 0; i < size; i++) {
				result = (slotsFromDB.get(i) || slotsFromSn.get(i)); // merging slots
				resSlotsList.add(result); // adding result slots to the result slots							
			}
			matt.setData(mattData);
			matt.setSlots(resSlotsList);
		}
		return matt;
	}
		
	
	@Override
	public Matt getMatt(String mattName, String username) {
		PersonEntity person = getPEbyEmail(username);
		Query query = em.createQuery("select m from MattInfoEntity m "
				+ "where m.personEntity= :person and m.name= :mattName"); 
		query.setParameter("person", person);
		query.setParameter("mattName", mattName);
		MattInfoEntity entity = (MattInfoEntity) query.getSingleResult();
		
		return getMattFromMattEntity(entity, username);
	}
	
	private ArrayList<Boolean> getSlotsFromDB(MattInfoEntity mattEntity) {
	//determining number of slots and creating Boolean list with all slots marked as false.
		int numberOfSlotsPerDay=mattEntity.getEndHour()-mattEntity.getStartHour();
		int slotsNumber = numberOfSlotsPerDay * mattEntity.getnDays() * FesBes1.MIN_PER_HOUR/mattEntity.getTimeSlot();
		ArrayList<Boolean> slotsFromDB = new ArrayList<Boolean>(Collections.nCopies(slotsNumber, false));
	//taking busy slot numbers from DB and changing ArrayList values (setting to true) by the index
		List<MattSlots> mattSlots = mattEntity.getSlots();
		if (mattSlots != null)
			for (MattSlots mattSlot : mattSlots)
				slotsFromDB.set(mattSlot.getSlot_number(), true);
			
		return slotsFromDB;
	}



	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	@Override
	public boolean removeMatt(String mattName, String username) {
		boolean result=false;
			if (mattName!=null && username!=null){
				Query query = em.createQuery("select m from MattInfoEntity m join m.personEntity p "
				+ "where m.name = :mattName and p.email= :username");
				query.setParameter("mattName", mattName);
				query.setParameter("username", username);
				List<MattInfoEntity> res=query.getResultList();
				if (res != null && !res.isEmpty()){
					MattInfoEntity deleteRow = res.get(0);
					em.remove(deleteRow);
					result=true;
				}
			}
		return result;
	}

	private ArrayList<Boolean> compareSlotMarks(ArrayList<Boolean> oldSlots,
			ArrayList<Boolean> newSlots) {
		int size = oldSlots.size();
		ArrayList<Boolean> result = new ArrayList<Boolean>();
		for (int i = 0; i < size; i++) {
			if (oldSlots.get(i).booleanValue() && newSlots.get(i).booleanValue()) //building new slots array with user-marked intervals.
				result.add(false);
			else if (!oldSlots.get(i).booleanValue() && !newSlots.get(i).booleanValue())
				result.add(false);
			else result.add(true);
		}
		return result;
	}


	@Override
	public String[] getMattNames(String username) {
		 PersonEntity prs = getPEbyEmail(username);
		 String str = "Select m.name from MattInfoEntity m where m.personEntity = :user";
		 Query query = em.createQuery(str); //sending query
		 query.setParameter("user", prs);
		 ArrayList<String> listOfNames = (ArrayList<String>) query.getResultList(); //getting result list
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
				Set<SocialNetworkEntity> personSocialNetworks = getSocialNetworks(person.getSnNames());
				pe.setPersonSocialNetworks(personSocialNetworks);
				result = Response.OK;
			}
		}
		return result;
	}
	
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW)
	public boolean deletePerson(String userName){
		boolean result = false;
		PersonEntity prs = getPEbyEmail(userName);
		if (prs != null){
			em.remove(prs);
			result=true;
		}
		return result;
	}
	
	//****COMMON SERVING PRIVATE FUNCTIONS****
	private PersonEntity getPEbyEmail(String email) {
		List<PersonEntity> prsList=null;
		Query query = em.createQuery("SELECT pe FROM PersonEntity pe WHERE pe.email=?1");
		query.setParameter(1, email);
		prsList = query.getResultList();
		if (prsList != null && !prsList.isEmpty())
			return prsList.get(0);
		return null;
	}

	private void launchActivation(PersonEntity pe) {
		ISendActivationMail sender = (ISendActivationMail) ctx.getBean("sender");
		sender.sendMail(pe);
	}

	@Override
	public List<Notification> getNotifications(String guestName) {
		  List<NotificationEntity> notList=null;
		  List<Notification> rt = new LinkedList<>();
		  Query query = em.createQuery("select n from NotificationEntity n where where n.guest_email= :guestName");
		  query.setParameter("guestName", guestName);
		  notList = query.getResultList();
		  if (notList != null && !notList.isEmpty())
			for(NotificationEntity ne:notList)
				rt.add(ne.toNotification());
			return rt;
	}
	@Override
	public void setGuests(String username, String tableName, String [] guestEmails) {
		
		
	}

}