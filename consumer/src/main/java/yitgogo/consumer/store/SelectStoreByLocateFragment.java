package yitgogo.consumer.store;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
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
import yitgogo.consumer.store.model.ModelStoreLocated;
import yitgogo.consumer.store.model.Store;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Content;
import yitgogo.consumer.tools.LogUtil;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.view.InnerListView;

public class SelectStoreByLocateFragment extends BaseNotifyFragment {

    TextView storeNameTextView, storeAddressTextView;

    InnerListView storeListView;

    List<ModelStoreLocated> storeLocateds;
    LocatedStoreAdapter storeAdapter;
    LocationClient locationClient;
    int locateTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_select_store_by_locate);
        init();
        findViews();
    }

    private void init() {
        storeLocateds = new ArrayList<>();
        storeAdapter = new LocatedStoreAdapter();
        initLocationTool();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(SelectStoreByLocateFragment.class.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(SelectStoreByLocateFragment.class.getName());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        locate();
    }

    @Override
    protected void findViews() {

        storeNameTextView = (TextView) contentView.findViewById(R.id.store_by_locate_store_name);
        storeAddressTextView = (TextView) contentView.findViewById(R.id.store_by_locate_store_address);
        storeListView = (InnerListView) contentView.findViewById(R.id.store_by_locate_store_list);

        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        storeListView.setAdapter(storeAdapter);
        storeNameTextView.setText(Store.getStore().getStoreName());
        storeAddressTextView.setText(Store.getStore().getStoreAddess());
    }

    @Override
    protected void registerViews() {
        storeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Content.saveIntContent(Parameters.CACHE_KEY_STORE_TYPE, Parameters.CACHE_VALUE_STORE_TYPE_LOCATED);
                Content.saveStringContent(Parameters.CACHE_KEY_STORE_JSONSTRING, storeLocateds.get(i).getJsonObject().toString());
                Store.init(getActivity());
                getActivity().finish();
            }
        });
    }

    class LocatedStoreAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return storeLocateds.size();
        }

        @Override
        public Object getItem(int position) {
            return storeLocateds.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_store_selected, null);
                holder = new ViewHolder();
                holder.nameTextView = (TextView) convertView.findViewById(R.id.list_store_name);
                holder.addressTextView = (TextView) convertView.findViewById(R.id.list_store_address);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ModelStoreLocated storeLocated = storeLocateds.get(position);
            holder.nameTextView.setText(storeLocated.getTitle());
            holder.addressTextView.setText(storeLocated.getAddress());
            return convertView;
        }

        class ViewHolder {
            TextView nameTextView, addressTextView;
        }
    }

    /**
     * 初始化定位工具
     */
    private void initLocationTool() {
        locationClient = new LocationClient(getActivity());
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
        option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        // option.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
        // option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
        locationClient.setLocOption(option);
        locationClient.registerLocationListener(new BDLocationListener() {

            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                hideLoading();
                locateTime++;
                // 防止重复定位，locateTime>1 表示已经定位过，无需再定位
                if (locateTime > 1) {
                    return;
                }
                if (bdLocation == null) {
                    contentView.setVisibility(View.GONE);
                    loadingEmpty("自动定位失败，请手动选择服务中心");
                } else {
                    getNearestStore(bdLocation);
                }
                locationClient.stop();
            }

        });
    }

    private void locate() {
        showLoading();
        locationClient.start();
        locationClient.requestLocation();
    }

    private void getNearestStore(BDLocation location) {
        Request request = new Request();
        request.setUrl(API.API_LBS_NEARBY);
        request.setRequestType(Request.REQUEST_TYPE_GET);
        request.addRequestParam("ak", Parameters.CONSTANT_LBS_AK);
        request.addRequestParam("geotable_id", Parameters.CONSTANT_LBS_TABLE);
        request.addRequestParam("sortby", "distance:1");
        request.addRequestParam("radius", "30000");
        request.addRequestParam("page_index", "0");
        request.addRequestParam("page_size", "10");
        request.addRequestParam("location", location.getLongitude() + "," + location.getLatitude());
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                contentView.setVisibility(View.GONE);
                loadingEmpty("自动定位失败，请手动选择服务中心");
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    LogUtil.logInfo("API_LBS_NEARBY", requestMessage.getResult());
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        JSONArray array = object.optJSONArray("contents");
                        if (array != null) {
                            for (int i = 0; i < array.length(); i++) {
                                storeLocateds.add(new ModelStoreLocated(array.optJSONObject(i)));
                            }
                            if (!storeLocateds.isEmpty()) {
                                if (getActivity() != null) {
                                    storeAdapter.notifyDataSetChanged();
                                    Content.saveIntContent(Parameters.CACHE_KEY_STORE_TYPE, Parameters.CACHE_VALUE_STORE_TYPE_LOCATED);
                                    Content.saveStringContent(Parameters.CACHE_KEY_STORE_JSONSTRING, storeLocateds.get(0).getJsonObject().toString());
                                    Store.init(getActivity());
                                    storeNameTextView.setText(Store.getStore().getStoreName());
                                    storeAddressTextView.setText(Store.getStore().getStoreAddess());
                                }
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                contentView.setVisibility(View.GONE);
                loadingEmpty("自动定位失败，请手动选择服务中心");
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

}
