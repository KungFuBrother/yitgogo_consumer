package yitgogo.consumer.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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

import java.util.regex.Pattern;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.main.ui.MainActivity;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.view.Notify;

/*
 * 店铺注册
 */
public class OpenStoreFragment extends BaseNotifyFragment implements
        OnClickListener {
    LayoutInflater inflater;
    /*
     * 参数：shopname(店铺名)，businessno(营业执照号)，cardnumber（身份证号），contacts（联系人），
     * serviceaddress
     * （店铺地址），contactphone（联系电话），contacttelephone（联系座机），email（邮箱），reid
     * （区域id），starttime(服务开始时间)，endtime（服务结束时间）
     */
    EditText et_store_shopname, et_store_businessno, et_store_cardnumber,
            et_store_contacts, et_store_serviceaddress, et_store_contactphone,
            et_store_contacttelephone, et_store_email;
    // 确认注册
    TextView tv_register;

    /*
     * 正则表达式
     */
    // 1.身份证
    String is_cardnumber = "^(\\d{14}[0-9a-zA-Z])|(\\d{17}[0-9a-zA-Z])$";
    // 3.email
    String is_email = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)";

    // 注册店铺参数：shopname(店铺名)，businessno(营业执照号)，cardnumber（身份证号），contacts（联系人），serviceaddress（店铺地址），contactphone（联系电话），contacttelephone（联系座机），email（邮箱），reid（区域id），starttime(服务开始时间)，endtime（服务结束时间）
    String shopname = null;
    String businessno = null;
    String cardnumber = null;
    String contacts = null;
    String serviceaddress = null;
    String contactphone = null;
    String contacttelephone = null;
    String email = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_mian);
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(OpenStoreFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(OpenStoreFragment.class.getName());
    }

    @Override
    protected void findViews() {
        et_store_shopname = (EditText) contentView
                .findViewById(R.id.store_shopname);
        et_store_businessno = (EditText) contentView
                .findViewById(R.id.store_businessno);
        et_store_cardnumber = (EditText) contentView
                .findViewById(R.id.store_cardnumber);

        et_store_contacts = (EditText) contentView
                .findViewById(R.id.store_contacts);
        et_store_serviceaddress = (EditText) contentView
                .findViewById(R.id.store_serviceaddress);
        et_store_contactphone = (EditText) contentView
                .findViewById(R.id.store_contactphone);

        et_store_contacttelephone = (EditText) contentView
                .findViewById(R.id.store_contacttelephone);
        et_store_email = (EditText) contentView.findViewById(R.id.store_email);
        tv_register = (TextView) contentView.findViewById(R.id.store_register);
        tv_register.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.store_register) {
            shopname = et_store_shopname.getText().toString();
            businessno = et_store_businessno.getText().toString();
            cardnumber = et_store_cardnumber.getText().toString();
            contacts = et_store_contacts.getText().toString();
            serviceaddress = et_store_serviceaddress.getText().toString();
            contactphone = et_store_contactphone.getText().toString();
            contacttelephone = et_store_contacttelephone.getText().toString();
            email = et_store_email.getText().toString();
            // 判断
            if (shopname.length() < 1) {
                Notify.show("请输入店铺名称");
                return;
            }
            if (businessno.length() < 1) {
                Notify.show("请输入营业执照号");
                return;
            }

            if (isCardnumber(cardnumber)) {
                Notify.show("请输入正确的身份证号码");
                return;
            }
            if (contacts.length() < 1) {
                Notify.show("请输入联系人");
                return;
            }
            if (serviceaddress.length() < 1) {
                Notify.show("请输入店铺地址");
                return;
            }
            if (!isPhoneNumber(contactphone)) {
                Notify.show("请输入正确的手机号码");
                return;
            }
            if (contacttelephone.length() < 8) {
                Notify.show("请输入正确的联系座机号码");
                return;
            }
            if (isEmail(email)) {
                Notify.show("请输入正确的邮箱地址");
                return;
            }
            registerStore();
        }
    }

    /**
     * 跳转到登录界面
     */
    private void jumpToMain() {
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }

    /*
     * 正则表达式判断
     */
    // 身份证
    public boolean isCardnumber(String s) {
        Pattern p = Pattern.compile(is_cardnumber);

        return !(p.matcher(s).matches());
    }

    // email
    public boolean isEmail(String s) {
        Pattern p = Pattern.compile(is_email);
        return !(p.matcher(s).matches());
    }

    private void registerStore() {
        Request request = new Request();
        request.setUrl(API.API_USER_OPEN_STORE);
        request.addRequestParam("shopname", shopname);
        request.addRequestParam("businessno", businessno);
        request.addRequestParam("cardnumber", cardnumber);
        request.addRequestParam("contacts", contacts);
        request.addRequestParam("serviceaddress", serviceaddress);
        request.addRequestParam("contactphone", contactphone);
        request.addRequestParam("contacttelephone", contacttelephone);
        request.addRequestParam("email", email);
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                Notify.show("申请开店失败");
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            Notify.show("申请开店成功");
                            return;
                        }
                        Notify.show(object.optString("message"));
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Notify.show("申请开店失败");
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

}
