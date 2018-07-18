/**
 * Calculates the K values for each species based on critical pressure and critical temperature
 */
public class WilsonSRKModel extends VLEModel
{
  /**
 * The K values here usually serve as initial guesses for the other models.
 * @param flashTank The flash tank being solved.
 * @return The initial K values of the species.
 */
  public double[] calculateKi(FlashTank flashTank) {
    int n = flashTank.getFeedStream().getSpecies().length;
    double[] initialKi = new double[n];
    
    for (int i=0;i<n;i++) {      
      double critP = flashTank.getFeedStream().getSpecies()[i].getCriticalPressure();
      double P = flashTank.getPressure();
      double w = flashTank.getFeedStream().getSpecies()[i].getAccentricFactor();
      double critT = flashTank.getFeedStream().getSpecies()[i].getCriticalTemperature();
      double T = flashTank.getFlashTemp();
      
      initialKi[i] = critP/P * Math.exp(5.37 * (1+w) * (1-critT/T));
    }
    
     for (int i=0; i<flashTank.getFeedStream().getSpecies().length; i++) {
               if(!flashTank.getFeedStream().getSpecies()[i].getIsCondensable()) //T>Tc
                    initialKi[i] = 1e30; //Just a really high number
          }
     
    return initialKi;
  }
}