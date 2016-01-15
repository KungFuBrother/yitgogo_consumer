package yitgogo.consumer.user.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
import yitgogo.consumer.user.model.User;
import yitgogo.consumer.view.Notify;

public class ModifyPhone extends BaseNotifyFragment {

    Button modify;
    TextView accountText, phoneOldText, phoneNewText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_phone);
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(ModifyPhone.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(ModifyPhone.class.getName());
    }

    protected void findViews() {
        accountText = (TextView) contentView
                .findViewById(R.id.user_info_phone_account);
        phoneOldText = (TextView) contentView
                .findViewById(R.id.user_info_phone_old);
        phoneNewText = (TextView) contentView
                .findViewById(R.id.user_info_phone_new);
        modify = (Button) contentView.findViewById(R.id.user_phone_modify);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        accountText.setText(User.getUser().getUseraccount());
        phoneOldText.setText(User.getUser().getPhone());
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
        String newphone = phoneNewText.getText().toString().trim();
        if (newphone.length() == 0) {
            Notify.show("请输入要绑定的手机号");
        } else if (newphone.length() != 11) {
            Notify.show("手机号格式不正确");
        } else {
            Request request = new Request();
            request.setUrl(API.API_USER_MODIFY_PHONE);
            request.addRequestParam("account", User.getUser().getUseraccount());
            request.addRequestParam("newphone", newphone);
            request.setUseCookie(true);
            MissionController.startRequestMission(getActivity(), request, new RequestListener() {
                @Override
                protected void onStart() {
                    showLoading();
                }

                @Override
                protected void onFail(MissionMessage missionMessage) {
                    Notify.show("修改手机号失败");
                }

                @Override
                protected void onSuccess(RequestMessage requestMessage) {
                    if (!TextUtils.isEmpty(requestMessage.getResult())) {
                        try {
                            JSONObject object = new JSONObject(requestMessage.getResult());
                            if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                                Notify.show("修改手机号成功");
                                getActivity().finish();
                                return;
                            }
                            Notify.show(object.optString("message"));
                            return;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Notify.show("修改手机号失败");
                }

                @Override
                protected void onFinish() {
                    hideLoading();
                }
            });
        }
    }

}
