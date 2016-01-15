package yitgogo.consumer.order.ui;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.order.model.ModelWuliu;
import yitgogo.consumer.order.model.ModelWuliuDetail;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.view.InnerListView;

public class OrderWuliuFragment extends BaseNotifyFragment {

    PullToRefreshScrollView refreshScrollView;
    TextView wuliuStateText;
    InnerListView wuliuList;
    List<ModelWuliu> wulius;
    WuliuAdapter wuliuAdapter;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss");
    String orderNumber = "";

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
        MobclickAgent.onPageStart(OrderWuliuFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(OrderWuliuFragment.class.getName());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getOrderWuliu();
    }

    private void init() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("orderNumber")) {
                orderNumber = bundle.getString("orderNumber");
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
                    public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                        getOrderWuliu();
                    }
                });
    }

    private void getOrderWuliu() {
        wulius.clear();
        wuliuAdapter.notifyDataSetChanged();
        Request request = new Request();
        request.setUrl(API.API_ORDER_WULIU);
        request.addRequestParam("orderNumber", orderNumber);
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
                            JSONArray wuliuArray = object.optJSONArray("dataList");
                            if (wuliuArray != null) {
                                for (int i = 0; i < wuliuArray.length(); i++) {
                                    JSONObject wuliuObject = wuliuArray
                                            .optJSONObject(i);
                                    if (wuliuObject != null) {
                                        wulius.add(new ModelWuliu(wuliuObject));
                                    }
                                }
                            }
                            if (wulius.size() > 0) {
                                wuliuStateText.setText("有" + wulius.size()
                                        + "条物流信息");
                                wuliuAdapter.notifyDataSetChanged();
                            } else {
                                wuliuStateText.setText("暂无物流信息");
                            }
                        } else {
                            wuliuStateText.setText("暂无物流信息");
                        }
                    } catch (JSONException e) {
                        wuliuStateText.setText("暂无物流信息");
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
            // TODO Auto-generated method stub
            ViewHolder holder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_wuliu_item,
                        null);
                holder = new ViewHolder();
                holder.companyNameText = (TextView) convertView
                        .findViewById(R.id.wuliu_item_company);
                holder.numberText = (TextView) convertView
                        .findViewById(R.id.wuliu_item_number);
                holder.senderText = (TextView) convertView
                        .findViewById(R.id.wuliu_item_sender);
                holder.stateText = (TextView) convertView
                        .findViewById(R.id.wuliu_item_state);
                holder.dateText = (TextView) convertView
                        .findViewById(R.id.wuliu_item_date);
                holder.detailList = (InnerListView) convertView
                        .findViewById(R.id.wuliu_item_list);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ModelWuliu wuliu = wulius.get(position);
            holder.companyNameText.setText(wuliu.getCompanyName());
            holder.numberText.setText(wuliu.getWayBill());
            holder.senderText.setText("发货商家：" + wuliu.getPerson() + "\n商家电话："
                    + wuliu.getPersonPhone() + "\n发货时间："
                    + wuliu.getDeliveryTime());
            if (wuliu.isSuceess()) {
                holder.stateText.setText(wuliu.getWuliuObject().getStatus());
                holder.detailList.setAdapter(new WuliuDetailAdapter(wuliu
                        .getWuliuObject().getWuliuDetails()));
            } else {
                holder.dateText.setText(wuliu.getMessage());
            }
            return convertView;
        }

        class ViewHolder {
            TextView companyNameText, numberText, senderText, stateText,
                    dateText;
            InnerListView detailList;
        }
    }

    class WuliuDetailAdapter extends BaseAdapter {

        List<ModelWuliuDetail> wuliuDetails;

        public WuliuDetailAdapter(List<ModelWuliuDetail> wuliuDetails) {
            this.wuliuDetails = wuliuDetails;
        }

        @Override
        public int getCount() {
            return wuliuDetails.size();
        }

        @Override
        public Object getItem(int position) {
            return wuliuDetails.get(position);
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
            ModelWuliuDetail wuliu = wuliuDetails.get(position);
            holder.timeText.setText(wuliu.getTime());
            holder.detailText.setText(wuliu.getContext());
            return convertView;
        }

        class ViewHolder {
            TextView timeText, detailText;
        }
    }

}
