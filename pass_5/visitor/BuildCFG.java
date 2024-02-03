//
// Generated by JTB 1.3.2
//

package visitor;
import syntaxtree.*;
import java.util.*;

class CustomCompare implements Comparator<LiveRange>{
  public int compare(LiveRange l1, LiveRange l2){
        return l1.start - l2.start;
  }
}

/**
 * Provides default methods which visit each node in the tree in depth-first
 * order.  Your visitors may extend this class.
 */
public class BuildCFG extends GJNoArguDepthFirst<ArrayList<NodeToken>> {
   //
   // Auto class visitors--probably don't need to be overridden.
   //

   public ArrayList<Proc> procs = new ArrayList<Proc>();
   ArrayList<Line> lines = new ArrayList<Line>();
   // ArrayList<NodeToken> curret;
   
   int lno = 1;
   boolean addtempflag = false;
   boolean labelflag = false;
   String  currlabel = "";
   
   public void printProc(int i){
	   System.out.println(procs.get(i).label);
	   System.out.println(procs.get(i).argc);
	   for(Line l: procs.get(i).proc){
		   System.out.println();
		   System.out.println(Integer.toString(l.lno) + ":" + l.label + ":" + l.statement);
		   System.out.println("Def: " + l.def + "\nUse: " + l.use);
		   System.out.print("Succ: ");
		   for(Line s: l.succ)
			   System.out.print(s.lno + ",");
		   System.out.println();
		   System.out.print("Pred: ");
		   for(Line s: l.pred)
			   System.out.print(s.lno + ",");
		   System.out.println();
		   System.out.println("In: " + l.in);
		   System.out.println("out: "+ l.out);
	   }
	   // System.out.println(procs.get(i).returnval);
   }
   
   public void addEdge(Proc p, int m, int n){
	   if(p.proc.size() <= n)	return;
	   p.proc.get(m).succ.add(p.proc.get(n));
	   p.proc.get(n).pred.add(p.proc.get(m));
   }
   
   public Line getLineFromAddr(String s){
	   for(Proc p: procs){
		   for(Line l: p.proc){
			   if(l.label == s) 	return l;
		   }
	   }
	   return null;
   }
   
   public void addEdgeAddr(Proc p, int m, String s){
	   Line l = getLineFromAddr(s);
	   p.proc.get(m).succ.add(l);
	   l.pred.add(p.proc.get(m));
   }
   
   public void buildEdges(){
	   for(Proc p: procs){
		   for(int i=0; i<p.proc.size(); i++){
			   Line l = p.proc.get(i);
			   if(l.statement.get(0).tokenImage == "NOOP")				addEdge(p, i, i+1);
			   else if(l.statement.get(0).tokenImage == "ERROR")		addEdge(p, i, i+1);
			   else if(l.statement.get(0).tokenImage == "CJUMP")		{ addEdge(p, i, i+1); addEdgeAddr(p, i, l.statement.get(3).tokenImage);}
			   else if(l.statement.get(0).tokenImage == "JUMP")			addEdgeAddr(p, i, l.statement.get(1).tokenImage);
			   else if(l.statement.get(0).tokenImage == "HSTORE")		addEdge(p, i, i+1);
			   else if(l.statement.get(0).tokenImage == "HLOAD")		addEdge(p, i, i+1);
			   else if(l.statement.get(0).tokenImage == "MOVE")			addEdge(p, i, i+1);
			   else if(l.statement.get(0).tokenImage == "PRINT")		addEdge(p, i, i+1);
			   else if(l.statement.get(0).tokenImage == "RETURN")		;
		   }
	   }
   }
   
   public void livenessAnalysis(){
	   for(Proc p: procs){
		   boolean flag = true;
		   while(flag){
			   flag = false;
			   for(Line l:p.proc){
				   // Computing Out
				   HashSet<String> temp_out = new HashSet<String>();
				   for(Line s: l.succ)
					   temp_out.addAll(s.in);
				   if(! l.out.containsAll(temp_out)){
					   flag = true;
					   l.out = temp_out;
				   }
				   
				   //Computing in
				   HashSet<String> temp_in = new HashSet<String>();
				   temp_in.addAll(l.out);
				   temp_in.removeAll(l.def);
				   temp_in.addAll(l.use);
				   if(! l.in.containsAll(temp_in)){
					   flag = true;
					   l.in = temp_in;
				   }   
			   }
		   }		   
	   }
   }
   
