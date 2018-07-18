import java.util.Arrays;
import customExceptions.*; //To get the innappropriate boundaries exception


/**
 * A class representing a species in the vapour phase.
 * This class also needs the liquid heat capacity, that is why liquid heat capacity is still in the species class.
 * This class adds methods to calculate gas heat capacities assuming they behave as ideal gases, and 
 */
public class VapourSpecies extends Species 
{   
//--------------------------------------------------CONSTRUCTORS------------------------------------------------------
     
     /**
      * The constructor. Calls the super constructor as there are no additional instance variables
      * @param physProperties an array of physical properties. Must be in the following order: idNum,Name,Formula,numMolecules,Molar Mass,antoineA,antoineB,antoineC,Tmin,Tmax,Latent Heat(molar),Boiling Temperature, liqheatC1, liqheaC2, liqheatC3, liqHeatC4, liqheatEquation, criticalTemp, criticalPress
      * */
     public VapourSpecies(String[] physProperties)
     {
          super(physProperties);
     }//End of constructor
     
     
     /**
      * The copy constructor. 
      * There are no new instance variables here so it should be able to take a liquid species and make it into a gas species.
      * @param toCopy The object to be copied.
      */
     public VapourSpecies(Species toCopy)
     {
          super(toCopy);
     }//End of copy constructor
     
     /**
      * Default constructor
      * */
     public VapourSpecies()
     {
          super(); 
     }
     
     
//-----------------------------------------------METHODS---------------------------------------------------------------     
     
     
     /**
      * Calculates the gas enthalpy assuming ideal gas and using the integrated equation from Van Ness
      * @param tempStart the starting temperature in K
      * @param tempEnd the final temperature in K
      * @return The ideal gas heat capacity in J/mol*K
      */
     private double integrateGasHeatCapacity(double tempStart, double tempEnd)
     {

               double gasEnthalpy = (super.getGasHeatCapConstants()[0] * (tempEnd - tempStart)
                                          + super.getGasHeatCapConstants()[1] * (Math.pow(tempEnd, 2) - Math.pow(tempStart, 2)) / 2
                                          + super.getGasHeatCapConstants()[2] * (Math.pow(tempEnd, 3) - Math.pow(tempStart, 3)) / 3
                                          - super.getGasHeatCapConstants()[3] * (1/tempEnd - 1/tempStart)) * super.R;
               
               return gasEnthalpy;
     }
     
     
     /**
      * Calculates the difference in enthalpy of a gas when temperature changes from the reference temp to the temp of interest.
      * Since the latent heat is only known at one temperature, we have to exploit the path-independant properties of enthalpy.
      * 
      * Calculates the enthalpy change to go from ref temp to boiling point as a liquid, and then from boiling point to actual temp as a gas.
      * 
      * Ignores pressure effects.
      * gaseous species are ideal gasses.
      * @param temp the temperature in K
      * @return the enthalpy in J/mol
      */
     public double calcEnthalpy(double temp)                                         
     {
          
          double heatCapacity = 0.0;
          double enthalpy = 0.0;
          boolean isZero = true;
          
          //Check if all heat capacity constants are 0
          for(int i=0; isZero && i<super.getGasHeatCapConstants().length; i++)
               isZero = (super.getGasHeatCapConstants()[i] == 0.0);
          
          if(isZero) { //approximate heat capacity with R               
               System.out.println("The gas heat capacity array is filled with zeros... \nApproximating the gas heat capacity" 
                                       + " with the ideal gas constant");
               if(super.getNumMolecules() == 1)
                    heatCapacity = 3/2 * R;
               else if(super.getNumMolecules() == 2)
                    heatCapacity = 5/2 * R;
               else if(super.getNumMolecules() > 2)
                    heatCapacity = 3 * R; 
               else {
                    System.out.println("The number of molecules is not a positive integer. Try re-entering the species");
                    System.exit(0);
               }
               
               //Calculate enthalpy with assumed heat cap
               if(super.getIsCondensable() == false)
                    enthalpy = heatCapacity * (temp - super.refTemp);
               else 
                    enthalpy = super.integrateLiqHeatCapacity(super.refTemp, super.getNormBoilTemp()) + super.getMolarLatHeat() + heatCapacity *(temp - super.getNormBoilTemp());
          }
          
          //if heat cap constants are known
          else if(super.getIsCondensable() == false) 
               enthalpy = this.integrateGasHeatCapacity(super.refTemp, temp);          
          else 
               enthalpy = super.integrateLiqHeatCapacity(super.refTemp, super.getNormBoilTemp()) + super.getMolarLatHeat() + this.integrateGasHeatCapacity(super.getNormBoilTemp(), temp);
          
          return enthalpy; //J/mol
     }
     
     
//------------------------------------------------HOUSEKEEPING METHODS----------------------------------
     
     
     /**
      * @return a new VapourSpecies object
      */
     public VapourSpecies clone()
     {
          return new VapourSpecies(this);
     }
     
}
