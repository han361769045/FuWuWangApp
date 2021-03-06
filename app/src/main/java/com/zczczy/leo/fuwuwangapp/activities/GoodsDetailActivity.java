package com.zczczy.leo.fuwuwangapp.activities;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.squareup.otto.Subscribe;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.modelpay.PayResp;
import com.zczczy.leo.fuwuwangapp.R;
import com.zczczy.leo.fuwuwangapp.fragments.GoodsFragment;
import com.zczczy.leo.fuwuwangapp.fragments.GoodsDetailFragment;
import com.zczczy.leo.fuwuwangapp.fragments.GoodsDetailFragment_;
import com.zczczy.leo.fuwuwangapp.fragments.GoodsFragment_;
import com.zczczy.leo.fuwuwangapp.items.GoodsCommentsItemView;
import com.zczczy.leo.fuwuwangapp.items.GoodsCommentsItemView_;
import com.zczczy.leo.fuwuwangapp.items.GoodsPropertiesPopup;
import com.zczczy.leo.fuwuwangapp.items.GoodsPropertiesPopup_;
import com.zczczy.leo.fuwuwangapp.listener.OttoBus;
import com.zczczy.leo.fuwuwangapp.model.BaseModel;
import com.zczczy.leo.fuwuwangapp.model.BaseModelJson;
import com.zczczy.leo.fuwuwangapp.model.Goods;
import com.zczczy.leo.fuwuwangapp.model.GoodsCommentsModel;
import com.zczczy.leo.fuwuwangapp.model.GoodsImgListModel;
import com.zczczy.leo.fuwuwangapp.model.PagerResult;
import com.zczczy.leo.fuwuwangapp.rest.MyDotNetRestClient;
import com.zczczy.leo.fuwuwangapp.rest.MyErrorHandler;
import com.zczczy.leo.fuwuwangapp.tools.AndroidTool;
import com.zczczy.leo.fuwuwangapp.tools.Constants;
import com.zczczy.leo.fuwuwangapp.viewgroup.MyTitleBar;
import com.zczczy.leo.fuwuwangapp.views.GlideSliderView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.rest.spring.annotations.RestService;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * @author Created by LuLeo on 2016/7/4.
 *         you can contact me at :361769045@qq.com
 * @since 2016/7/4.
 */
@EActivity(R.layout.activity_goods_detail)
public class GoodsDetailActivity extends BaseActivity implements BaseSliderView.OnSliderClickListener {

    @ViewById
    MyTitleBar myTitleBar;

    @ViewById
    TextView txt_goods_name, txt_rebate,
            txt_store_name, txt_rmb,
            txt_plus, txt_home_lb,
            goods_count, goods_sell_count, goods_by;

    @ViewById
    ImageView txt_coupon, img_btn_share;

    @Bean
    MyErrorHandler myErrorHandler;

    @RestService
    MyDotNetRestClient myRestClient;

    @ViewById
    RadioButton rb_good_detail, rb_good_suggest;

    @Extra
    String goodsId;

    @StringRes
    String home_rmb, home_lb, goods_no_store;

    @ViewById
    RelativeLayout parent;

    @ViewById
    LinearLayout ll_goods_by, ll_review;

    @ViewById
    SliderLayout sliderLayout;

    @Bean
    OttoBus bus;

    SendMessageToWX.Req req;

    AlertDialog ad = null;

    Goods goods;

    boolean isCanBuy;

    boolean isStart;

    FragmentManager fragmentManager;

    GoodsDetailFragment goodsDetailFragment;

    GoodsFragment goodsCommentsFragment;

    PopupWindow popupWindow;

    GoodsPropertiesPopup goodsPropertiesPopup;

    String linkUrl, PlUrl;

    @AfterInject
    void afterInject() {
        myRestClient.setRestErrorHandler(myErrorHandler);
        fragmentManager = getSupportFragmentManager();
    }

