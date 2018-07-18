import java.util.Arrays;
import customExceptions.*; //To get the innappropriate boundaries exception

/**
 * A molecular species with some of its associated properties and methods to calculate enthalpies, vapour pressures etc
 * Everything is done in SI units.
 * Abstract class with child classes being LiquidSpecies and VapourSpecies
 */
public abstract class Species implements Cloneable
{
 
     public static final double R = 8.314; //the ideal gas constant J/mol*K
     public static final double refTemp = 273.15; 
     
     //Basic properties
     private int idNum;
     private String name;
     private String formula;
     private int numMolecules;
     private double molarMass;  //kg/mol
     
     //Antoine's eq properties
     private double[] antoineConstants; 
     private double minTemp, maxTemp;  //the min and max temps for antoines equation in K
     
     //Latent heat properties
     private double molarLatHeat;  //J/mol @ normal boiling temp
     private double normBoilTemp;  //K
     
     //to calculate liquid heat capacity at a given temp
     private double[] liqHeatCapConstants;
     private boolean isSpecial; //True if the special equation should be used
     
     //to calculate gas heat capacity at a given temp
     private double[] gasHeatCapConstants;
     
     //To for non-ideal calculations
     private double criticalTemperature;  //K
     private double criticalPressure;  //pa
     private double accentricFactor;  //unitless
     
