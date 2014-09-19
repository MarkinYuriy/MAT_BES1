package mat;

import java.io.Serializable;
import java.util.ArrayList;

public class Matt implements Serializable {
mat.MattData data;
ArrayList<Boolean> slots;
public mat.MattData getData() {return data;}
public void setData(mat.MattData data) {this.data = data;}
public ArrayList<Boolean> getSlots() {return slots;}
public void setSlots(ArrayList<Boolean> slots) {this.slots = slots;}
public String mattToJson() {
	 String res=data.mattDataToJSON();
	 if(slots!=null){
	 for(boolean it:slots){
	  res+=','+(it?1:0);}
	 res="{"+res+"}";
	 }
	 return res;
	}

}
