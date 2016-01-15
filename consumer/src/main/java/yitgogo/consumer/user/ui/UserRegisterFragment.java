package yitgogo.consumer.user.ui;

import android.content.Intent;
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

import com.dtr.zxing.activity.CaptureActivity;
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
import yitgogo.consumer.store.model.Store;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.view.Notify;

public class UserRegisterFragment extends BaseNotifyFragment implements
        OnClickListener {

    EditText phoneEdit, smscodeEdit, passwordEdit, passwordConfirmEdit,
            inviteCodeEditText;
    TextView getSmscodeButton;
    ImageView showPassword, scanButton;
    Button registerButton;
    boolean isShown = false, isFinish = false;
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
        setContentView(R.layout.fragment_user_register);
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(UserRegisterFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(UserRegisterFragment.class.getName());
    }

    @Override
    public void onDestroy() {
        isFinish = true;
        super.onDestroy();
    }

    @Override
    protected void findViews() {
        phoneEdit = (EditText) contentView
                .findViewById(R.id.user_register_phone);
        smscodeEdit = (EditText) contentView
                .findViewById(R.id.user_register_smscode);
        passwordEdit = (EditText) contentView
                .findViewById(R.id.user_register_password);
        passwordConfirmEdit = (EditText) contentView
                .findViewById(R.id.user_register_password_confirm);
        inviteCodeEditText = (EditText) contentView
                .findViewById(R.id.user_register_invitecode);
        scanButton = (ImageView) contentView
                .findViewById(R.id.user_register_invitecode_scan);
        getSmscodeButton = (TextView) contentView
                .findViewById(R.id.user_register_smscode_get);
        registerButton = (Button) contentView
                .findViewById(R.id.user_register_enter);
        showPassword = (ImageView) contentView
                .findViewById(R.id.user_register_password_show);
        registerViews();
    }

    @Override
    protected void registerViews() {
        getSmscodeButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        showPassword.setOnClickListener(this);
        onBackButtonClick(new OnClickListener() {

            @Override
            public void onClick(View v) {
                jump(UserLoginFragment.class.getName(), "会员登录");
                getActivity().finish();
            }
        });
        scanButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CaptureActivity.class);
                startActivityForResult(intent, 5);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 5) {
            if (data != null) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    if (bundle.containsKey("userCode")) {
                        inviteCodeEditText
                                .setText(bundle.getString("userCode"));
                    }
                }
            }
        }
    }

    private void register() {
        if (!isPhoneNumber(phoneEdit.getText().toString())) {
            Notify.show("请输入正确的手机号");
        } else if (smscodeEdit.length() != 6) {
            Notify.show("请输入您收到的验证码");
        } else if (passwordEdit.length() == 0) {
            Notify.show("请输入密码");
        } else if (passwordConfirmEdit.length() == 0) {
            Notify.show("请确认密码");
        } else if (!passwordEdit.getText().toString().equals(passwordConfirmEdit.getText().toString())) {
            Notify.show("两次输入的密码不相同 ");
        } else {
            registerUser();
        }
    }

    private void getSmscode() {
        if (isPhoneNumber(phoneEdit.getText().toString())) {
            getSmscodeButton.setEnabled(false);
            getSmscodeButton.setTextColor(getResources().getColor(R.color.textColorThird));
            Request request = new Request();
            request.setUrl(API.API_USER_REGISTER_SMSCODE);
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
            case R.id.user_register_smscode_get:
                getSmscode();
                break;

            case R.id.user_register_enter:
                register();
                break;

            case R.id.user_register_password_show:
                showPassword();
                break;

            default:
                break;
        }
    }

    private void registerUser() {
        Request request = new Request();
        request.setUrl(API.API_USER_REGISTER);
        request.addRequestParam("phone", phoneEdit.getText().toString());
        request.addRequestParam("smsCode", smscodeEdit.getText().toString());
        request.addRequestParam("password", getEncodedPassWord(passwordEdit.getText().toString()));
        request.addRequestParam("refereeCode", inviteCodeEditText.getText().toString());
        request.addRequestParam("spNo", Store.getStore().getStoreNumber());
        request.setUseCookie(true);
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                Notify.show("注册失败");
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            Notify.show("注册成功");
                            Bundle bundle = new Bundle();
                            bundle.putString("phone", phoneEdit.getText().toString());
                            jump(UserLoginFragment.class.getName(), "会员登录", bundle);
                            getActivity().finish();
                            return;
                        }
                        Notify.show(object.optString("message"));
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Notify.show("注册失败");
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

}
