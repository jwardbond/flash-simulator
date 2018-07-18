import numericMethods.*;
import customExceptions.NotFlashable;
import customExceptions.BadFunction;
import java.util.Arrays;

/**
 * A FlashModel (FlashClient) class that represents system described in "Case One" in the project requirment document
 * Requires Validation and Exception Handling Analysis.
 * @version 11.0 - 2017/11/21
 */
public class CaseTwo extends FlashModel
{
     
     
     private FlashTank flashTank;
     private VLEModel vleModel;
     private double[] kiArray;
     
     
     
     
//------------------------------------------------CONSTRUCTORS---------------------------------------------------------
     
     
     /**
      * Takes in all requred instance variables for Case One (A tank, pressure and temperature).
      * @param flashTank must be a properly consturcted FlashTank Object
      * @param vleModel The model used in determining the K values
      */
     public CaseTwo(FlashTank flashTank, VLEModel vleModel){
          
          this.flashTank = flashTank.clone();
          this.vleModel = vleModel; //No params, nothing to clone
          this.kiArray = new double[flashTank.getFeedStream().getSpecies().length];
          
     }
     
     /**
      * Default constructor
      * For constructing a blank object in the main method for the purpose of being overwritten
      * */
     public CaseTwo()
     {
          this.flashTank = null;
          this.vleModel = null;
          this.kiArray = null;
     }
     
//-----------------------------------------------------METHODS---------------------------------------------------------
     
     
     /**
      * This FlashClients required solveSystem method definition.
      * @throws NotFlashable if the bubble and dewpoint checks fail.
      * @return a FlashTank object with all the parameters calculated according to case1
      */
     public FlashTank solveSystem() throws NotFlashable{
          
          //Create default liquid and vapour outlet streams @the flash temp and pressure
          LiquidSpecies[] liquidStreamSpecies = createLiquidSpeciesArray();
          VapourSpecies[] vapourStreamSpecies = createVapourSpeciesArray();
          
          double outletTemperature = 0; //default flashTemp
          double outletPressure = this.flashTank.getPressure();
          double[] defaultMoleFractions = this.flashTank.getFeedStream().getMoleFractions(); 
          
          this.flashTank.setLiquidStream(new Stream(liquidStreamSpecies, 0.0, defaultMoleFractions, 
                                                    outletPressure, outletTemperature));
          this.flashTank.setVapourStream(new Stream(vapourStreamSpecies, 0.0, defaultMoleFractions,
                                                    outletPressure, outletTemperature));

          //get Temperature bounds from bubble to dew 

          
          double[] bounds = findBounds();
          
          System.out.println(Arrays.toString(bounds));
          //Ridder's method through temp to find an adiabatic temp
          try{
               double adiabaticTemp = RootFinder.ridderRoot(bounds[0], bounds[1], 0, 0.1, 2000, new HasRoot() {               
                    public double findYGivenX(double T) throws BadFunction {
                         flashTank.setFlashTemp(T);
                         
                         //Guess ki values
                         kiArray = (new WilsonSRKModel()).calculateKi(flashTank);
                         
                         double tolerance = 0.0001;
                         double maxDifference = 0.0;
                         int counter = 1;
                         
                         do {//solve the system at the given temp
                              counter++;
                              //Calculate V/F uses Ki values w. Ridder's method
                              //Implements the interface in an anonymous class
                              double VFRatio = RootFinder.ridderRoot(0,1,0,0.0001,2000, new HasRoot() {
                                   public double findYGivenX(double psi) {
                                        
                                        double summation = 0.0;
                                        
                                        //Extract feed mole fractions
                                        double[] moleFractionsFeed = flashTank.getFeedStream().getMoleFractions();
                                        
                                        //Calculate the sum of yi
                                        for (int i=0; i<flashTank.getFeedStream().getSpecies().length;i++)
                                        {
                                             summation += moleFractionsFeed[i] * (kiArray[i] - 1)
                                                  / (1 + psi * (kiArray[i]-1));
                                        }
                                        //System.out.println("Summation is: " + summation + " psi is: " + psi);
                                        
                                        return summation;
                                   }
                              } ); //returns VFRatio that satisfies mass balance
                              
                              double vapourFlow = flashTank.getFeedStream().getTotalMolarFlow() * VFRatio;
                              
                              flashTank.setVapourStreamFlowRate(vapourFlow);
                              
                              //Calculate L
                              double liquidFlow = flashTank.getFeedStream().getTotalMolarFlow() - vapourFlow; 
                              flashTank.setLiquidStreamFlowRate(liquidFlow);
                              
                              //Calculate yi[]
                              double[] yiArray = calculateYiArray();
                              flashTank.setVapourStreamMoleFractions(yiArray);
                              
                              //Calculate xi[] using mass balance (not k values)
                              double[] xiArray = calculateXiArray();
                              flashTank.setLiquidStreamMoleFractions(xiArray);
                              
                              //Recalculate the K values and compare
                              double[] kiOld = kiArray;
                              try {
                                   kiArray = vleModel.calculateKi(flashTank.clone()); 
                              }
                              catch (NotFlashable e) {
                                   throw new BadFunction("The origin of this is a NotFlashable error in case 2." 
                                                              + "It likely means that no T could be found in the flashable interval such that"
                                                              + "Q = 0... Returning the lowest Q found");
                              }
                              
                              maxDifference = 0.0;
                              for(int i=0; i<kiArray.length; i++) {
                                   double difference = Math.abs(kiOld[i]-kiArray[i]);
                                   if(difference >= maxDifference)
                                        maxDifference = difference;
                              }
                              
                         } while((maxDifference>tolerance) && (counter<1000));
                         
                         //Calculate Q, the temperature of the flash tank is known at this point
                         //Q = V*Hv + L*Hl - F*Hf
                         double Q = flashTank.getVapourStream().getTotalMolarFlow() * flashTank.getVapourStream().totalEnthalpy()
                              + flashTank.getLiquidStream().getTotalMolarFlow() * flashTank.getLiquidStream().totalEnthalpy()
                              - flashTank.getFeedStream().getTotalMolarFlow() * flashTank.getFeedStream().totalEnthalpy();
                         flashTank.setHeatExchangerQ(Q);
                         
                         System.out.println(T+"-"+Q);
                         return Q;
                    }
               });
          }
          catch(BadFunction e)
          {
               throw new NotFlashable("An adiabatic flash between [" + bounds[0] + "," + bounds[1] + "] is not possible");
          }
          
          
          
          return this.flashTank.clone();
     }
     
