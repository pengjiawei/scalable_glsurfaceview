package pengjiawei.com.opengl_demo;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;

import java.util.Random;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by 91752 on 2018/2/8.
 */

public class MyGLSurfaceView extends GLSurfaceView {
    private final String TAG = "pengjiawei.com.opengl_demo.myGLSurfaceView";
    private ScaleGestureDetector scaleGestureDetector;
    private float mLastX,mLastY;
    private int pointerCount;
    private int NONE = 0,DRAG = 1,ZOOM = 2;
    private int state = NONE;
    private float preScale = 1f;
    private int scaledTouchSlop;
    private MyGLRender myGLRender;
    private float minScale = 1f,maxScale = 12f;
    public MyGLSurfaceView(Context context) {
        super(context);
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setEGLContextClientVersion(1);
//        this.setEGLConfigChooser(new MyConfigChooser());
        myGLRender = new MyGLRender();
        this.setRenderer(myGLRender);
        scaleGestureDetector = new ScaleGestureDetector(context,new scaleListener());
        //tolerance for translate
        scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
    public void updateData(float[] vertexArray){
        myGLRender.setVertexArray(vertexArray);
        refreshView();
    }

    public float getMinScale() {
        return minScale;
    }

    public void setMinScale(float minScale) {
        this.minScale = minScale;
    }

    public float getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(float maxScale) {
        this.maxScale = maxScale;
    }

    public MyGLRender getMyGLRender() {
        return myGLRender;
    }

    public void setMyGLRender(MyGLRender myGLRender) {
        this.myGLRender = myGLRender;
    }

    private void refreshView(){
        this.requestRender();
    }
    //TO-DO maybe it could be better
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x,y;
        if(event.getAction() == MotionEvent.ACTION_DOWN ){
            Log.d("TAG", "onTouch: action down");
            state = DRAG;
        }else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN){
            Log.d("TAG", "onTouch: pointer down");
            state = ZOOM;
        }
        if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP){
            Log.d("TAG", "onTouch: pointer up");
            state = NONE;
        }
        Log.d("TAG", "onTouch: state = "+state);
        if (state == ZOOM){
            scaleGestureDetector.onTouchEvent(event);
            return true;
        }
        if (state == DRAG) {
            x = event.getRawX();
            y = event.getRawY();

            Log.d("TAG", "onTouch: action"+event.getAction());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mLastX = event.getRawX();
                    mLastY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.d("TAG", "ACTION_MOVE");
                    float dx = x - mLastX;
                    //the coordinates of android and opengl are difference
                    float dy = -(y - mLastY);
                    Log.d("TAG", "onTouch: dx dy =" + dx + " " + dy);
                    if (Math.sqrt((dx * dx) + (dy * dy)) >= scaledTouchSlop){
                        Log.d("TAG", "onTouch: scaled touch slop = "+scaledTouchSlop);
                        myGLRender.setTrans(dx / this.getWidth(), dy / this.getHeight());
                        refreshView();
                    }
                    Log.d("TAG", "onTouch: width height = "+this.getWidth()+" "+this.getHeight());
                    mLastX = x;
                    mLastY = y;
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    Log.d("TAG", "onTouch: action cancel");
                    break;
            }
            return true;
        }
        return true;
    }

    public class scaleListener implements ScaleGestureDetector.OnScaleGestureListener{
        float initialSpan,currentSpan;
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            currentSpan = detector.getCurrentSpan();
            Log.d("TAG", "onScale: current init"+currentSpan+" "+initialSpan);
            if (Math.abs(currentSpan - initialSpan) > 10) {
                float currentScale = detector.getScaleFactor() * preScale;
                Log.d("TAG", "onScale: current scale and pre scale = "+currentScale+" "+preScale);
//                float scale = preScale * currentScale;
                if(currentScale < minScale){
                    currentScale = minScale;
                }else if (currentScale > maxScale){
                    currentScale = maxScale;
                }
                myGLRender.setScale(currentScale);
                refreshView();
                preScale = currentScale;
            }
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            initialSpan = detector.getCurrentSpan();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }

    }

    class MyGLRender implements GLSurfaceView.Renderer{
        private float scale = 1;
        private float x = 0f,y = 0f;
        private float[] vertexArray;
        private float pointSize = 1.0f;
        void setScale(float scale_){
            Log.d(TAG,"onscale set scale = "+scale_);
            scale = scale_;
        }
        void setTrans(float x_,float y_){
            x = x + x_;
            y = y + y_;
        }

        public float getPointSize() {
            return pointSize;
        }

        public void setPointSize(float pointSize) {
            this.pointSize = pointSize;
        }

        public float[] getVertexArray() {
            return vertexArray;
        }

        public void setVertexArray(float[] vertexArray) {
            this.vertexArray = vertexArray;
        }

        //坐标维数
        int coor = 3;
        //3-coord x y z

        //创建时候调用，配置环境
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            gl.glClearColor(1.0f,1.0f,1.0f,0.0f);
//        point = new Point();
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        }

        //view的几何形态发生变化时候调用，例如横竖屏切换
        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            //设置窗口大小
            Log.d(TAG, "onSurfaceChanged: width height = "+width+" "+height);
            gl.glViewport(0,0,width,height);
            //设置抗锯齿