     //
     private boolean isCondensable;
     
     
//---------------------------------------------CONSTRUCTORS------------------------------------------------------------
     
     
     /**
      * Takes in an array of strings from the table as parameters.
      * @param physProperties an array of physical properties. Must be in the following order: idNum,Name,Formula,numMolecules,Molar Mass,antoineA,antoineB,antoineC,Tmin,Tmax,Latent Heat(molar),Boiling Temperature, liqheatC1, liqheaC2, liqheatC3, liqHeatC4, liqheatEquation, criticalTemp, criticalPress
      */
     public Species(String[] physProperties)
     {
         
         this.idNum = Integer.parseInt(physProperties[0]);
         this.name = physProperties[1];
         this.formula = physProperties[2];
         this.numMolecules = Integer.parseInt(physProperties[3]);
         this.molarMass = Double.parseDouble(physProperties[4]);
         
         this.isCondensable = Boolean.parseBoolean(physProperties[5]);
         
         this.antoineConstants = new double[3];
         for(int i=0; i<3; i++)
              this.antoineConstants[i] = Double.parseDouble(physProperties[i+6]);
         this.minTemp = Double.parseDouble(physProperties[9]);
         this.maxTemp = Double.parseDouble(physProperties[10]);
         
         this.molarLatHeat = Double.parseDouble(physProperties[11]);
         this.normBoilTemp = Double.parseDouble(physProperties[12]);
         
         this.liqHeatCapConstants = new double[5];
         for(int i=0; i<5; i++)
              this.liqHeatCapConstants[i] = Double.parseDouble(physProperties[i+13]);
         this.isSpecial = Boolean.parseBoolean(physProperties[18]);
         
         this.gasHeatCapConstants = new double[4];
         for (int i=0; i<4; i++)
              this.gasHeatCapConstants[i] = Double.parseDouble(physProperties[i+19]);
         
         this.criticalTemperature = Double.parseDouble(physProperties[23]);
         this.criticalPressure = Double.parseDouble(physProperties[24]);
         this.accentricFactor = Double.parseDouble(physProperties[25]);
         
     }//End of constructor
     
     
     /**
      * The copy constructor
      * @param toCopy the Species object to be copied
      */
     public Species(Species toCopy)
     {
          
          this.idNum = toCopy.idNum;
          this.name = toCopy.name;
          this.formula = toCopy.formula;
          this.numMolecules = toCopy.numMolecules;
          this.molarMass = toCopy.molarMass;
          
          this.antoineConstants = Arrays.copyOf(toCopy.antoineConstants, toCopy.antoineConstants.length);
          this.minTemp = toCopy.minTemp;
          this.maxTemp = toCopy.maxTemp;
          
          this.molarLatHeat = toCopy.molarLatHeat;
          this.normBoilTemp = toCopy.normBoilTemp;
          
          this.liqHeatCapConstants = Arrays.copyOf(toCopy.liqHeatCapConstants, toCopy.liqHeatCapConstants.length);
          this.isSpecial = toCopy.isSpecial;
          
          this.gasHeatCapConstants = Arrays.copyOf(toCopy.gasHeatCapConstants, toCopy.gasHeatCapConstants.length);
          
          this.criticalTemperature = toCopy.criticalTemperature;
          this.criticalPressure = toCopy.criticalPressure;
          this.accentricFactor = toCopy.accentricFactor;
          
          this.isCondensable = toCopy.isCondensable;
                
     }//End of copy constructor
     
     
     /**
      * The default constructor
      */
     public Species()
     {
          
          this.idNum = 0;
          this.name = "No name yet";
          this.formula = "No formula yet";
          this.numMolecules = 0;
          this.molarMass = 0.0;
          
          this.antoineConstants = null;
          this.minTemp = 0.0;
          this.maxTemp = 0.0;
          
          this.molarLatHeat = 0.0;
          this.normBoilTemp = 0.0;
          
          this.liqHeatCapConstants = null;
          this.isSpecial = false;
          
          this.gasHeatCapConstants = null;
          
          this.criticalTemperature = 0.0;
          this.criticalPressure = 0.0;
          this.accentricFactor = 0.0;
          
          this.isCondensable = true;
          
     }//End of default constructor
     
     
//-------------------------------------------------METHODS-------------------------------------------------------------
     
     
     /**
      * Returns the vapour pressure given a temperature using the Antoine Equation
      * @param temp the temperature in Kelvin
      * @return the saturation pressure in Pa
      */
     public double calcVapourPressure(double temp)
     {
          
          double pSat;
          double tempInC = temp - 273.15; //convert to C
          
          pSat =  (Math.pow(Math.E, antoineConstants[0] - (antoineConstants[1] / (antoineConstants[2] + tempInC))));
          
          return pSat * 1000; //convert from kPa to Pa
     }
            
     
     /**
      * Returns the heat capacity ovintegrated over the given temperature interval using equations from Perry's handbook.
      * @param tempStart the starting temperature used to calculate the integrated heat capacity in K
      * @param tempEnd the ending temperature used to calculate the integrated heat capacity in K
      * @return the integrated heat capacity in J/mol*K
      */
     protected double integrateLiqHeatCapacity(double tempStart, double tempEnd)
     {
          
          double iHeatCapacity = 0.0;
          
          //Calculate the heat capacity according to the necessary equations from Perry's handbook
          if(this.isSpecial)
          {
               System.out.println("This species (" + this.name + ") is special");
                                       
               double tStart = 1 - tempStart / this.criticalTemperature;
               double tEnd = 1 - tempEnd / this.criticalTemperature;
               iHeatCapacity = (Math.pow(this.liqHeatCapConstants[0],2) * (Math.log(Math.abs(tEnd))-Math.log(Math.abs(tStart)))
                    + this.liqHeatCapConstants[1] * (tEnd - tStart)
                    - this.liqHeatCapConstants[0] * this.liqHeatCapConstants[2] * (Math.pow(tEnd,2) - Math.pow(tStart,2))
                    - this.liqHeatCapConstants[0] * this.liqHeatCapConstants[3] * (Math.pow(tEnd,3) - Math.pow(tStart,3)) / 3
                    - Math.pow(this.liqHeatCapConstants[2],2) * (Math.pow(tEnd,4) - Math.pow(tStart,4)) / 12
                    - this.liqHeatCapConstants[2] * this.liqHeatCapConstants[3] * (Math.pow(tEnd,5) - Math.pow(tStart,5)) / 10
                    - Math.pow(this.liqHeatCapConstants[3],2) * (Math.pow(tEnd,6) - Math.pow(tStart,6)) / 30)
                    / (-1.0 / this.criticalTemperature); //this term comes from the substitution of t for T
          }
          else
          {
               iHeatCapacity = this.liqHeatCapConstants[0] * (tempEnd - tempStart)
                    + this.liqHeatCapConstants[1] * (Math.pow(tempEnd,2) - Math.pow(tempStart,2)) / 2
                    + this.liqHeatCapConstants[2] * (Math.pow(tempEnd,3) - Math.pow(tempStart,3)) / 3
                    + this.liqHeatCapConstants[3] * (Math.pow(tempEnd,4) - Math.pow(tempStart,4)) / 4
                    + this.liqHeatCapConstants[4] * (Math.pow(tempEnd,5) - Math.pow(tempStart,5)) / 5;
          }
          
          return iHeatCapacity / 1000; //need to convert to J/mol*K
     }
     
     
     /**
      * Calculates the enthalpy of the species at a given temperature compared to the reference enthalpy of liquid at 273.15
      * gaseous species are ideal gasses.
      * @param temp the temperature in K
      * @return the enthalpy in J/mol
      */
     public abstract double calcEnthalpy(double temp);                                        

     
     
//------------------------------------------HOUSEKEEPING METHODS--------------------------------------------------------
     
     
     public int getIdNum() {
          return this.idNum;
     }
     
     public String getName() {
          return this.name;
     }
     
     public String getFormula() {
          return this.formula;
     }
     
     public int getNumMolecules() {
          return this.numMolecules;
     }
     
     public double getmolarMass() {
          return this.molarMass;
     }
     
     public double[] getAntoineConstants() {
          double[] constants = Arrays.copyOf(this.antoineConstants, this.antoineConstants.length);
          return constants;
     }
     
     public double getMinTemp()
     {
          return this.minTemp;
     }
     
     public double getMaxTemp() {
          return this.maxTemp;
     }
     
     public double getMolarLatHeat() {
          return this.molarLatHeat;
     }
     
