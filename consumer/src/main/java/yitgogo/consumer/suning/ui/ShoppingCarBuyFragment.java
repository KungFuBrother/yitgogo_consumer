package yitgogo.consumer.suning.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.List;

import yitgogo.consumer.BaseNotifyFragment;
import yitgogo.consumer.money.ui.PayFragment;
import yitgogo.consumer.order.ui.OrderConfirmPartPaymentFragment;
import yitgogo.consumer.store.model.Store;
import yitgogo.consumer.suning.model.GetNewSignature;
import yitgogo.consumer.suning.model.ModelProductPrice;
import yitgogo.consumer.suning.model.ModelSuningAreas;
import yitgogo.consumer.suning.model.ModelSuningCar;
import yitgogo.consumer.suning.model.ModelSuningOrderResult;
import yitgogo.consumer.suning.model.SuningCarController;
import yitgogo.consumer.suning.model.SuningManager;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.user.model.User;
import yitgogo.consumer.view.InnerListView;
import yitgogo.consumer.view.Notify;


public class ShoppingCarBuyFragment extends BaseNotifyFragment {

    InnerListView productListView;
    TextView totalMoneyTextView, confirmButton;
    List<ModelSuningCar> suningCars = new ArrayList<>();
    HashMap<String, ModelProductPrice> priceHashMap = new HashMap<>();

    EditText consumerNameEditText, consumerPhoneEditText, detailAddressEditText;
    TextView areaTextView;

    ProductAdapter productAdapter;
    double goodsMoney = 0;

    OrderConfirmPartPaymentFragment paymentFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_suning_order_add);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(ShoppingCarBuyFragment.class.getName());
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
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(ShoppingCarBuyFragment.class.getName());
    }

    private void init() {
        suningCars = SuningCarController.getSelectedCars();
        productAdapter = new ProductAdapter();
        paymentFragment = new OrderConfirmPartPaymentFragment(true, true, false);
    }

    @Override
    protected void findViews() {
        productListView = (InnerListView) contentView.findViewById(R.id.order_add_products);
        totalMoneyTextView = (TextView) contentView.findViewById(R.id.order_add_total_money);
        confirmButton = (TextView) contentView.findViewById(R.id.order_add_confirm);

        consumerNameEditText = (EditText) contentView.findViewById(R.id.address_consumer_name);
        consumerPhoneEditText = (EditText) contentView.findViewById(R.id.address_consumer_phone);
        detailAddressEditText = (EditText) contentView.findViewById(R.id.address_area_detail);
        areaTextView = (TextView) contentView.findViewById(R.id.address_area);

        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        productListView.setAdapter(productAdapter);
        getFragmentManager().beginTransaction().replace(R.id.order_add_payment, paymentFragment).commit();
    }

    @Override
    protected void registerViews() {
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addOrder();
            }
        });
        areaTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jump(SuningAreaFragment.class.getName(), "设置云商城收货区域");
            }
        });
    }

    private void addOrder() {
        if (TextUtils.isEmpty(consumerNameEditText.getText().toString())) {
            Notify.show("请输入收货人姓名");
        } else if (TextUtils.isEmpty(consumerPhoneEditText.getText().toString())) {
            Notify.show("请输入收货人联系电话");
        } else if (TextUtils.isEmpty(areaTextView.getText().toString())) {
            Notify.show("请选择收货区域");
        } else if (TextUtils.isEmpty(detailAddressEditText.getText().toString())) {
            Notify.show("请输入详细收货地址");
        } else {
            if (goodsMoney > 0) {
                new Buy().execute();
            } else {
                Notify.show("有商品信息有误，暂不能购买");
            }
        }
    }

    private void countTotalPrice() {
        goodsMoney = 0;
        double sendMoney = 0;
        for (int i = 0; i < suningCars.size(); i++) {
            if (suningCars.get(i).isSelected()) {
                long count = suningCars.get(i).getProductCount();
                if (priceHashMap.containsKey(suningCars.get(i).getProductDetail().getSku())) {
                    double price = priceHashMap.get(suningCars.get(i).getProductDetail().getSku()).getPrice();
                    if (price > 0) {
                        goodsMoney += count * price;
                    }
                }
            }
        }
        if (goodsMoney > 0 & goodsMoney < 69) {
            sendMoney = 5;
        }
        totalMoneyTextView.setText(Parameters.CONSTANT_RMB + decimalFormat.format(goodsMoney + sendMoney));
    }

    class ProductAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return suningCars.size();
        }

        @Override
        public Object getItem(int position) {
            return suningCars.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_car_buy,
                        null);
                holder = new ViewHolder();
                holder.goodNameText = (TextView) convertView
                        .findViewById(R.id.list_car_title);
                holder.goodsImageView = (ImageView) convertView
                        .findViewById(R.id.list_car_image);
                holder.goodsPriceText = (TextView) convertView
                        .findViewById(R.id.list_car_price);
                holder.guigeText = (TextView) convertView
                        .findViewById(R.id.list_car_guige);
                holder.stateText = (TextView) convertView
                        .findViewById(R.id.list_car_state);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ImageLoader.getInstance().displayImage(suningCars.get(position).getProductDetail().getImage(),
                    holder.goodsImageView);
            holder.goodNameText.setText(suningCars.get(position).getProductDetail().getName());
            if (priceHashMap.containsKey(suningCars.get(position).getProductDetail().getSku())) {
                holder.goodsPriceText.setText(Parameters.CONSTANT_RMB
                        + decimalFormat.format(priceHashMap.get(suningCars.get(position).getProductDetail().getSku()).getPrice()));
            }
            holder.stateText.setText("×" + suningCars.get(position).getProductCount());
            return convertView;
        }

        class ViewHolder {
            ImageView goodsImageView;
            TextView goodNameText, goodsPriceText, guigeText, stateText;
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
            for (int i = 0; i < suningCars.size(); i++) {
                if (priceHashMap.containsKey(suningCars.get(i).getProductDetail().getSku())) {
                    try {
                        JSONObject skuObject = new JSONObject();
                        skuObject.put("number", suningCars.get(i).getProductDetail().getSku());
                        skuObject.put("num", suningCars.get(i).getProductCount());
                        skuObject.put("price", priceHashMap.get(suningCars.get(i).getProductDetail().getSku()).getPrice());
                        skuObject.put("name", suningCars.get(i).getProductDetail().getName());
                        skuObject.put("attr", suningCars.get(i).getProductDetail().getModel());
                        skuArray.put(skuObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
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
                        SuningCarController.deleteSelectedCars();
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

    class GetSuningProductPrice extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoading();
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONArray dataArray = new JSONArray();
            for (int i = 0; i < suningCars.size(); i++) {
                dataArray.put(suningCars.get(i).getProductDetail().getSku());
            }
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
                                ModelProductPrice productPrice = new ModelProductPrice(array.optJSONObject(j));
                                priceHashMap.put(productPrice.getSkuId(), productPrice);
                            }
                            productAdapter.notifyDataSetChanged();
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
