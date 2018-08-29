package com.moonshot.library.imagedetector.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.moonshot.library.R;
import com.moonshot.library.imagedetector.DetectorInfo;

import java.util.ArrayList;
import java.util.List;

public class AlertListActivity extends Activity {


    private static List<DetectorInfo> sDetectorInfoList = new ArrayList<>();

    private RecyclerView mRecyclerView;

    private Adapter mAdapter;


    public static void launch(Context context, DetectorInfo detectorInfo) {
        if (!sDetectorInfoList.contains(detectorInfo)) {
            sDetectorInfoList.add(detectorInfo);
        }
        Intent intent = new Intent(context, AlertListActivity.class);


        context.startActivity(intent);


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_list);
        init();
    }

    private void init() {
        mRecyclerView = findViewById(R.id.recycler_view);

        mAdapter = new Adapter();
        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter.notifyDataSetChanged();
    }


    class Adapter extends RecyclerView.Adapter<ItemHolder> {

        @NonNull
        @Override
        public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detectinfo, parent, false);
            ItemHolder itemHolder = new ItemHolder(view);
            itemHolder.itemView.setOnClickListener(mOnClickListener);
            return itemHolder;
        }

        private View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int pos = mRecyclerView.getChildAdapterPosition(v);
                if (pos >= 0 && pos < sDetectorInfoList.size()) {

                    // 复制 Url信息
                    DetectorInfo detectorInfo = sDetectorInfoList.get(pos);
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, detectorInfo.mUrl));
                    Toast.makeText(getApplicationContext(), "已复制URL", Toast.LENGTH_SHORT).show();
                }

            }
        };

        @Override
        public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
            DetectorInfo detectorInfo = sDetectorInfoList.get(position);
            holder.tvUrl.setText(detectorInfo.mUrl);
            holder.tvActivity.setText(detectorInfo.mLoadActivity);
            holder.tvInfo.setText("Resource width:" + detectorInfo.mImgWidth + " height:" + detectorInfo.mImgHeight + "\n"
                    + "View width:" + detectorInfo.mViewWidth + " height:" + detectorInfo.mViewHeight);


        }

        @Override
        public int getItemCount() {
            return sDetectorInfoList.size();
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        public TextView tvActivity;
        public TextView tvInfo;
        public TextView tvUrl;

        public ItemHolder(View itemView) {
            super(itemView);
            tvActivity = itemView.findViewById(R.id.tv_activity);
            tvInfo = itemView.findViewById(R.id.tv_info);
            tvUrl = itemView.findViewById(R.id.tv_url);
        }
    }

}
