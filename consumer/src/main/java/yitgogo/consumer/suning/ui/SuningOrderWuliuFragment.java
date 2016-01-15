package yitgogo.consumer.suning.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ScrollView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
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
import java.util.List;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.suning.model.SuningManager;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.view.InnerListView;
import yitgogo.consumer.view.Notify;

public class SuningOrderWuliuFragment extends BaseNotifyFragment {

    PullToRefreshScrollView refreshScrollView;
    TextView wuliuStateText;
    List<ModelSuningWuliu> wulius;
    WuliuAdapter wuliuAdapter;
    InnerListView wuliuList;
    String orderId = "", skuId = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_order_wuliu);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(SuningOrderWuliuFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(SuningOrderWuliuFragment.class.getName());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getWuliu();
    }

    private void init() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("orderId")) {
                orderId = bundle.getString("orderId");
            }
            if (bundle.containsKey("skuId")) {
                skuId = bundle.getString("skuId");
            }
        }
        wulius = new ArrayList<>();
        wuliuAdapter = new WuliuAdapter();
    }

    @Override
    protected void findViews() {
        refreshScrollView = (PullToRefreshScrollView) contentView
                .findViewById(R.id.wuliu_refresh);
        wuliuStateText = (TextView) contentView.findViewById(R.id.wuliu_state);
        wuliuList = (InnerListView) contentView.findViewById(R.id.wuliu_list);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        wuliuList.setAdapter(wuliuAdapter);
        refreshScrollView.setMode(Mode.PULL_FROM_START);
    }

    @Override
    protected void registerViews() {
        refreshScrollView
                .setOnRefreshListener(new OnRefreshListener<ScrollView>() {

                    @Override
                    public void onRefresh(
                            PullToRefreshBase<ScrollView> refreshView) {
                        getWuliu();
                    }
                });
    }

    /**
     * {
     * "orderId": "6021394830",
     * "sku": "128410606",
     * "shippingTime": null,
     * "receiveTime": null,
     * "orderLogisticStatus": [
     * {
     * "operateTime": "2015-11-18 18:42:33",
     * "operateState": "您的发货清单【成都大件配送中心】已打印，待打印发票"
     * }
     * ],
     * "isSuccess": true,
     * "returnMsg": "success"
     * }
     */
    private void getWuliu() {
        Request request = new Request();
        request.setUrl(API.API_SUNING_ORDER_WULIU);
        JSONObject data = new JSONObject();
        try {
            data.put("accessToken", SuningManager.getSignature().getToken());
            data.put("appKey", SuningManager.appKey);
            data.put("orderId", orderId);
            data.put("skuId", skuId);
            data.put("v", SuningManager.version);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        request.addRequestParam("data", data.toString());
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
                wulius.clear();
                wuliuAdapter.notifyDataSetChanged();
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
                                    getWuliu();
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
                            JSONArray array = object.optJSONArray("orderLogisticStatus");
                            if (array != null) {
                                if (array.length() > 0) {
                                    for (int i = array.length() - 1; i >= 0; i--) {
                                        ModelSuningWuliu wuliu = new ModelSuningWuliu(array.optJSONObject(i));
                                        if (!TextUtils.isEmpty(wuliu.getOperateState())) {
                                            wulius.add(wuliu);
                                        }
                                    }
                                    wuliuStateText.setVisibility(View.GONE);
                                    wuliuAdapter.notifyDataSetChanged();
                                    return;
                                }
                            }
                            wuliuStateText.setVisibility(View.VISIBLE);
                            wuliuStateText.setText("暂无物流信息");
                            return;
                        }
                        Notify.show(object.optString("returnMsg"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onFinish() {
                hideLoading();
                refreshScrollView.onRefreshComplete();
            }
        });
    }

    class WuliuAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return wulius.size();
        }

        @Override
        public Object getItem(int position) {
            return wulius.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(
                        R.layout.list_wuliu_detail, null);
                holder = new ViewHolder();
                holder.detailText = (TextView) convertView
                        .findViewById(R.id.wuliu_detail_text);
                holder.timeText = (TextView) convertView
                        .findViewById(R.id.wuliu_detail_time);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ModelSuningWuliu wuliu = wulius.get(position);
            holder.timeText.setText(wuliu.getOperateTime());
            holder.detailText.setText(wuliu.getOperateState());
            return convertView;
        }

        class ViewHolder {
            TextView timeText, detailText;
        }
    }
//    class GetWuliu extends AsyncTask<Void, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//            showLoading();
//            wulius.clear();
//            wuliuAdapter.notifyDataSetChanged();
//        }
//
//        @Override
//        protected String doInBackground(Void... arg0) {
//            List<NameValuePair> nameValuePairs = new ArrayList<>();
//            JSONObject data = new JSONObject();
//            try {
//                data.put("accessToken", SuningManager.getSignature().getToken());
//                data.put("appKey", SuningManager.appKey);
//                data.put("orderId", orderId);
//                data.put("skuId", skuId);
//                data.put("v", SuningManager.version);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            nameValuePairs.add(new BasicNameValuePair("data", data.toString()));
//            return netUtil.postWithoutCookie(API.API_SUNING_ORDER_WULIU,
//                    nameValuePairs, false, false);
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            hideLoading();
//            refreshScrollView.onRefreshComplete();
//
//        }
//    }

    public class ModelSuningWuliu {

        String operateTime = "", operateState = "";

        public ModelSuningWuliu(JSONObject object) {
            if (object != null) {
                if (!object.optString("operateTime").equalsIgnoreCase("null")) {
                    operateTime = object.optString("operateTime");
                }
                if (!object.optString("operateState").equalsIgnoreCase("null")) {
                    operateState = object.optString("operateState");
                }
            }
        }

        public String getOperateTime() {
            return operateTime;
        }

        public String getOperateState() {
            return operateState;
        }
    }

}
