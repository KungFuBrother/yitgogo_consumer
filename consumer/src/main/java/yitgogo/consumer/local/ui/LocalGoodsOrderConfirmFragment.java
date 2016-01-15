package yitgogo.consumer.local.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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

import java.util.ArrayList;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.local.model.ModelLocalGoods;
import yitgogo.consumer.money.ui.PayFragment;
import yitgogo.consumer.order.ui.OrderConfirmPartAddressFragment;
import yitgogo.consumer.order.ui.OrderConfirmPartDeliverFragment;
import yitgogo.consumer.order.ui.OrderConfirmPartPaymentFragment;
import yitgogo.consumer.store.model.Store;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.user.model.User;
import yitgogo.consumer.view.Notify;

/**
 * @author Tiger
 * @description 本地商品订单确认
 */
public class LocalGoodsOrderConfirmFragment extends BaseNotifyFragment {

    int goodsCount = 1;
    double totalPrice = 0;
    ModelLocalGoods goods = new ModelLocalGoods();

    ImageView imageView;
    TextView nameTextView, priceTextView, countTextView, countAddButton,
            countDeleteButton;

    TextView totalMoneyTextView;
    Button confirmButton;

    OrderConfirmPartAddressFragment addressFragment;
    OrderConfirmPartDeliverFragment deliverFragment;
    OrderConfirmPartPaymentFragment paymentFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_confirm_order_local_goods);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(LocalGoodsOrderConfirmFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(LocalGoodsOrderConfirmFragment.class.getName());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getGoodsDetail();
    }

    private void init() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("goods")) {
                try {
                    goods = new ModelLocalGoods(new JSONObject(
                            bundle.getString("goodsId")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        addressFragment = new OrderConfirmPartAddressFragment();
        deliverFragment = new OrderConfirmPartDeliverFragment();
        paymentFragment = new OrderConfirmPartPaymentFragment(false);
    }

    @Override
    protected void findViews() {
        imageView = (ImageView) contentView
                .findViewById(R.id.local_goods_order_goods_image);
        nameTextView = (TextView) contentView
                .findViewById(R.id.local_goods_order_goods_name);
        priceTextView = (TextView) contentView
                .findViewById(R.id.local_goods_order_goods_price);
        countTextView = (TextView) contentView
                .findViewById(R.id.local_goods_order_goods_count);
        countDeleteButton = (TextView) contentView
                .findViewById(R.id.local_goods_order_goods_count_delete);
        countAddButton = (TextView) contentView
                .findViewById(R.id.local_goods_order_goods_count_add);
        totalMoneyTextView = (TextView) contentView
                .findViewById(R.id.local_goods_order_total_money);
        confirmButton = (Button) contentView
                .findViewById(R.id.local_goods_order_confirm);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        getFragmentManager().beginTransaction()
                .replace(R.id.local_goods_order_part_address, addressFragment)
                .replace(R.id.local_goods_order_part_deliver, deliverFragment)
                .replace(R.id.local_goods_order_part_payment, paymentFragment)
                .commit();
        countTextView.setText(goodsCount + "");
        countTotalPrice();
    }

    @Override
    protected void registerViews() {
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
        confirmButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                confirmOrder();
            }
        });
    }

    private void deleteCount() {
        if (goodsCount > 1) {
            goodsCount--;
        }
        if (goodsCount == 1) {
            countDeleteButton.setClickable(false);
        }
        countAddButton.setClickable(true);
        countTextView.setText(goodsCount + "");
        countTotalPrice();
    }

    private void addCount() {
        if (goodsCount < 100) {
            goodsCount++;
        }
        if (goodsCount == 100) {
            countAddButton.setClickable(false);
        }
        countDeleteButton.setClickable(true);
        countTextView.setText(goodsCount + "");
        countTotalPrice();
    }

    private void countTotalPrice() {
        deliverFragment.setBuyCount(goodsCount);
        totalPrice = goodsCount * goods.getRetailPrice();
        totalMoneyTextView.setText(Parameters.CONSTANT_RMB
                + decimalFormat.format(totalPrice));
    }

    private void confirmOrder() {
        if (totalPrice <= 0) {
            Notify.show("商品信息有误");
        } else if (addressFragment.getAddress() == null) {
            Notify.show("收货人地址有误");
        } else {
            addLocalGoodsOrder();
        }
    }

    private void showGoodsInfo() {
        ImageLoader.getInstance().displayImage(goods.getBigImgUrl(), imageView);
        nameTextView.setText(goods.getRetailProdManagerName());
        priceTextView.setText(Parameters.CONSTANT_RMB
                + decimalFormat.format(goods.getRetailPrice()));
        // deliverFragment.initDeliverType(goods.isDeliverYN(),
        // goods.getDeliverNum());
        // paymentFragment.setCanPaySend(goods.isPayOnDelivery());
        countTotalPrice();
    }

    /**
     * 添加本地产品订单
     *
     * @author Tiger
     * @Result {"message":"ok","state"
     * :"SUCCESS","cacheKey":null,"dataList":[{"servicetelephone"
     * :"028-32562356"
     * ,"orderDate":"2015-08-04","servicename":null,"productInfo":
     * "[{\"spname\":\"测试运营中心一-测试小吃产品一\",\"price\":\"10.0\",\"Amount\":\"10.0\",\"num\":\"1\"}]"
     * ,"ordernumber":"YT3765246787","orderPrice":"10.0","servicePhone":
     * "15821346521"}],"totalCount":1,"dataMap":{},"object":null}
     */
    private void addLocalGoodsOrder() {
        Request request = new Request();
        request.setUrl(API.API_LOCAL_BUSINESS_GOODS_ORDER_ADD);
        request.addRequestParam("customerName", addressFragment.getAddress().getPersonName());
        request.addRequestParam("customerPhone", addressFragment.getAddress().getPhone());
        request.addRequestParam("deliveryType", deliverFragment.getDeliverTypeName());
        switch (deliverFragment.getDeliverType()) {
            case OrderConfirmPartDeliverFragment.DELIVER_TYPE_SELF:
                request.addRequestParam("mustAddress", Store
                        .getStore().getStoreAddess());
                break;

            default:
                request.addRequestParam("deliveryAddress", addressFragment.getAddress().getAreaAddress()
                        + addressFragment.getAddress()
                        .getDetailedAddress());
                break;
        }
        request.addRequestParam("paymentType", paymentFragment.getPaymentType() + "");
        request.addRequestParam("retailOrderPrice", totalPrice + "");
        request.addRequestParam("serviceProvidID", Store
                .getStore().getStoreId());
        request.addRequestParam("memberAccount", User
                .getUser().getUseraccount());
        try {
            JSONArray array = new JSONArray();
            JSONObject object = new JSONObject();
            object.put("retailProductManagerID", goods.getId());
            object.put("shopNum", goodsCount);
            object.put("productPrice", goods.getRetailPrice());
            // 备注
            object.put("content", "");
            object.put("orderType", "0");
            array.put(object);
            request.addRequestParam("data", array.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading("下单中,请稍候...");
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                Notify.show("下单失败" + missionMessage.getMessage());

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    JSONObject object;
                    try {
                        object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            Notify.show("下单成功");
                            if (paymentFragment.getPaymentType() == OrderConfirmPartPaymentFragment.PAY_TYPE_CODE_ONLINE) {
                                JSONArray orderArray = object
                                        .optJSONArray("dataList");
                                if (orderArray != null) {
                                    double payPrice = 0;
                                    ArrayList<String> orderNumbers = new ArrayList<String>();
                                    for (int i = 0; i < orderArray.length(); i++) {
                                        JSONObject orderObject = orderArray
                                                .optJSONObject(i);
                                        if (orderObject != null) {
                                            orderNumbers.add(orderObject
                                                    .optString("ordernumber"));
                                            payPrice += orderObject
                                                    .optDouble("orderPrice");
                                        }
                                    }
                                    if (orderNumbers.size() > 0) {
                                        if (payPrice > 0) {
                                            payMoney(orderNumbers, payPrice,
                                                    PayFragment.ORDER_TYPE_LP);
                                            getActivity().finish();
                                            return;
                                        }
                                    }
                                }
                            }
                            showOrder(PayFragment.ORDER_TYPE_LP);
                            getActivity().finish();
                            return;
                        } else {
                            hideLoading();
                            Notify.show(object.optString("message"));
                            return;
                        }
                    } catch (JSONException e) {
                        hideLoading();
                        Notify.show("下单失败");
                        e.printStackTrace();
                        return;
                    }
                } else {
                    hideLoading();
                    Notify.show("下单失败");
                }
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }


    private void getGoodsDetail() {
        Request request = new Request();
        request.setUrl(API.API_LOCAL_BUSINESS_GOODS_DETAIL);
//        request.addRequestParam("retailProductManagerID", goodsId);
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    JSONObject object;
                    try {
                        object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            JSONObject object2 = object.optJSONObject("object");
                            if (object2 != null) {
                                goods = new ModelLocalGoods(object2);
                                showGoodsInfo();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onFinish() {
            }
        });
    }

}
