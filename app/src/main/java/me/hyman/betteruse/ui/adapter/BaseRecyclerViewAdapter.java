
package me.hyman.betteruse.ui.adapter;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import me.hyman.betteruse.R;
import me.hyman.betteruse.ui.adapter.animation.AlphaInAnimation;
import me.hyman.betteruse.ui.adapter.animation.BaseAnimation;
import me.hyman.betteruse.ui.adapter.animation.ScaleInAnimation;
import me.hyman.betteruse.ui.adapter.animation.SlideInBottomAnimation;
import me.hyman.betteruse.ui.adapter.animation.SlideInLeftAnimation;
import me.hyman.betteruse.ui.adapter.animation.SlideInRightAnimation;

/**
 * https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 */
public abstract class BaseRecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected static final String TAG = BaseRecyclerViewAdapter.class.getSimpleName();

    private boolean mNextLoadEnable = false;
    private boolean mLoadingMoreEnable = false;
    private boolean mFirstOnlyEnable = true;
    private boolean mOpenAnimationEnable = false;
    private boolean mEmptyEnable;
    private boolean mHeadAndEmptyEnable;
    private Interpolator mInterpolator = new LinearInterpolator();
    private int mDuration = 300;
    private int mLastPosition = -1;

    protected Context mContext;
    protected int mLayoutResId;
    protected LayoutInflater mLayoutInflater;
    protected List<T> mData;
    private int pageSize = -1;

    private View mContentView;
    private View mHeaderView;
    private View mFooterView;
    private View mEmptyView;
    private View mLoadingView;

    protected static final int HEADER_VIEW = 0x00000111;
    protected static final int LOADING_VIEW = 0x00000222;
    protected static final int FOOTER_VIEW = 0x00000333;
    protected static final int EMPTY_VIEW = 0x00000555;

    private OnRecyclerViewItemClickListener onRecyclerViewItemClickListener;
    private OnRecyclerViewItemLongClickListener onRecyclerViewItemLongClickListener;
    private RequestLoadMoreListener mRequestLoadMoreListener;


    @AnimationType
    private BaseAnimation mCustomAnimation;
    private BaseAnimation mSelectAnimation = new AlphaInAnimation();

    @IntDef({ALPHAIN, SCALEIN, SLIDEIN_BOTTOM, SLIDEIN_LEFT, SLIDEIN_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationType { }

    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int ALPHAIN = 0x00000001;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SCALEIN = 0x00000002;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDEIN_BOTTOM = 0x00000003;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDEIN_LEFT = 0x00000004;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDEIN_RIGHT = 0x00000005;


    /**
     * Same as BaseRecyclerViewAdapter#BaseRecyclerViewAdapter(Context,int) but with
     * some initialization data.
     *
     * @param context     The context.
     * @param layoutResId The layout resource id of each item.
     * @param data        A new list is created out of this one to avoid mutable list
     */
    public BaseRecyclerViewAdapter(Context context, int layoutResId, List<T> data) {
        this.mData = data == null ? new ArrayList<T>() : new ArrayList<T>(data);
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        if (layoutResId != 0) {
            this.mLayoutResId = layoutResId;
        }
    }

    public BaseRecyclerViewAdapter(Context context, List<T> data) {
        this(context, 0, data);
    }

    public BaseRecyclerViewAdapter(Context context, View contentView, List<T> data) {
        this(context, 0, data);
        mContentView = contentView;
    }

    public BaseRecyclerViewAdapter(Context context) {
        this(context, null);
    }


    public void setOnLoadMoreListener(RequestLoadMoreListener requestLoadMoreListener) {
        this.mRequestLoadMoreListener = requestLoadMoreListener;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    /**
     * when adapter's data size than pageSize and enable is true,the loading more function is enable,or disable
     *
     * @param pageSize
     * @param enable
     */
    public void openLoadMore(int pageSize, boolean enable) {
        this.pageSize = pageSize;
        mNextLoadEnable = enable;
    }

    /**
     * call the method before you should call setPageSize() method to setting up the enablePagerSize value,whether it will  invalid
     * enable the loading more data function if enable's value is true,or disable
     *
     * @param enable
     */
    public void openLoadMore(boolean enable) {
        mNextLoadEnable = enable;
    }

    /**
     * setting up the size to decide the loading more data function whether enable
     * enable if the data size than pageSize,or disable
     *
     * @param pageSize
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * return the value of pageSize
     *
     * @return
     */
    public int getPageSize() {
        return this.pageSize;
    }

    public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener onRecyclerViewItemClickListener) {
        this.onRecyclerViewItemClickListener = onRecyclerViewItemClickListener;
    }

    public interface OnRecyclerViewItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void setOnRecyclerViewItemLongClickListener(OnRecyclerViewItemLongClickListener onRecyclerViewItemLongClickListener) {
        this.onRecyclerViewItemLongClickListener = onRecyclerViewItemLongClickListener;
    }

    public interface OnRecyclerViewItemLongClickListener {
        public boolean onItemLongClick(View view, int position);
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
            if (mChildClickListener != null)
                mChildClickListener.onItemChildClick(BaseRecyclerViewAdapter.this, v, position - getHeaderViewCount());
        }
    }

    public void remove(int position) {
        mData.remove(position);
        notifyItemRemoved(position);

    }

    public void add(int position, T item) {
        mData.add(position, item);
        notifyItemInserted(position);
    }


    /**
     * setting up a new instance to data;
     *
     * @param data
     */
    public void setNewData(List<T> data) {
        this.mData = data;
        if (mRequestLoadMoreListener != null) {
            mNextLoadEnable = true;
            mFooterView = null;
        }
        notifyDataSetChanged();
    }

    /**
     * additional data;
     *
     * @param data
     */
    public void addData(List<T> data) {
        this.mData.addAll(data);
        notifyDataSetChanged();
    }

    public List getData() {
        return mData;
    }

    public void setLoadingView(View loadingView) {
        this.mLoadingView = loadingView;
    }


    public T getItem(int position) {
        return mData.get(position);
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
    public int getItemCount() {
        int i = isLoadMore() ? 1 : 0;
        int count = mData.size() + i + getHeaderViewCount() + getFooterViewCount();
        mEmptyEnable = false;
        if ((mHeadAndEmptyEnable && getHeaderViewCount() == 1 && count == 1) || count == 0) {
            mEmptyEnable = true;
            count += getEmptyViewCount();
        }
        return count;
    }


    @Override
    public int getItemViewType(int position) {
        if (mHeaderView != null && position == 0) {
            return HEADER_VIEW;
        } else if (mEmptyView != null && getItemCount() == (mHeadAndEmptyEnable ? 2 : 1) && mEmptyEnable) {
            return EMPTY_VIEW;
        } else if (position == mData.size() + getHeaderViewCount()) {
            if (mNextLoadEnable)
                return LOADING_VIEW;
            else
                return FOOTER_VIEW;
        }
        return super.getItemViewType(position);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder baseViewHolder = null;
        switch (viewType) {
            case LOADING_VIEW:
                baseViewHolder = getLoadingView(parent);
                initItemClickListener(baseViewHolder);
                break;
            case HEADER_VIEW:
                baseViewHolder = new BaseViewHolder(mContext, mHeaderView);
                break;
            case EMPTY_VIEW:
                baseViewHolder = new BaseViewHolder(mContext, mEmptyView);
                break;
            case FOOTER_VIEW:
                baseViewHolder = new BaseViewHolder(mContext, mFooterView);
                break;
            default:
                baseViewHolder = createBaseViewHolder(parent, mLayoutResId);
                initItemClickListener(baseViewHolder);
        }
        return baseViewHolder;

    }


    protected BaseViewHolder createBaseViewHolder(ViewGroup parent, int layoutResId) {
        if (mContentView == null) {
            return new BaseViewHolder(mContext, getItemView(layoutResId, parent));
        }
        return new BaseViewHolder(mContext, mContentView);
    }

    private BaseViewHolder getLoadingView(ViewGroup parent) {
        if (mLoadingView == null) {
            return createBaseViewHolder(parent, R.layout.def_loading);
        }
        return new BaseViewHolder(mContext, mLoadingView);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int type = holder.getItemViewType();
        if (type == EMPTY_VIEW || type == HEADER_VIEW || type == FOOTER_VIEW || type == LOADING_VIEW) {
            setFullSpan(holder);
        }
    }

    protected void setFullSpan(RecyclerView.ViewHolder holder) {
        if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            params.setFullSpan(true);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int positions) {

        switch (holder.getItemViewType()) {
            case 0:
                convert((BaseViewHolder) holder, mData.get(holder.getLayoutPosition() - getHeaderViewCount()));
                addAnimation(holder);
                break;
            case LOADING_VIEW:
                addLoadMore(holder);
                break;
            case HEADER_VIEW:
                break;
            case EMPTY_VIEW:
                break;
            case FOOTER_VIEW:
                break;
            default:
                convert((BaseViewHolder) holder, mData.get(holder.getLayoutPosition() - getHeaderViewCount()));
                //onBindDefViewHolder((BaseViewHolder) holder, mData.get(holder.getLayoutPosition() - getHeaderViewCount()));
                break;
        }

    }


    public void addHeaderView(View header) {
        if (header == null) {
            throw new RuntimeException("header is null");
        }
        this.mHeaderView = header;
        this.notifyDataSetChanged();
    }

    public void addFooterView(View footer) {
        mNextLoadEnable = false;
        if (footer == null) {
            throw new RuntimeException("footer is null");
        }
        this.mFooterView = footer;
        this.notifyDataSetChanged();
    }

    /**
     * Sets the view to show if the adapter is empty
     */
    public void setEmptyView(View emptyView) {
        setEmptyView(false, emptyView);
    }

    public void setEmptyView(boolean isHeadAndEmpty, View emptyView) {
        mHeadAndEmptyEnable = isHeadAndEmpty;
        mEmptyView = emptyView;
    }

    /**
     * When the current adapter is empty, the BaseQuickAdapter can display a special view
     * called the empty view. The empty view is used to provide feedback to the user
     * that no data is available in this AdapterView.
     *
     * @return The view to show if the adapter is empty.
     */
    public View getEmptyView() {
        return mEmptyView;
    }

    /**
     * see more {@link  public void notifyDataChangedAfterLoadMore(boolean isNextLoad)}
     *
     * @param isNextLoad
     */
    @Deprecated
    public void isNextLoad(boolean isNextLoad) {
        mNextLoadEnable = isNextLoad;
        mLoadingMoreEnable = false;
        notifyDataSetChanged();

    }

    public void notifyDataChangedAfterLoadMore(boolean isNextLoad) {
        mNextLoadEnable = isNextLoad;
        mLoadingMoreEnable = false;
        notifyDataSetChanged();

    }

    public void notifyDataChangedAfterLoadMore(List<T> data, boolean isNextLoad) {
        mData.addAll(data);
        notifyDataChangedAfterLoadMore(isNextLoad);

    }


    private void addLoadMore(RecyclerView.ViewHolder holder) {
        if (isLoadMore()) {
            mLoadingMoreEnable = true;
            mRequestLoadMoreListener.onLoadMoreRequested();
        }
    }

    private void initItemClickListener(final BaseViewHolder baseViewHolder) {
        if (onRecyclerViewItemClickListener != null) {
            baseViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRecyclerViewItemClickListener.onItemClick(v, baseViewHolder.getLayoutPosition() - getHeaderViewCount());
                }
            });
        }
        if (onRecyclerViewItemLongClickListener != null) {
            baseViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return onRecyclerViewItemLongClickListener.onItemLongClick(v, baseViewHolder.getLayoutPosition() - getHeaderViewCount());
                }
            });
        }
    }

    private void addAnimation(RecyclerView.ViewHolder holder) {
        if (mOpenAnimationEnable) {
            if (!mFirstOnlyEnable || holder.getLayoutPosition() > mLastPosition) {
                BaseAnimation animation = null;
                if (mCustomAnimation != null) {
                    animation = mCustomAnimation;
                } else {
                    animation = mSelectAnimation;
                }
                for (Animator anim : animation.getAnimators(holder.itemView)) {
                    anim.setDuration(mDuration).start();
                    anim.setInterpolator(mInterpolator);
                }
                mLastPosition = holder.getLayoutPosition();
            }
        }
    }

    private boolean isLoadMore() {
        return mNextLoadEnable && pageSize != -1 && !mLoadingMoreEnable && mRequestLoadMoreListener != null && mData.size() >= pageSize;
    }

    protected View getItemView(int layoutResId, ViewGroup parent) {
        return mLayoutInflater.inflate(layoutResId, parent, false);
    }


    public interface RequestLoadMoreListener {
        void onLoadMoreRequested();
    }


    /**
     * Set the view animation type.
     *
     * @param animationType One of {@link #ALPHAIN}, {@link #SCALEIN}, {@link #SLIDEIN_BOTTOM}, {@link #SLIDEIN_LEFT}, {@link #SLIDEIN_RIGHT}.
     */
    public void openLoadAnimation(@AnimationType int animationType) {
        this.mOpenAnimationEnable = true;
        mCustomAnimation = null;
        switch (animationType) {
            case ALPHAIN:
                mSelectAnimation = new AlphaInAnimation();
                break;
            case SCALEIN:
                mSelectAnimation = new ScaleInAnimation();
                break;
            case SLIDEIN_BOTTOM:
                mSelectAnimation = new SlideInBottomAnimation();
                break;
            case SLIDEIN_LEFT:
                mSelectAnimation = new SlideInLeftAnimation();
                break;
            case SLIDEIN_RIGHT:
                mSelectAnimation = new SlideInRightAnimation();
                break;
            default:
                break;
        }
    }

    /**
     * Set Custom ObjectAnimator
     *
     * @param animation ObjectAnimator
     */
    public void openLoadAnimation(BaseAnimation animation) {
        this.mOpenAnimationEnable = true;
        this.mCustomAnimation = animation;
    }

    public void openLoadAnimation() {
        this.mOpenAnimationEnable = true;
    }


    public void isFirstOnly(boolean firstOnly) {
        this.mFirstOnlyEnable = firstOnly;
    }

    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * @param helper A fully initialized helper.
     * @param item   The item that needs to be displayed.
     */
    protected abstract void convert(BaseViewHolder helper, T item);


    @Override
    public long getItemId(int position) {
        return position;
    }


}
