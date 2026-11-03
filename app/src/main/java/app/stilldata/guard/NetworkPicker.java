package app.stilldata.guard;
import android.net.*;
final class NetworkPicker {
 static Network choose(ConnectivityManager manager){
  Network active=manager.getActiveNetwork();NetworkCapabilities caps=manager.getNetworkCapabilities(active);
  if(caps!=null&&!caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN))return active;
  Network wifi=null,mobile=null,other=null,fallback=null;
  for(Network network:manager.getAllNetworks()){
   NetworkCapabilities c=manager.getNetworkCapabilities(network);
   if(c==null||c.hasTransport(NetworkCapabilities.TRANSPORT_VPN)||!c.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))continue;
   fallback=network;if(!c.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))continue;
   if(c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))wifi=network;else if(c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))mobile=network;else other=network;
  }
  return wifi!=null?wifi:other!=null?other:mobile!=null?mobile:fallback;
 }
}
