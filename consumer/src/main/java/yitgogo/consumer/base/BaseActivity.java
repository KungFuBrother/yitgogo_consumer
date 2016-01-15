package yitgogo.consumer.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.smartown.controller.mission.MissionController;
import com.smartown.yitian.gogo.R;

public class BaseActivity extends FragmentActivity {

    public LayoutInflater layoutInflater;
    private BroadcastReceiver broadcastReceiver;

    private View disconnectView;
    private FrameLayout settingButton;
    private boolean isAdded = false;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        init();
    }

    @Override
    protected void onDestroy() {
        MissionController.cancelMissions(this);
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private void init() {
        // if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
        // getWindow().addFlags(
        // WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // getWindow().addFlags(
        // WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        // setTranslucentStatus(true);
        // SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // tintManager.setStatusBarTintEnabled(true);
        // tintManager.setStatusBarTintResource(R.color.actionbar_bg);
        // SystemBarConfig config = tintManager.getConfig();
        // listViewDrawer.setPadding(0, config.getPixelInsetTop(true), 0,
        // config.getPixelInsetBottom());
        // }
        layoutInflater = LayoutInflater.from(this);
        disconnectView = layoutInflater.inflate(R.layout.view_disconnect, null);
        settingButton = (FrameLayout) disconnectView.findViewById(R.id.disconnect_setting);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
            }
        });
        initReceiver();
    }

    protected void findViews() {

    }

    protected void initViews() {

    }

    protected void registerViews() {

    }

    protected void jump(String fragmentName, String fragmentTitle) {
        Intent intent = new Intent(BaseActivity.this, ContainerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("fragmentName", fragmentName);
        bundle.putString("fragmentTitle", fragmentTitle);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * 判断是否连接网络
     *
     * @return
     */
    protected boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() != null) {
            if (connectivityManager.getActiveNetworkInfo().isAvailable()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void initReceiver() {
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    checkConnection();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void checkConnection() {
        if (isConnected()) {
            if (isAdded) {
                disconnectView.setVisibility(View.GONE);
            }
        } else {
            if (isAdded) {
                disconnectView.setVisibility(View.VISIBLE);
            } else {
                addContentView(disconnectView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                isAdded = true;
            }
        }
    }

}
