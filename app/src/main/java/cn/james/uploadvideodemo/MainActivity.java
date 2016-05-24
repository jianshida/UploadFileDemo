package cn.james.uploadvideodemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity implements View.OnClickListener {

    private ImageView videoImg;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button select = (Button) findViewById(R.id.select_btn);
        videoImg = (ImageView) findViewById(R.id.video_bitmapmm);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setMax(100);
        select.setOnClickListener(this);
    }

    private String imagePath;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                try {
                    imagePath = UpLoadQiNiuUtil.getRealFilePath(this, uri);
                    File f = new File(imagePath);
                    Log.e("imagePath", imagePath);
                    Bitmap bitmap = UpLoadQiNiuUtil.getBitmapsFromVideo(imagePath);
                    if (bitmap != null)
                        videoImg.setImageBitmap(bitmap);
                    Date date = new Date();
                    SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//24小时制
                    String LgTime = sdformat.format(date);
                    Log.e("time1", LgTime);
                    submit1(f);
                } catch (Exception e) {
                    Log.e("Exception", e.getMessage(), e);
                }
            }
        }
    }

    private void submit1(File f) {
        UpLoadQiNiuUtil.getToken(this, f, "james/upload/video/test", false, new handler());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_btn:
                Intent intent = new Intent();
                // 开启Pictures画面Type设定为image
                // intent.setType("image/*");
                // intent.setType(“audio/*”); //选择音频
                intent.setType("video/*"); //选择视频 （mp4 3gp 是android支持的视频格式）
                //intent.setType(“video/*;image/*”);//同时选择视频和图片
                // 使用Intent.ACTION_GET_CONTENT这个Action */
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // 取得相片后返回本画面 */
                startActivityForResult(intent, 1);
                break;
            default:
                break;
        }
    }

    private class handler extends UpLoadQiNiuUtil.ProgressAndisCompleteHandler {

        @Override
        public void progress(int progress) {
            progressBar.setProgress(progress);
        }

        @Override
        public void isComplete(boolean  isSucceed) {

        }
    }

}
