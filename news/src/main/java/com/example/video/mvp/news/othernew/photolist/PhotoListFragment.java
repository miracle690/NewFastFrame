package com.example.video.mvp.news.othernew.photolist;

import android.view.View;

import com.example.commonlibrary.BaseFragment;
import com.example.commonlibrary.baseadapter.SuperRecyclerView;
import com.example.commonlibrary.baseadapter.empty.EmptyLayout;
import com.example.commonlibrary.baseadapter.foot.LoadMoreFooterView;
import com.example.commonlibrary.baseadapter.foot.OnLoadMoreListener;
import com.example.commonlibrary.baseadapter.listener.OnSimpleItemClickListener;
import com.example.commonlibrary.baseadapter.manager.WrappedGridLayoutManager;
import com.example.commonlibrary.cusotomview.GridSpaceDecoration;
import com.example.commonlibrary.cusotomview.swipe.CustomSwipeRefreshLayout;
import com.example.commonlibrary.router.Router;
import com.example.commonlibrary.router.RouterRequest;
import com.example.commonlibrary.utils.Constant;
import com.example.commonlibrary.utils.DensityUtil;
import com.example.video.NewsApplication;
import com.example.video.R;
import com.example.video.adapter.PhotoListAdapter;
import com.example.video.bean.PictureBean;
import com.example.video.dagger.news.othernews.photolist.DaggerPhotoListComponent;
import com.example.video.dagger.news.othernews.photolist.PhotoListModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * 项目名称:    NewFastFrame
 * 创建人:        陈锦军
 * 创建时间:    2017/9/29      17:44
 * QQ:             1981367757
 */

public class PhotoListFragment extends BaseFragment<PictureBean, PhotoListPresenter> implements CustomSwipeRefreshLayout.OnRefreshListener, OnLoadMoreListener {
    @Inject
    PhotoListAdapter photoListAdapter;
    private CustomSwipeRefreshLayout refresh;
    private SuperRecyclerView display;

    @Override
    public void updateData(PictureBean pictureBean) {
        if (refresh.isRefreshing()) {
            photoListAdapter.refreshData(pictureBean.getResults());
        } else {
            photoListAdapter.addData(pictureBean.getResults());
        }
    }

    @Override
    protected boolean isNeedHeadLayout() {
        return false;
    }

    @Override
    protected boolean isNeedEmptyLayout() {
        return true;
    }


    @Override
    protected boolean needStatusPadding() {
        return false;
    }

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_photo_list;
    }

    @Override
    protected void initView() {
        display = findViewById(R.id.srcv_fragment_photo_list_display);
        refresh = findViewById(R.id.refresh_fragment_photo_list_refresh);
    }

    @Override
    protected void initData() {
        DaggerPhotoListComponent.builder().photoListModule(new PhotoListModule(this))
                .newsComponent(NewsApplication.getNewsComponent())
                .build().inject(this);
        display.setLayoutManager(new WrappedGridLayoutManager(getContext(), 2));
        display.addItemDecoration(new GridSpaceDecoration(2, DensityUtil.toDp(10), true));
        refresh.setOnRefreshListener(this);
        display.setLoadMoreFooterView(new LoadMoreFooterView(getContext()));
        display.setOnLoadMoreListener(this);
        photoListAdapter.setOnItemClickListener(new OnSimpleItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                List<PictureBean.PictureEntity> imageList = photoListAdapter.getData();
                if (imageList != null && imageList.size() > 0) {
                    List<String> result = new ArrayList<>();
                    for (PictureBean.PictureEntity item :
                            imageList) {
                        result.add(item.getUrl());
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put(Constant.POSITION, position);
                    Router.getInstance().deal(new RouterRequest.Builder()
                            .provideName("chat").actionName("preview")
                            .context(view.getContext())
                            .paramMap(map).object(result).build());
                }
            }
        });
        display.setAdapter(photoListAdapter);

    }

    @Override
    protected void updateView() {
        presenter.getPhotoListData(true, true);
    }

    @Override
    public void onRefresh() {
        presenter.getPhotoListData(false, true);
    }

    @Override
    public void loadMore() {
        presenter.getPhotoListData(false, false);
    }


    @Override
    public void hideLoading() {
        super.hideLoading();
        refresh.setRefreshing(false);
    }


    @Override
    public void showError(String errorMsg, EmptyLayout.OnRetryListener listener) {
        if (refresh.isRefreshing()) {
            super.showError(errorMsg, listener);
            refresh.setRefreshing(false);
        } else {
            ((LoadMoreFooterView) display.getLoadMoreFooterView()).setStatus(LoadMoreFooterView.Status.ERROR);
        }
    }

    public static PhotoListFragment newInstance() {
        return new PhotoListFragment();
    }
}