package model;

import java.util.*;
import java.util.Map.Entry;

import javax.persistence.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mail.ISendActivationMail;
import mat.*;

public class FesBes1 implements IFesBes1 {
	static final int MIN_PER_HOUR=60;
	
	//@PersistenceContext(unitName = "springHibernate", type = PersistenceContextType.EXTENDED)
	@PersistenceContext(type=PersistenceContextType.EXTENDED)
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

//populating user SN list ***********not in usage from 9.12.2014*********
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
					//System.out.println(slots.get(i).booleanValue());
					if(!slots.get(i).booleanValue()){ //returns false if slot value is true i.e. busy.
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
	public int saveMatt(Matt mattNew, String username) {
		int result=0;
		if (mattNew != null && username != null) {
			//determine person_id by username
			PersonEntity prs = getPEbyEmail(username);
			MattInfoEntity entity = checkIfMattIsAlreadyExists(mattNew, prs);
			if (entity != null){ 
				//if true - the Matt is exists in DB and we should perform updating of existing Matt.
				//otherwise (if false) - saving New Matt
				result=entity.getMatt_id();
				//check if slots were changed
				if(!getSlotsFromDB(entity).equals(mattNew.getSlots())){
					//deleting existing slots from DB
					for(MattSlots slot: entity.getSlots())
						em.remove(slot);
					//saving new slots
					entity.setSlots(createListOfMattSlots(mattNew, entity));
				}
				
				
				entity.setName(mattNew.getData().getName());
				entity.setEndHour(mattNew.getData().getEndHour());
				entity.setnDays(mattNew.getData().getnDays());
				entity.setPassword(mattNew.getData().getPassword());
				entity.setStartDate(mattNew.getData().getStartDate());
				entity.setStartHour(mattNew.getData().getStartHour());
				entity.setTimeSlot(mattNew.getData().getTimeSlot());
				//set snCalendars 
				LinkedList<SnCalendarsEntity> snCalendars = new LinkedList<>();
				//checking if SN [] to download is not null
				String [] snDownload = mattNew.getData().getDownloadSN();
				if(snDownload != null && snDownload[0] != null){
					//passing through array of SNs and getting all calendar names for each SN
					for(int i=0; i<snDownload.length; i++){
						List<String> downloadCalendarName = mattNew.getData().getDownloadCalendars(snDownload[i]);
						//getting SocialNetworkEntity instance from DB
						SocialNetworkEntity snEntity = getSNInstanceFromDB(snDownload[i]);
						//creating separate SnCalendarsEntity for each Calendar.
						//Add the entity to Calendars to Download list
						for(String calendName: downloadCalendarName)
							snCalendars.add(new SnCalendarsEntity(entity, snEntity, 
									SnCalendarsEntity.DOWNLOAD, calendName));
					}
					
				}
				//checking if SN [] to upload is not null
				String [] snUpload = mattNew.getData().getUploadSN();
				if (snUpload != null && snUpload[0] != null){
					//passing through array of SNs and getting all calendar names for each SN
					for(int i=0; i<snUpload.length; i++){
						List<String> uploadCalendarName = mattNew.getData().getUploadCalendars(snUpload[i]);
						//getting SocialNetworkEntity instance from DB
						SocialNetworkEntity snEntity = getSNInstanceFromDB(snUpload[i]);
						//creating separate SnCalendarsEntity for each Calendar.
						//Add the entity to Calendars to Download list
						for(String calendName: uploadCalendarName)
							snCalendars.add(new SnCalendarsEntity(entity, snEntity, 
									SnCalendarsEntity.UPLOAD, calendName));
					}
				}
				//saving snCalendars
				if(entity.getSncalendars() != null && !snCalendars.equals(entity.getSncalendars())){ //if snCalendars list was changed 
					for(SnCalendarsEntity snCal : entity.getSncalendars()) //deleting old entities from DB
						em.remove(snCal);
					entity.setSncalendars(snCalendars); //setting new snCalendar list
				}
				
			}
			else {
		//saving to DB if newMatt name unique for the user
				
				//creating MattInfoEntity
					MattData data = mattNew.getData();
					MattInfoEntity mattInfo = new MattInfoEntity(data.getName(), data.getPassword(), 
							data.getnDays(), data.getStartDate(), data.getStartHour(), 
							data.getEndHour(), data.getTimeSlot(), prs);
				//creating List<MattSlots> to save slots to DB
					mattInfo.setSlots(createListOfMattSlots(mattNew, mattInfo));
					em.persist(mattInfo);
					result=mattInfo.getMatt_id();
					//set snCalendars 
					LinkedList<SnCalendarsEntity> snCalendars = new LinkedList<>();
					//checking if SN [] to download is not null
					String [] snDownload = mattNew.getData().getDownloadSN();
					if(snDownload != null && snDownload[0] != null){
						//passing through array of SNs and getting all calendar names for each SN
						for(int i=0; i<snDownload.length; i++){
							List<String> downloadCalendarName = mattNew.getData().getDownloadCalendars(snDownload[i]);
							//getting SocialNetworkEntity instance from DB
							SocialNetworkEntity snEntity = getSNInstanceFromDB(snDownload[i]);
							//creating separate SnCalendarsEntity for each Calendar.
							//Add the entity to Calendars to Download list
							for(String calendName: downloadCalendarName)
								snCalendars.add(new SnCalendarsEntity(mattInfo, snEntity, 
										SnCalendarsEntity.DOWNLOAD, calendName));
						}
					}
					//checking if SN [] to upload is not null
					String [] snUpload = mattNew.getData().getUploadSN();
					if (snUpload != null && snUpload[0] != null){
						//passing through array of SNs and getting all calendar names for each SN
						for(int i=0; i<snUpload.length; i++){
							List<String> uploadCalendarName = mattNew.getData().getUploadCalendars(snUpload[i]);
							//getting SocialNetworkEntity instance from DB
							SocialNetworkEntity snEntity = getSNInstanceFromDB(snUpload[i]);
							//creating separate SnCalendarsEntity for each Calendar.
							//Add the entity to Calendars to Download list
							for(String calendName: uploadCalendarName)
								snCalendars.add(new SnCalendarsEntity(mattInfo, snEntity, 
										SnCalendarsEntity.UPLOAD, calendName));
						}
					}
					mattInfo.setSncalendars(snCalendars); //setting new snCalendar list
			}
		}
		return result;
	}
	
	
	
	
	//getting SocialNetworkEntity instance from DB
	private SocialNetworkEntity getSNInstanceFromDB(String snName) {
		
		Query query = em.createQuery("SELECT sn FROM SocialNetworkEntity sn where sn.name= :snName");
		query.setParameter("snName", snName);
		return (SocialNetworkEntity) query.getSingleResult();
		
	}

	//creating List<MattSlots> to save slots to DB
	private List<MattSlots> createListOfMattSlots(Matt mattNew, MattInfoEntity entity) {
		Map<Date, LinkedList<Integer> > boolSlots_toSlotNums = slotsBoolToMap(mattNew.getSlots(), mattNew.getData());
		List<MattSlots> mattSlots = new ArrayList<MattSlots>();
		if (!boolSlots_toSlotNums.isEmpty()){ //Map isEmpty if no user selection
			for(Map.Entry<Date, LinkedList<Integer>> entry: boolSlots_toSlotNums.entrySet()){
				LinkedList<Integer> slotsByDate = entry.getValue();
			//creating list of separate MattSlots 
				for(int slot_num : slotsByDate){
					mattSlots.add(new MattSlots(entry.getKey(), slot_num, entity));
				}	
			}
	}
		return mattSlots;
	}

	private MattInfoEntity checkIfMattIsAlreadyExists(Matt mattNew, PersonEntity prs) {
		//checking if there is no Matt with this name for this user
			Query query = em.createQuery("select m from MattInfoEntity m "
					+ "where m.name = :mattName and m.personEntity= :person");
			query.setParameter("mattName", mattNew.getData().getName());
			query.setParameter("person", prs);
			List<MattInfoEntity> entity = query.getResultList();
		return entity.size() > 0 ? entity.get(0) : null;
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
		
		}
	}



