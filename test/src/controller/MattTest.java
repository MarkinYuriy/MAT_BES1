package controller;

import java.util.*;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import mat.IFesBes1;
import mat.Matt;
import mat.MattData;
import mat.Person;

public class MattTest {
	private static final int nIterations = 100;
	private static final int nDaysMax = 10;
	private static final int nDaysMin = 1;
	private static final int HOURS = 24;
	private static final int TIME_SLOT = 60; //minutes
	private static final int nTRUE = 5;	//create nTRUE busy slots 
	private static final int nCHANGED_BY_USER = 3;
	private static final int nPersons = 2;
	private static final int nNames = 1000;
	private static final String[] networks = {"google","facebook","apple","vk"};

	public static void main(String[] args) {
		AbstractApplicationContext ctx = new FileSystemXmlApplicationContext("beans.xml");
		IFesBes1 bes1=(IFesBes1) ctx.getBean("ifesbes1");
		//testAlexandra(bes1);
		testAnatoly(bes1);
		
	}
	
	public static void testAlexandra(IFesBes1 bes1){
		//generating random Matt's, invoking tested functions
				for (int i=0; i<nIterations; i++){
					mat.MattData mData = generateMattData();	//randomly generating MattData
					ArrayList<Boolean> slots = generateSlots(mData);	//generating slots
					mat.Matt mattOld = new mat.Matt();	//creating Matt
					mattOld.setData(mData);
					mattOld.setSlots(slots);
					Matt mattNew = createNewMatt(mattOld); 
				//testing save Matt function
					String username = "name " + (int)(Math.random()*nIterations);
					bes1.saveMatt(mattOld, mattNew, username);
				}
	}
	
	public static void testAnatoly(IFesBes1 bes1){
		for (int i=0; i<nPersons; i++){
			Person prs = generatePerson();
			bes1.setProfile(prs);
		}
	}
	
	private static Matt createNewMatt(Matt mattOld) {
		Matt mattNew = new Matt();
		mattNew.setData(mattOld.getData());
		ArrayList<Boolean> newSlots = mattOld.getSlots();
		for(int i=0; i<nCHANGED_BY_USER; i++){
			int index =  (int)(Math.random()*mattOld.getSlots().size());
			newSlots.set(index, true);
		}
		mattNew.setSlots(newSlots);
		return mattNew;
	}

	private static ArrayList<Boolean> generateSlots(MattData mData) {
		int slotsNumber = mData.getnDays() * (mData.getEndHour() - mData.getStartHour())/(mData.getTimeSlot()/60); //60 - minutes in an hour.
		ArrayList<Boolean> slots = new ArrayList<Boolean>(slotsNumber);
		Collections.fill(slots, false);
		for(int i=0; i<nTRUE; i++){
			int index = (int)(Math.random()*slotsNumber);
			slots.set(index, true);
		}
		return slots;
	}

	public static mat.MattData generateMattData(){
		String name = "name " + (int)Math.random()*nIterations;
		int nDays = (int)(Math.random()*(nDaysMax-nDaysMin)) + nDaysMin;
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.add(GregorianCalendar.DATE, (int)(Math.random()*(nDaysMax-nDaysMin)) + nDaysMin);
		int startHour = (int)(Math.random()*HOURS); //generate number between 0 and 23
		int duration = (int)(Math.random()*(HOURS - startHour));
		return new mat.MattData(name, nDays, calendar.getTime(), startHour, (startHour+duration), TIME_SLOT, null);
			
	}
	public static mat.Person generatePerson(){
		int number = (int)(Math.random())*nNames;
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
}
