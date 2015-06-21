//package org.twinone.rubiksolver;
//
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.hardware.Camera;
//import android.hardware.Camera.CameraInfo;
//import android.util.SparseIntArray;
//import android.view.Surface;
//
///**
// * Created by twinone on 6/20/15.
// */
//
//public class SideCapturer {
//
//    private Context mContext;
//    private Camera mCamera;
//    private SideCallback mCallback;
//
//    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
//
//    static {
//        ORIENTATIONS.append(Surface.ROTATION_0, 90);
//        ORIENTATIONS.append(Surface.ROTATION_90, 0);
//        ORIENTATIONS.append(Surface.ROTATION_180, 270);
//        ORIENTATIONS.append(Surface.ROTATION_270, 180);
//    }
//
//
//    public SideCapturer(Context c) {
//        mContext = c;
//    }
//
//
//    public interface SideCallback {
//        /**
//         * Called when a side is captured
//         *
//         * @param side The side that was captured, or null if an error occurred
//         */
//        public void onSideCaptured(Side side);
//    }
//
//    public void getSide(SideCallback callback) {
//        mCallback = callback;
//        int id = getCameraId();
//        if (id == -1) {
//            mCallback.onSideCaptured(null);
//            return;
//        }
//
//        Camera c = Camera.open(id);
//        c.
//    }
//
//    /**
//     * Finds the id of the first back facing camera, or -1
//     */
//    private int getCameraId() {
//        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
//            return -1;
//        }
//
//        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
//            CameraInfo info = new CameraInfo();
//            Camera.getCameraInfo(i, info);
//            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
//                return i;
//            }
//        }
//
//        return -1;
//    }
//}
