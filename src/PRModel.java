import numericMethods.CubicEquationSolver;
import customExceptions.NotFlashable;
/**
 * Calculates Ki Values using the Peng-Robinson Equations of State
 * Requires cubic equations to be solved
 */
public class PRModel extends VLEModel
{
     private FlashTank flashTank;
     public static final double R = 8.314;
     
     /**
      * The method for determining Ki values.
      * Calculates Ki values based off of fugacity coefficients.
      * The only method that should be public. Other methods are intermediate steps in determining Ki values.
      * @param flashTank Is the flash tank to be solved.
      * @return The ki values for each species that is used to determine non-ideal VLE.
      * */
     public double[] calculateKi(FlashTank flashTank) throws NotFlashable {
          this.flashTank = flashTank;//it has to be shared between a ton of methods, but we don't want to use constructor          
          
          double A_vapour = calculateA(this.flashTank.getVapourStream().getMoleFractions());
          double B_vapour = calculateB(this.flashTank.getVapourStream().getMoleFractions());
          double A_liquid = calculateA(this.flashTank.getLiquidStream().getMoleFractions());
          double B_liquid = calculateB(this.flashTank.getLiquidStream().getMoleFractions());
          double[] Z_vapour = calculateZ(A_vapour,B_vapour); //For the liquid, the value of Z will be the smallest root from the cubic equation
          double[] Z_liquid = calculateZ(A_liquid,B_liquid); //For the vapour, the value of Z will be the largest root from the cubic equation
          
          double[] liquidFugacityCoefficients = calculateFugacityCoefficients(Z_liquid[0], this.flashTank.getLiquidStream().getMoleFractions());
          double[] vapourFugacityCoefficients = calculateFugacityCoefficients(Z_vapour[1], this.flashTank.getVapourStream().getMoleFractions());
          
          double[] ki = calculateFugacityCoefficientRatios(liquidFugacityCoefficients,vapourFugacityCoefficients);
          
           for (int i=0; i<this.flashTank.getFeedStream().getSpecies().length; i++) {
               if(!flashTank.getFeedStream().getSpecies()[i].getIsCondensable()) 
                    ki[i] = 1e30; //Just a really high number
          }
          
          return ki;
     }
     
     
//-------------------------------------------Calculating A--------------------------------------------
     
     /**
      * Need to calculate an 'A' term for both liquid and vapour phases.
      * @param moleFractions The species moleFractions will be from either the liquid phase (xi) or the vapour phase (yi).
      * @return The value of A for either the liquid or vapour phase. Dependent on all species in question.
      * */
     private double calculateA(double[] moleFractions) {
          double alphaA = calculateAlphaA(moleFractions);
          double pressure = flashTank.getPressure();
          double temperature = flashTank.getFlashTemp();
          
          return alphaA*pressure/(Math.pow(this.R, 2)*Math.pow(temperature, 2));
     }
     
      /**
      * Need to calculate an 'alphaA' term for both liquid and vapour phases.
      * @param moleFractions The species moleFractions will be from either the liquid phase (xi) or the vapour phase (yi).
      * Individual values of alphaA are combined to determine the overall alphaA parameter.
      * @return The value of alphaA for either the liquid or vapour phase. Dependent on all species in question.
      * */
     private double calculateAlphaA(double[] moleFractions) {
          //Equation 11.4a
          //Does summation include when i = j?
          double alphaA = 0;
          for (int i =0; i<moleFractions.length;i++) {
               for (int j = 0; j<this.flashTank.getFeedStream().getSpecies().length;j++) {
              
                 double alphaAi = calculateAlphaPure(i)*calculateaPure(i);
                 double alphaAj = calculateAlphaPure(j)*calculateaPure(j);
                 
                 double kij = calculateKij(i,j);
                 
                 double alphaAij = Math.sqrt(alphaAi*alphaAj)*(1-kij);
                 
                 alphaA += moleFractions[i]*moleFractions[j]*alphaAij;
               }
          }
          
          return alphaA;
     }
     
