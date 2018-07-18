package customExceptions; 


public class NotFlashable extends Exception {
     
     public NotFlashable(String message) {
          super(message);
     }
     
     public NotFlashable() {
          super("The current mixture is not flashable under these operating conditions.");
     }
     
}
