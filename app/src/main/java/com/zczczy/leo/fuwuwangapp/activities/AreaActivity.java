package com.zczczy.leo.fuwuwangapp.activities;

import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.marshalchen.ultimaterecyclerview.divideritemdecoration.HorizontalDividerItemDecoration;
import com.zczczy.leo.fuwuwangapp.R;
import com.zczczy.leo.fuwuwangapp.adapters.AreaAdapter;
import com.zczczy.leo.fuwuwangapp.adapters.BaseRecyclerViewAdapter;
import com.zczczy.leo.fuwuwangapp.model.NewArea;
import com.zczczy.leo.fuwuwangapp.model.NewCity;
import com.zczczy.leo.fuwuwangapp.model.NewProvince;
import com.zczczy.leo.fuwuwangapp.tools.AndroidTool;
import com.zczczy.leo.fuwuwangapp.viewgroup.MyTitleBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Leo on 2016/5/4.
 */
@EActivity(R.layout.activity_province)
public class AreaActivity extends BaseRecyclerViewActivity<NewArea> {

    @Extra
    NewProvince province;

    @Extra
    NewCity city;

    @Bean
    void setAdapter(AreaAdapter myAdapter) {
        this.myAdapter = myAdapter;
    }

    @AfterViews
    void afterView() {
        myTitleBar.setTitle("区域");
        myAdapter.getMoreData(city.CityId);
        myAdapter.setOnItemClickListener(new BaseRecyclerViewAdapter.OnItemClickListener<NewArea>() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder viewHolder, NewArea obj, int position) {
                Intent intent = new Intent();
                intent.putExtra("areaId", Integer.valueOf(obj.AreaId));
                intent.putExtra("pca", province.ProvinceName + city.CityName + obj.AreaName);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }


}