     /**
      * For the purpose of our simulator, this will always return 0.
      * @param i The index number for the first species in the for loop from calculateKi.
      * @param j The index number for the second species in the for loops from calculateKi.
      * @return The binary interaction parameter, which will simply be set to 0.
      **/
     private double calculateKij(int i, int j) {
       
       return 0;
     }
     
     /**
      * The alpha value is dependent on the species' accentric factor and critical temperature
      * @param i The index number for the first species in the for loop from calculateKi.
      * @return The alpha value, which is calculated for an individual species.
      **/
     private double calculateAlphaPure(int i) {
          double w = flashTank.getFeedStream().getSpecies()[i].getAccentricFactor();
          double criticalTemperature = this.flashTank.getFeedStream().getSpecies()[i].getCriticalTemperature();
          
          return Math.pow(1+(0.37464 + 1.54226*w-0.26992*Math.pow(w, 2))
                               *(1-Math.sqrt(this.flashTank.getFlashTemp()/criticalTemperature)),2);
     }
     
      /**
      * The a value is dependent on the species' critical pressure and critical temperature.
      * @param i The index number for the first species in the for loop from calculateKi.
      * @return The a value, which is calculated for an individual species.
      **/
     private double calculateaPure(int i) {
          
       double criticalPressure = flashTank.getFeedStream().getSpecies()[i].getCriticalPressure();
       double criticalTemperature = flashTank.getFeedStream().getSpecies()[i].getCriticalTemperature();
       
       return 0.45724*Math.pow(PRModel.R,2)*Math.pow(criticalTemperature, 2)/criticalPressure;
     }
     
     
//----------------------------------------------------Calculating B--------------------------------------
     
     
      /**
      * Need to calculate an 'A' term for both liquid and vapour phases.
      * @param moleFractions The species moleFractions will be from either the liquid phase (xi) or the vapour phase (yi).
      * @return The value of B for either the liquid or vapour phase. Dependent on all species in question.
      * */
     private double calculateB(double[] moleFractions) {
          
       double pressure = flashTank.getPressure();
       double b = calculatebMixture(moleFractions);
       double temperature = flashTank.getFlashTemp();
       
       return b*pressure/(PRModel.R*temperature);
     }
     
     /**
      * The 'B' term is dependent on the 'b' term, which is based on the mixture of species.
      * @param moleFractions The species moleFractions will be from either the liquid phase (xi) or the vapour phase (yi).
      * @return The value of b for either the liquid or vapour phase. Dependent on all species in question.
      * */
     private double calculatebMixture(double[] moleFractions) {
          
          double bMix=0;
          for (int i=0;i<flashTank.getFeedStream().getSpecies().length;i++) {
               double zi = moleFractions[i]; //Here zi is not the overall mole fraction, but that of the phase (xi or yi)
               double bi = calculatebPure(i);
               
               bMix += zi*bi;
          }
          
          return bMix;
     }
     
     /**
      * The 'b' term for the mixture is dependent on the 'b' term of each individual species.
      * @param i The ID number of the species being calculated.
      * @return The value of b for either the specific species.
      * */
     private double calculatebPure(int i) {
          double criticalTemperature = this.flashTank.getFeedStream().getSpecies()[i].getCriticalTemperature();
          double criticalPressure = this.flashTank.getFeedStream().getSpecies()[i].getCriticalPressure();
          
          
          return 0.07780*PRModel.R*criticalTemperature/criticalPressure;
     }
     
     
//---------------------------------------------------Calculating Z------------------------------------------
     
