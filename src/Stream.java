import java.util.Arrays;

/**
 * A class representing a stream.
 * Contains a set of species along with flow rates, temperature, and pressure.
 */
public class Stream implements Cloneable
{ 
     //Physical properties of each species
     private Species[] species;
     
     //Component breakdown
     private double[] molarFlows;
     private double[] moleFractions;
     private double totalMolarFlow;
     
     //Properties of stream
     private double temperature;
     private double pressure;
     
      
     //--------------------------------------------------CONSTRUCTORS---------------------------------------------------
          
     /**
      * Receies an array of @param Species objects, each with physical and thermodynamic properties.
      * Defined by amount of each component, temperature and pressure of system.
      * @param species Each species in the stream
      * @param molarFlows The molar flow of each species in the stream
      * @param pressure The pressure of the stream
      * @param temperature The temperature of the stream
      */    
     public Stream(Species[] species, double[] molarFlows, double pressure, double temperature) {
          
          if (species.length != molarFlows.length) {
               System.out.println("Error: Array lengths don't match");
               System.exit(0);
          }
          this.species = new Species[species.length];
          this.molarFlows = new double[molarFlows.length];
          this.moleFractions = new double[molarFlows.length];
          for (int i=0;i<species.length;i++) {
               this.species[i] = species[i];
               this.molarFlows[i] = molarFlows[i];
          }
          
          this.temperature = temperature;
          this.pressure = pressure;
          this.totalMolarFlow = 0; //calculated once molar flows have been assigned
          
       
          //Total molar flow rate calculated from individual flow rates
          for (int i=0; i<molarFlows.length;i++) {
               this.totalMolarFlow = this.totalMolarFlow + molarFlows[i];
          }
          
          //Mole Fractions determined based on molar flow rates and total molar flow rate
          for (int i=0;i<molarFlows.length;i++) {
            this.moleFractions[i]=this.molarFlows[i]/this.totalMolarFlow;
          }
          
     } //End of constructor
     
     //If the user provides the total flow instead of component flows
      public Stream(Species[] species, double totalMolarFlow, double[] moleFractions, double pressure, double temperature) {
          
          if(species.length != moleFractions.length) {
               System.out.println("Error: Array lengths don't match");
               System.exit(0);
          }
          this.species = new Species[species.length];
         
          this.moleFractions = new double[moleFractions.length];
          for (int i=0;i<species.length;i++) {
               this.species[i] = species[i];
               this.moleFractions[i] = moleFractions[i];
          }
          
          this.temperature = temperature;
          this.pressure = pressure;
          this.totalMolarFlow = totalMolarFlow; 
          
       
          //Component molar flow rates calculated from total flow rate and component fractions
          this.molarFlows = new double[species.length];
          for (int i=0; i<this.molarFlows.length;i++) {
               this.molarFlows[i] = this.totalMolarFlow*this.moleFractions[i];
          }
          
     } //End of constructor
    
      /**
       * The default constructor
       * For creating empty streams (outlets) that will be solved
       * */
      public Stream() {
        this.species = new LiquidSpecies[0];
     
        //Component breakdown
        this.molarFlows = new double[0];
        this.moleFractions = new double[0];
        totalMolarFlow=0;
     
        //Properties of stream
        this.temperature=0;
        this.pressure=0;
      }
      
