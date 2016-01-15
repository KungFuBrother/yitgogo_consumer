package yitgogo.consumer.user.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import yitgogo.consumer.tools.API;
import yitgogo.consumer.user.model.User;

public class UserScoreFragment extends BaseNotifyFragment {

    TextView scoreTotalTextView, signButton;
    LinearLayout detailButton, shareButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_score);
        findViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(UserScoreFragment.class.getName());
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(UserScoreFragment.class.getName());
        if (User.getUser().isLogin()) {
            getSignState();
            getUserScore();
        }
    }

    @Override
    protected void findViews() {
        scoreTotalTextView = (TextView) contentView
                .findViewById(R.id.score_total);
        signButton = (TextView) contentView.findViewById(R.id.score_sign);
        detailButton = (LinearLayout) contentView
                .findViewById(R.id.score_detail);
        shareButton = (LinearLayout) contentView.findViewById(R.id.score_share);
        registerViews();
    }

    @Override
    protected void registerViews() {
        signButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sign();
            }
        });
        detailButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                jump(UserScoreDetailFragment.class.getName(), "积分详情");
            }
        });
        shareButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                jump(UserShareFragment.class.getName(), "推荐好友");
            }
        });
    }

    private void getUserScore() {
        Request request = new Request();
        request.setUrl(API.API_USER_JIFEN);
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
                            JSONObject jifenObject = object.optJSONObject("object");
                            if (jifenObject != null) {
                                String score = jifenObject.optString("totalBonus");
                                if (!score.equalsIgnoreCase("null")) {
                                    scoreTotalTextView.setText(score);
                                } else {
                                    scoreTotalTextView.setText("0");
                                }
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

    private void getSignState() {
        Request request = new Request();
        request.setUrl(API.API_USER_SIGN_STATE);
        request.addRequestParam("userAccount", User.getUser().getUseraccount());
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
                            JSONObject jsonObject = object.getJSONObject("object");
                            String isSign = jsonObject.getString("isSign");
                            if (!isSign.equals("0")) {
                                signButton.setText("今日已签到");
                                signButton.setTextColor(getResources().getColor(
                                        R.color.textColorThird));
                                signButton.setClickable(false);
                            } else {
                                signButton.setText("签到领积分");
                                signButton.setTextColor(getResources().getColor(
                                        R.color.textColorSecond));
                                signButton.setClickable(true);
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

    private void sign() {
        signButton.setText("今日已签到");
        signButton.setTextColor(getResources().getColor(R.color.textColorThird));
        signButton.setClickable(false);
        Request request = new Request();
        request.setUrl(API.API_USER_SIGN);
        request.addRequestParam("userAccount", User.getUser().getUseraccount());
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
                            getSignState();
                            getUserScore();
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

}
