package yitgogo.consumer.order.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
public class OrderPlatformReturnFragment extends BaseNotifyFragment {

    TextView productNameTextView, productPriceTextView, contactPhoneTextView;
    EditText reasonEditText;
    Button commitButton;

    String productName = "";
    double productPrice = 0;

    String saleId = "";
    String supplierId = "";
    String orderNumber = "";
    String productInfo = "";
    int buyCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_platform_order_return);
        init();
        findViews();
    }

    @Override
    protected void findViews() {
        productNameTextView = (TextView) contentView.findViewById(R.id.fragment_platform_return_product_name);
        productPriceTextView = (TextView) contentView.findViewById(R.id.fragment_platform_return_product_price);
        contactPhoneTextView = (TextView) contentView.findViewById(R.id.fragment_platform_return_phone);
        reasonEditText = (EditText) contentView.findViewById(R.id.fragment_platform_return_reason);
        commitButton = (Button) contentView.findViewById(R.id.fragment_platform_return_commit);
        initViews();
        registerViews();
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

            if (bundle.containsKey("saleId")) {
                saleId = bundle.getString("saleId");
            }
            if (bundle.containsKey("supplierId")) {
                supplierId = bundle.getString("supplierId");
            }
            if (bundle.containsKey("orderNumber")) {
                orderNumber = bundle.getString("orderNumber");
            }
            if (bundle.containsKey("productInfo")) {
                productInfo = bundle.getString("productInfo");
            }
            if (bundle.containsKey("buyCount")) {
                buyCount = bundle.getInt("buyCount");
            }
        }
    }

    @Override
    protected void initViews() {
        productNameTextView.setText(productName);
        productPriceTextView.setText(Parameters.CONSTANT_RMB + decimalFormat.format(productPrice) + "  ×" + buyCount);
    }

    @Override
    protected void registerViews() {
        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(reasonEditText.getText().toString())) {
                    returnProduct();
                } else {
                    Notify.show("请填写退货原因");
                }
            }
        });
    }

    private void returnProduct() {
        Request request = new Request();
        request.setUrl(API.API_ORDER_RETURN);
        request.addRequestParam("saleId", saleId);
        request.addRequestParam("supplierId", supplierId);
        request.addRequestParam("orderNumber", orderNumber);
        request.addRequestParam("productInfo", productInfo);
        request.addRequestParam("reason", reasonEditText.getText().toString());
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
                            jump(OrderPlatformReturnCommitedFragment.class.getName(), "申请退货");
                            getActivity().finish();
                        } else {
                            Notify.show(object.optString("message"));
                        }
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Notify.show("申请退货失败");
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

}
