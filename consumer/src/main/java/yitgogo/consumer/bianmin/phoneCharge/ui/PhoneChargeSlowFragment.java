package yitgogo.consumer.bianmin.phoneCharge.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import yitgogo.consumer.bianmin.ModelBianminOrderResult;
import yitgogo.consumer.bianmin.ModelChargeInfo;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.tools.ScreenUtil;
import yitgogo.consumer.user.model.User;
import yitgogo.consumer.view.InnerGridView;
import yitgogo.consumer.view.Notify;

public class PhoneChargeSlowFragment extends BaseNotifyFragment {

    EditText numberEditText;
    ImageView chooseImageView;
    InnerGridView amountGridView, timeGridView;
    TextView areaTextView, amountTextView, chargeButton;
    AmountAdapter amountAdapter;
    TimeAdapter timeAdapter;

    int[] amounts = {30, 50, 100};
    float[] times = {0.5f, 4, 12, 24, 48, 72};
    int amountSelection = 0, timeSelection = 0;
    ModelChargeInfo chargeInfo = new ModelChargeInfo();
    String phoneNumber = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_bianmin_phone_charge_slow);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(PhoneChargeSlowFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(PhoneChargeSlowFragment.class.getName());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getChargeInfo();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor cursor = getActivity().managedQuery(contactData, null,
                            null, null, null);
                    cursor.moveToFirst();
                    String num = this.getContactPhone(cursor);
                    numberEditText.setText(num);
                }
                break;

            default:
                break;
        }
    }

    private void init() {
        amountAdapter = new AmountAdapter();
        timeAdapter = new TimeAdapter();
    }

    @Override
    protected void findViews() {
        numberEditText = (EditText) contentView
                .findViewById(R.id.phone_charge_slow_number);
        chooseImageView = (ImageView) contentView
                .findViewById(R.id.phone_charge_slow_number_choose);
        amountGridView = (InnerGridView) contentView
                .findViewById(R.id.phone_charge_slow_amounts);
        timeGridView = (InnerGridView) contentView
                .findViewById(R.id.phone_charge_slow_time);
        areaTextView = (TextView) contentView
                .findViewById(R.id.phone_charge_slow_area);
        amountTextView = (TextView) contentView
                .findViewById(R.id.phone_charge_slow_amount);
        chargeButton = (TextView) contentView
                .findViewById(R.id.phone_charge_slow_charge);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        numberEditText.setText(User.getUser().getPhone());
        amountGridView.setAdapter(amountAdapter);
        timeGridView.setAdapter(timeAdapter);
    }

    @Override
    protected void registerViews() {
        numberEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                getChargeInfo();
            }
        });
        chooseImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                choosePhone();
            }
        });
        amountGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                amountSelection = arg2;
                amountAdapter.notifyDataSetChanged();
                getChargeInfo();
            }
        });
        timeGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                timeSelection = arg2;
                timeAdapter.notifyDataSetChanged();
                getChargeInfo();
            }
        });
        chargeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                charge();
            }
        });
    }

    private void choosePhone() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    private String getContactPhone(Cursor cursor) {
        int phoneColumn = cursor
                .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
        int phoneNum = cursor.getInt(phoneColumn);
        String result = "";
        if (phoneNum > 0) {
            // 获得联系人的ID号
            int idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            String contactId = cursor.getString(idColumn);
            // 获得联系人电话的cursor
            Cursor phone = getActivity().getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "="
                            + contactId, null, null);
            if (phone.moveToFirst()) {
                for (; !phone.isAfterLast(); phone.moveToNext()) {
                    int index = phone
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int typeindex = phone
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
                    int phone_type = phone.getInt(typeindex);
                    String phoneNumber = phone.getString(index);
                    result = phoneNumber.replaceAll("-", "");
                    // switch (phone_type) {//此处请看下方注释
                    // case 2:
                    // result = phoneNumber;
                    // break;
                    //
                    // default:
                    // break;
                    // }
                }
                if (!phone.isClosed()) {
                    phone.close();
                }
            }
        }
        return result;
    }

    private void getChargeInfo() {
        phoneNumber = numberEditText.getText().toString();
        if (isPhoneNumber(phoneNumber)) {
            getPhoneChargeInfo();
        } else {
            amountTextView.setText("");
        }
    }

    private void charge() {
        if (isPhoneNumber(phoneNumber)) {
            if (chargeInfo.getSellprice() > 0) {
                getPhoneCharge();
            }
        } else {
            Notify.show("请输入正确的手机号");
        }
    }

    class AmountAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return amounts.length;
        }

        @Override
        public Object getItem(int position) {
            return amounts[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.list_class_min,
                        null);
                holder.textView = (TextView) convertView
                        .findViewById(R.id.class_min_name);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        ScreenUtil.dip2px(36));
                holder.textView.setLayoutParams(layoutParams);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (amountSelection == position) {
                holder.textView.setTextColor(getResources().getColor(
                        R.color.textColorCompany));
                holder.textView
                        .setBackgroundResource(R.drawable.back_white_rec_border_orange);
            } else {
                holder.textView.setTextColor(getResources().getColor(
                        R.color.textColorSecond));
                holder.textView
                        .setBackgroundResource(R.drawable.selector_white_rec_border);
            }
            holder.textView.setText(amounts[position] + "元");
            return convertView;
        }

        class ViewHolder {
            TextView textView;
        }
    }

    class TimeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return times.length;
        }

        @Override
        public Object getItem(int position) {
            return times[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.list_class_min,
                        null);
                holder.textView = (TextView) convertView
                        .findViewById(R.id.class_min_name);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        ScreenUtil.dip2px(36));
                holder.textView.setLayoutParams(layoutParams);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (timeSelection == position) {
                holder.textView.setTextColor(getResources().getColor(
                        R.color.textColorCompany));
                holder.textView
                        .setBackgroundResource(R.drawable.back_white_rec_border_orange);
            } else {
                holder.textView.setTextColor(getResources().getColor(
                        R.color.textColorSecond));
                holder.textView
                        .setBackgroundResource(R.drawable.selector_white_rec_border);
            }
            if (times[position] > 1) {
                holder.textView.setText((int) times[position] + "小时");
            } else {
                holder.textView.setText(times[position] + "小时");
            }
            return convertView;
        }

        class ViewHolder {
            TextView textView;
        }
    }

    /**
     * 查询充值信息-快充
     *
     * @author Tiger
     * @Url http://192.168.8.14:8888/api/facilitate/recharge/findPhoneInfo
     * @Parameters [phoneno=13032889558, pervalue=30, mctype=0.5]
     * @Result {"message":"暂时不支持此类号码的充值","state":"ERROR"}
     * @Result {"message":"ok","state":"SUCCESS","cacheKey":null,"dataList":[],
     * "totalCount"
     * :1,"dataMap":{},"object":{"cardid":"170227","cardname"
     * :"全国电信话费100元快充（活动通道）"
     * ,"inprice":null,"sellprice":"100.00","area":"四川南充电信"}}
     */
    private void getPhoneChargeInfo() {
        Request request = new Request();
        request.setUrl(API.API_BIANMIN_PHONE_CHARGE_INFO);
        request.addRequestParam("phoneno", phoneNumber);
        request.addRequestParam("pervalue",
                amounts[amountSelection] + "");
        if (times[timeSelection] > 1) {
            request.addRequestParam("mctype",
                    (int) times[timeSelection] + "");
        } else {
            request.addRequestParam("mctype",
                    times[timeSelection] + "");
        }
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                Notify.show(missionMessage.getMessage());

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            JSONObject infoObject = object.optJSONObject("object");
                            chargeInfo = new ModelChargeInfo(infoObject);
                            if (chargeInfo.getSellprice() > 0) {
                                areaTextView.setText(chargeInfo.getArea());
                                amountTextView.setText(Parameters.CONSTANT_RMB
                                        + decimalFormat.format(chargeInfo
                                        .getSellprice()));
                                return;
                            }
                        }
                        amountTextView.setText("");
                        Notify.show(object.optString("message"));
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


    /**
     * 添加充值订单-快充
     *
     * @author Tiger
     * @Url http://192.168.8.14:8888/api/facilitate/recharge/addOrder/addPhoneOrder
     * @Parameters [phoneno=18048831447, pervalue=100, mctype=12]
     * @Result {"message":"ok","state" :"SUCCESS","cacheKey":null,"dataList":[
     * ],"totalCount":1,"dataMap"
     * :{"orderNumber":"YT3394858942"},"object":null}
     */

    private void getPhoneCharge() {
        Request request = new Request();
        request.setUrl(API.API_BIANMIN_PHONE_CHARGE);
        request.addRequestParam("phoneno", phoneNumber);
        request.addRequestParam("pervalue",
                amounts[amountSelection] + "");
        if (times[timeSelection] > 1) {
            request.addRequestParam("mctype",
                    (int) times[timeSelection] + "");
        } else {
            request.addRequestParam("mctype",
                    times[timeSelection] + "");
        }
        if (User.getUser().isLogin()) {
            request.addRequestParam("memberAccount", User
                    .getUser().getUseraccount());
        }
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                Notify.show(missionMessage.getMessage());

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            JSONObject dataMap = object.optJSONObject("dataMap");
                            ModelBianminOrderResult orderResult = new ModelBianminOrderResult(
                                    dataMap);
                            if (orderResult != null) {
                                if (orderResult.getSellPrice() > 0) {
                                    payMoney(orderResult);
                                    getActivity().finish();
                                    return;
                                }
                            }
                        }
                        Notify.show(object.optString("message"));
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
