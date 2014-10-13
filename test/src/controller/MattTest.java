package controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import mat.IFesBes1;
import mat.Matt;
import mat.MattData;
import mat.Person;

public class MattTest {
	private static final int nIterations = 1;
	private static final int nDaysMax = 3;
	private static final int nDaysMin = 1;
	private static final int HOURS = 24;
	private static final int TIME_SLOT = 60; //minutes
	private static final int nTRUE = 5;	//create nTRUE busy slots 
	private static final int nCHANGED_BY_USER = 1;
	private static final int nPersons = 1;
	private static final int nNames = 1000;
	private static final String[] networks = {"Google", "Facebook"}; //,"facebook","apple","vk"};

	public static void main(String[] args) throws ParseException {
		AbstractApplicationContext ctx = new FileSystemXmlApplicationContext("beans.xml");
		IFesBes1 bes1=(IFesBes1) ctx.getBean("ifesbes1");
		testAlexandra(bes1);
		//testAnatoly(bes1);
		//testGlobal(bes1);
		ctx.close();
	}
	
	public static void testGlobal(IFesBes1 bes1) {
		mat.Person prs1 = newPersonWithMatt(bes1);
		mat.Person prs2 = newPersonWithMatt(bes1);
		mat.Matt matt2 = newMattForUser(bes1, prs2);
		mat.Person prs3 = newPersonWithMatt(bes1);
		mat.Matt matt3 = newMattForUser(bes1, prs2);
		mat.Matt testMatt = bes1.getMatt(matt2.getData().getName(), prs2.getEmail());
		
	}


	public static void testAlexandra(IFesBes1 bes1) throws ParseException{
	//fill SN networks table
	
	/*//create person
		  Person prs = new Person("Sasha", networks, "test1.iskatel@gmail.com", "12345"); //generatePerson();
		  bes1.setProfile(prs);*/
	//get Person from DB
		Person prs = bes1.getProfile("test1.iskatel@gmail.com");
	//generating random Matt's, invoking tested functions
		for (int i=0; i<nIterations; i++){
		//create person
			/*  Person prs = generatePerson();
			  prs.setSnNames(networks);
			  bes1.setProfile(prs);
			  String[] snNames = {"Google"};
			  prs.setSnNames(snNames);
			  bes1.updateProfile(prs);*/
		//generating random Matt data
			mat.MattData mData = generateMattData();	//randomly generating MattData
			//MattData mData = new MattData("manual Test1", 2, new SimpleDateFormat("MM-dd-yyyy").parse("10-13-2014"), 14, 18, 60, null);
			mat.Matt mattOld = bes1.createMatt(mData, prs.getEmail()); //creating Matt
			System.out.println(mData.getStartDate().toString());
			System.out.println(mData.getStartHour());
			assert(mattOld.getData() != null);
			assert(mattOld.getData().equals(mData));
					
			if (mattOld.getSlots() == null){
				ArrayList<Boolean> slots = generateSlots(mData);	//generating slots
				mattOld.setSlots(slots);
			}	
			Matt mattNew = createNewMatt(mattOld); 
			assert(mattNew.getSlots().size() == mattOld.getSlots().size());
			assert(mattNew.getData().equals(mattOld.getData()));
	
		//testing save and get Matt functions
			bes1.saveMatt(mattOld, mattNew, prs.getEmail());
			Matt testGetMatt = bes1.getMatt(mattNew.getData().getName(), prs.getEmail());
			
			assert(mattNew.getData().getName().equals(testGetMatt.getData().getName()));
			assert(mattNew.getData().getnDays() == testGetMatt.getData().getnDays());
			assert(mattNew.getData().getStartDate().equals(testGetMatt.getData().getStartDate()));
			
			assert(mattNew.getSlots().size() == testGetMatt.getSlots().size());
			int size = mattNew.getSlots().size();
			for(int j=0; j<size; j++)
				assert(mattNew.getSlots().get(j) == testGetMatt.getSlots().get(j));
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
		if (size > 0)
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
		if (slotsNumber > 0)
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
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
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
