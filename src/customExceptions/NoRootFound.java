package customExceptions;

public class NoRootFound extends Exception{ 
     
     private String msg;
     
     public NoRootFound(String msg) {
          this.msg = msg;
     }
     
     public NoRootFound() {
          this.msg = "No Root Found";
     }
     
     public String getMsg() { 
          return this.msg;
     }
     
     public void setMsg(String msg) {
          this.msg = msg;
     }
}