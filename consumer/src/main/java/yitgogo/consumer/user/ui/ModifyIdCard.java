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
import yitgogo.consumer.user.model.User;
import yitgogo.consumer.view.Notify;

public class ModifyIdCard extends BaseNotifyFragment {

    LinearLayout editor;
    Button modify;
    TextView accountText, idOld, idNew;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_idcard);
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(ModifyIdCard.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(ModifyIdCard.class.getName());
    }

    protected void findViews() {
        accountText = (TextView) contentView
                .findViewById(R.id.user_info_idcard_account);
        idOld = (TextView) contentView.findViewById(R.id.user_info_idcard_old);
        idNew = (TextView) contentView.findViewById(R.id.user_info_idcard_new);
        modify = (Button) contentView.findViewById(R.id.user_idcard_modify);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        accountText.setText(User.getUser().getUseraccount());
        idOld.setText(User.getUser().getIdcard());
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
        String idcard = idNew.getText().toString().trim();
        if (TextUtils.isEmpty(idcard)) {
            Notify.show("请输入要绑定的身份证号码");
        } else if (idcard.length() != 15 & idcard.length() != 18) {
            Notify.show("身份证号码格式不正确");
        } else {
            Request request = new Request();
            request.setUrl(API.API_USER_MODIFY_IDCARD);
            request.addRequestParam("account", User.getUser().getUseraccount());
            request.addRequestParam("idcard", idcard);
            request.setUseCookie(true);
            MissionController.startRequestMission(getActivity(), request, new RequestListener() {
                @Override
                protected void onStart() {
                    showLoading();
                }

                @Override
                protected void onFail(MissionMessage missionMessage) {
                    Notify.show("绑定身份证失败");
                }

                @Override
                protected void onSuccess(RequestMessage requestMessage) {
                    if (!TextUtils.isEmpty(requestMessage.getResult())) {
                        try {
                            JSONObject object = new JSONObject(requestMessage.getResult());
                            if (object.getString("state").equalsIgnoreCase("SUCCESS")) {
                                Notify.show("绑定身份证成功");
                                getActivity().finish();
                                return;
                            }
                            Notify.show(object.getString("message"));
                            return;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Notify.show("绑定身份证失败");
                }

                @Override
                protected void onFinish() {
                    hideLoading();
                }
            });
        }
    }

}
