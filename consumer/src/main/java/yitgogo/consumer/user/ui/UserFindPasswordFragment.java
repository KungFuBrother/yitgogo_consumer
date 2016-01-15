package yitgogo.consumer.user.ui;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import yitgogo.consumer.view.Notify;

public class UserFindPasswordFragment extends BaseNotifyFragment implements
        OnClickListener {

    EditText phoneEdit, smscodeEdit, passwordEdit, passwordConfirmEdit;
    TextView getSmscodeButton;
    ImageView showPassword;
    Button registerButton;
    boolean isShown = false;
    boolean isFinish = false;

    int smsTimes = 0;
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (isFinish) {
                return;
            }
            if (msg.what == 1) {
                if (smsTimes > 0) {
                    getSmscodeButton.setText(smsTimes + "s");
                    smsTimes--;
                    handler.sendEmptyMessageDelayed(1, 1000);
                } else {
                    getSmscodeButton.setEnabled(true);
                    getSmscodeButton.setTextColor(getResources().getColor(R.color.textColorSecond));
                    getSmscodeButton.setText("获取验证码");
                }
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_find_password);
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(UserFindPasswordFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(UserFindPasswordFragment.class.getName());
    }

    @Override
    public void onDestroy() {
        isFinish = true;
        super.onDestroy();
    }

    @Override
    protected void findViews() {
        phoneEdit = (EditText) contentView.findViewById(R.id.user_find_password_phone);
        smscodeEdit = (EditText) contentView.findViewById(R.id.user_find_password_smscode);
        passwordEdit = (EditText) contentView.findViewById(R.id.user_find_password_password);
        passwordConfirmEdit = (EditText) contentView.findViewById(R.id.user_find_password_password_confirm);
        getSmscodeButton = (TextView) contentView.findViewById(R.id.user_find_password_smscode_get);
        registerButton = (Button) contentView.findViewById(R.id.user_find_password_enter);
        showPassword = (ImageView) contentView.findViewById(R.id.user_find_password_password_show);
        registerViews();
    }

    @Override
    protected void registerViews() {
        getSmscodeButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        showPassword.setOnClickListener(this);
    }

    private void findPassword() {
        if (!isPhoneNumber(phoneEdit.getText().toString())) {
            Notify.show("请输入正确的手机号");
        } else if (smscodeEdit.length() != 6) {
            Notify.show("请输入您收到的验证码");
        } else if (passwordEdit.length() == 0) {
            Notify.show("请输入新密码");
        } else if (passwordConfirmEdit.length() == 0) {
            Notify.show("请确认新密码");
        } else if (!passwordEdit.getText().toString().equals(passwordConfirmEdit.getText().toString())) {
            Notify.show("两次输入的密码不相同 ");
        } else {
            resetPassword();
        }
    }

    private void showPassword() {
        if (isShown) {
            passwordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordConfirmEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            passwordEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            passwordConfirmEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
        isShown = !isShown;
        if (isShown) {
            showPassword.setImageResource(R.drawable.ic_hide);
        } else {
            showPassword.setImageResource(R.drawable.ic_show);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_find_password_smscode_get:
                getSmscode();
                break;

            case R.id.user_find_password_enter:
                findPassword();
                break;

            case R.id.user_find_password_password_show:
                showPassword();
                break;

            default:
                break;
        }
    }

    private void getSmscode() {
        if (isPhoneNumber(phoneEdit.getText().toString())) {
            getSmscodeButton.setEnabled(false);
            getSmscodeButton.setTextColor(getResources().getColor(R.color.textColorThird));
            Request request = new Request();
            request.setUrl(API.API_USER_FIND_PASSWORD_SMSCODE);
            request.addRequestParam("phone", phoneEdit.getText().toString());
            request.setSaveCookie(true);
            MissionController.startRequestMission(getActivity(), request, new RequestListener() {
                @Override
                protected void onStart() {
                    showLoading();
                }

                @Override
                protected void onFail(MissionMessage missionMessage) {
                    getSmscodeButton.setEnabled(true);
                    getSmscodeButton.setTextColor(getResources().getColor(R.color.textColorSecond));
                    getSmscodeButton.setText("获取验证码");
                    Notify.show("获取验证码失败");
                }

                @Override
                protected void onSuccess(RequestMessage requestMessage) {
                    if (!TextUtils.isEmpty(requestMessage.getResult())) {
                        try {
                            JSONObject object = new JSONObject(requestMessage.getResult());
                            if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                                Notify.show("验证码已发送至您的手机");
                                smsTimes = 60;
                                handler.sendEmptyMessage(1);
                                return;
                            }
                            getSmscodeButton.setEnabled(true);
                            getSmscodeButton.setTextColor(getResources().getColor(R.color.textColorSecond));
                            getSmscodeButton.setText("获取验证码");
                            Notify.show(object.optString("message"));
                            return;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    getSmscodeButton.setEnabled(true);
                    getSmscodeButton.setTextColor(getResources().getColor(R.color.textColorSecond));
                    getSmscodeButton.setText("获取验证码");
                    Notify.show("获取验证码失败");
                }

                @Override
                protected void onFinish() {
                    hideLoading();
                }
            });
        } else {
            Notify.show("请输入正确的手机号");
        }
    }

    private void resetPassword() {
        Request request = new Request();
        request.setUrl(API.API_USER_FIND_PASSWORD);
        request.addRequestParam("phone", phoneEdit.getText().toString());
        request.addRequestParam("smsCode", smscodeEdit.getText().toString());
        request.addRequestParam("password", getEncodedPassWord(passwordEdit.getText().toString()));
        request.setUseCookie(true);
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                Notify.show("重设密码失败");
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            Notify.show("重设密码成功，请使用新密码登录");
                            getActivity().finish();
                            return;
                        }
                        Notify.show(object.optString("message"));
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Notify.show("重设密码失败");
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

}
