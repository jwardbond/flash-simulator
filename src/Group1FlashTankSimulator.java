import java.util.Scanner;
import customExceptions.NotFlashable;
import java.io.*;
import java.text.DecimalFormat;


/**
 * The main method
 * */
public class Group1FlashTankSimulator {
     public static void main(String[] args) throws IOException {
          //For Scanner, IO 
          Scanner inputs = new Scanner(System.in);
          DecimalFormat numbers = new DecimalFormat("####0.000");
          ChemicalPropertiesTable givens = new ChemicalPropertiesTable();
          String[][] listOfComponents;
          PrintWriter outputs = null;
        
          //For species properties
          Species[] species;
          int numberOfSpecies = 0;
          String[] speciesNames;
          int idNumber=0;
          
          //For stream properties
          Stream inletStream = new Stream();
          double[] moleFractions;
          double totalFlow=0;
          double[] componentFlows;
          
          //For operating conditions
          double feedTemperature=0;
          double flashTemperature=0;
          double flashPressure;
          double feedPressure;
          int idealOrNot = 2; //user will set it to 1 or 0
          HeatExchanger heater = new HeatExchanger();
          VLEModel vle= new IdealModel();
          FlashModel workingCase = new CaseOne(); // Use no-argument constructor as this will be overwritten depending on the selected case
          
          //Welcome!
          System.out.println("Welcome to Group 1's flash tank simulator.");
          System.out.println("Please consult the attached CSV file regarding the species for this simulator.");
          System.out.println("If you have your own species that aren't included please add them at the bottom of the table.");
          System.out.println("Would you like to enter your data through the console? Answer 'yes' or 'no'.");
          
          //User is given the option to enter data through the console or through the .txt file
          boolean rightData = false;
          while (rightData == false)
          {
            String dataEntry = inputs.next();
            
            if(dataEntry.equals("no")) {
              System.out.println("Please enter your information in the 'ReadMe.txt' file. Save the file and close it once completed.");
              System.out.println("Instructions for order of information can be found in the associated 'ReadMe.txt' file.");
              //Essentially pauses the program so the user can enter their data if they haven't already.
              System.out.println("Once your data is entered, type 'yes' in the interactions pane.");
              boolean letsGo = false;
              while (letsGo == false) {
                String yes = inputs.next();
                if (yes.equals("yes")){
                System.out.println("Thank you."); 
                letsGo=true;
                }
                else System.out.println("Incorrect input. Type 'yes' once data is properly entered.");              
              }
             
              //Open up the file. If it's not there for some reason, the IOException will be thrown
              Scanner txtInputs = new Scanner(new File("Inputs.txt"));
          
              //Need to determine which temperature is being provided. 
              System.out.println("Are you trying to solve case 1, case 2, or case 3?");
              System.out.println("Answer with '1', 2', or '3'.");
              boolean rightCase = false;
              while (rightCase == false)
              {
                int caseNumber = inputs.nextInt();
                if ((caseNumber != 1) && (caseNumber != 2) && (caseNumber != 3))
                  System.out.println("Incorrect number entered. Try again.");
                else if (caseNumber == 1) {
                  feedTemperature = txtInputs.nextDouble();
                  System.out.println("Feed Temperature: " + feedTemperature);
                  System.out.println("Flash Temperature: " + feedTemperature); //Isothermal case for case 1
                  flashTemperature = feedTemperature;
                  rightCase = true;                  
                }
                else if (caseNumber == 2) {
                  feedTemperature = txtInputs.nextDouble();
                  System.out.println("Temperature: " + feedTemperature); //Only feed temperature is given for case 2
                  rightCase = true;                  
                }
                else if (caseNumber == 3) {
                  flashTemperature = txtInputs.nextDouble();
                  System.out.println("Temperature: " + flashTemperature); //Only flash temperature is given for case 3
                  rightCase = true;                  
                }
              }
              //Fill in more operating conditions
              feedPressure = txtInputs.nextDouble();
              System.out.println("feedPressure: " + feedPressure);
              flashPressure = txtInputs.nextDouble();
              System.out.println("flashPressure: " + flashPressure);
              numberOfSpecies = txtInputs.nextInt();
              System.out.println("Number of species: " + numberOfSpecies);
              species = new Species[numberOfSpecies];
              
              //Get species and flowrate data
              for (int i=0;i<numberOfSpecies;i++)
              {
                idNumber = txtInputs.nextInt();
                System.out.println("ID #" + (i+1) + ": " + idNumber);
                if ((idNumber == 5) || (idNumber == 11))  
                  species[i] = new VapourSpecies(givens.extractRow(idNumber+1));
                else species[i] = new LiquidSpecies(givens.extractRow(idNumber+1)); 
              }
              componentFlows = new double[numberOfSpecies];
              for (int i=0;i<numberOfSpecies;i++)
              {
                componentFlows[i]=txtInputs.nextDouble();
                System.out.println("Flow " + (i+1) + ": " + componentFlows[i]);
              }
              
              //Construct all 3 streams. Outlets are constructed as empty for the purpose of being solved
              inletStream = new Stream(species, componentFlows, feedPressure, feedTemperature);
              Stream vapourStream = new Stream();
              Stream liquidStream = new Stream();
              
              //Construct flash tank
              FlashTank flashTank = new FlashTank(heater, inletStream, vapourStream, liquidStream, flashTemperature, flashPressure);
              
              //Figure out ideal or non-ideal. 
              //Ideal uses Raoult's Law while non-ideal uses Peng-Robinson EOS
              idealOrNot = txtInputs.nextInt();
              if (idealOrNot == 1)
                vle = new IdealModel();
              else vle = new PRModel();
                           
              //Determine which case is being solved
              //In line with what user specified
              if (feedTemperature == flashTemperature) {
                workingCase = new CaseOne(flashTank, vle);
              }
              else if (flashTemperature == 0) {
                workingCase = new CaseTwo(flashTank, vle);
              }
              else if (feedTemperature == 0) {
                workingCase = new CaseThree(flashTank, vle);
              }
              rightData = true;
              txtInputs.close();
            } //End of file input
            else if (dataEntry.equals("yes")) //user enters data through the interactions pane
            {
              //First: Determine which case is being solved
              boolean correctSelection = false;
              while (correctSelection == false) {
                System.out.println("Our simulator will help you solve one of three cases.");
                System.out.println("Is your system adiabatic? Type 'yes' or 'no'.");
                String adiabaticOrNot = inputs.next();
                
                if (adiabaticOrNot.equals("yes")) {
                  boolean correctChoice = false;
                  while (correctChoice == false) {
                    System.out.println("Do you have the feed or flash temperature? Type 'feed' or 'flash'");
                    String feedOrFlash = inputs.next();
                    if (feedOrFlash.equals("feed")) {
                      //This corresponds to Case 2.
                      System.out.println("Please enter the feed temperature in K.");
                      feedTemperature = inputs.nextDouble();
                      correctChoice = true;
                    }
                    else if (feedOrFlash.equals("flash")) {
                      //This corresponds to Case 3.
                      System.out.println("Please enter the flash temperature in K.");
                      flashTemperature = inputs.nextDouble();
                      correctChoice = true;
                    }
                  }
                  
                  correctSelection = true;
                }
                
                else if (adiabaticOrNot.equals("no")) {
                  //This corresponds to Case 1.
                  System.out.println("Enter the constant operating temperature in K.");
                  
                  double isoTemp = inputs.nextDouble();
                  feedTemperature = isoTemp;
                  flashTemperature = isoTemp;
                  correctSelection = true;
                }
              }
              
              //Pressure is the only operating variable that is always defined
              System.out.println("Please enter the feed pressure in Pa.");
              feedPressure = inputs.nextDouble();
              System.out.println("Please enter the flash tank pressure in Pa. Note that it must be less than the feed pressure.");
              flashPressure = inputs.nextDouble();
              
              //Giving the user the ID of each species based on the CSV file
              listOfComponents = givens.extractIdentities();
              System.out.println("Species ID numbers and names: ");
              for (int i=0;i<listOfComponents.length;i++) {
                System.out.println(listOfComponents[i][0] + "\t" + listOfComponents[i][1]);
              }
              
              //Determine the number of species in the system
              boolean correctNumberOfSpecies = false;
              while (correctNumberOfSpecies == false) {
                System.out.println("Please enter the number of species");
                numberOfSpecies = inputs.nextInt();
                if ((numberOfSpecies > listOfComponents.length) || (numberOfSpecies <= 0)) 
                  System.out.println("Incorrect number of species entered.");
                else correctNumberOfSpecies = true;
              }
              
              //components defined based on number of species to be entered
              moleFractions = new double[numberOfSpecies];
              speciesNames = new String[numberOfSpecies];
              species = new Species[numberOfSpecies];
              
              //Determine which species are being studied
              for (int i=0;i<numberOfSpecies;i++) {
                boolean correctID = false;
                while (correctID == false) {
                  System.out.println("From the list of species above, please enter the ID of species " + (i+1));
                  idNumber = inputs.nextInt();
                  if ((idNumber <= 0) || (idNumber > listOfComponents.length))
                    System.out.println("Incorrect species ID entered.");
                  else correctID = true;
                }
                
                //Ethane (ID 5) and nitrogen (ID 11) are considered non-condensible vapours in our simulation.
                //All other compounds will enter the feed as liquids
                if ((idNumber == 5) || (idNumber == 11))  
                  species[i] = new VapourSpecies(givens.extractRow(idNumber+1));
                else species[i] = new LiquidSpecies(givens.extractRow(idNumber+1)); 
                
                speciesNames[i]=listOfComponents[idNumber+1][1]; //Get the name of the species from the list of components
              }
              
              //User can input total flows or component flows
              //If the total flow is given, then the mole fractions must also be given
              //Individual flow rates are then determined in the Stream's constructor
              int typeOfFlow=2;
              boolean properFlow = false;
              while (properFlow == false) {
                System.out.println("Do you have the total flow rate or the flow rate of each component?");
                System.out.println("Type '0' for total or '1' for components");
                typeOfFlow = inputs.nextInt();
                if (typeOfFlow == 0) {
                  double sum = 1;
                  boolean correctTotalFlow = false;
                  while (correctTotalFlow == false) {
                    System.out.println("Enter the total flow rate in mol/s:");
                    totalFlow=inputs.nextDouble();
                    if (totalFlow <= 0)
                      System.out.println("Invalid flowrate entered.");
                    else correctTotalFlow = true;
                  }
                  
                  for (int i=0;i<numberOfSpecies-1;i++) {
                    boolean correctMoleFraction = false;   
                    while (correctMoleFraction == false) {
                      System.out.println("Enter the mole fraction of " + speciesNames[i]);
                      moleFractions[i]=inputs.nextDouble();
                      if ((moleFractions[i] > 1) || (moleFractions[i] <= 0))
                        System.out.println("Incorrect mole fraction inputted.");
                      else correctMoleFraction = true;
                    }
                    sum -= moleFractions[i]; //final mole fraction is determined from the others
                  }
                  moleFractions[numberOfSpecies-1] = sum;
                  System.out.println("Final mole fraction: " + sum);
                  inletStream = new Stream(species, totalFlow, moleFractions, feedPressure, feedTemperature);
                  properFlow=true;
                }
                //Individual flows are given. Total flow and mole fractions are calculated in the Stream's constructor
                else if (typeOfFlow == 1) {
                  componentFlows = new double[numberOfSpecies];
                  for (int i=0;i<numberOfSpecies;i++) {
                    boolean correctFlow = false;
                    while (correctFlow == false) {
                      System.out.println("Enter the molar flow in mol/s of " + speciesNames[i]);
                      componentFlows[i]=inputs.nextDouble();
                      if (componentFlows[i] <=0) System.out.println("Invalid flowrate entered.");
                      else correctFlow = true;
                    }
                  }
                  inletStream = new Stream(species, componentFlows, feedPressure, feedTemperature);
                  properFlow=true;
                }   
              }
                            
              //Create empty outlet streams to be solved
              Stream vapourStream = new Stream();
              Stream liquidStream = new Stream();
                            
              //With all the givens, we can construct the reactor
              FlashTank flashTank = new FlashTank(heater, inletStream, vapourStream, liquidStream, flashTemperature, flashPressure);
                            
              //Need to determine if we have an ideal case or non-ideal case
              //Raoult's Law for ideal case, Peng-Robinson EOS for non-ideal case
              boolean rightInput = false;
              while (rightInput == false) {
                System.out.println("Are we working with ideal solutions? Type 'yes' or 'no'");
                String vlePicker = inputs.next();
                if (vlePicker.equals("yes")) {
                  vle = new IdealModel();
                  idealOrNot = 1;
                  rightInput = true;
                }
                else if (vlePicker.equals("no")) {
                  vle = new PRModel();
                  idealOrNot = 0;
                  rightInput = true;
                }
              }
              
              //Now we must determine which case we are working with
              if (feedTemperature == flashTemperature) {
                workingCase = new CaseOne(flashTank, vle);
              }
              else if (flashTemperature == 0) {
                workingCase = new CaseTwo(flashTank, vle);
              }
              else if (feedTemperature == 0) {
                workingCase = new CaseThree(flashTank, vle);
              }
              
              rightData = true;
            }
            
          }//End of user input
          
          //Solve the system
          //Prints to the interactions pane and to the .txt file
          //If the system can't be solved, neither action is performed
            try 
          {
            outputs = new PrintWriter(new FileOutputStream("SolvedSystem.txt"));
          }
          catch (FileNotFoundException e)
          {
            System.out.println("Error opening output file.");
          }
         
          
          FlashTank solved = null;
          String fileName = null;
          //Ask the user if they want to name the results file
          //If not, a generic name will be given
          try {
            solved = workingCase.solveSystem();
            System.out.println("Would you like to name the file of your simulation?");
            System.out.println("Type 'yes' or 'no'");
            boolean yesOrNo = false;
            while (yesOrNo == false){
              String option = inputs.next();
              if (option.equals("yes")){
                System.out.println("Enter the file name with .txt at the end.");
                fileName = inputs.next();
                yesOrNo=true;
              }
              else if (option.equals("no")) {
                fileName = "SolvedSystem.txt";
                yesOrNo=true;
              }
              else System.out.println("Invalid answer. Try again.");                                        
            }
            System.out.println(solved);
             try {
                  outputs = new PrintWriter(new FileOutputStream(fileName));
                }
                catch (FileNotFoundException e)
                {
                  System.out.println("Error opening output file.");
                }
            
            if (idealOrNot == 1)
              outputs.println("Solved using ideal solutions");
            else if (idealOrNot == 0)
              outputs.println("solved using non-ideal solutions");
            outputs.println("");
            outputs.println("Q = " + numbers.format(solved.getFlashTankHeatExchanger().getQ()) + " J/s");
            outputs.println("");
            outputs.println("INLET");
            outputs.println("");
            outputs.println("Total Molar Flow: " + numbers.format(solved.getFeedStream().getTotalMolarFlow()) + " mol/s");
            outputs.println("Temperature: " + numbers.format(solved.getFeedStream().getTemperature()) + " K");
            outputs.println("Pressure: " + solved.getFeedStream().getPressure() + " Pa");
            for (int i=0;i<solved.getFeedStream().getSpecies().length;i++)
            {
              outputs.println("Species " + (i+1) + " = " + solved.getFeedStream().getSpecies()[i].getName());
              outputs.println("\tMolar Flow = " + numbers.format(solved.getFeedStream().getMolarFlows()[i]) + " mol/s");
              outputs.println("\tMole Fraction = " + numbers.format(solved.getFeedStream().getMoleFractions()[i]));
            }
            outputs.println("");
            outputs.println("VAPOUR OUTLET");
            outputs.println("");
            outputs.println("Total Molar Flow: " + numbers.format(solved.getVapourStream().getTotalMolarFlow()) + " mol/s");
            outputs.println("Temperature: " + numbers.format(solved.getVapourStream().getTemperature()) + " K");
            outputs.println("Pressure: " + solved.getVapourStream().getPressure() + " Pa");
            for (int i=0;i<solved.getFeedStream().getSpecies().length;i++)
            {
              outputs.println("Species " + (i+1) + " = " + solved.getVapourStream().getSpecies()[i].getName());
              outputs.println("\tMolar Flow = " + numbers.format(solved.getVapourStream().getMolarFlows()[i]) + " mol/s");
              outputs.println("\tMole Fraction = " + numbers.format(solved.getVapourStream().getMoleFractions()[i]));
            }
            outputs.println("");
            outputs.println("LIQUID OUTLET");
            outputs.println("");
            outputs.println("Total Molar Flow: " + numbers.format(solved.getLiquidStream().getTotalMolarFlow()) + " mol/s");
            outputs.println("Temperature: " + numbers.format(solved.getLiquidStream().getTemperature()) + " K");
            outputs.println("Pressure: " + numbers.format(solved.getLiquidStream().getPressure()) + " Pa");
            for (int i=0;i<solved.getFeedStream().getSpecies().length;i++)
            {
              outputs.println("Species " + (i+1) + " = " + solved.getLiquidStream().getSpecies()[i].getName());
              outputs.println("\tMolar Flow = " + numbers.format(solved.getLiquidStream().getMolarFlows()[i]) + " mol/s");
              outputs.println("\tMole Fraction = " + numbers.format(solved.getLiquidStream().getMoleFractions()[i]));
            }
            System.out.println("The solved system has been saved to the file " + "'" + fileName + "'.");
            System.out.println("Thank you!");
           
          }
          catch (NotFlashable e) {
            System.out.println(e.getMessage());
          }
          finally { 
            inputs.close();
            outputs.close();
          }

     }//End of main
     
}//End of class