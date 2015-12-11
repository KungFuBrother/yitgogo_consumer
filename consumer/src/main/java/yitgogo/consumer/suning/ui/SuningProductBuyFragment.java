package yitgogo.consumer.suning.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartown.yitian.gogo.R;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import yitgogo.consumer.BaseNotifyFragment;
import yitgogo.consumer.money.ui.PayFragment;
import yitgogo.consumer.order.ui.OrderConfirmPartPaymentFragment;
import yitgogo.consumer.store.model.Store;
import yitgogo.consumer.suning.model.GetNewSignature;
import yitgogo.consumer.suning.model.ModelProductDetail;
import yitgogo.consumer.suning.model.ModelProductPrice;
import yitgogo.consumer.suning.model.ModelSuningAreas;
import yitgogo.consumer.suning.model.ModelSuningOrderResult;
import yitgogo.consumer.suning.model.SuningManager;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.user.model.User;
import yitgogo.consumer.view.Notify;

public class SuningProductBuyFragment extends BaseNotifyFragment {

    ImageView imageView;
    TextView nameTextView, priceTextView, countTextView, countAddButton,
            countDeleteButton, additionTextView;

    FrameLayout paymentLayout;
    TextView totalPriceTextView, confirmButton;

    EditText consumerNameEditText, consumerPhoneEditText, detailAddressEditText;
    TextView areaTextView;

    ModelProductDetail productDetail = new ModelProductDetail();
    ModelProductPrice productPrice = new ModelProductPrice();

    OrderConfirmPartPaymentFragment paymentFragment;

    int buyCount = 1;
    double goodsMoney = 0;

