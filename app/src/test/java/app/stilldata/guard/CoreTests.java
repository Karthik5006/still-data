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
  ZoneId zone=ZoneId.of("America/Los_Angeles");long now=ZonedDateTime.of(2026,3,9,0,0,0,0,zone).toInstant().toEpochMilli();long[] week=UsageWindows.range(UsageWindows.WEEK,1,now,zone);check(Instant.ofEpochMilli(week[0]).atZone(zone).toLocalDate().equals(LocalDate.of(2026,3,3)),"Seven calendar days survive DST");check(now-week[0]==143L*3600000,"DST ranges do not assume 24-hour days");
  long billing=ZonedDateTime.of(2026,3,5,12,0,0,0,zone).toInstant().toEpochMilli();long[] cycle=UsageWindows.range(UsageWindows.CYCLE,10,billing,zone);check(Instant.ofEpochMilli(cycle[0]).atZone(zone).toLocalDate().equals(LocalDate.of(2026,2,10)),"Billing date rolls into previous month");
  check(UsageWindows.range(UsageWindows.TODAY,1,billing,zone)[1]==billing,"End of range is the actual current time");
  check(UsageWindows.bytes(-1).equals("Unavailable"),"Missing totals do not appear as zero");check(UsageWindows.bytes(0).equals("0 B"),"Zero means measured zero");check(UsageWindows.bytes(1073741824L).endsWith("GB"),"Usage units convert correctly");
  System.out.println("Passed "+checks+" core checks: network-specific rules, empty-rule safety, quiet mode, calendar ranges, and honest counters.");
 }
}
