package app.stilldata.guard;
import android.content.*;
import java.util.*;
final class Store {
 final SharedPreferences prefs;
 Store(Context c){prefs=c.getSharedPreferences("still_data",Context.MODE_PRIVATE);}
 boolean armed(){return prefs.getBoolean("armed",false);}void armed(boolean b){prefs.edit().putBoolean("armed",b).apply();}
 boolean quiet(){return prefs.getBoolean("quiet",false);}void quiet(boolean b){prefs.edit().putBoolean("quiet",b).apply();}
 boolean motion(){return prefs.getBoolean("motion",true);}void motion(boolean b){prefs.edit().putBoolean("motion",b).apply();}
 Set<String> blocked(boolean wifi){return new HashSet<>(prefs.getStringSet(wifi?"wifi_blocked":"mobile_blocked",Collections.emptySet()));}
 void block(Collection<String> packages,boolean wifi,boolean block){Set<String> set=blocked(wifi);if(block)set.addAll(packages);else set.removeAll(packages);prefs.edit().putStringSet(wifi?"wifi_blocked":"mobile_blocked",set).apply();}
 int cycleDay(){return prefs.getInt("cycle_day",1);}long budget(){return prefs.getLong("budget_bytes",0);}
 void budget(long bytes,int day){prefs.edit().putLong("budget_bytes",bytes).putInt("cycle_day",day).apply();}
 long packets(){return prefs.getLong("packets",0);}long droppedBytes(){return prefs.getLong("dropped_bytes",0);}
 void counters(long packets,long bytes){prefs.edit().putLong("packets",packets).putLong("dropped_bytes",bytes).apply();}
 String status(){return prefs.getString("status","Off");}void status(String s){prefs.edit().putString("status",s).apply();}
 String dnsHost(){return prefs.getString("dns_host","dns.adguard-dns.com");}void dnsHost(String s){prefs.edit().putString("dns_host",s).apply();}
}
