package customExceptions;

/**
 * Used when the boundaries of an interval end on  values that they shouldn't
 */
public class InappropriateBoundaries extends Exception
{
     
     
     private String message;
     
     
//------------------------------------------------CONSTRUCTORS-------------------------------------------------------
     
     
     /**
      * Creates an InappropriateBoundaries object with the default error message.
      * @param boundaryOne One of the illegal boundaries
      * @param boundaryTwo The other boundary
      */
     public InappropriateBoundaries(double boundaryOne, double boundaryTwo)
     {
          this.message = "The boundaries " + boundaryOne + " and " + boundaryTwo + " cannot be used for these calculations./n"
               + "Please input new boundaries.";
     }
     
     
     /**
      * Creates an InappropriateBoundaries object with a custom error message.
      * @param message The custom error message.
      */
     public InappropriateBoundaries(String message) 
     { 
          super(message);
     }//End of custom message constructor
     
}//End of InappropriateBoundaries class
