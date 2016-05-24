
# 上传文件
**上传文件**从app端降待上传的文件（图片、视频等）转化成File，并上传到七牛云存储空间，上传成功返回url。这样后台只需存储一个url字段，可以大大减轻后台服务器的压力。

### 注册
* 在官网(http://www.qiniu.com/) 注册七牛帐号，并创建一个上传空间。
* jar包引用
	- 在gradle文件中引用 compile 'com.qiniu:qiniu-android-sdk:7.1.2'

### 获取token及上传
* 为了隐私性和安全性，token由后台生成(调用UploadQiNiuUtil类中的getToken)
```
        HttpUtils http = new HttpUtils();
        String url = "http://sec.xiangwangolf.com/api/qiniu/token";
        http.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {

            @Override
            public void onSuccess(final ResponseInfo<String> responInfo) {
                // TODO Auto-generated method stub
                try {
                    JSONObject jsonObject = new JSONObject(responInfo.result);
                    String token = jsonObject.getString("token");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(HttpException arg0, String content) {
                // TODO Auto-generated method stub
            }

        });
    }
```
==获取token由后台提供请求url，请求获取方法任意(旨在获取到token)，上文使用的xutil框架的获取方法==

* 上传file

	- 根据获取到的image文件或者视频文件等以及token调用UploadQiNiuUtil类中的uploadFile方法，传入对应参数

	==在调用UploadQiNiuUtil类中方法前，必须先定义一个handler类继承UploadQiNiuUtil中的ProgressAndisCompleteHandler，并重写两个回调方法==

### 附加
* 根据uri获取绝对路径方法（调用UpLoadQiNiuUtil中的getRealFilePath()）
* 根据绝对路径获取视频截图的方法(调用UpLoadQiNiuUtil中的getBitmapsFromVideo())

