package customExceptions;

public class BadFunction extends Exception
{
      
     private String msg;
     
     public BadFunction (String msg)
     {
          this.msg = msg;
     }
     
     public BadFunction()
     {
          this.msg = "The function you are using for rootfinder isn't going to work!";
     }
     
     public String getMsg()
     { 
          return this.msg;
     }
     
     public void setMsg(String msg)
     {
          this.msg = msg;
     }
}