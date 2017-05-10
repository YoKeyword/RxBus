package me.yokeyword.rxbusdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import me.yokeyword.rxbus.RxBus;
import me.yokeyword.rxbus.RxBusSubscriber;
import me.yokeyword.rxbusdemo.event.Event;
import me.yokeyword.rxbusdemo.event.EventSticky;
import me.yokeyword.rxbusdemo.helper.RxSubscriptions;
import me.yokeyword.rxbusdemo.helper.TUtil;
import rx.Subscription;
import rx.functions.Func1;

/**
 * 观察者(订阅者)
 */
public class SubscriberFragment extends Fragment {
    private static final String TAG = "RxBus";

    private TextView mTvResult, mTvResultSticky;
    private Button mBtnSubscribeSticky;
    private CheckBox mCheckBox;

    private Subscription mRxSub, mRxSubSticky;

    public static SubscriberFragment newInstance() {
        return new SubscriberFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subscriber, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mTvResult = (TextView) view.findViewById(R.id.tv_result);
        mTvResultSticky = (TextView) view.findViewById(R.id.tv_resultSticky);
        mBtnSubscribeSticky = (Button) view.findViewById(R.id.btn_subscribeSticky);
        mCheckBox = (CheckBox) view.findViewById(R.id.checkbox);

        // 订阅普通RxBus事件
        subscribeEvent();
        TUtil.showShort(getActivity(), R.string.rxbus);

        mBtnSubscribeSticky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 订阅Sticky事件
                subscribeEventSticky();
            }
        });
    }

    private void subscribeEvent() {
        RxSubscriptions.remove(mRxSub);
        mRxSub = RxBus.getDefault().toObservable(Event.class)
                .map(new Func1<Event, Event>() {
                    @Override
                    public Event call(Event event) {
                        // 变换等操作
                        return event;
                    }
                })
                .subscribe(new RxBusSubscriber<Event>() {
                    @Override
                    protected void onEvent(Event myEvent) {
                        Log.i(TAG, "onNext--->" + myEvent.event);
                        String str = mTvResult.getText().toString();
                        mTvResult.setText(TextUtils.isEmpty(str) ? String.valueOf(myEvent.event) : str + ", " + myEvent.event);

                        // 这里模拟产生 Error
                        if (mCheckBox.isChecked()) {
                            throw new RuntimeException("模拟异常");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.e(TAG, "onError");
                        /**
                         * 这里注意: 一旦订阅过程中发生异常,走到onError,则代表此次订阅事件完成,后续将收不到onNext()事件,
                         * 即 接受不到后续的任何事件,实际环境中,我们需要在onError里 重新订阅事件!
                         */
                        subscribeEvent();
                    }
                });
        RxSubscriptions.add(mRxSub);

        TUtil.showShort(getActivity(), R.string.resubscribe);
    }


    private void subscribeEventSticky() {
        if (mRxSubSticky != null && !mRxSubSticky.isUnsubscribed()) {
            mTvResultSticky.setText("");
            RxSubscriptions.remove(mRxSubSticky);

            mBtnSubscribeSticky.setText(R.string.subscribeSticky);
            TUtil.showShort(getActivity(), R.string.unsubscribeSticky);
        } else {
            EventSticky s = RxBus.getDefault().getStickyEvent(EventSticky.class);
            Log.i(TAG, "获取到StickyEvent--->" + s);

            mRxSubSticky = RxBus.getDefault().toObservableSticky(EventSticky.class)
                    // 建议在Sticky时,在操作符内主动try,catch
                    .map(new Func1<EventSticky, EventSticky>() {
                        @Override
                        public EventSticky call(EventSticky eventSticky) {
                            try {
                                // 变换操作
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return eventSticky;
                        }
                    })
                    .subscribe(new RxBusSubscriber<EventSticky>() {
                        @Override
                        protected void onEvent(EventSticky eventSticky) {
                            Log.i(TAG, "onNext--Sticky-->" + eventSticky.event);

                            String str = mTvResultSticky.getText().toString();
                            mTvResultSticky.setText(TextUtils.isEmpty(str) ? String.valueOf(eventSticky.event) : str + ", " + eventSticky.event);

                            // 这里模拟产生 Error
                            if (mCheckBox.isChecked()) {
                                throw new RuntimeException("模拟异常");
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                            Log.e(TAG, "onError--Sticky");
                            /**
                             * 这里注意: Sticky事件 不能在onError时重绑事件,这可能导致因绑定时得到引起Error的Sticky数据而产生死循环
                             */
                        }
                    });
            RxSubscriptions.add(mRxSubSticky);

            mBtnSubscribeSticky.setText(R.string.unsubscribeSticky);
            TUtil.showShort(getActivity(), R.string.subscribeSticky);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // 从CompositeSubscription中移除取消订阅事件,防止内存泄漏
        RxSubscriptions.remove(mRxSub);
        RxSubscriptions.remove(mRxSubSticky);
    }
}
