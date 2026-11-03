package app.stilldata.guard;
import java.time.*;
import java.util.*;
public final class CoreTests {
 static int checks=0;
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 static Set<String> set(String... values){return new HashSet<>(Arrays.asList(values));}
 public static void main(String[] args){
  Set<String> installed=set("video","chat","still"),mobile=set("video","removed","still"),wifi=set("chat");
  RulePlanner.Plan m=RulePlanner.plan(RulePlanner.NetworkKind.MOBILE,false,mobile,wifi,installed,"still");check(m.packages.equals(set("video")),"Mobile rules filter uninstalled and self packages");check(m.needsTunnel(),"Active rule needs a tunnel");check(!m.all,"Single-app rule must not affect all apps");
  RulePlanner.Plan w=RulePlanner.plan(RulePlanner.NetworkKind.WIFI,false,mobile,wifi,installed,"still");check(w.packages.equals(set("chat")),"Wi-Fi rules are independent");
  check(!RulePlanner.plan(RulePlanner.NetworkKind.MOBILE,false,set("removed"),wifi,installed,"still").needsTunnel(),"An empty allowed-app set must never create an all-app tunnel");
  check(RulePlanner.plan(RulePlanner.NetworkKind.MOBILE,true,mobile,wifi,installed,"still").all,"Quiet mode blocks all on mobile");
  check(!RulePlanner.plan(RulePlanner.NetworkKind.WIFI,true,mobile,wifi,installed,"still").all,"Quiet mode preserves Wi-Fi");
  check(!RulePlanner.plan(RulePlanner.NetworkKind.OFFLINE,true,mobile,wifi,installed,"still").needsTunnel(),"No tunnel without a network");
  check(!RulePlanner.plan(RulePlanner.NetworkKind.OTHER,true,mobile,wifi,installed,"still").needsTunnel(),"Other transports are not misclassified as cellular");
  check(FeatureState.all(true).count()==5,"Enable all turns on each local feature");check(FeatureState.all(false).count()==0,"Disable all turns off each local feature");
  check(!RulePlanner.plan(RulePlanner.NetworkKind.MOBILE,false,true,true,true,mobile,wifi,installed,"still").needsTunnel(),"Paused mobile rules override mobile block-all");
  check(RulePlanner.plan(RulePlanner.NetworkKind.WIFI,false,true,true,false,mobile,wifi,installed,"still").packages.equals(set("chat")),"Pausing mobile keeps Wi-Fi rules active");
  check(!RulePlanner.plan(RulePlanner.NetworkKind.WIFI,true,false,true,true,mobile,wifi,installed,"still").needsTunnel(),"Paused Wi-Fi overrides Wi-Fi block-all");
  check(RulePlanner.plan(RulePlanner.NetworkKind.MOBILE,true,false,false,true,mobile,wifi,installed,"still").packages.equals(set("video")),"Pausing Wi-Fi keeps mobile rules active");
  check(RulePlanner.plan(RulePlanner.NetworkKind.WIFI,true,true,false,true,mobile,wifi,installed,"still").all,"Wi-Fi can block all apps independently");
  check(!RulePlanner.plan(RulePlanner.NetworkKind.MOBILE,true,true,false,true,mobile,wifi,installed,"still").all,"Wi-Fi block-all does not become mobile block-all");
  check(RulePlanner.plan(RulePlanner.NetworkKind.MOBILE,true,true,true,false,mobile,wifi,installed,"still").all,"Mobile block-all is independent of Wi-Fi");
  check(!RulePlanner.plan(RulePlanner.NetworkKind.WIFI,true,true,true,false,mobile,wifi,installed,"still").all,"Mobile block-all does not become Wi-Fi block-all");
  check(!RulePlanner.plan(RulePlanner.NetworkKind.WIFI,true,true,false,false,set(),set(),installed,"still").needsTunnel(),"Allow-all leaves no accidental all-app VPN");
  SceneMotion motion=new SceneMotion();float start=motion.frame(1000,true),moving=motion.frame(1050,true);check(moving>start,"Scene clock advances when motion is enabled");
  check(SceneMotion.skierX(moving,400)>SceneMotion.skierX(start,400),"Skier visibly moves with the real scene clock");
  float frozen=motion.frame(1100,false);check(motion.frame(9000,false)==frozen,"Disabling motion freezes the clock");check(motion.frame(9050,true)-frozen<.1f,"Resuming motion avoids a large position jump");
  check(SceneMotion.snowY(10,1,20,180)!=SceneMotion.snowY(10,2,20,180),"Snow positions change across frames");
  check(SceneMotion.snowY(170,2,20,180)<180,"Snow wraps inside the viewport");
  check(!SceneMotion.pauseReason(false,false,1).isEmpty(),"Animation-off setting has a visible reason");
  check(SceneMotion.pauseReason(true,true,1).contains("Battery"),"Battery Saver pause is explained");
  check(SceneMotion.pauseReason(true,false,0).contains("Android"),"System animation pause is explained");
  check(SceneMotion.pauseReason(true,false,1).isEmpty(),"Enabled animation is not incorrectly paused");
  ZoneId zone=ZoneId.of("America/Los_Angeles");long now=ZonedDateTime.of(2026,3,9,0,0,0,0,zone).toInstant().toEpochMilli();long[] week=UsageWindows.range(UsageWindows.WEEK,1,now,zone);check(Instant.ofEpochMilli(week[0]).atZone(zone).toLocalDate().equals(LocalDate.of(2026,3,3)),"Seven calendar days survive DST");check(now-week[0]==143L*3600000,"DST ranges do not assume 24-hour days");
  long billing=ZonedDateTime.of(2026,3,5,12,0,0,0,zone).toInstant().toEpochMilli();long[] cycle=UsageWindows.range(UsageWindows.CYCLE,10,billing,zone);check(Instant.ofEpochMilli(cycle[0]).atZone(zone).toLocalDate().equals(LocalDate.of(2026,2,10)),"Billing date rolls into previous month");
  check(UsageWindows.range(UsageWindows.TODAY,1,billing,zone)[1]==billing,"End of range is the actual current time");
  check(UsageWindows.bytes(-1).equals("Unavailable"),"Missing totals do not appear as zero");check(UsageWindows.bytes(0).equals("0 B"),"Zero means measured zero");check(UsageWindows.bytes(1073741824L).endsWith("GB"),"Usage units convert correctly");
  System.out.println("Passed "+checks+" core checks: feature controls, independent network rules, block-all, scene motion/pause behavior, calendar ranges, and honest counters.");
 }
}
