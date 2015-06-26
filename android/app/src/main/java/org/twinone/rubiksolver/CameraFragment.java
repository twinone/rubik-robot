package org.twinone.rubiksolver;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;

import java.text.DecimalFormat;

/**
 * Created by twinone on 6/20/15.
 */
@SuppressWarnings("deprecation")
public class CameraFragment extends Fragment implements View.OnClickListener, Camera.PictureCallback {

    private static final String TAG = "CameraFragment";

    // We take square images
    private static final int IMAGE_SIZE = 100;

    private CameraPreview mCameraPreview;
    private Button mButtonCapture;
    private FrameLayout mFrameLayout;
    private Camera mCamera;
    private int mCameraId;
    private int mCameraRotation;
    private HighlightView mHLView;
    private RelativeLayout mRelativeLayout;
    private TextView[][] mSquare = new TextView[MainActivity.SIZE][MainActivity.SIZE];
    private Layer[] mLayers = new Layer[6];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        mRelativeLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_camera, null);
        mButtonCapture = (Button) mRelativeLayout.findViewById(R.id.button_capture);
        mButtonCapture.setOnClickListener(this);

        mFrameLayout = (FrameLayout) mRelativeLayout.findViewById(R.id.frame_layout);
        return mRelativeLayout;
    }

    public static Camera getCameraInstance(int id) {
        Camera c = null;
        try {
            c = Camera.open(id);
        } catch (Exception e) {
            Log.d(TAG, "Camera not available");
        }
        return c;
    }

    private int getCameraId() {
        int n = Camera.getNumberOfCameras();
        for (int i = 0; i < n; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onStart() {
        super.onStart();

        mRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mCamera.autoFocus(null);
                return true;
            }
        });
        mCameraId = getCameraId();
        if (mCameraId == -1) {
            Toast.makeText(getActivity(), "Camera not available", Toast.LENGTH_SHORT).show();
            return;
        }
        mCamera = getCameraInstance(mCameraId);
        setupCamera();
        mCameraPreview = new CameraPreview(getActivity(), mCamera);

        mCameraRotation = getCameraRotation(getActivity(), mCameraId, mCamera);
        mCamera.setDisplayOrientation(mCameraRotation);

        mFrameLayout.removeAllViews();
        mFrameLayout.addView(mCameraPreview);

        if (mHLView != null && mHLView.getParent() == mRelativeLayout)
            mRelativeLayout.removeView(mHLView);
        mHLView = new HighlightView(getActivity());
        mRelativeLayout.addView(mHLView);

        int s = dpToPx(200) / MainActivity.SIZE;
        LinearLayout cpw = (LinearLayout) mRelativeLayout.findViewById(R.id.colorPreviewWrapper);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(s, s);
        int margin = (int) 0.1 * s;
        lp.setMargins(margin, margin, margin, margin);
        for (int i = 0; i < MainActivity.SIZE; i++) {
            LinearLayout l = new LinearLayout(getActivity());
            l.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = 0; j < MainActivity.SIZE; j++) {
                TextView tv = new TextView(getActivity());
                tv.setTextSize(10);
                tv.setLayoutParams(lp);
                l.addView(tv);
                mSquare[i][j] = tv;
            }
            cpw.addView(l);
        }
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    void setupCamera() {
        Log.d(TAG, "Capturing...");
        Camera.Parameters p = mCamera.getParameters();
        p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        p.setPictureFormat(ImageFormat.JPEG);
        int min = Integer.MAX_VALUE;
        int w = 0;
        int h = 0;
        for (Camera.Size s : p.getSupportedPictureSizes()) {
            Log.d(TAG, "Supported resolution:" + s.width + "x" + s.height);
            int val = s.width * s.height;
            if (val < min) {
                min = val;
                w = s.width;
                h = s.height;
            }
        }
        Log.d(TAG, "Setting camera resolution:" + w + "x" + h);
        p.setPictureSize(w, h);
        p.setPreviewSize(w, h);

        mCamera.setParameters(p);
    }

    @Override
    public void onStop() {
        super.onStop();

        mCamera.release();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_capture) {
            capture();
        }
    }

    void capture() {
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                // Even if it's not focused, we should probably take the picture anyway
                mCamera.takePicture(null, null, CameraFragment.this);
            }
        });
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.d(TAG, "Picture taken!!");

        Bitmap b = rotateBitmap(BitmapFactory.decodeByteArray(data, 0, data.length), mCameraRotation);
        int w = b.getWidth();
        int h = b.getHeight();

        int sw = mCameraPreview.getWidth();
        int sh = mCameraPreview.getHeight();

        /**
         * Map screen coordinates to image coordinates
         * Assumes aspect ratios of preview and image are the same
         */
        Point[] coords = mHLView.getCoords();
        float[] src = new float[coords.length * 2];
        for (int i = 0; i < coords.length; i++) {
            Point coord = coords[i];
            src[i * 2 + 0] = (coord.x / (float) sw) * w;
            src[i * 2 + 1] = (coord.y / (float) sh) * h;
        }

        Bitmap target = Bitmap.createBitmap(b.getWidth(), b.getHeight(), b.getConfig());
        int tw = target.getWidth();
        int th = target.getHeight();

        Canvas canvas = new Canvas(target);
        Matrix m = new Matrix();
        m.setPolyToPoly(src, 0, new float[]{0, 0, tw, 0, tw, th, 0, th}, 0, 4);
        canvas.drawBitmap(b, m, null);

        ImageView iv = new ImageView(getActivity());
        iv.setImageBitmap(target);
        mRelativeLayout.addView(iv);

        Layer s = getSideFromBitmap(target);

    }


    private Layer getSideFromBitmap(Bitmap b) {
        int size = MainActivity.SIZE;
        int dx = b.getWidth() / size;
        int dy = b.getHeight() / size;
        // Sticker margin
        int sm = (int) (Math.min(dy, dx) * 0.45);
        Layer s = new Layer(size);
        double[][][] points = new double[size][size][];

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Bitmap bm = Bitmap.createBitmap(b, x * dx + sm, y * dy + sm, dx - sm * 2, dy - sm * 2);
//                mSquare[j*size+i].setBackground(new BitmapDrawable(bm));
                int color = averageColor(bm);
                s.m[y][x] = color;

                // Translate color to point
                double[] point = {Color.red(color), Color.green(color), Color.blue(color)};
                for (int i = 0; i < 3; i++) point[i] = Math.pow(point[i] / 255.0, 2.2);
                points[y][x] = point;
            }
        }

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                mSquare[y][x].setBackgroundColor(s.m[y][x]);

                // Calculate distance between this and target point
                double[] p1 = points[0][x];
                double[] p2 = points[y][x];
                double[] ds = {p2[0] - p1[0], p2[1] - p1[1], p2[2] - p1[2]};
                double error = Math.sqrt((ds[0] * ds[0] + ds[1] * ds[1] + ds[2] * ds[2]) / 3);

                mSquare[y][x].setText(String.format("%.4f%%", error * 100));
            }
        }

        return s;
    }

    // Round f to n decimal places
    double round(double d, int n) {
        int t = 1;
        for (int i = 0; i < n; i++) t *= 10;
        return (double) Math.round(d * t) / t;
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static int getCameraRotation(Activity activity, int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    int doubleArrayToColor(double[] array) {
        return Color.rgb((int) array[0], (int) array[1], (int) array[2]);
    }

    double[] average(Bitmap bm) {
        int w = bm.getWidth();
        int h = bm.getHeight();
        int s = w * h;
        double r = 0;
        double g = 0;
        double b = 0;
        int[] pixels = new int[s];
        bm.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int c = pixels[i * w + j];
                int pr = Color.red(c);
                int pg = Color.green(c);
                int pb = Color.blue(c);
                r += pr * pr;
                g += pg * pg;
                b += pb * pb;
            }
        }
        r = Math.sqrt(r / (double) s);
        g = Math.sqrt(g / (double) s);
        b = Math.sqrt(b / (double) s);
        return new double[]{r, g, b};
    }

    int averageColor(Bitmap b) {
        return doubleArrayToColor(average(b));
    }

}
