package cn.james.uploadvideodemo;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Administrator on 2016/5/4.
 */
public class UpLoadQiNiuUtil {

    private final int PROGERSS = 1;
    private final int UPLOAD_SUCCEED = 2;

    /**
     * 获取token
     *
     * @param context
     * @param file      待上传文件
     * @param expectKey 文件上传的路径前缀
     * @param isImage   图片或视频的标志位
     * @param handler   回调handler
     */
    public static void getToken(final Context context, final File file, final String expectKey, final boolean isImage, final Handler handler) {
        HttpUtils http = new HttpUtils();
        String url = "http://sec.xiangwangolf.com/api/qiniu/token";
        http.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {

            @Override
            public void onSuccess(final ResponseInfo<String> responInfo) {
                // TODO Auto-generated method stub
                try {
                    JSONObject jsonObject = new JSONObject(responInfo.result);
                    String token = jsonObject.getString("token");
                    Log.e("七牛获取token成功", token);
                    Date date = new Date();
                    SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//24小时制
                    String LgTime = sdformat.format(date);
                    Log.e("time2", LgTime);
                    uploadFile(context, file, token, expectKey, isImage, handler);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.e("七牛上传失败", e.toString());
                }
            }

            @Override
            public void onFailure(HttpException arg0, String content) {
                // TODO Auto-generated method stub
                Log.e("失败了吗", content);
            }

        });
    }

    private static void uploadFile(final Context context, File file, String token, final String expectKey, boolean isImage, final Handler handler) {
        final UploadManager uploadManager = new UploadManager();
        final UpProgressHandler progressHandler = new UpProgressHandler() {
            @Override
            public void progress(String key, double percent) {
                Message msg = new Message();
                msg.what = PROGERSS;
                msg.arg1 = (int) (percent * 100);
                handler.sendMessage(msg);
            }
        };
        Map<String, String> params = new HashMap<>();
        params.put("x:foo", "fooval");//自定义变量，key必须以 x: 开始。
        final UploadOptions opt = new UploadOptions(params, null, true, progressHandler, null);
        UUID uuid = UUID.randomUUID();
        final String uuidStr;
        if (isImage)
            uuidStr = uuid.toString().replace("-", "") + ".jpg";
        else
            uuidStr = uuid.toString().replace("-", "") + ".mp4";
        String key = expectKey + uuidStr;
        uploadManager.put(file, key, token, new UpCompletionHandler() {

            @Override
            public void complete(String key, com.qiniu.android.http.ResponseInfo info, JSONObject response) {
                // TODO Auto-generated method stub
                Message msg = new Message();
                msg.what = UPLOAD_SUCCEED;
                if (info.isOK()) {
                    msg.obj = true;
                    Log.e("上传_七牛", "上传成功了");
                    Log.e("路径", expectKey + uuidStr);
                    Toast.makeText(context, "上传成功了", Toast.LENGTH_LONG);
                } else {
                    msg.obj = false;
                    Log.e("上传失败", "上传失败");
                }
                handler.sendMessage(msg);
                Date date = new Date();
                SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 24小时制
                String LgTime = sdformat.format(date);
                Log.e("timeFinish", LgTime);
            }
        }, opt);
    }

    public static abstract class ProgressAndisCompleteHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PROGERSS:
                    progress(msg.arg1);
                    break;
                case UPLOAD_SUCCEED:
                    boolean isSucceed = (boolean) msg.obj;
                    isComplete(isSucceed);
                    break;
                default:
                    break;
            }

        }

        /**
         * 上传进度的回调
         *
         * @param progress 上传进度百分比回调
         */
        public abstract void progress(int progress);

        /**
         * 上传成功的回调
         *
         * @param isSucceed
         */
        public abstract void isComplete(boolean isSucceed);
    }

    /**
     * Try to return the absolute file path from the given Uri
     * 从给定的Uri返回绝对文件路径
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 从视频中获取视频截图
     *
     * @param dataPath 视频本地路径
     * @return 返回bitmap类型的截图
     */
    public static Bitmap getBitmapsFromVideo(String dataPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(dataPath);
        // 取得视频的长度(单位为毫秒)
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        // 取得视频的长度(单位为秒)
        int seconds = Integer.valueOf(time) / 1000;
        // 得到每一秒时刻的bitmap比如第一秒,第二秒
        if (seconds > 1) {
            Bitmap bitmap = retriever.getFrameAtTime(1 * 1000 * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            String path = Environment.getExternalStorageDirectory() + File.separator + 1 + ".jpg";
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(path);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        return null;
    }

}
