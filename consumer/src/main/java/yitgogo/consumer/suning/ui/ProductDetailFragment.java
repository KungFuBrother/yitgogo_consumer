package yitgogo.consumer.suning.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.product.ui.WebFragment;
import yitgogo.consumer.suning.model.ModelProductDetail;
import yitgogo.consumer.suning.model.ModelProductImage;
import yitgogo.consumer.suning.model.ModelProductPrice;
import yitgogo.consumer.suning.model.SuningCarController;
import yitgogo.consumer.suning.model.SuningManager;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.user.model.User;
import yitgogo.consumer.user.ui.UserLoginFragment;
import yitgogo.consumer.view.Notify;

public class ProductDetailFragment extends BaseNotifyFragment {

    ViewPager imagePager;
    LinearLayout htmlButton;
    TextView nameTextView, brandTextView, modelTextView, stateTextView, priceTextView, serviceTextView;
    ImageView lastImageButton, nextImageButton;
    TextView imageIndexText;
    TextView carButton, buyButton;

    ImageAdapter imageAdapter;

    ModelProductDetail product = new ModelProductDetail();
    List<ModelProductImage> productImages = new ArrayList<>();
    ModelProductPrice productPrice = new ModelProductPrice();

    Bundle bundle = new Bundle();
    String skuId = "";

