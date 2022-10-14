import java.lang.Thread;
import java.lang.String;

public class testtime {
      public static void main(String args[]){
            try{
                  long timestamp = System.currentTimeMillis() / 1000;
                  System.out.println("Current time: "+timestamp+" s");
                  Thread.sleep((long)(Math.random() * 1000)); 
                  long timestamp2 = System.currentTimeMillis() / 1000;
                  System.out.println("Current time: "+timestamp+" s");
                  System.out.println("Difference: "+(timestamp2-timestamp)+" s");
            }
            catch(Exception e){
                  e.getMessage();
            }
            
      }   
}
