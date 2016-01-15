package yitgogo.consumer.activity.egg.ui;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartown.controller.mission.MissionController;
import com.smartown.controller.mission.MissionMessage;
import com.smartown.controller.mission.Request;
import com.smartown.controller.mission.RequestListener;
import com.smartown.controller.mission.RequestMessage;
import com.smartown.yitian.gogo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.activity.shake.model.ModelActivity;
import yitgogo.consumer.activity.shake.model.ModelAward;
import yitgogo.consumer.money.ui.PayMoneyFragment;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.tools.ScreenUtil;
import yitgogo.consumer.user.model.User;
import yitgogo.consumer.view.Notify;

/**
 * Created by Tiger on 2015-12-07.
 */
public class EggMainFragment extends BaseNotifyFragment {

    int position = 0;
    int[] eggImages = {R.drawable.egg_anim_2, R.drawable.egg_anim_3, R.drawable.egg_anim_4, R.drawable.egg_anim_5, R.drawable.egg_anim_6, R.drawable.egg_anim_7, R.drawable.egg_anim_8};
    private ImageView backgroundImageView;
    private ImageView animImageView;
    private LinearLayout historyButton;
    private Button payButton;
    private RecyclerView awardRecyclerView;
    private TextView ruleTextView;
    private SoundPool soundPool;
    private SparseIntArray soundIds;
    private int eggMusic = 1;
    private String activityId = "90";
    private ModelActivity activityetail = new ModelActivity();
    private List<ModelAward> awards;
    private AwardAdapter awardAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main_egg);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        getEggActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void init() {
        awards = new ArrayList<>();
        awardAdapter = new AwardAdapter();
        initSoundPool();
    }

    @Override
    protected void findViews() {
        backgroundImageView = (ImageView) contentView.findViewById(R.id.egg_back);

        animImageView = (ImageView) contentView.findViewById(R.id.egg_anim);

        historyButton = (LinearLayout) contentView.findViewById(R.id.egg_history);
        payButton = (Button) contentView.findViewById(R.id.egg_pay);
        awardRecyclerView = (RecyclerView) contentView.findViewById(R.id.egg_award);
        ruleTextView = (TextView) contentView.findViewById(R.id.egg_rule);

        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        animImageView.setEnabled(false);

        FrameLayout.LayoutParams backLayoutParams = new FrameLayout.LayoutParams(ScreenUtil.getScreenWidth(), (int) ((float) ScreenUtil.getScreenWidth() / 9.0f * 16.0f));
        backgroundImageView.setLayoutParams(backLayoutParams);

        LinearLayout.LayoutParams animLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) ((float) ScreenUtil.getScreenWidth() / 540.0f * 453.0f));
        animImageView.setLayoutParams(animLayoutParams);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) ((float) ScreenUtil.getScreenWidth() / 4.0f));
        awardRecyclerView.setLayoutParams(layoutParams);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        awardRecyclerView.setLayoutManager(linearLayoutManager);
        awardRecyclerView.setAdapter(awardAdapter);
    }

    private void animate() {
        animImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (position == 3) {
                    playSound();
                }
                if (position < eggImages.length) {
                    animImageView.setImageResource(eggImages[position]);
                    position++;
                    if (position < eggImages.length) {
                        animate();
                    } else {
                        winEgg();
                    }
                }
            }
        }, 100);
    }

    @Override
    protected void registerViews() {
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAwardHistory();
            }
        });
        animImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoading();
                position = 0;
                animate();
            }
        });
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payMoney();
            }
        });
    }

    private void showActivityDetail() {
        //初始状态
        position = 0;
        ImageLoader.getInstance().displayImage(getBigImageUrl(activityetail.getActivityImg()), backgroundImageView, new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.egg_back_default)
                .showImageForEmptyUri(R.drawable.egg_back_default)
                .showImageOnFail(R.drawable.egg_back_default)
                .resetViewBeforeLoading(true).cacheInMemory(false)
                .cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build());
        animImageView.setImageResource(R.drawable.egg_anim_0);
        ruleTextView.setText(activityetail.getRule());
        awardAdapter.notifyDataSetChanged();
        switch (activityetail.getJoinState()) {
            case 0:
                //能参与活动
                animImageView.setEnabled(true);
                historyButton.setVisibility(View.VISIBLE);
                payButton.setVisibility(View.GONE);
                break;
            case 1:
                //未付款
                if (activityetail.getJoinMoney() == 0) {
                    animImageView.setEnabled(true);
                    payButton.setVisibility(View.GONE);
                    break;
                }
                animImageView.setEnabled(false);
                historyButton.setVisibility(View.VISIBLE);
                payButton.setEnabled(true);
                payButton.setVisibility(View.VISIBLE);
                payButton.setBackgroundResource(R.drawable.shape_pay_money_btn_bg);
                payButton.setText((int) activityetail.getJoinMoney() + "元砸惊喜");
                break;
            case 2:
                //活动结束
                animImageView.setEnabled(false);
                historyButton.setVisibility(View.VISIBLE);
                payButton.setEnabled(false);
                payButton.setVisibility(View.VISIBLE);
                payButton.setBackgroundResource(R.drawable.shape_pay_money_btn_disable);
                payButton.setText("已结束");
                break;
            case 3:
                //未开始
                animImageView.setEnabled(false);
                historyButton.setVisibility(View.GONE);
                payButton.setEnabled(false);
                payButton.setVisibility(View.VISIBLE);
                payButton.setBackgroundResource(R.drawable.shape_pay_money_btn_disable);
                payButton.setText("未开始");
                break;
            case 4:
                //参与次数上限
                animImageView.setEnabled(false);
                historyButton.setVisibility(View.VISIBLE);
                payButton.setEnabled(false);
                payButton.setVisibility(View.VISIBLE);
                payButton.setBackgroundResource(R.drawable.shape_pay_money_btn_disable);
                payButton.setText("次数已达上限");
                break;
            case 5:
                //当天参与次数上限
                animImageView.setEnabled(false);
                historyButton.setVisibility(View.VISIBLE);
                payButton.setEnabled(false);
                payButton.setVisibility(View.VISIBLE);
                payButton.setBackgroundResource(R.drawable.shape_pay_money_btn_disable);
                payButton.setText("次数已达上限");
                break;
        }
    }

    private void payMoney() {
        Bundle bundle = new Bundle();
        bundle.putString("activityId", activityetail.getId());
        bundle.putString("activityName", activityetail.getActivityName());
        bundle.putInt("activityType", PayMoneyFragment.PAY_TYPE_EGG);
        bundle.putDouble("totalMoney", activityetail.getJoinMoney());
        jump(PayMoneyFragment.class.getName(), "支付", bundle);
    }

    private void initSoundPool() {
        soundIds = new SparseIntArray();
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 100);

        AssetManager assetManager = getActivity().getAssets();
        AssetFileDescriptor afd = null;
        try {
            afd = assetManager.openFd("egg.mp3");
        } catch (IOException e) {
            e.printStackTrace();
        }
        soundIds.put(eggMusic, soundPool.load(afd, 1));
    }

    private void playSound() {
        soundPool.play(soundIds.get(eggMusic), 1, 1, 1, 0, 1);
    }

    private void getEggActivity() {
        Request request = new Request();
        request.setUrl(API.API_ACTIVITY_EGG_DETAIL);
        request.addRequestParam("id", activityId);
        request.addRequestParam("memberAccount", User.getUser().getUseraccount());
        request.setUseCookie(true);
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
                awards.clear();
                awardAdapter.notifyDataSetChanged();
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
                            JSONArray awardArray = object.optJSONArray("dataList");
                            if (awardArray != null) {
                                for (int i = 0; i < awardArray.length(); i++) {
                                    awards.add(new ModelAward(awardArray.optJSONObject(i)));
                                }
                            }
                            activityetail = new ModelActivity(object.optJSONObject("object"));
                            showActivityDetail();
                            return;
                        }
                        Notify.show(object.optString("message"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Notify.show("参与人数太多，请稍后再试。");
                }
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

//    class GetEggActivity extends AsyncTask<Void, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//            List<NameValuePair> nameValuePairs = new ArrayList<>();
//            nameValuePairs.add(new BasicNameValuePair("id", activityId));
//            nameValuePairs.add(new BasicNameValuePair("memberAccount", User.getUser().getUseraccount()));
//            return netUtil.postWithCookie(API.API_ACTIVITY_EGG_DETAIL, nameValuePairs);
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//
//            if (!TextUtils.isEmpty(result)) {
//                try {
//                    JSONObject object = new JSONObject(result);
//                    if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
//                        JSONArray awardArray = object.optJSONArray("dataList");
//                        if (awardArray != null) {
//                            for (int i = 0; i < awardArray.length(); i++) {
//                                awards.add(new ModelAward(awardArray.optJSONObject(i)));
//                            }
//                        }
//                        activityetail = new ModelActivity(object.optJSONObject("object"));
//                        showActivityDetail();
//                        return;
//                    }
//                    Notify.show(object.optString("message"));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                Notify.show("参与人数太多，请稍后再试。");
//            }
//        }
//    }

    private void reTry() {
        Request request = new Request();
        request.setUrl(API.API_ACTIVITY_EGG_DETAIL);
        request.addRequestParam("id", activityId);
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
                            JSONArray awardArray = object.optJSONArray("dataList");
                            if (awardArray != null) {
                                for (int i = 0; i < awardArray.length(); i++) {
                                    awards.add(new ModelAward(awardArray.optJSONObject(i)));
                                }
                            }
                            activityetail = new ModelActivity(object.optJSONObject("object"));
                            showActivityDetail();
                            if (activityetail.getJoinMoney() > 0) {
                                if (activityetail.getJoinState() == 1) {
                                    payMoney();
                                }
                            }
                            return;
                        }
                        Notify.show(object.optString("message"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Notify.show("参与人数太多，请稍后再试。");
                }
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

//    class Retry extends AsyncTask<Void, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//            showLoading();
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//            List<NameValuePair> nameValuePairs = new ArrayList<>();
//            nameValuePairs.add(new BasicNameValuePair("id", activityId));
//            nameValuePairs.add(new BasicNameValuePair("memberAccount", User.getUser().getUseraccount()));
//            return netUtil.postWithCookie(API.API_ACTIVITY_EGG_DETAIL, nameValuePairs);
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            hideLoading();
//            if (!TextUtils.isEmpty(result)) {
//                try {
//                    JSONObject object = new JSONObject(result);
//                    if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
//                        JSONArray awardArray = object.optJSONArray("dataList");
//                        if (awardArray != null) {
//                            for (int i = 0; i < awardArray.length(); i++) {
//                                awards.add(new ModelAward(awardArray.optJSONObject(i)));
//                            }
//                        }
//                        activityetail = new ModelActivity(object.optJSONObject("object"));
//                        showActivityDetail();
//                        if (activityetail.getJoinMoney() > 0) {
//                            if (activityetail.getJoinState() == 1) {
//                                payMoney();
//                            }
//                        }
//                        return;
//                    }
//                    Notify.show(object.optString("message"));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                Notify.show("参与人数太多，请稍后再试。");
//            }
//        }
//    }

    private void getAwardHistory() {
        Request request = new Request();
        request.setUrl(API.API_ACTIVITY_AWARD_HISTORY);
        request.addRequestParam("activityId", activityId);
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
                    FragmentResultDialog dialog = FragmentResultDialog.newInstance(requestMessage.getResult());
                    dialog.show(getFragmentManager(), FragmentResultDialog.class.getName());
                }
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

//    class GetAwardHistory extends AsyncTask<Void, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//            showLoading();
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//            List<NameValuePair> nameValuePairs = new ArrayList<>();
//            nameValuePairs.add(new BasicNameValuePair("activityId", activityId));
//            nameValuePairs.add(new BasicNameValuePair("memberAccount", User.getUser().getUseraccount()));
//            return NetUtil.getInstance().postWithCookie(API.API_ACTIVITY_AWARD_HISTORY, nameValuePairs);
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            hideLoading();
//            if (!TextUtils.isEmpty(result)) {
//                FragmentResultDialog dialog = FragmentResultDialog.newInstance(result);
//                dialog.show(getFragmentManager(), FragmentResultDialog.class.getName());
//            }
//        }
//    }

    private void winEgg() {
        Request request = new Request();
        request.setUrl(API.API_ACTIVITY_EGG_WIN);
        request.addRequestParam("activctId", activityId);
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
                            JSONObject awardObject = object.optJSONObject("object");
                            if (awardObject == null) {
                                winNothing();
                            } else {
                                ModelAward award = new ModelAward(awardObject);
                                if (award.getType() == 1) {
                                    winMoney(award.getTypeValue());
                                } else if (award.getType() == 2) {
                                    winGoods(award.getName(), award.getImage());
                                }
                            }
                            return;
                        }
                        Notify.show(object.optString("message"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Notify.show("参与人数太多，请稍后再试。");
                }
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

//    class WinEgg extends AsyncTask<Void, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//            showLoading();
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//            List<NameValuePair> nameValuePairs = new ArrayList<>();
//            nameValuePairs.add(new BasicNameValuePair("activctId", activityId));
//            nameValuePairs.add(new BasicNameValuePair("memberAccount", User.getUser().getUseraccount()));
//            return netUtil.postWithCookie(API.API_ACTIVITY_EGG_WIN, nameValuePairs);
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            hideLoading();
//            if (!TextUtils.isEmpty(result)) {
//                try {
//                    JSONObject object = new JSONObject(result);
//                    if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
//                        JSONObject awardObject = object.optJSONObject("object");
//                        if (awardObject == null) {
//                            winNothing();
//                        } else {
//                            ModelAward award = new ModelAward(awardObject);
//                            if (award.getType() == 1) {
//                                winMoney(award.getTypeValue());
//                            } else if (award.getType() == 2) {
//                                winGoods(award.getName(), award.getImage());
//                            }
//                        }
//                        return;
//                    }
//                    Notify.show(object.optString("message"));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                Notify.show("参与人数太多，请稍后再试。");
//            }
//        }
//    }

    private void winNothing() {
        FragmentNoPriceDialog dialog = new FragmentNoPriceDialog();
        dialog.setOnDialogDismissListner(new OnDialogDismissListner() {
            @Override
            public void onDialogDismiss(boolean retry) {
                if (retry) {
                    reTry();
                } else {
                    getEggActivity();
                }
            }
        });
        dialog.show(getFragmentManager(), FragmentNoPriceDialog.class.getName());
    }

    private void winMoney(double money) {
        FragmentPriceMoneyDialog dialog = FragmentPriceMoneyDialog.newInstance("恭喜你砸中: " + Parameters.CONSTANT_RMB + decimalFormat.format(money) + "元现金");
        dialog.setOnDialogDismissListner(new OnDialogDismissListner() {
            @Override
            public void onDialogDismiss(boolean retry) {
                if (retry) {
                    reTry();
                } else {
                    getEggActivity();
                }
            }
        });
        dialog.show(getFragmentManager(), FragmentPriceMoneyDialog.class.getName());
    }

    private void winGoods(String name, String image) {
        FragmentPriceGoodsDialog dialog = FragmentPriceGoodsDialog.newInstance("恭喜你砸中: " + name, image);
        dialog.setOnDialogDismissListner(new OnDialogDismissListner() {
            @Override
            public void onDialogDismiss(boolean retry) {
                if (retry) {
                    reTry();
                } else {
                    getEggActivity();
                }
            }
        });
        dialog.show(getFragmentManager(), FragmentPriceGoodsDialog.class.getName());
    }

    class AwardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public int getItemCount() {
            return awards.size();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            AwardViewHolder holder = (AwardViewHolder) viewHolder;
            if (awards.get(position).getType() == 1) {
                holder.imageView.setImageResource(R.drawable.egg_award_money);
            } else {
                ImageLoader.getInstance().displayImage(getSmallImageUrl(awards.get(position).getImage()), holder.imageView);
            }
            holder.textView.setText(awards.get(position).getName());
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
            View view = layoutInflater.inflate(R.layout.price_good_item, null);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams((int) ((float) ScreenUtil.getScreenWidth() / 4.5f), ViewGroup.LayoutParams.MATCH_PARENT);
            view.setLayoutParams(layoutParams);
            AwardViewHolder viewHolder = new AwardViewHolder(view);
            return viewHolder;
        }

        class AwardViewHolder extends RecyclerView.ViewHolder {

            ImageView imageView;
            TextView textView;

            public AwardViewHolder(View view) {
                super(view);
                imageView = (ImageView) view.findViewById(R.id.list_award_image);
                textView = (TextView) view.findViewById(R.id.list_award_name);
            }
        }
    }

}

