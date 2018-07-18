package numericMethods;
import customExceptions.NoRootFound;
import customExceptions.BadFunction;

/**
 * Finds the solution to any object implementing HasRoot using: Bisection or Ridder's method
 * */
public class RootFinder
{
     /**
      * Solves the roots of findYGivenX that sets Y to Ytarget.
      */
     public static double bisectionRoot(double xmin , double xmax, double yTarget, double tol, long maxIt, HasRoot e) throws BadFunction
     {
          double a, b, m, y_m, y_a,y_b,xDiff;
          
          a=Math.min(xmin,xmax);
          b=Math.max(xmin,xmax);
          m=0.0;//just to initialize
          
          xDiff=b-a;
          long iter=0;
          while ( xDiff > tol&& iter<maxIt)
          {
               m = (a+b)/2;           // Mid point
                              
               y_a = e.findYGivenX(a)-yTarget;       // y_a = f(a)
               y_m = e.findYGivenX(m)-yTarget;       // y_m = f(m)
               y_b = e.findYGivenX(b)-yTarget;       // y_b = f(b)
               
               if ( (y_m > 0 && y_a < 0) || (y_m < 0 && y_a > 0) )
               {
                    b = m;
                    // f(a) and f(m) have different signs: move b
               }
               
               else
               {
                    if((y_m > 0 && y_b > 0) || (y_m < 0 && y_b < 0))
                    {
                         System.out.println("ya, ym, yb all have the same sign. Returning 0");
                         return 0;
                    }
                    
                    // f(a) and f(m) have same signs: move a
                    
                    a = m;
                    
               }
               xDiff=Math.abs(b-a);
               iter++;  
          }
     return m;
     }//End of bisection method
     
     public static double ridderRoot(double xMin , double xMax, double yTarget, double tolerance, int maxIt, HasRoot e) throws BadFunction { 
          double xL=Math.min(xMin,xMax);
          double xU=Math.max(xMin,xMax);
          double xM=(xL+xU)/2;
          int sgn=0;
          double xR=0;
          
          double f_xL;
          double f_xU;
          double f_xM;
          double f_xR;
          
          double epsilon_a=xU-xL;
          int iter=0;
          
          double[] solution=new double[maxIt];
          
          while(epsilon_a>tolerance && iter<maxIt)
          {
                                   
               f_xL=e.findYGivenX(xL)-yTarget;
               f_xU=e.findYGivenX(xU)-yTarget;
               f_xM=e.findYGivenX(xM)-yTarget;
                            
               //find xR
               if((Math.pow(f_xM, 2)-f_xL*f_xU<0))
                    throw new BadFunction("There are no real roots");
               
               xR = xM + (xM-xL) * (Math.signum(f_xL-f_xU)*f_xM / Math.sqrt(Math.pow(f_xM, 2)-f_xL*f_xU));
               f_xR=e.findYGivenX(xR)-yTarget;
               solution[iter]=xR;
               
               if(xR<xM) {
                    if((f_xL * f_xR)<0) {
                         xU = xR;
                    }
                    else if((f_xR * f_xM)<0) {
                         xL = xR;
                         xU = xM;
                    }
                    else if((f_xM * f_xU)<0) {
                         xL = xM;
                    }
                    else {
                         
                    }
               }
               
               if(xR>xM) {
                    if((f_xL * f_xM)<0) {
                         xU = xM;
                    }
                    else if((f_xM * f_xR)<0) {
                         xL = xM; 
                         xU = xR;
                    }
                    else if((f_xR * f_xU)<0) {
                         xL = xR;
                    }
                    else {
                         System.out.println("Wow ridders nailed the solution");
                         return xR; 
                    }
               }
               
               xM=(xL+xU)/2;      

               if(iter>0) {//calculate the error
                    epsilon_a=Math.abs((solution[iter]-solution[iter-1])/solution[iter]);
               }
               
               iter ++;
          }
          
          System.out.println("... solved after "+iter+" iterations...");
          
          if(epsilon_a>tolerance)
               System.out.println("Maximum number of iterations reached before convergence");
          
          return xR;
     }//end of Ridder's method
     
    
     
     public static double Newton(double x_0, double tol, long maxIt, NonlinearEquation e) throws BadFunction
     {
       double x_old=x_0;
       double x_new;
       double x_diff;
       long iter=0;
       
       do
       {
         x_new=x_old-e.returnValue(x_old)/e.returnDerivative(x_old);
         x_diff=Math.abs(x_old-x_new);
         x_old=x_new;
         
         iter++;
       } while(x_diff>tol && iter<maxIt);
       
       if(x_diff>tol) System.out.println("Maximum number of iterations reached before convergence");
       
       return x_new;
     }
}//end of bisection class