package yitgogo.consumer.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import yitgogo.consumer.local.model.ModelAddress;
import yitgogo.consumer.store.SelectAreaFragment;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.view.Notify;

/**
 * 用户收货地址管理（添加/修改）
 *
 * @author Tiger
 */
public class UserAddressEditFragment extends BaseNotifyFragment {

    EditText nameEditText, phoneEditText, addressEditText, telephoneEditText,
            postcodeEditText, emailEditText;
    TextView areaTextView;
    Button addButton;

    String addressId = "";
    String areaName = "", areaId = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_address_edit);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(UserAddressEditFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(UserAddressEditFragment.class.getName());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!TextUtils.isEmpty(addressId)) {
            getAddressDetail();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 22) {
            if (resultCode == 23) {
                areaName = data.getStringExtra("name");
                areaId = data.getStringExtra("id");
                areaTextView.setText(areaName);
            }
        }
    }

    private void init() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("addressId")) {
                addressId = bundle.getString("addressId");
            }
        }
    }

    @Override
    protected void findViews() {
        nameEditText = (EditText) contentView.findViewById(R.id.address_add_name);
        phoneEditText = (EditText) contentView.findViewById(R.id.address_add_phone);
        addressEditText = (EditText) contentView.findViewById(R.id.address_add_address);
        telephoneEditText = (EditText) contentView.findViewById(R.id.address_add_telephone);
        postcodeEditText = (EditText) contentView.findViewById(R.id.address_add_postcode);
        emailEditText = (EditText) contentView.findViewById(R.id.address_add_email);
        areaTextView = (TextView) contentView.findViewById(R.id.address_add_area);
        addButton = (Button) contentView.findViewById(R.id.address_add_add);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        if (addressId.length() > 0) {
            addButton.setText("修改");
        }
    }

    @Override
    protected void registerViews() {
        areaTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                jumpForResult(SelectAreaFragment.class.getName(), "选择区域", 22);
            }
        });
        addButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                editAddress();
            }
        });
    }

    private void editAddress() {
        if (nameEditText.length() <= 0) {
            Notify.show("请输入收货人姓名");
        } else if (phoneEditText.length() <= 0) {
            Notify.show("请输入收货人手机号");
        } else if (!isPhoneNumber(phoneEditText.getText().toString())) {
            Notify.show("请输入正确的手机号");
        } else if (areaId.length() <= 0) {
            Notify.show("请选择收货区域");
        } else if (addressEditText.length() <= 0) {
            Notify.show("请输入详细收货地址");
        } else if (telephoneEditText.length() > 0 & telephoneEditText.length() < 11) {
            Notify.show("请输入正确的固定电话号码");
        } else if (postcodeEditText.length() > 0 & postcodeEditText.length() < 6) {
            Notify.show("请输入正确的邮政编码");
        } else {
            if (addressId.length() > 0) {
                modifyAddress();
            } else {
                addAdress();
            }
        }
    }

    private void modifyAddress() {
        Request request = new Request();
        request.setUrl(API.API_USER_ADDRESS_MODIFY);
        request.addRequestParam("id", addressId);
        request.addRequestParam("personName", nameEditText.getText().toString());
        request.addRequestParam("phone", phoneEditText.getText().toString());
        request.addRequestParam("areaAddress", areaName);
        request.addRequestParam("areaId", areaId);
        request.addRequestParam("detailedAddress", addressEditText.getText().toString());
        request.addRequestParam("fixPhone", telephoneEditText.getText().toString());
        request.addRequestParam("postcode", postcodeEditText.getText().toString());
        request.addRequestParam("email", emailEditText.getText().toString());
        request.setUseCookie(true);
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                Notify.show("修改失败");
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            Notify.show("修改成功");
                            getActivity().finish();
                            return;
                        }
                        Notify.show(object.optString("message"));
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Notify.show("修改失败");
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

    private void getAddressDetail() {
        Request request = new Request();
        request.setUrl(API.API_USER_ADDRESS_DETAIL);
        request.addRequestParam("id", addressId);
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
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            JSONObject dataMap = object.optJSONObject("dataMap");
                            if (dataMap != null) {
                                JSONObject updateMemberAddress = dataMap.optJSONObject("updateMemberAddress");
                                ModelAddress address = new ModelAddress(updateMemberAddress);
                                nameEditText.setText(address.getPersonName());
                                areaName = address.getAreaAddress();
                                areaTextView.setText(areaName);
                                areaId = address.getAreaId();
                                addressEditText.setText(address.getDetailedAddress());
                                phoneEditText.setText(address.getPhone());
                                telephoneEditText.setText(address.getFixPhone());
                                postcodeEditText.setText(address.getPostcode());
                                emailEditText.setText(address.getEmail());
                            }
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

    private void addAdress() {
        Request request = new Request();
        request.setUrl(API.API_USER_ADDRESS_ADD);
        request.addRequestParam("id", addressId);
        request.addRequestParam("personName", nameEditText.getText().toString());
        request.addRequestParam("phone", phoneEditText.getText().toString());
        request.addRequestParam("areaAddress", areaName);
        request.addRequestParam("areaId", areaId);
        request.addRequestParam("detailedAddress", addressEditText.getText().toString());
        request.addRequestParam("fixPhone", telephoneEditText.getText().toString());
        request.addRequestParam("postcode", postcodeEditText.getText().toString());
        request.addRequestParam("email", emailEditText.getText().toString());
        request.setUseCookie(true);
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                Notify.show("添加收货地址失败");
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            Notify.show("添加收货地址成功");
                            getActivity().finish();
                            return;
                        }
                        Notify.show(object.optString("message"));
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Notify.show("添加收货地址失败");
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

}
