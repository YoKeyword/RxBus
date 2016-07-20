package me.yokeyword.rxbusdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import me.yokeyword.rxbus.RxBus;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            SubscriberFragment subscriberFragment = SubscriberFragment.newInstance();
            ObservableFragment observableFragment = ObservableFragment.newInstance();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fl_subscriber, subscriberFragment)
                    .add(R.id.fl_observable, observableFragment)
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 移除所有Sticky事件
        RxBus.getDefault().removeAllStickyEvents();
    }
}
