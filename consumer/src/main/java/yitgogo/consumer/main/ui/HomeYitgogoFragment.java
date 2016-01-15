package yitgogo.consumer.main.ui;

import android.os.Bundle;
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
import java.util.HashMap;
import java.util.List;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.home.model.ModelListPrice;
import yitgogo.consumer.home.model.ModelProduct;
import yitgogo.consumer.home.part.PartAdsFragment;
import yitgogo.consumer.product.ui.ClassesFragment;
import yitgogo.consumer.product.ui.ProductSearchFragment;
import yitgogo.consumer.store.model.Store;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.view.InnerGridView;

public class HomeYitgogoFragment extends BaseNotifyFragment {

    PullToRefreshScrollView refreshScrollView;
    InnerGridView productGridView;
    List<ModelProduct> products;
    HashMap<String, ModelListPrice> priceMap;
    ProductAdapter productAdapter;

    ImageView classButton, searchButton;
    PartAdsFragment adsFragment;

    String currentStoreId = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home_yitgogo);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(HomeYitgogoFragment.class.getName());
        if (!currentStoreId.equals(Store.getStore().getStoreId())) {
            currentStoreId = Store.getStore().getStoreId();
            refresh();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(HomeYitgogoFragment.class.getName());
    }

    private void init() {
        measureScreen();
        products = new ArrayList<>();
        priceMap = new HashMap<>();
        productAdapter = new ProductAdapter();
        adsFragment = new PartAdsFragment();
    }

    @Override
    protected void findViews() {
        refreshScrollView = (PullToRefreshScrollView) contentView.findViewById(R.id.home_yitgogo_refresh);
        productGridView = (InnerGridView) contentView.findViewById(R.id.home_yitgogo_product_list);
        classButton = (ImageView) contentView.findViewById(R.id.home_yitgogo_class);
        searchButton = (ImageView) contentView.findViewById(R.id.home_yitgogo_search);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        refreshScrollView.setMode(Mode.BOTH);
        productGridView.setAdapter(productAdapter);
        getFragmentManager().beginTransaction().replace(R.id.home_yitgogo_ads_layout, adsFragment).commit();
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
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                showProductDetail(products.get(arg2).getId(), products.get(arg2).getProductName(), CaptureActivity.SALE_TYPE_NONE);
            }
        });
        classButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                jump(ClassesFragment.class.getName(), "商品分类");
            }
        });
        searchButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                jump(ProductSearchFragment.class.getName(), "商品搜索", true);
            }
        });
    }

    private void refresh() {
        getAds();
        refreshScrollView.setMode(Mode.BOTH);
        pagenum = 0;
        products.clear();
        productAdapter.notifyDataSetChanged();
        getProduct();
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
                adsFragment.refresh(requestMessage.getResult());
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
                if (pagenum == 1) {
                    showLoading();
                }
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
                hideLoading();
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
                convertView = layoutInflater.inflate(R.layout.grid_product, null);
                holder.imageView = (ImageView) convertView.findViewById(R.id.grid_product_image);
                holder.nameTextView = (TextView) convertView.findViewById(R.id.grid_product_name);
                holder.priceTextView = (TextView) convertView.findViewById(R.id.grid_product_price);
                LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, screenWidth / 25 * 16);
                convertView.setLayoutParams(params);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ModelProduct product = products.get(position);
            holder.nameTextView.setText(product.getProductName());
            if (priceMap.containsKey(product.getId())) {
                holder.priceTextView.setText(Parameters.CONSTANT_RMB + decimalFormat.format(priceMap.get(product.getId()).getPrice()));
            }
            ImageLoader.getInstance().displayImage(getSmallImageUrl(product.getImg()), holder.imageView);
            return convertView;
        }

        class ViewHolder {
            ImageView imageView;
            TextView priceTextView, nameTextView;
        }
    }

}
