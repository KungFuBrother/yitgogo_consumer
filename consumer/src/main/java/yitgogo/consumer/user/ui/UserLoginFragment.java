package yitgogo.consumer.user.ui;

import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
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
import com.smartown.jni.YtBox;
import com.smartown.yitian.gogo.R;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Content;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.tools.SignatureTool;
import yitgogo.consumer.user.model.User;
import yitgogo.consumer.view.Notify;

public class UserLoginFragment extends BaseNotifyFragment implements
        OnClickListener {

    EditText nameEdit, passwordEdit;
    Button loginButton;
    TextView registerButton, passwordButton;
    ImageView showPassword;
    boolean isShown = false;
    String phone = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_login);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(UserLoginFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(UserLoginFragment.class.getName());
    }

    private void init() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("phone")) {
                phone = bundle.getString("phone");
            }
        }
    }

    @Override
    protected void findViews() {
        nameEdit = (EditText) contentView.findViewById(R.id.user_login_name);
        passwordEdit = (EditText) contentView.findViewById(R.id.user_login_password);
        loginButton = (Button) contentView.findViewById(R.id.user_login_login);
        registerButton = (TextView) contentView.findViewById(R.id.user_login_register);
        passwordButton = (TextView) contentView.findViewById(R.id.user_login_findpassword);
        showPassword = (ImageView) contentView.findViewById(R.id.user_login_password_show);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        nameEdit.setText(phone);
    }

    @Override
    protected void registerViews() {
        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        passwordButton.setOnClickListener(this);
        showPassword.setOnClickListener(this);
    }

    private void login() {
        if (!isPhoneNumber(nameEdit.getText().toString())) {
            Notify.show("请输入正确的手机号");
        } else if (passwordEdit.length() == 0) {
            Notify.show("请输入密码");
        } else {
            loginUser();
        }
    }

    private void showPassword() {
        if (isShown) {
            passwordEdit.setTransformationMethod(PasswordTransformationMethod
                    .getInstance());
        } else {
            passwordEdit
                    .setTransformationMethod(HideReturnsTransformationMethod
                            .getInstance());
        }
        isShown = !isShown;
        if (isShown) {
            showPassword.setImageResource(R.drawable.ic_hide);
        } else {
            showPassword.setImageResource(R.drawable.ic_show);
        }
    }

    private void loginUser() {
        Request request = new Request();
        request.setUrl(API.API_USER_LOGIN);
        request.addRequestParam("phone", nameEdit.getText().toString());
        request.addRequestParam("password", getEncodedPassWord(passwordEdit.getText().toString()));
        request.setSaveCookie(true);
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading("正在登录...");
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            Notify.show("登录成功");

                            Content.saveStringContent(Parameters.CACHE_KEY_MONEY_SN, object.optString("cacheKey"));
                            JSONObject userObject = object.optJSONObject("object");
                            if (userObject != null) {
                                Content.saveStringContent(Parameters.CACHE_KEY_USER_JSON, userObject.toString());
                                Content.saveStringContent(Parameters.CACHE_KEY_USER_PASSWORD, getEncodedPassWord(passwordEdit.getText().toString().trim()));
                                User.init(getActivity());
                                initSignature();
                            }
                            getActivity().finish();
                            return;
                        }
                        Notify.show(object.optString("message"));
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Notify.show("登录失败");
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_login_login:
                login();
                break;

            case R.id.user_login_register:
                jump(UserRegisterFragment.class.getName(), "注册");
                getActivity().finish();
                break;

            case R.id.user_login_password_show:
                showPassword();
                break;

            case R.id.user_login_findpassword:
                jump(UserFindPasswordFragment.class.getName(), "重设密码");
                break;

            default:
                break;
        }
    }

    private void initSignature() {
        TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        SignatureTool.saveSignature(YtBox.encode(SignatureTool.key, User.getUser().getUseraccount() + "ytgogo" + telephonyManager.getDeviceId()));
    }

}
