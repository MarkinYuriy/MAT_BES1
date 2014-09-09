package model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mat.IBes1Bes2;
import mat.IFesBes1;
import mat.Matt;
import mat.MattData;
import mat.Person;

public class FesBes1 implements IFesBes1, IBes1Bes2{
	
	//Constructor???
	//fields???
	@PersistenceContext(unitName="springHibernate")
	EntityManager em;
	
	@Autowired
	ApplicationContext ctx;
	
	@Autowired
	IBes1Bes2 b1b2;
	
	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW)
	public boolean setProfile(Person person) {
		boolean result = false;
		if ((person != null) && (em.find(Person.class, person.getUserName()) == null)) {
			em.persist(person);
			result = true;
		}
		return result;
	}

	@Override
	public boolean matLogin(String username, String password) {
		boolean result = false;
		Person prs = em.find(Person.class, username);
		if ((prs != null) && (prs.getPassword() == password))
			result = true;
		
		return result;
	}

	@Override
	public boolean snLogin(String username, String snName) {
		boolean result = false;
		IBes1Bes2 bes2 = (IBes1Bes2) ctx.getBean("bes2");
		Person prs = em.find(Person.class, username);
		if (prs != null) {
			List<mat.SocialNetwork> networks = prs.getNetworks();
			String snUsername = (networks.get(networks.indexOf(snName))).getSnUsername(); // looking for compliance SN - snUsername
			result = bes2.setIdentity(snUsername, prs.getUserName(), snName);
		}
		return result;
	}

	@Override
	public Matt createMatt(MattData data, String username) {
/*	using service IBes1Bes2, described in file beans.xml
	bean id="bes1bes2"
	property name="serviceUrl" value="http://localhost:8080/bes1bes2_service/bes1bes2_service.service"
	property name="serviceInterface" value="mat.IBes1Bes2" */
		String[] snName = {"google+"}; //temporary!!!
		ArrayList<Boolean> slots = (ArrayList<Boolean>) b1b2.getSlots(username, snName, data);
		mat.Matt newMatt=null;
		if (slots != null){
			newMatt = new mat.Matt();
			newMatt.setData(data);
			newMatt.setSlots(slots);
		}
		return newMatt;
	}
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW)
	@Override
	public boolean saveMatt(Matt mattOld, Matt mattNew, String username) {
		boolean result=false;
		if(mattNew != null && mattOld != null && username != null){
			mat.Matt forSave = new mat.Matt();
			forSave.setData(mattNew.getData());
			forSave.setSlots(compareSlotMarks(mattOld.getSlots(), mattNew.getSlots()));
			//if(em.find(mat.Matt.class, forSave.data.name + "_" + username) == null){
				em.persist(forSave);
				result = true; 
				//}
		}
	return result;
	}
		
	@Override
	public Matt getMatt(String mattName, String username) {
		
		Matt mattFromDB = em.find(Matt.class, mattName);	//getters?
		MattData dataFromDB = mattFromDB.getData(); 		//MattData from DB
		Matt resMatt = new Matt();
		String[] snName = {"google+"};//  //temporary!!!
		
		ArrayList<Boolean> slotsFromSN = getSlots(username, snName, dataFromDB); //getting slots from SN
		ArrayList<Boolean> slotsFromDB = mattFromDB.getSlots(); //getting slots from DB
		ArrayList<Boolean> resSlotsList = null; 
		boolean result; //result variable for merging slots
		for(int i=0; i<slotsFromDB.size(); i++){
			result = slotsFromDB.get(i)&slotsFromSN.get(i); //merging slots logical &
			resSlotsList.add(result);	//adding result slots to the result slots List
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

	@Override
	public boolean setIdentity(String snUsername, String matUsername, String snName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<Boolean> getSlots(String username, String[] snName, MattData interval) {
		// TODO Auto-generated method stub
		return null;
	}

	
	private ArrayList<Boolean> compareSlotMarks(ArrayList<Boolean> oldSlots, ArrayList<Boolean> newSlots) {
		int size = oldSlots.size();
		ArrayList<Boolean> result= new ArrayList<Boolean>();
		for(int i=0; i<size; i++){
			if(oldSlots.get(i) != newSlots.get(i))
				result.add(true);
			else
				result.add(false);
		}
		return result;
	}
}
