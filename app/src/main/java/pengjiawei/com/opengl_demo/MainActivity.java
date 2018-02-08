package pengjiawei.com.opengl_demo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.util.Currency;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private MyGLRender myGLRender;
    float currentScale = -1f;
    float mLastX,mLastY;
    int pointerCount;
    int state = 0,DRAG = 1,ZOOM = 2;
    float preScale = 1f;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
        gestureDetector = new GestureDetector(this,new gestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this,new scaleListener());
        Log.d("Debug", "onCreate: supported ?"+supportsEs2);
//        gestureDetector = new GestureDetector
        glSurfaceView = findViewById(R.id.gl_view);
        glSurfaceView.setEGLContextClientVersion(1);
        myGLRender = new MyGLRender();
        glSurfaceView.setRenderer(myGLRender);

//        Tool.measureView(glSurfaceView);
//        Log.d("TAG", "onCreate: view width height = "+glSurfaceView.getMeasuredWidth()+" "+glSurfaceView.getMeasuredHeight());

        //大于这个距离才可以平移
        final int scaledTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        //调用requestRender() 才需要重绘
//        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 拿到触摸点的个数
                pointerCount = event.getPointerCount();
                // 得到多个触摸点的x与y均值
                Log.d("TAG", "onTouch: point count = "+pointerCount);
                float x = 0, y = 0;
//                if (pointerCount == 2){
//                    state = ZOOM;
//                }else if (pointerCount == 1){
//                    state = DRAG;
//                }
                if(event.getAction() == MotionEvent.ACTION_DOWN ){
                    Log.d("TAG", "onTouch: action down");

                    state = DRAG;
                }else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN){
                    Log.d("TAG", "onTouch: pointer down");
                    state = ZOOM;
                }
                if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP){
                    Log.d("TAG", "onTouch: pointer up");
                    state = 0;
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
                            float dy = -(y - mLastY);
                            Log.d("TAG", "onTouch: dx dy =" + dx + " " + dy);
                            if (Math.sqrt((dx * dx) + (dy * dy)) >= scaledTouchSlop){
                                Log.d("TAG", "onTouch: scaled touch slop = "+scaledTouchSlop);
                                myGLRender.setTrans(dx / glSurfaceView.getWidth(), dy / glSurfaceView.getHeight());
                            }
                            Log.d("TAG", "onTouch: width height = "+glSurfaceView.getWidth()+" "+glSurfaceView.getHeight());
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

        });
    }
    public class gestureListener implements GestureDetector.OnGestureListener{

        float x = 0f,y = 0f;
        @Override
        public boolean onDown(MotionEvent e) {
            Log.d("TAG", "onDown: ");
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d("TAG", "onScroll: ");
            x -= distanceX;y-= distanceY;
            myGLRender.setTrans(x,y);
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
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
                if(currentScale < 0.25f){
                    currentScale = 0.25f;
                }else if (currentScale > 4f){
                    currentScale = 4f;
                }
                myGLRender.setScale(currentScale);

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
}
class Point{
    static final int COORDS_PER_VERTEX = 3;

    private int mProgram, mPositionHandle, mColorHandle;
    private FloatBuffer vertexBuffer;
    private float vertices[];

    float color[] = {0.63671875f, 0.76953125f, 0.22265625f, 1.0f};

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private final String vertexShaderCode ="attribute vec4 vPosition;"
            + "uniform mat4 uMVPMatrix;"
            + "void main() {"
            + "  gl_Position = uMVPMatrix * vPosition;"
            + "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    public Point() {
        int vertexShader = MyGLRender.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRender.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        vertices = new float[364 * 3];
        vertices[0] = 0;
        vertices[1] = 0;
        vertices[2] = 0;

        for (int i =1; i <364; i++){
            vertices[(i * 3)+ 0] = (float) (0.5 * Math.cos((3.14/180) * (float)i ));
            vertices[(i * 3)+ 1] = (float) (0.5 * Math.sin((3.14/180) * (float)i ));
            vertices[(i * 3)+ 2] = 0;
        }


        Log.v("Thread", "" + vertices[0] + "," + vertices[1] + "," + vertices[2]);
        ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        vertexByteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = vertexByteBuffer.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);


        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);
    }


    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);


        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

//        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        // Apply the projection and view transformation
//        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the point
        int vertexCount = mvpMatrix.length / COORDS_PER_VERTEX;
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
class MyGLRender implements GLSurfaceView.Renderer{
    private Point point;
    private float scale = 1;
    private float x = 0f,y = 0f;
    void setScale(float scale_){
        Log.d("TAG","onscale set scale = "+scale_);
        scale = scale_;
    }
    void setTrans(float x_,float y_){
        x = x + x_;
        y = y + y_;
    }
    //坐标维数
    int coor = 3;
    //3-coord x y z
    private float[] vertexArray;
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

        Log.d("TAG", "onDrawFrame: scale = "+scale);
        Log.d("TAG", "onDrawFrame: x y = "+x+" "+y);
        gl.glTranslatef(x * 2,y * 2,0.0f);
        gl.glScalef(scale,scale,1f);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        Log.d("TAG", "onDrawFrame: length = "+vertexArray.length);
        //指定顶点
        gl.glVertexPointer(coor,GL10.GL_FLOAT,0,Tool.getFloatBuffer(vertexArray));
        //最后一个参数为顶点的数量
        gl.glDrawArrays(GL10.GL_POINTS,0,vertexArray.length/3);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
//        point.draw(vertexArray);
    }
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}