	private Matt getMattFromMattEntity(MattInfoEntity entity, String username) {
		Matt matt = new Matt();
		MattData mattData = new MattData(entity.getName(), entity.getnDays(), entity.getStartDate(), 
					entity.getStartHour(), entity.getEndHour(), entity.getTimeSlot(), entity.getPassword());
		mattData.setMattId(entity.getMatt_id());							//additional mattId to mattDate
		//populating HashMap<String, List<String>[]> sncalendars
		List<SnCalendarsEntity> snCalendarsEntities= entity.getSncalendars(); //getting list of SnCalendars
		if(snCalendarsEntities != null && !snCalendarsEntities.isEmpty()){
			HashMap<String, List<String>[]> snCalendars = new HashMap<>(); //creating HashMap
			for(SnCalendarsEntity calend: snCalendarsEntities){
				//getting List of calendars for current SN
				List<String>[] calendarNames  = snCalendars.get(calend.getSocial_net().getName()); 
				if(snCalendars.containsKey(calend.getSocial_net().getName())){ //updating value for this key
					if(calend.getUpload_download_fl() == SnCalendarsEntity.UPLOAD){
						if (calendarNames[0] == null) //if no Calendar to Upload exists
							calendarNames[0] = new ArrayList<String>(); //creating upload list
						calendarNames[0].add(calend.getCalendarName());
						snCalendars.replace(calend.getSocial_net().getName(), calendarNames);
					}
					else if(calend.getUpload_download_fl() == SnCalendarsEntity.DOWNLOAD){
						if (calendarNames[1] == null) //if no Calendar to Download exists
							calendarNames[1] = new ArrayList<String>(); //creating Download list
						calendarNames[1].add(calend.getCalendarName());
						snCalendars.replace(calend.getSocial_net().getName(), calendarNames);
					}
					
				}
				else { //adding new key
					List<String>[] newCalendarNames = new List[2]; //creating array of Lists
					if(calend.getUpload_download_fl() == SnCalendarsEntity.UPLOAD){
						newCalendarNames[0] = new ArrayList<String>(); //creating upload list
						newCalendarNames[0].add(calend.getCalendarName());
						snCalendars.put(calend.getSocial_net().getName(), newCalendarNames);
					}
					else if (calend.getUpload_download_fl() == SnCalendarsEntity.DOWNLOAD){
						newCalendarNames[1] = new ArrayList<String>();
						newCalendarNames[1].add(calend.getCalendarName());
						snCalendars.put(calend.getSocial_net().getName(), newCalendarNames);
						
					}
				}
				
			}
			//setting snCalendars to MattData
			mattData.setSNCalendars(snCalendars);		
		}
			matt.setData(mattData);
			matt.setSlots(getSlotsFromDB(entity)); //getting slots from DB
			//if the Matt isn't synchronized with SN => returning existing Matt, otherwise - invoking getSlots() from iBackCon.
		return (entity.getSncalendars() != null && !entity.getSncalendars().isEmpty()) ?
				   iBackCon.getSlots(entity.getPersonEntity().getEmail(), matt) : matt;
	}
		
	
	@Override
	@Transactional
	//functions call sequence: getMatt() -> getMattFromMattEntity() -> getSlotsFromDB()
	public Matt getMatt(int matt_id) {
		MattInfoEntity entity = em.find(MattInfoEntity.class, matt_id); //looking for mattEntity by ID
		//getting username from the entity and invoking getMattFromMattEntity() if MattEntity was found
		//returning null if MattEntity doesn't exists
		return (entity != null) ? 
				getMattFromMattEntity(entity, entity.getPersonEntity().getEmail()) : null; 
	}
	
	
	
