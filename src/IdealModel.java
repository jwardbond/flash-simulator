import customExceptions.NotFlashable;

/**
 * Calculates the K values for each species with Psat/P (Raoult's Law)
 */
public class IdealModel extends VLEModel
{
  
     /**
      * Calculates the K values for each species with Psat/P (Raoult's Law).
      * @param flashTank The flash tank being solved.
      * @return The ideal K values for each species.
      */
     public double[] calculateKi(FlashTank flashTank)
     {
          
          double[] kiArray = new double[flashTank.getFeedStream().getSpecies().length];
               
          for (int i=0; i<flashTank.getFeedStream().getSpecies().length; i++)
          {
               //calculate vapour pressure at flash tank temperature.
               kiArray[i] = flashTank.getFeedStream().getSpecies()[i].calcVapourPressure(flashTank.getFlashTemp())/flashTank.getPressure();
          }
          
          for (int i=0; i<flashTank.getFeedStream().getSpecies().length; i++) {
               if(!flashTank.getFeedStream().getSpecies()[i].getIsCondensable()) //T>Tc
                    kiArray[i] = 1e30; //Just a really high number
          }
          
          return kiArray;
     }     
}