   public ArrayList<LiveRange> genLiveRanges(Proc p){
	   HashMap<String, Integer> start = new HashMap<String, Integer>();
	   HashMap<String, Integer> end = new HashMap<String, Integer>();
	   for(Line l: p.proc){
		   HashSet<String> live = new HashSet<String>();
		   live.addAll(l.in);
		   live.addAll(l.def);
		   for(String s: live){
			   if(!start.containsKey(s)){
				   start.put(s, l.lno);
				   end.put(s, l.lno);
			   }
			   else{
				   if(start.get(s) > l.lno)
					   start.put(s, l.lno);
				   if(end.get(s) < l.lno)
					   end.put(s, l.lno);
			   }
		   }
	   }
	   
	   ArrayList<LiveRange> ret = new ArrayList<LiveRange>();
	   for(String s: start.keySet()){
		   LiveRange temp = new LiveRange();
		   temp.start = start.get(s);
		   temp.end = end.get(s);
		   temp.temp = s;
		   ret.add(temp);
	   }
	   return ret;
   }
   
   public void expireOldIntervals(ArrayList<String> freeRegs, ArrayList<LiveRange> active, LiveRange l){
	   int i = 0;
	   while(i < active.size() && active.get(i).end < l.start){
		   freeRegs.add(active.get(i).location);		   
		   active.remove(i);
	   }
   }
   
   public int spill(ArrayList<LiveRange> active, LiveRange l, int stackLoc){
	   if(active.get(active.size()-1).end > l.end){
		   l.location = active.get(active.size()-1).location;
		   
		   active.get(active.size()-1).location = "";
		   active.get(active.size()-1).stacked = true;
		   active.get(active.size()-1).stackLoc = stackLoc;
		   stackLoc++;
	   
		   active.remove(active.size()-1);
		   
		   for(int al=0; al < active.size(); al++)
			   if(l.end < active.get(al).end){
				   active.add(al, l);
				   break;
			   }
	   }
	   else{
		   l.stacked = true;
		   l.stackLoc = stackLoc;
		   stackLoc++;
	   }
	   return stackLoc;
   }
   
   public int perProcLinScanAlloc(ArrayList<LiveRange> liveRanges, int stackBegin, Proc p){
	   int numRegs = 18;
	   int stackLoc = stackBegin;
	   
	   // Build Free Registers
	   ArrayList<String> freeRegs = new ArrayList<String>();
	   for(int i=0;i<=7;i++)	freeRegs.add("s" + Integer.toString(i));
	   for(int i=0;i<=9;i++)	freeRegs.add("t" + Integer.toString(i));
	   
	   HashSet<String> usedRegs = new HashSet<String>();
	   // Build Active Live Range Intervals
	   ArrayList<LiveRange> active  = new ArrayList<LiveRange>(); 
	   
	   // Build liveRanges sorted on start pt
	   Collections.sort(liveRanges, new CustomCompare());
	   
	   for(LiveRange l: liveRanges){
		   expireOldIntervals(freeRegs, active, l);  
		   if(active.size() == numRegs-1){
			   stackLoc = spill(active, l, stackLoc);
		   }
		   else{
			   // Use a free register
			   l.location = freeRegs.get(freeRegs.size()-1);
			   usedRegs.add(l.location);
			   // Mark reg used
			   freeRegs.remove(freeRegs.size()-1);
			   // Insert l to active sorted based on end time
			   int al;
			   for(al=0; al < active.size(); al++)
				   if(l.end < active.get(al).end)	break;
			   active.add(al, l);
		   }
			   
	   }
	   p.used = usedRegs;
	   return stackLoc;
   }
   
   public void linearScanAlloc(){
	   for(Proc p:procs){
		   ArrayList<LiveRange> liveRanges = genLiveRanges(p);
		   
		   ArrayList<LiveRange> args = new ArrayList<LiveRange>();
		   for(int i = 0; i < liveRanges.size(); i++){
			   if(Integer.parseInt(liveRanges.get(i).temp) < p.argc)
				   args.add(liveRanges.get(i));
		   }
		   
		   int stackLoc = 0;
		   ArrayList<LiveRange> argsFinal = new ArrayList<LiveRange>();
		   for(LiveRange l: args){
			   if(Integer.parseInt(l.temp) <= 3){
				   l.argLoc = "a" + Integer.toString(Integer.parseInt(l.temp));
				   argsFinal.add(l);
			   }
			   else{
				   l.stacked = true;
				   l.stackLoc = Integer.parseInt(l.temp)-4;
				   stackLoc++;
			   }
		   }
		   args.removeAll(argsFinal);
		   liveRanges.removeAll(args);
		   
		   int spillcount = perProcLinScanAlloc(liveRanges, stackLoc, p);
		   liveRanges.addAll(args);
		   
		   p.stackSpace = spillcount;
		   p.liveRanges = liveRanges;
		   p.args = argsFinal;
	   }
   }
   
