package yitgogo.consumer.main.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dtr.zxing.activity.CaptureActivity;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.activity.egg.ui.EggMainFragment;
import yitgogo.consumer.home.model.ModelListPrice;
import yitgogo.consumer.home.model.ModelProduct;
import yitgogo.consumer.home.part.PartAdsFragment;
import yitgogo.consumer.home.part.PartBianminFragment;
import yitgogo.consumer.home.part.PartBrandFragment;
import yitgogo.consumer.home.part.PartFreshFragment;
import yitgogo.consumer.home.part.PartLocalBusinessFragment;
import yitgogo.consumer.home.part.PartMiaoshaFragment;
import yitgogo.consumer.home.part.PartSaleTimeFragment;
import yitgogo.consumer.home.part.PartScoreFragment;
import yitgogo.consumer.home.part.PartStoreFragment;
import yitgogo.consumer.home.part.PartTejiaFragment;
import yitgogo.consumer.home.part.PartThemeFragment;
import yitgogo.consumer.local.ui.NongfuFragment;
import yitgogo.consumer.product.ui.ClassesFragment;
import yitgogo.consumer.product.ui.ProductSearchFragment;
import yitgogo.consumer.store.model.ModelStoreArea;
import yitgogo.consumer.store.model.Store;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Content;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.tools.ScreenUtil;
import yitgogo.consumer.user.model.User;
import yitgogo.consumer.user.ui.UserLoginFragment;
import yitgogo.consumer.view.InnerGridView;
import yitgogo.consumer.view.Notify;

public class HomeFragment extends BaseNotifyFragment implements OnClickListener {

    PullToRefreshScrollView refreshScrollView;
    InnerGridView productGridView;
    ImageView classButton, scanButton, nongfuButton;
    TextView searchTextView;

    ImageView bannerEggImageView;

    List<ModelProduct> products;
    HashMap<String, ModelListPrice> priceMap;
    ProductAdapter productAdapter;

    String currentStoreId = "";
    boolean isAlive = false;
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0x12:
                    PartTejiaFragment.getTejiaFragment().initViews();
                    if (isAlive) {
                        handler.sendEmptyMessageDelayed(0x12, 10000);
                    }
                    break;

                default:
                    break;
            }
        }

    };
//    BroadcastReceiver broadcastReceiver;

    private void runAnimateThread() {
        isAlive = true;
        handler.sendEmptyMessageDelayed(0x12, 10000);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home_main);
        init();
        findViews();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        initReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(HomeFragment.class.getName());
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(HomeFragment.class.getName());
        if (!currentStoreId.equals(Store.getStore().getStoreId())) {
            currentStoreId = Store.getStore().getStoreId();
            refresh();
        }
    }

    @Override
    public void onDestroy() {
        isAlive = false;
//        if (broadcastReceiver != null) {
//            getActivity().unregisterReceiver(broadcastReceiver);
//        }
        super.onDestroy();
    }

    private void init() {
        measureScreen();
        products = new ArrayList<>();
        priceMap = new HashMap<>();
        productAdapter = new ProductAdapter();
        getStoreAreas();
    }

