package yitgogo.consumer.activity.egg.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartown.yitian.gogo.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import yitgogo.consumer.activity.shake.model.ModelAwardHistory;

public class GoldenResultListViewAdapter extends BaseAdapter {


    private List<ModelAwardHistory> awardHistories;
    private LayoutInflater inflater;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public GoldenResultListViewAdapter(Context context, List<ModelAwardHistory> awardHistories) {
        inflater = LayoutInflater.from(context);
        this.awardHistories = awardHistories;
    }

    @Override
    public int getCount() {
        return awardHistories.size();
    }

    @Override
    public Object getItem(int position) {
        return awardHistories.get(position);
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
        ModelAwardHistory awardHistory = awardHistories.get(position);
        try {
            String name = "";
            if (awardHistory.getAward().getType() == 1) {
                name = "现金" + (int) awardHistory.getAward().getTypeValue() + "元";
            } else {
                name = awardHistory.getAward().getName();
            }
            Date date = simpleDateFormat.parse(awardHistories.get(position).getWinDate());
            String hour = String.valueOf(date.getHours());
            String minute = String.valueOf(date.getMinutes());
            if (date.getHours() < 10) {
                hour = "0" + date.getHours();
            }
            if (date.getMinutes() < 10) {
                minute = "0" + date.getMinutes();
            }
            holder.resultTv.setText(hour + ":" + minute + "\t\t恭喜你砸中" + name);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertView;
    }

    class ViewHolder {
        TextView resultTv;
    }

}
