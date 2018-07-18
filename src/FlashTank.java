/**
 * This class represents a flash distillation vessel and contains three stream objects for the inflow and outflows.
 * Each stream is composed of an array of species. The outlet streams should usually be the same temperature as the flash tank.
 * Notes: When flash tank temp is set, outlet stream temps are also set
  */
public class FlashTank implements Cloneable
{
    //  INSTANCE VARIABLES

    private HeatExchanger flashTankHeatExchanger;
    private Stream feedStream;
    private Stream vapourStream;
    private Stream liquidStream;

    private double flashTemp;
    private double pressure;
    
    private String name;
   
    
//-------------------------------------------------CONSTRUCTORS--------------------------------------------------------
    
    /**
      * Builds a flash tank out of its necessary components.
      * @param flashTankHeatExchanger The simple heat exchanger. Used to store the value of Q.
      * @param feedStream The inlet of the flash tank. Contains known species and compositions.
      * @param vapourStream The outlet vapour stream. For now it will be empty, but it will be solved in downstream methods. 
      * @param liquidStream The outlet liquid stream. For now it will be empty, but it will be solved in downstream methods. 
      * @param flashTemp The temperature of the flash tank. For case 1 and 3 it is known; for case 2 it will be solved for. 
      * @param pressure The operating pressure of the flash tank
      */
    public FlashTank(HeatExchanger flashTankHeatExchanger, Stream feedStream, Stream vapourStream, Stream liquidStream, double flashTemp, double pressure)
    {
         
      this.flashTankHeatExchanger = flashTankHeatExchanger.clone();
      
      this.feedStream = feedStream.clone();
      this.vapourStream = vapourStream.clone();
      this.liquidStream = liquidStream.clone();
      
      this.flashTemp = flashTemp;
      this.pressure = pressure;
      
      this.name = "NoName";
        
    }
    
    
    /**
      * Same constructor as above but also has a name of the flash tank. 
      * @param flashTankHeatExchanger The simple heat exchanger. Used to store the value of Q.
      * @param feedStream The inlet of the flash tank. Contains known species and compositions.
      * @param vapourStream The outlet vapour stream. For now it will be empty, but it will be solved in downstream methods. 
      * @param liquidStream The outlet liquid stream. For now it will be empty, but it will be solved in downstream methods. 
      * @param flashTemp The temperature of the flash tank. For case 1 and 3 it is known; for case 2 it will be solved for. 
      * @param pressure The operating pressure of the flash tank.
      * @param name The name of the flash tank.
      */
        public FlashTank(HeatExchanger flashTankHeatExchanger, Stream feedStream, Stream vapourStream, Stream liquidStream, double flashTemp, double pressure, String name)
    {
         
      this.flashTankHeatExchanger = flashTankHeatExchanger.clone();
      
      this.feedStream = feedStream;
      this.vapourStream = vapourStream;
      this.liquidStream = liquidStream;
      
      this.flashTemp = flashTemp;
      this.pressure = pressure; 
      
      this.name = name;
        
    }

        
    /**
     * The copy constructor.
     * @param toCopy The object that is copied.
     */
    public FlashTank(FlashTank toCopy)
    {
         
      this.flashTankHeatExchanger = toCopy.flashTankHeatExchanger.clone();
      
      this.feedStream = toCopy.feedStream.clone();
      this.vapourStream = toCopy.vapourStream.clone();
      this.liquidStream = toCopy.liquidStream.clone();
      
      this.flashTemp = toCopy.flashTemp;
      this.pressure = toCopy.pressure;
      
      this.name = toCopy.name;      
    }//end of copy constructor
    
   
//---------------------------------------------------HOUSEKEEPING METHODS----------------------------------------------

    
    public Stream getVapourStream() {
         return this.vapourStream.clone();
    }
    public Stream getLiquidStream() {
        return this.liquidStream.clone();
    }
    public Stream getFeedStream() {
         return this.feedStream.clone();
    }
    public String getName() {
        return this.name;
    }
    public double getFlashTemp() {
         return this.flashTemp;
    }
    public double getPressure() {
        return this.pressure;
    }
    public HeatExchanger getFlashTankHeatExchanger() {
         return this.flashTankHeatExchanger.clone();
    }
    
