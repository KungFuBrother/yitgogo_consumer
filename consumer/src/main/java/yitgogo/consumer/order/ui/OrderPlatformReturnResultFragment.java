package yitgogo.consumer.order.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
 * Created by Tiger on 2015-11-26.
 */
public class OrderPlatformReturnResultFragment extends BaseNotifyFragment {

    TextView totalMoneyTextView, returnMoneyTextView;
    String productInfoId = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_platform_order_return_result);
        init();
        findViews();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getReturnResult();
    }

    @Override
    protected void findViews() {
        totalMoneyTextView = (TextView) contentView.findViewById(R.id.fragment_platform_return_result_total);
        returnMoneyTextView = (TextView) contentView.findViewById(R.id.fragment_platform_return_result_return);
    }

    private void init() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("productInfoId")) {
                productInfoId = bundle.getString("productInfoId");
            }
        }
    }

    @Override
    protected void initViews() {
        totalMoneyTextView.setText("总计退款金额");
        returnMoneyTextView.setText("(含补偿金额￥10.00)");
    }

    @Override
    protected void registerViews() {
    }

    private void getReturnResult() {
        Request request = new Request();
        request.setUrl(API.API_ORDER_RETURN_RESULT);
        request.addRequestParam("productInfoId", productInfoId);
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
                                double damages = dataMap.optDouble("damages");
                                double totalMoney = dataMap.optDouble("totalMoney");
                                int responsibility = dataMap.optInt("responsibility");
                                totalMoneyTextView.setText("总计退款金额" + Parameters.CONSTANT_RMB + decimalFormat.format(totalMoney));
                                StringBuilder builder = new StringBuilder();
                                builder.append("(");
                                if (responsibility == 2) {
                                    builder.append("含补偿金额");
                                } else {
                                    builder.append("扣取补偿金额");
                                }
                                builder.append(Parameters.CONSTANT_RMB);
                                builder.append(decimalFormat.format(damages));
                                builder.append(")");
                                returnMoneyTextView.setText(builder.toString());
                                return;
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
