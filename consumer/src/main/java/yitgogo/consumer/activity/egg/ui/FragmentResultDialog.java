package yitgogo.consumer.activity.egg.ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import yitgogo.consumer.activity.egg.ui.adapter.GoldenResultListViewAdapter;
import yitgogo.consumer.activity.shake.model.ModelAwardHistory;
import yitgogo.consumer.view.Notify;

public class FragmentResultDialog extends DialogFragment implements OnClickListener {

    private int screenWidth;
    private int screenHeight;
    private TextView tvEnsure;
    private ListView mListView;

    private String result = "";

    public static FragmentResultDialog newInstance(String result) {
        FragmentResultDialog priceDialog = new FragmentResultDialog();
        Bundle bundle = new Bundle();
        bundle.putString("result", result);
        priceDialog.setArguments(bundle);
        return priceDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        measureScreen();
        init();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showHistory();
    }

    private void showHistory() {
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject object = new JSONObject(result);
                if (object.optString("state").equalsIgnoreCase("SUCCESS")) {
                    JSONArray array = object.optJSONArray("dataList");
                    if (array != null) {
                        List<ModelAwardHistory> awardHistories = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            awardHistories.add(new ModelAwardHistory(array.optJSONObject(i)));
                        }
                        if (getActivity() != null) {
                            mListView.setAdapter(new GoldenResultListViewAdapter(getActivity(), awardHistories));
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

    private void init() {

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("result")) {
                result = bundle.getString("result");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow()
                .setBackgroundDrawableResource(R.color.dialog_bg);

        View view = inflater.inflate(R.layout.golden_egg_result_fragment, null);

        initView(view);
        return view;
    }

    private void initView(View view) {
        tvEnsure = (TextView) view.findViewById(R.id.ensure_tv);
        mListView = (ListView) view.findViewById(R.id.result_tips_listview);
        tvEnsure.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getDialog() == null) {
            return;
        }

        getDialog().getWindow().setLayout(screenWidth * 4 / 5, screenWidth);
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
