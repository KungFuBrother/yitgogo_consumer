package yitgogo.consumer.suning.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.money.ui.PayFragment;
import yitgogo.consumer.order.ui.OrderConfirmPartPaymentFragment;
import yitgogo.consumer.store.model.Store;
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

    ModelProductDetail product = new ModelProductDetail();
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
        getSuningProductPrice();
        getProductStock();
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
                product = new ModelProductDetail(new JSONObject(bundle.getString("product")));
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
        ImageLoader.getInstance().displayImage(product.getImage(), imageView);
        nameTextView.setText(product.getName());
        priceTextView.setText(Parameters.CONSTANT_RMB + decimalFormat.format(productPrice.getPrice()));
        countTextView.setText(buyCount + "");
        countTotalPrice();
    }

    private void deleteCount() {
        if (buyCount > 1) {
            buyCount--;
            countTotalPrice();
            getProductStock();
        }
    }

    private void addCount() {
        buyCount++;
        countTotalPrice();
        getProductStock();
    }

    private void countTotalPrice() {
        countTextView.setText(String.valueOf(buyCount));
        if (productPrice.getPrice() > 0) {
            priceTextView.setText(Parameters.CONSTANT_RMB + decimalFormat.format(productPrice.getPrice()));
        } else {
            priceTextView.setText("价格查询失败");
        }
        goodsMoney = 0;
        double sendMoney = 0;
        goodsMoney = productPrice.getPrice() * buyCount;
        if (goodsMoney > 0 & goodsMoney < 69) {
            sendMoney = 5;
        }
        if (goodsMoney > 0) {
            totalPriceTextView.setText(Parameters.CONSTANT_RMB + decimalFormat.format(goodsMoney + sendMoney));
        } else {
            totalPriceTextView.setText("价格查询失败");
        }
    }

    private void buy() {
        if (TextUtils.isEmpty(consumerNameEditText.getText().toString())) {
            Notify.show("请输入收货人姓名");
        } else if (!isPhoneNumber(consumerPhoneEditText.getText().toString())) {
            Notify.show("请输入正确的收货人联系电话");
        } else if (TextUtils.isEmpty(areaTextView.getText().toString())) {
            Notify.show("请选择收货区域");
        } else if (TextUtils.isEmpty(detailAddressEditText.getText().toString())) {
            Notify.show("请输入详细收货地址");
        } else {
            if (state.equals("00")) {
                if (goodsMoney > 0) {
                    addOrder();
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
    private void addOrder() {
        ModelSuningAreas suningAreas = SuningManager.getSuningAreas();
        suningAreas.setConsumerName(consumerNameEditText.getText().toString());
        suningAreas.setConsumerPhone(consumerPhoneEditText.getText().toString());
        suningAreas.setConsumerAddress(detailAddressEditText.getText().toString());
        suningAreas.save();
        Request request = new Request();
        request.setUrl(API.API_SUNING_ORDER_ADD);
        request.addRequestParam("menberAccount", User.getUser().getUseraccount());
        request.addRequestParam("name", SuningManager.getSuningAreas().getConsumerName());
        request.addRequestParam("mobile", SuningManager.getSuningAreas().getConsumerPhone());
        request.addRequestParam("address", SuningManager.getSuningAreas().getConsumerAddress());
        request.addRequestParam("spId", Store.getStore().getStoreId());
        request.addRequestParam("amount", decimalFormat.format(goodsMoney));
        request.addRequestParam("provinceId", SuningManager.getSuningAreas().getProvince().getCode());
        request.addRequestParam("cityId", SuningManager.getSuningAreas().getCity().getCode());
        request.addRequestParam("countyId", SuningManager.getSuningAreas().getDistrict().getCode());
        request.addRequestParam("townId", SuningManager.getSuningAreas().getTown().getCode());
        JSONArray skuArray = new JSONArray();
        try {
            JSONObject skuObject = new JSONObject();
            skuObject.put("number", product.getSku());
            skuObject.put("num", buyCount);
            skuObject.put("price", decimalFormat.format(productPrice.getPrice()));
            skuObject.put("name", product.getName());
            skuObject.put("attr", product.getModel());
            skuArray.put(skuObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        request.addRequestParam("sku", skuArray.toString());
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                Notify.show("下单失败," + missionMessage.getMessage());
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
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
                } else {
                    Notify.show("下单失败");
                }
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });

    }

//    class Buy extends AsyncTask<Void, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//
//        }
//
//        @Override
//        protected String doInBackground(Void... voids) {
//            List<NameValuePair> nameValuePairs = new ArrayList<>();
//            nameValuePairs.add(new BasicNameValuePair("menberAccount", User.getUser().getUseraccount()));
//            nameValuePairs.add(new BasicNameValuePair("name", SuningManager.getSuningAreas().getConsumerName()));
//            nameValuePairs.add(new BasicNameValuePair("mobile", SuningManager.getSuningAreas().getConsumerPhone()));
//            nameValuePairs.add(new BasicNameValuePair("address", SuningManager.getSuningAreas().getConsumerAddress()));
//            nameValuePairs.add(new BasicNameValuePair("spId", Store.getStore().getStoreId()));
//            nameValuePairs.add(new BasicNameValuePair("amount", decimalFormat.format(goodsMoney)));
//            nameValuePairs.add(new BasicNameValuePair("provinceId", SuningManager.getSuningAreas().getProvince().getCode()));
//            nameValuePairs.add(new BasicNameValuePair("cityId", SuningManager.getSuningAreas().getCity().getCode()));
//            nameValuePairs.add(new BasicNameValuePair("countyId", SuningManager.getSuningAreas().getDistrict().getCode()));
//            nameValuePairs.add(new BasicNameValuePair("townId", SuningManager.getSuningAreas().getTown().getCode()));
//            JSONArray skuArray = new JSONArray();
//            try {
//                JSONObject skuObject = new JSONObject();
//                skuObject.put("number", product.getSku());
//                skuObject.put("num", buyCount);
//                skuObject.put("price", productPrice.getPrice());
//                skuObject.put("name", product.getName());
//                skuObject.put("attr", product.getModel());
//                skuArray.put(skuObject);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            nameValuePairs.add(new BasicNameValuePair("sku", skuArray.toString()));
//            return netUtil.postWithoutCookie(API.API_SUNING_ORDER_ADD, nameValuePairs, false, false);
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//
//            if (!TextUtils.isEmpty(s)) {
//                try {
//                    JSONObject object = new JSONObject(s);
//                    if (object.optString("state").equals("SUCCESS")) {
//                        Notify.show("下单成功");
//                        ModelSuningOrderResult orderResult = new ModelSuningOrderResult(object.optJSONObject("dataMap"));
//                        if (orderResult.getZongjine() > 0) {
//                            if (paymentFragment.getPaymentType() == OrderConfirmPartPaymentFragment.PAY_TYPE_CODE_ONLINE) {
//                                payMoney(orderResult.getOrderNumber(), orderResult.getZongjine() + orderResult.getFreight(), PayFragment.ORDER_TYPE_SN);
//                                getActivity().finish();
//                                return;
//                            }
//                        }
//                        showOrder(PayFragment.ORDER_TYPE_SN);
//                        getActivity().finish();
//                        return;
//                    }
//                    Notify.show(object.optString("message"));
//                    return;
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//            Notify.show("下单失败");
//        }
//    }

    private void getProductStock() {
        Request request = new Request();
        request.setUrl(API.API_SUNING_PRODUCT_STOCK);
        JSONObject data = new JSONObject();
        try {
            data.put("accessToken", SuningManager.getSignature().getToken());
            data.put("appKey", SuningManager.appKey);
            data.put("v", SuningManager.version);
            data.put("cityId", SuningManager.getSuningAreas().getCity().getCode());
            data.put("countyId", SuningManager.getSuningAreas().getDistrict().getCode());
            data.put("sku", product.getSku());
            data.put("num", buyCount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        request.addRequestParam("data", data.toString());
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
                    if (SuningManager.isSignatureOutOfDate(requestMessage.getResult())) {
                        SuningManager.getNewSignature(getActivity(), new RequestListener() {
                            @Override
                            protected void onStart() {

                            }

                            @Override
                            protected void onFail(MissionMessage missionMessage) {

                            }

                            @Override
                            protected void onSuccess(RequestMessage requestMessage) {
                                if (SuningManager.initSignature(requestMessage)) {
                                    getProductStock();
                                }
                            }

                            @Override
                            protected void onFinish() {

                            }
                        });
                        return;
                    }
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optBoolean("isSuccess")) {
                            state = object.optString("state");
                            if (state.equals("00")) {
                                additionTextView.setText("有货");
                            } else if (state.equals("01")) {
                                additionTextView.setText("暂不销售");
                            } else {
                                additionTextView.setText("无货");
                            }
                        } else {
                            additionTextView.setText(object.optString("returnMsg"));
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

//    class GetProductStock extends AsyncTask<Void, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//            showLoading();
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//
//            List<NameValuePair> nameValuePairs = new ArrayList<>();
//            nameValuePairs.add(new BasicNameValuePair("data", data.toString()));
//            return netUtil.postWithoutCookie(API.API_SUNING_PRODUCT_STOCK, nameValuePairs, false, false);
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            hideLoading();
//
//        }
//    }

    private void getSuningProductPrice() {
        Request request = new Request();
        request.setUrl(API.API_SUNING_PRODUCT_PRICE);
        JSONArray dataArray = new JSONArray();
        dataArray.put(product.getSku());
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
        request.addRequestParam("data", data.toString());
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
                productPrice = new ModelProductPrice();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    if (SuningManager.isSignatureOutOfDate(requestMessage.getResult())) {
                        SuningManager.getNewSignature(getActivity(), new RequestListener() {
                            @Override
                            protected void onStart() {

                            }

                            @Override
                            protected void onFail(MissionMessage missionMessage) {

                            }

                            @Override
                            protected void onSuccess(RequestMessage requestMessage) {
                                if (SuningManager.initSignature(requestMessage)) {
                                    getSuningProductPrice();
                                }
                            }

                            @Override
                            protected void onFinish() {

                            }
                        });
                        return;
                    }
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optBoolean("isSuccess")) {
                            JSONArray array = object.optJSONArray("result");
                            if (array != null) {
                                for (int j = 0; j < array.length(); j++) {
                                    productPrice = new ModelProductPrice(array.optJSONObject(j));
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                countTotalPrice();
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

//    class GetSuningProductPrice extends AsyncTask<Void, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//            showLoading();
//            productPrice = new ModelProductPrice();
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//
//            List<NameValuePair> nameValuePairs = new ArrayList<>();
//            nameValuePairs.add(new BasicNameValuePair("data", data.toString()));
//            return netUtil.postWithoutCookie(API.API_SUNING_PRODUCT_PRICE, nameValuePairs, false, false);
//        }
//
//        /**
//         * {"result":[{"skuId":"108246148","price":15000.00}],"isSuccess":true,"returnMsg":"查询成功。"}
//         */
//        @Override
//        protected void onPostExecute(String result) {
//
//
//        }
//    }
}
