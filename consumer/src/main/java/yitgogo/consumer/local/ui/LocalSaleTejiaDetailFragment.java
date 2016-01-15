package yitgogo.consumer.local.ui;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
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
import yitgogo.consumer.local.model.ModelLocalSaleTejiaDetail;
import yitgogo.consumer.product.ui.WebFragment;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.user.model.User;
import yitgogo.consumer.user.ui.UserLoginFragment;
import yitgogo.consumer.view.Notify;

public class LocalSaleTejiaDetailFragment extends BaseNotifyFragment {

    String id = "";
    ModelLocalSaleTejiaDetail localSaleTejiaDetail;

    ViewPager imagesPager;
    ImageView imageLastButton, imageNextButton;
    TextView imageIndexTextView, nameTextView, priceTextView,
            originalPriceTextView, buyButton, infoTextView;
    LinearLayout detailButton;

    ImageAdapter imageAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_local_sale_detail);
        init();
        findViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(LocalSaleTejiaDetailFragment.class.getName());
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(LocalSaleTejiaDetailFragment.class.getName());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDetail();
    }

    private void init() {
        measureScreen();
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("id")) {
                id = bundle.getString("id");
            }
        }
        localSaleTejiaDetail = new ModelLocalSaleTejiaDetail();
        imageAdapter = new ImageAdapter();
    }

    @Override
    protected void findViews() {
        imagesPager = (ViewPager) contentView
                .findViewById(R.id.local_sale_detail_images);
        imageLastButton = (ImageView) contentView
                .findViewById(R.id.local_sale_detail_image_last);
        imageNextButton = (ImageView) contentView
                .findViewById(R.id.local_sale_detail_image_next);
        imageIndexTextView = (TextView) contentView
                .findViewById(R.id.local_sale_detail_image_index);
        nameTextView = (TextView) contentView
                .findViewById(R.id.local_sale_detail_name);
        priceTextView = (TextView) contentView
                .findViewById(R.id.local_sale_detail_price);
        originalPriceTextView = (TextView) contentView
                .findViewById(R.id.local_sale_detail_price_original);
        buyButton = (TextView) contentView
                .findViewById(R.id.local_sale_detail_buy);
        detailButton = (LinearLayout) contentView
                .findViewById(R.id.local_sale_detail);
        infoTextView = (TextView) contentView
                .findViewById(R.id.local_sale_detail_info);
        initViews();
        registerViews();
    }

    @SuppressLint("NewApi")
    @Override
    protected void initViews() {
        LayoutParams layoutParams = new LayoutParams(screenWidth, screenWidth);
        imagesPager.setLayoutParams(layoutParams);
        imagesPager.setAdapter(imageAdapter);
    }

    @Override
    protected void registerViews() {
        buyButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (User.getUser().isLogin()) {
                    if (localSaleTejiaDetail != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString("object", localSaleTejiaDetail
                                .getJsonObject().toString());
                        jump(LocalGoodsSaleTejiaBuyFragment.class.getName(),
                                "确认订单", bundle);
                    }
                } else {
                    Toast.makeText(getActivity(), "请先登录", Toast.LENGTH_SHORT)
                            .show();
                    jump(UserLoginFragment.class.getName(), "会员登录");
                    return;
                }
            }
        });
        detailButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("html",
                        localSaleTejiaDetail.getProductDetais());
                bundle.putInt("type", WebFragment.TYPE_HTML);
                jump(WebFragment.class.getName(),
                        localSaleTejiaDetail.getProductName(), bundle);
            }
        });
        imageLastButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageAdapter.getCount() > 0) {
                    if (imagesPager.getCurrentItem() == 0) {
                        imagesPager.setCurrentItem(imageAdapter.getCount() - 1, true);
                    } else {
                        imagesPager.setCurrentItem(imagesPager.getCurrentItem() - 1, true);
                    }
                }
            }
        });
        imageNextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageAdapter.getCount() > 0) {
                    if (imagesPager.getCurrentItem() == imageAdapter.getCount() - 1) {
                        imagesPager.setCurrentItem(0, true);
                    } else {
                        imagesPager.setCurrentItem(imagesPager.getCurrentItem() + 1, true);
                    }
                }
            }
        });
    }

    private void showDetail() {
        if (localSaleTejiaDetail != null) {
            imageAdapter.notifyDataSetChanged();
            nameTextView.setText(localSaleTejiaDetail.getProductName());
            priceTextView
                    .setText(Parameters.CONSTANT_RMB
                            + decimalFormat.format(localSaleTejiaDetail
                            .getSalePrice()));
            originalPriceTextView.setText("原价:" + Parameters.CONSTANT_RMB
                    + decimalFormat.format(localSaleTejiaDetail.getPrice()));
            infoTextView.setText("*"
                    + localSaleTejiaDetail.getSalePromotionName());
        }
    }

    private void getDetail() {
        Request request = new Request();
        request.setUrl(API.API_LOCAL_SALE_TEJIA_DETAIL);
        request.addRequestParam("id", id);
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                Notify.show(missionMessage.getMessage());

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            localSaleTejiaDetail = new ModelLocalSaleTejiaDetail(
                                    object.optJSONObject("dataMap"));
                            showDetail();
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

    class ImageAdapter extends PagerAdapter {

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return localSaleTejiaDetail.getImages().size();
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            View imageLayout = layoutInflater.inflate(
                    R.layout.adapter_viewpager, view, false);
            assert imageLayout != null;
            ImageView imageView = (ImageView) imageLayout
                    .findViewById(R.id.view_pager_img);
            final ProgressBar spinner = (ProgressBar) imageLayout
                    .findViewById(R.id.view_pager_loading);
            TextView indexTextView = (TextView) imageLayout
                    .findViewById(R.id.view_pager_index);
            indexTextView.setText((position + 1) + "/"
                    + localSaleTejiaDetail.getImages().size());
            ImageLoader.getInstance().displayImage(
                    getBigImageUrl(localSaleTejiaDetail.getImages()
                            .get(position).getImgName()), imageView,
                    new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            spinner.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view,
                                                    FailReason failReason) {
                            spinner.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri,
                                                      View view, Bitmap loadedImage) {
                            spinner.setVisibility(View.GONE);
                        }
                    });
            view.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

}