//    private void initReceiver() {
//        if (System.currentTimeMillis() > (long) 1449935999 * 1000) {
//            bannerEggImageView.setVisibility(View.GONE);
//            return;
//        }
//        broadcastReceiver = new BroadcastReceiver() {
//
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
//                    if (System.currentTimeMillis() > (long) 1449935999 * 1000) {
//                        bannerEggImageView.setVisibility(View.GONE);
//                    }
//                }
//            }
//        };
//        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
//        getActivity().registerReceiver(broadcastReceiver, intentFilter);
//    }

    protected void findViews() {
        refreshScrollView = (PullToRefreshScrollView) contentView
                .findViewById(R.id.home_scroll);
        productGridView = (InnerGridView) contentView
                .findViewById(R.id.home_product_list);
        classButton = (ImageView) contentView
                .findViewById(R.id.home_title_class);
        scanButton = (ImageView) contentView.findViewById(R.id.home_title_scan);
        nongfuButton = (ImageView) contentView
                .findViewById(R.id.home_part_nongfu);
        searchTextView = (TextView) contentView
                .findViewById(R.id.home_title_edit);

        bannerEggImageView = (ImageView) contentView.findViewById(R.id.home_banner_egg);

        initViews();
        registerViews();
    }

    protected void initViews() {
        handler.sendEmptyMessage(1);
        refreshScrollView.setMode(Mode.BOTH);
        productGridView.setAdapter(productAdapter);

        LinearLayout.LayoutParams bannerLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) ((float) screenWidth / 6.0f));
        bannerLayoutParams.setMargins(0, 0, 0, ScreenUtil.dip2px(8));
        bannerEggImageView.setLayoutParams(bannerLayoutParams);
        bannerEggImageView.setImageResource(R.drawable.image_home_banner_egg);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) ((float) screenWidth / 3.0f));
        layoutParams.setMargins(0, 0, 0, ScreenUtil.dip2px(8));
        nongfuButton.setLayoutParams(layoutParams);

        getFragmentManager().beginTransaction()
                .replace(R.id.home_part_ads_layout,
                        PartAdsFragment.getAdsFragment())
                .replace(R.id.home_part_activity_layout,
                        PartBianminFragment.getBianminFragment())
                .replace(R.id.home_part_miaosha_layout,
                        PartMiaoshaFragment.getMiaoshaFragment())
                .replace(R.id.home_part_fresh_layout,
                        PartFreshFragment.getFreshFragment())
                .replace(R.id.home_part_score_layout,
                        PartScoreFragment.getScoreFragment())
                .replace(R.id.home_part_sale_time_layout,
                        PartSaleTimeFragment.getSaleTimeFragment())
                .replace(R.id.home_part_store_layout,
                        PartStoreFragment.getStoreFragment())
                .replace(R.id.home_part_theme_layout,
                        PartThemeFragment.getThemeFragment())
                .replace(R.id.home_part_tejia_layout,
                        PartTejiaFragment.getTejiaFragment())
                .replace(R.id.home_part_local_layout,
                        PartLocalBusinessFragment.getLocalBusinessFragment())
                .replace(R.id.home_part_brand_layout,
                        PartBrandFragment.getBrandFragment()).commit();
        handler.sendEmptyMessageDelayed(0x13, 5000);

        if (System.currentTimeMillis() > (long) 1449935999 * 1000) {
            bannerEggImageView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void registerViews() {
        refreshScrollView
                .setOnRefreshListener(new OnRefreshListener2<ScrollView>() {

                    @Override
                    public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                        refresh();
                    }

                    @Override
                    public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                        getProduct();
                    }
                });
        productGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                showProductDetail(products.get(arg2).getId(), products.get(arg2).getProductName(), CaptureActivity.SALE_TYPE_NONE);
            }
        });
        classButton.setOnClickListener(this);
        scanButton.setOnClickListener(this);
        searchTextView.setOnClickListener(this);
        nongfuButton.setOnClickListener(this);
        bannerEggImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (User.getUser().isLogin()) {
                    jumpFull(EggMainFragment.class.getName(), "砸金蛋", null);
                } else {
                    Notify.show("请先登录");
                    jump(UserLoginFragment.class.getName(), "会员登录");
                }
            }
        });
    }

    private void refresh() {
        isAlive = false;
        pagenum = 0;
        refreshScrollView.setMode(Mode.BOTH);
        products.clear();
        productAdapter.notifyDataSetChanged();
        getSaleTheme();
        getMiaoshaProduct();
        getLoveFresh();
        getScoreProduct();
        getSaleTimes();
        getStore();
        getAds();
        getBrand();
        getLocalGoods();
        getLocalService();
        getSaleTejia();
        getProduct();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.home_title_class:
                jump(ClassesFragment.class.getName(), "商品分类");
                break;

            case R.id.home_title_scan:
                startActivity(new Intent(getActivity(), CaptureActivity.class));
                break;

            case R.id.home_title_edit:
                jump(ProductSearchFragment.class.getName(), "商品搜索", true);
                break;

            case R.id.home_part_nongfu:
                jump(NongfuFragment.class.getName(), "农副产品");
                break;

            default:
                break;
        }
    }

    private void getSaleTheme() {
        Request request = new Request();
        request.setUrl(API.API_SALE_ACTIVITY);
        request.addRequestParam("strno", Store.getStore().getStoreNumber());
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {

            }

            @Override
            protected void onFail(MissionMessage missionMessage) {

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                PartThemeFragment.getThemeFragment().refresh(requestMessage.getResult());
            }

            @Override
            protected void onFinish() {

            }
        });
    }

    private void getMiaoshaProduct() {
        Request request = new Request();
        request.setUrl(API.API_SALE_MIAOSHA);
        request.addRequestParam("strno", Store.getStore().getStoreNumber());
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {

            }

            @Override
            protected void onFail(MissionMessage missionMessage) {

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                PartMiaoshaFragment.getMiaoshaFragment().refresh(requestMessage.getResult());
            }

            @Override
            protected void onFinish() {

            }
        });
    }

    private void getLoveFresh() {
        Request request = new Request();
        request.setUrl(API.API_LOCAL_BUSINESS_SERVICE_FRESH);
        request.addRequestParam("pageNo", "1");
        request.addRequestParam("pageSize", "5");
        request.addRequestParam("organizationId", Store.getStore().getStoreId());
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {

            }

            @Override
            protected void onFail(MissionMessage missionMessage) {

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                PartFreshFragment.getFreshFragment().refresh(requestMessage.getResult());
            }

            @Override
            protected void onFinish() {

            }
        });
    }

    private void getScoreProduct() {
        Request request = new Request();
        request.setUrl(API.API_SCORE_PRODUCT_LIST);
        request.addRequestParam("jgbh", Store.getStore().getStoreNumber());
        request.addRequestParam("pagenum", "1");
        request.addRequestParam("pagesize", "8");
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {

            }

            @Override
            protected void onFail(MissionMessage missionMessage) {

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                PartScoreFragment.getScoreFragment().refresh(requestMessage.getResult());
            }

            @Override
            protected void onFinish() {

            }
        });
    }

    private void getSaleTimes() {
        Request request = new Request();
        request.setUrl(API.API_SALE_CLASS);
        request.addRequestParam("strno", Store.getStore().getStoreNumber());
        request.addRequestParam("flag", "1");
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {

            }

            @Override
            protected void onFail(MissionMessage missionMessage) {

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                PartSaleTimeFragment.getSaleTimeFragment().refresh(requestMessage.getResult());
            }

            @Override
            protected void onFinish() {

            }
        });
    }

    private void getSaleTejia() {
        Request request = new Request();
        request.setUrl(API.API_SALE_TEJIA);
        request.addRequestParam("strno", Store.getStore().getStoreNumber());
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {

            }

            @Override
            protected void onFail(MissionMessage missionMessage) {

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                PartTejiaFragment.getTejiaFragment().refresh(requestMessage.getResult());
            }

            @Override
            protected void onFinish() {

            }
        });
    }

    private void getStore() {
        Request request = new Request();
        request.setUrl(API.API_LOCAL_STORE_LIST);
        request.addRequestParam("storeId", Store.getStore().getStoreId());
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {

            }

            @Override
            protected void onFail(MissionMessage missionMessage) {

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                PartStoreFragment.getStoreFragment().refresh(requestMessage.getResult());
            }

            @Override
            protected void onFinish() {

            }
        });
    }

    private void getAds() {
        Request request = new Request();
        request.setUrl(API.API_ADS);
        request.addRequestParam("number", Store.getStore().getStoreNumber());
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {

            }

            @Override
            protected void onFail(MissionMessage missionMessage) {

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                PartAdsFragment.getAdsFragment().refresh(requestMessage.getResult());
            }

            @Override
            protected void onFinish() {

            }
        });
    }

    private void getLocalGoods() {
        Request request = new Request();
        request.setUrl(API.API_LOCAL_BUSINESS_GOODS);
        request.addRequestParam("pageNo", "1");
        request.addRequestParam("pageSize", "3");
        request.addRequestParam("serviceProviderID", Store.getStore().getStoreId());
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {

            }

            @Override
            protected void onFail(MissionMessage missionMessage) {

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                PartLocalBusinessFragment.getLocalBusinessFragment().refreshGoods(requestMessage.getResult());
            }

            @Override
            protected void onFinish() {

            }
        });
    }

    private void getLocalService() {
        Request request = new Request();
        request.setUrl(API.API_LOCAL_BUSINESS_SERVICE);
        request.addRequestParam("pageNo", "1");
        request.addRequestParam("pageSize", "3");
        request.addRequestParam("organizationId", Store.getStore().getStoreId());
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {

            }

            @Override
            protected void onFail(MissionMessage missionMessage) {

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                PartLocalBusinessFragment.getLocalBusinessFragment().refreshService(requestMessage.getResult());
            }

            @Override
            protected void onFinish() {

            }
        });
    }

    private void getBrand() {
        Request request = new Request();
        request.setUrl(API.API_HOME_BRAND);
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {

            }

            @Override
            protected void onFail(MissionMessage missionMessage) {

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                PartBrandFragment.getBrandFragment().refresh(requestMessage.getResult());
            }

            @Override
            protected void onFinish() {

            }
        });
    }

    private void getProduct() {
        pagenum++;
        Request request = new Request();
        request.setUrl(API.API_PRODUCT_LIST);
        request.addRequestParam("jmdId", Store.getStore().getStoreId());
        request.addRequestParam("pageNo", String.valueOf(pagenum));
        request.addRequestParam("pageSize", String.valueOf(pagesize));
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {

            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                refreshScrollView.onRefreshComplete();
                pagenum--;
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                refreshScrollView.onRefreshComplete();
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    JSONObject info;
                    try {
                        info = new JSONObject(requestMessage.getResult());
                        if (info.getString("state").equalsIgnoreCase("SUCCESS")) {
                            JSONArray productArray = info.optJSONArray("dataList");
                            if (productArray != null) {
                                if (productArray.length() > 0) {
                                    if (productArray.length() < pagesize) {
                                        refreshScrollView.setMode(Mode.PULL_FROM_START);
                                    }
                                    StringBuilder stringBuilder = new StringBuilder();
                                    for (int i = 0; i < productArray.length(); i++) {
                                        ModelProduct product = new ModelProduct(productArray.getJSONObject(i));
                                        products.add(product);
                                        if (i > 0) {
                                            stringBuilder.append(",");
                                        }
                                        stringBuilder.append(product.getId());
                                    }
                                    productAdapter.notifyDataSetChanged();
                                    getProductPrice(stringBuilder.toString());
                                    return;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                refreshScrollView.setMode(Mode.PULL_FROM_START);
            }

            @Override
            protected void onFinish() {

            }
        });
    }

    private void getProductPrice(String productIds) {
        Request request = new Request();
        request.setUrl(API.API_PRICE_LIST);
        request.addRequestParam("jmdId", Store.getStore().getStoreId());
        request.addRequestParam("productId", productIds);
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
                    JSONObject object;
                    try {
                        object = new JSONObject(requestMessage.getResult());
                        if (object.getString("state").equalsIgnoreCase("SUCCESS")) {
                            JSONArray priceArray = object.getJSONArray("dataList");
                            if (priceArray.length() > 0) {
                                for (int i = 0; i < priceArray.length(); i++) {
                                    ModelListPrice priceList = new ModelListPrice(priceArray.getJSONObject(i));
                                    priceMap.put(priceList.getProductId(), priceList);
                                }
                                productAdapter.notifyDataSetChanged();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onFinish() {
            }
        });
    }

    private void getStoreAreas() {
        if (TextUtils.isEmpty(Content.getStringContent("product_detail_area_name", ""))) {
            Request request = new Request();
            request.setUrl(API.API_STORE_SELECTED_AREA);
            request.addRequestParam("spid", Store.getStore().getStoreId());
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
                                    HashMap<Integer, ModelStoreArea> areaHashMap = new HashMap<>();
                                    for (int i = 0; i < array.length(); i++) {
                                        ModelStoreArea storeArea = new ModelStoreArea(array.optJSONObject(i));
                                        areaHashMap.put(storeArea.getType(), storeArea);
                                    }
                                    Content.saveStringContent("product_detail_area_name", getAreaName(areaHashMap));
                                    Content.saveStringContent("product_detail_area_id", getAreaId(areaHashMap));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                protected void onFinish() {

                }
            });
        }
    }

    private String getAreaName(HashMap<Integer, ModelStoreArea> areaHashMap) {
        StringBuilder builder = new StringBuilder();
        List<Map.Entry<Integer, ModelStoreArea>> entries = new ArrayList<>(areaHashMap.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Integer, ModelStoreArea>>() {
            @Override
            public int compare(Map.Entry<Integer, ModelStoreArea> area1, Map.Entry<Integer, ModelStoreArea> area2) {
                return area1.getKey().compareTo(area2.getKey());
            }
        });
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                builder.append(">");
            }
            builder.append(entries.get(i).getValue().getName());
        }
        return builder.toString();
    }

    private String getAreaId(HashMap<Integer, ModelStoreArea> areaHashMap) {
        List<Map.Entry<Integer, ModelStoreArea>> entries = new ArrayList<>(areaHashMap.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Integer, ModelStoreArea>>() {
            @Override
            public int compare(Map.Entry<Integer, ModelStoreArea> area1, Map.Entry<Integer, ModelStoreArea> area2) {
                return area1.getKey().compareTo(area2.getKey());
            }
        });
        return entries.get(entries.size() - 1).getValue().getId();
    }

    class ProductAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return products.size();
        }

        @Override
        public Object getItem(int position) {
            return products.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.grid_product,
                        null);
                holder.imageView = (ImageView) convertView
                        .findViewById(R.id.grid_product_image);
                holder.nameTextView = (TextView) convertView
                        .findViewById(R.id.grid_product_name);
                holder.priceTextView = (TextView) convertView
                        .findViewById(R.id.grid_product_price);
                LayoutParams params = new LayoutParams(
                        LayoutParams.MATCH_PARENT, screenWidth / 25 * 16);
                convertView.setLayoutParams(params);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ModelProduct product = products.get(position);
            holder.nameTextView.setText(product.getProductName());
            if (priceMap.containsKey(product.getId())) {
                holder.priceTextView.setText(Parameters.CONSTANT_RMB
                        + decimalFormat.format(priceMap.get(product.getId())
                        .getPrice()));
            }
            ImageLoader.getInstance().displayImage(
                    getSmallImageUrl(product.getImg()), holder.imageView);
            return convertView;
        }

        class ViewHolder {
            ImageView imageView;
            TextView priceTextView, nameTextView;
        }
    }

}