    String state = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_product_suning_detail);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(ProductDetailFragment.class.getName());
        if (SuningCarController.containProduct(product.getSku())) {
            carButton.setBackgroundResource(android.R.color.darker_gray);
        } else {
            carButton.setBackgroundResource(R.drawable.button_rec_blue);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(ProductDetailFragment.class.getName());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getProductDetail();
    }

    private void init() {
        measureScreen();
        bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("skuId")) {
                skuId = bundle.getString("skuId");
            }
        }
        imageAdapter = new ImageAdapter();
    }

    protected void findViews() {
        imagePager = (ViewPager) contentView
                .findViewById(R.id.product_detail_images);
        lastImageButton = (ImageView) contentView
                .findViewById(R.id.product_detail_image_last);
        nextImageButton = (ImageView) contentView
                .findViewById(R.id.product_detail_image_next);
        imageIndexText = (TextView) contentView
                .findViewById(R.id.product_detail_image_index);

        nameTextView = (TextView) contentView
                .findViewById(R.id.product_detail_name);
        brandTextView = (TextView) contentView
                .findViewById(R.id.product_detail_brand);
        modelTextView = (TextView) contentView
                .findViewById(R.id.product_detail_model);
        stateTextView = (TextView) contentView
                .findViewById(R.id.product_detail_state);
        htmlButton = (LinearLayout) contentView
                .findViewById(R.id.product_detail_html);
        serviceTextView = (TextView) contentView
                .findViewById(R.id.product_detail_service);

        priceTextView = (TextView) contentView
                .findViewById(R.id.product_detail_price);
        carButton = (TextView) contentView
                .findViewById(R.id.product_detail_car);
        buyButton = (TextView) contentView
                .findViewById(R.id.product_detail_buy);

        addImageButton(R.drawable.iconfont_cart, "购物车", new OnClickListener() {

            @Override
            public void onClick(View v) {
                jump(ShoppingCarFragment.class.getName(), "云商城购物车");
            }
        });
        initViews();
        registerViews();
    }

    protected void initViews() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(screenWidth, screenWidth);
        imagePager.setLayoutParams(layoutParams);
        imagePager.setAdapter(imageAdapter);
        imageIndexText.setText("1/" + imageAdapter.getCount());
    }

    private void showDetail() {
        nameTextView.setText(product.getName());
        brandTextView.setText(product.getBrand());
        modelTextView.setText(product.getModel());
        serviceTextView.setText(Html.fromHtml(product.getService()));
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void registerViews() {
        htmlButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("html", product.getIntroduction());
                bundle.putInt("type", WebFragment.TYPE_HTML);
                jump(WebFragment.class.getName(), product.getName(), bundle);
            }
        });
        lastImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageAdapter.getCount() > 0) {
                    if (imagePager.getCurrentItem() == 0) {
                        setImagePosition(imageAdapter.getCount() - 1);
                    } else {
                        setImagePosition(imagePager.getCurrentItem() - 1);
                    }
                }
            }
        });
        nextImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageAdapter.getCount() > 0) {
                    if (imagePager.getCurrentItem() == imageAdapter.getCount() - 1) {
                        setImagePosition(0);
                    } else {
                        setImagePosition(imagePager.getCurrentItem() + 1);
                    }
                }
            }
        });
        imagePager.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                imageIndexText.setText((imagePager.getCurrentItem() + 1) + "/"
                        + imageAdapter.getCount());
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
        carButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                addToCar();
            }
        });
        buyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state.equals("00")) {
                    if (productPrice.getPrice() > 0) {
                        if (User.getUser().isLogin()) {
                            Bundle bundle = new Bundle();
                            bundle.putString("product", product.getJsonObject().toString());
                            jump(SuningProductBuyFragment.class.getName(), "确认订单", bundle);
                        } else {
                            Toast.makeText(getActivity(), "请先登录", Toast.LENGTH_SHORT).show();
                            jump(UserLoginFragment.class.getName(), "会员登录");
                        }
                    } else {
                        Notify.show("商品信息有误，不能购买");
                    }
                } else {
                    Notify.show("此商品暂不能购买");
                }
            }
        });
    }

    /**
     * 添加到购物车
     */
    private void addToCar() {
        if (state.equals("00")) {
            if (productPrice.getPrice() > 0) {
                if (SuningCarController.addProduct(product)) {
                    Notify.show("添加到购物车成功");
                    carButton.setBackgroundResource(android.R.color.darker_gray);
                } else {
                    Notify.show("已添加过此商品");
                }
            } else {
                Notify.show("商品信息有误，不能购买");
            }
        } else {
            Notify.show("此商品暂不能购买");
        }
    }

    /**
     * 点击左右导航按钮切换图片
     *
     * @param imagePosition
     */
    private void setImagePosition(int imagePosition) {
        imagePager.setCurrentItem(imagePosition, true);
        imageIndexText.setText((imagePosition + 1) + "/" + imageAdapter.getCount());
    }

    private void getProductDetail() {
        Request request = new Request();
        request.setUrl(API.API_SUNING_PRODUCT_DETAIL);
        JSONObject data = new JSONObject();
        try {
            data.put("accessToken", SuningManager.getSignature().getToken());
            data.put("appKey", SuningManager.appKey);
            data.put("v", SuningManager.version);
            data.put("sku", skuId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        request.addRequestParam("data", data.toString());
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
                            if (SuningManager.isSignatureOutOfDate(requestMessage.getResult())) {
                                SuningManager.getNewSignature(getActivity(), new RequestListener() {
                                    @Override
                                    protected void onStart() {

                                    }

                                    @Override
                                    protected void onFail(MissionMessage missionMessage) {

                                    }

                                    @Override
                                    protected void onSuccess(RequestMessage requestMessage) {
                                        if (SuningManager.initSignature(requestMessage)) {
                                            getProductDetail();
                                        }
                                    }

                                    @Override
                                    protected void onFinish() {

                                    }
                                });
                                return;
                            }
                            try {
                                JSONObject object = new JSONObject(requestMessage.getResult());
                                if (object.optBoolean("isSuccess")) {
                                    product = new ModelProductDetail(object);
                                    showDetail();
                                    getProductImage();
                                    getProductPrice();
                                    getProductStock();
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
                }

        );
    }

    private void getProductImage() {
        Request request = new Request();
        request.setUrl(API.API_SUNING_PRODUCT_IMAGES);
        JSONArray dataArray = new JSONArray();
        dataArray.put(product.getSku());
        JSONObject data = new JSONObject();
        try {
            data.put("accessToken", SuningManager.getSignature().getToken());
            data.put("appKey", SuningManager.appKey);
            data.put("v", SuningManager.version);
            data.put("sku", dataArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        request.addRequestParam("data", data.toString());
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
                    if (SuningManager.isSignatureOutOfDate(requestMessage.getResult())) {
                        SuningManager.getNewSignature(getActivity(), new RequestListener() {
                            @Override
                            protected void onStart() {

                            }

                            @Override
                            protected void onFail(MissionMessage missionMessage) {

                            }

                            @Override
                            protected void onSuccess(RequestMessage requestMessage) {
                                if (SuningManager.initSignature(requestMessage)) {
                                    getProductImage();
                                }
                            }

                            @Override
                            protected void onFinish() {

                            }
                        });
                        return;
                    }
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optBoolean("isSuccess")) {
                            JSONArray array = object.optJSONArray("result");
                            if (array != null) {
                                if (array.length() > 0) {
                                    JSONObject imageObject = array.optJSONObject(0);
                                    if (imageObject != null) {
                                        JSONArray imageArray = imageObject.optJSONArray("urls");
                                        if (imageArray != null) {
                                            for (int i = 0; i < imageArray.length(); i++) {
                                                productImages.add(new ModelProductImage(imageArray.optJSONObject(i)));
                                            }
                                            imageAdapter.notifyDataSetChanged();
                                            imageIndexText.setText("1/" + imageAdapter.getCount());
                                        }
                                    }
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

    private void getProductPrice() {
        Request request = new Request();
        request.setUrl(API.API_SUNING_PRODUCT_PRICE);

        JSONArray skuJsonArray = new JSONArray();
        skuJsonArray.put(product.getSku());

        JSONObject data = new JSONObject();
        try {
            data.put("accessToken", SuningManager.getSignature().getToken());
            data.put("appKey", SuningManager.appKey);
            data.put("v", SuningManager.version);
            data.put("cityId", SuningManager.getSuningAreas().getCity().getCode());
            data.put("sku", skuJsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        request.addRequestParam("data", data.toString());

        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                Notify.show("查询价格失败");
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    if (SuningManager.isSignatureOutOfDate(requestMessage.getResult())) {
                        SuningManager.getNewSignature(getActivity(), new RequestListener() {
                            @Override
                            protected void onStart() {

                            }

                            @Override
                            protected void onFail(MissionMessage missionMessage) {

                            }

                            @Override
                            protected void onSuccess(RequestMessage requestMessage) {
                                if (SuningManager.initSignature(requestMessage)) {
                                    getProductStock();
                                }
                            }

                            @Override
                            protected void onFinish() {

                            }
                        });
                        return;
                    }
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optBoolean("isSuccess")) {
                            JSONArray array = object.optJSONArray("result");
                            if (array != null) {
                                if (array.length() > 0) {
                                    productPrice = new ModelProductPrice(array.optJSONObject(0));
                                    priceTextView.setText(Parameters.CONSTANT_RMB + decimalFormat.format(productPrice.getPrice()));
                                    return;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Notify.show("查询价格失败");
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

    private void getProductStock() {
        Request request = new Request();
        request.setUrl(API.API_SUNING_PRODUCT_STOCK);
        JSONObject data = new JSONObject();
        try {
            data.put("accessToken", SuningManager.getSignature().getToken());
            data.put("appKey", SuningManager.appKey);
            data.put("v", SuningManager.version);
            data.put("cityId", SuningManager.getSuningAreas().getCity().getCode());
            data.put("countyId", SuningManager.getSuningAreas().getDistrict().getCode());
            data.put("sku", product.getSku());
            data.put("num", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        request.addRequestParam("data", data.toString());

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
                    if (SuningManager.isSignatureOutOfDate(requestMessage.getResult())) {
                        SuningManager.getNewSignature(getActivity(), new RequestListener() {
                            @Override
                            protected void onStart() {

                            }

                            @Override
                            protected void onFail(MissionMessage missionMessage) {

                            }

                            @Override
                            protected void onSuccess(RequestMessage requestMessage) {
                                if (SuningManager.initSignature(requestMessage)) {
                                    getProductStock();
                                }
                            }

                            @Override
                            protected void onFinish() {

                            }
                        });
                        return;
                    }
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optBoolean("isSuccess")) {
                            state = object.optString("state");
                            if (state.equals("00")) {
                                stateTextView.setText("有货");
                            } else if (state.equals("01")) {
                                stateTextView.setText("暂不销售");
                            } else {
                                stateTextView.setText("无货");
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

    /**
     * viewpager适配器
     */
    private class ImageAdapter extends PagerAdapter {

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return productImages.size();
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
            ImageLoader.getInstance().displayImage(productImages.get(position).getPath(),
                    imageView, new SimpleImageLoadingListener() {
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
