package yitgogo.consumer.money.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartown.controller.mission.MissionController;
import com.smartown.controller.mission.MissionMessage;
import com.smartown.controller.mission.Request;
import com.smartown.controller.mission.RequestListener;
import com.smartown.controller.mission.RequestMessage;
import com.smartown.yitian.gogo.R;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.money.model.ModelBankCard;
import yitgogo.consumer.money.model.MoneyAccount;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.user.model.User;
import yitgogo.consumer.user.ui.UserLoginFragment;
import yitgogo.consumer.view.InnerListView;
import yitgogo.consumer.view.Notify;

public class PayFragment extends BaseNotifyFragment {

    public static final int ORDER_TYPE_YY = 1;
    public static final int ORDER_TYPE_YD = 2;
    public static final int ORDER_TYPE_LP = 3;
    public static final int ORDER_TYPE_LS = 4;
    public static final int ORDER_TYPE_BM = 5;
    public static final int ORDER_TYPE_SN = 6;
    /**
     * 订单数据
     */
    String orderNumbers = "";
    double totalMoney = 0;
    int orderType = 1;
    int productCount = 1;
    String serialNumber = "";
    InnerListView bankCardListView;
    TextView orderNumberTextView, amountTextView, payButton;
    SimpleDateFormat serialNumberFormat = new SimpleDateFormat(
            "yyyyMMddHHmmssSSS");
    ModelBankCard selectedBankCard = new ModelBankCard();
    BindResult bindResult = new BindResult();

    List<ModelBankCard> bankCards;
    BandCardAdapter bandCardAdapter;

