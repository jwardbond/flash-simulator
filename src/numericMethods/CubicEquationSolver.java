package numericMethods;
import customExceptions.NotFlashable;

/**
   * Solves a cubic equation of form:
   * x^3 + a*x^2 + b*x + c = 0
   * Used for the PR EOS to find compressibility factor Z
   * */
public class CubicEquationSolver
{
  

  /**
   * Solves a cubic equation of form:
   * x^3 + a*x^2 + b*x + c = 0
   * @return the minimum root and the maximum root (of 3 roots).
   * */
  public double[] solveCubicEquation(double[] constants) throws NotFlashable 
  {
    if (constants.length != 3) {
      System.out.println("Wrong number of constants.");
      System.exit(0);
    }
    double[] roots;
    double a = constants[0];
    double b = constants[1];
    double c = constants[2];
    
    
    double Q = (Math.pow(a,2)-3*b)/9;
    double R = (2*Math.pow(a,3)-9*a*b+27*c)/54;
    
    double M = Math.pow(R, 2) - Math.pow(Q, 3);
    
    if(M<0) {
      roots = new double[2];
      double theta = Math.acos(R/Math.sqrt(Math.pow(Q, 3)));
      
      double x1 = -(2*Math.sqrt(Q)*Math.cos(theta/3))-a/3;
      double x2 = -(2*Math.sqrt(Q)*Math.cos((theta+2*Math.PI)/3))-a/3;
      double x3 = -(2*Math.sqrt(Q)*Math.cos((theta-2*Math.PI)/3))-a/3;
      double minX = Math.min(x1,x2); //Only the smallest and largest roots are needed to find Z.
      roots[0] = Math.min(minX,x3);
      double maxX = Math.max(x1,x2);
      roots[1] = Math.max(maxX,x3);
      
      return roots;
      
    }
    else
    {
         throw new NotFlashable("System not solved...\nOnly one root found for the cubic EoS.");
    }    
  }  
}