package numericMethods;

import java.lang.Math;

public class test implements HasRoot
{
 public double function(double x)
  {
    double y=5*(0.07-Math.exp(-0.04*x))+2*Math.exp(-0.04*x);
    
    return y;
  }//end of efficiency calculation
  
  public double calcRoot(double y)
  {
    double xMin=53;
    double xMax=54;
    double tolerance=0.0001;
    int maxIt=1000;
    
    //already works
    //return RootFinder.bisectionRoot(this, y, xMin, xMax);
    
    //testing with Ridder's method
    return RootFinder.ridderRoot(xMin, xMax, y, tolerance, maxIt, this);
      
    //has to match the bisection method in RootFinder
  }//end of root finder method
  
  public double findYGivenX(double x)
  {
    double root=this.function(x);
    
    return root;
  }//end of root finder method  
}//end of class