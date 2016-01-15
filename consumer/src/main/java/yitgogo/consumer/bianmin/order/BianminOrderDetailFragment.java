package yitgogo.consumer.bianmin.order;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
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
import yitgogo.consumer.money.ui.PayFragment;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.view.NormalAskDialog;
import yitgogo.consumer.view.Notify;

public class BianminOrderDetailFragment extends BaseNotifyFragment {

    //    private SwipeRefreshLayout refreshLayout;
//    private InnerListView productList;
    private TextView orderNumberText, orderStateText, orderDateText, moneyText, typeTextView, accountTextView, detailTextView;
    private TextView payButton;
    private ModelBianminOrder bianminOrder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_order_bianmin_detail);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(BianminOrderDetailFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(BianminOrderDetailFragment.class.getName());
    }

    private void init() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("object")) {
                try {
                    bianminOrder = new ModelBianminOrder(new JSONObject(
                            bundle.getString("object")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void findViews() {
//        refreshLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.order_detail_refresh);
        orderNumberText = (TextView) contentView.findViewById(R.id.order_detail_number);
        orderStateText = (TextView) contentView.findViewById(R.id.order_detail_state);
        orderDateText = (TextView) contentView.findViewById(R.id.order_detail_date);
        orderDateText = (TextView) contentView.findViewById(R.id.order_detail_date);
        moneyText = (TextView) contentView.findViewById(R.id.order_detail_total_money);
        typeTextView = (TextView) contentView.findViewById(R.id.list_order_type);
        accountTextView = (TextView) contentView.findViewById(R.id.list_order_account);
        detailTextView = (TextView) contentView.findViewById(R.id.list_order_detail);
        payButton = (TextView) contentView.findViewById(R.id.order_detail_action_pay);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        if (bianminOrder.getOrderState().equalsIgnoreCase("未付款")) {
            payButton.setVisibility(View.VISIBLE);
        } else {
            payButton.setVisibility(View.GONE);
        }
        orderNumberText.setText("订单号：" + bianminOrder.getOrderNumber());
        orderStateText.setText(bianminOrder.getOrderState());
        orderDateText.setText(bianminOrder.getOrderTime());
        moneyText.setText(Parameters.CONSTANT_RMB + decimalFormat.format(bianminOrder.getSellprice()));
        typeTextView.setText(bianminOrder.getRechargeType());
        accountTextView.setText(bianminOrder.getRechargeAccount());
        if (bianminOrder.getRechargeType().equals("手机")) {
            detailTextView.setText("为" + bianminOrder.getRechargeAccount()
                    + "充值"
                    + decimalFormat.format(bianminOrder.getRechargeMoney())
                    + "元");
        } else if (bianminOrder.getRechargeType().equals("固话/宽带")) {
            detailTextView.setText("为" + bianminOrder.getRechargeAccount()
                    + "充值"
                    + decimalFormat.format(bianminOrder.getRechargeMoney())
                    + "元");
        } else if (bianminOrder.getRechargeType().equals("游戏/Q币")) {
            if (TextUtils.isEmpty(bianminOrder.getGame_area())) {
                detailTextView.setText("为" + bianminOrder.getRechargeAccount()
                        + "充值" + bianminOrder.getRechargeNum() + "Q币");
            } else {
                detailTextView.setText(bianminOrder.getGame_area() + "/"
                        + bianminOrder.getGame_server() + "(单价:"
                        + bianminOrder.getRechargeMoney() + "/数量:"
                        + bianminOrder.getRechargeNum() + ")");
            }
        } else {

        }
        addImageButton(R.drawable.get_goods_delete, "删除订单",
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        NormalAskDialog askDialog = new NormalAskDialog(
                                "确定要删除此订单吗？", "删除", "不删除") {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                if (makeSure) {
                                    deleteBianminOrder();
                                }
                                super.onDismiss(dialog);
                            }
                        };
                        askDialog.show(getFragmentManager(), null);
                    }
                });
    }

    @Override
    protected void registerViews() {
        payButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                payMoney(bianminOrder.getOrderNumber(),
                        bianminOrder.getSellprice(), PayFragment.ORDER_TYPE_BM);
            }
        });
    }


    private void deleteBianminOrder() {
        Request request = new Request();
        request.setUrl(API.API_BIANMIN_ORDER_DELETE);
        request.addRequestParam("orderNumber", bianminOrder.getOrderNumber());
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                Notify.show("删除失败");
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    JSONObject object;
                    try {
                        object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            Notify.show("删除成功");
                            getActivity().finish();
                            return;
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


}
