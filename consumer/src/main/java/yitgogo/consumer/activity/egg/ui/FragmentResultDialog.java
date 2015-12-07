package yitgogo.consumer.activity.egg.ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.smartown.yitian.gogo.R;

import java.util.ArrayList;

import yitgogo.consumer.activity.egg.ui.adapter.GoldenResultListViewAdapter;

public class FragmentResultDialog extends DialogFragment implements OnClickListener {


    private int screenWidth;
    private int screenHeight;
    private TextView tvEnsure;
    private ListView mListView;
    private GoldenResultListViewAdapter mAdapter;
    private ArrayList<String> datas;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        measureScreen();
        init();
    }

    private void init() {
        datas = new ArrayList<String>();
        datas.add("11:00\t\t\t恭喜你砸中品牌电视");
        datas.add("12:00\t\t\t什么也没有砸中");
        datas.add("12:30\t\t\t什么也没有砸中");
        datas.add("13:00\t\t\t什么也没有砸中");
        datas.add("15:00\t\t\t恭喜你砸中10元现金");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow()
                .setBackgroundDrawableResource(R.color.dialog_bg);

        View view = inflater.inflate(R.layout.golden_egg_result_fragment, null);

        initView(view);
        loadResultDatas();
        return view;
    }

    private void initView(View view) {
        tvEnsure = (TextView) view.findViewById(R.id.ensure_tv);
        mListView = (ListView) view.findViewById(R.id.result_tips_listview);
        mAdapter = new GoldenResultListViewAdapter(getActivity());
        mListView.setAdapter(mAdapter);

        tvEnsure.setOnClickListener(this);
    }

    private void loadResultDatas() {
        mAdapter.addDatas(datas);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getDialog() == null) {
            return;
        }

        getDialog().getWindow()
                .setLayout(screenWidth * 5 / 7, screenHeight / 2);
        getDialog().getWindow().setGravity(Gravity.CENTER);
    }

    private void measureScreen() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay()
                .getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
    }

    @Override
    public void onClick(View v) {
        if (v == tvEnsure) {

            dismiss();
        }
    }

}	
