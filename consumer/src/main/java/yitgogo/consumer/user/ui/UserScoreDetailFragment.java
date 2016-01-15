package yitgogo.consumer.user.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
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
import yitgogo.consumer.tools.API;
import yitgogo.consumer.user.model.ModelScoreDetail;
import yitgogo.consumer.user.model.User;

public class UserScoreDetailFragment extends BaseNotifyFragment {

    PullToRefreshListView refreshListView;
    List<ModelScoreDetail> scoreDetails;
    ScoreDetailAdapter scoreDetailAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_score_detail);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(UserScoreDetailFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(UserScoreDetailFragment.class.getName());
    }

    private void init() {
        scoreDetails = new ArrayList<>();
        scoreDetailAdapter = new ScoreDetailAdapter();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getScoreDetial();
    }

    @Override
    protected void findViews() {
        refreshListView = (PullToRefreshListView) contentView
                .findViewById(R.id.score_detail_list);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        refreshListView.setAdapter(scoreDetailAdapter);
        refreshListView.setMode(Mode.BOTH);
    }

    @Override
    protected void registerViews() {
        refreshListView
                .setOnRefreshListener(new OnRefreshListener2<ListView>() {

                    @Override
                    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                        refreshListView.setMode(Mode.BOTH);
                        pagenum = 0;
                        scoreDetails.clear();
                        scoreDetailAdapter.notifyDataSetChanged();
                        getScoreDetial();
                    }

                    @Override
                    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                        getScoreDetial();
                    }
                });
    }

    private void getScoreDetial() {
        if (pagenum == 0) {
            showLoading();
        }
        pagenum++;
        Request request = new Request();
        request.setUrl(API.API_USER_JIFEN_DETAIL);
        request.addRequestParam("pagenum", String.valueOf(pagenum));
        request.addRequestParam("pagesize", String.valueOf(pagesize));
        request.addRequestParam("memberAccount", User.getUser().getUseraccount());
        request.setUseCookie(true);
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
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            JSONArray array = object.optJSONArray("dataList");
                            if (array != null) {
                                if (array.length() > 0) {
                                    if (array.length() < pagesize) {
                                        refreshListView.setMode(Mode.PULL_FROM_START);
                                    }
                                    for (int i = 0; i < array.length(); i++) {
                                        scoreDetails.add(new ModelScoreDetail(array
                                                .optJSONObject(i)));
                                    }
                                    scoreDetailAdapter.notifyDataSetChanged();
                                    return;
                                } else {
                                    refreshListView.setMode(Mode.PULL_FROM_START);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (scoreDetails.isEmpty()) {
                    loadingEmpty();
                }
            }

            @Override
            protected void onFinish() {
                hideLoading();
                refreshListView.onRefreshComplete();
            }
        });
    }

    class ScoreDetailAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return scoreDetails.size();
        }

        @Override
        public Object getItem(int position) {
            return scoreDetails.get(position);
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
                        R.layout.list_score_detail, null);
                holder = new ViewHolder();
                holder.amountTextView = (TextView) convertView
                        .findViewById(R.id.list_score_amount);
                holder.dateTextView = (TextView) convertView
                        .findViewById(R.id.list_score_date);
                holder.detailTextView = (TextView) convertView
                        .findViewById(R.id.list_score_detail);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (scoreDetails.get(position).getBonusType().contains("收入")) {
                holder.amountTextView.setTextColor(getResources().getColor(
                        R.color.blue));
                holder.amountTextView.setText("+"
                        + scoreDetails.get(position).getBonusAmount());
            } else if (scoreDetails.get(position).getBonusType().contains("支出")) {
                holder.amountTextView.setTextColor(getResources().getColor(
                        R.color.red));
                holder.amountTextView.setText("-"
                        + scoreDetails.get(position).getBonusAmount());
            } else {
                holder.amountTextView.setTextColor(getResources().getColor(
                        R.color.textColorSecond));
                holder.amountTextView.setText(""
                        + scoreDetails.get(position).getBonusAmount());
            }
            holder.dateTextView.setText(scoreDetails.get(position)
                    .getRecordTime());
            holder.detailTextView.setText(scoreDetails.get(position)
                    .getDetails());
            return convertView;
        }

        class ViewHolder {
            TextView detailTextView, amountTextView, dateTextView;
        }

    }

}
