package yitgogo.consumer.order.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
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

import java.util.HashMap;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.money.ui.PayFragment;
import yitgogo.consumer.product.model.ModelFreight;
import yitgogo.consumer.store.model.Store;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.user.model.User;
import yitgogo.consumer.view.Notify;

public class PlatformProductBuyFragment extends BaseNotifyFragment {

    ImageView imageView;
    TextView providerTextView, nameTextView, attrTextView, countTextView, priceTextView, freightTextView;

    FrameLayout addressLayout, paymentLayout;
    TextView totalMoneyTextView, confirmButton;

    String supplierId = "";
    String supplierName = "";
    String productId = "";
    String productNumber = "";
    String productAttr = "";
    String name = "";
    String image = "";
    int isIntegralMall = 0;
    double price = 0;
    int buyCount = 0;

    HashMap<String, ModelFreight> freightMap;

    OrderConfirmPartAddressFragment addressFragment;
    OrderConfirmPartPaymentFragment paymentFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_confirm_order_sale);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(PlatformProductBuyFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(PlatformProductBuyFragment.class.getName());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(R.id.platform_product_buy_address, addressFragment)
                .replace(R.id.platform_product_buy_payment, paymentFragment)
                .commit();
    }

    private void init() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("supplierId")) {
                supplierId = bundle.getString("supplierId");
            }
            if (bundle.containsKey("supplierName")) {
                supplierName = bundle.getString("supplierName");
            }
            if (bundle.containsKey("productId")) {
                productId = bundle.getString("productId");
            }
            if (bundle.containsKey("productNumber")) {
                productNumber = bundle.getString("productNumber");
            }
            if (bundle.containsKey("name")) {
                name = bundle.getString("name");
            }
            if (bundle.containsKey("productAttr")) {
                productAttr = bundle.getString("productAttr");
            }
            if (bundle.containsKey("image")) {
                image = bundle.getString("image");
            }
            if (bundle.containsKey("isIntegralMall")) {
                isIntegralMall = bundle.getInt("isIntegralMall");
            }
            if (bundle.containsKey("price")) {
                price = bundle.getDouble("price");
            }
            if (bundle.containsKey("buyCount")) {
                buyCount = bundle.getInt("buyCount");
            }
        }
        freightMap = new HashMap<>();
        addressFragment = new OrderConfirmPartAddressFragment();
        paymentFragment = new OrderConfirmPartPaymentFragment(true, false);
        addressFragment.setOnSetAddressListener(new OrderConfirmPartAddressFragment.OnSetAddressListener() {
            @Override
            public void onSetAddress() {
                getFreight();
            }
        });
    }

    protected void findViews() {
        imageView = (ImageView) contentView.findViewById(R.id.platform_product_buy_image);
        providerTextView = (TextView) contentView.findViewById(R.id.platform_product_buy_provider);
        nameTextView = (TextView) contentView.findViewById(R.id.platform_product_buy_name);
        attrTextView = (TextView) contentView.findViewById(R.id.platform_product_buy_attr);
        countTextView = (TextView) contentView.findViewById(R.id.platform_product_buy_count);
        priceTextView = (TextView) contentView.findViewById(R.id.platform_product_buy_price);
        freightTextView = (TextView) contentView.findViewById(R.id.platform_product_buy_freight);
        addressLayout = (FrameLayout) contentView.findViewById(R.id.platform_product_buy_address);
        paymentLayout = (FrameLayout) contentView.findViewById(R.id.platform_product_buy_payment);
        totalMoneyTextView = (TextView) contentView.findViewById(R.id.platform_product_buy_money);
        confirmButton = (TextView) contentView.findViewById(R.id.platform_product_buy_ok);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        ImageLoader.getInstance().displayImage(getSmallImageUrl(image), imageView);
        providerTextView.setText(supplierName);
        nameTextView.setText(name);
        attrTextView.setText(productAttr);
        priceTextView.setText("单价:" + Parameters.CONSTANT_RMB + decimalFormat.format(price));
        countTextView.setText("数量:" + String.valueOf(buyCount));
    }

    @Override
    protected void registerViews() {
        confirmButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                addOrder();
            }
        });
    }

    private void addOrder() {
        if (buyCount * price > 0) {
            if (freightMap.containsKey(supplierId)) {
                if (addressFragment.getAddress() == null) {
                    Notify.show("收货人信息有误");
                } else {
                    buy();
                }
            } else {
                Notify.show("查询运费失败，不能购买");
            }
        } else {
            Notify.show("商品信息有误，不能购买");
        }
    }

    private void buy() {
        Request request = new Request();
        request.setUrl(API.API_ORDER_ADD_CENTER);
        request.addRequestParam("userNumber", User.getUser().getUseraccount());
        request.addRequestParam("customerName", addressFragment.getAddress().getPersonName());
        request.addRequestParam("phone", addressFragment.getAddress().getPhone());
        request.addRequestParam("shippingaddress", addressFragment.getAddress().getAreaAddress() + addressFragment.getAddress().getDetailedAddress());
        request.addRequestParam("totalMoney", decimalFormat.format(buyCount * price));
        request.addRequestParam("sex", User.getUser().getSex());
        request.addRequestParam("age", User.getUser().getAge());
        request.addRequestParam("address", Store.getStore().getStoreArea());
        request.addRequestParam("jmdId", Store.getStore().getStoreId());
        request.addRequestParam("orderType", "0");
        try {
            JSONArray dataArray = new JSONArray();
            JSONObject object = new JSONObject();
            object.put("productIds", productId);
            object.put("shopNum", buyCount);
            object.put("price", price);
            object.put("isIntegralMall", isIntegralMall);
            dataArray.put(object);
            request.addRequestParam("data", dataArray.toString());

            JSONArray freightArray = new JSONArray();
            JSONObject freightObject = new JSONObject();
            freightObject.put("supplyId", supplierId);
            freightObject.put("freight", freightMap.get(supplierId).getFregith());
            freightArray.put(freightObject);
            request.addRequestParam("freights", freightArray.toString());

        } catch (JSONException e) {
            e.printStackTrace();
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
                            Notify.show("下单成功");
                            if (paymentFragment.getPaymentType() == OrderConfirmPartPaymentFragment.PAY_TYPE_CODE_ONLINE) {
                                payMoney(object.optJSONArray("object"));
                                getActivity().finish();
                                return;
                            }
                            showOrder(PayFragment.ORDER_TYPE_YY);
                            getActivity().finish();
                            return;
                        } else {
                            Notify.show(object.optString("message"));
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Notify.show("下单失败");
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

    private void getFreight() {
        freightMap.clear();
        Request request = new Request();
        request.setUrl(API.API_PRODUCT_FREIGHT);
        request.addRequestParam("productNumber", productNumber + "-" + buyCount);
        request.addRequestParam("areaid", addressFragment.getAddress().getAreaId());
        request.addRequestParam("spid", Store.getStore().getStoreId());
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
                            JSONArray jsonArray = object.optJSONArray("dataList");
                            if (jsonArray != null) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    ModelFreight modelFreight = new ModelFreight(jsonArray.optJSONObject(i));
                                    if (!TextUtils.isEmpty(modelFreight.getAgencyId())) {
                                        freightMap.put(modelFreight.getAgencyId(), modelFreight);
                                    }
                                }
                                if (freightMap.containsKey(supplierId)) {
                                    freightTextView.setText(Parameters.CONSTANT_RMB + decimalFormat.format(freightMap.get(supplierId).getFregith()));
                                    totalMoneyTextView.setText(Parameters.CONSTANT_RMB + decimalFormat.format((buyCount * price) + freightMap.get(supplierId).getFregith()));
                                }
                            }
                            return;
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