	private ArrayList<Boolean> getSlotsFromDB(MattInfoEntity mattEntity) {
	//determining number of slots and creating Boolean list with all slots marked as false.
		int numberOfSlotsPerDay=mattEntity.getEndHour()-mattEntity.getStartHour();
		int slotsNumber = numberOfSlotsPerDay * mattEntity.getnDays() * FesBes1.MIN_PER_HOUR/mattEntity.getTimeSlot();
		ArrayList<Boolean> slotsFromDB = new ArrayList<Boolean>(Collections.nCopies(slotsNumber, true));
	//taking busy slot numbers from DB and changing ArrayList values (setting to true) by the index
		List<MattSlots> mattSlots = mattEntity.getSlots();
		if (mattSlots != null)
			for (MattSlots mattSlot : mattSlots)
				//TODO check if we should put true/false in the list
				slotsFromDB.set(mattSlot.getSlot_number(), false);
			
		return slotsFromDB;
	}



	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	@Override
	public boolean removeMatt(int matt_id) {
		boolean result=false;
		MattInfoEntity deleteRow = em.find(MattInfoEntity.class, matt_id);
		if (deleteRow != null){
			em.remove(deleteRow);
			result=true;
		}		
		return result;
	}
	

	@Override
	public HashMap<Integer, String> getMattNames(String username) {
		HashMap<Integer, String> result=new HashMap<Integer, String>(); 
		PersonEntity prs = getPEbyEmail(username);
		 String str = "Select m from MattInfoEntity m where m.personEntity = :user";
		 Query query = em.createQuery(str); //sending query
		 query.setParameter("user", prs);
		 ArrayList<MattInfoEntity> listOfMats = (ArrayList<MattInfoEntity>) (query.getResultList()); //getting result list
		 for(MattInfoEntity entity:listOfMats)
		 		result.put(entity.getMatt_id(),entity.getName());
		 return result;
	}

