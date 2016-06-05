package org.twinone.rubiksolver.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.twinone.rubiksolver.R;
import org.twinone.rubiksolver.model.CapturedFace;
import org.twinone.rubiksolver.ui.CameraFragment;
import org.twinone.rubiksolver.ui.CameraPreview;
import org.twinone.rubiksolver.ui.HighlightView;
import org.twinone.rubiksolver.ui.MainActivity;

/**
 * Created by twinone on 6/30/15.
 */
public class FaceCapturer {

    private static final String TAG = "FaceCapturer";

    private final CameraFragment mFragment;


    private CameraPreview mCameraPreview;
    private Camera mCamera;
    private int mCameraId;
    private int mCameraRotation;
    private HighlightView mHLView;

    public interface Callback {
        void onFaceCaptured(int id, CapturedFace f);
    }

    public FaceCapturer(CameraFragment f) {
        mFragment = f;
    }

    public void start() {
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

        FrameLayout fl = (FrameLayout) mFragment.getView().findViewById(R.id.frame_layout);

        fl.removeAllViews();
        fl.addView(mCameraPreview);

        if (mHLView != null && mHLView.getParent() == mFragment.getRootView())
            mFragment.getRootView().removeView(mHLView);
        mHLView = new HighlightView(getActivity());
        mFragment.getRootView().addView(mHLView);


    }

    private Activity getActivity() {
        return mFragment.getActivity();
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


    public void stop() {
        mCamera.release();
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


    public void capture(final int id, final Callback c) {

        //mCamera.autoFocus(new Camera.AutoFocusCallback() {
          //  @Override
           // public void onAutoFocus(boolean success, Camera camera) {
                // Even if it's not focused, we should probably take the picture anyway
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Bitmap bitmap = getTransformedBitmap(data);
                        CapturedFace capturedFace = getFaceFromBitmap(bitmap);
                        c.onFaceCaptured(id, capturedFace);
                        mCamera.startPreview();
                    }
                });
            //}
        //});
    }

    public Bitmap getTransformedBitmap(byte[] data) {
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

        // Show the image
//        ImageView iv = new ImageView(mFragment.getActivity());
//        iv.setImageBitmap(target);
//        mFragment.getRootView().addView(iv);

        return target;
    }

    private CapturedFace getFaceFromBitmap(Bitmap b) {
        int size = MainActivity.SIZE;
        int dx = b.getWidth() / size;
        int dy = b.getHeight() / size;
        // Sticker margin
        int sm = (int) (Math.min(dy, dx) * 0.45);
        CapturedFace s = new CapturedFace(size);
//        double[][][] points = new double[size][size][];

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                Bitmap bm = Bitmap.createBitmap(b, x * dx + sm, y * dy + sm, dx - sm * 2, dy - sm * 2);
                s.m[y][x] = average(bm);
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
