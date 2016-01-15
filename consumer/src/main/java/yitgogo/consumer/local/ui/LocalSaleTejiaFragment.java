package yitgogo.consumer.local.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.List;

import yitgogo.consumer.base.BaseNotifyFragment;
import yitgogo.consumer.local.model.ModelLocalSaleTejia;
import yitgogo.consumer.store.model.Store;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.view.InnerListView;
import yitgogo.consumer.view.Notify;

/**
 * 本地秒杀
 */
public class LocalSaleTejiaFragment extends BaseNotifyFragment {

    PullToRefreshScrollView refreshScrollView;
    InnerListView serviceList;

    List<ModelLocalSaleTejia> localSaleTejias;
    ProductAdapter productAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_local_business_nongfu);
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
        getLocalTejia();
    }

    private void init() {
        measureScreen();
        localSaleTejias = new ArrayList<>();
        productAdapter = new ProductAdapter();
    }

    @Override
    protected void findViews() {
        refreshScrollView = (PullToRefreshScrollView) contentView
                .findViewById(R.id.local_business_content_refresh);
        serviceList = (InnerListView) contentView
                .findViewById(R.id.local_business_content_list);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        refreshScrollView.setMode(Mode.DISABLED);
        serviceList.setAdapter(productAdapter);
    }

    @Override
    protected void registerViews() {
        serviceList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> paramAdapterView,
                                    View paramView, int paramInt, long paramLong) {
                Bundle bundle = new Bundle();
                bundle.putString("id", localSaleTejias.get(paramInt).getProductId());
                jump(LocalSaleTejiaDetailFragment.class.getName(),
                        localSaleTejias.get(paramInt).getProductName(), bundle);

            }
        });
    }

    class ProductAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return localSaleTejias.size();
        }

        @Override
        public Object getItem(int position) {
            return localSaleTejias.get(position);
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
                convertView = layoutInflater.inflate(
                        R.layout.list_local_sale_tejia, null);
                holder.imageView = (ImageView) convertView
                        .findViewById(R.id.local_sale_tejia_image);
                holder.nameTextView = (TextView) convertView
                        .findViewById(R.id.local_sale_tejia_name);
                holder.priceTextView = (TextView) convertView
                        .findViewById(R.id.local_sale_tejia_price);
                holder.originalPriceTextView = (TextView) convertView
                        .findViewById(R.id.local_sale_tejia_original_price);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ImageLoader.getInstance().displayImage(
                    getSmallImageUrl(localSaleTejias.get(position)
                            .getPromotionImg()), holder.imageView);
            holder.nameTextView.setText(localSaleTejias.get(position)
                    .getProductName());
            holder.priceTextView.setText(Parameters.CONSTANT_RMB
                    + decimalFormat.format(localSaleTejias.get(position)
                    .getPromotionalPrice()));
            holder.originalPriceTextView.setText("原价:"
                    + Parameters.CONSTANT_RMB
                    + decimalFormat.format(localSaleTejias.get(position)
                    .getProductPrice()));
            return convertView;
        }

        class ViewHolder {
            ImageView imageView;
            TextView priceTextView, originalPriceTextView, nameTextView;
        }
    }

    private void getLocalTejia() {
        Request request = new Request();
        request.setUrl(API.API_LOCAL_SALE_TEJIA);
        request.addRequestParam("jgbh", Store.getStore().getStoreNumber());
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
                localSaleTejias.clear();
                productAdapter.notifyDataSetChanged();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                Notify.show(missionMessage.getMessage());

            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    JSONObject object;
                    try {
                        object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            JSONArray array = object.optJSONArray("dataList");
                            if (array != null) {
                                for (int i = 0; i < array.length(); i++) {
                                    localSaleTejias.add(new ModelLocalSaleTejia(array
                                            .optJSONObject(i)));
                                }
                                productAdapter.notifyDataSetChanged();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (localSaleTejias.isEmpty()) {
                    loadingEmpty();
                }
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

}