    LinearLayout addBankCardButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_pay);
        init();
        findViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(PayFragment.class.getName());
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(PayFragment.class.getName());
        if (User.getUser().isLogin()) {
            loginMoney();
        } else {
            jump(UserLoginFragment.class.getName(), "会员登录");
            getActivity().finish();
        }
    }

    private void init() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("orderNumbers")) {
                List<String> orderNumbers = bundle.getStringArrayList("orderNumbers");
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < orderNumbers.size(); i++) {
                    if (i > 0) {
                        builder.append(",");
                    }
                    builder.append(orderNumbers.get(i));
                }
                this.orderNumbers = builder.toString();
            }
            if (bundle.containsKey("totalMoney")) {
                totalMoney = bundle.getDouble("totalMoney");
            }
            if (bundle.containsKey("orderType")) {
                orderType = bundle.getInt("orderType");
            }
            if (bundle.containsKey("productCount")) {
                productCount = bundle.getInt("productCount");
            }
        }
        bankCards = new ArrayList<>();
        bandCardAdapter = new BandCardAdapter();
    }

    protected void findViews() {
        orderNumberTextView = (TextView) contentView
                .findViewById(R.id.pay_order_number);
        bankCardListView = (InnerListView) contentView
                .findViewById(R.id.pay_bankcards);
        amountTextView = (TextView) contentView.findViewById(R.id.pay_amount);
        payButton = (TextView) contentView.findViewById(R.id.pay_pay);
        addBankCardButton = (LinearLayout) layoutInflater.inflate(
                R.layout.list_pay_bank_card_add, null);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        orderNumberTextView.setText(orderNumbers.toString());
        amountTextView.setText(Parameters.CONSTANT_RMB
                + decimalFormat.format(totalMoney));
        bankCardListView.addFooterView(addBankCardButton);
        bankCardListView.setAdapter(bandCardAdapter);
    }

    @Override
    protected void registerViews() {
        bankCardListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                selectedBankCard = bankCards.get(arg2);
                bandCardAdapter.notifyDataSetChanged();
            }
        });
        payButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (totalMoney > 0) {
                    if (TextUtils.isEmpty(selectedBankCard.getId())) {
                        Notify.show("请选择付款方式");
                    } else {
                        if (selectedBankCard.getCardType().contains("储蓄")) {
                            inputPayPassword(1);
                        } else if (selectedBankCard.getCardType().contains("信用")) {
                            new CreditInfoDialog().show(getFragmentManager(), null);
                        } else if (selectedBankCard.getCardType().contains("钱袋子")) {
                            inputPayPassword(2);
                        }
                    }
                }
            }
        });
        addBankCardButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                jump(BankCardBindFragment.class.getName(), "绑定银行卡");
            }
        });
    }

    private void pay() {
        serialNumber = serialNumberFormat.format(new Date(System.currentTimeMillis()));
        getSmsCode();
    }

    /**
     * @param type 1:银行卡支付
     *             <p/>
     *             2:钱袋子月支付
     */
    private void inputPayPassword(int type) {
        final int payType = type;
        PayPasswordDialog passwordDialog = new PayPasswordDialog("请输入支付密码",
                false) {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (!TextUtils.isEmpty(payPassword)) {
                    switch (payType) {
                        case 1:
                            verifyPayPassword(payPassword);
                            break;
                        case 2:
                            payBalance(payPassword);
                            break;
                        default:
                            break;
                    }
                }
                super.onDismiss(dialog);
            }
        };
        passwordDialog.show(getFragmentManager(), null);
    }

    private void paySuccess() {
        Notify.show("付款成功");
        showOrder(orderType);
        getActivity().finish();
    }

    private String getShortCardNumber(String cardNumber) {
        if (cardNumber.length() > 10) {
            return cardNumber.substring(0, 6)
                    + cardNumber.substring(cardNumber.length() - 4,
                    cardNumber.length());
        }
        return "";
    }

    private void loginMoney() {
        Request request = new Request();
        request.setUrl(API.MONEY_LOGIN);
        request.addRequestParam("sn", User.getUser().getCacheKey());
        request.setSaveCookie(true);
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                Notify.show("暂无法进入钱袋子，请稍候再试");
                getActivity().finish();
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                MoneyAccount.init(null);
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("success")) {
                            MoneyAccount.init(object.optJSONObject("databody"));
                            if (MoneyAccount.getMoneyAccount().isLogin()) {
                                getBankCards();
                            } else {
                                getActivity().finish();
                            }
                            return;
                        }
                        Notify.show(object.optString("msg"));
                        getActivity().finish();
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Notify.show("暂无法进入钱袋子，请稍候再试");
                getActivity().finish();
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

    private void getBankCards() {
        bankCards.clear();
        bandCardAdapter.notifyDataSetChanged();
        Request request = new Request();
        request.setUrl(API.MONEY_BANK_BINDED);
        request.addRequestParam("sn", User.getUser().getCacheKey());
        request.addRequestParam("memberid", User.getUser().getUseraccount());
        request.setUseCookie(true);
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                Notify.show("获取绑定的银行卡信息失败！");
                getActivity().finish();
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("success")) {
                            JSONArray array = object.optJSONArray("databody");
                            if (array != null) {
                                for (int i = 0; i < array.length(); i++) {
                                    bankCards.add(new ModelBankCard(array.optJSONObject(i)));
                                }
                                ModelBankCard bankCard = new ModelBankCard("钱袋子余额");
                                bankCards.add(0, bankCard);
                                bandCardAdapter.notifyDataSetChanged();
                            }
                            return;
                        }
                        Notify.show(object.optString("msg"));
                        getActivity().finish();
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Notify.show("获取绑定的银行卡信息失败！");
                getActivity().finish();
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

    private void verifyPayPassword(String payPassword) {
        Request request = new Request();
        request.setUrl(API.MONEY_PAY_PASSWORD_VALIDATE);
        request.addRequestParam("sn", User.getUser().getCacheKey());
        request.addRequestParam("payaccount", MoneyAccount.getMoneyAccount().getPayaccount());
        request.addRequestParam("paypwd", payPassword);
        request.setUseCookie(true);
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                Notify.show("验证支付密码失败，请重试");
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("success")) {
                            JSONObject jsonObject = object.optJSONObject("databody");
                            if (jsonObject != null) {
                                if (jsonObject.optBoolean("vli")) {
                                    pay();
                                } else {
                                    Notify.show("支付密码错误");
                                }
                            }
                            return;
                        }
                        Notify.show(object.optString("msg"));
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Notify.show("验证支付密码失败，请重试");
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

    private void getSmsCode() {
        Request request = new Request();
        request.setUrl(API.API_PAY_BIND);
        request.addRequestParam("pan", selectedBankCard.getBanknumber());
        request.addRequestParam("expiredDate", selectedBankCard.getExpiredDate());
        request.addRequestParam("cvv2", selectedBankCard.getCvv2());
        request.addRequestParam("amount", decimalFormat.format(totalMoney));
        request.addRequestParam("externalRefNumber", serialNumber);
        request.addRequestParam("customerId", selectedBankCard.getOrg());
        request.addRequestParam("cardHolderName", selectedBankCard.getCradname());
        request.addRequestParam("cardHolderId", selectedBankCard.getIdCard());
        request.addRequestParam("phoneNO", selectedBankCard.getMobile());
        request.addRequestParam("bankCode", selectedBankCard.getBank().getCode());
        if (selectedBankCard.getCardType().equalsIgnoreCase("储蓄卡")) {
            request.addRequestParam("isBankType", "1");
        } else {
            request.addRequestParam("isBankType", "2");
        }
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
                                bindResult = new BindResult(dataMap);
                                if (bindResult.getResponseCode().equals("00")) {
                                    new SmsDialog().show(getFragmentManager(), null);
                                    return;
                                }
                                if (TextUtils.isEmpty(bindResult.getResponseTextMessage())) {
                                    Notify.show("获取验证码失败");
                                } else {
                                    Notify.show(bindResult.getResponseTextMessage());
                                }
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Notify.show("获取验证码失败");
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

    private void payFirstTime(String validCode) {
        Request request = new Request();
        request.setUrl(API.API_PAY_FIRST_TIME);
        request.addRequestParam("payInfoType", String.valueOf(orderType));
        request.addRequestParam("orderNumber", orderNumbers);
        request.addRequestParam("cardNo", selectedBankCard.getBanknumber());
        request.addRequestParam("externalRefNumber", serialNumber);
        request.addRequestParam("storableCardNo", getShortCardNumber(selectedBankCard.getBanknumber()));
        request.addRequestParam("expiredDate", selectedBankCard.getExpiredDate());
        request.addRequestParam("cvv2", selectedBankCard.getCvv2());
        request.addRequestParam("amount", decimalFormat.format(totalMoney));
        request.addRequestParam("customerId", selectedBankCard.getOrg());
        request.addRequestParam("cardHolderName", selectedBankCard.getCradname());
        request.addRequestParam("cardHolderId", selectedBankCard.getIdCard());
        request.addRequestParam("phone", selectedBankCard.getMobile());
        request.addRequestParam("validCode", validCode);
        if (!TextUtils.isEmpty(bindResult.getToken())) {
            request.addRequestParam("token", bindResult.getToken());
        }
        request.addRequestParam("bankCode", selectedBankCard.getBank().getCode());
        if (selectedBankCard.getCardType().equalsIgnoreCase("储蓄卡")) {
            request.addRequestParam("isBankType", "1");
        } else {
            request.addRequestParam("isBankType", "2");
        }
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
                                BindResult payResult = new BindResult(dataMap);
                                if (payResult.getResponseCode().equals("00")) {
                                    paySuccess();
                                    return;
                                }
                                if (TextUtils.isEmpty(payResult.getResponseTextMessage())) {
                                    Notify.show("付款失败");
                                } else {
                                    Notify.show(payResult.getResponseTextMessage());
                                }
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Notify.show("付款失败");
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

    private void paySecondTime(String validCode) {
        Request request = new Request();
        request.setUrl(API.API_PAY_SECOND_TIME);
        request.addRequestParam("payInfoType", String.valueOf(orderType));
        request.addRequestParam("orderNumber", orderNumbers);
        request.addRequestParam("storableCardNo", getShortCardNumber(selectedBankCard.getBanknumber()));
        request.addRequestParam("externalRefNumber", serialNumber);
        request.addRequestParam("amount", decimalFormat.format(totalMoney));
        request.addRequestParam("customerId", selectedBankCard.getOrg());
        request.addRequestParam("phone", selectedBankCard.getMobile());
        request.addRequestParam("validCode", validCode);
        request.addRequestParam("token", bindResult.getToken());
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                Notify.show("付款失败");
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            JSONObject dataMap = object.optJSONObject("dataMap");
                            if (dataMap != null) {
                                BindResult payResult = new BindResult(dataMap);
                                if (payResult.getResponseCode().equals("00")) {
                                    paySuccess();
                                    return;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Notify.show("付款失败");
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

    private void payBalance(String pwd) {
        Request request = new Request();
        request.setUrl(API.API_PAY_BALANCE);
        request.addRequestParam("memberAccount", User.getUser().getUseraccount());
        request.addRequestParam("orderType", String.valueOf(orderType));
        request.addRequestParam("orderNumbers", orderNumbers);
        request.addRequestParam("customerName", User.getUser().getRealname());
        request.addRequestParam("apAmount", decimalFormat.format(totalMoney));
        request.addRequestParam("pwd", pwd);
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
                                if (dataMap.optString("status").equalsIgnoreCase("ok")) {
                                    paySuccess();
                                    return;
                                } else {
                                    Notify.show(dataMap.optString("msg"));
                                    return;
                                }
                            }
                        } else {
                            Notify.show(object.optString("message"));
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Notify.show("付款失败");
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

    class BandCardAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return bankCards.size();
        }

        @Override
        public Object getItem(int position) {
            return bankCards.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = layoutInflater.inflate(
                        R.layout.list_pay_bank_card, null);
                viewHolder.selected = (ImageView) convertView
                        .findViewById(R.id.bank_card_bank_selection);
                viewHolder.bankImageView = (ImageView) convertView
                        .findViewById(R.id.bank_card_bank_image);
                viewHolder.cardNumberTextView = (TextView) convertView
                        .findViewById(R.id.bank_card_number);
                viewHolder.cardTypeTextView = (TextView) convertView
                        .findViewById(R.id.bank_card_type);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            ModelBankCard bankCard = bankCards.get(position);
            if (bankCard.getId().equals(selectedBankCard.getId())) {
                viewHolder.selected
                        .setImageResource(R.drawable.iconfont_check_checked);
            } else {
                viewHolder.selected
                        .setImageResource(R.drawable.iconfont_check_normal);
            }
            ImageLoader.getInstance().displayImage(
                    bankCard.getBank().getIcon(), viewHolder.bankImageView);
            if (bankCard.getCardType().contains("钱袋子")) {
                viewHolder.cardNumberTextView.setText("钱袋子余额");
                viewHolder.cardTypeTextView.setText("剩余:"
                        + Parameters.CONSTANT_RMB
                        + decimalFormat.format(MoneyAccount.getMoneyAccount()
                        .getBalance()));
            } else {
                viewHolder.cardNumberTextView
                        .setText(getSecretCardNuber(bankCard.getBanknumber()));
                viewHolder.cardTypeTextView.setText(bankCard.getBank()
                        .getName() + "  " + bankCard.getCardType());
            }
            return convertView;
        }

        class ViewHolder {
            ImageView selected, bankImageView;
            TextView cardNumberTextView, cardTypeTextView;
        }
    }

    /**
     * @author Tiger
     * @Json "dataMap":{"responseCode":"00", "customerId"
     * :"zhaojin1992","token":"1133738","merchantId": "104110045112012"
     * ,"storablePan":"6225883427"}
     */
    class BindResult {

        String responseCode = "", responseTextMessage = "", customerId = "",
                token = "", merchantId = "", storablePan = "";

        public BindResult() {
        }

        public BindResult(JSONObject object) {
            if (object != null) {
                if (object.has("responseCode")) {
                    if (!object.optString("responseCode").equalsIgnoreCase(
                            "null")) {
                        responseCode = object.optString("responseCode");
                    }
                }
                if (object.has("responseTextMessage")) {
                    if (!object.optString("responseTextMessage")
                            .equalsIgnoreCase("null")) {
                        responseTextMessage = object
                                .optString("responseTextMessage");
                    }
                }
                if (object.has("customerId")) {
                    if (!object.optString("customerId")
                            .equalsIgnoreCase("null")) {
                        customerId = object.optString("customerId");
                    }
                }
                if (object.has("token")) {
                    if (!object.optString("token").equalsIgnoreCase("null")) {
                        token = object.optString("token");
                    }
                }
                if (object.has("merchantId")) {
                    if (!object.optString("merchantId")
                            .equalsIgnoreCase("null")) {
                        merchantId = object.optString("merchantId");
                    }
                }
                if (object.has("storablePan")) {
                    if (!object.optString("storablePan").equalsIgnoreCase(
                            "null")) {
                        storablePan = object.optString("storablePan");
                    }
                }
            }
        }

        public String getResponseCode() {
            return responseCode;
        }

        public String getCustomerId() {
            return customerId;
        }

        public String getToken() {
            return token;
        }

        public String getMerchantId() {
            return merchantId;
        }

        public String getStorablePan() {
            return storablePan;
        }

        public String getResponseTextMessage() {
            return responseTextMessage;
        }
    }

    class SmsDialog extends DialogFragment {

        View dialogView;
        TextView okButton, getCodeButton;
        EditText smscodeEditText;
        ImageView closeButton;
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
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setCancelable(false);
            findViews();
            smsTimes = 60;
            handler.sendEmptyMessage(1);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            isFinish = true;
            super.onDismiss(dialog);
        }

        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = new Dialog(getActivity());
            dialog.getWindow().setBackgroundDrawableResource(R.color.divider);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(dialogView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            return dialog;
        }

        private void findViews() {
            dialogView = layoutInflater.inflate(R.layout.dialog_pay_smscode, null);
            closeButton = (ImageView) dialogView.findViewById(R.id.pay_sms_close);
            smscodeEditText = (EditText) dialogView.findViewById(R.id.pay_sms_code);
            getCodeButton = (TextView) dialogView.findViewById(R.id.pay_sms_get);
            okButton = (TextView) dialogView.findViewById(R.id.pay_sms_ok);
            getCodeButton.setEnabled(false);
            okButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (TextUtils.isEmpty(smscodeEditText.getText().toString().trim())) {
                        Notify.show("请输入验证码");
                    } else {
                        payFirstTime(smscodeEditText.getText().toString());
                        dismiss();
                    }
                }
            });
            closeButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            getCodeButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    getSmsCode();
                    dismiss();
                }
            });
        }
    }

    class CreditInfoDialog extends DialogFragment {

        View dialogView;
        TextView okButton, cancelButton;
        EditText timeEditText, codeEditText;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            init();
            findViews();
        }

        private void init() {
            setCancelable(false);
        }

        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = new Dialog(getActivity());
            dialog.getWindow().setBackgroundDrawableResource(R.color.divider);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(dialogView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            return dialog;
        }

        private void findViews() {
            dialogView = layoutInflater.inflate(R.layout.dialog_credit_edit, null);
            timeEditText = (EditText) dialogView.findViewById(R.id.pay_credit_card_time);
            codeEditText = (EditText) dialogView.findViewById(R.id.pay_credit_card_code);
            cancelButton = (TextView) dialogView.findViewById(R.id.pay_credit_card_cancel);
            okButton = (TextView) dialogView.findViewById(R.id.pay_credit_card_ok);
            okButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (TextUtils.isEmpty(timeEditText.getText().toString().trim())) {
                        Notify.show("请输入信用卡有效期");
                    } else if (TextUtils.isEmpty(codeEditText.getText().toString().trim())) {
                        Notify.show("请输入信用卡校验码");
                    } else {
                        selectedBankCard.setExpiredDate(timeEditText.getText().toString().trim());
                        selectedBankCard.setCvv2(codeEditText.getText().toString().trim());
                        inputPayPassword(1);
                        dismiss();
                    }
                }
            });
            cancelButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
    }

}
