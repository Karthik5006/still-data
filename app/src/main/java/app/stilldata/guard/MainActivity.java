package app.stilldata.guard;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.provider.Settings;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

public final class MainActivity extends Activity {
 private static final int INK=0xffedf3ec,MUTED=0xffb6c6c8,MINT=0xffaad8ca,PEACH=0xffeab18d,CARD=0xdd173449;
 private Store store;private MountainView scenery,landscape;private LinearLayout page,nav;private TextView stateLabel,packetLabel;private UsageRepository.Snapshot snapshot=new UsageRepository.Snapshot();
 private final ExecutorService loader=Executors.newSingleThreadExecutor();private Future<?> loadTask;private boolean wifiView;private int tab=0,period=0,loadId=0;private boolean loading,registered,lastArmed;private int appLimit=30;private String search="";private boolean userOnly=false;private LinearLayout appList;
 private final BroadcastReceiver updates=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){if(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(i.getAction())){applyMotion();render();return;}if(stateLabel!=null)stateLabel.setText(store.armed()?store.status():"Firewall is off");if(packetLabel!=null)packetLabel.setText(String.format(Locale.getDefault(),"%,d",store.packets()));if(lastArmed!=store.armed()){lastArmed=store.armed();render();}}};
 @Override public void onCreate(Bundle state){
  super.onCreate(state);store=new Store(this);wifiView=store.prefs.getBoolean("wifi_view",false);lastArmed=store.armed();getWindow().setStatusBarColor(Color.TRANSPARENT);getWindow().setNavigationBarColor(Color.TRANSPARENT);if(Build.VERSION.SDK_INT>=30)getWindow().setDecorFitsSystemWindows(false);
  FrameLayout root=new FrameLayout(this);scenery=new MountainView(this);scenery.motion(store.motion());root.addView(scenery,new FrameLayout.LayoutParams(-1,-1));
  LinearLayout shell=new LinearLayout(this);shell.setOrientation(1);shell.setPadding(dp(20),dp(20),dp(20),dp(12));root.addView(shell,new FrameLayout.LayoutParams(-1,-1));
  root.setOnApplyWindowInsetsListener((v,insets)->{int left,top,right,bottom;if(Build.VERSION.SDK_INT>=30){android.graphics.Insets i=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());left=i.left;top=i.top;right=i.right;bottom=i.bottom;}else{left=insets.getSystemWindowInsetLeft();top=insets.getSystemWindowInsetTop();right=insets.getSystemWindowInsetRight();bottom=insets.getSystemWindowInsetBottom();}shell.setPadding(left+dp(20),top+dp(12),right+dp(20),bottom+dp(8));return insets;});
  LinearLayout header=row();header.setPadding(dp(4),0,dp(4),dp(20));TextView brand=text("still",29,INK);brand.setTypeface(Typeface.create("sans-serif-light",0));brand.setLetterSpacing(.05f);header.addView(brand,new LinearLayout.LayoutParams(0,-2,1));TextView badge=text("DATA, AT YOUR PACE",9,MINT);badge.setLetterSpacing(.16f);header.addView(badge);shell.addView(header);
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);scroll.setClipToPadding(false);scroll.setVerticalScrollBarEnabled(false);page=new LinearLayout(this);page.setOrientation(1);page.setPadding(0,0,0,dp(18));scroll.addView(page);shell.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));nav=row();nav.setPadding(dp(2),dp(8),dp(2),0);shell.addView(nav);setContentView(root);render();
 }
 @Override protected void onStart(){super.onStart();IntentFilter filter=new IntentFilter(FirewallService.ACTION_CHANGED);filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);if(Build.VERSION.SDK_INT>=33)registerReceiver(updates,filter,Context.RECEIVER_NOT_EXPORTED);else registerReceiver(updates,filter);registered=true;}
 @Override protected void onResume(){super.onResume();applyMotion();scenery.active(true);if(landscape!=null)landscape.active(true);if(store.armed()&&android.net.VpnService.prepare(this)==null)restartFirewall();refresh();}
 @Override protected void onStop(){scenery.active(false);if(landscape!=null)landscape.active(false);if(registered){unregisterReceiver(updates);registered=false;}super.onStop();}
 @Override protected void onDestroy(){loader.shutdownNow();super.onDestroy();}
 private void refresh(){
  final int request=++loadId,selected=period;final boolean tracking=store.tracking();
  if(loadTask!=null)loadTask.cancel(true);loading=true;render();
  loadTask=loader.submit(()->{UsageRepository.Snapshot result=UsageRepository.load(getApplicationContext(),selected,store.cycleDay(),tracking);runOnUiThread(()->{if(isFinishing()||isDestroyed()||request!=loadId)return;snapshot=result;loading=false;render();});});
 }
 private void render(){if(page==null)return;page.animate().cancel();page.setAlpha(1);page.setTranslationY(0);page.removeAllViews();landscape=null;stateLabel=null;packetLabel=null;if(tab==0)overview();if(tab==1)apps();if(tab==2)guard();if(tab==3)settings();navigation();}
 private void applyMotion(){scenery.motion(store.motion());if(landscape!=null)landscape.motion(store.motion());}
 private void transition(){if(MountainView.pauseReason(this,store.motion()).isEmpty()){page.setAlpha(.4f);page.setTranslationY(dp(8));page.animate().alpha(1).translationY(0).setDuration(220).start();}}
 private void networkTabs(LinearLayout parent){
  LinearLayout tabs=row();String[] titles={"Mobile data","Wi-Fi"};for(int i=0;i<2;i++){final boolean selected=i==1;TextView chip=text(titles[i],13,wifiView==selected?0xff163343:MUTED);chip.setGravity(Gravity.CENTER);chip.setMinHeight(dp(46));chip.setBackground(new RippleDrawable(ColorStateList.valueOf(0x335fb7a2),round(wifiView==selected?MINT:0xaa214454,18),null));LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,dp(46),1);params.setMargins(0,0,i==0?dp(8):0,0);tabs.addView(chip,params);chip.setOnClickListener(v->{wifiView=selected;store.prefs.edit().putBoolean("wifi_view",selected).apply();appLimit=30;render();transition();});}add(parent,tabs,0,0,0,14);
 }
 private void featureControls(LinearLayout parent,boolean expanded){
  LinearLayout controls=card(parent);label(controls,"FEATURE CONTROLS");controls.addView(text("Everything, at your pace.",20,INK));note(controls,store.features().count()+" of 5 local features enabled. Enable all restores your saved rules; it does not block every app.");
  buttonPair(controls,"Enable all",()->setAllFeatures(true),"Disable all",()->setAllFeatures(false));
  if(expanded){
   access(controls,"Usage tracking",store.tracking(),on->{store.tracking(on);refresh();});
   access(controls,"App firewall",store.armed(),on->{if(on)requestFirewall();else disableFirewall();});
   access(controls,"Mobile rules",store.networkEnabled(false),on->{store.networkEnabled(false,on);rulesChanged();render();});
   access(controls,"Wi-Fi rules",store.networkEnabled(true),on->{store.networkEnabled(true,on);rulesChanged();render();});
   access(controls,"Animations",store.motion(),on->{store.motion(on);applyMotion();render();});
  }
  note(controls,"Private DNS is controlled by Android. Enable or disable its ad blocking separately in Guard.");
 }
 private void setAllFeatures(boolean enabled){
  store.allFeatures(enabled);applyMotion();
  if(enabled){store.armed(false);requestFirewall();if(!UsageRepository.hasAccess(this))toast("Allow Usage Access to see totals. Your other features are enabled.");}
  else{stopService(new Intent(this,FirewallService.class));store.status("Off");lastArmed=false;}
  refresh();
 }
 private void networkControls(LinearLayout parent,boolean wifi){
  String title=wifi?"Wi-Fi":"Mobile data";LinearLayout box=card(parent);label(box,wifi?"WI-FI CONTROLS":"MOBILE DATA CONTROLS");box.addView(text(title+" app access",20,INK));
  access(box,"Apply "+title.toLowerCase(Locale.ROOT)+" rules",store.networkEnabled(wifi),on->{store.networkEnabled(wifi,on);rulesChanged();render();});
  access(box,"Block every app on "+title,store.blockAll(wifi),on->{store.blockAll(wifi,on);if(on)store.networkEnabled(wifi,true);rulesChanged();render();});
  buttonPair(box,"Block all",()->{store.blockAll(wifi,true);store.networkEnabled(wifi,true);if(store.armed())rulesChanged();else requestFirewall();render();},"Allow all",()->{store.allowAll(wifi);rulesChanged();render();});
  note(box,!store.networkEnabled(wifi)?title+" rules are paused; saved app choices will resume when you enable them.":store.blockAll(wifi)?"Every app is blocked on "+title+" while the firewall runs. Turn off the all-app switch to use individual rules.":"Individual "+title+" app choices apply while the firewall runs. The other network has its own controls.");
 }
 private void navigation(){nav.removeAllViews();String[] labels={"Overview","Apps","Guard","Settings"};for(int i=0;i<4;i++){final int target=i;TextView item=text(labels[i],12,i==tab?MINT:MUTED);item.setGravity(Gravity.CENTER);item.setPadding(dp(4),dp(16),dp(4),dp(16));item.setBackground(round(i==tab?0xaa214b58:Color.TRANSPARENT,18));item.setOnClickListener(v->{tab=target;search="";render();transition();});LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(50),1);p.setMargins(dp(2),0,dp(2),0);nav.addView(item,p);}}
 private void overview(){
  label(page,"A QUIETER CONNECTION");TextView title=text("Stay connected.\nSpend less.",36,INK);title.setTypeface(Typeface.create("sans-serif-light",0));add(page,title,0,7,0,16);
  FrameLayout frame=new FrameLayout(this);frame.setBackground(round(0xff173449,22));frame.setClipToOutline(true);landscape=new MountainView(this,true);landscape.motion(store.motion());frame.addView(landscape,new FrameLayout.LayoutParams(-1,-1));LinearLayout.LayoutParams sceneSize=new LinearLayout.LayoutParams(-1,dp(180));sceneSize.setMargins(0,0,0,dp(8));page.addView(frame,sceneSize);
  String reason=MountainView.pauseReason(this,store.motion());note(page,reason.isEmpty()?"Live landscape · snow, clouds & a skier. Drag to explore.":reason+". Check animation controls in Settings.");
  featureControls(page,false);networkTabs(page);
  LinearLayout chips=row();String[] periods={"Today","7 days","Billing cycle"};for(int i=0;i<3;i++){final int which=i;TextView chip=text(periods[i],11,period==i?0xff163343:MUTED);chip.setGravity(Gravity.CENTER);chip.setBackground(round(period==i?MINT:0x66173449,24));LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,dp(40),1);params.setMargins(0,0,dp(7),0);chips.addView(chip,params);chip.setOnClickListener(v->{period=which;refresh();});}add(page,chips,0,0,0,16);
  long selectedUsage=wifiView?snapshot.wifi:snapshot.mobile;boolean available=store.tracking()&&snapshot.allowed&&snapshot.period==period&&selectedUsage>=0;
  LinearLayout usage=card(page);label(usage,wifiView?"WI-FI DATA USED":"MOBILE DATA USED");TextView total=text(!store.tracking()?"Tracking paused":available?UsageWindows.bytes(selectedUsage):loading?"Reading…":"See your usage",36,INK);total.setTypeface(Typeface.create("sans-serif-light",0));add(usage,total,0,10,0,5);
  note(usage,!store.tracking()?"Still is not reading usage. Android continues its own system counters.":available?periodName()+" · "+(wifiView?"Mobile  "+UsageWindows.bytes(snapshot.mobile):"Wi-Fi  "+UsageWindows.bytes(snapshot.wifi)):snapshot.allowed?"Waiting for Android’s usage totals.":"Android Usage Access unlocks real usage totals.");
  if(!store.tracking())button(usage,"Enable usage tracking",()->{store.tracking(true);refresh();},true);else if(!snapshot.allowed)button(usage,"Allow usage access",()->usageAccess(),true);else button(usage,loading?"Refreshing…":"Refresh usage",()->{if(!loading)refresh();},false);
  if(store.tracking()&&!snapshot.error.isEmpty())note(usage,snapshot.error);
  LinearLayout chart=card(page);label(chart,(wifiView?"WI-FI":"MOBILE DATA")+" · LAST 7 DAYS");Chart graph=new Chart(this,wifiView?snapshot.dailyWifi:snapshot.daily,available,wifiView);add(chart,graph,0,14,0,4);note(chart,"Tap a bar for that day’s total. Android counters can lag behind carrier billing.");
  LinearLayout protection=card(page);protection.addView(text("Your connection, protected.",18,INK));stateLabel=text(store.armed()?store.status():"Firewall is off",12,MINT);add(protection,stateLabel,0,10,0,2);button(protection,store.armed()?"Pause firewall":"Enable firewall",()->toggleFirewall(),!store.armed());
  LinearLayout counts=row();LinearLayout a=column();packetLabel=text(String.format(Locale.getDefault(),"%,d",store.packets()),26,PEACH);a.addView(packetLabel);note(a,"Packets stopped");counts.addView(a,new LinearLayout.LayoutParams(0,-2,1));LinearLayout b=column();b.addView(text(String.valueOf(store.blocked(false).size()+store.blocked(true).size()),26,MINT));note(b,"Saved app rules");counts.addView(b,new LinearLayout.LayoutParams(0,-2,1));add(protection,counts,0,16,0,0);
  LinearLayout budget=card(page);label(budget,"MOBILE BUDGET · THIS BILLING CYCLE");long cap=store.budget();if(store.tracking()&&cap>0&&snapshot.cycleMobile>=0){budget.addView(text(UsageWindows.bytes(snapshot.cycleMobile)+" / "+UsageWindows.bytes(cap),23,INK));ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(1000);progress.setProgress((int)Math.min(1000,(double)snapshot.cycleMobile/cap*1000));progress.setProgressTintList(ColorStateList.valueOf(snapshot.cycleMobile>=cap?PEACH:MINT));add(budget,progress,0,14,0,7);note(budget,snapshot.cycleMobile>=cap?"Your budget is reached. Block all mobile apps to pause access.":UsageWindows.bytes(Math.max(0,cap-snapshot.cycleMobile))+" left in your budget.");}else note(budget,store.tracking()?"Set a monthly mobile budget to keep your usage in perspective.":"Enable usage tracking to compare your budget with Android’s totals.");button(budget,"Set data budget",()->budgetDialog(),false);
 }
 private void apps(){
  label(page,"SMALL CHANGES. LESS DATA.");heading(page,"Every app, in view.");networkTabs(page);networkControls(page,wifiView);note(page,"Numbers show "+periodName().toLowerCase(Locale.ROOT)+". Turn an access switch off to block that app on the selected network.");
  if(!store.tracking())button(page,"Resume usage tracking",()->{store.tracking(true);refresh();},true);else if(!snapshot.allowed)button(page,"Enable per-app usage totals",()->usageAccess(),true);
  if(!store.armed())note(page,"Your rules are saved. Enable the firewall to enforce them.");
  EditText find=input("Find an app or package",false);find.setSingleLine(true);find.setText(search);add(page,find,0,16,0,12);
  LinearLayout filters=row();TextView all=text(userOnly?"All apps":"All apps ✓",12,userOnly?MUTED:MINT),user=text(userOnly?"User apps ✓":"User apps",12,userOnly?MINT:MUTED);all.setPadding(dp(4),dp(12),dp(22),dp(12));user.setPadding(dp(4),dp(12),dp(4),dp(12));filters.addView(all);filters.addView(user);page.addView(filters);all.setOnClickListener(v->{userOnly=false;render();});user.setOnClickListener(v->{userOnly=true;render();});
  appList=column();page.addView(appList);populateApps();find.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int count,int after){}public void onTextChanged(CharSequence s,int st,int before,int count){search=s.toString();appLimit=30;populateApps();}public void afterTextChanged(Editable e){}});
 }
 private void populateApps(){
  if(appList==null)return;appList.removeAllViews();Set<String> mobile=store.blocked(false),wifi=store.blocked(true);String query=search.toLowerCase(Locale.ROOT);int count=0;
  List<UsageRepository.App> sorted=new ArrayList<>(snapshot.apps);Collections.sort(sorted,(a,b)->{int usage=Long.compare(wifiView?b.wifi:b.mobile,wifiView?a.wifi:a.mobile);return usage!=0?usage:a.label.compareToIgnoreCase(b.label);});
  for(UsageRepository.App app:sorted){if(userOnly&&app.system)continue;boolean match=app.label.toLowerCase(Locale.ROOT).contains(query);if(!match)for(String p:app.packages)if(p.toLowerCase(Locale.ROOT).contains(query))match=true;if(!match)continue;count++;if(count>appLimit)continue;
   LinearLayout card=card(appList);LinearLayout nameRow=row();ImageView icon=new ImageView(this);try{if(app.iconInfo!=null)icon.setImageDrawable(app.iconInfo.loadIcon(getPackageManager()));else icon.setImageResource(R.drawable.ic_still);}catch(Exception e){icon.setImageResource(R.drawable.ic_still);}LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(35),dp(35));ip.setMargins(0,0,dp(12),0);nameRow.addView(icon,ip);LinearLayout names=column();TextView label=text(app.label,15,INK);names.addView(label);note(names,app.packages.size()>1?app.packages.size()+" packages · shared UID "+app.uid:app.packages.isEmpty()?"Android usage record":app.packages.get(0));nameRow.addView(names,new LinearLayout.LayoutParams(0,-2,1));card.addView(nameRow);
   TextView totals=text(!store.tracking()?"Usage tracking paused":snapshot.allowed&&snapshot.period==period&&snapshot.mobile>=0?(wifiView?"Wi-Fi  "+UsageWindows.bytes(app.wifi)+"     Mobile  "+UsageWindows.bytes(app.mobile):"Mobile  "+UsageWindows.bytes(app.mobile)+"     Wi-Fi  "+UsageWindows.bytes(app.wifi)):loading?"Refreshing usage…":"Usage Access needed for totals",12,MINT);add(card,totals,0,13,0,9);
   if(app.packages.isEmpty())note(card,"Historical Android totals. There is no installed package to block.");else{
    boolean blocked=intersects(wifiView?wifi:mobile,app.packages),all=store.blockAll(wifiView);
    Switch appAccess=access(card,wifiView?"Wi-Fi access":"Mobile data access",!all&&!blocked,allowed->{store.block(app.packages,wifiView,!allowed);rulesChanged();});appAccess.setEnabled(!all);
    if(all)note(card,"Blocked by the all-app rule above.");
    if(app.packages.size()>1)note(card,"These packages share an Android UID, so their usage and blocking are grouped.");
   }
  }
  if(count>appLimit)button(appList,"Show more apps ("+count+" total)",()->{appLimit+=30;populateApps();},false);
  if(count==0)note(appList,loading?"Reading installed apps…":"No matching apps.");else note(appList,count+" app groups · Still groups shared Android UIDs to avoid counting the same data twice.");
 }
 private void guard(){
  label(page,"LET YOUR DATA BREATHE");heading(page,"A little more control.");
  LinearLayout firewall=card(page);firewall.addView(text("Local app firewall",21,INK));stateLabel=text(store.armed()?store.status():"Firewall is off",12,MINT);add(firewall,stateLabel,0,10,0,8);note(firewall,"Selected apps are sent into a local VPN and their traffic is dropped. Other apps keep their connection. Nothing is uploaded or inspected.");button(firewall,store.armed()?"Pause firewall":"Enable firewall",()->toggleFirewall(),!store.armed());note(firewall,"Android allows one VPN at a time. This replaces another VPN while enabled.");
  featureControls(page,true);networkControls(page,false);networkControls(page,true);
  LinearLayout dns=card(page);label(dns,"SYSTEM AD & TRACKER BLOCKING");dns.addView(text("Fewer distractions.",22,INK));DnsState status=dnsState();TextView state=text(status.message,12,status.active?MINT:PEACH);add(dns,state,0,10,0,10);note(dns,"Android Private DNS can filter ad and tracker domains across apps and browsers. It works alongside the local firewall. YouTube and other first-party ads may remain.");TextView host=text(store.dnsHost(),16,MINT);host.setTextIsSelectable(true);add(dns,host,0,15,0,5);buttonPair(dns,"Enable ads filter",()->dnsSetup(),"Disable ads filter",()->dnsDisable());button(dns,"Check DNS status",()->render(),false);note(dns,"Domain lookups go to your chosen DNS provider. Still does not receive them. Apps using their own encrypted DNS can bypass this filter.");
  LinearLayout android=card(page);android.addView(text("Android Data Saver",20,INK));note(android,"Let Android restrict background mobile data too. On Samsung, check your allowed apps and exceptions in system settings.");button(android,"Open system Data Saver",()->openSettings(new Intent("android.settings.DATA_SAVER_SETTINGS")),false);
 }
 private void settings(){
  label(page,"MAKE IT YOUR OWN");heading(page,"Room for a little calm.");
  featureControls(page,true);
  LinearLayout plan=card(page);plan.addView(text("Your mobile plan",20,INK));note(plan,store.budget()>0?UsageWindows.bytes(store.budget())+" budget · cycle starts on day "+store.cycleDay():"No data budget set · cycle starts on day "+store.cycleDay());button(plan,"Edit budget & billing day",()->budgetDialog(),false);
  LinearLayout appearance=card(page);appearance.addView(text("The quiet details",20,INK));access(appearance,"Ambient snow & motion",store.motion(),on->{store.motion(on);applyMotion();render();});String motionReason=MountainView.pauseReason(this,store.motion());note(appearance,motionReason.isEmpty()?"Live snowfall, moving clouds, a skier, and gentle screen transitions are enabled. See the landscape on Overview.":motionReason+". On Samsung, check Settings → Accessibility → Vision enhancements → Remove animations, and Power saving.");
  LinearLayout access=card(page);access.addView(text("Permissions",20,INK));note(access,"Usage Access: "+(UsageRepository.hasAccess(this)?"allowed":"not allowed"));button(access,"Open Usage Access",()->usageAccess(),false);button(access,"App & battery settings",()->openSettings(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName()))),false);note(access,"If Samsung puts Still to sleep, add it to Never auto sleeping apps in Battery settings. Keep the ongoing notification while blocking is enabled.");
  LinearLayout privacy=card(page);privacy.addView(text("Your data stays yours.",20,INK));note(privacy,"Still has no Internet permission, accounts, analytics, or remote server. It stores rules, your budget, and aggregate dropped-packet counters on this phone. Usage totals come from Android. It never saves packet contents or browsing history.");note(privacy,"Private DNS is a separate Android service. Your chosen DNS provider processes domain lookups. This build supports AdGuard DNS and Control D’s public ad-blocking DNS.");button(privacy,"Choose DNS provider",()->new AlertDialog.Builder(this).setTitle("Ad-blocking DNS provider").setItems(new String[]{"AdGuard · dns.adguard-dns.com","Control D · p2.freedns.controld.com"},(d,which)->{store.dnsHost(which==0?"dns.adguard-dns.com":"p2.freedns.controld.com");render();}).setNegativeButton("Cancel",null).show(),false);
  LinearLayout about=card(page);about.addView(text("Still Data 1.1",18,INK));note(about,"Designed for Android 10 and later, including Galaxy S25 Ultra. Android usage counters can lag and may differ from your carrier. Dropped packets include retries and are not an exact measure of data saved. A budget is a visual guide, not an automatic cutoff.");
 }
 private String periodName(){return period==0?"Today":period==1?"Last 7 days":"Current billing cycle";}
 private void rulesChanged(){if(store.armed())restartFirewall();else toast("Rule saved. Enable the firewall in Guard to apply it.");}
 private void toggleFirewall(){if(store.armed())disableFirewall();else requestFirewall();}
 private void disableFirewall(){store.armed(false);stopService(new Intent(this,FirewallService.class));store.status("Off");lastArmed=false;render();}
 private void requestFirewall(){Intent consent=android.net.VpnService.prepare(this);if(consent!=null){store.armed(false);startActivityForResult(consent,80);}else enableFirewall();}
 private void enableFirewall(){store.armed(true);store.status("Starting firewall…");lastArmed=true;restartFirewall();if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},81);render();}
 private void restartFirewall(){try{startForegroundService(new Intent(this,FirewallService.class).setAction(FirewallService.ACTION_START));}catch(Exception e){store.armed(false);store.status("Could not start. Try enabling the firewall again.");toast("Android could not start the firewall. Try again from Guard.");}}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==80){if(result==RESULT_OK)enableFirewall();else toast("VPN permission was not granted. Blocking remains off.");}}
 private void usageAccess(){try{startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS,Uri.parse("package:"+getPackageName())));}catch(ActivityNotFoundException e){openSettings(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));}}
 private void openSettings(Intent intent){try{startActivity(intent);}catch(Exception e){try{startActivity(new Intent(Settings.ACTION_SETTINGS));}catch(Exception ignored){toast("Open Android Settings manually.");}}}
 private void dnsSetup(){String host=store.dnsHost();new AlertDialog.Builder(this).setTitle("Set up system ad blocking").setMessage("1. Copy this hostname:\n\n"+host+"\n\n2. Open Private DNS settings.\n3. Choose Private DNS provider hostname.\n4. Paste the hostname and tap Save.\n\nReturn to Guard to check the active connection. Tap Check DNS status after saving. The provider must be connected for filtering to work.") .setPositiveButton("Copy & open settings",(d,w)->{android.content.ClipboardManager clip=getSystemService(android.content.ClipboardManager.class);clip.setPrimaryClip(ClipData.newPlainText("Private DNS provider",host));openSettings(new Intent("android.settings.PRIVATE_DNS_SETTINGS"));}).setNegativeButton("Cancel",null).show();}
 private void dnsDisable(){new AlertDialog.Builder(this).setTitle("Disable system ad blocking").setMessage("Android controls Private DNS, so Still cannot switch it off silently.\n\nOpen Private DNS settings, choose Automatic (or Off), and tap Save. Return to Guard and check DNS status.\n\nThis does not change your separate Wi-Fi or mobile app rules.").setPositiveButton("Open Private DNS",(d,w)->openSettings(new Intent("android.settings.PRIVATE_DNS_SETTINGS"))).setNegativeButton("Cancel",null).show();}
 private static final class DnsState {boolean active;String message="Private DNS is not verified on this connection.";}
 private DnsState dnsState(){
  DnsState result=new DnsState();ConnectivityManager cm=getSystemService(ConnectivityManager.class);Network current=NetworkPicker.choose(cm);
  if(current==null)return result;LinkProperties properties=cm.getLinkProperties(current);
  if(properties!=null&&properties.isPrivateDnsActive()){
   String host=properties.getPrivateDnsServerName();
   if(host!=null&&host.equalsIgnoreCase(store.dnsHost())){result.active=true;result.message="Private DNS active · "+host;}
   else result.message=host==null?"Encrypted DNS active · ad filtering unverified":"A different Private DNS provider is active: "+host;
  }return result;
 }
 private void budgetDialog(){LinearLayout form=column();form.setPadding(dp(24),dp(10),dp(24),0);TextView hint=text("Budget in GB (0 means no budget)",12,MUTED);form.addView(hint);EditText amount=input("For example, 5",true);amount.setText(store.budget()>0?String.format(Locale.US,"%.2f",store.budget()/1073741824d):"0");form.addView(amount);add(form,text("Billing cycle starts on day 1–28",12,MUTED),0,16,0,5);EditText day=input("1",true);day.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);day.setText(String.valueOf(store.cycleDay()));form.addView(day);AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Your data budget").setView(form).setPositiveButton("Save",null).setNegativeButton("Cancel",null).create();dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{try{double gb=Double.parseDouble(amount.getText().toString());int start=Integer.parseInt(day.getText().toString());if(!Double.isFinite(gb)||gb<0||gb>100000||start<1||start>28)throw new NumberFormatException();store.budget((long)(gb*1073741824d),start);dialog.dismiss();refresh();}catch(Exception e){day.setError("Use day 1–28 and a non-negative GB budget.");}}));dialog.show();}
 private interface Changed {void set(boolean b);}
 private Switch access(LinearLayout parent,String title,boolean checked,Changed change){LinearLayout line=row();line.setMinimumHeight(dp(49));TextView label=text(title,13,INK);line.addView(label,new LinearLayout.LayoutParams(0,-2,1));Switch control=new Switch(this);control.setContentDescription(title);control.setChecked(checked);control.setThumbTintList(ColorStateList.valueOf(MINT));control.setTrackTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{0xff598a82,0xff466171}));control.setOnCheckedChangeListener((v,on)->change.set(on));line.addView(control);parent.addView(line);return control;}
 private TextView action(String title,Runnable action,boolean primary){TextView b=text(title,13,primary?0xff163343:MINT);b.setGravity(Gravity.CENTER);b.setTypeface(Typeface.create("sans-serif-medium",0));b.setPadding(dp(12),dp(13),dp(12),dp(13));b.setMinHeight(dp(48));b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x335fb7a2),round(primary?MINT:0x992f5967,15),null));b.setOnClickListener(v->action.run());return b;}
 private void button(LinearLayout parent,String title,Runnable action,boolean primary){add(parent,action(title,action,primary),0,15,0,0);}
 private void buttonPair(LinearLayout parent,String first,Runnable one,String second,Runnable two){LinearLayout pair=row();LinearLayout.LayoutParams left=new LinearLayout.LayoutParams(0,-2,1);left.setMargins(0,0,dp(8),0);pair.addView(action(first,one,true),left);pair.addView(action(second,two,false),new LinearLayout.LayoutParams(0,-2,1));add(parent,pair,0,15,0,4);}
 private void heading(LinearLayout parent,String s){TextView view=text(s,30,INK);view.setTypeface(Typeface.create("sans-serif-light",0));add(parent,view,0,10,0,16);}
 private void label(LinearLayout parent,String s){TextView t=text(s,10,MINT);t.setLetterSpacing(.15f);add(parent,t,0,0,0,3);}
 private void note(LinearLayout parent,String s){TextView t=text(s,11,MUTED);t.setLineSpacing(dp(3),1);add(parent,t,0,7,0,0);}
 private LinearLayout card(LinearLayout parent){LinearLayout card=column();card.setPadding(dp(20),dp(20),dp(20),dp(20));card.setBackground(round(CARD,23));add(parent,card,0,0,0,14);return card;}
 private LinearLayout column(){LinearLayout v=new LinearLayout(this);v.setOrientation(1);return v;}
 private LinearLayout row(){LinearLayout v=new LinearLayout(this);v.setOrientation(0);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
 private TextView text(String s,int size,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setFontFeatureSettings("kern");return v;}
 private EditText input(String hint,boolean number){EditText e=new EditText(this);e.setTextColor(INK);e.setHintTextColor(MUTED);e.setTextSize(14);e.setHint(hint);e.setPadding(dp(14),dp(12),dp(14),dp(12));e.setBackground(round(0xdd234456,12));e.setSingleLine(true);if(number)e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);return e;}
 private Drawable round(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));d.setStroke(dp(1),0x225f8392);return d;}
 private void add(LinearLayout parent,View child,int left,int top,int right,int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(left),dp(top),dp(right),dp(bottom));if(child instanceof Chart)p.height=dp(110);parent.addView(child,p);}
 private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
 private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
 private static boolean intersects(Set<String> a,Collection<String> b){for(String p:b)if(a.contains(p))return true;return false;}
 private static final class Chart extends View {
  private final long[] values;private final boolean ready,wifi;private final Paint paint=new Paint(3);
  Chart(Context c,long[] values,boolean ready,boolean wifi){super(c);this.values=values.clone();this.ready=ready;this.wifi=wifi;setContentDescription(ready?"Actual "+(wifi?"Wi-Fi":"mobile")+" data for the last seven days":"Enable usage tracking and Usage Access to load the last seven days");}
  @Override public boolean onTouchEvent(android.view.MotionEvent event){if(event.getAction()==android.view.MotionEvent.ACTION_UP){performClick();if(ready){int day=Math.max(0,Math.min(6,(int)(event.getX()/Math.max(1,getWidth())*7)));java.time.LocalDate date=java.time.LocalDate.now().minusDays(6-day);Toast.makeText(getContext(),date.toString()+" · "+UsageWindows.bytes(values[day])+(wifi?" Wi-Fi data":" mobile data"),Toast.LENGTH_SHORT).show();}return true;}return true;}
  @Override public boolean performClick(){super.performClick();return true;}
  @Override protected void onDraw(Canvas canvas){float w=getWidth(),h=getHeight(),density=getResources().getDisplayMetrics().density;paint.setTextSize(9*density);paint.setTextAlign(Paint.Align.CENTER);long max=1;for(long value:values)max=Math.max(max,value);for(int i=0;i<7;i++){float unit=w/7,x=unit*i+unit*.23f,height=ready?(h-25*density)*values[i]/max:0;paint.setColor(0xff385e6b);canvas.drawRoundRect(x,0,x+unit*.54f,h-23*density,6*density,6*density,paint);if(height>0){paint.setColor(i==6?PEACH:MINT);canvas.drawRoundRect(x,h-23*density-height,x+unit*.54f,h-23*density,6*density,6*density,paint);}paint.setColor(MUTED);java.time.DayOfWeek day=java.time.LocalDate.now().minusDays(6-i).getDayOfWeek();String label=day.getDisplayName(java.time.format.TextStyle.SHORT,Locale.getDefault());canvas.drawText(label,x+unit*.27f,h-3*density,paint);}}
 }
}
