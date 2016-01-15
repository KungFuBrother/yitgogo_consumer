package yitgogo.consumer.user.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
import yitgogo.consumer.user.model.ModelRecommend;
import yitgogo.consumer.user.model.User;

public class UserRecommendFragment extends BaseNotifyFragment {

    ListView recommendListView;
    TextView countTextView, moneyTextView;
    List<ModelRecommend> recommends;
    RecommendAdapter recommendAdapter;
    Statistics statistics = new Statistics();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_recommend);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(UserRecommendFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(UserRecommendFragment.class.getName());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getStatistics();
        getRecommendList();
    }

    private void init() {
        recommends = new ArrayList<>();
        recommendAdapter = new RecommendAdapter();
    }

    @Override
    protected void findViews() {
        recommendListView = (ListView) contentView
                .findViewById(R.id.recommend_list);
        countTextView = (TextView) contentView
                .findViewById(R.id.recommend_count);
        moneyTextView = (TextView) contentView
                .findViewById(R.id.recommend_money);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        recommendListView.setAdapter(recommendAdapter);
    }

    @Override
    protected void registerViews() {
    }

    private void showStatistics() {
        countTextView.setText(statistics.getNum() + "");
        moneyTextView.setText(statistics.getBonus() + "");
    }

    private void getStatistics() {
        Request request = new Request();
        request.setUrl(API.API_USER_RECOMMEND_STATISTICS);
        request.addRequestParam("memberAccount", User.getUser().getUseraccount());
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
                statistics = new Statistics(requestMessage.getResult());
                showStatistics();
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

    private void getRecommendList() {
        recommends.clear();
        recommendAdapter.notifyDataSetChanged();
        Request request = new Request();
        request.setUrl(API.API_USER_RECOMMEND_LIST);
        request.addRequestParam("memberAccount", User.getUser().getUseraccount());
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
                            JSONArray array = object.optJSONArray("dataList");
                            if (array != null) {
                                for (int i = 0; i < array.length(); i++) {
                                    recommends.add(new ModelRecommend(array.optJSONObject(i)));
                                }
                                recommendAdapter.notifyDataSetChanged();
                            }
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

    class RecommendAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return recommends.size();
        }

        @Override
        public Object getItem(int position) {
            return recommends.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_recommend,
                        null);
                holder = new ViewHolder();
                holder.accountTextView = (TextView) convertView
                        .findViewById(R.id.list_recommend_account);
                holder.scoreTextView = (TextView) convertView
                        .findViewById(R.id.list_recommend_score);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ModelRecommend recommend = recommends.get(position);
            holder.accountTextView.setText(recommend.getMemberAccount());
            holder.scoreTextView.setText(recommend.getTotalBonus());
            return convertView;
        }

        class ViewHolder {
            TextView accountTextView, scoreTextView;
        }
    }

    class Statistics {
        long num = 0;
        long bonus = 0;

        public Statistics() {
        }

        public Statistics(String result) {
            if (result.length() > 0) {
                try {
                    JSONObject object = new JSONObject(result);
                    if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                        JSONObject jsonObject = object.getJSONObject("dataMap");
                        if (jsonObject != null) {
                            num = jsonObject.optLong("num");
                            bonus = jsonObject.optLong("bonus");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

        public long getNum() {
            return num;
        }

        public long getBonus() {
            return bonus;
        }

        @Override
        public String toString() {
            return "Statistics [num=" + num + ", bonus=" + bonus + "]";
        }

    }
}
