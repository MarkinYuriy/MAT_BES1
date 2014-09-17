package model;

import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.aspectj.apache.bcel.classfile.Signature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mail.ISendActivationMail;
import mat.IBes1Bes2;
import mat.IFesBes1;
import mat.Matt;
import mat.MattData;
import mat.Person;
import mat.Response;

public class FesBes1 implements IFesBes1 {

	// Constructor???
	// fields???
	@PersistenceContext(unitName = "springHibernate")
	EntityManager em;

	@Autowired
	ApplicationContext ctx;

	@Autowired
	IBes1Bes2 b1b2;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public int setProfile(Person person) {
		int result = Response.NO_REGISTRATION;
		if (person != null) {
			PersonEntity currentPE = getPEbyEmail(person.getEmail()); //currentPE is a personEntity with considered email from database
			if (currentPE == null) {									//currentPE not exists
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
		Query query = em.createQuery("SELECT pe FROM PersonEntity pe WHERE pe.email=?1");
		query.setParameter(1, email);
		return (PersonEntity) query.getSingleResult();
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
	public Matt createMatt(MattData data, String username) {
		/*
		 * using service IBes1Bes2, described in file beans.xml bean
		 * id="bes1bes2" property name="serviceUrl"
		 * value="http://localhost:8080/bes1bes2_service/bes1bes2_service.service"
		 * property name="serviceInterface" value="mat.IBes1Bes2"
		 */
		String[] snName = { "google+" }; // temporary!!!
/*		Query query = em.createQuery("select p from Person p where p.email= :username");
		query.setParameter("username", username);
		Person prs=(Person) query.getSingleResult(); //?????? may be changed to getResultList() for more safety.
		String[] snName = prs.getSnNames();*/
		ArrayList<Boolean> slots = (ArrayList<Boolean>) b1b2.getSlots(username,
				snName, data);
		Map<Date, LinkedList<Integer> > slotNumbersByDate = slotsBoolToMap(slots, data);
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
			for (int i=0; i<size; i++){
				if(slots.get(i)){
					int curr = i+1; //because i begins with zero
					int dayNumber = curr/numberOfSlotsPerDay; //because division returns the number of past days
					Calendar calendar = new GregorianCalendar();
					calendar.setTime(data.getStartDate());
					calendar.add(Calendar.DATE, dayNumber);
					
					
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

		ArrayList<Boolean> slotsFromSN = (ArrayList<Boolean>) b1b2.getSlots(username, snName, dataFromDB); // getting
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
		// TODO Auto-generated method stub
		return false;
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
		int result = Response.NO_REGISTRATION;
		if (person != null) {
			String email = person.getEmail();
			PersonEntity pe = getPEbyEmail(email);
			if (pe != null) {
				em.getTransaction().begin();
				pe.setSnNames(person.getSnNames());
				em.getTransaction().commit();
				result = Response.OK;
			}
		}
		return result;
	}
	
	
}