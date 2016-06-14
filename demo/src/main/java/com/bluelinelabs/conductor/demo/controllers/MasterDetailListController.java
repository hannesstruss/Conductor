package com.bluelinelabs.conductor.demo.controllers;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MasterDetailListController extends BaseController {

    private static final String KEY_SELECTED_INDEX = "MasterDetailListController.selectedIndex";

    public enum DetailItemModel {
        ONE("Item 1", "This is a quick demo of master/detail flow using Conductor. In portrait mode you'll see a standard list. In landscape, you'll see a two-pane layout.", R.color.green_300),
        TWO("Item 2", "This is another item.", R.color.cyan_300),
        THREE("Item 3", "Wow, a 3rd item!", R.color.deep_purple_300);

        String title;
        String detail;
        int backgroundColor;

        DetailItemModel(String title, String detail, int backgroundColor) {
            this.title = title;
            this.detail = detail;
            this.backgroundColor = backgroundColor;
        }
    }

    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @Nullable @BindView(R.id.detail_container) ViewGroup mDetailContainer;

    private int mSelectedIndex;
    private boolean mTwoPaneView;

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_master_detail_list, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.setAdapter(new DetailItemAdapter(LayoutInflater.from(view.getContext()), DetailItemModel.values()));

        mTwoPaneView = (mDetailContainer != null);
        if (mTwoPaneView) {
            onRowSelected(mSelectedIndex);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_SELECTED_INDEX, mSelectedIndex);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mSelectedIndex = savedInstanceState.getInt(KEY_SELECTED_INDEX);
    }

    @Override
    protected String getTitle() {
        return "Master/Detail Flow";
    }

    void onRowSelected(int index) {
        mSelectedIndex = index;

        DetailItemModel model = DetailItemModel.values()[index];
        ChildController controller = new ChildController(model.detail, model.backgroundColor, true);

        if (mTwoPaneView) {
            getChildRouter(mDetailContainer, null).setRoot(RouterTransaction.builder(controller).build());
        } else {
            getRouter().pushController(RouterTransaction.builder(controller)
                    .pushChangeHandler(new HorizontalChangeHandler())
                    .popChangeHandler(new HorizontalChangeHandler())
                    .build());
        }
    }

    class DetailItemAdapter extends RecyclerView.Adapter<DetailItemAdapter.ViewHolder> {

        private final LayoutInflater mInflater;
        private final DetailItemModel[] mItems;

        public DetailItemAdapter(LayoutInflater inflater, DetailItemModel[] items) {
            mInflater = inflater;
            mItems = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(mInflater.inflate(R.layout.row_detail_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(mItems[position], position);
        }

        @Override
        public int getItemCount() {
            return mItems.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.row_root) View mRoot;
            @BindView(R.id.tv_title) TextView mTvTitle;
            private int mPosition;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            void bind(DetailItemModel item, int position) {
                mTvTitle.setText(item.title);
                mPosition = position;

                if (mTwoPaneView && position == mSelectedIndex) {
                    mRoot.setBackgroundColor(ContextCompat.getColor(mRoot.getContext(), R.color.grey_400));
                } else {
                    mRoot.setBackgroundColor(ContextCompat.getColor(mRoot.getContext(), android.R.color.transparent));
                }
            }

            @OnClick(R.id.row_root)
            void onRowClick() {
                onRowSelected(mPosition);
                notifyDataSetChanged();
            }

        }
    }

}