    @AfterViews
    void afterView() {
        AndroidTool.showLoadDialog(this);
        goodsPropertiesPopup = GoodsPropertiesPopup_.build(this);
        myTitleBar.setRightButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkUserIsLogin()) {
                    CartActivity_.intent(GoodsDetailActivity.this).start();
                } else {
                    LoginActivity_.intent(GoodsDetailActivity.this).start();
                }
            }
        });
        getGoodsDetailById(goodsId);
    }

    @CheckedChange
    void rb_good_detail(boolean isChecked) {
        if (isChecked) {
            changeFragment(linkUrl);
        } else {
            changeFragment(goodsId);
        }
    }


    void changeFragment(String parameter) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (goodsDetailFragment != null) {
            transaction.hide(goodsDetailFragment);
        }
        if (goodsCommentsFragment != null) {
            transaction.hide(goodsCommentsFragment);
        }
        if (rb_good_detail.isChecked()) {
            if (goodsDetailFragment == null) {
                goodsDetailFragment = GoodsDetailFragment_.builder().linkUrl(parameter).build();
                transaction.add(R.id.goods_detail_fragment, goodsDetailFragment);
            } else {
                transaction.show(goodsDetailFragment);
            }
        } else {
            if (goodsCommentsFragment == null) {
                goodsCommentsFragment = GoodsFragment_.builder().goods(goods).build();
                transaction.add(R.id.goods_detail_fragment, goodsCommentsFragment);
            } else {
                transaction.show(goodsCommentsFragment);
            }
        }
        //transaction.commit();
        transaction.commitAllowingStateLoss();
    }

    @Background
    void getGoodsDetailById(String goodsInfoId) {
        afterGetGoodsDetailById(myRestClient.getGoodsDetailById(goodsInfoId));
    }

    @UiThread
    void afterGetGoodsDetailById(BaseModelJson<Goods> bmj) {
        AndroidTool.dismissLoadDialog();
        if (bmj == null) {
            AndroidTool.showToast(this, no_net);
        } else if (!bmj.Successful) {
            AndroidTool.showToast(this, bmj.Error);
        } else {
            goods = bmj.Data;
            if (Constants.GOODS_STATE_UP.equals(bmj.Data.GoodsDelStatus) && Constants.GOODS_STATE_PASS.equals(bmj.Data.GoodsCheckStatus)) {
                txt_goods_name.setText(bmj.Data.GodosName);
                //判断库存是否大于0
                isCanBuy = Integer.valueOf(bmj.Data.GoodsStock) > 0;
                goods_count.setText(isCanBuy ? bmj.Data.GoodsStock : "0");
                txt_store_name.setText(bmj.Data.StoreInfoName);
                goods_sell_count.setText(String.valueOf(bmj.Data.GoodsXl));
                ll_goods_by.setVisibility(("1".equals(bmj.Data.GoodsType)) ? View.GONE : View.VISIBLE);
                goods_by.setText(bmj.Data.GoodsIsBy);
                ll_goods_by.setVisibility(("1".equals(bmj.Data.GoodsType)) ? View.GONE : View.VISIBLE);
                goods_by.setText(bmj.Data.GoodsIsBy);

                if ("0".equals(bmj.Data.GoodsReturnTicket)) {
                    txt_coupon.setSelected(false);
                    txt_rebate.setText("");
                } else {
                    txt_rebate.setText(bmj.Data.TempDisp);
                    txt_coupon.setSelected(true);
                }

                PlUrl = goodsId;
                changeFragment(bmj.Data.StaticHtmlUrl);
                if (Float.valueOf(bmj.Data.GoodsPrice) > 0 && Integer.valueOf(bmj.Data.GoodsLBPrice) > 0) {
                    txt_rmb.setVisibility(View.VISIBLE);
                    txt_plus.setVisibility(View.VISIBLE);
                    txt_rmb.setText(String.format(home_rmb, bmj.Data.GoodsPrice));
                    txt_home_lb.setText(String.format(home_lb, bmj.Data.GoodsLBPrice));
                } else if (Float.valueOf(bmj.Data.GoodsPrice) > 0) {
                    txt_rmb.setVisibility(View.VISIBLE);
                    txt_plus.setVisibility(View.GONE);
                    txt_home_lb.setVisibility(View.GONE);
                    txt_rmb.setText(String.format(home_rmb, bmj.Data.GoodsPrice));
                } else if (Integer.valueOf(bmj.Data.GoodsLBPrice) > 0) {
                    txt_rmb.setVisibility(View.GONE);
                    txt_plus.setVisibility(View.GONE);
                    txt_home_lb.setVisibility(View.VISIBLE);
                    txt_home_lb.setText(String.format(home_lb, bmj.Data.GoodsLBPrice));
                }
                for (GoodsImgListModel nb : bmj.Data.GoodsImgList) {
                    GlideSliderView textSliderView = new GlideSliderView(this);
                    textSliderView.image(nb.GoodsImgUrl).
                            empty(R.drawable.goods_detail_banner).
                            error(R.drawable.goods_detail_banner).
                            setScaleType(BaseSliderView.ScaleType.CenterInside).
                            setOnSliderClickListener(this);
                    sliderLayout.addSlider(textSliderView);
                }
                if (bmj.Data.GoodsImgList == null || bmj.Data.GoodsImgList.size() == 0) {
                    isStart = false;
                    sliderLayout.stopAutoCycle();
                    sliderLayout.setBackgroundResource(R.drawable.goods_detail_banner);

                } else if (bmj.Data.GoodsImgList.size() == 1) {
                    isStart = false;
                    sliderLayout.stopAutoCycle();
                } else {
                    isStart = true;
                    sliderLayout.startAutoCycle();
                }
                if (StringUtils.isEmpty(bmj.Data.ShareGoodsLink) || bmj.Data.ShareGoodsLink.contains("#")) {
                    img_btn_share.setVisibility(View.GONE);
                } else {
                    img_btn_share.setVisibility(View.VISIBLE);
                }

                for (GoodsCommentsModel gcm : bmj.Data.GoodsCommentsList) {
                    GoodsCommentsItemView goodsCommentsItemView = GoodsCommentsItemView_.build(this);
                    goodsCommentsItemView.init(gcm);
                    ll_review.addView(goodsCommentsItemView);
                    ll_review.addView(layoutInflater.inflate(R.layout.horizontal_line, null));
                }
            }
        }
    }

    @Click
    @Background
    void img_btn_share() {
        if (req == null) {
            WXWebpageObject webpage = new WXWebpageObject();
            webpage.webpageUrl = goods.ShareGoodsLink;
            WXMediaMessage msg = new WXMediaMessage(webpage);
            msg.title = goods.GodosName;
            msg.description = goods.TempDisp;
            try {
                if (!StringUtils.isEmpty(goods.GoodsImgSl))
                    msg.thumbData = Glide.with(this)
                            .load(goods.GoodsImgSl)
                            .asBitmap()
                            .toBytes(Bitmap.CompressFormat.PNG, 100)
                            .into(100, 100).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            req = new SendMessageToWX.Req();
            req.transaction = buildTransaction("webpage");
            req.message = msg;
        }
        send();
    }

    @UiThread
    void send() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        View view = layoutInflater.inflate(R.layout.share, null);
        adb.setView(view);
        view.findViewById(R.id.img_friend);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                req.scene = SendMessageToWX.Req.WXSceneSession;
                app.iWXApi.sendReq(req);
                ad.dismiss();
            }
        });
        view.findViewById(R.id.img_timeline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                req.scene = SendMessageToWX.Req.WXSceneTimeline;
                app.iWXApi.sendReq(req);
                ad.dismiss();
            }
        });
        ad = adb.setPositiveButton("分享到朋友圈", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                req.scene = SendMessageToWX.Req.WXSceneTimeline;
                app.iWXApi.sendReq(req);
            }
        }).setNeutralButton("分享给好友", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                req.scene = SendMessageToWX.Req.WXSceneSession;
                app.iWXApi.sendReq(req);
            }
        }).create();
        ad.show();
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    @Subscribe
    public void NotifyUI(PayResp resp) {
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                AndroidTool.showToast(this, "分享成功");
                break;
            case BaseResp.ErrCode.ERR_COMM:
                AndroidTool.showToast(this, "分享异常");
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                AndroidTool.showToast(this, "您取消了分享");
                break;
        }
    }

    @Click
    void txt_add_cart() {
        if (!checkUserIsLogin()) {
            LoginActivity_.intent(this).start();
        } else {
            if (goods != null && goods.IsUsing == 1 && goods.GoodsAttributeList != null && goods.GoodsAttributeList.size() > 0) {
                showProperties(true);
            } else {
                AndroidTool.showLoadDialog(this);
                addShoppingCart();
            }
        }
    }

    @Click
    void txt_buy() {
        if (!checkUserIsLogin()) {
            LoginActivity_.intent(this).start();
        } else {
            if (goods != null && goods.IsUsing == 1 && goods.GoodsAttributeList != null && goods.GoodsAttributeList.size() > 0) {
                showProperties(false);
            } else {
                AndroidTool.showLoadDialog(this);
                PreOrderActivity_.intent(this).goodsInfoId(goodsId).StoreInfoId(goods.StoreInfoId).GoodsAttributeId(0).orderCount(1).start();
            }
        }
    }

    /**
     * 添加商品
     *
     * @param
     */
    @Background
    void addShoppingCart() {
        myRestClient.setHeader("Token", pre.token().get());
        myRestClient.setHeader("ShopToken", pre.shopToken().get());
        myRestClient.setHeader("Kbn", Constants.ANDROID);
        HashMap<String, String> map = new HashMap<>();
        map.put("GoodsInfoId", goods.GoodsInfoId);
        map.put("GoodsAttributeId", "0");
        map.put("SelCount", "1");
        afterAddShoppingCart(myRestClient.addShoppingCart(map));
    }

    /**
     * 添加商品后更新UI
     *
     * @param bm
     */
    @UiThread
    void afterAddShoppingCart(BaseModel bm) {
        AndroidTool.dismissLoadDialog();
        if (bm == null) {
            AndroidTool.showToast(this, "商品添加失败");
        } else if (bm.Successful) {
            AndroidTool.showToast(this, "商品添加成功");
        } else {
            AndroidTool.showToast(this, bm.Error);
        }
    }

    void showProperties(boolean isCart) {
        if (popupWindow == null) {
            popupWindow = new PopupWindow(goodsPropertiesPopup, ViewGroup.LayoutParams.MATCH_PARENT, parent.getHeight(), true);
            goodsPropertiesPopup.setData(popupWindow, goods);
            //实例化一个ColorDrawable颜色为半透明
            ColorDrawable dw = new ColorDrawable(0xb0000000);
            //设置SelectPicPopupWindow弹出窗体的背景
            popupWindow.setBackgroundDrawable(dw);
        }
        goodsPropertiesPopup.setCart(isCart);
        popupWindow.showAtLocation(parent, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    @Click
    void ll_review() {
        GoodsCommentsActivity_.intent(this).goodsId(goodsId).start();
    }

    @Click
    void txt_store() {
        if (goods != null) {
            StoreInformationActivity_.intent(this).storeId(goods.StoreInfoId).start();
        }
    }

    @Override
    public void onPause() {
        sliderLayout.stopAutoCycle();
        super.onPause();
        bus.unregister(this);
    }

    @Override
    public void finish() {
        sliderLayout.removeAllSliders();
        sliderLayout.stopAutoCycle();
        super.finish();
    }

    @Override
    public void onResume() {
        if (isStart && sliderLayout != null) {
            sliderLayout.startAutoCycle();
        }
        super.onResume();
        bus.register(this);
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }
}
