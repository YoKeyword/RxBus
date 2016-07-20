package me.yokeyword.rxbus;

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
import android.widget.CompoundButton;
import android.widget.TextView;

import me.yokeyword.rxbus.event.Event;
import me.yokeyword.rxbus.event.EventSticky;
import me.yokeyword.rxbus.rx.RxBus;
import me.yokeyword.rxbus.rx.RxSubscriptions;
import me.yokeyword.rxbus.util.TUtil;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.functions.Func2;

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
                .subscribe(
                        new Subscriber<Event>() {
                            @Override
                            public void onCompleted() {
                                Log.i(TAG, "onCompleted");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onError");
                                e.printStackTrace();

                                /**
                                 * 这里注意: 一旦订阅过程中发生异常,走到onError,则代表此次订阅事件完成,后续将收不到onNext()事件,
                                 * 即 接受不到后续的任何事件,实际环境中,我们需要在onError里 重新订阅事件!
                                 */
                                subscribeEvent();
                            }

                            @Override
                            public void onNext(Event myEvent) {
                                Log.i(TAG, "onNext--->" + myEvent.event);
                                String str = mTvResult.getText().toString();
                                mTvResult.setText(TextUtils.isEmpty(str) ? String.valueOf(myEvent.event) : str + ", " + myEvent.event);

                                // 这里模拟产生 Error
                                if (mCheckBox.isChecked()) {
                                    myEvent = null;
                                    int error = myEvent.event;
                                }
                            }
                        }

                );
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
                    .subscribe(
                            new Subscriber<EventSticky>() {
                                @Override
                                public void onCompleted() {
                                    Log.i(TAG, "onCompleted--Sticky");
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e(TAG, "onError--Sticky");
                                    e.printStackTrace();
                                    /**
                                     * 这里注意: Sticky事件 不能在onError时重绑事件,这可能导致因绑定时得到引起Error的Sticky数据而产生死循环
                                     */
                                }

                                @Override
                                public void onNext(EventSticky myEvent) {
                                    // 为了避免走onError导致结束了订阅事件, onNext内要try,catch
                                    try {
                                        Log.i(TAG, "onNext--Sticky-->" + myEvent.event);

                                        String str = mTvResultSticky.getText().toString();
                                        mTvResultSticky.setText(TextUtils.isEmpty(str) ? String.valueOf(myEvent.event) : str + ", " + myEvent.event);

                                        // 这里模拟产生 Error
                                        if (mCheckBox.isChecked()) {
                                            myEvent = null;
                                            String error = myEvent.event;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        TUtil.showShort(getActivity(), R.string.sticky);
                                    }
                                }
                            }

                    );
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
