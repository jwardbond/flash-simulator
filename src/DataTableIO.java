import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;

import customExceptions.*; 

/**
 * Imports a comma-delimited txt file to read data and to add new data.
 * The values in the table cannot be changed but new rows can be added, in which case the original file is also appended. 
 * Note: Exception is commented out
 * @version 1.2 2017/10/31
 */
public class DataTableIO 
{
     
     
     private String[][] tableString;
     private String path;
     
     
//-------------------------------------CONSTRUCTORS----------------------------------------------------------
     
     
     /**
      * Prompts the user to select a .txt file containig a comma-delimited table.
      * Counts the number of rows and headings, and turns the table into a 2D array of strings.
      * */
     public DataTableIO()
     {
          System.out.println("Reading file...");
             
          //Must be initialized outside a codeblock
          tableString = null;
          Scanner inputScanner = null;
          Scanner scanNumOfLines = null;
               

          try { //reading in the file automatically
               inputScanner = new Scanner(new FileInputStream("DataSIUnits.csv"));
               scanNumOfLines = new Scanner(new FileInputStream("DataSIUnits.csv"));
               this.path = "DataSIUnits.csv";
          }
          catch(FileNotFoundException e) {
               System.out.println("DataSIUnits.csv not found in default directory. Please navigate to it");
               try {//opening a dialogue box                                                                                            //shouldn't throw this exception but it needs to be handled to compile
                    //Build dialogue box
                    JFileChooser chooser = new JFileChooser();
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(".csv or .txt files", "txt", "csv");      //will only display txt and csv files
                    chooser.setFileFilter(filter);
                    //Display DB
                    int isApproved = chooser.showOpenDialog(null); 
                    //Init scanner
                    inputScanner = new Scanner(new FileInputStream(chooser.getSelectedFile()));
                    scanNumOfLines = new Scanner(new FileInputStream(chooser.getSelectedFile()));
                    this.path = chooser.getSelectedFile().getAbsolutePath(); 
               }
               catch(FileNotFoundException f) {
                    System.out.println("The file you selected wasn't found.\nHonestly congrats.\nI don't how this happened");
                    System.exit(0);
               }
          }
          
                                                        
 
          /*
           * For the following, the scanNumOfLines is used.
           * Scanner can't reset it's position... 
           * We need the other scanner object to actually read the table.
           */
          String[] headingStringArray = scanNumOfLines.nextLine().split(",");     //Split the first row into headings       
          int numRows, numColumns;
          
          numColumns = headingStringArray.length;                                   
          numRows = 1;                                                       //The heading row has already been used
          while(scanNumOfLines.hasNextLine())
          {
               numRows++;
               scanNumOfLines.nextLine();
          }
          scanNumOfLines.close();
          
          /*
           * Now the other scanner object can be used.
           * We can create the data table from a tab or comma delimited file.
           */
          this.tableString = new String[numRows][numColumns];
          
          inputScanner.useDelimiter(",|\\n");  
          for(int i=0; i<this.tableString.length; i++)
          {
               for(int j=0; j<this.tableString[i].length; j++)
               {
                    this.tableString[i][j] = inputScanner.next();
               }
          }
          inputScanner.close();
          
     }//End of constructor
     
     
     /**
      * Takes in the path name for the CSV file and creates a string array containing the data
      * @param path a string for the absolute path to the data file
      *
      */
     public DataTableIO(String path)
     {
          System.out.println("Reading file...");
          
          this.path = path;
                  
          //Must be initialized outside a codeblock
          tableString = null;
          Scanner inputScanner = null;
          Scanner scanNumOfLines = null;
               
          //Designate the input stream and catch errors
          try
          {
               inputScanner = new Scanner(new FileInputStream(path));
               scanNumOfLines = new Scanner(new FileInputStream(path));  
          }
          catch(FileNotFoundException e)
          {
               System.out.println("No accessible file @" + path + " found.");
               System.exit(0);
          }
          
          /*
           * For the following, the scanNumOfLines is used.
           * Scanner can't reset it's position... 
           * We need the other scanner object to actually read the table.
           */
          String[] headingStringArray = scanNumOfLines.nextLine().split(",");     //Split the first row into headings       
          int numRows, numColumns;
          
          numColumns = headingStringArray.length;                                   
          numRows = 1;                                                       //The heading row has already been used
          while(scanNumOfLines.hasNextLine())
          {
               numRows++;
               scanNumOfLines.nextLine();
          }
          scanNumOfLines.close();
          
          /*
           * Now the other scanner object can be used.
           * We can create the data table from a tab or comma delimited file.
           */
          this.tableString = new String[numRows][numColumns];
          
          inputScanner.useDelimiter(",|\\n");  
          for(int i=0; i<this.tableString.length; i++)
          {
               for(int j=0; j<this.tableString[i].length; j++)
               {
                    this.tableString[i][j] = inputScanner.next();
               }
          }
          inputScanner.close();
          
     }//End of constructor
     
     
//------------------------------------------------------METHODS--------------------------------------------------------
     
     
     /**
      * Prints the table of strings stored in DataTableIO to the console
      */
     public void printTable()
     {
          for(int i=0; i<this.tableString.length; i++)
          {
               for(int j=0; j<this.tableString[i].length; j++)
               {
                    System.out.printf("%-5.5s\t", this.tableString[i][j]);
               }
               System.out.println();
          }
     }//End of printTable method
     
     
     /**
      * Adds a new row to the bottom of an existing 2-dimensional table of Strings.
      * @param newRow an array of strings to be added to the bottom of the table
      * @throws IncorrectArraySize if the number of columns in the new row doesn't match the number of columns in the old table.
      */
     public void appendTable(String[] newRow) throws IncorrectArraySize
     {
         //Throw error if the array doesn't have the right amount of columns
          if(newRow.length != this.tableString[0].length)
               throw new IncorrectArraySize("Check the number of columns in the array you entered." +
                                            "\n It should match the number of columns in the table");
                    
          //Deep copy the old table to the placeholder (copying a smaller table into a bigger one)
          String[][] newTableString = new String[this.tableString.length + 1][this.tableString[0].length];
          for(int i=0; i<this.tableString.length; i++)
          {
               //System.out.println(tableString.length + " " + tableString[1].length +"\t" + this.numRows + " " + this.numColumns);
               for(int j=0; j<this.tableString[i].length; j++)
               {
                    newTableString[i][j] = this.tableString[i][j];
               }
          }
          
          //Add the new row as the bottom row of the format is table[finalRow].length
          for(int j=0; j<newTableString[newTableString.length - 1].length; j++)
               newTableString[newTableString.length - 1][j] = newRow[j];
          
          //Re-assign the instance variables. It has already been deep copied, there will be privacy leaks.
          this.tableString = newTableString;
          
          //Open the table csv @path
          PrintWriter tableWriter = null;
          try
          {
               System.out.println("Searching for file...");
               tableWriter = new PrintWriter(this.path);   //Should overwrite the existing file
          }
          catch(FileNotFoundException e)
          {
               System.out.println("No file found @" + this.path);
               System.exit(0);
          }
          
          //Print the table in csv
          System.out.println("Writing new table to " + this.path);
          for(int x=0; x<tableString.length; x++)
          {
               for(int y=0; y<tableString[x].length; y++)
               {
                    tableWriter.print(tableString[x][y]);  
                    if(y == (tableString[x].length -1))       // If it is the last element, print a new line
                         tableWriter.println();
                    else
                         tableWriter.print(",");              //Otherwise print a comma
               }
          }
          tableWriter.close();
          
     }//End of appendTable
     
     
     /**
      * Gets a row from the table corresponding to a row index number.
      * @param rowNum the row number
      * @return a string array corresponding to the desired table row
      */
     public String[] extractRow(int rowNum)
     {
       String[] row = new String[this.tableString[0].length];
       for (int i=0;i<this.tableString[0].length;i++)
       {
         row[i] = this.tableString[rowNum][i];
       }
         return row;
     }
     
     
     /**
      * Gets a column from the table corresponding to a column index number.
      * @param columnNum the column number (starting at 1)
      * @return a String array containing the contents of that column
      */
     public String[] extractColumn(int columnNum)
     {
          
          String[] column = new String[this.tableString.length];
          
          for(int i=0; i<this.tableString.length; i++)
               column[i] = this.tableString[i][columnNum];
          
          return column;
     }
 
     
//-----------------------------------------------HOUSEKEEPING METHODS--------------------------------------------------
     
     
     /**
      * Returns a the held table in a 2D array of Strings.
      * @return A 2D array of Strings containing the data in the imported table.
      */
     public String[][] getTableString()
     {
          
          String[][] toReturn = new String[this.tableString.length][this.tableString[0].length];
          
          for(int i=0; i<this.tableString.length; i++)
          {
               for(int j=0; j<this.tableString[i].length; j++)
               {
                    toReturn[i][j] = this.tableString[i][j];
               }
          }
          
     return toReturn;
     }//End of getTableString
     
     
     /**
      * Returns the absolute path of the imported table.
      * @return A string representing the absolute path of the imported table
      */
     public String getPath()
     {
          return this.path;
     }
     
     
}//End of DataTableIO