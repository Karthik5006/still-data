package app.stilldata.guard;
/** One clock drives the real Canvas scene. Pausing freezes its positions. */
final class SceneMotion {
 private long last=-1;private float seconds;
 static String pauseReason(boolean enabled,boolean batterySaver,float animationScale){return !enabled?"Animations are off":batterySaver?"Paused by Android Battery Saver":animationScale==0?"Paused by Android’s animation setting":"";}
 float frame(long uptime,boolean running){if(last>=0&&running)seconds+=Math.max(0,Math.min(100,uptime-last))/1000f;last=uptime;return seconds;}
 void resetClock(){last=-1;}
 static float wrap(float value,float span){return span<=0?0:((value%span)+span)%span;}
 static float snowY(float seed,float time,float speed,float height){return wrap(seed+time*speed,height);}
 static float skierX(float time,float width){return width*(.12f+wrap(time*.055f,.76f));}
}
