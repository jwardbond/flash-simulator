Welcome to group 1's flash tank simulator.

Our simulator is designed to solve three cases seen in flash tank separation. 
To run the simulation, open the 'Group1FlashTankSimulator.java' file.
The main method will allow you to enter information through the interactions pane or through a .txt file.
If a simulation is successful, it will be saved in its own .txt file. 
This file will be found in the same directory as the main method. 

If changing any information externally from the main method, please read the following:


***IF YOU NEED TO ADD MORE SPECIES TO THE .csv FILE***
Please see the file named 'DataSIUnits.csv'
To enter a new species, simply add it to the bottom of the list by following the pattern established 
by the other species.


***IF YOU WISH TO INPUT INFORMATION VIA THE .txt FILE***
In the file named "Inputs.txt" please input the given information in the order displayed below.
Please make sure that at the end of each value there are no extra spaces.

temperature(K)(double)
inletPressure(Pa)(double)
operatingPressure(Pa)(double)
numberOfSpecies(int)
idSpecies1(int)
idSpecies2(int)
idSpeciesN(int)
flowRateSpecies1(mol/s)(double)
flowRateSpecies2(mol/s)(double)
flowRateSpeciesN(mol/s)(double)
1forIdeal0forNonideal(int)

An example:

338.15
200000.0
100000.0
2
4
12
70.0
30.0
0

Thank you.