    public void setFeedStream(Stream feedStream) {
        this.feedStream = feedStream.clone();
    }
    
    public void setVapourStream(Stream vapourStream) {
         this.vapourStream = vapourStream.clone();
    }
    
    public void setLiquidStream(Stream liquidStream) {
         this.liquidStream = liquidStream.clone();
    }
    
    public void setFlashTankHeatExchanger(HeatExchanger flashTankHeatExchanger) {
        this.flashTankHeatExchanger = flashTankHeatExchanger.clone();
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Also changes the temperatures of the outlet streams
     * @param flashTemp The updated flash temperature
     */
    public void setFlashTemp(double flashTemp) {
         this.flashTemp = flashTemp;
         
         //needs to set the outlet streams to the flash temp as well
         this.vapourStream.setTemperature(flashTemp);
         this.liquidStream.setTemperature(flashTemp);
    }
    
    public void setPressure(double pressure) {
        this.pressure = pressure;
    }
    
    public FlashTank clone()
    {
         return new FlashTank(this);
    }
    
    public String toString() {
         return ("Q= "+this.flashTankHeatExchanger.getQ() +
                 "\n\nINLET\t" + this.feedStream.toString() +
                 "\n\nVAPOUR OUTLET\t" + this.vapourStream.toString() +
                 "\n\nLIQUID OUTLET\t" + this.liquidStream.toString());                 
    }

    
//---------------------------------------------DEEP HOUSEKEEPING METHODS---------------------------------------------
    
    
    /**
     * Sets the molar flow rate of the vapour stream outlet.
     * @param totalMolarFlow the molar flow rate of the vapour outlet in mol/s
     */
    public void setVapourStreamFlowRate(double totalMolarFlow){
         this.vapourStream.setTotalMolarFlow(totalMolarFlow);
    }
    
    /**
     * Sets the molar flow rate of the liquid outlet stream.
     * @param totalMolarFlow the molar flow rate of the liquid outlet in mol/s
     */
    public void setLiquidStreamFlowRate(double totalMolarFlow){
         this.liquidStream.setTotalMolarFlow(totalMolarFlow);
    }
    
    /**
     * Sets the mole fractions in the vapour outlet.
     * The deep copy is done in the Stream class.
     * @param yiArray an array of yi values
     */
    public void setVapourStreamMoleFractions(double[] yiArray) {
         this.vapourStream.setMoleFractions(yiArray);
    }
    
    /**
     * Sets the mole fractions in the liquid outlet.
     * The deep copy is done in the Stream class.
     * @param xiArray an array of xi values
     */
    public void setLiquidStreamMoleFractions(double[] xiArray) {
         this.liquidStream.setMoleFractions(xiArray);
    }
    
    /**
     * Sets the feed stream temperature, used only at the end of case 3
     * @param temperature temperature in K
     */
    public void setFeedStreamTemperature(double temperature) {
         this.feedStream.setTemperature(temperature);
    }
    
    /**
     * Sets the temperatures of both outlet streams
     * @param temperature temperature in K
     */
    public void setOutletTemperatures(double temperature) {
         this.vapourStream.setTemperature(temperature);
         this.liquidStream.setTemperature(temperature);
    }
    
    /**
     * Sets the pressure of both outlet streams
     * @param pressures Pressure in Pa
     */
    public void setOutletPressures(double pressures) {
         this.vapourStream.setPressure(pressure);
         this.vapourStream.setPressure(pressure);
    }
        
    /**
     * Sets the Q value for the heat exchanger.
     * @param Q the Q value in J/s
     */
    public void setHeatExchangerQ(double Q) {
         this.flashTankHeatExchanger.setQ(Q);
    }
}