     /**
      * Determines if the mixture is flashable at a given pressure and temperature by calculating the sum(xi*ki) and sum(yi/ki).
      * If either of these are below 1, then the mixture is not flashable under the given conditions.
      * @throws NotFlashable if the mixture exceeds the bubble pressure or falls short of the dew pressure
      */
     private void checkFlashable() throws NotFlashable{
     //DEW POINT CHECK
          FlashTank localTank = this.flashTank.clone();
          //guess Ki
          double[] localKiArray = (new WilsonSRKModel()).calculateKi(localTank);
          double[] localKiArrayOld = null;
          
          //yi = zi
          localTank.setVapourStreamMoleFractions(localTank.getFeedStream().getMoleFractions());
          
          //calculate Ki and Xi
          double[] xiArray = new double[localTank.getFeedStream().getSpecies().length];
          double tolerance = 0.0001;
          double maxDifference = 0.0;
          int counter = 0;
          do{
               counter++;
               
               //Calculate xiArray               
               for(int i=0; i<xiArray.length; i++) {
                    if(localTank.getFeedStream().getSpecies()[i].getIsCondensable())
                         xiArray[i] = localTank.getFeedStream().getMoleFractions()[i]/localKiArray[i];
                    else 
                         xiArray[i] = 0;
               }
               localTank.setLiquidStreamMoleFractions(xiArray);
               
               //calculate new ki vals
               localKiArrayOld = localKiArray;
               localKiArray = this.vleModel.calculateKi(localTank);
               
               maxDifference = 0.0;
               for(int i=0; i<localKiArray.length; i++) {
                    double difference = Math.abs(localKiArrayOld[i]-localKiArray[i]);
                    if(difference >= maxDifference)
                         maxDifference = difference;
               }
          }while((maxDifference>tolerance) && (counter<1000));
          
          //calculate the dewsum
          double dewSum = 0.0;
          for(int i=0; i<xiArray.length; i++) {
               dewSum += xiArray[i]; //y/k
          }
          
     //BUBBLE POINT CHECK
          localTank = this.flashTank.clone();
          //guess Ki
          localKiArray = (new WilsonSRKModel()).calculateKi(localTank);
          localKiArrayOld = null;
          
          //xi=zi
          localTank.setLiquidStreamMoleFractions(localTank.getFeedStream().getMoleFractions());
          
          //calculate Ki and yi
          double[] yiArray = new double[localTank.getFeedStream().getSpecies().length];
          tolerance = 0.0001;
          maxDifference = 0.0;
          counter = 0;
          do{
               counter++;
               
               //Calculate yiArray               
               for(int i=0; i<yiArray.length; i++) {
                    if(localTank.getFeedStream().getSpecies()[i].getIsCondensable())
                         yiArray[i] = localTank.getFeedStream().getMoleFractions()[i] * localKiArray[i];
                    else 
                         yiArray[i] = localTank.getFeedStream().getMoleFractions()[i];
               }
               localTank.setVapourStreamMoleFractions(yiArray);
               
               //calculate new ki vals
               localKiArrayOld = localKiArray;
               localKiArray = this.vleModel.calculateKi(localTank);
               
               maxDifference = 0.0;
               for(int i=0; i<localKiArray.length; i++) {
                    double difference = Math.abs(localKiArrayOld[i]-localKiArray[i]);
                    if(difference >= maxDifference)
                         maxDifference = difference;
               }
          }while((maxDifference>tolerance) && (counter<1000));
          
          //calculate the bubbleSum
          double bubbleSum = 0.0;
          for(int i=0; i<yiArray.length; i++) {
               bubbleSum += yiArray[i]; //x*k
          }
          
          if(bubbleSum<1)
               throw new NotFlashable("System not solved...\nThe operating pressure is above the bubble pressure for this system.");
          else if(dewSum<1)
               throw new NotFlashable("System not solved...\nThe operating pressure is below the dew pressure for this system.");
     }
     
