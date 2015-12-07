package yitgogo.consumer.activity.egg.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartown.yitian.gogo.R;

import java.util.ArrayList;
import java.util.List;

public class GoldenResultListViewAdapter extends BaseAdapter {


    private List<String> datas;
    private LayoutInflater inflater;

    public GoldenResultListViewAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        datas = new ArrayList<String>();
    }

    public void addDatas(List<String> datas) {
        this.datas.addAll(datas);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public String getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {

            convertView = inflater.inflate(R.layout.result_item, null);
            holder = new ViewHolder();
            holder.resultTv = (TextView) convertView.findViewById(R.id.item_result_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String result = datas.get(position);
        holder.resultTv.setText(result);
        return convertView;
    }

    class ViewHolder {
        TextView resultTv;
    }

}
