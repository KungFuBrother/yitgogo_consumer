package yitgogo.consumer.suning.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smartown.controller.mission.MissionController;
import com.smartown.controller.mission.MissionMessage;
import com.smartown.controller.mission.Request;
import com.smartown.controller.mission.RequestListener;
import com.smartown.controller.mission.RequestMessage;
import com.smartown.yitian.gogo.R;

import org.json.JSONException;
import org.json.JSONObject;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.view.Notify;

/**
 * Created by Tiger on 2015-12-01.
 */
public class SuningOrderReturnBeforeReceivedFragment extends BaseNotifyFragment {

    String productName = "";
    double productPrice = 0;
    int buyCount = 0;
    String tradeNo = "", produNo = "";
    int type = 0;

    TextView productNameTextView;
    TextView productPriceTextView;
    Button returnButton;
    TextView phoneTextView;
    TextView ruleTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_suning_order_return_before_received);
        init();
        findViews();
    }

    private void init() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("productName")) {
                productName = bundle.getString("productName");
            }
            if (bundle.containsKey("productPrice")) {
                productPrice = bundle.getDouble("productPrice");
            }
            if (bundle.containsKey("buyCount")) {
                buyCount = bundle.getInt("buyCount");
            }
            if (bundle.containsKey("tradeNo")) {
                tradeNo = bundle.getString("tradeNo");
            }
            if (bundle.containsKey("produNo")) {
                produNo = bundle.getString("produNo");
            }
            if (bundle.containsKey("type")) {
                type = bundle.getInt("type");
            }
        }
    }

    @Override
    protected void findViews() {
        productNameTextView = (TextView) contentView.findViewById(R.id.fragment_suning_return_product_name);
        productPriceTextView = (TextView) contentView.findViewById(R.id.fragment_suning_return_product_price);
        returnButton = (Button) contentView.findViewById(R.id.fragment_suning_return_commit);
        phoneTextView = (TextView) contentView.findViewById(R.id.fragment_suning_return_phone);
        ruleTextView = (TextView) contentView.findViewById(R.id.fragment_suning_return_rule);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        productNameTextView.setText(productName);
        productPriceTextView.setText(Parameters.CONSTANT_RMB + decimalFormat.format(productPrice) + "  ×" + buyCount);
    }

    @Override
    protected void registerViews() {
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnProduct();
            }
        });
        phoneTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                Uri data = Uri.parse("tel:4008516516");
                intent.setData(data);
                startActivity(intent);
            }
        });
        ruleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jump(SuningOrderReturnRuleFragment.class.getName(), "退换货规则");
            }
        });
    }

    private void returnProduct() {
        Request request = new Request();
        request.setUrl(API.API_SUNING_ORDER_RECEIVE_RETURN);
        request.addRequestParam("tradeNo", tradeNo);
        request.addRequestParam("produNo", produNo);
        request.addRequestParam("type", String.valueOf(type));
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
                            SuningOrderListFragment.needRefersh = true;
                            Notify.show("申请退货成功");
                            getActivity().finish();
                        } else {
                            Notify.show(object.optString("message"));
                        }
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Notify.show("申请退货失败");
                }
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

//    class ReturnProduct extends AsyncTask<Void, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//            showLoading();
//        }
//
//        @Override
//        protected String doInBackground(Void... voids) {
//            List<NameValuePair> nameValuePairs = new ArrayList<>();
//            nameValuePairs.add(new BasicNameValuePair("tradeNo", tradeNo));
//            nameValuePairs.add(new BasicNameValuePair("produNo", produNo));
//            nameValuePairs.add(new BasicNameValuePair("type", String.valueOf(type)));
//            return NetUtil.getInstance().postWithCookie(API.API_SUNING_ORDER_RECEIVE_RETURN, nameValuePairs);
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            hideLoading();
//            if (!TextUtils.isEmpty(result)) {
//                try {
//                    JSONObject object = new JSONObject(result);
//                    if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
//                        SuningOrderListFragment.needRefersh = true;
//                        Notify.show("申请退货成功");
//                        getActivity().finish();
//                    } else {
//                        Notify.show(object.optString("message"));
//                    }
//                    return;
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//            Notify.show("申请退货失败");
//        }
//    }

}
