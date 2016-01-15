package yitgogo.consumer.user.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartown.controller.mission.MissionController;
import com.smartown.controller.mission.MissionMessage;
import com.smartown.controller.mission.Request;
import com.smartown.controller.mission.RequestListener;
import com.smartown.controller.mission.RequestMessage;
import com.smartown.yitian.gogo.R;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Content;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.user.model.User;
import yitgogo.consumer.view.Notify;

public class ModifySecret extends BaseNotifyFragment {

    LinearLayout editor;
    Button modify;
    TextView accountText, secretOld, secretNew, secretVerify;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_secret);
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(ModifySecret.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(ModifySecret.class.getName());
    }

    protected void findViews() {
        accountText = (TextView) contentView.findViewById(R.id.user_info_secret_account);
        secretOld = (TextView) contentView.findViewById(R.id.user_info_secret_old);
        secretNew = (TextView) contentView.findViewById(R.id.user_info_secret_new);
        secretVerify = (TextView) contentView.findViewById(R.id.user_info_secret_verify);
        modify = (Button) contentView.findViewById(R.id.user_secret_modify);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        accountText.setText(User.getUser().getUseraccount());
    }

    @Override
    protected void registerViews() {
        modify.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                modify();
            }
        });
    }

    private void modify() {
        String oldpassword = secretOld.getText().toString().trim();
        String newpassword = secretNew.getText().toString().trim();
        String renewpassword = secretVerify.getText().toString().trim();
        if (TextUtils.isEmpty(oldpassword)) {
            Notify.show("请输入旧密码");
        } else if (TextUtils.isEmpty(newpassword)) {
            Notify.show("请输入新密码");
        } else if (TextUtils.isEmpty(renewpassword)) {
            Notify.show("请确认新密码");
        } else if (newpassword.equalsIgnoreCase(oldpassword)) {
            Notify.show("新密码与旧密码相同");
        } else if (!newpassword.equalsIgnoreCase(renewpassword)) {
            Notify.show("两次输入的新密码不相同");
        } else {
            Request request = new Request();
            request.setUrl(API.API_USER_MODIFY_SECRET);
            request.addRequestParam("useraccount", User.getUser().getUseraccount());
            request.addRequestParam("oldpassword", getEncodedPassWord(oldpassword));
            request.addRequestParam("newpassword", getEncodedPassWord(newpassword));
            request.setUseCookie(true);
            MissionController.startRequestMission(getActivity(), request, new RequestListener() {
                @Override
                protected void onStart() {
                    showLoading();
                }

                @Override
                protected void onFail(MissionMessage missionMessage) {
                    Notify.show("修改密码失败");
                }

                @Override
                protected void onSuccess(RequestMessage requestMessage) {
                    if (!TextUtils.isEmpty(requestMessage.getResult())) {
                        try {
                            JSONObject object = new JSONObject(requestMessage.getResult());
                            if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                                Content.removeContent(Parameters.CACHE_KEY_USER_JSON);
                                Content.removeContent(Parameters.CACHE_KEY_USER_PASSWORD);
                                Content.removeContent(Parameters.CACHE_KEY_COOKIE);
                                User.init(getActivity());
                                Notify.show("修改成功,修改密码成功");
                                jump(UserLoginFragment.class.getName(), "会员登录");
                                getActivity().finish();
                                return;
                            }
                            Notify.show(object.optString("message"));
                            return;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Notify.show("修改密码失败");
                }

                @Override
                protected void onFinish() {
                    hideLoading();
                }
            });
        }
    }

}
