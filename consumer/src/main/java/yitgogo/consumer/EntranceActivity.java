package yitgogo.consumer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.WindowManager;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.smartown.controller.mission.MissionController;
import com.smartown.controller.mission.MissionMessage;
import com.smartown.controller.mission.Request;
import com.smartown.controller.mission.RequestListener;
import com.smartown.controller.mission.RequestMessage;
import com.smartown.yitian.gogo.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengDialogButtonListener;
import com.umeng.update.UmengDownloadListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import yitgogo.consumer.base.BaseActivity;
import yitgogo.consumer.main.ui.MainActivity;
import yitgogo.consumer.store.SelectStoreByAreaFragment;
import yitgogo.consumer.store.model.Store;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Content;
import yitgogo.consumer.tools.LogUtil;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.user.model.User;
import yitgogo.consumer.view.NormalAskDialog;
import yitgogo.consumer.view.Notify;

public class EntranceActivity extends BaseActivity {

    private LocationClient locationClient;
    private BDLocation location;
    private int locateTime = 0;
    private boolean disConnect = false;
    private UpdateResponse updateResponse;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_entrance);
        initLocationTool();
        checkConnection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd(EntranceActivity.class.getName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart(EntranceActivity.class.getName());
        if (disConnect) {
            if (isConnected()) {
                disConnect = false;
                checkUpdate();
            } else {
                NormalAskDialog askDialog = new NormalAskDialog(
                        "无法连接网络，请检查网络设置！", "查看设置", "退出", false) {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        super.onDismiss(dialog);
                        if (makeSure) {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            startActivity(intent);
                        } else {
                            finish();
//                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    }
                };
                askDialog.show(getSupportFragmentManager(), null);
            }
        }
    }

    private void jumpToHome() {
        startActivity(new Intent(EntranceActivity.this, MainActivity.class));
        finish();
    }

    /**
     * 检查网络连通性
     */
    private void checkConnection() {
        if (isConnected()) {
            //能访问网络，检查更新
            checkUpdate();
        } else {
            //不能访问网络
            disConnect = true;
        }
    }

    /**
     * 判断是否需要更新配送点
     */
    private void shouldUpdateStore() {
        if (Store.getStore() == null) {
            updateStore(true);
        } else {
            if (Content.getIntContent(Parameters.CACHE_KEY_STORE_TYPE, Parameters.CACHE_VALUE_STORE_TYPE_LOCATED) == Parameters.CACHE_VALUE_STORE_TYPE_LOCATED) {
                updateStore(false);
            } else {
                jumpToHome();
            }
        }
    }

    /**
     * 记录用户位置
     */
    private void updateUserLocation() {
        if (User.getUser().isLogin()) {
            if (location != null) {
                Request request = new Request();
                request.setUrl(API.API_USER_UPDATE_LOCATION);
                request.setUseCookie(true);
                request.addRequestParam("member_account", User.getUser().getUseraccount());
                request.addRequestParam("store_id", Store.getStore().getStoreId());
                request.addRequestParam("location", location.getAddrStr());
                request.addRequestParam("coordinate", location.getLongitude() + "," + location.getLatitude());
                MissionController.startRequestMission(this, request, new RequestListener() {
                    @Override
                    protected void onStart() {

                    }

                    @Override
                    protected void onFail(MissionMessage missionMessage) {

                    }

                    @Override
                    protected void onSuccess(RequestMessage requestMessage) {

                    }

                    @Override
                    protected void onFinish() {

                    }
                });
            }
        }
    }

    private void updateStore(boolean must) {
        if (location != null) {
            getNearestStore(must);
        } else {
            if (must) {
                selectJmd();
            } else {
                jumpToHome();
            }
        }
    }

    private void selectJmd() {
        jump(SelectStoreByAreaFragment.class.getName(), "选择服务中心");
        finish();
    }

    /**
     * 初始化定位工具
     */
    private void initLocationTool() {
        locationClient = new LocationClient(getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
        option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        // option.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
        // option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
        locationClient.setLocOption(option);
        locationClient.registerLocationListener(new BDLocationListener() {

            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                locateTime++;
                // 防止重复定位，locateTime>1 表示已经定位过，无需再定位
                if (locateTime > 1) {
                    return;
                }
                if (bdLocation != null) {
                    if (bdLocation.getLocType() == 61
                            || bdLocation.getLocType() == 65
                            || bdLocation.getLocType() == 161) {
                        location = bdLocation;
                    }
                }
                updateUserLocation();
                shouldUpdateStore();
                locationClient.stop();
            }

        });
    }

    private void locate() {
        locationClient.start();
        locationClient.requestLocation();
    }

    private void getNearestStore(boolean must) {
        final boolean mustGetStore = must;
        Request request = new Request();
        request.setUrl(API.API_LBS_NEARBY);
        request.setRequestType(Request.REQUEST_TYPE_GET);
        request.addRequestParam("ak", Parameters.CONSTANT_LBS_AK);
        request.addRequestParam("geotable_id", Parameters.CONSTANT_LBS_TABLE);
        request.addRequestParam("sortby", "distance:1");
        request.addRequestParam("radius", "30000");
        request.addRequestParam("page_index", "0");
        request.addRequestParam("page_size", "1");
        request.addRequestParam("location", location.getLongitude() + "," + location.getLatitude());
        MissionController.startRequestMission(this, request, new RequestListener() {
            @Override
            protected void onStart() {

            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                // 执行到这里说明没有自动定位到到最近加盟店，需要手选
                if (mustGetStore) {
                    selectJmd();
                } else {
                    jumpToHome();
                }
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    LogUtil.logInfo("API_LBS_NEARBY", requestMessage.getResult());
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        JSONArray array = object.optJSONArray("contents");
                        if (array != null) {
                            if (array.length() > 0) {
                                Content.saveIntContent(Parameters.CACHE_KEY_STORE_TYPE, Parameters.CACHE_VALUE_STORE_TYPE_LOCATED);
                                Content.saveStringContent(Parameters.CACHE_KEY_STORE_JSONSTRING, array.getString(0));
                                Store.init(getApplicationContext());
                                // 自动定位到到最近加盟店，跳转到主页
                                jumpToHome();
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // 执行到这里说明没有自动定位到到最近加盟店，需要手选
                    if (mustGetStore) {
                        selectJmd();
                    } else {
                        jumpToHome();
                    }
                }
            }

            @Override
            protected void onFinish() {

            }
        });
    }

    private void checkUpdate() {
        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse response) {
                if (updateStatus == UpdateStatus.Yes) {
                    updateResponse = response;
                    showUpdateDialog();
                    return;
                }
                locate();
            }
        });
        UmengUpdateAgent.update(this);
    }

    private void showUpdateDialog() {
        UmengUpdateAgent.setDialogListener(new UmengDialogButtonListener() {
            @Override
            public void onClick(int status) {
                switch (status) {
                    case UpdateStatus.Update:
                        File downloadedFile = UmengUpdateAgent.downloadedFile(EntranceActivity.this, updateResponse);
                        if (downloadedFile != null) {
                            UmengUpdateAgent.startInstall(EntranceActivity.this, downloadedFile);
                            finish();
                        } else {
                            download();
                            locate();
                        }
                        break;
                    case UpdateStatus.Ignore:
                        locate();
                        break;
                    case UpdateStatus.NotNow:
                        locate();
                        break;
                }
            }
        });
        UmengUpdateAgent.showUpdateDialog(EntranceActivity.this, updateResponse);
    }

    private void download() {
        UmengUpdateAgent.setDownloadListener(new UmengDownloadListener() {
            @Override
            public void OnDownloadStart() {
                Notify.show("正在下载新版本,请在通知栏查看下载进度");
            }

            @Override
            public void OnDownloadUpdate(int i) {

            }

            @Override
            public void OnDownloadEnd(int i, String s) {
                File downloadedFile = UmengUpdateAgent.downloadedFile(EntranceActivity.this, updateResponse);
                if (downloadedFile != null) {
                    UmengUpdateAgent.startInstall(EntranceActivity.this, downloadedFile);
                } else {
                    Notify.show("下载新版本失败");
                }
            }
        });
        UmengUpdateAgent.startDownload(EntranceActivity.this, updateResponse);
    }

}
