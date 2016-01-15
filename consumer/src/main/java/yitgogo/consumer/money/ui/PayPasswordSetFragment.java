package yitgogo.consumer.money.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

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
import yitgogo.consumer.money.model.MoneyAccount;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.view.Notify;

public class PayPasswordSetFragment extends BaseNotifyFragment {

    EditText idCardEditText, nameEditText;
    Button button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_money_password_set);
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(PayPasswordSetFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(PayPasswordSetFragment.class.getName());
    }

    @Override
    protected void findViews() {
        idCardEditText = (EditText) contentView.findViewById(R.id.set_password_idcard);
        nameEditText = (EditText) contentView.findViewById(R.id.set_password_name);
        button = (Button) contentView.findViewById(R.id.set_password_ok);
        initViews();
        registerViews();
    }

    @Override
    protected void registerViews() {
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setPassword();
            }
        });
    }

    private void setPassword() {
        if (TextUtils.isEmpty(idCardEditText.getText().toString().trim())) {
            Notify.show("请输入您的身份证号");
        } else if (TextUtils.isEmpty(nameEditText.getText().toString().trim())) {
            Notify.show("请输入您的真实姓名");
        } else {
            PayPasswordDialog passwordDialog = new PayPasswordDialog("设置支付密码",
                    true) {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!TextUtils.isEmpty(payPassword)) {
                        setPayPassword(payPassword);
                    }
                    super.onDismiss(dialog);
                }
            };
            passwordDialog.show(getFragmentManager(), null);
        }
    }

    private void setPayPassword(String paypwd) {
        Request request = new Request();
        request.setUrl(API.MONEY_PAY_PASSWORD_SET);
        request.setUseCookie(true);
        request.addRequestParam("idcard", idCardEditText.getText().toString());
        request.addRequestParam("realname", nameEditText.getText().toString());
        request.addRequestParam("paypwd", paypwd);
        request.addRequestParam("payaccount", MoneyAccount.getMoneyAccount().getPayaccount());
        request.addRequestParam("seckey", MoneyAccount.getMoneyAccount().getSeckey());
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
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("success")) {
                            JSONObject jsonObject = object.optJSONObject("databody");
                            if (jsonObject != null) {
                                if (jsonObject.optString("setpwd").equalsIgnoreCase("ok")) {
                                    Notify.show("设置支付密码成功");
                                    getActivity().finish();
                                    return;
                                }
                            }
                        }
                        Notify.show(object.optString("msg"));
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

}
