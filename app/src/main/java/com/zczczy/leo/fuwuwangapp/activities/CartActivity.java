package com.zczczy.leo.fuwuwangapp.activities;

import android.graphics.Paint;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.marshalchen.ultimaterecyclerview.CustomLinearLayoutManager;
import com.marshalchen.ultimaterecyclerview.CustomUltimateRecyclerview;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.divideritemdecoration.FlexibleDividerDecoration;
import com.marshalchen.ultimaterecyclerview.divideritemdecoration.HorizontalDividerItemDecoration;
import com.squareup.otto.Subscribe;
import com.zczczy.leo.fuwuwangapp.MyApplication;
import com.zczczy.leo.fuwuwangapp.R;
import com.zczczy.leo.fuwuwangapp.adapters.BaseUltimateRecyclerViewAdapter;
import com.zczczy.leo.fuwuwangapp.adapters.CartAdapter;
import com.zczczy.leo.fuwuwangapp.listener.OttoBus;
import com.zczczy.leo.fuwuwangapp.model.BaseModel;
import com.zczczy.leo.fuwuwangapp.tools.AndroidTool;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;

/**
 * Created by Leo on 2016/5/1.
 */
@EActivity(R.layout.activity_cart)
public class CartActivity extends BaseActivity {

    @ViewById
    CustomUltimateRecyclerview ultimateRecyclerView;

    @Bean(CartAdapter.class)
    BaseUltimateRecyclerViewAdapter myAdapter;

    @Bean
    OttoBus bus;

    LinearLayoutManager linearLayoutManager;

    MaterialHeader materialHeader;

    int pageIndex = 1;

    boolean isRefresh;

    @AfterViews
    void afterView() {
        bus.register(this);
        ultimateRecyclerView.setHasFixedSize(true);
        linearLayoutManager = new CustomLinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        ultimateRecyclerView.setLayoutManager(linearLayoutManager);
        ultimateRecyclerView.setAdapter(myAdapter);
        ultimateRecyclerView.enableLoadmore();
        myAdapter.setCustomLoadMoreView(R.layout.custom_bottom_progressbar);
        afterLoadMore();
        ultimateRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, int maxLastVisiblePosition) {
                if (myAdapter.getItems().size() >= myAdapter.getTotal()) {
                    AndroidTool.showToast(CartActivity.this, "没有更多的数据了！~");
                    ultimateRecyclerView.disableLoadmore();
//                    myAdapter.notifyItemRemoved(maxLastVisiblePosition);
                } else {
                    pageIndex++;
                    afterLoadMore();
                }
            }
        });
        ultimateRecyclerView.setCustomSwipeToRefresh();
        Paint paint = new Paint();
        paint.setStrokeWidth(1);
        paint.setColor(line_color);
        ultimateRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).margin(35).visibilityProvider(new FlexibleDividerDecoration.VisibilityProvider() {
            @Override
            public boolean shouldHideDivider(int position, RecyclerView parent) {
                return false;
            }
        }).paint(paint).build());
        refreshingMaterial();
    }

    void afterLoadMore() {
        myAdapter.getMoreData(pageIndex, MyApplication.PAGECOUNT, isRefresh);
    }

    void refreshingMaterial() {
        materialHeader = new MaterialHeader(this);
        int[] colors = getResources().getIntArray(R.array.google_colors);
        materialHeader.setColorSchemeColors(colors);
        materialHeader.setLayoutParams(new PtrFrameLayout.LayoutParams(-1, -2));
        materialHeader.setPadding(0, 15, 0, 10);
        materialHeader.setPtrFrameLayout(ultimateRecyclerView.mPtrFrameLayout);
        ultimateRecyclerView.mPtrFrameLayout.autoRefresh(false);
        ultimateRecyclerView.mPtrFrameLayout.setHeaderView(materialHeader);
        ultimateRecyclerView.mPtrFrameLayout.addPtrUIHandler(materialHeader);
        ultimateRecyclerView.mPtrFrameLayout.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                isRefresh = true;
                pageIndex = 1;
                afterLoadMore();
            }
        });
    }

    @Subscribe
    public void notifyUI(BaseModel bm) {
        if (isRefresh) {
            linearLayoutManager.scrollToPosition(0);
            ultimateRecyclerView.mPtrFrameLayout.refreshComplete();
            isRefresh = false;
            if (myAdapter.getTotal() > 0 && myAdapter.getItems().size() < myAdapter.getTotal()) {
                ultimateRecyclerView.reenableLoadmore(layoutInflater.inflate(R.layout.custom_bottom_progressbar, null));
            } else {
                ultimateRecyclerView.disableLoadmore();
            }
        } else if (pageIndex == 1) {
            linearLayoutManager.scrollToPosition(0);
        }
    }
}
