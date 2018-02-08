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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by 91752 on 2018/2/8.
 */

public class myGLSurfaceView extends GLSurfaceView {
    private final String TAG = "pengjiawei.com.opengl_demo.myGLSurfaceView";
    private ScaleGestureDetector scaleGestureDetector;
    float mLastX,mLastY;
    int pointerCount;
    int NONE = 0,DRAG = 1,ZOOM = 2;
    int state = NONE;
    float preScale = 1f;
    int scaledTouchSlop;
    private MyGLRender myGLRender;
    private float minScale = 0.25f,maxScale = 4f;
    public myGLSurfaceView(Context context) {
        super(context);

    }

    public myGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setEGLContextClientVersion(1);
        myGLRender = new MyGLRender();
        this.setRenderer(myGLRender);
        scaleGestureDetector = new ScaleGestureDetector(context,new scaleListener());
        //tolerance for translate
        scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
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
        void setScale(float scale_){
            Log.d(TAG,"onscale set scale = "+scale_);
            scale = scale_;
        }
        void setTrans(float x_,float y_){
            x = x + x_;
            y = y + y_;
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
        private float[] vertexArray;
        //test array
        void initVertexArray(int size){
            vertexArray = new float[size * 3 * 2];
            Random random = new Random();
            int index = 0;
            float x_add = 0.01f;
            for (int i = 0;i < size ; ++i){
                float v = random.nextFloat();
                vertexArray[index] = x_add;
                x_add += 0.01f;
                ++index;
                v = random.nextFloat();
                vertexArray[index] = 0.0f;
                ++index;
                vertexArray[index] = 0.0f;
                ++index;
            }
            float y_add = 0.01f;
            for (int i = 0;i < size ; ++i){
                float v = random.nextFloat();
                vertexArray[index] = 0.0f;
                y_add += 0.01f;
                ++index;
                v = random.nextFloat();
                vertexArray[index] = y_add;
                ++index;
                vertexArray[index] = 0.0f;
                ++index;
            }

        }
        //创建时候调用，配置环境
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            gl.glClearColor(1.0f,1.0f,1.0f,0.0f);
//        point = new Point();
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            initVertexArray(100);
        }

        //view的几何形态发生变化时候调用，例如横竖屏切换
        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            //设置窗口大小
            gl.glViewport(0,0,width,height);
        }

        //重新绘制view调用
        @Override
        public void onDrawFrame(GL10 gl) {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            gl.glColor4f(0.63671875f, 0.76953125f, 0.22265625f, 0.0f);
            gl.glPointSize(10f * scale);
            gl.glLoadIdentity();

            Log.d(TAG, "onDrawFrame: scale = "+scale);
            Log.d(TAG, "onDrawFrame: x y = "+x+" "+y);
            gl.glTranslatef(x * 2,y * 2,0.0f);
            gl.glScalef(scale,scale,1f);
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

            Log.d(TAG, "onDrawFrame: length = "+vertexArray.length);
            //指定顶点
            gl.glVertexPointer(coor,GL10.GL_FLOAT,0,Tool.getFloatBuffer(vertexArray));
            //最后一个参数为顶点的数量
            gl.glDrawArrays(GL10.GL_POINTS,0,vertexArray.length/3);

            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        }
    }

}

