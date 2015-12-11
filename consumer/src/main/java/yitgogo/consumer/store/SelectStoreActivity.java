package yitgogo.consumer.store;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartown.yitian.gogo.R;

import yitgogo.consumer.BaseActivity;
import yitgogo.consumer.tools.Content;
import yitgogo.consumer.tools.Parameters;

/**
 * Created by Tiger on 2015-12-10.
 */
public class SelectStoreActivity extends BaseActivity {

    LinearLayout backButton;
    LinearLayout switchLayout;
    TextView switchAreaTextView, switchLocateTextView;

    int type = 0;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_select_store);
        findViews();
    }

    @Override
    protected void findViews() {
        backButton = (LinearLayout) findViewById(R.id.select_store_back);
        switchLayout = (LinearLayout) findViewById(R.id.select_store_switch);
        switchAreaTextView = (TextView) findViewById(R.id.select_store_switch_area);
        switchLocateTextView = (TextView) findViewById(R.id.select_store_switch_locate);
        initViews();
        registerViews();
    }

    @Override
    protected void initViews() {
        if (Content.getIntContent(Parameters.CACHE_KEY_STORE_TYPE, Parameters.CACHE_VALUE_STORE_TYPE_LOCATED) == Parameters.CACHE_VALUE_STORE_TYPE_LOCATED) {
            switchFragment(2);
        } else {
            switchFragment(1);
        }
    }

    @Override
    protected void registerViews() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        switchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type == 1) {
                    switchFragment(2);
                } else {
                    switchFragment(1);
                }
            }
        });
    }

    private void switchFragment(int i) {
        if (i == type) {
            return;
        }
        type = i;
        if (type == 1) {
            switchAreaTextView.setTextColor(getResources().getColor(R.color.white));
            switchAreaTextView.setBackgroundResource(R.drawable.switch_select_left);
            switchLocateTextView.setTextColor(getResources().getColor(R.color.textColorThird));
            switchLocateTextView.setBackgroundResource(android.R.color.transparent);
            getSupportFragmentManager().beginTransaction().replace(R.id.select_store_fragment, new SelectStoreByAreaFragment()).commit();
        } else {
            switchLocateTextView.setTextColor(getResources().getColor(R.color.white));
            switchLocateTextView.setBackgroundResource(R.drawable.switch_select_right);
            switchAreaTextView.setTextColor(getResources().getColor(R.color.textColorThird));
            switchAreaTextView.setBackgroundResource(android.R.color.transparent);
            getSupportFragmentManager().beginTransaction().replace(R.id.select_store_fragment, new SelectStoreByLocateFragment()).commit();
        }
    }
}
