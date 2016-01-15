package yitgogo.consumer.product.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.dtr.zxing.activity.CaptureActivity;
import com.smartown.controller.mission.ControllableListener;
import com.smartown.controller.mission.ControllableMission;
import com.smartown.controller.mission.MissionController;
import com.smartown.yitian.gogo.R;
import com.umeng.analytics.MobclickAgent;

import java.io.File;

import yitgogo.consumer.base.BaseNotifyFragment;

public class WebFragment extends BaseNotifyFragment {

    WebView webView;
    ProgressBar progressBar;
    ImageView goTopButton;
    String html = "", url = "http://www.baidu.com";
    int type = -1;

    public final static int TYPE_HTML = 0;
    public final static int TYPE_URL = 1;

    File htmlFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_web);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(WebFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(WebFragment.class.getName());
    }

    private void init() {
        htmlFile = new File(getActivity().getCacheDir().getPath(), "index.html");
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("html")) {
                html = bundle.getString("html");
            }
            if (bundle.containsKey("url")) {
                if (bundle.getString("url").length() > 0) {
                    url = bundle.getString("url");
                }
            }
            if (bundle.containsKey("type")) {
                type = bundle.getInt("type");
            }
        }
    }

    @Override
    protected void findViews() {
        webView = (WebView) contentView.findViewById(R.id.web_webview);
        progressBar = (ProgressBar) contentView.findViewById(R.id.web_progress);
        goTopButton = (ImageView) contentView.findViewById(R.id.web_go_top);
        initViews();
        registerViews();
    }

    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    protected void initViews() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    if (progressBar.getVisibility() == View.GONE)
                        progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
        webView.addJavascriptInterface(new JsInterface(), "yitgogo");
        WebSettings settings = webView.getSettings();
        settings.setDefaultTextEncodingName("utf-8");
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setUseWideViewPort(true);
        settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        settings.setLoadWithOverviewMode(true);

        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAppCachePath(getActivity().getCacheDir().getPath());
        settings.setAppCacheEnabled(true);

        showWebData();
    }

    @Override
    protected void registerViews() {
        addImageButton(R.drawable.ic_clear_black_24dp, "close",
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        getActivity().finish();
                    }
                });
        onBackButtonClick(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    getActivity().finish();
                }
            }
        });
        goTopButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                webView.scrollTo(0, 0);
            }
        });
    }

    private boolean downloadSuccess = false;

    private void downloadHtmlFile() {
        ControllableMission controllableMission = new ControllableMission() {
            @Override
            protected void doing() {
                downloadSuccess = MissionController.download(url, htmlFile);
            }
        };
        controllableMission.setControllableListener(new ControllableListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFinish() {
                hideLoading();
                if (downloadSuccess) {
                    webView.loadUrl("file://" + htmlFile.getPath());
                } else {
                    getActivity().finish();
                }
            }
        });
        MissionController.startControllableMission(getActivity(), controllableMission);
    }

    private void showWebData() {
        switch (type) {
            case TYPE_HTML:
                webView.loadData(html, "text/html; charset=utf-8", "utf-8");
                break;

            case TYPE_URL:
                downloadHtmlFile();
                break;
            default:
                webView.loadUrl(url);
                break;
        }
    }

    class JsInterface {

        @JavascriptInterface
        public boolean showProductInfo(String productId) {
            showProductDetail(productId, "商品详情", CaptureActivity.SALE_TYPE_NONE);
            return true;
        }
    }

}
