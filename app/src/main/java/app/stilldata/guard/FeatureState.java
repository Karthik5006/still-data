package app.stilldata.guard;
/** Settings controlled by this app; Android Private DNS is deliberately separate. */
final class FeatureState {
 final boolean tracking,firewall,mobile,wifi,motion;
 FeatureState(boolean tracking,boolean firewall,boolean mobile,boolean wifi,boolean motion){this.tracking=tracking;this.firewall=firewall;this.mobile=mobile;this.wifi=wifi;this.motion=motion;}
 static FeatureState all(boolean enabled){return new FeatureState(enabled,enabled,enabled,enabled,enabled);}
 int count(){return (tracking?1:0)+(firewall?1:0)+(mobile?1:0)+(wifi?1:0)+(motion?1:0);}
}
