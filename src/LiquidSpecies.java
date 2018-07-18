import java.util.Arrays;
import customExceptions.*; //To get the innappropriate boundaries exception


/**
 * Class representing a species in the liquid phase.
 * This class uses a liquid-only method of calculating species enthalpy.
 */
public class LiquidSpecies extends Species
{
//------------------------------------------------CONSTRUCTORS------------------------------------------------------
     
     /**
      * The constructor. Calls the super constructor as there are no additional instance variables
      * @param physProperties an array of physical properties. Must be in the following order: idNum,Name,Formula,numMolecules,Molar Mass,antoineA,antoineB,antoineC,Tmin,Tmax,Latent Heat(molar),Boiling Temperature, liqheatC1, liqheaC2, liqheatC3, liqHeatC4, liqheatEquation, criticalTemp, criticalPress
      * */
     public LiquidSpecies(String[] physProperties)
     {
          super(physProperties);
     }//End of constructor
     
     
     /**
      * The copy constructor.
      * There are no new instance variables here, so it should be able to take a gas species and make it into a liquid object
      * @param toCopy The object to be changed.
      */
     public LiquidSpecies(Species toCopy)
     {
          super(toCopy);
     }//End of Copy Constructor
     
     
     /**
      * The default constructor.
      */
     public LiquidSpecies()
     {
          super();
     }//End of default constructor
     

//-------------------------------------------------METHODS------------------------------------------------------------
       
     
     /**
      * Calculates the enthalpy of the liquid species at a given temperature using the reference temp.
      * Will return 0 if the species is not condensable.
      * * @param temp the temperature in K
      * @return the enthalpy in J/mol
      */
     public double calcEnthalpy(double temp)
     {                                 
          double enthalpy = 0.0;
          
          if(super.getIsCondensable() == false)
               enthalpy = 0.0;
          else 
               enthalpy = integrateLiqHeatCapacity(super.refTemp, temp);

          return enthalpy; //J/mol     
     }
     
     
     /**
      * @return a new LiquidSpecies object
      */
     public LiquidSpecies clone()
     {
          return new LiquidSpecies(this);
     }

     
}//End of LiquidSpecies class
