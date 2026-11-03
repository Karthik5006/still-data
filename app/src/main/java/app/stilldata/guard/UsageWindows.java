package app.stilldata.guard;
import java.time.*;
final class UsageWindows {
 static final int TODAY=0,WEEK=1,CYCLE=2;
 static long[] range(int period,int cycleDay,long now,ZoneId zone){
  LocalDate date=Instant.ofEpochMilli(now).atZone(zone).toLocalDate();
  LocalDate start=date;
  if(period==WEEK)start=date.minusDays(6);
  if(period==CYCLE){int day=Math.max(1,Math.min(28,cycleDay));start=date.withDayOfMonth(day);if(start.isAfter(date))start=start.minusMonths(1);}
  return new long[]{start.atStartOfDay(zone).toInstant().toEpochMilli(),now};
 }
 static String bytes(long bytes){if(bytes<0)return "Unavailable";if(bytes<1024)return bytes+" B";double value=bytes;String[] units={"KB","MB","GB","TB"};int i=-1;do{value/=1024;i++;}while(value>=1024&&i<units.length-1);return String.format(java.util.Locale.getDefault(),value>=100?"%.0f %s":"%.1f %s",value,units[i]);}
}
