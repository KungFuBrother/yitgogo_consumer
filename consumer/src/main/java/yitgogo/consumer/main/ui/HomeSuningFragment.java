package yitgogo.consumer.main.ui;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
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
import java.util.HashMap;
import java.util.List;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.product.ui.ProductSearchFragment;
import yitgogo.consumer.suning.model.ModelProduct;
import yitgogo.consumer.suning.model.ModelProductClass;
import yitgogo.consumer.suning.model.ModelProductPrice;
import yitgogo.consumer.suning.model.SuningManager;
import yitgogo.consumer.suning.ui.ProductDetailFragment;
import yitgogo.consumer.suning.ui.SuningAreaFragment;
import yitgogo.consumer.suning.ui.SuningClassesFragment;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.view.InnerGridView;
import yitgogo.consumer.view.Notify;

public class HomeSuningFragment extends BaseNotifyFragment {

    ImageView classButton;
    LinearLayout searchButton;
    TextView cityTextView;
    LinearLayout cityButton;
    FrameLayout classLayout;
    DrawerLayout drawerLayout;
    PullToRefreshScrollView refreshScrollView;
    InnerGridView productGridView;

    ModelProductClass productClass = new ModelProductClass();

    List<ModelProduct> products = new ArrayList<>();
    HashMap<String, ModelProductPrice> priceHashMap = new HashMap<>();

    ProductAdapter productAdapter;

