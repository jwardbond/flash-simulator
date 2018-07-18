import numericMethods.*;
import customExceptions.NotFlashable;

/**
 * Parent class for cases, just for the sake of compiling
 * Child classes consist of CaseOne, CaseTwo, and CaseThree
 */
public abstract class FlashModel
{
    public abstract FlashTank solveSystem() throws NotFlashable;
     
}
