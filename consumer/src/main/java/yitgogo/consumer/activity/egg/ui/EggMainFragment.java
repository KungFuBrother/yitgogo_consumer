package yitgogo.consumer.activity.egg.ui;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.smartown.yitian.gogo.R;

import yitgogo.consumer.BaseNotifyFragment;
import yitgogo.consumer.tools.ScreenUtil;

/**
 * Created by Tiger on 2015-12-07.
 */
public class EggMainFragment extends BaseNotifyFragment {

    ImageView headerImageView;

    FrameLayout animLayout;

    ImageView animClickImageView, animImageView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main_egg);
        findViews();
    }

    @Override
    protected void findViews() {
        headerImageView = (ImageView) contentView.findViewById(R.id.egg_header);
        animLayout = (FrameLayout) contentView.findViewById(R.id.egg_anim_layout);
        animClickImageView = (ImageView) contentView.findViewById(R.id.egg_click);
        animImageView = (ImageView) contentView.findViewById(R.id.egg_anim);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        FrameLayout.LayoutParams headerLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (int) ((float) ScreenUtil.getScreenWidth() / 1080.0f * 960.0f));
        headerImageView.setLayoutParams(headerLayoutParams);

        LinearLayout.LayoutParams animLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) ((float) ScreenUtil.getScreenWidth() / 1080.0f * 700.0f));
        animLayout.setLayoutParams(animLayoutParams);
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
//            animImageView.setBackgroundResource(R.drawable.loading);
//            drawable = (AnimationDrawable) animImageView.getBackground();
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

    int position = 0;
    int[] eggImages = {R.drawable.egg1, R.drawable.egg2, R.drawable.egg3, R.drawable.egg4, R.drawable.egg5, R.drawable.egg6};

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (position < eggImages.length) {
                animImageView.setImageResource(eggImages[position]);
                position++;
                handler.sendEmptyMessageDelayed(0, 10);
            }else {
                animClickImageView.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    protected void registerViews() {
        animClickImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animClickImageView.setVisibility(View.GONE);
                position = 0;
                handler.sendEmptyMessage(0);
            }
        });
    }

}