    SuningClassesFragment classesFragment = new SuningClassesFragment() {

        @Override
        public void onClassSelected(ModelProductClass selectedProductClass) {
            if (productClass == selectedProductClass) return;
            productClass = selectedProductClass;
            drawerLayout.closeDrawers();
            refresh();
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home_suning);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(HomeSuningFragment.class.getName());
        cityTextView.setText(SuningManager.getSuningAreas().getCity().getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(HomeSuningFragment.class.getName());
    }

    private void init() {
        measureScreen();
        productAdapter = new ProductAdapter();
    }

    @Override
    protected void findViews() {
        classButton = (ImageView) contentView.findViewById(R.id.home_suning_class);
        searchButton = (LinearLayout) contentView.findViewById(R.id.home_suning_search);
        cityTextView = (TextView) contentView.findViewById(R.id.home_suning_city);
        cityButton = (LinearLayout) contentView.findViewById(R.id.home_suning_city_select);
        classLayout = (FrameLayout) contentView.findViewById(R.id.home_suning_product_class);
        drawerLayout = (DrawerLayout) contentView.findViewById(R.id.home_suning_drawer);
        refreshScrollView = (PullToRefreshScrollView) contentView.findViewById(R.id.home_suning_refresh);
        productGridView = (InnerGridView) contentView.findViewById(R.id.home_suning_product_list);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        refreshScrollView.setMode(Mode.BOTH);
        productGridView.setAdapter(productAdapter);
        getFragmentManager().beginTransaction().replace(R.id.home_suning_product_class, classesFragment).commit();
    }

    @Override
    protected void registerViews() {
        refreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                refresh();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                getSuningProducts();
            }
        });
        productGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                ModelProduct product = products.get(arg2);
                if (priceHashMap.containsKey(product.getSku())) {
                    if (priceHashMap.get(product.getSku()).getPrice() > 0) {
                        Bundle bundle = new Bundle();
                        bundle.putString("skuId", product.getSku());
                        jump(ProductDetailFragment.class.getName(), product.getName(), bundle);
                    } else {
                        Notify.show("此商品暂未设置价格");
                    }
                } else {
                    Notify.show("此商品暂未设置价格");
                }
            }
        });
        classButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(classLayout)) {
                    drawerLayout.closeDrawers();
                } else {
                    drawerLayout.openDrawer(classLayout);
                }
            }
        });
        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt("type", ProductSearchFragment.SEARCH_TYPE_SUNING);
                jump(ProductSearchFragment.class.getName(), "商品搜索", bundle, true);
            }
        });
        cityButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                jump(SuningAreaFragment.class.getName(), "设置云商城收货区域");
            }
        });
    }

    private void refresh() {
        pagenum = 0;
        products.clear();
        priceHashMap.clear();
        productAdapter.notifyDataSetChanged();
        getSuningProducts();
    }

    private void getSuningProducts() {
        pagenum++;
        Request request = new Request();
        request.setUrl(API.API_SUNING_PRODUCT_LIST);
        request.addRequestParam("classId", productClass.getId());
        request.addRequestParam("pagenum", String.valueOf(pagenum));
        request.addRequestParam("pagesize", String.valueOf(pagesize));
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                loadingEmpty("获取商品数据失败");
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            JSONArray array = object.optJSONArray("dataList");
                            if (array != null) {
                                if (array.length() < pagesize) {
                                    refreshScrollView.setMode(Mode.PULL_FROM_START);
                                }
                                JSONArray priceJsonArray = new JSONArray();
                                for (int i = 0; i < array.length(); i++) {
                                    ModelProduct product = new ModelProduct(array.optJSONObject(i));
                                    products.add(product);
                                    priceJsonArray.put(product.getSku());
                                }
                                if (products.isEmpty()) {
                                    loadingEmpty();
                                } else {
                                    productAdapter.notifyDataSetChanged();
                                    if (priceJsonArray.length() > 0) {
                                        getSuningProductPrice(priceJsonArray);
                                    }
                                }
                            }
                            return;
                        }
                        Notify.show(object.optString("message"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (products.isEmpty()) {
                    loadingEmpty();
                }
            }

            @Override
            protected void onFinish() {
                hideLoading();
                refreshScrollView.onRefreshComplete();
            }
        });
    }

    private void getSuningProductPrice(final JSONArray priceJsonArray) {
        Request request = new Request();
        request.setUrl(API.API_SUNING_PRODUCT_PRICE);

        JSONObject data = new JSONObject();
        try {
            data.put("accessToken", SuningManager.getSignature().getToken());
            data.put("appKey", SuningManager.appKey);
            data.put("v", SuningManager.version);
            data.put("cityId", SuningManager.getSuningAreas().getCity().getCode());
            data.put("sku", priceJsonArray);
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
                                getSuningProductPrice(priceJsonArray);
                            }
                        }

                        @Override
                        protected void onFinish() {

                        }
                    });
                    return;
                }
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optBoolean("isSuccess")) {
                            JSONArray array = object.optJSONArray("result");
                            if (array != null) {
                                for (int j = 0; j < array.length(); j++) {
                                    ModelProductPrice productPrice = new ModelProductPrice(array.optJSONObject(j));
                                    priceHashMap.put(productPrice.getSkuId(), productPrice);
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
                hideLoading();
            }
        });
    }

    class ProductAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return products.size();
        }

        @Override
        public Object getItem(int i) {
            return products.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = layoutInflater.inflate(R.layout.list_product_suning, null);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView) view.findViewById(R.id.list_product_suning_image);
                viewHolder.nameTextView = (TextView) view.findViewById(R.id.list_product_suning_name);
                viewHolder.priceTextView = (TextView) view.findViewById(R.id.list_product_suning_price);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, screenWidth / 2);
                viewHolder.imageView.setLayoutParams(layoutParams);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            ModelProduct product = products.get(i);
            ImageLoader.getInstance().displayImage(product.getImage(), viewHolder.imageView);
            viewHolder.nameTextView.setText(product.getName());
            if (priceHashMap.containsKey(product.getSku())) {
                if (priceHashMap.get(product.getSku()).getPrice() > 0) {
                    viewHolder.priceTextView.setText(Parameters.CONSTANT_RMB
                            + decimalFormat.format(priceHashMap.get(product.getSku()).getPrice()));
                } else {
                    viewHolder.priceTextView.setHint("暂未设置价格");
                }
            } else {
                viewHolder.priceTextView.setHint("暂未设置价格");
            }
            return view;
        }

        class ViewHolder {
            ImageView imageView;
            TextView nameTextView;
            TextView priceTextView;
        }

    }

}