	@Override
	public Person getProfile(String email) {
		PersonEntity pe = getPEbyEmail(email);
		return new Person(pe.getName(), pe.getEmail(), pe.getPassword(), pe.getTimeZone());
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
				pe.setName(person.getName());
				pe.setTimeZone(person.getTimeZone());	
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
		Query query = em.createQuery("SELECT pe FROM PersonEntity pe WHERE pe.email=?1");
		query.setParameter(1, email);
		List<PersonEntity> prsList = query.getResultList();
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
	    List<NotificationEntity> noteList=null;
	    List<Notification> rt = new LinkedList<>();
	    Query query = em.createQuery("select n from NotificationEntity n where n.guest_email= :guestName");
	    query.setParameter("guestName", guestName);
	    noteList = query.getResultList();
	    if (noteList != null && !noteList.isEmpty())
	    for(NotificationEntity ne:noteList){
	    	Notification notif = new Notification();
	    	
	    	notif.mattId = ne.getMattInfo().getMatt_id();
	    	notif.mattName = ne.getMattInfo().getName();
	    	PersonEntity person=ne.getMattInfo().getPersonEntity();
	    	if(person != null){
	    		notif.nameOfUser=person.getName();//or username??
	    		notif.userEmail=person.getEmail();
	    	}
	    	
	    																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																								
	    	rt.add(notif);
	   	   }
	   return rt;
	 }
	
	
	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW)
	public boolean setGuests(int matt_id, String [] guestEmails) {
		Boolean result=false;
		MattInfoEntity mattInfo = em.find(MattInfoEntity.class, matt_id);
			if (mattInfo!= null){
				result=true;
				for(int i=0;i<guestEmails.length;i++){
					NotificationEntity notification = new NotificationEntity(mattInfo, guestEmails[i]);
					em.persist(notification);
				}
			iBackCon.sendInvitation(mattInfo.getPersonEntity().getEmail(), 
					mattInfo.getPersonEntity().getName(), mattInfo.getName(), guestEmails);
			}
			return result;
	}

	/* ***** Deprecated Methods *******/
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
		
		
		
		//Deprecated
		//@Override
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
		
		
		//Deprecated
		/* getting list of user Social Networks, 
		   invoking getSlots() function to get Boolean ArrayList of free/busy intervals.*/
		private ArrayList<Boolean> getSlotsFromSN(MattData data, String username) {
			ArrayList<Boolean> slots=null;
		//get the list of SN for the user
		/*	PersonEntity prs = getPEbyEmail(username);
			// TODO
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
				//TODO
				//slots = (ArrayList<Boolean>)iBackCon.getSlots(prs.getEmail(), snNames.toArray(new String[snNames.size()]), data); //prs.getEmail() = username
						}*/
			return slots;
		}

		@Override
		public String[] getGuests(int mattId) {
			Query query = em.createQuery("select e from NotificationEntity e join e.mattInfo m where m.matt_id = :matt_id");
			query.setParameter("matt_id", mattId);
			List<NotificationEntity> nf = query.getResultList();
			if(nf!=null){
				String[] result = new String[nf.size()];
				int ind = 0;
				for(NotificationEntity ne: nf)
					result[ind++] = ne.getGuest_email();
				return result;
			}
			return null;
		}

		@Override
		public Matt updateInvitationMatt(int matt_id, String username,
				HashMap<String, List<String>> sncalendars) {
				
				Matt result=getMatt(matt_id);  												// create new Matt obtained by id		
				if(sncalendars!=null){
					MattData resultdata=result.getData();
					resultdata.setSNCalendars(new HashMap<String, List<String>[]>());
					Set<Entry<String,List<String>>> start=sncalendars.entrySet();  			//create start point for mooving through HashMap
						for(Entry<String,List<String>>entry:start){							//go around sncalendars 
						resultdata.setDownloadCalendars(entry.getKey(), entry.getValue());	//reset result HashMap from shcalendars
					}
				result=iBackCon.getSlots(username, result);									//update result
				}
			
		return result;
		}

	
}