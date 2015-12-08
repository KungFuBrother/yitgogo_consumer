package yitgogo.consumer.activity.egg.ui;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartown.yitian.gogo.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import yitgogo.consumer.BaseNotifyFragment;
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

    ImageView headerImageView;

    FrameLayout clickLayout;

    ImageView animClickImageView, animImageView;

    Button payButton;

    RecyclerView awardRecyclerView;

    TextView ruleTextView;

    private SoundPool soundPool;
    private SparseIntArray soundIds;
    private int eggMusic = 1;

    ModelActivity activityetail = new ModelActivity();
    List<ModelAward> awards;
    AwardAdapter awardAdapter;

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
        new GetEggActivity().execute();

        new GetEggHistory().execute();
    }

    private void init() {
        awards = new ArrayList<>();
        awardAdapter = new AwardAdapter();
        initSoundPool();
    }

    @Override
    protected void findViews() {
        headerImageView = (ImageView) contentView.findViewById(R.id.egg_header);

        clickLayout = (FrameLayout) contentView.findViewById(R.id.egg_click_layout);
        animClickImageView = (ImageView) contentView.findViewById(R.id.egg_click);
        animImageView = (ImageView) contentView.findViewById(R.id.egg_anim);

        payButton = (Button) contentView.findViewById(R.id.egg_pay);
        awardRecyclerView = (RecyclerView) contentView.findViewById(R.id.egg_award);
        ruleTextView = (TextView) contentView.findViewById(R.id.egg_rule);

        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        FrameLayout.LayoutParams headerLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (int) ((float) ScreenUtil.getScreenWidth() / 540.0f * 453.0f));
        headerImageView.setLayoutParams(headerLayoutParams);

        LinearLayout.LayoutParams animLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) ((float) ScreenUtil.getScreenWidth() / 540.0f * 453.0f));
        animImageView.setLayoutParams(animLayoutParams);

        FrameLayout.LayoutParams clickLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (int) ((float) ScreenUtil.getScreenWidth() / 540.0f * 350.0f));
        clickLayout.setLayoutParams(clickLayoutParams);
        FrameLayout.LayoutParams clickImageLayoutParams = new FrameLayout.LayoutParams((int) ((float) ScreenUtil.getScreenWidth() * 197.0f / 540.0f), (int) ((float) ScreenUtil.getScreenWidth() * 205.0f / 540.0f));
        clickImageLayoutParams.gravity = Gravity.CENTER;
        animClickImageView.setLayoutParams(clickImageLayoutParams);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) ((float) ScreenUtil.getScreenWidth() / 4.0f));
        awardRecyclerView.setLayoutParams(layoutParams);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        awardRecyclerView.setLayoutManager(linearLayoutManager);
        awardRecyclerView.setAdapter(awardAdapter);
    }

    int position = 0;
    int[] eggImages = {R.drawable.egg_anim_2, R.drawable.egg_anim_3, R.drawable.egg_anim_4, R.drawable.egg_anim_5, R.drawable.egg_anim_6, R.drawable.egg_anim_7, R.drawable.egg_anim_8, R.drawable.egg_anim_9};

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (position < eggImages.length) {
                animImageView.setImageResource(eggImages[position]);
                position++;
                handler.sendEmptyMessageDelayed(0, 200);
            } else {
                animClickImageView.setVisibility(View.VISIBLE);
            }
        }
    };

    private void animate() {
        animImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (position == 4) {
                    playSound();
                }
                if (position < eggImages.length) {
                    animImageView.setImageResource(eggImages[position]);
                    position++;
                    animate();
                } else {
                    new WinEgg().execute();
                }
            }
        }, 10);
    }

    @Override
    protected void registerViews() {
        animClickImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animClickImageView.setVisibility(View.GONE);
                animImageView.setVisibility(View.VISIBLE);
                headerImageView.setImageResource(R.drawable.egg_back_2);
                position = 0;
                showLoading();
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

    class GetEggActivity extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoading();
            awards.clear();
            awardAdapter.notifyDataSetChanged();
        }

        @Override
        protected String doInBackground(Void... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("id", "16"));
            nameValuePairs.add(new BasicNameValuePair("memberAccount", User.getUser().getUseraccount()));
            return netUtil.postWithCookie(API.API_ACTIVITY_EGG_DETAIL, nameValuePairs);
        }

        @Override
        protected void onPostExecute(String result) {
            hideLoading();
            if (!TextUtils.isEmpty(result)) {
                try {
                    JSONObject object = new JSONObject(result);
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
            }
        }
    }

    class Retry extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoading();
        }

        @Override
        protected String doInBackground(Void... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("id", "16"));
            nameValuePairs.add(new BasicNameValuePair("memberAccount", User.getUser().getUseraccount()));
            return netUtil.postWithCookie(API.API_ACTIVITY_EGG_DETAIL, nameValuePairs);
        }

        @Override
        protected void onPostExecute(String result) {
            hideLoading();
            if (!TextUtils.isEmpty(result)) {
                try {
                    JSONObject object = new JSONObject(result);
                    if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                        JSONArray awardArray = object.optJSONArray("dataList");
                        if (awardArray != null) {
                            for (int i = 0; i < awardArray.length(); i++) {
                                awards.add(new ModelAward(awardArray.optJSONObject(i)));
                            }
                        }
                        ModelActivity activity = new ModelActivity(object.optJSONObject("object"));
                        if (activity.getType() == 1) {
                            payMoney();
                        } else {
                            activityetail = activity;
                            showActivityDetail();
                        }
                        return;
                    }
                    Notify.show(object.optString("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class GetEggHistory extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoading();
        }

        @Override
        protected String doInBackground(Void... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("activityId", "16"));
            nameValuePairs.add(new BasicNameValuePair("memberAccount", User.getUser().getUseraccount()));
            return netUtil.postWithCookie(API.API_ACTIVITY_AWARD_HISTORY, nameValuePairs);
        }

        @Override
        protected void onPostExecute(String result) {
            hideLoading();
            if (!TextUtils.isEmpty(result)) {
                try {
                    JSONObject object = new JSONObject(result);
                    if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                        return;
                    }
                    Notify.show(object.optString("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class WinEgg extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoading();
        }

        @Override
        protected String doInBackground(Void... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("activctId", "16"));
            nameValuePairs.add(new BasicNameValuePair("memberAccount", User.getUser().getUseraccount()));
            return netUtil.postWithCookie(API.API_ACTIVITY_EGG_WIN, nameValuePairs);
        }

        @Override
        protected void onPostExecute(String result) {
            hideLoading();
            if (!TextUtils.isEmpty(result)) {
                try {
                    JSONObject object = new JSONObject(result);
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
            }
        }
    }

    private void winNothing() {
        FragmentNoPriceDialog dialog = new FragmentNoPriceDialog();
        dialog.setOnDialogDismissListner(new OnDialogDismissListner() {
            @Override
            public void onDialogDismiss(boolean retry) {
                if (retry) {
                    new Retry().execute();
                } else {
                    new GetEggActivity().execute();
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
                    new Retry().execute();
                } else {
                    new GetEggActivity().execute();
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
                    new Retry().execute();
                } else {
                    new GetEggActivity().execute();
                }
            }
        });
        dialog.show(getFragmentManager(), FragmentPriceGoodsDialog.class.getName());
    }

    private void showActivityDetail() {
        //初始状态
        position = 0;
        animClickImageView.setVisibility(View.VISIBLE);
        animImageView.setVisibility(View.INVISIBLE);
        headerImageView.setImageResource(R.drawable.egg_back_1);

        ruleTextView.setText(activityetail.getRule());
        awardAdapter.notifyDataSetChanged();
        switch (activityetail.getJoinState()) {
            case 0:
                //能参与活动
                animClickImageView.setEnabled(true);
                payButton.setVisibility(View.GONE);
                break;
            case 1:
                //未付款
                animClickImageView.setEnabled(false);
                payButton.setVisibility(View.VISIBLE);
                payButton.setBackgroundResource(R.drawable.shape_pay_money_btn_bg);
                payButton.setText((int) activityetail.getJoinMoney() + "元砸惊喜");
                break;
            case 2:
                //活动结束
                animClickImageView.setEnabled(false);
                payButton.setVisibility(View.VISIBLE);
                payButton.setBackgroundResource(R.drawable.shape_pay_money_btn_disable);
                payButton.setText("已结束");
                break;
            case 3:
                animClickImageView.setEnabled(false);
                payButton.setVisibility(View.VISIBLE);
                payButton.setBackgroundResource(R.drawable.shape_pay_money_btn_disable);
                payButton.setText("未开始");
                break;
            case 4:
                //参与次数上限
                animClickImageView.setEnabled(false);
                payButton.setVisibility(View.VISIBLE);
                payButton.setBackgroundResource(R.drawable.shape_pay_money_btn_disable);
                payButton.setText("参与次数已达上限");
                break;
        }
    }

    class AwardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        class AwardViewHolder extends RecyclerView.ViewHolder {

            ImageView imageView;
            TextView textView;

            public AwardViewHolder(View view) {
                super(view);
                imageView = (ImageView) view.findViewById(R.id.list_award_image);
                textView = (TextView) view.findViewById(R.id.list_award_name);
            }
        }

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
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams((int) ((float) ScreenUtil.getScreenWidth() / 5.0f), ViewGroup.LayoutParams.MATCH_PARENT);
            view.setLayoutParams(layoutParams);
            AwardViewHolder viewHolder = new AwardViewHolder(view);
            return viewHolder;
        }
    }

//    @Override
//    protected void registerViews() {
//        animClickImageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startEgg();
//            }
//        });
//    }
//
//    private AnimationDrawable drawable;
//
//    private void startEgg() {
//
//        if (drawable == null) {
//            animImageView.setImageResource(R.drawable.loading);
//            drawable = (AnimationDrawable) animImageView.getDrawable();
//        }
//        drawable.start();
//
//        int duration = 0;
//
//        for (int i = 0; i < drawable.getNumberOfFrames(); i++) {
//            duration += drawable.getDuration(i);
//        }
//
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//                stopEgg();
//            }
//        }, duration + 300);
//    }
//
//    private void stopEgg() {
//        drawable.stop();
//    }

}

