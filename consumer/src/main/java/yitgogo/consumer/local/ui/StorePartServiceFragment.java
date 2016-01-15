package yitgogo.consumer.local.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import yitgogo.consumer.local.model.ModelLocalService;
import yitgogo.consumer.store.model.Store;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.view.InnerGridView;

public class StorePartServiceFragment extends BaseNormalFragment {

    LinearLayout moreButton;
    InnerGridView serviceList;
    List<ModelLocalService> localServices;
    ServiceAdapter serviceAdapter;
    String storeId = "";

    public StorePartServiceFragment(String storeId) {
        this.storeId = storeId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(StorePartServiceFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(StorePartServiceFragment.class.getName());
    }

    private void init() {
        measureScreen();
        localServices = new ArrayList<ModelLocalService>();
        serviceAdapter = new ServiceAdapter();
        getService();
    }

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.store_part_service, null);
        findViews(view);
        return view;
    }

    @Override
    protected void findViews(View view) {
        moreButton = (LinearLayout) view
                .findViewById(R.id.part_store_service_more);
        serviceList = (InnerGridView) view
                .findViewById(R.id.part_store_service_list);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        serviceList.setAdapter(serviceAdapter);
    }

    @Override
    protected void registerViews() {
    }

    class ServiceAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return localServices.size();
        }

        @Override
        public Object getItem(int position) {
            return localServices.get(position);
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
            final ModelLocalService localService = localServices.get(position);
            holder.nameTextView.setText(localService.getProductName());
            holder.priceTextView.setText(Parameters.CONSTANT_RMB
                    + decimalFormat.format(localService.getProductPrice()));
            ImageLoader.getInstance().displayImage(
                    getSmallImageUrl(localService.getImg()), holder.imageView);
            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("productId", localService.getId());
                    jump(LocalServiceDetailFragment.class.getName(),
                            localService.getProductName(), bundle);
                }
            });
            return convertView;
        }

        class ViewHolder {
            ImageView imageView;
            TextView priceTextView, nameTextView;
        }
    }

    private void getService() {
        Request request = new Request();
        request.setUrl(API.API_LOCAL_BUSINESS_SERVICE);
        request.addRequestParam("pageNo", "1");
        request.addRequestParam("pageSize", "20");
        request.addRequestParam("providerId", storeId);
        request.addRequestParam("organizationId", Store
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
                    JSONObject object;
                    try {
                        object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            JSONArray array = object.optJSONArray("dataList");
                            if (array != null) {
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject jsonObject = array.optJSONObject(i);
                                    if (jsonObject != null) {
                                        localServices.add(new ModelLocalService(
                                                jsonObject));
                                    }
                                }
                                serviceAdapter.notifyDataSetChanged();
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
