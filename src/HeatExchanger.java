/**
 * Very simple heat exchanger with a Q value in J/s.
 */
public class HeatExchanger implements Cloneable
{
     
     
     private double Q;


//----------------------------------------CONSTRUCTORS----------------------------------------------------------------
     
     /**
      * Simple Heat Exchanger.
      * No Q is set as it will be calculated later. 
      */
     public HeatExchanger() {
          this.Q = 0;
     }
     
     
     /**
      * Simple Heat Exchanger.
      * @param Q the heat supplied/removed
      */
     public HeatExchanger(double Q) {
          this.Q = Q;
     }
     
     
     /**
      * The copy constructor
      * @param heatExchanger The object to be copied.
      */
     public HeatExchanger(HeatExchanger heatExchanger) {
          this.Q = heatExchanger.Q;
     }
          
     
//----------------------------------------HOUSEKEEPING METHODS--------------------------------------------------------
     
     
     public double getQ() {
          return this.Q;
     }
     public void setQ(double Q) {
          this.Q = Q;
     }
     public HeatExchanger clone() {
          return new HeatExchanger(this);
     }
    
}