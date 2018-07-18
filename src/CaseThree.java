import numericMethods.*;
import customExceptions.NotFlashable;
import customExceptions.BadFunction;

/**
 * A FlashModel (FlashClient) class that represents system described in "Case Three" in the project requirement document
 * Requires Validation and Exception Handling Analysis.
 */
public class CaseThree extends FlashModel
{
     
     
     private FlashTank flashTank;
     private VLEModel vleModel;
     private double[] kiArray;
     
     
     
//------------------------------------------------CONSTRUCTORS---------------------------------------------------------
     
     
     /**
      * Takes in all requred instance variables for Case One (A tank, pressure and temperature).
      * @param flashTank must be a properly consturcted FlashTank Object.
      * @param vleModel The model used in determining the K values.
      */
     public CaseThree(FlashTank flashTank, VLEModel vleModel){
          
          this.flashTank = flashTank.clone();
          this.vleModel = vleModel; //No params, nothing to clone
          this.kiArray = new double[flashTank.getFeedStream().getSpecies().length];
          
     }
     
     /**
      * Default constructor
      * For constructing a blank object in the main method for the purpose of being overwritten
      * */
     public CaseThree()
     {
          this.flashTank = null;
          this.vleModel = null;
          this.kiArray = null;
     }
     
//-----------------------------------------------------METHODS---------------------------------------------------------
     
     
     /**
      * This FlashClients required solveSystem method definition. Should have a "Logger" to inform user status of system solution (completed succesfully, completed with errors etc)
      * @throws NotFlashable if the bubble and dewpoint checks fail.
      * @return a FlashTank object with all the parameters calculated according to case1
      */
     public FlashTank solveSystem() throws NotFlashable{
          
          //Create default liquid and vapour outlet streams @the flash temp and pressure
          LiquidSpecies[] liquidStreamSpecies = createLiquidSpeciesArray();
          VapourSpecies[] vapourStreamSpecies = createVapourSpeciesArray();
          
          double outletTemperature = this.flashTank.getFlashTemp();
          double outletPressure = this.flashTank.getPressure();
          double[] defaultMoleFractions = this.flashTank.getFeedStream().getMoleFractions(); 
          
          this.flashTank.setLiquidStream(new Stream(liquidStreamSpecies, 0.0, defaultMoleFractions, 
                                                    outletPressure, outletTemperature));
          this.flashTank.setVapourStream(new Stream(vapourStreamSpecies, 0.0, defaultMoleFractions,
                                                    outletPressure, outletTemperature));
           
          //Check if it is flashable
          checkFlashable();
          
          //Guess ki values
          this.kiArray = (new WilsonSRKModel()).calculateKi(this.flashTank);
          
          double tolerance = 0.0001;
          double maxDifference = 0.0;
          int counter = 1;
  
          try{
               do {
                    counter++;
                    //Calculate V, uses Ki values w. Ridder's method
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
                            
                              return summation;
                         }
                    } ); //returns VFRatio that satisfies mass balance
                    
                    double vapourFlow = this.flashTank.getFeedStream().getTotalMolarFlow() * VFRatio;
                    
                    this.flashTank.setVapourStreamFlowRate(vapourFlow);
                    
                    //Calculate L
                    double liquidFlow = flashTank.getFeedStream().getTotalMolarFlow() - vapourFlow; 
                    this.flashTank.setLiquidStreamFlowRate(liquidFlow);
                    
                    //Calculate yi[]
                    double[] yiArray = calculateYiArray();
                    this.flashTank.setVapourStreamMoleFractions(yiArray);
                    
                    //Calculate xi[] using mass balance (not k values)
                    double[] xiArray = calculateXiArray();
                    this.flashTank.setLiquidStreamMoleFractions(xiArray);
                    
                    //Recalculate the K values and compare
                    double[] kiOld = this.kiArray;
                    this.kiArray = vleModel.calculateKi(this.flashTank.clone()); 
                    
                    maxDifference = 0.0;
                    for(int i=0; i<this.kiArray.length; i++) {
                         double difference = Math.abs(kiOld[i]-kiArray[i]);
                         if(difference >= maxDifference)
                              maxDifference = difference;
                    }
                    
               } while((maxDifference>tolerance) && (counter<1000));

              
               for(int T = 300; T<=400; T++) {
                    FlashTank localFT = flashTank.clone();
                    
                    localFT.setFeedStreamTemperature(T);
                    
                    double toReturn =  localFT.getVapourStream().getTotalMolarFlow() * localFT.getVapourStream().totalEnthalpy()
                         + localFT.getLiquidStream().getTotalMolarFlow() * localFT.getLiquidStream().totalEnthalpy()
                         - localFT.getFeedStream().getTotalMolarFlow() * localFT.getFeedStream().totalEnthalpy();
                    
                    
                    System.out.println("T is: " + T + " and Q is " + toReturn);
               }
               
                    //Calculate T of the feed from 0=VHv+LHl-FHf
                    double backT = RootFinder.ridderRoot(1,2000.0,0,0.01,1000, new HasRoot() {
                    public double findYGivenX(double T) {
                         
                         FlashTank localFT = flashTank.clone();
                         
                         localFT.setFeedStreamTemperature(T);
                         double toReturn =  localFT.getVapourStream().getTotalMolarFlow() * localFT.getVapourStream().totalEnthalpy()
                              + localFT.getLiquidStream().getTotalMolarFlow() * localFT.getLiquidStream().totalEnthalpy()
                              - localFT.getFeedStream().getTotalMolarFlow() * localFT.getFeedStream().totalEnthalpy();
                         
                         
                         System.out.println("T is: " + T + " and is it 0? " + toReturn);
                         
                         return toReturn;
                    }   
               }); //returns T that satisfies mass balance
               this.flashTank.setFeedStreamTemperature(backT);
               
          }
          catch(BadFunction e) {
               System.out.println("Something broke in case three. You should never get this error here");
               System.exit(0);
          }
          
          
          return this.flashTank.clone();
     }
     
     
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
}//End of class