     /**
      * Determines an approximate dew and bubble temperature for the mixture by scanning from 100 to 2000K for the (first) range
      * at which the mixture is flashable.
      * @return an array with [bubble Temp, dew Temp]
      * @throws NotFlashable if no flashable range is found
      */
     private double[] findBounds() throws NotFlashable {
          FlashTank flashTankHolder = this.flashTank.clone(); //gotta hold dat. I need the original back
          
          double T = 100; //gotta start somewhere
          double[] toReturn = new double[2];
          int counter = 0;
          
          //Get the lower flashable temperature. loops while it isnt flashable
          boolean isFlashable = false;
          while(!isFlashable&&(counter<=2000)) {
               try {
                    checkFlashable();
                    toReturn[0] = T;//lowerbound
                    isFlashable = true;//end the loop when its true
               }
               catch(NotFlashable e) {//if it isnt flashable, try the next temp
                    T++;
                    this.flashTank.setFlashTemp(T);
                    counter++;
               }  
          }
          
          if(isFlashable == false) //if it hasn't reached flashable by 2000 iterations, its probably not flashable
               throw new NotFlashable();
          
          //Get the upper flashable bound. Loops while it is flashable.
          while(isFlashable&&(counter<=2000)) {
               this.flashTank.setFlashTemp(T);
               try {//if it is flashable, try the next temperature
                    checkFlashable();
                    T++;
                    this.flashTank.setFlashTemp(T);
                    counter++;
               }
               catch(NotFlashable e) {
                    toReturn[1] = T;//upper bound
                    isFlashable = false;//end the loop when its false
               }
               
          }
          
          this.flashTank = flashTankHolder.clone();   
          return toReturn;         
     }
     
