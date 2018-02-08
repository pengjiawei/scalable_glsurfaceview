package pengjiawei.com.opengl_demo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES20;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by 91752 on 2018/2/7.
 */

public class Tool {
    private static final String TAG = "pengjiawei.com.opengl_demo.Tool";
        /**
         * @param vertexs float 数组
         * @return 获取浮点形缓冲数据
         */
        public static FloatBuffer getFloatBuffer(float[] vertexs) {
            FloatBuffer buffer;
            ByteBuffer bf = ByteBuffer.allocateDirect(vertexs.length * 4);
            bf.order(ByteOrder.nativeOrder());
            buffer = bf.asFloatBuffer();
            //写入数组
            buffer.put(vertexs);
            //设置默认的读取位置
            buffer.position(0);
            return buffer;
        }
    public static void measureView(View child) {
        ViewGroup.LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0,
                params.width);
        int lpHeight = params.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(lpHeight,
                    View.MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(0,
                    View.MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    public static boolean checkSupportGl20(Context context) {
        final ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
        return supportsEs2;
    }

    public static ArrayList<Float> readFile(InputStream inputStream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(inputStreamReader);
        String str = null;
        ArrayList<Float> arrayList = new ArrayList<Float>();
        while ((str = br.readLine()) != null) {
            String[] split = str.split(" ");
            arrayList.add(Float.valueOf(split[2]));
        }
        br.close();
        inputStreamReader.close();
        Log.d(TAG, "readFile: array size = "+arrayList.size());
        return arrayList;
    }
    public static ArrayList<Float> transfer(ArrayList<Float> infloats){
        ArrayList<Float> floats = new ArrayList<>();
        BigDecimal b1 = new BigDecimal(Double.valueOf(2));
        BigDecimal b2 = new BigDecimal(Double.valueOf(480));

            float distance = b1.divide(b2,1000,BigDecimal.ROUND_HALF_UP).floatValue();
            for (int i = 0; i < infloats.size() ;++i){
                if(infloats.get(i) == 254){
                    int x = i % 480;
                    int y = i / 480;
                    floats.add(-1 + distance * x);
                    floats.add(-1 + distance * y);
                    floats.add(0f);
                }
            }
        Log.d(TAG, "transfer: outfloats size = "+floats.size());
        return floats;
    }
}
