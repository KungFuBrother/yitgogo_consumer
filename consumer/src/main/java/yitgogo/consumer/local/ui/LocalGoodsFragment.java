package yitgogo.consumer.local.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

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
import java.util.List;

import yitgogo.consumer.base.BaseNormalFragment;
import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.local.model.ModelLocalGoods;
import yitgogo.consumer.local.model.ModelLocalGoodsClass;
import yitgogo.consumer.local.model.ModelLocalServiceClass;
import yitgogo.consumer.store.model.Store;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.view.InnerGridView;

/**
 * @author Tiger
 * @description 易商圈-本地商品
 */
public class LocalGoodsFragment extends BaseNotifyFragment implements
        OnClickListener {

    TextView selectorClasses;

    LinearLayout miaoshaButton, tejiaButton;

    PullToRefreshScrollView refreshScrollView;
    InnerGridView goodsList;
    FrameLayout selectorFragmentLayout;
    LinearLayout selectorLayout;

    List<ModelLocalGoods> localGoods;
    GoodsAdapter goodsAdapter;

    LocalGoodsClasses localGoodsClasses, localGoodsClasses2;
    GoodsClassAdapter goodsClassAdapter;
    GoodsClassAdapter2 goodsClassAdapter2;

    String currentStoreId = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_local_business_goods);
        init();
        findViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(LocalGoodsFragment.class.getName());
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(LocalGoodsFragment.class.getName());
        if (!currentStoreId.equals(Store.getStore().getStoreId())) {
            currentStoreId = Store.getStore().getStoreId();
            getGoodsClasses();
            refresh();
        }
    }

    private void init() {
        measureScreen();
        localGoods = new ArrayList<>();
        goodsAdapter = new GoodsAdapter();

        localGoodsClasses = new LocalGoodsClasses();
        goodsClassAdapter = new GoodsClassAdapter();

        localGoodsClasses2 = new LocalGoodsClasses();
        goodsClassAdapter2 = new GoodsClassAdapter2();

    }

    @Override
    protected void findViews() {
        selectorClasses = (TextView) contentView
                .findViewById(R.id.local_business_selector_classes);
        miaoshaButton = (LinearLayout) contentView
                .findViewById(R.id.local_business_button_miaosha);
        tejiaButton = (LinearLayout) contentView
                .findViewById(R.id.local_business_button_tejia);
        refreshScrollView = (PullToRefreshScrollView) contentView
                .findViewById(R.id.local_business_content_refresh);
        goodsList = (InnerGridView) contentView
                .findViewById(R.id.local_business_content_list);
        selectorFragmentLayout = (FrameLayout) contentView
                .findViewById(R.id.local_business_selector_fragment);
        selectorLayout = (LinearLayout) contentView
                .findViewById(R.id.local_business_selector_layout);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        refreshScrollView.setMode(Mode.BOTH);
        goodsList.setAdapter(goodsAdapter);
    }

    @Override
    protected void registerViews() {
        selectorLayout.setOnClickListener(this);
        selectorClasses.setOnClickListener(this);
        miaoshaButton.setOnClickListener(this);
        tejiaButton.setOnClickListener(this);
        refreshScrollView
                .setOnRefreshListener(new OnRefreshListener2<ScrollView>() {

                    @Override
                    public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                        refresh();
                    }

                    @Override
                    public void onPullUpToRefresh(
                            PullToRefreshBase<ScrollView> refreshView) {
                        getGoods();
                    }

                });
    }

    private void refresh() {
        hideSelector();
        refreshScrollView.setMode(Mode.BOTH);
        pagenum = 0;
        localGoods.clear();
        goodsAdapter.notifyDataSetChanged();
        getGoods();
    }

    private void showSelector(Fragment fragment) {
        getFragmentManager().beginTransaction()
                .replace(R.id.local_business_selector_fragment, fragment)
                .commit();
        selectorLayout.setVisibility(View.VISIBLE);
    }

    public void hideSelector() {
        selectorLayout.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.local_business_selector_layout:
                hideSelector();
                break;

            case R.id.local_business_selector_classes:
                showSelector(new GoodsClassSelector());
                break;

            case R.id.local_business_button_miaosha:
                jump(LocalSaleMiaoshaFragment.class.getName(), "本地秒杀");
                break;

            case R.id.local_business_button_tejia:
                jump(LocalSaleTejiaFragment.class.getName(), "本地特价");
                break;

            default:
                break;
        }
    }


    private void getGoods() {
        pagenum++;
        Request request = new Request();
        request.setUrl(API.API_LOCAL_BUSINESS_GOODS);
        request.addRequestParam("pageNo", String.valueOf(pagenum));
        request.addRequestParam("pageSize", String.valueOf(pagesize));
        request.addRequestParam("serviceProviderID", Store.getStore().getStoreId());
        if (localGoodsClasses2.getSelection() >= 0) {
            if (localGoodsClasses2.getGoodsClasses().size() > localGoodsClasses2.getSelection()) {
                String classId = localGoodsClasses2.getGoodsClasses().get(localGoodsClasses2.getSelection()).getId();
                request.addRequestParam("retailProTypeValueID", classId);
            }
        }
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                if (pagenum == 0) {
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
                    JSONObject object;
                    try {
                        object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            JSONArray array = object.optJSONArray("dataList");
                            if (array != null) {
                                if (array.length() > 0) {
                                    if (array.length() < pagesize) {
                                        refreshScrollView.setMode(Mode.PULL_FROM_START);
                                    }
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject goods = array.optJSONObject(i);
                                        if (goods != null) {
                                            localGoods.add(new ModelLocalGoods(goods));
                                        }
                                    }
                                    goodsAdapter.notifyDataSetChanged();
                                    return;
                                } else {
                                    refreshScrollView.setMode(Mode.PULL_FROM_START);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (localGoods.size() == 0) {
                        loadingEmpty();
                    }
                } else {
                    loadingFailed();
                }
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }


    private void getGoodsClasses() {
        Request request = new Request();
        request.setUrl(API.API_LOCAL_BUSINESS_GOODS_CLASS_PRIMARY);
        request.addRequestParam("serviceProviderID", Store
                .getStore().getStoreId());
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
                        localGoodsClasses = new LocalGoodsClasses(requestMessage.getResult(), 1);
                        goodsClassAdapter.notifyDataSetChanged();
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

    private void GetGoodsClasses2(String params) {
        Request request = new Request();
        request.setUrl(API.API_LOCAL_BUSINESS_GOODS_CLASS_SECOND);
        request.addRequestParam("serviceProviderID", Store
                .getStore().getStoreId());
        request.addRequestParam("productTypeValueID", params);
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
                        localGoodsClasses2 = new LocalGoodsClasses(requestMessage.getResult(), 2);
                        goodsClassAdapter2.notifyDataSetChanged();
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


    class LocalGoodsClasses {

        List<ModelLocalGoodsClass> goodsClasses = new ArrayList<ModelLocalGoodsClass>();
        int selection = 0;

        public LocalGoodsClasses() {
        }

        public LocalGoodsClasses(String result, int type) throws JSONException {
            if (result.length() > 0) {
                JSONObject object = new JSONObject(result);
                if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                    JSONArray array = object.optJSONArray("dataList");
                    if (array != null) {
                        if (type == 1) {
                            goodsClasses.add(new ModelLocalGoodsClass());
                            selection = 0;
                        } else {
                            selection = -1;
                        }
                        for (int i = 0; i < array.length(); i++) {
                            goodsClasses.add(new ModelLocalGoodsClass(array
                                    .getJSONObject(i)));
                        }
                    }
                }
            }
        }

        public List<ModelLocalGoodsClass> getGoodsClasses() {
            return goodsClasses;
        }

        public int getSelection() {
            return selection;
        }

        public void setSelection(int selection) {
            this.selection = selection;
        }
    }

    class LocalGoodsBrand {

        List<ModelLocalServiceClass> serviceClasses = new ArrayList<ModelLocalServiceClass>();
        int selection = 0;

        public LocalGoodsBrand() {
        }

        public LocalGoodsBrand(String result) {
            if (result.length() > 0) {
                serviceClasses.add(new ModelLocalServiceClass());
                JSONObject object;
                try {
                    object = new JSONObject(result);
                    if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                        JSONArray array = object.optJSONArray("dataList");
                        if (array != null) {
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.optJSONObject(i);
                                if (jsonObject != null) {
                                    serviceClasses
                                            .add(new ModelLocalServiceClass(
                                                    jsonObject));
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public int getSelection() {
            return selection;
        }

        public void setSelection(int selection) {
            this.selection = selection;
        }

        public List<ModelLocalServiceClass> getServiceClasses() {
            return serviceClasses;
        }

    }

    class GoodsClassSelector extends BaseNormalFragment {

        ListView primaryListView, secondListView;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        @Nullable
        public View onCreateView(LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(
                    R.layout.selector_local_business_goods_class, null);
            findViews(view);
            return view;
        }

        @Override
        public void onResume() {
            super.onResume();
            if (localGoodsClasses.getGoodsClasses().isEmpty()) {
                getGoodsClasses();
            }
        }

        @Override
        protected void findViews(View view) {
            primaryListView = (ListView) view
                    .findViewById(R.id.selector_goods_class_primary);
            secondListView = (ListView) view
                    .findViewById(R.id.selector_goods_class_second);
            initViews();
            registerViews();
        }

        @Override
        protected void initViews() {
            primaryListView.setAdapter(goodsClassAdapter);
            secondListView.setAdapter(goodsClassAdapter2);
        }

        @Override
        protected void registerViews() {
            primaryListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1,
                                        int arg2, long arg3) {
                    localGoodsClasses.setSelection(arg2);
                    goodsClassAdapter.notifyDataSetChanged();
                    if (arg2 == 0) {
                        localGoodsClasses2 = new LocalGoodsClasses();
                        goodsClassAdapter2.notifyDataSetChanged();
                        refresh();
                    } else {
                        GetGoodsClasses2(localGoodsClasses.getGoodsClasses().get(arg2).getId());
                    }
                }
            });
            secondListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1,
                                        int arg2, long arg3) {
                    localGoodsClasses2.setSelection(arg2);
                    goodsClassAdapter2.notifyDataSetChanged();
                    refresh();
                }
            });
        }
    }

    class GoodsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return localGoods.size();
        }

        @Override
        public Object getItem(int position) {
            return localGoods.get(position);
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
                        LayoutParams.MATCH_PARENT, screenWidth / 3 * 2);
                convertView.setLayoutParams(params);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final ModelLocalGoods goods = localGoods.get(position);
            holder.nameTextView.setText(goods.getRetailProdManagerName());
            holder.priceTextView.setText(Parameters.CONSTANT_RMB
                    + decimalFormat.format(goods.getRetailPrice()));
            ImageLoader.getInstance().displayImage(
                    getSmallImageUrl(goods.getBigImgUrl()), holder.imageView);
            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("id", goods.getId());
                    jump(LocalGoodsDetailFragment.class.getName(),
                            goods.getRetailProdManagerName(), bundle);
                }
            });
            return convertView;
        }

        class ViewHolder {
            ImageView imageView;
            TextView priceTextView, nameTextView;
        }
    }

    class GoodsClassAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return localGoodsClasses.getGoodsClasses().size();
        }

        @Override
        public Object getItem(int position) {
            return localGoodsClasses.getGoodsClasses().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = layoutInflater.inflate(
                        R.layout.list_local_business_class, null);
                viewHolder.view = convertView
                        .findViewById(R.id.local_business_class_selector);
                viewHolder.serviceClassName = (TextView) convertView
                        .findViewById(R.id.local_business_class_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            ModelLocalGoodsClass goodsClass = localGoodsClasses
                    .getGoodsClasses().get(position);
            viewHolder.serviceClassName.setText(goodsClass
                    .getRetailProdTypeValueName());
            if (localGoodsClasses.getSelection() == position) {
                viewHolder.view.setBackgroundResource(R.color.textColorCompany);
                viewHolder.serviceClassName.setTextColor(getResources()
                        .getColor(R.color.textColorCompany));
            } else {
                viewHolder.view.setBackgroundResource(android.R.color.transparent);
                viewHolder.serviceClassName.setTextColor(getResources()
                        .getColor(R.color.textColorSecond));
            }
            return convertView;
        }

        class ViewHolder {
            View view;
            TextView serviceClassName;
        }
    }

    class GoodsClassAdapter2 extends BaseAdapter {

        @Override
        public int getCount() {
            return localGoodsClasses2.getGoodsClasses().size();
        }

        @Override
        public Object getItem(int position) {
            return localGoodsClasses2.getGoodsClasses().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = layoutInflater.inflate(
                        R.layout.list_local_business_class, null);
                viewHolder.view = convertView
                        .findViewById(R.id.local_business_class_selector);
                viewHolder.serviceClassName = (TextView) convertView
                        .findViewById(R.id.local_business_class_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            ModelLocalGoodsClass goodsClass = localGoodsClasses2
                    .getGoodsClasses().get(position);
            if (localGoodsClasses2.getSelection() == position) {
                viewHolder.view.setBackgroundResource(R.color.textColorCompany);
                viewHolder.serviceClassName.setTextColor(getResources()
                        .getColor(R.color.textColorCompany));
            } else {
                viewHolder.view.setBackgroundResource(android.R.color.transparent);
                viewHolder.serviceClassName.setTextColor(getResources()
                        .getColor(R.color.textColorSecond));
            }
            viewHolder.serviceClassName.setText(goodsClass
                    .getRetailProdTypeValueName());
            return convertView;
        }

        class ViewHolder {
            View view;
            TextView serviceClassName;
        }
    }

}