      /**
      * The copy constructor
      * @param streamToCopy an object of type Stream
      */
     public Stream(Stream streamToCopy) {
          
          this.temperature=streamToCopy.temperature;
          this.pressure=streamToCopy.pressure;
          this.totalMolarFlow=streamToCopy.totalMolarFlow;
          
          this.species = new Species[streamToCopy.species.length];
          this.molarFlows = new double[streamToCopy.molarFlows.length];
          this.moleFractions = new double[streamToCopy.moleFractions.length];
          
          for (int i=0;i<this.species.length;i++)
          {
               this.species[i] = streamToCopy.species[i].clone(); //will make new species objects
               this.molarFlows[i] = streamToCopy.molarFlows[i];
               this.moleFractions[i] = streamToCopy.moleFractions[i];
          }
          
     } //End of copy constructor
     
     
     //---------------------------------------------------METHODS-------------------------------------------------------
     
     
     /*
      * calculates the total enthalpy in J/mol of the stream using the streams temperature
      * @Return enthalpy of the feed stream in J/s
      */
     public double totalEnthalpy() {
          double totalEnthalpy;
          double totalMolarEnthalpy = 0;
                    
          for (int i=0;i<this.species.length;i++) {
               totalMolarEnthalpy = totalMolarEnthalpy + this.moleFractions[i]*this.species[i].calcEnthalpy(this.temperature);
          }
          
          totalEnthalpy = totalMolarEnthalpy*this.totalMolarFlow;
          return totalEnthalpy;//J          
     }
     
     
//-------------------------------------------------HOUSEKEEPING METHODS-------------------------------------------------
     
     
     public Species[] getSpecies() {
          Species[] returnSpecies = new Species[this.species.length];
          
          for (int i = 0; i< species.length; i++) {
               returnSpecies[i] = this.species[i].clone();
          }
          
          return returnSpecies;
     }
     public double[] getMolarFlows() {
          return (Arrays.copyOf(this.molarFlows, this.molarFlows.length));
     }  
     public double[] getMoleFractions() {
          return (Arrays.copyOf(this.moleFractions, this.moleFractions.length));
     }
     public double getTotalMolarFlow() {
          return this.totalMolarFlow;
     }
     public double getTemperature() {
          return this.temperature;
     }
     public double getPressure() {
          return this.pressure;
     }
     
        
     public void setSpecies(Species[] species) {
          for (int i=0; i<species.length; i++) {
               this.species[i] = species[i].clone();
          }
     }
     /**
      * Sets the molar flow rates of each species, and recalculates the corresponding total molar flow and mole fractions
      * @param molarFlows The incoming array of molar flows
      */
     public void setMolarFlow(double[] molarFlows) {
          this.totalMolarFlow = 0;   
          for(int i=0;i<molarFlows.length;i++) {
               this.molarFlows[i]=molarFlows[i];
               this.totalMolarFlow = this.totalMolarFlow + molarFlows[i];
          }   
          for(int i=0; i<molarFlows.length;i++) {
               this.moleFractions[i] = this.molarFlows[i]/this.totalMolarFlow;
          }
     }
     
     /**
      * Sets the mole fraction of each species and recalculates the molar flows.
      * @param moleFractions The incomming array of mole fractions
      */
     public void setMoleFractions(double[] moleFractions) {
          this.moleFractions = Arrays.copyOf(moleFractions, moleFractions.length);
          for(int i=0; i<this.species.length; i++)
               this.molarFlows[i] = this.totalMolarFlow * this.moleFractions[i];
     }
     /**
      * Sets the total molar flow rate, and recalculates the corresponding individual molar flow rates.
      * @param totalMolarFlow The incoming total molar flow rate.
      */
     public void setTotalMolarFlow(double totalMolarFlow) {
          this.totalMolarFlow = totalMolarFlow;
          
          //Calc new molar flows
          for(int i=0; i<this.species.length; i++)
               this.molarFlows[i] = totalMolarFlow * this.moleFractions[i];
     }
     public void setTemperature(double temperature) {
          this.temperature=temperature;
     } 
     public void setPressure(double pressure) {
          this.pressure = pressure;
     }

     /**
      * Overriden equals method.
      * Invokes the equals method from the species class to compare each variable within each species
      * returns false if any variable is different in value
      * @param stream the stream that is compared with for equals
      * @return true if equals, false if not
      */
     public boolean equals(Stream stream)
     {
          if ((this.species.length!=stream.species.length) || (this.molarFlows.length!=stream.molarFlows.length) || (this.moleFractions.length!=stream.moleFractions.length))
          {
               return false;
          }
          
          for (int i=0;i<this.species.length;i++)
          {
               boolean isTrue = (this.species[i].equals(stream.species[i]));
               if (isTrue==false)
                    return false;
          }
          for (int i=0;i<this.molarFlows.length;i++)
          {
               if ((this.molarFlows[i] != stream.molarFlows[i]) || (this.moleFractions[i] != stream.moleFractions[i]))
               {
                    return false;
               }
          }
          
          return ((this.temperature==stream.temperature) && (this.pressure==stream.pressure) && (this.totalMolarFlow==stream.totalMolarFlow));
     }
     
     
     /**
      * Overriden toString Method
      * For the species array, only the name is returned, using getName()
      * @return sends a string containing desired information
      * Puts molar flow and fraction of each species together
      */
     public String toString()
     {
          String str;
          str = "\nTotal Molar Flow= " + this.totalMolarFlow + "\nTemperature= " + this.temperature + "\nPressure= " + this.pressure;
          for (int i=0;i<this.species.length;i++)
          {
               str = str + "\nSpecies " + (i+1) + "=" + this.species[i].getName();
               str = str + "\n\tMolar Flow=" + this.molarFlows[i];
               str = str + "\n\tMol Fraction=" + this.moleFractions[i];
               
          }
          
          
          return str;
     }
     

     public Stream clone()
     {
          return new Stream(this);
     }     
} //End of class