     /**
      * Solves the cubic equation of state for the compressibility factor.
      * @param A The A value for either the liquid or vapour phase.
      * @param B The B value for either the liquid or vapour phase. 
      * @return a sorted array of Z values.
      */
     private double[] calculateZ(double A, double B) throws NotFlashable {
          
       double a = -(1-B);
       double b = (A-2*B-3*B*B);
       double c = -(A*B-B*B-Math.pow(B,3));
       double[] constants = {a,b,c};
       
       return (new CubicEquationSolver()).solveCubicEquation(constants);
     }
     
     
//-----------------------------------Calculating Fugacity Coefficients---------------------------------
     
     
     /**
      * Determines the fugacity coefficients for either the liquid or vapour phase.
      * @param Z The compressibility factor for either the liquid or vapour phase.
      * @param moleFractions the xi or yi values.
      * @return a sorted array of Z values.
      */
     private double[] calculateFugacityCoefficients(double Z, double[] moleFractions) {
          
       double[] fugacityCoefficients = new double[this.flashTank.getFeedStream().getSpecies().length];
       
       for (int i =0; i<fugacityCoefficients.length; i++) {
         
         double A = calculateA(moleFractions);
         double AA = calculateAA(i, moleFractions);
         double B = calculateB(moleFractions);
         double BB = calculateBB(i, moleFractions);
         
         double lnFugacity = BB*(Z-1)-Math.log(Z-B)-A/(2*Math.sqrt(2)*B)*(AA-BB)
           *Math.log((Z+(Math.sqrt(2)+1)*B)/(Z-((Math.sqrt(2)-1)*B))); 
         
         fugacityCoefficients[i] = Math.exp(lnFugacity);
       }
       
       return fugacityCoefficients;
     }
     
      /**
      * Needed for determining fugacity coefficients.
      * Calculated for each species and is dependent on the mixture alphaA value and the alphaA value of the species with every other species.
      * @param i The ID number of the species whose AA value is being calculated. 
      * @param moleFractions the xi or yi values.
      * @return The AA value for the specific species in the liquid or vapour phase.
      */
     private double calculateAA(int i, double[] moleFractions) {
          
          double alphaAijSum = 0;
          double alphaAi = calculateAlphaPure(i)*calculateaPure(i);
          
          for (int j = 0; j<flashTank.getFeedStream().getSpecies().length; j++) {
               
            double alphaAj = calculateAlphaPure(j)*calculateaPure(j);
            
            double kij = calculateKij(i,j);
            
            double alphaAij = Math.sqrt(alphaAi*alphaAj)*(1-kij);
            alphaAijSum += moleFractions[j]*alphaAij;
          }   
          
          double alphaAMixture = calculateAlphaA(moleFractions);
          
          return (2/alphaAMixture)*alphaAijSum;
     }
     
       /**
      * Needed for determining fugacity coefficients.
      * Calculated for each species and is dependent on the mixture b value and the b value of the species with every other species.
      * @param i The ID number of the species whose AA value is being calculated. 
      * @param moleFractions the xi or yi values.
      * @return The BB value for the specific species in the liquid or vapour phase.
      */
     private double calculateBB(int i, double[] moleFractions) {
          
       double bi = calculatebPure(i);
       double bm = calculatebMixture(moleFractions);
          
       return bi/bm;
     }
     
     
//-------------------------------------------Calculating Coeff ratios--------------------------------
     
     
     /**
      * The actual Ki calculation!
      * Calculated for each species based on the liquid and vapour fugacity coefficients.
      * @param liquidFugacityCoefficients The fugacity coefficients of each species in the liquid phase.
      * @param vapourFugacityCoefficients The fugacity coefficients of each species in the vapour phase.
      * @return The Ki values for each species in an array.
      */
     private double[] calculateFugacityCoefficientRatios(double[] liquidFugacityCoefficients, double[] vapourFugacityCoefficients) {
          
          double[] ki = new double[liquidFugacityCoefficients.length]; 
          
          for (int i = 0; i<liquidFugacityCoefficients.length; i++)
            ki[i] = liquidFugacityCoefficients[i]/vapourFugacityCoefficients[i];
          
          
          return ki;
     }
     

     
}