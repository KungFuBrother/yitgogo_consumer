package yitgogo.consumer.money.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
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
import java.util.List;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.money.model.ModelBankCard;
import yitgogo.consumer.money.model.ModelTakeOutHistory;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.user.model.User;
import yitgogo.consumer.view.InnerListView;
import yitgogo.consumer.view.Notify;

public class TakeOutHistoryFragment extends BaseNotifyFragment {

    DrawerLayout drawerLayout;
    PullToRefreshScrollView refreshScrollView;
    InnerListView dataListView, bankCardsListView;
    TextView clearButton, selectButton;

    List<ModelTakeOutHistory> takeOutHistories;
    TakeOutHistoryAdapter historyAdapter;

    List<ModelBankCard> bankCards;
    BandCardAdapter bandCardAdapter;
    ModelBankCard selectedBankCard = new ModelBankCard();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_money_takeout_list);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TakeOutHistoryFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TakeOutHistoryFragment.class.getName());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getBankCards();
        refresh();
    }

    private void init() {
        takeOutHistories = new ArrayList<>();
        historyAdapter = new TakeOutHistoryAdapter();

        bankCards = new ArrayList<>();
        bandCardAdapter = new BandCardAdapter();
    }

    @Override
    protected void findViews() {
        drawerLayout = (DrawerLayout) contentView.findViewById(R.id.takeout_drawer);
        refreshScrollView = (PullToRefreshScrollView) contentView.findViewById(R.id.takeout_refresh);
        dataListView = (InnerListView) contentView.findViewById(R.id.takeout_list);
        bankCardsListView = (InnerListView) contentView.findViewById(R.id.takeout_list_selector_bankcards);
        clearButton = (TextView) contentView.findViewById(R.id.takeout_list_selector_clear);
        selectButton = (TextView) contentView.findViewById(R.id.takeout_list_selector_select);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        dataListView.setAdapter(historyAdapter);
        bankCardsListView.setAdapter(bandCardAdapter);
        refreshScrollView.setMode(Mode.BOTH);
    }

    @Override
    protected void registerViews() {
        addTextButton("筛选", new OnClickListener() {

            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.RIGHT);
            }
        });
        refreshScrollView
                .setOnRefreshListener(new OnRefreshListener2<ScrollView>() {

                    @Override
                    public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                        refresh();
                    }

                    @Override
                    public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                        getTakeOutHistory();
                    }
                });
        bankCardsListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                selectedBankCard = bankCards.get(arg2);
                bandCardAdapter.notifyDataSetChanged();
            }
        });
        clearButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                selectedBankCard = new ModelBankCard();
                bandCardAdapter.notifyDataSetChanged();

                refresh();
            }
        });
        selectButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                refresh();
            }
        });
    }

    private void refresh() {
        refreshScrollView.setMode(Mode.BOTH);
        pagenum = 0;
        takeOutHistories.clear();
        historyAdapter.notifyDataSetChanged();
        getTakeOutHistory();
    }

    private void getBankCards() {
        bankCards.clear();
        bandCardAdapter.notifyDataSetChanged();
        Request request = new Request();
        request.setUrl(API.MONEY_BANK_BINDED);
        request.addRequestParam("sn", User.getUser().getCacheKey());
        request.addRequestParam("memberid", User.getUser().getUseraccount());
        request.setUseCookie(true);
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                Notify.show("获取绑定的银行卡信息失败！");
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("success")) {
                            JSONArray array = object.optJSONArray("databody");
                            if (array != null) {
                                for (int i = 0; i < array.length(); i++) {
                                    bankCards.add(new ModelBankCard(array.optJSONObject(i)));
                                }
                                bandCardAdapter.notifyDataSetChanged();
                            }
                            return;
                        }
                        Notify.show(object.optString("msg"));
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Notify.show("获取绑定的银行卡信息失败！");
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

    private void getTakeOutHistory() {
        pagenum++;
        Request request = new Request();
        request.setUrl(API.MONEY_BANK_TAKEOUT_HISTORY);
        request.setUseCookie(true);
        request.addRequestParam("pageindex", String.valueOf(pagenum));
        request.addRequestParam("pagecount", String.valueOf(pagesize));
        if (!TextUtils.isEmpty(selectedBankCard.getId())) {
            request.addRequestParam("bankcardid", selectedBankCard.getId());
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
                        if (object.optString("state").equalsIgnoreCase("success")) {
                            JSONObject jsonObject = object.optJSONObject("databody");
                            if (jsonObject != null) {
                                JSONArray array = jsonObject.optJSONArray("data");
                                if (array != null) {
                                    if (array.length() < pagesize) {
                                        refreshScrollView.setMode(Mode.PULL_FROM_START);
                                    }
                                    for (int i = 0; i < array.length(); i++) {
                                        takeOutHistories.add(new ModelTakeOutHistory(array.optJSONObject(i)));
                                    }
                                    if (!takeOutHistories.isEmpty()) {
                                        historyAdapter.notifyDataSetChanged();
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
                if (takeOutHistories.isEmpty()) {
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

    class TakeOutHistoryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return takeOutHistories.size();
        }

        @Override
        public Object getItem(int position) {
            return takeOutHistories.get(position);
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
                        R.layout.list_money_take_out_history, null);
                viewHolder.amountTextView = (TextView) convertView
                        .findViewById(R.id.list_takeout_amount);
                viewHolder.stateTextView = (TextView) convertView
                        .findViewById(R.id.list_takeout_state);
                viewHolder.bankTextView = (TextView) convertView
                        .findViewById(R.id.list_takeout_bank);
                viewHolder.dateTextView = (TextView) convertView
                        .findViewById(R.id.list_takeout_time);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            ModelTakeOutHistory takeOutHistory = takeOutHistories.get(position);
            viewHolder.amountTextView.setText(Parameters.CONSTANT_RMB
                    + decimalFormat.format(takeOutHistory.getAmount()));
            viewHolder.bankTextView.setText(takeOutHistory.getUserbank()
                    + "(尾号"
                    + takeOutHistory.getUserbankid().substring(
                    takeOutHistory.getUserbankid().length() - 4,
                    takeOutHistory.getUserbankid().length()) + ")");
            viewHolder.dateTextView.setText(takeOutHistory.getDatatime());
            viewHolder.stateTextView.setText(takeOutHistory.getState());
            return convertView;
        }

        class ViewHolder {
            TextView amountTextView, stateTextView, bankTextView, dateTextView;
        }
    }

    class BandCardAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return bankCards.size();
        }

        @Override
        public Object getItem(int position) {
            return bankCards.get(position);
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
                        R.layout.list_pay_bank_card, null);
                viewHolder.selected = (ImageView) convertView
                        .findViewById(R.id.bank_card_bank_selection);
                viewHolder.bankImageView = (ImageView) convertView
                        .findViewById(R.id.bank_card_bank_image);
                viewHolder.cardNumberTextView = (TextView) convertView
                        .findViewById(R.id.bank_card_number);
                viewHolder.cardTypeTextView = (TextView) convertView
                        .findViewById(R.id.bank_card_type);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            ModelBankCard bankCard = bankCards.get(position);
            if (bankCard.getId().equals(selectedBankCard.getId())) {
                viewHolder.selected.setImageResource(R.drawable.iconfont_check_checked);
            } else {
                viewHolder.selected.setImageResource(R.drawable.iconfont_check_normal);
            }
            ImageLoader.getInstance().displayImage(bankCard.getBank().getIcon(), viewHolder.bankImageView);
            viewHolder.cardNumberTextView.setText(getSecretCardNuber(bankCard.getBanknumber()));
            viewHolder.cardTypeTextView.setText(bankCard.getBank().getName() + " " + bankCard.getCardType());
            return convertView;
        }

        class ViewHolder {
            ImageView selected, bankImageView;
            TextView cardNumberTextView, cardTypeTextView;
        }
    }

}
