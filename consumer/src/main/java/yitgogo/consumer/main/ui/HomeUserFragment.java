package yitgogo.consumer.main.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.money.model.MoneyAccount;
import yitgogo.consumer.money.ui.MoneyHomeFragment;
import yitgogo.consumer.order.ui.OrderFragment;
import yitgogo.consumer.product.ui.ShoppingCarFragment;
import yitgogo.consumer.store.SelectStoreActivity;
import yitgogo.consumer.store.model.Store;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Content;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.user.model.User;
import yitgogo.consumer.user.ui.OpenStoreFragment;
import yitgogo.consumer.user.ui.UserAddressFragment;
import yitgogo.consumer.user.ui.UserInfoFragment;
import yitgogo.consumer.user.ui.UserLoginFragment;
import yitgogo.consumer.user.ui.UserRecommendFragment;
import yitgogo.consumer.user.ui.UserScoreFragment;
import yitgogo.consumer.user.ui.UserShareFragment;
import yitgogo.consumer.view.NormalAskDialog;
import yitgogo.consumer.view.Notify;

public class HomeUserFragment extends BaseNotifyFragment implements
        OnClickListener {

    ImageView userHeadImageView, levelImageView;
    TextView userNameTextView, userLevelTextView;

    TextView storeNameTextView, storeAddressTextView;

    LinearLayout storeContactButton;

    LinearLayout infoButton, storeButton, addressButton,
            shareButton, userListButton, openStoreButton, updateButtton;

    TextView loginButton;

    LinearLayout entranceLayout;
    FrameLayout moneyEntranceButton, orderEntranceButton, scoreEntranceButton,
            carEntranceButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user);
        measureScreen();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(HomeUserFragment.class.getName());
        initViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(HomeUserFragment.class.getName());
    }

    @Override
    protected void findViews() {
        userHeadImageView = (ImageView) contentView.findViewById(R.id.user_info_userhead);
        userNameTextView = (TextView) contentView.findViewById(R.id.user_info_name);
        levelImageView = (ImageView) contentView.findViewById(R.id.user_info_level_image);
        userLevelTextView = (TextView) contentView.findViewById(R.id.user_info_level);

        entranceLayout = (LinearLayout) contentView.findViewById(R.id.user_entrance);
        moneyEntranceButton = (FrameLayout) contentView.findViewById(R.id.user_entrance_money);
        orderEntranceButton = (FrameLayout) contentView.findViewById(R.id.user_entrance_order);
        scoreEntranceButton = (FrameLayout) contentView.findViewById(R.id.user_entrance_score);
        carEntranceButton = (FrameLayout) contentView.findViewById(R.id.user_entrance_car);

        openStoreButton = (LinearLayout) contentView.findViewById(R.id.user_open_store);
        infoButton = (LinearLayout) contentView.findViewById(R.id.user_info);

        storeButton = (LinearLayout) contentView.findViewById(R.id.user_store);

        addressButton = (LinearLayout) contentView.findViewById(R.id.user_address);
        shareButton = (LinearLayout) contentView.findViewById(R.id.user_share);
        userListButton = (LinearLayout) contentView.findViewById(R.id.user_user_list);
        loginButton = (TextView) contentView.findViewById(R.id.user_login);
        updateButtton = (LinearLayout) contentView.findViewById(R.id.user_user_update);

        storeNameTextView = (TextView) contentView.findViewById(R.id.user_store_name);
        storeAddressTextView = (TextView) contentView.findViewById(R.id.user_store_address);
        storeContactButton = (LinearLayout) contentView.findViewById(R.id.user_store_contact);

        registerViews();
    }

    @Override
    protected void initViews() {
        if (TextUtils.isEmpty(Store.getStore().getPhone())) {
            storeContactButton.setVisibility(View.GONE);
        } else {
            storeContactButton.setVisibility(View.VISIBLE);
        }
//        if (Content.getIntContent(Parameters.CACHE_KEY_STORE_TYPE, Parameters.CACHE_VALUE_STORE_TYPE_LOCATED) == Parameters.CACHE_VALUE_STORE_TYPE_LOCATED) {
//            storeButton.setVisibility(View.GONE);
//        } else {
//            storeButton.setVisibility(View.VISIBLE);
//        }
        storeNameTextView.setText(Store.getStore().getStoreName());
        storeAddressTextView.setText(Store.getStore().getStoreAddess());
        if (User.getUser().isLogin()) {
            loginButton.setText("注销");
            getUserInfo();
        } else {
            userNameTextView.setText("");
            userLevelTextView.setText("");
            loginButton.setText("登录");
        }
    }

    @Override
    protected void registerViews() {
        entranceLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, screenWidth / 5));
        loginButton.setOnClickListener(this);
        infoButton.setOnClickListener(this);
        addressButton.setOnClickListener(this);
        shareButton.setOnClickListener(this);
        userListButton.setOnClickListener(this);
        moneyEntranceButton.setOnClickListener(this);
        orderEntranceButton.setOnClickListener(this);
        scoreEntranceButton.setOnClickListener(this);
        carEntranceButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                jump(ShoppingCarFragment.class.getName(), "易商城购物车");
            }
        });
        storeContactButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(Store.getStore().getPhone())) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    Uri data = Uri.parse("tel:" + Store.getStore().getPhone());
                    intent.setData(data);
                    startActivity(intent);
                }
            }
        });
        storeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SelectStoreActivity.class));
            }
        });
        openStoreButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                jump(OpenStoreFragment.class.getName(), "申请开店");
            }
        });
        updateButtton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                checkUpdate();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (User.getUser().isLogin()) {
            switch (v.getId()) {

                case R.id.user_info:
                    jump(UserInfoFragment.class.getName(), "我的资料");
                    break;

                case R.id.user_entrance_money:
                    jump(MoneyHomeFragment.class.getName(), "钱袋子");
                    break;

                case R.id.user_entrance_order:
                    jump(OrderFragment.class.getName(), "我的订单");
                    break;

                case R.id.user_entrance_score:
                    jump(UserScoreFragment.class.getName(), "赚积分");
                    break;

                case R.id.user_login:
                    NormalAskDialog askDialog = new NormalAskDialog("确定要注销吗？",
                            "确定", "取消") {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if (makeSure) {
                                Content.removeContent(Parameters.CACHE_KEY_USER_JSON);
                                Content.removeContent(Parameters.CACHE_KEY_USER_PASSWORD);
                                Content.removeContent(Parameters.CACHE_KEY_COOKIE);
                                Content.removeContent(Parameters.CACHE_KEY_MONEY_SN);
                                User.init(getActivity());
                                MoneyAccount.init(null);
                                loginButton.setText("登录");
                                userNameTextView.setText("");
                                userLevelTextView.setText("");
                            }
                            super.onDismiss(dialog);
                        }
                    };
                    askDialog.show(getFragmentManager(), null);
                    break;

                case R.id.user_address:
                    jump(UserAddressFragment.class.getName(), "收货地址管理");
                    break;

                case R.id.user_share:
                    jump(UserShareFragment.class.getName(), "推荐好友");
                    break;

                case R.id.user_user_list:
                    jump(UserRecommendFragment.class.getName(), "推荐会员信息");
                    break;

                default:
                    break;
            }
        } else {
            jump(UserLoginFragment.class.getName(), "会员登录");
        }
    }

    private void getUserInfo() {
        Request request = new Request();
        request.setUrl(API.API_USER_INFO_GET);
        request.addRequestParam("username", User.getUser().getUseraccount());
        request.setUseCookie(true);
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    JSONObject object;
                    try {
                        object = new JSONObject(requestMessage.getResult());
                        if (object.getString("state").equalsIgnoreCase("SUCCESS")) {
                            JSONObject userObject = object.optJSONObject("object");
                            if (userObject != null) {
                                Content.saveStringContent(Parameters.CACHE_KEY_USER_JSON, userObject.toString());
                                User.init(getActivity());
                                showUserInfo();
                            }
                        } else {
                            Notify.show(object.getString("message"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

    private void showUserInfo() {
        ImageLoader.getInstance().displayImage(User.getUser().getuImg(),
                userHeadImageView);
        if (TextUtils.isEmpty(User.getUser().getRealname())) {
            userNameTextView.setText(User.getUser().getPhone());
        } else {
            userNameTextView.setText(User.getUser().getRealname());
        }
        userLevelTextView.setText(User.getUser().getGrade().getGradeName());
        ImageLoader.getInstance().displayImage(
                User.getUser().getGrade().getGradeImg(), levelImageView);
    }

    private UpdateResponse updateResponse;

    private void checkUpdate() {
        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse response) {
                if (updateStatus == UpdateStatus.Yes) {
                    updateResponse = response;
                    if (getActivity() != null) {
                        showUpdateDialog();
                    }
                    return;
                }
                Notify.show("已是最新版本");
            }
        });
        UmengUpdateAgent.update(getActivity());
    }

    private void showUpdateDialog() {
        UmengUpdateAgent.setDialogListener(new UmengDialogButtonListener() {
            @Override
            public void onClick(int status) {
                if (status == UpdateStatus.Update) {
                    File downloadedFile = UmengUpdateAgent.downloadedFile(getActivity(), updateResponse);
                    if (downloadedFile != null) {
                        UmengUpdateAgent.startInstall(getActivity(), downloadedFile);
                    } else {
                        download();
                    }
                }
            }
        });
        UmengUpdateAgent.showUpdateDialog(getActivity(), updateResponse);
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
                File downloadedFile = UmengUpdateAgent.downloadedFile(getActivity(), updateResponse);
                if (downloadedFile != null) {
                    if (getActivity() != null) {
                        UmengUpdateAgent.startInstall(getActivity(), downloadedFile);
                    }
                } else {
                    Notify.show("下载新版本失败");
                }
            }
        });
        UmengUpdateAgent.startDownload(getActivity(), updateResponse);
    }

}
