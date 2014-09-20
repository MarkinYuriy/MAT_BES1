package model;

import java.io.IOException;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mail.ISendActivationMail;
import mat.IBackConnector;
import mat.IFesBes1;
import mat.Matt;
import mat.MattData;
import mat.Person;
import mat.Response;

public class FesBes1 implements IFesBes1 {

	// Constructor???
	// fields???
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
	//getting list of user Social Networks
		Query query = em.createQuery("select p from Persons p where p.email= :username");
		query.setParameter("username", username);
		PersonEntity prs=(PersonEntity) query.getSingleResult(); //?????? may be changed to getResultList() for more safety.
		List<SocialNetworkEntity> snList = prs.getPersonSocialNetworks();
		List<String> snNames = new LinkedList<String>();
		for (SocialNetworkEntity sn: snList)
			snNames.add(sn.getName());
		ArrayList<Boolean> slots=null;
		
		try {
			slots = (ArrayList<Boolean>) iBackCon.getSlots(username,
						snNames.toArray(new String[snNames.size()]), data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	//creating new Matt
		mat.Matt newMatt = null;
		if (slots != null) {
			newMatt = new mat.Matt();
			newMatt.setData(data);
			newMatt.setSlots(slots);
		}
		return newMatt;
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
			Map<Date, LinkedList<Integer> > slotNumsFromSNs = slotsBoolToMap(mattOld.getSlots(), mattNew.getData());
			mat.Matt forSave = new mat.Matt();
			forSave.setData(mattNew.getData());
			forSave.setSlots(compareSlotMarks(mattOld.getSlots(),
					mattNew.getSlots()));
			// if(em.find(mat.Matt.class, forSave.data.name + "_" + username) ==
			// null){
			em.persist(forSave);
			result = true;
			// }
		}
		return result;
	}

	@Override
	public Matt getMatt(String mattName, String username) {

		Matt mattFromDB = em.find(Matt.class, mattName); // getters?
		MattData dataFromDB = mattFromDB.getData(); // MattData from DB
		Matt resMatt = new Matt();
		String[] snName = { "google+" };// //temporary!!!
		ArrayList<Boolean> slotsFromSN = null;
		try {
			slotsFromSN = (ArrayList<Boolean>) iBackCon.getSlots(username, snName, dataFromDB);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // getting
																					// slots
																					// from
																					// SN
		ArrayList<Boolean> slotsFromDB = mattFromDB.getSlots(); // getting slots
																// from DB
		ArrayList<Boolean> resSlotsList = null;
		boolean result; // result variable for merging slots
		for (int i = 0; i < slotsFromDB.size(); i++) {
			result = slotsFromDB.get(i) & slotsFromSN.get(i); // merging slots
																// logical &
			resSlotsList.add(result); // adding result slots to the result slots
										// List
		}
		resMatt.setData(dataFromDB);
		resMatt.setSlots(resSlotsList);
		return resMatt;
	}

	@Override
	public boolean removeMatt(String mattName, String username) {
		boolean result=false;
		Matt resMatt = new Matt();
			resMatt=getMatt(mattName, username);
			if (resMatt!=null){
				
			}
		return result;
	}

	private ArrayList<Boolean> compareSlotMarks(ArrayList<Boolean> oldSlots,
			ArrayList<Boolean> newSlots) {
		int size = oldSlots.size();
		ArrayList<Boolean> result = new ArrayList<Boolean>();
		for (int i = 0; i < size; i++) {
			if (oldSlots.get(i) != newSlots.get(i))
				result.add(true);
			else
				result.add(false);
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
		return em.find(Person.class, email);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void setActive(String email) {
		PersonEntity pe = getPEbyEmail(email);
		em.getTransaction().begin();
		pe.setActive(true);
		em.getTransaction().commit();
		
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
				em.getTransaction().begin();
				List<SocialNetworkEntity> personSocialNetworks = new ArrayList<SocialNetworkEntity>();
				for (int i=0; i<person.getSnNames().length; i++){
					SocialNetworkEntity sne = new SocialNetworkEntity(person.getSnNames()[i]);
					personSocialNetworks.add(sne);
				}
				pe.setPersonSocialNetworks(personSocialNetworks);
				em.getTransaction().commit();
				result = Response.OK;
			}
		}
		return result;
	}
	
	
}