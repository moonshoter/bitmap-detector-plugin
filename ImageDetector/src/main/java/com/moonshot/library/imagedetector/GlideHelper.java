package com.moonshot.library.imagedetector;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.SingleRequest;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.moonshot.library.imagedetector.ui.AlertListActivity;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlideHelper {


    /**
     * 检测是否有异常数据
     */
    public static void detect(final ImageViewTarget imageViewTarget, final Object resource) {

        if (imageViewTarget == null || resource == null) {
            return;
        }
        final ImageView imageView = (ImageView) imageViewTarget.getView();
        if (imageView == null) {
            return;
        }
        imageView.post(new Runnable() {
            @Override
            public void run() {
                //View大小 与 加载的 资源大小
                int viewWidth = imageView.getWidth();
                int viewHeight = imageView.getHeight();
                int resWidth = 0, resHeight = 0;

                if (resource instanceof Bitmap) {
                    Bitmap bitmap = (Bitmap) resource;
                    resWidth = bitmap.getWidth();
                    resHeight = bitmap.getHeight();
                } else if (resource instanceof Drawable) {
                    Drawable drawable = (Drawable) resource;
                    resWidth = drawable.getIntrinsicWidth();
                    resHeight = drawable.getIntrinsicHeight();
                }
                //
                DetectorInfo detectorInfo = new DetectorInfo();
                if (resHeight > viewWidth && resWidth > viewWidth) {
                    //加载资源超过视图大小。需要预警
                    detectorInfo.isTooLarge = true;
                    detectorInfo.mImgWidth = resWidth;
                    detectorInfo.mImgHeight = resHeight;
                    detectorInfo.mViewHeight = viewHeight;
                    detectorInfo.mViewWidth = viewWidth;
                }

                //获取加载的url
                String url = getUrlFromRequest(imageViewTarget.getRequest());
                detectorInfo.mUrl = url;
                detectorInfo.isLegalUrl = isLegalUrl(url);

                if (detectorInfo.isTooLarge || !detectorInfo.isLegalUrl) {
                    Activity activity = Utils.getActivity(imageView.getContext());
                    if (activity != null) {
                        detectorInfo.mLoadActivity = activity.getClass().getName();
                    }
                    //打开预警界面
                    AlertListActivity.launch(imageView.getContext(), detectorInfo);
                }


            }
        });


    }

    /**
     * 获取请求的Url
     *
     * @param request
     * @return
     */
    public static String getUrlFromRequest(Request request) {
        if (request != null && request instanceof SingleRequest) {
            SingleRequest singleRequest = (SingleRequest) request;
            //反射获取加载的url
            Class requestClass = singleRequest.getClass();
            try {
                Field modelFiled = requestClass.getDeclaredField("model");
                modelFiled.setAccessible(true);
                Object model = modelFiled.get(singleRequest);
                String url = null;
                if (model instanceof String) {
                    url = (String) model;
                } else if (model instanceof Uri) {
                    url = ((Uri) model).toString();
                }

                Log.e("moonshot", "url" + url);
                return url;

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        return null;
    }

    /**
     * 是否是合法的Url
     *
     * @param url
     * @return
     */
    public static boolean isLegalUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        Pattern pattern = Pattern.compile(Constant.URL_REGEX_CDN);
        Matcher matcher = pattern.matcher(url);
        if (matcher != null && matcher.find()) {
            return true;
        }
        return false;

    }

}
