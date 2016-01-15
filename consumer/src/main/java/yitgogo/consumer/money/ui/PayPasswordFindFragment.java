package yitgogo.consumer.money.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
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
import yitgogo.consumer.money.model.MoneyAccount;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.view.Notify;

public class PayPasswordFindFragment extends BaseNotifyFragment {

    TextView getCodeButton;
    EditText idcardEditText, smsCodeEditText;
    Button button;
    boolean isFinish = false;
    int smsTimes = 0;
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (isFinish) {
                return;
            }
            if (msg.what == 1) {
                if (smsTimes > 0) {
                    getCodeButton.setText(smsTimes + "s");
                    smsTimes--;
                    handler.sendEmptyMessageDelayed(1, 1000);
                } else {
                    getCodeButton.setEnabled(true);
                    getCodeButton.setTextColor(getResources().getColor(R.color.textColorSecond));
                    getCodeButton.setText("获取验证码");
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_money_password_find);
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(PayPasswordFindFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(PayPasswordFindFragment.class.getName());
    }

    @Override
    public void onDestroy() {
        isFinish = true;
        super.onDestroy();
    }

    @Override
    protected void findViews() {
        idcardEditText = (EditText) contentView
                .findViewById(R.id.find_pay_password_idcard);
        getCodeButton = (TextView) contentView
                .findViewById(R.id.find_pay_password_smscode_get);
        smsCodeEditText = (EditText) contentView
                .findViewById(R.id.find_pay_password_smscode);
        button = (Button) contentView.findViewById(R.id.find_pay_password_ok);
        registerViews();
    }

    @Override
    protected void registerViews() {
        getCodeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getSmsCode();
            }
        });
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                findPassword();
            }
        });
    }

    private void findPassword() {
        if (TextUtils.isEmpty(idcardEditText.getText().toString().trim())) {
            Notify.show("请输入您的身份证号码");
        } else if (TextUtils.isEmpty(smsCodeEditText.getText().toString().trim())) {
            Notify.show("请输入您收到的验证码");
        } else {
            final Request request = new Request();
            request.setUrl(API.MONEY_PAY_PASSWORD_FIND);
            request.setUseCookie(true);
            request.addRequestParam("seckey", MoneyAccount.getMoneyAccount().getSeckey());
            request.addRequestParam("cardid", idcardEditText.getText().toString());
            request.addRequestParam("mcode", smsCodeEditText.getText().toString());
            PayPasswordDialog newPasswordDialog = new PayPasswordDialog("请输入新支付密码", false) {
                public void onDismiss(DialogInterface dialog) {
                    if (!TextUtils.isEmpty(payPassword)) {
                        request.addRequestParam("newpaypwd", payPassword);
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
                                            JSONObject databody = object.optJSONObject("databody");
                                            if (databody != null) {
                                                if (databody.optString("paypwd").equalsIgnoreCase("ok")) {
                                                    Notify.show("修改支付密码成功");
                                                    getActivity().finish();
                                                    return;
                                                }
                                            }
                                            Notify.show("修改支付密码失败");
                                            return;
                                        }
                                        Notify.show(object.optString("msg"));
                                        return;
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                Notify.show("修改支付密码失败");
                            }

                            @Override
                            protected void onFinish() {
                                hideLoading();
                            }
                        });
                    }
                    super.onDismiss(dialog);
                }
            };
            newPasswordDialog.show(getFragmentManager(), null);
        }
    }

    private void getSmsCode() {
        getCodeButton.setEnabled(false);
        getCodeButton.setTextColor(getResources().getColor(R.color.textColorThird));
        Request request = new Request();
        request.setUrl(API.MONEY_SMS_CODE);
        request.setUseCookie(true);
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                getCodeButton.setEnabled(true);
                getCodeButton.setTextColor(getResources().getColor(R.color.textColorSecond));
                getCodeButton.setText("获取验证码");
                Notify.show("获取验证码失败");
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("success")) {
                            JSONObject databody = object.optJSONObject("databody");
                            if (databody != null) {
                                if (databody.optString("send").equalsIgnoreCase("ok")) {
                                    Notify.show("已将验证码发送至尾号为 " + databody.optString("mobile") + " 的手机");
                                    smsTimes = 60;
                                    handler.sendEmptyMessage(1);
                                    return;
                                }
                            }
                        }
                        getCodeButton.setEnabled(true);
                        getCodeButton.setTextColor(getResources().getColor(R.color.textColorSecond));
                        getCodeButton.setText("获取验证码");
                        Notify.show(object.optString("msg"));
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                getCodeButton.setEnabled(true);
                getCodeButton.setTextColor(getResources().getColor(R.color.textColorSecond));
                getCodeButton.setText("获取验证码");
                Notify.show("获取验证码失败");
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

}
