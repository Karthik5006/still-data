package app.stilldata.guard;
import java.util.*;
final class RulePlanner {
 enum NetworkKind { MOBILE, WIFI, OTHER, OFFLINE }
 static final class Plan {
  final boolean all;final Set<String> packages;
  Plan(boolean all,Set<String> packages){this.all=all;this.packages=Collections.unmodifiableSet(packages);}
  boolean needsTunnel(){return all||!packages.isEmpty();}
  String key(){List<String> sorted=new ArrayList<>(packages);Collections.sort(sorted);return all+":"+sorted.toString();}
 }
 static Plan plan(NetworkKind network,boolean quietMobile,Set<String> mobile,Set<String> wifi,Set<String> installed,String self){
  boolean all=quietMobile&&network==NetworkKind.MOBILE;
  Set<String> selected=new HashSet<>();
  if(network==NetworkKind.MOBILE)selected.addAll(mobile);
  if(network==NetworkKind.WIFI)selected.addAll(wifi);
  selected.retainAll(installed);selected.remove(self);
  return new Plan(all,selected);
 }
}