   public void epilogue(){
	   buildEdges();
	   livenessAnalysis();
	   linearScanAlloc();
	   
       Translator t = new Translator();
	   String translated = "";
	   
	   ArrayList<String> fns = new ArrayList<String>();
	   for(Proc p: procs)
		   fns.add(p.label);
	   
	   t.fns = fns;
	   int count = 0;
	   for(Proc p : procs){
		   count ++;
		   t.proc = p;
		   t.locMap.clear();
		   t.stackMap.clear();
		   t.argMap = p.args;
		   t.used = p.used;
		   t.buildHash();
		   t.maxCallArgs = 0;
		   t.stackSpace = p.stackSpace;
		   t.label_prefix = "local_" + Integer.toString(count) + "_";
		   translated += p.root.accept(t) + "\n";
	   }
	   translated += "// Number of  vars after packing =" + "0" + "; Number of Spilled vars =" + "0";
	   System.out.println(translated);
   }
   
   public ArrayList<NodeToken> visit(NodeList n) {
      ArrayList<NodeToken> _ret = new ArrayList<NodeToken>();
      int _count=0;
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
         _ret.addAll(e.nextElement().accept(this));
         _count++;
      }
      return _ret;
   }

   public ArrayList<NodeToken> visit(NodeListOptional n) {
      if ( n.present() ) {
         ArrayList<NodeToken> _ret = new ArrayList<NodeToken>();
         int _count=0;
         for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            _ret.addAll(e.nextElement().accept(this));
            _count++;
         }
         return _ret;
      }
      else
         return new ArrayList<NodeToken>();
   }

   public ArrayList<NodeToken> visit(NodeOptional n) {
      if ( n.present() )
         return n.node.accept(this);
      else
         return new ArrayList<NodeToken>();
   }

   public ArrayList<NodeToken> visit(NodeSequence n) {
      ArrayList<NodeToken> _ret = new ArrayList<NodeToken>();
      int count=0;
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
    	  if(count == 0)	labelflag = true; 	
    	  _ret.addAll(e.nextElement().accept(this));
    	  labelflag = false;
    	  count++;
      }
      return _ret;
   }

   public ArrayList<NodeToken> visit(NodeToken n) { return new ArrayList<NodeToken>(); }

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
   public ArrayList<NodeToken> visit(Goal n) {
      ArrayList<NodeToken> _ret = new ArrayList<NodeToken>();
      procs.add(new Proc());
      procs.get(procs.size()-1).label = "MAIN";
      procs.get(procs.size()-1).argc  = 0;
      procs.get(procs.size()-1).root  = n;
      
      n.f1.accept(this);
      procs.get(procs.size()-1).proc  = lines;
      lines = new ArrayList<Line>();

      n.f3.accept(this);
      
      epilogue();
      return _ret;
   }

   /**
    * f0 -> ( ( Label() )? Stmt() )*
    */
   public ArrayList<NodeToken> visit(StmtList n) {
      ArrayList<NodeToken> _ret = new ArrayList<NodeToken>();
      _ret.addAll(n.f0.accept(this));
      return _ret;
   }

   /**
    * f0 -> Label()
    * f1 -> "["
    * f2 -> IntegerLiteral()
    * f3 -> "]"
    * f4 -> StmtExp()
    */
   public ArrayList<NodeToken> visit(Procedure n) {
      ArrayList<NodeToken> _ret = new ArrayList<NodeToken>();
      procs.add(new Proc());

      procs.get(procs.size()-1).label = n.f0.accept(this).get(0).tokenImage;
      procs.get(procs.size()-1).argc  = Integer.parseInt(n.f2.accept(this).get(0).tokenImage);
      procs.get(procs.size()-1).root  = n;
      
      n.f4.accept(this);
      procs.get(procs.size()-1).proc  = lines;
      lines = new ArrayList<Line>();
      
//      procs.get(procs.size()-1).returnval = curret;
//      curret = new ArrayList<NodeToken>();
      
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
   public ArrayList<NodeToken> visit(Stmt n) {
	  lines.add(new Line());
	  lines.get(lines.size()-1).lno   = lno;
	  lines.get(lines.size()-1).label = currlabel;
	  lines.get(lines.size()-1).root  = n;
	  currlabel = "";
	  
	  ArrayList<NodeToken> _ret = n.f0.accept(this);
      lines.get(lines.size()-1).statement = _ret;
      lno++;
      return _ret;
   }

   /**
    * f0 -> "NOOP"
    */
   public ArrayList<NodeToken> visit(NoOpStmt n) {
      ArrayList<NodeToken> _ret = new ArrayList<NodeToken>();
      _ret.add(n.f0);
      return _ret;
   }

   /**
    * f0 -> "ERROR"
    */
   public ArrayList<NodeToken> visit(ErrorStmt n) {
      ArrayList<NodeToken> _ret = new ArrayList<NodeToken>();
      _ret.add(n.f0);
      return _ret;
   }

   /**
    * f0 -> "CJUMP"
    * f1 -> Temp()
    * f2 -> Label()
    */
   public ArrayList<NodeToken> visit(CJumpStmt n) {
      ArrayList<NodeToken> _ret = new ArrayList<NodeToken>();
      _ret.add(n.f0);
      
      ArrayList<NodeToken> t = n.f1.accept(this);
      lines.get(lines.size()-1).use.add(t.get(1).tokenImage);
      _ret.addAll(t);
      
      _ret.addAll(n.f2.accept(this));
      return _ret;
   }

   /**
    * f0 -> "JUMP"
    * f1 -> Label()
    */
   public ArrayList<NodeToken> visit(JumpStmt n) {
      ArrayList<NodeToken> _ret = n.f1.accept(this);
      _ret.add(0, n.f0);
      return _ret;
   }

   /**
    * f0 -> "HSTORE"
    * f1 -> Temp()
    * f2 -> IntegerLiteral()
    * f3 -> Temp()
    */
   public ArrayList<NodeToken> visit(HStoreStmt n) {
      ArrayList<NodeToken> _ret = new ArrayList<NodeToken>();
      _ret.add(n.f0);
      
      ArrayList<NodeToken> t1 = n.f1.accept(this);
      lines.get(lines.size()-1).use.add(t1.get(1).tokenImage);
      _ret.addAll(t1);
      _ret.addAll(n.f2.accept(this));
      
      ArrayList<NodeToken> t2 = n.f3.accept(this);
      lines.get(lines.size()-1).use.add(t2.get(1).tokenImage);
      _ret.addAll(t2);
      
      return _ret;
   }

   /**
    * f0 -> "HLOAD"
    * f1 -> Temp()
    * f2 -> Temp()
    * f3 -> IntegerLiteral()
    */
   public ArrayList<NodeToken> visit(HLoadStmt n) {
      ArrayList<NodeToken> _ret = new ArrayList<NodeToken>();
      _ret.add(n.f0);
      
      ArrayList<NodeToken> t1 = n.f1.accept(this);
      ArrayList<NodeToken> t2 = n.f2.accept(this);
      lines.get(lines.size()-1).def.add(t1.get(1).tokenImage);
      lines.get(lines.size()-1).use.add(t2.get(1).tokenImage);
      
      _ret.addAll(t1);
      _ret.addAll(t2);
      _ret.addAll(n.f3.accept(this));
      
      return _ret;
   }

   /**
    * f0 -> "MOVE"
    * f1 -> Temp()
    * f2 -> Exp()
    */
   public ArrayList<NodeToken> visit(MoveStmt n) {
      ArrayList<NodeToken> _ret = new ArrayList<NodeToken>();
      _ret.add(n.f0);
      
      ArrayList<NodeToken> t = n.f1.accept(this);
      lines.get(lines.size()-1).def.add(t.get(1).tokenImage);
      _ret.addAll(t);
      
      _ret.addAll(n.f2.accept(this));
      return _ret;
   }

   /**
    * f0 -> "PRINT"
    * f1 -> SimpleExp()
    */
   public ArrayList<NodeToken> visit(PrintStmt n) {
      ArrayList<NodeToken> _ret = new ArrayList<NodeToken>();
      _ret.add(n.f0);
      _ret.addAll(n.f1.accept(this));
      
      return _ret;
   }

   /**
    * f0 -> Call()
    *       | HAllocate()
    *       | BinOp()
    *       | SimpleExp()
    */
   public ArrayList<NodeToken> visit(Exp n) {
      return n.f0.accept(this);
   }

   /**
    * f0 -> "BEGIN"
    * f1 -> StmtList()
    * f2 -> "RETURN"
    * f3 -> SimpleExp()
    * f4 -> "END"
    */
   public ArrayList<NodeToken> visit(StmtExp n) {
      ArrayList<NodeToken> _ret = new ArrayList<NodeToken>();
      _ret.add(n.f0);
      _ret.addAll(n.f1.accept(this));
      _ret.add(n.f2);
      
      
	  lines.add(new Line());
	  lines.get(lines.size()-1).lno   = lno;
	  lines.get(lines.size()-1).root  = n.f3;
	  
	  ArrayList<NodeToken> t = n.f3.accept(this);
      t.add(0,n.f2);
	  lines.get(lines.size()-1).statement = t;
      lno++;
      
      _ret.addAll(t);
      _ret.add(n.f4);
      return _ret;
   }

   /**
    * f0 -> "CALL"
    * f1 -> SimpleExp()
    * f2 -> "("
    * f3 -> ( Temp() )*
    * f4 -> ")"
    */
   public ArrayList<NodeToken> visit(Call n) {
      ArrayList<NodeToken> _ret = new ArrayList<NodeToken>();
      _ret.add(n.f0);
      _ret.addAll(n.f1.accept(this));
      _ret.add(n.f2);
      
      addtempflag = true;
      _ret.addAll(n.f3.accept(this));
      addtempflag = false;
      
      _ret.add(n.f4);
      
      return _ret;
   }

   /**
    * f0 -> "HALLOCATE"
    * f1 -> SimpleExp()
    */
   public ArrayList<NodeToken> visit(HAllocate n) {
      ArrayList<NodeToken> _ret = new ArrayList<NodeToken>();
      _ret.add(n.f0);
      _ret.addAll(n.f1.accept(this));
      
      return _ret;
   }

   /**
    * f0 -> Operator()
    * f1 -> Temp()
    * f2 -> SimpleExp()
    */
   public ArrayList<NodeToken> visit(BinOp n) {
      ArrayList<NodeToken> _ret = n.f0.accept(this);
      ArrayList<NodeToken> t1 = n.f1.accept(this);
      ArrayList<NodeToken> t2 = n.f2.accept(this);
      
	  lines.get(lines.size()-1).use.add(t1.get(1).tokenImage);
      _ret.addAll(t1);
      _ret.addAll(t2);
      
      return _ret;
   }

   /**
    * f0 -> "LE"
    *       | "NE"
    *       | "PLUS"
    *       | "MINUS"
    *       | "TIMES"
    *       | "DIV"
    */
   public ArrayList<NodeToken> visit(Operator n) {
      ArrayList<NodeToken> t = new ArrayList<NodeToken>();
      t.add((NodeToken)n.f0.choice);
      return t;
   }

   /**
    * f0 -> Temp()
    *       | IntegerLiteral()
    *       | Label()
    */
   public ArrayList<NodeToken> visit(SimpleExp n) {
	   ArrayList<NodeToken> t = n.f0.accept(this);
	   if(n.f0.which == 0)	lines.get(lines.size()-1).use.add(t.get(1).tokenImage);
	   
	   return t;
   }

   /**
    * f0 -> "TEMP"
    * f1 -> IntegerLiteral()
    */
   public ArrayList<NodeToken> visit(Temp n) {
      ArrayList<NodeToken> _ret = n.f1.accept(this);

      if(addtempflag)
    	  lines.get(lines.size()-1).use.add(_ret.get(0).tokenImage);
      
      _ret.add(0, n.f0);
      return _ret;
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public ArrayList<NodeToken> visit(IntegerLiteral n) {
	   ArrayList<NodeToken> a = new ArrayList<NodeToken>();
	   a.add(n.f0);
	   return a;
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public ArrayList<NodeToken> visit(Label n) {
	   if(labelflag)	currlabel = n.f0.tokenImage;
	   
	   ArrayList<NodeToken> a = new ArrayList<NodeToken>();
	   a.add(n.f0);
	   return a;
   }
}
