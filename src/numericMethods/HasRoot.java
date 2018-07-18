package numericMethods;
import customExceptions.BadFunction;

/**
 * Interface for equations that can be solved.
 * Note: got rid of the y-target. It will just be a parameter of Bisection method in RootFinder
 */
public interface HasRoot
{
     public double findYGivenX(double x) throws BadFunction;
          
}//end of interface