//            gl.glEnable(GL10.GL_BLEND);
//            gl.glBlendFunc(GL10.GL_SRC_ALPHA,GL10.GL_ONE_MINUS_SRC_ALPHA);
//            gl.glEnable(GL10.GL_POINT_SMOOTH);
//            gl.glEnable(GL10.GL_NICEST);
        }

        //重新绘制view调用
        @Override
        public void onDrawFrame(GL10 gl) {
            if (vertexArray.length != 0) {
                gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

                //set color of point
                gl.glColor4f(0.63671875f, 0.76953125f, 0.22265625f, 0.0f);
                gl.glPointSize(pointSize * scale);
                gl.glLoadIdentity();

                gl.glEnable(GL10.GL_POINT_SMOOTH);
                gl.glHint(GL10.GL_POINT_SMOOTH,GL10.GL_NICEST);

                Log.d(TAG, "onDrawFrame: scale = "+scale);
                Log.d(TAG, "onDrawFrame: x y = "+x+" "+y);
                gl.glTranslatef(x * 2,y * 2,0.0f);
                gl.glScalef(scale,scale,1f);
                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

                Log.d(TAG, "onDrawFrame: length = "+vertexArray.length);
                //指定顶点
                gl.glVertexPointer(coor,GL10.GL_FLOAT,0,Tool.getFloatBuffer(vertexArray));
                //最后一个参数为顶点的数量
                gl.glDrawArrays(GL10.GL_POINTS,0,vertexArray.length/coor);

                gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            }else {
                Log.e(TAG, "onDrawFrame: vertexArray length = 0");
            }
        }
    }


    class MyConfigChooser implements GLSurfaceView.EGLConfigChooser {
        @Override
        public EGLConfig chooseConfig(EGL10 egl,
                                      javax.microedition.khronos.egl.EGLDisplay display) {

            int attribs[] = {
                    EGL10.EGL_LEVEL, 0,
                    EGL10.EGL_RENDERABLE_TYPE, 4,  // EGL_OPENGL_ES2_BIT
                    EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_DEPTH_SIZE, 16,
                    EGL10.EGL_SAMPLE_BUFFERS, 1,
                    EGL10.EGL_SAMPLES, 4,  // 在这里修改MSAA的倍数，4就是4xMSAA，再往上开程序可能会崩
                    EGL10.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] configCounts = new int[1];
            egl.eglChooseConfig(display, attribs, configs, 1, configCounts);

            if (configCounts[0] == 0) {
                // Failed! Error handling.
                return null;
            } else {
                return configs[0];
            }
        }
    }

}

