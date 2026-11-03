package app.stilldata.guard;
import android.app.*;
import android.app.usage.*;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.os.Process;
import java.time.*;
import java.util.*;
final class UsageRepository {
 static final class App {
  int uid;String label;final List<String> packages=new ArrayList<>();ApplicationInfo iconInfo;boolean system;long mobile,wifi;
 }
 static final class Snapshot {
  boolean allowed;String error="";long mobile=-1,wifi=-1,cycleMobile=-1;long updated;List<App> apps=new ArrayList<>();long[] daily=new long[7];
 }
 static boolean hasAccess(Context c){AppOpsManager ops=c.getSystemService(AppOpsManager.class);return ops!=null&&ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,Process.myUid(),c.getPackageName())==AppOpsManager.MODE_ALLOWED;}
 static Snapshot load(Context c,int period,int cycleDay){
  Snapshot out=new Snapshot();out.updated=System.currentTimeMillis();out.allowed=hasAccess(c);
  PackageManager pm=c.getPackageManager();Map<Integer,App> groups=new HashMap<>();
  for(ApplicationInfo info:pm.getInstalledApplications(0)){
   if(info.packageName.equals(c.getPackageName()))continue;
   App a=groups.get(info.uid);if(a==null){a=new App();a.uid=info.uid;a.label=info.loadLabel(pm).toString();a.iconInfo=info;a.system=(info.flags&ApplicationInfo.FLAG_SYSTEM)!=0;groups.put(a.uid,a);}else{if(a.system&&(info.flags&ApplicationInfo.FLAG_SYSTEM)==0){a.label=info.loadLabel(pm).toString();a.iconInfo=info;a.system=false;}}
   a.packages.add(info.packageName);
  }
  if(out.allowed){try{
   NetworkStatsManager manager=c.getSystemService(NetworkStatsManager.class);long[] range=UsageWindows.range(period,cycleDay,out.updated,ZoneId.systemDefault());
   Map<Integer,Long> mobile=query(manager,ConnectivityManager.TYPE_MOBILE,range[0],range[1]);Map<Integer,Long> wifi=query(manager,ConnectivityManager.TYPE_WIFI,range[0],range[1]);
   out.mobile=sum(mobile);out.wifi=sum(wifi);
   if(period==UsageWindows.CYCLE)out.cycleMobile=out.mobile;else{long[] cycle=UsageWindows.range(UsageWindows.CYCLE,cycleDay,out.updated,ZoneId.systemDefault());out.cycleMobile=sum(query(manager,ConnectivityManager.TYPE_MOBILE,cycle[0],cycle[1]));}
   for(Integer uid:mobile.keySet())if(!groups.containsKey(uid)){App a=new App();a.uid=uid;a.system=true;a.label=uid==-4?"Removed apps":uid==-5?"Hotspot & tethering":uid==1000?"Android system":"Android UID "+uid;groups.put(uid,a);}
   for(Integer uid:wifi.keySet())if(!groups.containsKey(uid)){App a=new App();a.uid=uid;a.system=true;a.label=uid==-4?"Removed apps":uid==-5?"Hotspot & tethering":"Android UID "+uid;groups.put(uid,a);}
   for(App a:groups.values()){a.mobile=mobile.getOrDefault(a.uid,0L);a.wifi=wifi.getOrDefault(a.uid,0L);}
   LocalDate today=Instant.ofEpochMilli(out.updated).atZone(ZoneId.systemDefault()).toLocalDate();
   for(int i=0;i<7;i++){LocalDate d=today.minusDays(6-i);long start=d.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();long end=Math.min(out.updated,d.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());out.daily[i]=sum(query(manager,ConnectivityManager.TYPE_MOBILE,start,end));}
  }catch(SecurityException e){out.allowed=false;out.error="Usage Access is needed to read Android’s totals.";}catch(Exception e){out.error="Android could not return usage totals. Tap refresh to retry.";out.mobile=-1;out.wifi=-1;out.cycleMobile=-1;}}
  out.apps.addAll(groups.values());Collections.sort(out.apps,(a,b)->{int usage=Long.compare(b.mobile,a.mobile);return usage!=0?usage:a.label.compareToIgnoreCase(b.label);});return out;
 }
 private static Map<Integer,Long> query(NetworkStatsManager manager,int type,long from,long to)throws Exception{
  Map<Integer,Long> result=new HashMap<>();try(NetworkStats stats=manager.querySummary(type,null,from,to)){NetworkStats.Bucket b=new NetworkStats.Bucket();while(stats.hasNextBucket()){stats.getNextBucket(b);int uid=b.getUid();long value=b.getRxBytes()+b.getTxBytes();result.put(uid,result.getOrDefault(uid,0L)+value);}}return result;
 }
 private static long sum(Map<Integer,Long> map){long total=0;for(long n:map.values())total+=n;return total;}
}