    String state = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_confirm_order_suning);
        try {
            init();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(SuningProductBuyFragment.class.getName());
        showSuningAreas();
    }

    private void showSuningAreas() {
        StringBuilder builder = new StringBuilder();
        if (!TextUtils.isEmpty(SuningManager.getSuningAreas().getProvince().getName())) {
            builder.append(SuningManager.getSuningAreas().getProvince().getName());
            if (!TextUtils.isEmpty(SuningManager.getSuningAreas().getCity().getName())) {
                builder.append(">");
                builder.append(SuningManager.getSuningAreas().getCity().getName());
                if (!TextUtils.isEmpty(SuningManager.getSuningAreas().getDistrict().getName())) {
                    builder.append(">");
                    builder.append(SuningManager.getSuningAreas().getDistrict().getName());
                    if (!TextUtils.isEmpty(SuningManager.getSuningAreas().getTown().getName())) {
                        builder.append(">");
                        builder.append(SuningManager.getSuningAreas().getTown().getName());
                    }
                }
            }
        }
        areaTextView.setText(builder.toString());
        consumerNameEditText.setText(SuningManager.getSuningAreas().getConsumerName());
        consumerPhoneEditText.setText(SuningManager.getSuningAreas().getConsumerPhone());
        detailAddressEditText.setText(SuningManager.getSuningAreas().getConsumerAddress());
        new GetSuningProductPrice().execute();
        new GetProductStock().execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(SuningProductBuyFragment.class.getName());
    }

    private void init() throws JSONException {
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("product")) {
                productDetail = new ModelProductDetail(new JSONObject(bundle.getString("product")));
            }
        }
        paymentFragment = new OrderConfirmPartPaymentFragment(true, true, false);
    }

    protected void findViews() {
        imageView = (ImageView) contentView
                .findViewById(R.id.order_confirm_sale_image);
        nameTextView = (TextView) contentView
                .findViewById(R.id.order_confirm_sale_name);
        priceTextView = (TextView) contentView
                .findViewById(R.id.order_confirm_sale_price);
        countTextView = (TextView) contentView
                .findViewById(R.id.order_confirm_sale_count);
        countDeleteButton = (TextView) contentView
                .findViewById(R.id.order_confirm_sale_count_delete);
        countAddButton = (TextView) contentView
                .findViewById(R.id.order_confirm_sale_count_add);
        additionTextView = (TextView) contentView
                .findViewById(R.id.order_confirm_sale_addition);
        paymentLayout = (FrameLayout) contentView
                .findViewById(R.id.order_confirm_sale_payment);
        totalPriceTextView = (TextView) contentView
                .findViewById(R.id.order_confirm_sale_total_money);
        confirmButton = (TextView) contentView
                .findViewById(R.id.order_confirm_sale_confirm);

        consumerNameEditText = (EditText) contentView.findViewById(R.id.address_consumer_name);
        consumerPhoneEditText = (EditText) contentView.findViewById(R.id.address_consumer_phone);
        detailAddressEditText = (EditText) contentView.findViewById(R.id.address_area_detail);
        areaTextView = (TextView) contentView.findViewById(R.id.address_area);

        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        showProductInfo();
        getFragmentManager().beginTransaction()
                .replace(R.id.order_confirm_sale_payment, paymentFragment)
                .commit();
    }

    @Override
    protected void registerViews() {
        confirmButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                buy();
            }
        });
        countDeleteButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                deleteCount();
            }
        });
        countAddButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                addCount();
            }
        });
        areaTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                jump(SuningAreaFragment.class.getName(), "设置云商城收货区域");
            }
        });
    }

    private void showProductInfo() {
        ImageLoader.getInstance().displayImage(productDetail.getImage(), imageView);
        nameTextView.setText(productDetail.getName());
        priceTextView.setText(Parameters.CONSTANT_RMB + decimalFormat.format(productPrice.getPrice()));
        countTextView.setText(buyCount + "");
        countTotalPrice();
    }

    private void deleteCount() {
        if (buyCount > 1) {
            buyCount--;
            countTotalPrice();
            new GetProductStock().execute();
        }
    }

    private void addCount() {
        buyCount++;
        countTotalPrice();
        new GetProductStock().execute();
    }

    private void countTotalPrice() {
        goodsMoney = 0;
        double sendMoney = 0;
        goodsMoney = productPrice.getPrice() * buyCount;
        if (goodsMoney > 0 & goodsMoney < 69) {
            sendMoney = 5;
        }
        countTextView.setText(buyCount + "");
        totalPriceTextView.setText(Parameters.CONSTANT_RMB
                + decimalFormat.format(goodsMoney + sendMoney));
    }

    private void buy() {
        if (TextUtils.isEmpty(consumerNameEditText.getText().toString())) {
            Notify.show("请输入收货人姓名");
        } else if (TextUtils.isEmpty(consumerPhoneEditText.getText().toString())) {
            Notify.show("请输入收货人联系电话");
        } else if (TextUtils.isEmpty(areaTextView.getText().toString())) {
            Notify.show("请选择收货区域");
        } else if (TextUtils.isEmpty(detailAddressEditText.getText().toString())) {
            Notify.show("请输入详细收货地址");
        } else {
            if (state.equals("00")) {
                if (goodsMoney > 0) {
                    new Buy().execute();
                } else {
                    Notify.show("商品信息有误，暂不能购买");
                }
            } else {
                Notify.show("此商品暂不能购买");
            }
        }
    }


    /**
     * @Url: http://192.168.8.36:8089/api/order/cloudMallOrder/CloudMallAction/CreatSuNingOrder
     * @Parameters: [menberAccount=13032889558, name=雷小武, mobile=13032889558, address=解放路二段, spId=674, amount=30.0, provinceId=230, cityId=028, countyId=03, townId=02, sku=[{"num":1,"number":"120855028","name":"NEW17","price":30}]]
     * @Result: {"message":"ok","state":"SUCCESS","cacheKey":null,"dataList":[],"totalCount":1,"dataMap":{"fuwuZuoji":"028-66133688","orderType":"新订单","orderNumber":"SN464351824310","zongjine":"30.0","freight":"5","productInfo":[{"num":1"Amount":30.0,"price":30.0,"spname":"NEW17"}],"fuwushang":"成都成华区罗飞加盟商","shijian":"2015-10-28 09:46:43","fuwuPhone":"13880353588"},"object":null}
     */
    class Buy extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoading();
            ModelSuningAreas suningAreas = SuningManager.getSuningAreas();
            suningAreas.setConsumerName(consumerNameEditText.getText().toString());
            suningAreas.setConsumerPhone(consumerPhoneEditText.getText().toString());
            suningAreas.setConsumerAddress(detailAddressEditText.getText().toString());
            suningAreas.save();
        }

        @Override
        protected String doInBackground(Void... voids) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("menberAccount", User.getUser().getUseraccount()));
            nameValuePairs.add(new BasicNameValuePair("name", SuningManager.getSuningAreas().getConsumerName()));
            nameValuePairs.add(new BasicNameValuePair("mobile", SuningManager.getSuningAreas().getConsumerPhone()));
            nameValuePairs.add(new BasicNameValuePair("address", SuningManager.getSuningAreas().getConsumerAddress()));
            nameValuePairs.add(new BasicNameValuePair("spId", Store.getStore().getStoreId()));
            nameValuePairs.add(new BasicNameValuePair("amount", decimalFormat.format(goodsMoney)));
            nameValuePairs.add(new BasicNameValuePair("provinceId", SuningManager.getSuningAreas().getProvince().getCode()));
            nameValuePairs.add(new BasicNameValuePair("cityId", SuningManager.getSuningAreas().getCity().getCode()));
            nameValuePairs.add(new BasicNameValuePair("countyId", SuningManager.getSuningAreas().getDistrict().getCode()));
            nameValuePairs.add(new BasicNameValuePair("townId", SuningManager.getSuningAreas().getTown().getCode()));
            JSONArray skuArray = new JSONArray();
            try {
                JSONObject skuObject = new JSONObject();
                skuObject.put("number", productDetail.getSku());
                skuObject.put("num", buyCount);
                skuObject.put("price", productPrice.getPrice());
                skuObject.put("name", productDetail.getName());
                skuObject.put("attr", productDetail.getModel());
                skuArray.put(skuObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            nameValuePairs.add(new BasicNameValuePair("sku", skuArray.toString()));
            return netUtil.postWithoutCookie(API.API_SUNING_ORDER_ADD, nameValuePairs, false, false);
        }

        @Override
        protected void onPostExecute(String s) {
            hideLoading();
            if (!TextUtils.isEmpty(s)) {
                try {
                    JSONObject object = new JSONObject(s);
                    if (object.optString("state").equals("SUCCESS")) {
                        Notify.show("下单成功");
                        ModelSuningOrderResult orderResult = new ModelSuningOrderResult(object.optJSONObject("dataMap"));
                        if (orderResult.getZongjine() > 0) {
                            if (paymentFragment.getPaymentType() == OrderConfirmPartPaymentFragment.PAY_TYPE_CODE_ONLINE) {
                                payMoney(orderResult.getOrderNumber(), orderResult.getZongjine() + orderResult.getFreight(), PayFragment.ORDER_TYPE_SN);
                                getActivity().finish();
                                return;
                            }
                        }
                        showOrder(PayFragment.ORDER_TYPE_SN);
                        getActivity().finish();
                        return;
                    }
                    Notify.show(object.optString("message"));
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Notify.show("下单失败");
        }
    }

    class GetProductStock extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoading();
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONObject data = new JSONObject();
            try {
                data.put("accessToken", SuningManager.getSignature().getToken());
                data.put("appKey", SuningManager.appKey);
                data.put("v", SuningManager.version);
                data.put("cityId", SuningManager.getSuningAreas().getCity().getCode());
                data.put("countyId", SuningManager.getSuningAreas().getDistrict().getCode());
                data.put("sku", productDetail.getSku());
                data.put("num", buyCount);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("data", data.toString()));
            return netUtil.postWithoutCookie(API.API_SUNING_PRODUCT_STOCK, nameValuePairs, false, false);
        }

        @Override
        protected void onPostExecute(String result) {
            hideLoading();
            if (SuningManager.isSignatureOutOfDate(result)) {
                GetNewSignature getNewSignature = new GetNewSignature() {

                    @Override
                    protected void onPreExecute() {
                        showLoading();
                    }

                    @Override
                    protected void onPostExecute(Boolean isSuccess) {
                        hideLoading();
                        if (isSuccess) {
                            new GetProductStock().execute();
                        }
                    }
                };
                getNewSignature.execute();
                return;
            }
            /**
             * {"sku":null,"state":null,"isSuccess":false,"returnMsg":"无货"}
             */
            if (!TextUtils.isEmpty(result)) {
                try {
                    JSONObject object = new JSONObject(result);
                    if (object.optBoolean("isSuccess")) {
                        state = object.optString("state");
                        if (state.equals("00")) {
                            additionTextView.setText("有货");
                        } else if (state.equals("01")) {
                            additionTextView.setText("暂不销售");
                        } else {
                            additionTextView.setText("无货");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class GetSuningProductPrice extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoading();
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONArray dataArray = new JSONArray();
            dataArray.put(productDetail.getSku());
            JSONObject data = new JSONObject();
            try {
                data.put("accessToken", SuningManager.getSignature().getToken());
                data.put("appKey", SuningManager.appKey);
                data.put("v", SuningManager.version);
                data.put("cityId", SuningManager.getSuningAreas().getCity().getCode());
                data.put("sku", dataArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("data", data.toString()));
            return netUtil.postWithoutCookie(API.API_SUNING_PRODUCT_PRICE, nameValuePairs, false, false);
        }

        /**
         * {"result":[{"skuId":"108246148","price":15000.00}],"isSuccess":true,"returnMsg":"查询成功。"}
         */
        @Override
        protected void onPostExecute(String result) {
            hideLoading();
            if (SuningManager.isSignatureOutOfDate(result)) {
                GetNewSignature getNewSignature = new GetNewSignature() {
                    @Override
                    protected void onPreExecute() {
                        showLoading();
                    }

                    @Override
                    protected void onPostExecute(Boolean isSuccess) {
                        hideLoading();
                        if (isSuccess) {
                            new GetSuningProductPrice().execute();
                        }
                    }
                };
                getNewSignature.execute();
                return;
            }
            if (!TextUtils.isEmpty(result)) {
                try {
                    JSONObject object = new JSONObject(result);
                    if (object.optBoolean("isSuccess")) {
                        JSONArray array = object.optJSONArray("result");
                        if (array != null) {
                            for (int j = 0; j < array.length(); j++) {
                                productPrice = new ModelProductPrice(array.optJSONObject(j));
                                priceTextView.setText(Parameters.CONSTANT_RMB + decimalFormat.format(productPrice.getPrice()));
                            }
                            countTotalPrice();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
