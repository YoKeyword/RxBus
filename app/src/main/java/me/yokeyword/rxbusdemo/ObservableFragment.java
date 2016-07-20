package me.yokeyword.rxbusdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import me.yokeyword.rxbus.RxBus;
import me.yokeyword.rxbusdemo.event.Event;
import me.yokeyword.rxbusdemo.event.EventSticky;

/**
 * 被观察者
 */
public class ObservableFragment extends Fragment {
    private int mCountNum, mCountStickyNum;
    private Button mBtnPost, mBtnPostSticky;
    private TextView mTvPost, mTvPostSticky;

    public static ObservableFragment newInstance() {
        return new ObservableFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_observable, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mBtnPost = (Button) view.findViewById(R.id.btn_post);
        mTvPost = (TextView) view.findViewById(R.id.tv_post);
        mBtnPostSticky = (Button) view.findViewById(R.id.btn_postSticky);
        mTvPostSticky = (TextView) view.findViewById(R.id.tv_postSticky);


        mBtnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxBus.getDefault().post(new Event(++mCountNum));

                String str = mTvPost.getText().toString();
                mTvPost.setText(TextUtils.isEmpty(str) ? String.valueOf(mCountNum) : str + ", " + mCountNum);
            }
        });

        mBtnPostSticky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxBus.getDefault().postSticky(new EventSticky(String.valueOf(--mCountStickyNum)));

                String str = mTvPostSticky.getText().toString();
                mTvPostSticky.setText(TextUtils.isEmpty(str) ? String.valueOf(mCountStickyNum) : str + ", " + mCountStickyNum);

            }
        });
    }
}
