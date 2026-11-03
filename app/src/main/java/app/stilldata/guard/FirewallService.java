package app.stilldata.guard;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.os.*;
import android.system.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public final class FirewallService extends VpnService {
 public static final String ACTION_START="app.stilldata.guard.START",ACTION_STOP="app.stilldata.guard.STOP",ACTION_CHANGED="app.stilldata.guard.CHANGED";
 private static final String CHANNEL="still_firewall";private static final int NOTICE=72;
 private Store store;private ConnectivityManager connectivity;
 private final ScheduledExecutorService work=Executors.newSingleThreadScheduledExecutor();
 private final AtomicInteger generation=new AtomicInteger();
 private final AtomicLong packets=new AtomicLong(),droppedBytes=new AtomicLong();
 private volatile boolean destroyed;private ParcelFileDescriptor tunnel;private volatile String currentKey="";
 private ConnectivityManager.NetworkCallback callback;
 @Override public void onCreate(){
  super.onCreate();store=new Store(this);packets.set(store.packets());droppedBytes.set(store.droppedBytes());
  connectivity=getSystemService(ConnectivityManager.class);
  NotificationManager nm=getSystemService(NotificationManager.class);
  nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Firewall status",NotificationManager.IMPORTANCE_LOW));
  store.status("Starting firewall…");foreground(notification("Starting firewall…"));
  callback=new ConnectivityManager.NetworkCallback(){
   @Override public void onAvailable(Network n){schedule();}
   @Override public void onLost(Network n){schedule();}
   @Override public void onCapabilitiesChanged(Network n,NetworkCapabilities c){schedule();}
  };
  try{connectivity.registerNetworkCallback(new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN).build(),callback);}catch(RuntimeException e){setStatus("Could not monitor the connection");}
  work.scheduleWithFixedDelay(()->{if(!destroyed){store.counters(packets.get(),droppedBytes.get());broadcast();}},5,5,TimeUnit.SECONDS);
 }
 private void foreground(Notification n){if(Build.VERSION.SDK_INT>=34)startForeground(NOTICE,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);else startForeground(NOTICE,n);}
 @Override public int onStartCommand(Intent intent,int flags,int startId){
  if(intent!=null&&ACTION_STOP.equals(intent.getAction())){store.armed(false);stopSelf();return START_NOT_STICKY;}
  if(!store.armed()){stopSelf();return START_NOT_STICKY;}
  schedule();return START_STICKY;
 }
 private void schedule(){if(!destroyed)try{work.execute(()->{if(!destroyed)rebuild();});}catch(RejectedExecutionException ignored){}}
 private void rebuild(){
  if(!store.armed()){closeTunnel();stopSelf();return;}
  if(VpnService.prepare(this)!=null){store.armed(false);closeTunnel();setStatus("VPN permission is needed");stopSelf();return;}
  Network chosen=chooseNetwork();RulePlanner.NetworkKind kind=kind(chosen);
  Set<String> installed=new HashSet<>();
  for(ApplicationInfo a:getPackageManager().getInstalledApplications(0))installed.add(a.packageName);
  RulePlanner.Plan plan=RulePlanner.plan(kind,store.networkEnabled(false),store.networkEnabled(true),store.blockAll(false),store.blockAll(true),store.blocked(false),store.blocked(true),installed,getPackageName());
  boolean networkEnabled=kind==RulePlanner.NetworkKind.WIFI?store.networkEnabled(true):store.networkEnabled(false);
  String key=(chosen==null?"offline":chosen.toString())+":"+networkEnabled+":"+plan.key();
  if(key.equals(currentKey))return;
  currentKey="";
  if(!plan.needsTunnel()){
   closeTunnel();
   currentKey=key;if(!networkEnabled&&(kind==RulePlanner.NetworkKind.WIFI||kind==RulePlanner.NetworkKind.MOBILE)){setStatus((kind==RulePlanner.NetworkKind.WIFI?"Wi-Fi":"Mobile")+" rules paused");return;}setStatus(kind==RulePlanner.NetworkKind.OFFLINE?"Ready · no connection":kind==RulePlanner.NetworkKind.WIFI?"Ready · Wi-Fi":kind==RulePlanner.NetworkKind.MOBILE?"Ready · mobile data":"Ready · other network");return;
  }
  try{
   Builder b=new Builder().setSession("Still Data · local firewall").setMtu(1500).addAddress("10.111.0.1",32).addRoute("0.0.0.0",0).addAddress("fd75:51:11::1",128).addRoute("::",0).addDnsServer("10.111.0.2").setBlocking(true).setMetered(kind==RulePlanner.NetworkKind.MOBILE);
   if(chosen!=null)b.setUnderlyingNetworks(new Network[]{chosen});
   if(plan.all)b.addDisallowedApplication(getPackageName());
   else{
    int valid=0;for(String p:plan.packages)try{b.addAllowedApplication(p);valid++;}catch(PackageManager.NameNotFoundException ignored){}
    // An empty allowed-app list means EVERY app in Android. Never establish it accidentally.
    if(valid==0){closeTunnel();setStatus("Ready · no installed apps selected");return;}
   }
   PendingIntent configure=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);b.setConfigureIntent(configure);
   ParcelFileDescriptor next=b.establish();if(next==null)throw new IOException("VPN permission was withdrawn");
   closeTunnel();tunnel=next;
   final ParcelFileDescriptor inputFd=ParcelFileDescriptor.dup(tunnel.getFileDescriptor());final int id=generation.incrementAndGet();
   new Thread(()->drain(inputFd,id),"Still-local-firewall").start();currentKey=key;
   String network=kind==RulePlanner.NetworkKind.WIFI?"Wi-Fi":"mobile data";
   setStatus(plan.all?"All apps blocked · "+network:"Blocking "+plan.packages.size()+" app rules · "+network);
  }catch(Exception e){android.util.Log.e("StillFirewall","Could not establish the local VPN",e);closeTunnel();setStatus("Firewall paused · toggle off and on to retry");}
 }
 private void drain(ParcelFileDescriptor fd,int id){
  try(ParcelFileDescriptor.AutoCloseInputStream input=new ParcelFileDescriptor.AutoCloseInputStream(fd)){
   StructPollfd poll=new StructPollfd();poll.fd=fd.getFileDescriptor();poll.events=(short)OsConstants.POLLIN;byte[] packet=new byte[65535];
   while(!destroyed&&generation.get()==id){
    int count=Os.poll(new StructPollfd[]{poll},1000);
    if(count>0&&(poll.revents&OsConstants.POLLIN)!=0){int size=input.read(packet);if(size<0)break;packets.incrementAndGet();droppedBytes.addAndGet(size);}
   }
  }catch(Exception e){if(!destroyed&&generation.get()==id){currentKey="";setStatus("Firewall interrupted · tap enable to retry");schedule();}}
 }
 private Network chooseNetwork(){return NetworkPicker.choose(connectivity);}
 private RulePlanner.NetworkKind kind(Network n){
  NetworkCapabilities c=connectivity.getNetworkCapabilities(n);if(c==null)return RulePlanner.NetworkKind.OFFLINE;
  if(c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))return RulePlanner.NetworkKind.WIFI;
  if(c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))return RulePlanner.NetworkKind.MOBILE;
  return RulePlanner.NetworkKind.OTHER;
 }
 private void closeTunnel(){generation.incrementAndGet();if(tunnel!=null){try{tunnel.close();}catch(IOException ignored){}tunnel=null;}}
 private Notification notification(String message){
  Intent open=new Intent(this,MainActivity.class);PendingIntent pi=PendingIntent.getActivity(this,0,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  PendingIntent stop=PendingIntent.getService(this,1,new Intent(this,FirewallService.class).setAction(ACTION_STOP),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  return new Notification.Builder(this,CHANNEL).setSmallIcon(app.stilldata.guard.R.drawable.ic_guard).setContentTitle("Still Data").setContentText(message).setContentIntent(pi).setOngoing(true).setCategory(Notification.CATEGORY_SERVICE).addAction(new Notification.Action.Builder(null,"Turn off",stop).build()).build();
 }
 private void setStatus(String status){store.status(status);if(!destroyed)getSystemService(NotificationManager.class).notify(NOTICE,notification(status));broadcast();}
 private void broadcast(){sendBroadcast(new Intent(ACTION_CHANGED).setPackage(getPackageName()));}
 @Override public void onRevoke(){store.armed(false);store.status("VPN permission was revoked");closeTunnel();broadcast();stopSelf();super.onRevoke();}
 @Override public void onDestroy(){destroyed=true;closeTunnel();try{if(callback!=null)connectivity.unregisterNetworkCallback(callback);}catch(RuntimeException ignored){}work.shutdownNow();store.counters(packets.get(),droppedBytes.get());if(!store.armed())store.status("Off");broadcast();stopForeground(STOP_FOREGROUND_REMOVE);super.onDestroy();}
}
