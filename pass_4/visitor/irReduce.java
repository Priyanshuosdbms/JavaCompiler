//
// Generated by JTB 1.3.2
//

package visitor;
import syntaxtree.*;
import java.util.*;

public class irReduce extends GJNoArguDepthFirst<String> {
   public int currTemp = 0;
   String pretext = "";
   boolean isfndef = false;

   public void appendPretext(String s){
	   pretext += s;
   }
   
   public String extractPretext(){
	   String temp = pretext;
	   pretext = "";
	   return temp;
   }
   
   public String getNewTemp(){
	   currTemp += 1;
	   return "TEMP " + Integer.toString(currTemp-1) + " ";
   }
   
   public String visit(NodeList n) {
      String _ret="";
      int _count=0;
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
         _ret += e.nextElement().accept(this);
         _ret += "\n";
         _count++;
      }
      return _ret;
   }

   public String visit(NodeListOptional n) {
      if ( n.present() ) {
         String _ret="";
         int _count=0;
         for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            _ret += e.nextElement().accept(this);
            _ret += "\n";
            _count++;
         }
         return _ret;
      }
      else
         return "";
   }

   public String visit(NodeOptional n) {
      if ( n.present() )
         // ??
         return n.node.accept(this);
    	 // return ((Label)n.node).f0.tokenImage + " ";
      else
         return "";
   }

   public String visit(NodeSequence n) {
      String _ret="";
      int _count=0;
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
         _ret += e.nextElement().accept(this);
         _ret += "\n";
         _count++;
      }
      return _ret;
   }

   public String visit(NodeToken n) { return ""; }

   //
   // User-generated visitor methods below
   //

   /**
    * f0 -> "MAIN"
    * f1 -> StmtList()
    * f2 -> "END"
    * f3 -> ( Procedure() )*
    * f4 -> <EOF>
    */
   public String visit(Goal n) {
      String _ret="";
      _ret += "MAIN\n";
      _ret += n.f1.accept(this);
      _ret += "\nEND\n\n";
      _ret += n.f3.accept(this);
      System.out.println(_ret);
      return _ret;
   }

   /**
    * f0 -> ( ( Label() )? Stmt() )*
    */
   public String visit(StmtList n) {
      return n.f0.accept(this);
   }

   /**
    * f0 -> Label()
    * f1 -> "["
    * f2 -> IntegerLiteral()
    * f3 -> "]"
    * f4 -> StmtExp()
    */
   public String visit(Procedure n) {
      String _ret="";
      _ret += n.f0.accept(this);
      _ret += "[";
      _ret += n.f2.accept(this);
      _ret += "]\n";
      isfndef = true;
      _ret += n.f4.accept(this);
      isfndef = false;
      _ret += "\n";
      return _ret;
   }

   /**
    * f0 -> NoOpStmt()
    *       | ErrorStmt()
    *       | CJumpStmt()
    *       | JumpStmt()
    *       | HStoreStmt()
    *       | HLoadStmt()
    *       | MoveStmt()
    *       | PrintStmt()
    */
   public String visit(Stmt n) {
	   return n.f0.accept(this);
   }

   /**
    * f0 -> "NOOP"
    */
   public String visit(NoOpStmt n) {
	   return "NOOP\n";
   }

   /**
    * f0 -> "ERROR"
    */
   public String visit(ErrorStmt n) {
	   return "ERROR\n";
   }

   /**
    * f0 -> "CJUMP"
    * f1 -> Exp()
    * f2 -> Label()
    */
   public String visit(CJumpStmt n) {
      String _ret = "";
      String temp = n.f1.accept(this);
      _ret = extractPretext() + _ret;
      _ret += "CJUMP " + temp + n.f2.accept(this) + "\n";
      return _ret;
   }

   /**
    * f0 -> "JUMP"
    * f1 -> Label()
    */
   public String visit(JumpStmt n) {
      return "JUMP " + n.f1.accept(this) + "\n";
   }

   /**
    * f0 -> "HSTORE"
    * f1 -> Exp()
    * f2 -> IntegerLiteral()
    * f3 -> Exp()
    */
   public String visit(HStoreStmt n) {
	  String _ret="";	  
	  String t1 = n.f1.accept(this);
	  _ret += extractPretext();
	  String t2 = n.f3.accept(this);
	  _ret += extractPretext();
	  _ret += "HSTORE " + t1 + n.f2.accept(this) + " " + t2 + "\n";
      return _ret;
   }

   /**
    * f0 -> "HLOAD"
    * f1 -> Temp()
    * f2 -> Exp()
    * f3 -> IntegerLiteral()
    */
   public String visit(HLoadStmt n) {
	  String _ret="";
	  String t = n.f2.accept(this);
    _ret = extractPretext() + _ret;
	  _ret += "HLOAD " + n.f1.accept(this) + " " + t + n.f3.accept(this) + "\n";
      return _ret;
   }

   /**
    * f0 -> "MOVE"
    * f1 -> Temp()
    * f2 -> Exp()
    */
   public String visit(MoveStmt n) {
      String _ret="";
      _ret += "MOVE " + n.f1.accept(this) + " " + n.f2.accept(this) + "\n";
      _ret = extractPretext() + _ret;
      return _ret;
   }

   /**
    * f0 -> "PRINT"
    * f1 -> Exp()
    */
   public String visit(PrintStmt n) {
      String _ret="";
  	  String t = n.f1.accept(this);
      _ret = extractPretext() + _ret;
      _ret += "PRINT " + t + "\n";
      return _ret;
   }

   /**
    * f0 -> StmtExp()
    *       | Call()
    *       | HAllocate()
    *       | BinOp()
    *       | Temp()
    *       | IntegerLiteral()
    *       | Label()
    */
   public String visit(Exp n) {
		  String t = getNewTemp();
		  appendPretext("MOVE " + t + n.f0.accept(this) + "\n");
		  return t;
   }

   /**
    * f0 -> "BEGIN"
    * f1 -> StmtList()
    * f2 -> "RETURN"
    * f3 -> Exp()
    * f4 -> "END"
    */
   public String visit(StmtExp n) {
      String _ret = "";
	    if(isfndef){
		    isfndef = false;
    	  _ret = "BEGIN\n";
    	  _ret += n.f1.accept(this) + "\n";
        String t = n.f3.accept(this);
    	  _ret += extractPretext(); 
    	  _ret += "RETURN " + t + "END\n";
      }
      else{
    	  appendPretext(n.f1.accept(this) + "\n");
        _ret = n.f3.accept(this);
      }
      return _ret;
   }

   /**
    * f0 -> "CALL"
    * f1 -> Exp()
    * f2 -> "("
    * f3 -> ( Exp() )*
    * f4 -> ")"
    */
   public String visit(Call n) {
    String t = n.f1.accept(this);
    String _ret="CALL " + t + "("; 
    _ret += n.f3.accept(this);
    _ret += ")\n";
    return _ret;
   }

   /**
    * f0 -> "HALLOCATE"
    * f1 -> Exp()
    */
   public String visit(HAllocate n) {
    String t = n.f1.accept(this);
	  return "HALLOCATE " + t + "\n";
   }

   /**
    * f0 -> Operator()
    * f1 -> Exp()
    * f2 -> Exp()
    */
   public String visit(BinOp n) {
      String t1 = n.f1.accept(this), t2 = n.f2.accept(this);
      return n.f0.accept(this) + t1 + t2 + "\n";
   }

   /**
    * f0 -> "LE"
    *       | "NE"
    *       | "PLUS"
    *       | "MINUS"
    *       | "TIMES"
    *       | "DIV"
    */
   public String visit(Operator n) {
      return ((NodeToken) n.f0.choice).tokenImage + " ";
   }

   /**
    * f0 -> "TEMP"
    * f1 -> IntegerLiteral()
    */
   public String visit(Temp n) {
      return "TEMP " + n.f1.accept(this);
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public String visit(IntegerLiteral n) {
      return n.f0.tokenImage + " ";
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public String visit(Label n) {
      return n.f0.tokenImage + " ";
   }
}