package yitgogo.consumer.base;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dtr.zxing.activity.CaptureActivity;
import com.smartown.controller.mission.MissionController;
import com.smartown.yitian.gogo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import yitgogo.consumer.bianmin.ModelBianminOrderResult;
import yitgogo.consumer.bianmin.phoneCharge.ui.PhoneChargeFragment;
import yitgogo.consumer.local.model.ModelLocalCar;
import yitgogo.consumer.money.ui.PayFragment;
import yitgogo.consumer.order.model.ModelOrderResult;
import yitgogo.consumer.order.model.ModelStorePostInfo;
import yitgogo.consumer.order.ui.OrderFragment;
import yitgogo.consumer.product.ui.ProductDetailFragment;
import yitgogo.consumer.product.ui.ProductListFragment;
import yitgogo.consumer.tools.MD5;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.tools.ScreenUtil;

/**
 * 有通知功能的fragment
 *
 * @author Tiger
 */
public class BaseNotifyFragment extends BaseFragment {

    LinearLayout emptyLayout;
    ImageView emptyImage;
    TextView emptyText;

    LinearLayout failLayout;
    Button failButton;
    TextView failText;

    LinearLayout loadingLayout;
    ProgressBar loadingProgressBar;
    TextView loadingText;

    FrameLayout contentLayout;
    public View contentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup base_fragment, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_base, null);
        findView(view);
        return view;
    }

    private void findView(View view) {
        contentLayout = (FrameLayout) view.findViewById(R.id.base_fragment_content);
        emptyLayout = (LinearLayout) view.findViewById(R.id.base_fragment_empty);
        failLayout = (LinearLayout) view.findViewById(R.id.base_fragment_fail);
        loadingLayout = (LinearLayout) view.findViewById(R.id.base_fragment_loading);
        emptyImage = (ImageView) view.findViewById(R.id.base_fragment_empty_image);
        emptyText = (TextView) view.findViewById(R.id.base_fragment_empty_text);
        failText = (TextView) view.findViewById(R.id.base_fragment_fail_text);
        loadingText = (TextView) view.findViewById(R.id.base_fragment_loading_text);
        failButton = (Button) view.findViewById(R.id.base_fragment_fail_button);
        loadingProgressBar = (ProgressBar) view.findViewById(R.id.base_fragment_loading_progressbar);
        showContentView();
    }

    protected void findViews() {

    }

    protected void initViews() {

    }

    protected void registerViews() {
    }

    protected void reload() {
    }

    protected void showContentView() {
        if (contentLayout.getChildCount() > 0) {
            contentLayout.removeAllViews();
        }
        if (contentView != null) {
            contentLayout.addView(contentView);
        }
    }

    protected void setContentView(int layoutId) {
        contentView = layoutInflater.inflate(layoutId, null);
    }

    protected View getContentView() {
        return contentView;
    }

    protected void showLoading() {
        loadingText.setText("请稍候...");
        emptyLayout.setVisibility(View.GONE);
        failLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
    }

    protected void showLoading(String text) {
        loadingText.setText(text);
        emptyLayout.setVisibility(View.GONE);
        failLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
    }

    protected void hideLoading() {
        loadingLayout.setVisibility(View.GONE);
    }

    protected void loadingEmpty() {
        emptyText.setText("暂无数据");
        emptyLayout.setVisibility(View.VISIBLE);
    }

    protected void loadingEmpty(String text) {
        emptyText.setText(text);
        emptyLayout.setVisibility(View.VISIBLE);
    }

    protected void loadingFailed() {
        failLayout.setVisibility(View.VISIBLE);
    }

}