     /**
      * Caculates the dew and bubble temperatures 
      * @return an array with [bubble temperature, dew temperature]
      */
     private double[] calculateTemperatureBounds() throws NotFlashable{ 
          FlashTank localTank = this.flashTank.clone();
          double[] toReturn = new double[2];
          
     
          //Estimate T
          double guessTemperature = 0.0;
          for(int i=0; i<localTank.getFeedStream().getSpecies().length; i++) {
               double Tc = localTank.getFeedStream().getSpecies()[i].getCriticalTemperature();
               double Pc = localTank.getFeedStream().getSpecies()[i].getCriticalPressure();
               double w = localTank.getFeedStream().getSpecies()[i].getAccentricFactor();
               double Ti_sat = Tc/(1 - 3 * Math.log(localTank.getPressure()/Pc) / 
                                   (Math.log(10) * (7+7*w)));
               
               if(localTank.getFeedStream().getSpecies()[i].getIsCondensable())
                    guessTemperature += Ti_sat * localTank.getFeedStream().getMoleFractions()[i];
          }
                    
          localTank = this.flashTank.clone();
          
      //BUBBLE TEMPERATURE
          //guess Ki
          double[] localKiArray = (new WilsonSRKModel()).calculateKi(localTank);
          double[] localKiArrayOld = null;
          
          //xi=zi
          localTank.setLiquidStreamMoleFractions(localTank.getFeedStream().getMoleFractions());
          
          double bubbleTemperature = guessTemperature;
          double bubbleSum = 0.0;
          int counterOuter = 0;
          boolean hasBeenPassed = false;
          do {
               counterOuter++;
               
               //calculate Ki and yi
               double[] yiArray = new double[localTank.getFeedStream().getSpecies().length];
               double tolerance = 0.0001;
               double maxDifference = 0.0;
               int counterInner = 0;
               do{
                    counterInner++;
                    
                    //Calculate yiArray               
                    for(int i=0; i<yiArray.length; i++) {
                         if(localTank.getFeedStream().getSpecies()[i].getIsCondensable())
                              yiArray[i] = localTank.getFeedStream().getMoleFractions()[i] * localKiArray[i];
                         else 
                              yiArray[i] = localTank.getFeedStream().getMoleFractions()[i];
                    }
                    localTank.setVapourStreamMoleFractions(yiArray);
                    
                    //calculate new ki vals
                    
                   
                    localKiArrayOld = localKiArray;
                    localKiArray = this.vleModel.calculateKi(localTank);
                    
                    maxDifference = 0.0;
                    for(int i=0; i<localKiArray.length; i++) {
                         double difference = Math.abs(localKiArrayOld[i]-localKiArray[i]);
                         if(difference >= maxDifference)
                              maxDifference = difference;
                    }
               }while((maxDifference>tolerance) && (counterInner<1000));
               
               //calculate the bubbleSum
               double bubbleSumOld = bubbleSum;
               bubbleSum = 0.0;
               for(int i=0; i<yiArray.length; i++) {
                    bubbleSum += yiArray[i]; //x*k
               }
               
               if(((bubbleSumOld<1)&&(bubbleSum>1))||((bubbleSumOld>1)&&(bubbleSum<1)))//if the step has passed the bubble point
                    hasBeenPassed = true;
               else if (bubbleSum>1) {//the temp is too high
                    bubbleTemperature--;
                    localTank.setFlashTemp(bubbleTemperature);
               }
               else if(bubbleSum<1) {//the temp is too low
                    bubbleTemperature++;
                    localTank.setFlashTemp(bubbleTemperature);
               }
               
          }while((!hasBeenPassed)&&(counterOuter<1000));
          toReturn[0] = bubbleTemperature;   
          
     //DEW TEMPERATURE
          localTank = this.flashTank.clone();
         //guess Ki
          localKiArray = (new WilsonSRKModel()).calculateKi(localTank);
          localKiArrayOld = null;
          
          //yi = zi
          localTank.setVapourStreamMoleFractions(localTank.getFeedStream().getMoleFractions());
          
          double dewTemperature = guessTemperature;
          double dewSum = 0;
          counterOuter = 0;
          hasBeenPassed = false;
          do{
               counterOuter++;
               
               //calculate Ki and Xi
               double[] xiArray = new double[localTank.getFeedStream().getSpecies().length];
               double tolerance = 0.0001;
               double maxDifference = 0.0;
               int counterInner = 0;
               do{
                    counterInner++;
                    
                    //Calculate xiArray               
                    for(int i=0; i<xiArray.length; i++) {
                         if(localTank.getFeedStream().getSpecies()[i].getIsCondensable())
                              xiArray[i] = localTank.getFeedStream().getMoleFractions()[i]/localKiArray[i];
                         else 
                              xiArray[i] = 0;
                    }
                    localTank.setLiquidStreamMoleFractions(xiArray);
                    
                    //calculate new ki vals
                    localKiArrayOld = localKiArray;
                    localKiArray = this.vleModel.calculateKi(localTank);
                    
                    maxDifference = 0.0;
                    for(int i=0; i<localKiArray.length; i++) {
                         double difference = Math.abs(localKiArrayOld[i]-localKiArray[i]);
                         if(difference >= maxDifference)
                              maxDifference = difference;
                    }
               }while((maxDifference>tolerance) && (counterInner<1000));
               
               //calculate the dewSum
               double dewSumOld = dewSum;
               dewSum = 0.0;
               for(int i=0; i<xiArray.length; i++) {
                    dewSum += xiArray[i]; //y/k
               }     
               
               //figure out which way to move the dewSum
               if(((dewSumOld<1)&&(dewSum>1))||((dewSumOld>1)&&(dewSum<1)))//if the step has passed the bubble point
                    hasBeenPassed = true;
               else if (dewSum>1) {//the temp is too high
                    dewTemperature--;
                    localTank.setFlashTemp(dewTemperature);
               }
               else if(dewSum<1) {//the temp is too low
                    dewTemperature++;
                    localTank.setFlashTemp(dewTemperature);
               }
               
          }while((!hasBeenPassed)&&(counterOuter<1000));
          toReturn[1] = dewTemperature;
          
          return toReturn; 
     }
     
     
     /**
      * Creates an array of liquid species matching the species in the feed stream.
      * @return an array of LiquidSpecies matching the feed stream species.
      */
     private LiquidSpecies[] createLiquidSpeciesArray() {
          
          int length = this.flashTank.getFeedStream().getSpecies().length;
          LiquidSpecies[] toReturn = new LiquidSpecies[length];
          
          for(int i=0; i<length; i++)
          {
               toReturn[i] = new LiquidSpecies(flashTank.getFeedStream().getSpecies()[i]); //The way LiquidSpecies is made, this will work even if the feed has different phases
          }
          
          return toReturn;      
     }
     
