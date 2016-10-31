package me.hyman.betteruse.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.hyman.betteruse.R;
import me.hyman.betteruse.support.util.Logger;

/**
 * Created by Hyman on 2016/6/11.
 */
public abstract class BaseLoadingMoreAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = BaseLoadingMoreAdapter.class.getSimpleName();

    private boolean mNextLoadEnable = false;
    private boolean mLoadingMoreEnable = false;
    private boolean mEmptyEnable;
    private boolean mHeadAndEmptyEnable;
    private int mLastPosition = -1;

    protected Context mContext;
    protected LayoutInflater mLayoutInflater;
    protected List<T> mDatas;
    private int pageSize = -1;

    private View mContentView;
    private View mHeaderView;
    private View mFooterView;
    private View mEmptyView;
    private View mLoadingView;

    protected static final int TYPE_HEADER_VIEW = 0;
    protected static final int TYPE_LOADING_VIEW = 1;
    protected static final int TYPE_FOOTER_VIEW = 2;
    protected static final int TYPE_EMPTY_VIEW = 3;
    protected static final int TYPE_CONTENT_VIEW = 4;


    //加载viewHolder
    private LoadingViewHolder mLoadingViewHolder;

    private OnRecyclerViewItemClickListener onRecyclerViewItemClickListener;
    private OnRecyclerViewItemLongClickListener onRecyclerViewItemLongClickListener;
    private RequestLoadMoreListener mRequestLoadMoreListener;


    public BaseLoadingMoreAdapter(Context context) {
        this(context, null);
    }

    public BaseLoadingMoreAdapter(Context context, List<T> data) {
        this.mDatas = data == null ? new ArrayList<T>() : data;
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
    }


    public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener onRecyclerViewItemClickListener) {
        this.onRecyclerViewItemClickListener = onRecyclerViewItemClickListener;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnRecyclerViewItemLongClickListener(OnRecyclerViewItemLongClickListener onRecyclerViewItemLongClickListener) {
        this.onRecyclerViewItemLongClickListener = onRecyclerViewItemLongClickListener;
    }

    public interface OnRecyclerViewItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }

    private OnRecyclerViewItemChildClickListener mChildClickListener;

    public void setOnRecyclerViewItemChildClickListener(OnRecyclerViewItemChildClickListener childClickListener) {
        this.mChildClickListener = childClickListener;
    }

    public interface OnRecyclerViewItemChildClickListener {
        void onItemChildClick(BaseRecyclerViewAdapter adapter, View view, int position);
    }

    public class OnItemChildClickListener implements View.OnClickListener {
        public int position;

        @Override
        public void onClick(View v) {
           /* if (mChildClickListener != null)
                mChildClickListener.onItemChildClick(BaseRecyclerViewAdapter.this, v, position - getHeaderViewCount());*/
        }
    }

    public interface RequestLoadMoreListener {
        void onLoadMoreRequested();
    }

    public void setRequestLoadMoreListener(RequestLoadMoreListener requestLoadMoreListener) {
        this.mRequestLoadMoreListener = requestLoadMoreListener;
    }

    public BaseLoadingMoreAdapter(List<T> mDatas) {
        this.mDatas = mDatas;
        //mRecyclerView = recyclerView;
    }


    public void openLoadMore(int pageSize, boolean enable) {
        this.pageSize = pageSize;
        mNextLoadEnable = enable;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void removeItem(int position) {
        mDatas.remove(position);
        notifyItemRemoved(position);

    }

    public void addItem(int position, T item) {
        mDatas.add(position, item);
        notifyItemInserted(position);
    }

    public T getItem(int position) {
        return mDatas.get(position);
    }

    public void resetData(List<T> data) {
        this.mDatas = data;
        if (mRequestLoadMoreListener != null) {
            mNextLoadEnable = true;
            mFooterView = null;
        }
        notifyDataSetChanged();
    }

    public void addData(List<T> data) {
        this.mDatas.addAll(data);
        notifyDataSetChanged();
    }

    public List getData() {
        return mDatas;
    }

    public int getHeaderViewCount() {
        return mHeaderView == null ? 0 : 1;
    }

    public int getFooterViewCount() {
        return mFooterView == null ? 0 : 1;
    }

    public int getEmptyViewCount() {
        return mEmptyView == null ? 0 : 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //Logger.e("onCreateViewHolder: " + viewType);

        RecyclerView.ViewHolder holder = null;
        switch (viewType) {
            case TYPE_LOADING_VIEW:
                View view = LayoutInflater.from(mContext).inflate(R.layout.def_loading, parent, false);
                holder = mLoadingViewHolder = new LoadingViewHolder(view);
                break;

            case TYPE_HEADER_VIEW:
                mHeaderView = LayoutInflater.from(mContext).inflate(R.layout.def_loading, parent, false);
                holder = new LoadingViewHolder(mHeaderView);
                break;

            case TYPE_FOOTER_VIEW:
                mFooterView = LayoutInflater.from(mContext).inflate(R.layout.def_loading, parent, false);
                holder = new LoadingViewHolder(mFooterView);
                break;

            case TYPE_CONTENT_VIEW:
                holder = onCreateNormalViewHolder(parent);
                break;

            default:
                //holder = onCreateNormalViewHolder(parent);

        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        //Logger.e("onBindViewHolder: " + holder.getItemViewType());

        switch (holder.getItemViewType()) {
            case TYPE_LOADING_VIEW:
                addLoadMore();
                break;

            case TYPE_HEADER_VIEW:
                break;

            case TYPE_FOOTER_VIEW:
                break;

            case TYPE_CONTENT_VIEW:
                onBindNormalViewHolder(holder, position);
                break;

            default:
                //onBindNormalViewHolder(holder, position);
        }
    }

    private void addLoadMore() {
        if(isLoadMore()) {
            mLoadingMoreEnable = true;
            mRequestLoadMoreListener.onLoadMoreRequested();
        }
    }

    private boolean isLoadMore() {
        return mNextLoadEnable && pageSize != -1 && !mLoadingMoreEnable && mRequestLoadMoreListener != null && mDatas.size() >= pageSize;
    }

    @Override
    public int getItemCount() {
        int i = isLoadMore() ? 1 : 0;
        int count = mDatas.size() + i + getHeaderViewCount() + getFooterViewCount();
        mEmptyEnable = false;
        if((mHeadAndEmptyEnable && getHeaderViewCount() == 1 && count == 1) || count == 0) {
            mEmptyEnable = true;
            count += getEmptyViewCount();
        }

        //Logger.e("getItemCount: " + count);

        return count;
    }


    @Override
    public int getItemViewType(int position) {
        if(mHeaderView != null && position == 0) {

            Logger.e("getItemViewType: TYPE_HEADER_VIEW - " + position);

            return TYPE_HEADER_VIEW;
        } else if (mEmptyView != null && getItemCount() == (mHeadAndEmptyEnable ? 2 : 1) && mEmptyEnable) {

            Logger.e("getItemViewType: TYPE_EMPTY_VIEW - " + position);

            return TYPE_EMPTY_VIEW;
        } else if(position == mDatas.size() + getHeaderViewCount()) {
            if(mNextLoadEnable) {

                Logger.e("getItemViewType: TYPE_LOADING_VIEW - " + position);

                return TYPE_LOADING_VIEW;
            } else {

                Logger.e("getItemViewType: TYPE_FOOTER_VIEW - " + position);

                return TYPE_FOOTER_VIEW;
            }
        } else {

            Logger.e("getItemViewType: TYPE_CONTENT_VIEW - " + position);

            return TYPE_CONTENT_VIEW;
        }

        //Logger.e("getItemViewType: super.getItemViewType(position) - " + position);
        //return super.getItemViewType(position);
    }


    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int type = holder.getItemViewType();
        if (type == TYPE_LOADING_VIEW || type == TYPE_HEADER_VIEW || type == TYPE_FOOTER_VIEW || type == TYPE_EMPTY_VIEW) {
            setFullSpan(holder);
        }
    }

    protected void setFullSpan(RecyclerView.ViewHolder holder) {
        if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            params.setFullSpan(true);
        }
    }


    public void setHeaderView(View header) {
        if (header == null) {
            throw new RuntimeException("header is null");
        }
        this.mHeaderView = header;
        this.notifyDataSetChanged();
    }

    public void setFooterView(View footer) {
        mNextLoadEnable = false;
        if (footer == null) {
            throw new RuntimeException("footer is null");
        }
        this.mFooterView = footer;
        this.notifyDataSetChanged();
    }

    public void setEmptyView(View emptyView) {
        setEmptyView(false, emptyView);
    }

    public void setEmptyView(boolean isHeadAndEmpty, View emptyView) {
        mHeadAndEmptyEnable = isHeadAndEmpty;
        mEmptyView = emptyView;
    }

    public View getEmptyView() {
        return mEmptyView;
    }


    /**
     * 创建viewHolder
     *
     * @param parent viewGroup
     * @return viewHolder
     */
    protected abstract RecyclerView.ViewHolder onCreateNormalViewHolder(ViewGroup parent);

    /**
     * 绑定viewHolder
     *
     * @param holder   viewHolder
     * @param position position
     */
    public abstract void onBindNormalViewHolder(RecyclerView.ViewHolder holder, int position);


    /**
     * 正在加载的布局
     */
    private class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        public TextView tvLoading;
        public LinearLayout llyLoading;

        public LoadingViewHolder(View view) {
            super(view);

            progressBar = (ProgressBar) view.findViewById(R.id.loading_progress);
            tvLoading = (TextView) view.findViewById(R.id.loading_text);
            llyLoading = (LinearLayout) view.findViewById(R.id.loading_view);
        }
    }

}
