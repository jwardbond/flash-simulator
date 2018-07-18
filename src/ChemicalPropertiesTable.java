/**
 * A table for chemical properties with unique methods for getting units and species names.
 */
public class ChemicalPropertiesTable extends DataTableIO 
{
     
//---------------------------------------------------CONSTRUCTORS-----------------------------------------------------
     
     
     /**
      * Prompts the user to select a CSV file containing the data table.
      */
     public ChemicalPropertiesTable()
     {
          super();
     }
     
     
     /**
      * Reads in the table at the path provided.
      * @param path a String containing the absolute path of the data file
      */
     public ChemicalPropertiesTable(String path)
     {
          super(path);
     }
//----------------------------------------------------METHODS--------------------------------------------------------
     
     
     /**
      * Gets the first two rows of the table (Headings and units) and returns them as columns.
      * It's better to return a vertical array as it prints to the console much nicer.
      * @return a 2D array containing the headings and their associated units as columns
      */
     public String[][] extractHeadings()
     {
          String[][] headingsRow = new String[2][extractRow(0).length];
          
          //Get the first two rows of the table
          headingsRow[0] = extractRow(0); //the headings
          headingsRow[1] = extractRow(1); //the units
          
          //Transpose the array
          String[][] transposed = new String[headingsRow[0].length][headingsRow.length]; //makes number of rows in new array = no columns in the old
          
          for(int i=0; i<headingsRow[0].length; i++)
          {
               for(int j=0; j<2; j++)
               {
                    transposed[i][j] = headingsRow[j][i];
               }
          }
          
          return transposed;
     }
     
     
     /**
      * Gets the species name and ID number columns and returns them as columns.
      * It's better to return a vertical array as it prints to the console much nicer.
      * @return a 2D array containing the species name and ID number
      */
     public String[][] extractIdentities()
     {
          //Get the two leftmost columns from the data table as arrays
          String[][] columnsAsRows = new String[2][extractColumn(0).length];
          columnsAsRows[0] = extractColumn(0);
          columnsAsRows[1] = extractColumn(1);
          
          //Transpose the array
          String[][] transposed = new String[columnsAsRows[0].length][columnsAsRows.length]; //makes number of rows in new array = no columns in the old
          
          for(int i=0; i<columnsAsRows[0].length; i++)
          {
               for(int j=0; j<2; j++)
               {
                    transposed[i][j] = columnsAsRows[j][i];
               }
          }
          
          return transposed;
     }

     
//--------------------------------------------------TEST-------------------------------------------------------------
//     public static void main(String[] args)
//     {
//          ChemicalPropertiesTable chemTable = new ChemicalPropertiesTable();
//          
//          String[][] toPrint = chemTable.extractIdentities();
//          for(String[] row : toPrint)
//          {
//               for(String element : row)
//               {
//                    System.out.printf("%12.12s\t", element);
//               }
//               System.out.println("");
//          }
//     }


}//End of ChemicalPropertiesTable class