     /**
      * Creates an array of vapour species matching the species in the feed stream
      * @return an array of vapour species matching the feed stream species
      */
     private VapourSpecies[] createVapourSpeciesArray() {
          
          int length = flashTank.getFeedStream().getSpecies().length;
          VapourSpecies[] toReturn = new VapourSpecies[length];
          
          for(int i=0; i<length; i++)
          {
               toReturn[i] = new VapourSpecies(flashTank.getFeedStream().getSpecies()[i]); //The way LiquidSpecies is made, this will work even if the feed has different phases
          }
          
          return toReturn;      
     }
     
     
     /**
      * Only called after the flash tank V has been calculated
      * P
      */
     private double[] calculateYiArray() {
          
          double[] yiArray = new double[this.flashTank.getFeedStream().getSpecies().length];
          
          double[] moleFractionsFeed = this.flashTank.getFeedStream().getMoleFractions();
          double V = this.flashTank.getVapourStream().getTotalMolarFlow();
          
          //Calculate yis
          for (int i=0; i<this.flashTank.getFeedStream().getSpecies().length;i++)
          {
               if(!flashTank.getFeedStream().getSpecies()[i].getIsCondensable()) { //non-condensable
                    yiArray[i] = flashTank.getFeedStream().getTotalMolarFlow() * moleFractionsFeed[i] / V;
               }
               else {//condensable
                    yiArray[i] = flashTank.getFeedStream().getTotalMolarFlow() * moleFractionsFeed[i]* kiArray[i] 
                         / (flashTank.getFeedStream().getTotalMolarFlow() + V*(this.kiArray[i]-1));
               }
          }
          
          return yiArray;  
     }
     
     
     private double[] calculateXiArray() {
          
          double[] xiArray = new double[this.flashTank.getFeedStream().getSpecies().length];
          
          double[] yiArray = this.flashTank.getVapourStream().getMoleFractions();
          
          for(int i=0; i<xiArray.length; i++) {
               xiArray[i] = (this.flashTank.getFeedStream().getTotalMolarFlow() * this.flashTank.getFeedStream().getMoleFractions()[i]
                                  -  this.flashTank.getVapourStream().getTotalMolarFlow() * this.flashTank.getVapourStream().getMoleFractions()[i])
                    / this.flashTank.getLiquidStream().getTotalMolarFlow();

          }
          
          return xiArray;
     } 
}//End of Class