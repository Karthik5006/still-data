package app.stilldata.guard;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
final class MountainView extends View {
 private final Paint paint=new Paint(3);private final Path path=new Path();private final long start=System.currentTimeMillis();private boolean motion=true;
 MountainView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
 void motion(boolean value){motion=value;invalidate();}
 private void polygon(Canvas c,int color,float... points){paint.setColor(color);paint.setShader(null);path.reset();path.moveTo(points[0],points[1]);for(int i=2;i<points.length;i+=2)path.lineTo(points[i],points[i+1]);path.close();c.drawPath(path,paint);}
 @Override protected void onDraw(Canvas c){
  float w=getWidth(),h=getHeight(),t=(System.currentTimeMillis()-start)/1000f;
  paint.setShader(new LinearGradient(0,0,0,h,new int[]{0xff102433,0xff284c63,0xffa68d83},new float[]{0,.72f,1},Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,paint);paint.setShader(null);
  paint.setColor(0xffe7b494);paint.setAlpha(150);c.drawCircle(w*.79f,h*.21f,w*.085f,paint);paint.setAlpha(255);
  polygon(c,0xff35556a,0,h*.64f,w*.28f,h*.29f,w*.51f,h*.56f,w*.73f,h*.34f,w,h*.64f,w,h,0,h);
  polygon(c,0xff879b9e,w*.28f,h*.29f,w*.17f,h*.43f,w*.26f,h*.40f,w*.30f,h*.44f,w*.34f,h*.39f);
  polygon(c,0xff879b9e,w*.73f,h*.34f,w*.65f,h*.43f,w*.72f,h*.41f,w*.77f,h*.45f,w*.79f,h*.42f);
  polygon(c,0xff244657,0,h*.77f,w*.29f,h*.51f,w*.57f,h*.72f,w*.91f,h*.50f,w,h*.58f,w,h,0,h);
  polygon(c,0xff193b4a,0,h*.83f,w*.35f,h*.76f,w*.61f,h*.86f,w,h*.69f,w,h,0,h);
  polygon(c,0xff102f3c,0,h*.95f,w*.38f,h*.86f,w*.74f,h*.96f,w,h*.85f,w,h,0,h);
  paint.setColor(0xfff2ede4);for(int i=0;i<25;i++){float x=((i*73.83f+(motion?t*(i%3+1)*1.8f:0))%w);float y=((i*127.3f+(motion?t*(i%4+1)*3:0))%h);paint.setAlpha(25+i%5*10);c.drawCircle(x,y,1f+(i%3)*.5f,paint);}paint.setAlpha(255);
  boolean animate=motion&&isShown();PowerManager power=(PowerManager)getContext().getSystemService(Context.POWER_SERVICE);if(power!=null&&power.isPowerSaveMode())animate=false;
  try{if(Settings.Global.getFloat(getContext().getContentResolver(),Settings.Global.ANIMATOR_DURATION_SCALE,1)==0)animate=false;}catch(Exception ignored){}
  if(animate)postInvalidateDelayed(50);
 }
}
