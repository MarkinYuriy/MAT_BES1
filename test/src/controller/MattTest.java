package controller;

import java.util.*;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import mat.IFesBes1;
import mat.Matt;
import mat.MattData;
import mat.Person;

public class MattTest {
	private static final int nIterations = 2;
	private static final int nDaysMax = 10;
	private static final int nDaysMin = 1;
	private static final int HOURS = 24;
	private static final int TIME_SLOT = 60; //minutes
	private static final int nTRUE = 5;	//create nTRUE busy slots 
	private static final int nCHANGED_BY_USER = 3;
	private static final int nPersons = 1;
	private static final int nNames = 1000;
	private static final String[] networks = {"google","facebook","apple","vk"};

	public static void main(String[] args) {
		AbstractApplicationContext ctx = new FileSystemXmlApplicationContext("beans.xml");
		IFesBes1 bes1=(IFesBes1) ctx.getBean("ifesbes1");
<<<<<<< HEAD
		//testAlexandra(bes1);
		//testAnatoly(bes1);
		testGlobal(bes1);
=======
		testAlexandra(bes1);
		//testAnatoly(bes1);
>>>>>>> origin/development
		
	}
	
	public static void testGlobal(IFesBes1 bes1) {
		mat.Person prs1 = newPersonWithMatt(bes1);
		mat.Person prs2 = newPersonWithMatt(bes1);
		mat.Matt matt2 = newMattForUser(bes1, prs2);
		mat.Person prs3 = newPersonWithMatt(bes1);
		mat.Matt matt3 = newMattForUser(bes1, prs2);
		mat.Matt testMatt = bes1.getMatt(matt2.getData().getName(), prs2.getEmail());
		
	}


	public static void testAlexandra(IFesBes1 bes1){
	
	//generating random Matt's, invoking tested functions
		for (int i=0; i<nIterations; i++){
		//create person
			  Person prs = generatePerson();
			  bes1.setProfile(prs);
		//generating random Matt data
			mat.MattData mData = generateMattData();	//randomly generating MattData
			mat.Matt mattOld = bes1.createMatt(mData, prs.getEmail()); //creating Matt
			assert(mattOld.getData() != null);
			assert(mattOld.getData().equals(mData));
					
			if (mattOld.getSlots() == null){
				ArrayList<Boolean> slots = generateSlots(mData);	//generating slots
				mattOld.setSlots(slots);
			}	
			Matt mattNew = createNewMatt(mattOld); 
			assert(mattNew.getSlots().size() == mattOld.getSlots().size());
			assert(mattNew.getData().equals(mattOld.getData()));
			
		/*	System.out.println(mattOld.getData().getName());
			System.out.println(mattOld.getData().getStartDate());
			System.out.println(mattOld.getSlots().size());*/
		//testing save Matt function
			//String username = "name " + (int)(Math.random()*nIterations);
			bes1.saveMatt(mattOld, mattNew, prs.getEmail());
			}
}
	
	public static void testAnatoly(IFesBes1 bes1){
		int set1 = 10, set2 = 10, set3 = 10, set4 = 10;
		Person prs = null;
		for (int i=0; i<nPersons; i++){
			prs = generatePerson();
			set1 = bes1.setProfile(prs);
			set2 = bes1.setProfile(prs);
			//bes1.setActive(prs.getEmail());
			set3 = bes1.setProfile(prs);
			prs.setSnNames(networks);
			set4 = bes1.updateProfile(prs);
		}
		String email = prs.getEmail();
		String password = prs.getPassword();
		int login1 = bes1.matLogin(email, password);
		email = "email49456@gmail.com";
		password  = "password49456";
		int login2 = bes1.matLogin(email, password);
		System.out.println(set1);
		System.out.println(set2);
		System.out.println(set3);
		System.out.println(set4);
		System.out.println(login1);
		System.out.println(login2);
	}
	
	private static Matt createNewMatt(Matt mattOld) {
		Matt mattNew = new Matt();
		mattNew.setData(mattOld.getData());
		ArrayList<Boolean> newSlots = new ArrayList<Boolean>();
		newSlots.addAll(mattOld.getSlots());
		int size = newSlots.size();
		for(int i=0; i<nCHANGED_BY_USER; i++){
			int index =  (int)(Math.random()*size);
			newSlots.set(index, true);
		}
		mattNew.setSlots(newSlots);
		return mattNew;
	}

	private static ArrayList<Boolean> generateSlots(MattData mData) {
		int slotsNumber = mData.getnDays() * (mData.getEndHour() - mData.getStartHour())*(mData.getTimeSlot()/60); //60 - minutes in an hour.
		ArrayList<Boolean> slots = new ArrayList<Boolean>(Collections.nCopies(slotsNumber, false));
		for(int i=0; i<nTRUE; i++){
			int index = (int)(Math.random()*slotsNumber);
			slots.set(index, true);
		}
		return slots;
	}

	public static mat.MattData generateMattData(){
		String name = "name " + (int)(Math.random()*nIterations*1000);
		int nDays = (int)(Math.random()*(nDaysMax-nDaysMin)) + nDaysMin;
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.add(GregorianCalendar.DATE, (int)(Math.random()*(nDaysMax-nDaysMin)) + nDaysMin);
		int startHour = (int)(Math.random()*HOURS); //generate number between 0 and 23
		int duration = (int)(Math.random()*(HOURS - startHour));
		return new mat.MattData(name, nDays, calendar.getTime(), startHour, (startHour+duration), TIME_SLOT, null);
			
	}
	public static mat.Person generatePerson(){
		int number = (int)(Math.random()*nNames);
		String name = "name"+number;
		String email = "email"+number+"@gmail.com";
		String password = "password"+number;
		int snNumber = (int)(Math.random()*networks.length);
		String[] snNames = new String [snNumber];
		for (int i=0; i<snNumber; i++){
			snNames[i] = networks[i];
		}
		return new Person(name, snNames, email, password);
	}
	
	private static Person newPersonWithMatt(IFesBes1 bes1) {
		Person prs1 = generatePerson();
		bes1.setProfile(prs1);
		newMattForUser(bes1, prs1);
		return prs1;
	}

	private static mat.Matt newMattForUser(IFesBes1 bes1, Person prs1) {
		mat.MattData mData = generateMattData();
		ArrayList<Boolean> slots = generateSlots(mData);
		mat.Matt mattOld = new mat.Matt();
		mattOld.setData(mData);
		mattOld.setSlots(slots);
		Matt mattNew = createNewMatt(mattOld);
		bes1.saveMatt(mattOld, mattNew, prs1.getEmail());
		return mattNew;
	}

}
