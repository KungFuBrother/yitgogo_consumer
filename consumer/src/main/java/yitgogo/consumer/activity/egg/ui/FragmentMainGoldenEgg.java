package yitgogo.consumer.activity.egg.ui;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartown.yitian.gogo.R;

import yitgogo.consumer.BaseNotifyFragment;
import yitgogo.consumer.activity.egg.ui.adapter.GoldenPriceGoodsAdapter;
import yitgogo.consumer.view.InnerGridView;

public class FragmentMainGoldenEgg extends BaseNotifyFragment {


    private RelativeLayout mainLayout;
    private TextView mPayMoneyTv;
    private InnerGridView gridView;
    private ImageView egg;
    private GoldenPriceGoodsAdapter mGoodsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main_golden_egg);
        measureScreen();
        initView();
    }

    private void initView() {
        gridView = (InnerGridView) contentView.findViewById(R.id.gridview);
        mGoodsAdapter = new GoldenPriceGoodsAdapter(getActivity());
        gridView.setAdapter(mGoodsAdapter);
        int allWidth = screenWidth - 10;
        int itemWidth = screenWidth / 4;
        LinearLayout.LayoutParams gridviewParams = new LinearLayout.LayoutParams(allWidth, LinearLayout.LayoutParams.MATCH_PARENT);
        gridView.setLayoutParams(gridviewParams);
        gridView.setColumnWidth(itemWidth);
        gridView.setNumColumns(5);
        gridView.setStretchMode(GridView.NO_STRETCH);

//        egg.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                startEgg();
//            }
//        });

        mainLayout = (RelativeLayout) contentView.findViewById(R.id.fragment_main_top_layout);
        mPayMoneyTv = (TextView) contentView.findViewById(R.id.fragment_main_pay_money_tv);

        LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) mainLayout.getLayoutParams();
        params.height = screenHeight / 2;
        mainLayout.setLayoutParams(params);

        mPayMoneyTv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // 支付页面
                payMoneyAfter();
            }
        });

        // mPayMoneyTv.getViewTreeObserver().addOnGlobalLayoutListener(new
        // OnGlobalLayoutListener() {
        //
        // @Override
        // public void onGlobalLayout() {
        // mPayMoneyTv.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        // mPayMoreTvHeight = mPayMoneyTv.getHeight();
        //
        // LinearLayout.LayoutParams params =
        // (android.widget.LinearLayout.LayoutParams)
        // mPayMoneyTv.getLayoutParams();
        // params.setMargins(
        // screenWidth / 3,
        // screenHeight / 2 - mPayMoreTvHeight*2,
        // screenWidth / 3,
        // screenHeight / 2 + mPayMoreTvHeight);
        // mPayMoneyTv.setLayoutParams(params);
        //
        // // mPayMoneyTv.layout(
        // // screenWidth / 3,
        // // screenHeight / 2 - mPayMoreTvHeight,
        // // screenWidth / 3,
        // // screenHeight / 2);
        // }
        // });
    }

    public void payMoneyAfter() {

        mainLayout.setBackgroundResource(R.drawable.after_top_bg);
        mPayMoneyTv.setVisibility(View.INVISIBLE);
    }

    public void payMoneyBefore() {

        mainLayout.setBackgroundResource(R.drawable.before_top_bg);
        mPayMoneyTv.setVisibility(View.VISIBLE);
    }

    private AnimationDrawable drawable;

    private void startEgg() {

        if (drawable == null) {
            egg.setBackgroundResource(R.drawable.loading);
            drawable = (AnimationDrawable) egg.getBackground();
        }
        drawable.start();

        int duration = 0;

        for (int i = 0; i < drawable.getNumberOfFrames(); i++) {
            duration += drawable.getDuration(i);
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                egg.setVisibility(View.GONE);

            }
        }, duration + 300);
    }

}
