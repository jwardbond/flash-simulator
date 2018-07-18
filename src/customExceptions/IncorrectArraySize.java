package customExceptions;

/**
 * This is an exception that will be thrown when an array is the wrong size
 */
public class IncorrectArraySize extends Exception
{
     
     
     private String message;
     
     
//---------------------------------------------CONSTRUCTORS--------------------------------------------------
     
     
     /**
      * Creates a custom error message.
      * @param errorMessage A string containing a custom error message
      */
     public IncorrectArraySize(String message)
     {
          super(message);
     }//End of Constructor    
     
     
     /**
      * Creates an IncorrectArraySize object with the following error message
      * 
      * "The array you have entered is not the proper size for this method."
      * "Double check the number of elements you are supposed to have."
      */
     public IncorrectArraySize()
     {
          this.message = "The array you have entered is not the proper size for this method." +
               "\nDouble check the number of elements you are supposed to have.";
     }
     
}
