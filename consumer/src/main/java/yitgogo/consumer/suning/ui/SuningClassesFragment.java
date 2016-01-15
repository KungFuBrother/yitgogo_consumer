package yitgogo.consumer.suning.ui;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
import yitgogo.consumer.suning.model.ModelProductClass;
import yitgogo.consumer.suning.model.SuningManager;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.view.Notify;

/**
 * Created by Tiger on 2015-10-21.
 */
public abstract class SuningClassesFragment extends BaseNotifyFragment {

    private SwipeRefreshLayout refreshLayout;
    private ListView listView;
    private List<ModelProductClass> productClasses;
    private ProductClassAdapter productClassAdapter;
    private ModelProductClass selectedProductClass = new ModelProductClass();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_suning_classes);
        init();
        findViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(SuningClassesFragment.class.getName());
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(SuningClassesFragment.class.getName());
        if (productClasses.isEmpty()) {
            if (!TextUtils.isEmpty(SuningManager.getSuningAreas().getTown().getCode())) {
                getSuningClasses();
            }
        }
    }

    private void init() {
        productClasses = new ArrayList<>();
        productClassAdapter = new ProductClassAdapter();
    }

    @Override
    protected void findViews() {
        refreshLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.suning_classes_refresh);
        listView = (ListView) contentView.findViewById(R.id.suning_classes);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        listView.setAdapter(productClassAdapter);
    }

    @Override
    protected void registerViews() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                select(productClasses.get(i));
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getSuningClasses();
            }
        });
    }

    private void select(ModelProductClass productClass) {
        selectedProductClass = productClass;
        productClassAdapter.notifyDataSetChanged();
        onClassSelected(selectedProductClass);
    }

    public abstract void onClassSelected(ModelProductClass selectedProductClass);

    private void getSuningClasses() {
        productClasses.clear();
        productClassAdapter.notifyDataSetChanged();
        Request request = new Request();
        request.setUrl(API.API_SUNING_PRODUCT_CALSSES);
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                refreshLayout.setRefreshing(true);
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                loadingEmpty("获取分类数据失败");
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            JSONArray array = object.optJSONArray("dataList");
                            if (array != null) {
                                for (int i = 0; i < array.length(); i++) {
                                    productClasses.add(new ModelProductClass(array.optJSONObject(i)));
                                }
                                if (!productClasses.isEmpty()) {
                                    select(productClasses.get(0));
                                }
                            }
                            return;
                        }
                        Notify.show(object.optString("message"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onFinish() {
                refreshLayout.setRefreshing(false);
            }
        });
    }

    class ProductClassAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return productClasses.size();
        }

        @Override
        public Object getItem(int position) {
            return productClasses.get(position);
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
                viewHolder.selector = convertView
                        .findViewById(R.id.local_business_class_selector);
                viewHolder.className = (TextView) convertView
                        .findViewById(R.id.local_business_class_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.className.setText(productClasses.get(position).getName());
            if (productClasses.get(position).getId().equals(selectedProductClass.getId())) {
                viewHolder.selector.setBackgroundResource(R.color.textColorCompany);
                viewHolder.className.setTextColor(getResources().getColor(R.color.textColorCompany));
            } else {
                viewHolder.selector.setBackgroundResource(android.R.color.transparent);
                viewHolder.className.setTextColor(getResources().getColor(R.color.textColorSecond));
            }
            return convertView;
        }

        class ViewHolder {
            TextView className;
            View selector;
        }
    }

}