     public double getNormBoilTemp() {
          return this.normBoilTemp;
     }
     
     public double[] getLiqHeatCapConstants() {
          double[] constants = Arrays.copyOf(this.liqHeatCapConstants, this.liqHeatCapConstants.length);
          return constants;
     }
     
     public boolean getIsSpecial() {
          return this.isSpecial;
     }
     
     public double[] getGasHeatCapConstants() {
          return Arrays.copyOf(this.gasHeatCapConstants, this.gasHeatCapConstants.length);
     }
     
     public double getCriticalTemperature() {
          return this.criticalTemperature;
     }
     
     public double getCriticalPressure() {
          return this.criticalPressure;
     }
     
     public double getAccentricFactor() {
          return this.accentricFactor;
     }
     
     public boolean getIsCondensable() {
          return this.isCondensable;
     }
     
    
     /**
      * Changes the species to a different species. 
      * You can't change only one parameter in a species.
      * @param physProperties a new species from the species table
      */
     public void setSpecies(String[] physProperties) {
          
         this.idNum = Integer.parseInt(physProperties[0]);
         this.name = physProperties[1];
         this.formula = physProperties[2];
         this.numMolecules = Integer.parseInt(physProperties[3]);
         this.molarMass = Double.parseDouble(physProperties[4]);
         
         this.isCondensable = Boolean.parseBoolean(physProperties[5]);
         
         this.antoineConstants = new double[3];
         for(int i=0; i<3; i++)
              this.antoineConstants[i] = Double.parseDouble(physProperties[i+6]);
         this.minTemp = Double.parseDouble(physProperties[9]);
         this.maxTemp = Double.parseDouble(physProperties[10]);
         
         this.molarLatHeat = Double.parseDouble(physProperties[11]);
         this.normBoilTemp = Double.parseDouble(physProperties[12]);
         
         this.liqHeatCapConstants = new double[5];
         for(int i=0; i<5; i++)
              this.liqHeatCapConstants[i] = Double.parseDouble(physProperties[i+13]);
         this.isSpecial = Boolean.parseBoolean(physProperties[18]);
         
         this.gasHeatCapConstants = new double[4];
         for (int i=0; i<4; i++)
              this.gasHeatCapConstants[i] = Double.parseDouble(physProperties[i+19]);
         
         this.criticalTemperature = Double.parseDouble(physProperties[23]);
         this.criticalPressure = Double.parseDouble(physProperties[24]);
         this.accentricFactor = Double.parseDouble(physProperties[25]);
         
     }
     
     
     /**
      * Compares the instance variables of two species objects and returns TRUE if all equal.
      * @param toCompare an object of Species type
      * @return true if equals, false if not
      */
     public boolean equals(Species toCompare)
     {
          if(this.idNum == toCompare.idNum 
                  && this.name == toCompare.name
                  && this.formula == toCompare.formula
                  && this.numMolecules == toCompare.numMolecules
                  && this.molarMass == toCompare.molarMass
                  && Arrays.equals(this.antoineConstants, toCompare.antoineConstants)
                  && this.minTemp == toCompare.minTemp
                  && this.maxTemp == toCompare.maxTemp
                  && this.molarLatHeat == toCompare.molarLatHeat
                  && this.normBoilTemp == toCompare.normBoilTemp
                  && Arrays.equals(this.liqHeatCapConstants, toCompare.liqHeatCapConstants)
                  && this.isSpecial == toCompare.isSpecial
                  && Arrays.equals(this.gasHeatCapConstants, toCompare.gasHeatCapConstants)
                  && this.criticalTemperature == toCompare.criticalTemperature
                  && this.criticalPressure == toCompare.criticalPressure
                  && this.accentricFactor == toCompare.accentricFactor
                  && this.isCondensable == toCompare.isCondensable)
               
               return true;
          else
               return false;
     }//End of equals
     
     
     /**
      * Prints all the instance variables.
      * @return a string of all the species info
      */
     public String toString() 
     {
          return "Species idNum=" + idNum + "\nname=" + name + "\nformula="
               + formula + "\nnumMolecules=" + numMolecules + "\nmolarMass="
               + molarMass + "\nantoineConstants="
               + "\nisCondensable=" + isCondensable + "\n" 
               + Arrays.toString(antoineConstants) + "\nminTemp=" + minTemp
               + "\nmaxTemp=" + maxTemp + "\nmolarLatHeat=" + molarLatHeat
               + "\nnormBoilTemp=" + normBoilTemp + "\nliqHeatCapConstants="
               + Arrays.toString(liqHeatCapConstants) + "\nisSpecial="
               + isSpecial + "\ngasHeatCapConstants=" + Arrays.toString(gasHeatCapConstants) 
               + "\ncriticalTemperature=" + criticalTemperature
               + "\ncriticalPressure=" + criticalPressure
               + "\naccentricFactor=" + accentricFactor;
     }//End of tostring
     
     
     /**
      * @return new Species object
      */
     public abstract Species clone(); 
}