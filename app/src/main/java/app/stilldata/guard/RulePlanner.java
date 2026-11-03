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
  return plan(network,true,true,quietMobile,false,mobile,wifi,installed,self);
 }
 static Plan plan(NetworkKind network,boolean mobileEnabled,boolean wifiEnabled,boolean quietMobile,boolean quietWifi,Set<String> mobile,Set<String> wifi,Set<String> installed,String self){
  boolean active=(network==NetworkKind.MOBILE&&mobileEnabled)||(network==NetworkKind.WIFI&&wifiEnabled);
  Set<String> selected=new HashSet<>();
  if(!active)return new Plan(false,selected);
  boolean all=network==NetworkKind.MOBILE?quietMobile:quietWifi;
  selected.addAll(network==NetworkKind.MOBILE?mobile:wifi);
  selected.retainAll(installed);selected.remove(self);
  return new Plan(all,selected);
 }
}
