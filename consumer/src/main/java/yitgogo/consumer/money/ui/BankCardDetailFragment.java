package yitgogo.consumer.money.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
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

import org.json.JSONException;
import org.json.JSONObject;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.money.model.ModelBankCard;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.view.NormalAskDialog;
import yitgogo.consumer.view.Notify;

public class BankCardDetailFragment extends BaseNotifyFragment {

    ImageView imageView;
    TextView cardNumberTextView, cardTypeTextView, deleteButton;
    ModelBankCard bankCard = new ModelBankCard();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_money_backcard_detail);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(BankCardDetailFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(BankCardDetailFragment.class.getName());
    }

    private void init() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("bankCard")) {
                try {
                    bankCard = new ModelBankCard(new JSONObject(bundle.getString("bankCard")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void findViews() {
        imageView = (ImageView) contentView.findViewById(R.id.bank_card_detail_image);
        cardNumberTextView = (TextView) contentView.findViewById(R.id.bank_card_detail_number);
        cardTypeTextView = (TextView) contentView.findViewById(R.id.bank_card_detail_type);
        deleteButton = (TextView) contentView.findViewById(R.id.bank_card_detail_delete);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        ImageLoader.getInstance().displayImage(bankCard.getBank().getIcon(), imageView);
        cardNumberTextView.setText(getSecretCardNuber(bankCard.getBanknumber()));
        cardTypeTextView.setText(bankCard.getBank().getName() + "  " + bankCard.getCardType());
    }

    @Override
    protected void registerViews() {
        deleteButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                NormalAskDialog askDialog = new NormalAskDialog("确定要解绑这张银行卡吗？", "解绑", "取消") {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (makeSure) {
                            PayPasswordDialog payPasswordDialog = new PayPasswordDialog(
                                    "请输入支付密码", false) {
                                public void onDismiss(DialogInterface dialog) {
                                    if (!TextUtils.isEmpty(payPassword)) {
                                        unBindBankCard(payPassword);
                                    }
                                    super.onDismiss(dialog);
                                }
                            };
                            payPasswordDialog.show(getFragmentManager(), null);
                        }
                        super.onDismiss(dialog);
                    }
                };
                askDialog.show(getFragmentManager(), null);
            }
        });
    }

    private void unBindBankCard(String paypassword) {
        Request request = new Request();
        request.setUrl(API.MONEY_BANK_UNBIND);
        request.addRequestParam("bankcardid", bankCard.getId());
        request.addRequestParam("paypassword", paypassword);
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
                        if (object.optString("state").equalsIgnoreCase("success")) {
                            JSONObject databody = object.optJSONObject("databody");
                            if (databody.optBoolean("unbind")) {
                                Notify.show("解绑成功");
                                getActivity().finish();
                                return;
                            }
                        }
                        Notify.show(object.optString("msg"));
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
