package app.stilldata.guard;

import android.content.*;
import android.graphics.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;

final class MountainView extends View {
 private final Paint paint=new Paint(3);private final Path path=new Path();private final SceneMotion clock=new SceneMotion();
 private final boolean showcase;private final float density;private boolean enabled=true,active=true,dragging;private float drift,downX,downY;private Shader sky;
 MountainView(Context c){this(c,false);}
 MountainView(Context c,boolean showcase){super(c);this.showcase=showcase;density=getResources().getDisplayMetrics().density;setContentDescription(showcase?"Animated mountain landscape with snow, clouds, and a skier. Drag sideways to move the scenery.":"Mountain background");}
 void motion(boolean value){enabled=value;clock.resetClock();invalidate();}
 void active(boolean value){active=value;clock.resetClock();if(value)invalidate();}
 static String pauseReason(Context c,boolean enabled){
  PowerManager power=c.getSystemService(PowerManager.class);float scale=1;
  try{scale=Settings.Global.getFloat(c.getContentResolver(),Settings.Global.ANIMATOR_DURATION_SCALE,1);}catch(Exception ignored){}
  return SceneMotion.pauseReason(enabled,power!=null&&power.isPowerSaveMode(),scale);
 }
 @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){sky=new LinearGradient(0,0,0,h,new int[]{0xff102b40,0xff426477,0xffbea38e},new float[]{0,.65f,1},Shader.TileMode.CLAMP);}
 @Override protected void onAttachedToWindow(){super.onAttachedToWindow();clock.resetClock();invalidate();}
 @Override protected void onDetachedFromWindow(){clock.resetClock();super.onDetachedFromWindow();}
 @Override protected void onWindowVisibilityChanged(int visibility){super.onWindowVisibilityChanged(visibility);clock.resetClock();if(visibility==VISIBLE)invalidate();}
 private void polygon(Canvas canvas,int color,float... points){paint.setColor(color);paint.setShader(null);paint.setStyle(Paint.Style.FILL);path.reset();path.moveTo(points[0],points[1]);for(int i=2;i<points.length;i+=2)path.lineTo(points[i],points[i+1]);path.close();canvas.drawPath(path,paint);}
 @Override protected void onDraw(Canvas canvas){
  float w=getWidth(),h=getHeight();if(w<=0||h<=0)return;
  boolean running=active&&isAttachedToWindow()&&getWindowVisibility()==VISIBLE&&isShown()&&pauseReason(getContext(),enabled).isEmpty();
  float time=clock.frame(SystemClock.uptimeMillis(),running);if(!dragging&&running)drift*=.93f;
  paint.setStyle(Paint.Style.FILL);paint.setShader(sky);canvas.drawRect(0,0,w,h,paint);paint.setShader(null);
  float sway=(float)Math.sin(time*.22f)*density*(showcase?5:2)+drift;
  paint.setColor(0xffedbd98);paint.setAlpha(210);canvas.drawCircle(w*.79f+sway*.12f,h*(showcase?.24f:.21f),w*(showcase?.055f:.085f),paint);paint.setAlpha(255);
  // Three cloud layers drift at different speeds, independently of the snow.
  for(int i=0;i<3;i++){float x=SceneMotion.wrap(i*w*.43f+time*density*(3+i),w+w*.35f)-w*.18f;float y=h*(.19f+i*.105f);paint.setColor(0xffbcd2d3);paint.setAlpha(showcase?65:24);canvas.drawRoundRect(x,y,x+w*.29f,y+density*(showcase?8:11),density*7,density*7,paint);}
  paint.setAlpha(255);canvas.save();canvas.translate(sway*.35f,0);
  polygon(canvas,0xff547083,-w*.1f,h*.7f,w*.27f,h*.29f,w*.51f,h*.6f,w*.74f,h*.34f,w*1.1f,h*.72f,w*1.1f,h,-w*.1f,h);
  polygon(canvas,0xffd2dcd6,w*.27f,h*.29f,w*.16f,h*.43f,w*.25f,h*.40f,w*.29f,h*.44f,w*.33f,h*.39f);
  polygon(canvas,0xffc3d2cf,w*.74f,h*.34f,w*.65f,h*.44f,w*.73f,h*.41f,w*.78f,h*.45f,w*.81f,h*.42f);canvas.restore();
  canvas.save();canvas.translate(sway*.7f,0);polygon(canvas,0xff2d5366,-w*.1f,h*.82f,w*.28f,h*.51f,w*.59f,h*.76f,w*.92f,h*.5f,w*1.1f,h*.67f,w*1.1f,h,-w*.1f,h);canvas.restore();
  polygon(canvas,0xff24495b,0,h*.92f,w*.34f,h*.73f,w*.66f,h*.89f,w,h*.73f,w,h,0,h);
  if(showcase)drawSkier(canvas,w,h,time);else polygon(canvas,0xff143747,0,h*.98f,w*.37f,h*.86f,w*.74f,h*.97f,w,h*.85f,w,h,0,h);
  // Density-scaled flakes are visible on high-density phones such as the S25 Ultra.
  for(int i=0;i<(showcase?42:30);i++){
   float x=SceneMotion.wrap(i*61.37f*density+time*density*(4+i%4)+drift*.5f,w);
   float y=SceneMotion.snowY(i*39.17f*density,time,density*(11+i%5*3),h);
   paint.setColor(0xfff2f3e8);paint.setAlpha(showcase?105+i%5*24:38+i%4*14);canvas.drawCircle(x,y,density*(.8f+i%3*.45f),paint);
  }paint.setAlpha(255);
  if(showcase){paint.setColor(0xfff0f3e9);paint.setTypeface(Typeface.create("sans-serif-medium",0));paint.setTextSize(density*9);canvas.drawText("A MOMENT IN THE MOUNTAINS",density*15,density*23,paint);paint.setTextSize(density*8);paint.setColor(0xffd2dedb);canvas.drawText("Drag gently to move the scene",density*15,h-density*12,paint);}
  if(running)postInvalidateDelayed(33);
 }
 private void drawSkier(Canvas canvas,float w,float h,float time){
  path.reset();path.moveTo(0,h);for(int i=0;i<=24;i++){float x=w*i/24f;path.lineTo(x,slope(x,w,h));}path.lineTo(w,h);path.close();paint.setColor(0xffabc3c3);canvas.drawPath(path,paint);
  float x=SceneMotion.skierX(time,w),y=slope(x,w,h)-density*2;canvas.save();canvas.translate(x,y);float lean=(float)Math.cos(x/w*Math.PI*2)*7;canvas.rotate(lean);
  paint.setColor(0xff284b5d);paint.setStyle(Paint.Style.STROKE);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeWidth(density*1.8f);canvas.drawLine(-density*11,density*3,density*10,density*3,paint);canvas.drawLine(-density*2,0,density*2,-density*7,paint);canvas.drawLine(density*2,-density*7,-density*2,-density*13,paint);
  paint.setColor(0xffedb28f);paint.setStrokeWidth(density*4);canvas.drawLine(-density*2,-density*13,density*1,-density*8,paint);paint.setStyle(Paint.Style.FILL);paint.setColor(0xff203f52);canvas.drawCircle(-density*3,-density*18,density*3,paint);
  paint.setColor(0xffedb28f);path.reset();path.moveTo(-density*4,-density*14);path.lineTo(-density*17,-density*10+(float)Math.sin(time*5)*density*2);path.lineTo(-density*7,-density*12);path.close();canvas.drawPath(path,paint);canvas.restore();paint.setStyle(Paint.Style.FILL);
 }
 private float slope(float x,float w,float h){return h*(.79f+.06f*(float)Math.sin(x/w*Math.PI*2));}
 @Override public boolean onTouchEvent(MotionEvent event){
  if(!showcase)return false;
  if(event.getAction()==MotionEvent.ACTION_DOWN){downX=event.getX();downY=event.getY();dragging=false;}
  if(event.getAction()==MotionEvent.ACTION_MOVE){float dx=event.getX()-downX,dy=event.getY()-downY;if(Math.abs(dx)>density*4&&Math.abs(dx)>Math.abs(dy)){dragging=true;getParent().requestDisallowInterceptTouchEvent(true);}if(dragging){drift=(event.getX()/Math.max(1,getWidth())-.5f)*density*32;invalidate();}}
  if(event.getAction()==MotionEvent.ACTION_UP||event.getAction()==MotionEvent.ACTION_CANCEL){dragging=false;getParent().requestDisallowInterceptTouchEvent(false);performClick();invalidate();}return true;
 }
 @Override public boolean performClick(){super.performClick();return true;}
}
