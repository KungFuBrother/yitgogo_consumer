package yitgogo.consumer.money.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
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
import yitgogo.consumer.money.model.ModelTrade;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.view.InnerListView;
import yitgogo.consumer.view.Notify;

public class TradeHistoryFragment extends BaseNotifyFragment {

    PullToRefreshScrollView refreshScrollView;
    ListView listView;

    List<ModelTrade> trades;
    TradeHistoryAdapter tradeHistoryAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_money_trade_list);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TradeHistoryFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TradeHistoryFragment.class.getName());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refresh();
    }

    private void init() {
        trades = new ArrayList<>();
        tradeHistoryAdapter = new TradeHistoryAdapter();
    }

    @Override
    protected void findViews() {
        listView = (InnerListView) contentView.findViewById(R.id.trade_list);
        refreshScrollView = (PullToRefreshScrollView) contentView
                .findViewById(R.id.trade_refresh);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        listView.setAdapter(tradeHistoryAdapter);
        refreshScrollView.setMode(Mode.BOTH);
    }

    @Override
    protected void registerViews() {
        refreshScrollView.setOnRefreshListener(new OnRefreshListener2<ScrollView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                refresh();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                getTradeHistory();
            }
        });
    }

    private void refresh() {
        refreshScrollView.setMode(Mode.BOTH);
        pagenum = 0;
        trades.clear();
        tradeHistoryAdapter.notifyDataSetChanged();
        getTradeHistory();
    }

    private void getTradeHistory() {
        pagenum++;
        Request request = new Request();
        request.setUrl(API.MONEY_TRADE_DETAIL);
        request.setUseCookie(true);
        request.addRequestParam("pageindex", String.valueOf(pagenum));
        request.addRequestParam("pagecount", String.valueOf(pagesize));
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
                            JSONObject jsonObject = object.optJSONObject("databody");
                            if (jsonObject != null) {
                                JSONArray array = jsonObject.optJSONArray("data");
                                if (array != null) {
                                    if (array.length() < pagesize) {
                                        refreshScrollView.setMode(Mode.PULL_FROM_START);
                                    }
                                    for (int i = 0; i < array.length(); i++) {
                                        trades.add(new ModelTrade(array.optJSONObject(i)));
                                    }
                                    if (trades.size() > 0) {
                                        tradeHistoryAdapter.notifyDataSetChanged();
                                        return;
                                    }
                                }
                            }
                        }
                        Notify.show(object.optString("msg"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                refreshScrollView.setMode(Mode.PULL_FROM_START);
                if (trades.isEmpty()) {
                    loadingEmpty();
                }
            }

            @Override
            protected void onFinish() {
                hideLoading();
                refreshScrollView.onRefreshComplete();
            }
        });
    }

    class TradeHistoryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return trades.size();
        }

        @Override
        public Object getItem(int position) {
            return trades.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = layoutInflater.inflate(
                        R.layout.list_money_trade_history, null);
                viewHolder.amountTextView = (TextView) convertView
                        .findViewById(R.id.list_trade_amount);
                viewHolder.detailTextView = (TextView) convertView
                        .findViewById(R.id.list_trade_detail);
                viewHolder.dateTextView = (TextView) convertView
                        .findViewById(R.id.list_trade_time);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            ModelTrade trade = trades.get(position);
            if (trade.isAmountFlow()) {
                viewHolder.amountTextView.setTextColor(Color.rgb(69, 183, 69));
                viewHolder.amountTextView.setText("+" + Parameters.CONSTANT_RMB + decimalFormat.format(trade.getAmount()));
            } else {
                viewHolder.amountTextView.setTextColor(Color.rgb(218, 72, 68));
                viewHolder.amountTextView.setText("-" + Parameters.CONSTANT_RMB + decimalFormat.format(trade.getAmount()));
            }
            viewHolder.detailTextView.setText(trade.getDescription());
            viewHolder.dateTextView.setText(trade.getDatatime());
            return convertView;
        }

        class ViewHolder {
            TextView amountTextView, detailTextView, dateTextView;
        }
    }

}
