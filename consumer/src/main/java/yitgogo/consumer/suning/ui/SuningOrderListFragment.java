package yitgogo.consumer.suning.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
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
import yitgogo.consumer.suning.model.ModelSuningOrder;
import yitgogo.consumer.suning.model.ModelSuningOrderProduct;
import yitgogo.consumer.tools.API;
import yitgogo.consumer.tools.Parameters;
import yitgogo.consumer.user.model.User;
import yitgogo.consumer.view.InnerGridView;

public class SuningOrderListFragment extends BaseNotifyFragment {

    public static boolean needRefersh = true;
    PullToRefreshListView orderList;
    OrderAdapter orderAdapter;
    List<ModelSuningOrder> orders;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_order_platform);
        init();
        findViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(SuningOrderListFragment.class.getName());
        if (needRefersh) {
            needRefersh = false;
            refresh();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(SuningOrderListFragment.class.getName());
    }

    @Override
    public void onDestroy() {
        needRefersh = true;
        super.onDestroy();
    }

    private void init() {
        measureScreen();
        orders = new ArrayList<>();
        orderAdapter = new OrderAdapter();
    }

    @Override
    protected void findViews() {
        orderList = (PullToRefreshListView) contentView
                .findViewById(R.id.order_list);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        orderList.setAdapter(orderAdapter);
        orderList.setMode(Mode.BOTH);
    }

    @Override
    protected void registerViews() {
        orderList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                refresh();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                getOrders();
            }

        });
    }

    private void refresh() {
        orderList.setMode(Mode.BOTH);
        pagenum = 0;
        orders.clear();
        orderAdapter.notifyDataSetChanged();
        getOrders();
    }

    private void showOrderDetail(int index) {
        Bundle bundle = new Bundle();
        bundle.putString("object", orders.get(index).getJsonObject().toString());
        jump(SuningOrderDetailFragment.class.getName(), "订单详情", bundle);
    }

    private void getOrders() {
        pagenum++;
        Request request = new Request();
        request.setUrl(API.API_SUNING_ORDER_LIST);
        request.addRequestParam("menberAccount", User.getUser().getUseraccount());
        request.addRequestParam("pagenum", pagenum + "");
        request.addRequestParam("pagesize", pagesize + "");
        MissionController.startRequestMission(getActivity(), request, new RequestListener() {
            @Override
            protected void onStart() {
                showLoading();
            }

            @Override
            protected void onFail(MissionMessage missionMessage) {
                orderList.onRefreshComplete();
                pagenum--;
            }

            @Override
            protected void onSuccess(RequestMessage requestMessage) {
                orderList.onRefreshComplete();
                if (!TextUtils.isEmpty(requestMessage.getResult())) {
                    try {
                        JSONObject object = new JSONObject(requestMessage.getResult());
                        if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                            JSONArray array = object.optJSONArray("dataList");
                            if (array != null) {
                                if (array.length() < pagesize) {
                                    orderList.setMode(Mode.PULL_FROM_START);
                                }
                                for (int i = 0; i < array.length(); i++) {
                                    orders.add(new ModelSuningOrder(array
                                            .getJSONObject(i)));
                                }
                                if (orders.size() > 0) {
                                    orderAdapter.notifyDataSetChanged();
                                    return;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (orders.size() == 0) {
                    loadingEmpty();
                }
            }

            @Override
            protected void onFinish() {
                hideLoading();
            }
        });
    }

//    class GetOrders extends AsyncTask<Void, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//            showLoading();
//            pagenum++;
//        }
//
//        @Override
//        protected String doInBackground(Void... arg0) {
//            List<NameValuePair> nameValuePairs = new ArrayList<>();
//            nameValuePairs.add(new BasicNameValuePair("menberAccount", User
//                    .getUser().getUseraccount()));
//            nameValuePairs.add(new BasicNameValuePair("pagenum", pagenum + ""));
//            nameValuePairs.add(new BasicNameValuePair("pagesize", pagesize + ""));
//            return netUtil.postWithoutCookie(API.API_SUNING_ORDER_LIST,
//                    nameValuePairs, false, false);
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            hideLoading();
//            orderList.onRefreshComplete();
//            if (!TextUtils.isEmpty(result)) {
//                try {
//                    JSONObject object = new JSONObject(result);
//                    if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
//                        JSONArray array = object.optJSONArray("dataList");
//                        if (array != null) {
//                            if (array.length() < pagesize) {
//                                orderList.setMode(Mode.PULL_FROM_START);
//                            }
//                            for (int i = 0; i < array.length(); i++) {
//                                orders.add(new ModelSuningOrder(array
//                                        .getJSONObject(i)));
//                            }
//                            if (orders.size() > 0) {
//                                orderAdapter.notifyDataSetChanged();
//                                return;
//                            }
//                        }
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (orders.size() == 0) {
//                loadingEmpty();
//            }
//        }
//    }

    class OrderAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return orders.size();
        }

        @Override
        public Object getItem(int position) {
            return orders.get(position);
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
                convertView = layoutInflater.inflate(R.layout.list_order, null);
                holder.orderProducts = (InnerGridView) convertView
                        .findViewById(R.id.list_order_product);
                holder.orderStateText = (TextView) convertView
                        .findViewById(R.id.list_order_state);
                holder.orderMoneyText = (TextView) convertView
                        .findViewById(R.id.list_order_money);
                holder.orderTimeText = (TextView) convertView
                        .findViewById(R.id.list_order_date);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ModelSuningOrder order = orders.get(position);
            holder.orderStateText.setText(order.getOrderType());
            holder.orderTimeText.setText(order.getSellTime());
            holder.orderMoneyText.setText(Parameters.CONSTANT_RMB
                    + decimalFormat.format(order.getAccount() + order.getFreight()));
            if (order.getProducts().size() > 1) {
                holder.orderProducts.setNumColumns(4);
            } else {
                holder.orderProducts.setNumColumns(1);
            }
            holder.orderProducts.setAdapter(new OrderProductAdapter(order
                    .getProducts()));
            final int index = position;
            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    showOrderDetail(index);
                }
            });
            holder.orderProducts
                    .setOnItemClickListener(new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1,
                                                int arg2, long arg3) {
                            showOrderDetail(index);
                        }
                    });
            return convertView;
        }

        class ViewHolder {
            InnerGridView orderProducts;
            TextView orderStateText, orderMoneyText, orderTimeText;
        }
    }

    class OrderProductAdapter extends BaseAdapter {

        List<ModelSuningOrderProduct> products;

        public OrderProductAdapter(List<ModelSuningOrderProduct> products) {
            this.products = products;
        }

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
                convertView = layoutInflater.inflate(
                        R.layout.list_product_order, null);
                holder.image = (ImageView) convertView
                        .findViewById(R.id.list_product_order_image);
                holder.productNameText = (TextView) convertView
                        .findViewById(R.id.list_product_order_name);
                holder.productPriceText = (TextView) convertView
                        .findViewById(R.id.list_product_order_price);
                if (products.size() > 1) {
                    holder.productNameText.setVisibility(View.GONE);
                    holder.productPriceText.setVisibility(View.GONE);
                    holder.image.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT));
                    convertView.setLayoutParams(new AbsListView.LayoutParams(
                            AbsListView.LayoutParams.MATCH_PARENT,
                            screenWidth / 4));
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ModelSuningOrderProduct product = products.get(position);
            holder.productNameText.setText(product.getName());
            holder.productPriceText.setText(Parameters.CONSTANT_RMB
                    + decimalFormat.format(product.getPrice()));
            if (!product.getImages().isEmpty()) {
                ImageLoader.getInstance().displayImage(product.getImages().get(1),
                        holder.image);
            }
            return convertView;
        }

        class ViewHolder {
            ImageView image;
            TextView productNameText, productPriceText;
        }

    }

}
