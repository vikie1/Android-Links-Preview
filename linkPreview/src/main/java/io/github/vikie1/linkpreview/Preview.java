package io.github.vikie1.linkpreview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Preview extends RelativeLayout {
    private static final String TAG = Preview.class.getSimpleName();
    private ImageView mImgViewImage;
    private TextView mTxtViewTitle;
    private TextView mTxtViewDescription;
    private TextView mTxtViewSiteName;
    private TextView mTxtViewMessage;
    private LinearLayout previewLayout;
    private Context mContext;
    private Handler mHandler;
    private String mTitle = null;
    private String mDescription = null;
    private String mImageLink = null;
    private String mSiteName = null;
    private String mSite;
    private String mLink;
//    private RotateLoading mLoadingDialog;
    private FrameLayout mFrameLayout;
    private PreviewListener mListener;
    private WebView iframePlayer;
    private ViewSwitcher imgIframeSwitcher;

    public Preview(Context context) {
        super(context);
        initialize(context);
    }

    public Preview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public Preview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(Context context) {
        mContext = context;
        inflate(context, R.layout.layout_preview, this);
        mImgViewImage = findViewById(R.id.imgViewImage);
        mTxtViewTitle = findViewById(R.id.txtViewTitle);
        mTxtViewDescription = findViewById(R.id.txtViewDescription);
        mTxtViewSiteName = findViewById(R.id.txtViewSiteName);
//        mLoadingDialog = findViewById(R.id.rotateloading);
        mTxtViewMessage = findViewById(R.id.txtViewMessage);
        previewLayout = findViewById(R.id.preview_layout);
        mFrameLayout = findViewById(R.id.frameLoading);
        iframePlayer = findViewById(R.id.iframe_video_view);
        imgIframeSwitcher = findViewById(R.id.img_iframe_switcher);
        mFrameLayout.setVisibility(GONE);
        mHandler = new Handler(mContext.getMainLooper());
    }

    public void setListener(PreviewListener listener)
    {
        this.mListener=listener;
    }

    public void setData(String title,String description,String image, String site) {
        clear();
        mTitle = title;
        mDescription = description;
        mImageLink = image;
        mSiteName = site;
        if (getTitle() != null) {
            Log.v(TAG, getTitle());
            runOnUiThread(() -> mTxtViewTitle.setText(getTitle()));
        }
        if (getDescription() != null) {
            Log.v(TAG, getDescription());
            runOnUiThread(() -> mTxtViewDescription.setText(getDescription()));
        }
        if (getLink().toLowerCase().contains("youtu")){
            String videoId = getVideoId(getLink());
            iframePlayer.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    imgIframeSwitcher.setDisplayedChild(1);
                }
            });
            iframePlayer.setWebChromeClient(new WebChromeClient(){
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    if (newProgress == 100) imgIframeSwitcher.setDisplayedChild(1);
                }
            });
            String path= """
                    <iframe src='https://www.youtube.com/embed/${videoId}' width='100%' height='100%' style='border: none;'></iframe>
                 """.replace("${videoId}", videoId);
            iframePlayer.getSettings().setJavaScriptEnabled(true);
            iframePlayer.getSettings().setLoadWithOverviewMode(true);
            iframePlayer.getSettings().setUseWideViewPort(true);
            iframePlayer.getSettings().setDomStorageEnabled(true);
            iframePlayer.loadDataWithBaseURL("https://www.youtube.com/embed/" + videoId, path,"text/html","utf-8", null);
            Log.v("Youtube videoId", videoId);

            runOnUiThread(() -> {
                imgIframeSwitcher.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                imgIframeSwitcher.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 255, getResources().getDisplayMetrics());
                previewLayout.setOrientation(LinearLayout.VERTICAL);
                mTxtViewDescription.setVisibility(GONE);
                LinearLayout linearLayout = findViewById(R.id.text_linear_layout);
                linearLayout.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
                linearLayout.requestLayout();
            });
        } else if (getImageLink() != null && !getImageLink().equals("")) {
            Log.v(TAG, getImageLink());
            runOnUiThread(() -> {
                try {
                    Glide.with(mContext)
                            .asBitmap()
                            .load(getImageLink())
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    int w = resource.getWidth();
                                    int h = resource.getHeight();
                                    if (w > h) {
                                        previewLayout.setOrientation(LinearLayout.VERTICAL);
                                        mImgViewImage.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                                        LinearLayout linearLayout = findViewById(R.id.text_linear_layout);
                                        linearLayout.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
                                        mImgViewImage.requestLayout();
                                        linearLayout.requestLayout();
                                    } else {
                                        previewLayout.setOrientation(LinearLayout.HORIZONTAL);
                                        mImgViewImage.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 110, getResources().getDisplayMetrics());
                                        mImgViewImage.requestLayout();
                                        mImgViewImage.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }
                                    mImgViewImage.setImageBitmap(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
                } catch (Exception ignored) {
                }
            });
        } else runOnUiThread(() -> {
            try {
                Glide.with(mContext).load(R.drawable.baseline_error_outline_24).into(mImgViewImage);
            } catch (Exception ignored) {}
            mImgViewImage.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 110, getResources().getDisplayMetrics());
            mImgViewImage.requestLayout();
            mImgViewImage.setScaleType(ImageView.ScaleType.FIT_XY);
        });

        if (getSiteName() != null) {
            Log.v(TAG, getSiteName());
            runOnUiThread(() -> mTxtViewSiteName.setText(getSiteName()));
        }
    }

    public void setData(final String url) {
        if(!TextUtils.isEmpty(url)) {
            runOnUiThread(() -> {
                mFrameLayout.setVisibility(VISIBLE);
//                mLoadingDialog.start();
            });
            clear();

            if (url.toLowerCase().contains("youtu")){
                String videoId = getVideoId(url);
                iframePlayer.setWebViewClient(new WebViewClient(){
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        imgIframeSwitcher.setDisplayedChild(1);
                    }
                });
                iframePlayer.setWebChromeClient(new WebChromeClient(){
                    @Override
                    public void onProgressChanged(WebView view, int newProgress) {
                        super.onProgressChanged(view, newProgress);
                        if (newProgress == 100) imgIframeSwitcher.setDisplayedChild(1);
                    }
                });
                String path= """
                    <iframe src='https://www.youtube.com/embed/${videoId}' width='100%' height='100%' style='border: none;'></iframe>
                 """.replace("${videoId}", videoId);
                iframePlayer.getSettings().setJavaScriptEnabled(true);
                iframePlayer.getSettings().setLoadWithOverviewMode(true);
                iframePlayer.getSettings().setUseWideViewPort(true);
                iframePlayer.getSettings().setDomStorageEnabled(true);
                iframePlayer.loadDataWithBaseURL("https://www.youtube.com/embed/" + videoId, path,"text/html","utf-8", null);
                Log.v("Youtube videoId", videoId);

                runOnUiThread(() -> {
                    imgIframeSwitcher.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                    imgIframeSwitcher.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 230, getResources().getDisplayMetrics());
                    previewLayout.setOrientation(LinearLayout.VERTICAL);
                    mTxtViewDescription.setVisibility(GONE);
                    LinearLayout linearLayout = findViewById(R.id.text_linear_layout);
                    linearLayout.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
                    linearLayout.requestLayout();
                });
            }
            OkHttpClient client = new OkHttpClient();
            try {
                Request request = new Request.Builder().url(url).build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (!response.isSuccessful())
                            throw new IOException("Unexpected code " + response);

                        Elements titleElements, descriptionElements, imageElements, siteElements, linkElements;
                        String site = "";
                        Document doc;
                        doc = Jsoup.parse(response.body().string());
                        titleElements = doc.select("title");
                        descriptionElements = doc.select("meta[name=description]");
                        if (url.contains("bhphotovideo")) {
                            imageElements = doc.select("image[id=mainImage]");
                            site = "bhphotovideo";
                        } else if (url.contains("www.amazon.com")) {
                            imageElements = doc.select("img[data-old-hires]");
                            site = "www.amazon.com";
                        } else if (url.contains("www.amazon.co.uk")) {
                            imageElements = doc.select("img[data-old-hires]");
                            site = "www.amazon.co.uk";
                        } else if (url.contains("www.amazon.de")) {
                            imageElements = doc.select("img[data-old-hires]");
                            site = "www.amazon.de";
                        } else if (url.contains("www.amazon.fr")) {
                            imageElements = doc.select("img[data-old-hires]");
                            site = "www.amazon.fr";
                        } else if (url.contains("www.amazon.it")) {
                            imageElements = doc.select("img[data-old-hires]");
                            site = "www.amazon.it";
                        } else if (url.contains("www.amazon.es")) {
                            imageElements = doc.select("img[data-old-hires]");
                            site = "www.amazon.es";
                        } else if (url.contains("www.amazon.ca")) {
                            imageElements = doc.select("img[data-old-hires]");
                            site = "www.amazon.ca";
                        } else if (url.contains("www.amazon.co.jp")) {
                            imageElements = doc.select("img[data-old-hires]");
                            site = "www.amazon.co.jp";
                        } else if (url.contains("m.clove.co.uk")) {
                            imageElements = doc.select("img[id]");
                            site = "m.clove.co.uk";
                        } else if (url.contains("www.clove.co.uk")) {
                            imageElements = doc.select("li[data-thumbnail-path]");
                            site = "www.clove.co.uk";
                        } else
                            imageElements = doc.select("meta[property=og:image]");
                        mImageLink = getImageLinkFromSource(imageElements, site);
                        siteElements = doc.select("meta[property=og:site_name]");
                        linkElements = doc.select("meta[property=og:url]");

                        if (titleElements.size() > 0) {
                            mTitle = titleElements.get(0).text();
                        }
                        if (descriptionElements.size() > 0) {
                            mDescription = descriptionElements.get(0).attr("content");
                        }
                        if (linkElements.size() > 0) {
                            mLink = linkElements.get(0).attr("content");
                        } else {
                            linkElements = doc.select("link[rel=canonical]");
                            if (linkElements.size() > 0) {
                                mLink = linkElements.get(0).attr("href");
                            }
                        }
                        if (siteElements.size() > 0) {
                            mSiteName = siteElements.get(0).attr("content");
                        }

                        if (getTitle() != null) {
                            Log.v(TAG, getTitle());
                            runOnUiThread(() -> mTxtViewTitle.setText(getTitle()));
                        }
                        if (getDescription() != null) {
                            Log.v(TAG, getDescription());
                            runOnUiThread(() -> mTxtViewDescription.setText(getDescription()));
                        }
                        if (getImageLink() != null && !getImageLink().equals("")) {
                            Log.v(TAG, getImageLink());
                            runOnUiThread(() -> {
                                try {
                                    Glide.with(mContext)
                                            .asBitmap()
                                            .load(getImageLink())
                                            .into(new CustomTarget<Bitmap>() {
                                                @Override
                                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                    int w = resource.getWidth();
                                                    int h = resource.getHeight();
                                                    if (w > h) {
                                                        previewLayout.setOrientation(LinearLayout.VERTICAL);
                                                        mImgViewImage.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                                                        LinearLayout linearLayout = findViewById(R.id.text_linear_layout);
                                                        linearLayout.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
                                                        mImgViewImage.requestLayout();
                                                        linearLayout.requestLayout();
                                                    }
                                                    else {
                                                        previewLayout.setOrientation(LinearLayout.HORIZONTAL);
                                                        mImgViewImage.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 110, getResources().getDisplayMetrics());
                                                        mImgViewImage.requestLayout();
                                                        mImgViewImage.setScaleType(ImageView.ScaleType.FIT_XY);
                                                    }
                                                    mImgViewImage.setImageBitmap(resource);
                                                }

                                                @Override
                                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                                }
                                            });
                                } catch (Exception ignored){}
                            });
                        } else {
                            runOnUiThread(() -> {
                                try {
                                    Glide.with(mContext).load(R.drawable.baseline_error_outline_24).into(mImgViewImage);
                                } catch (Exception ignored) {}
                                mImgViewImage.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 110, getResources().getDisplayMetrics());
                                mImgViewImage.requestLayout();
                                mImgViewImage.setScaleType(ImageView.ScaleType.FIT_XY);
                            });
                        }
                        if (url.toLowerCase().contains("amazon"))
                            if (getSiteName() == null || getSiteName().equals(""))
                                mSiteName = "Amazon";
                        if (getSiteName() != null) {
                            Log.v(TAG, getSiteName());
                            runOnUiThread(() -> mTxtViewSiteName.setText(getSiteName()));
                        }

                        Log.v(TAG, "Link: " + getLink());

                        runOnUiThread(() -> {
//                            if (mLoadingDialog.isStart()) mLoadingDialog.stop();
                            mFrameLayout.setVisibility(GONE);
                        });

                        mListener.onDataReady(Preview.this);
                    }

                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        if(!TextUtils.isEmpty(e.getMessage())) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
            }
            catch (Exception ex) {
                runOnUiThread(() -> {
//                    if (mLoadingDialog.isStart()) mLoadingDialog.stop();
                    mFrameLayout.setVisibility(GONE);
                });
            }

        }
    }

    public void setMessage(final String message) {
        runOnUiThread(() -> {
            if(message==null) mTxtViewMessage.setVisibility(GONE);
            else mTxtViewMessage.setVisibility(VISIBLE);
            mTxtViewMessage.setText(message);
        });
    }

    public void setMessage(final String message, final int color) {
        runOnUiThread(() -> {
            if(message==null) mTxtViewMessage.setVisibility(GONE);
            else mTxtViewMessage.setVisibility(VISIBLE);
            mTxtViewMessage.setTextColor(color);
            mTxtViewMessage.setText(message);
        });
    }

    private String getImageLinkFromSource(Elements elements, String site) {
        String imageLink = null;
        if (elements != null && elements.size() > 0) {
            switch (site) {
                case "m.clove.co.uk", "bhphotovideo" -> imageLink = elements.get(0).attr("src");
                case "www.amazon.com", "www.amazon.co.uk", "www.amazon.de", "www.amazon.fr", "www.amazon.it", "www.amazon.es", "www.amazon.ca", "www.amazon.co.jp" -> {
                    imageLink = elements.get(0).attr("data-old-hires");
                    if (TextUtils.isEmpty(imageLink)) {
                        imageLink = elements.get(0).attr("src");
                        if (imageLink.contains("data:image/jpeg;base64,")) {
                            imageLink = elements.get(0).attr("data-a-dynamic-image");
                            if (!TextUtils.isEmpty(imageLink)) {
                                String[] array = imageLink.split(":\\[");
                                if (array.length > 1) {
                                    imageLink = array[0];
                                    if (!TextUtils.isEmpty(imageLink)) {
                                        imageLink = imageLink.replace("{\"", "");
                                        imageLink = imageLink.replace("\"", "");
                                    }
                                }
                            }
                        }
                    }
                }
                case "www.clove.co.uk" -> imageLink = "https://www.clove.co.uk" + elements.get(0).attr("data-thumbnail-path");
                default -> imageLink = elements.get(0).attr("content");
            }

        }
        return imageLink;
    }

    private static String getVideoId(String videoUrl) {
        String videoId = "";
        String regex = "https?://(?:m.)?(?:www\\.)?youtu(?:\\.be/|(?:be-nocookie|be)\\.com/(?:watch|\\w+\\?(?:feature=\\w+.\\w+&)?v=|v/|e/|embed/|live/|shorts/|user/(?:[\\w#]+/)+))([^&#?\\n]+)";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(videoUrl);
        if(matcher.find()){
            videoId = matcher.group(1);
        }
        return videoId;
    }

    private void clear() {
        mImgViewImage.setImageResource(0);
        mTxtViewTitle.setText("");
        mTxtViewDescription.setText("");
        mTxtViewSiteName.setText("");
        mTxtViewMessage.setText("");
        mTitle = null;
        mDescription = null;
        mImageLink = null;
        mSiteName = null;
        mSite = null;
        mLink = null;
    }

    public interface PreviewListener {
        void onDataReady(Preview preview);
    }

    private void runOnUiThread(Runnable r) {
        mHandler.post(r);
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getImageLink() {
        return mImageLink;
    }

    public String getSiteName() {
        return mSiteName;
    }

    public String getSite() {
        return mSite;
    }

    public String getLink() {
        return mLink;
    }
}
