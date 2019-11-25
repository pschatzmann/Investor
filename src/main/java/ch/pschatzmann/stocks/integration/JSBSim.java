package ch.pschatzmann.stocks.integration;
import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * Simple Java integration to JSBSim
 * 
 * @author pschatzmann
 *
 */
public class JSBSim {
	
    public interface CLibrary extends Library {
    	CLibrary INSTANCE = (CLibrary) Native.loadLibrary("/usr/local/bin/jsbsim", CLibrary.class);
        public void main(int arc,String[]argc);
        public double getValue(String parameter);
        public void setValue(String parameter, double value);
        public void printCatalog();
    }

    public static void main(String[] args) {
 	   CLibrary.INSTANCE.main(args.length,args);
 	}
    
    public JSBSim() {   
    }
    
    public static JSBSim newInstance(String[] args) {
    	JSBSim sim = new JSBSim();
    	sim.start(args);
    	return sim;
    }
    

    public void start(String[] args) {
    	Thread thread = new Thread(new Runnable() {
    	    @Override
    	    public void run() {
    	   	   CLibrary.INSTANCE.main(args.length,args);
    	    }
    	});
    	thread.start();
  	}
    


    public void catalog() {
    	CLibrary.INSTANCE.printCatalog();
    }
    
    public void terminate() {
    	setValue("fdm/jsbsim/simulation/terminate ",1);
    }

    public void setValue(String par, double value) {
    	CLibrary.INSTANCE.setValue(par, value);
    }
    public double getValue(String par, double value) {
    	return CLibrary.INSTANCE.getValue(par);
    }

}