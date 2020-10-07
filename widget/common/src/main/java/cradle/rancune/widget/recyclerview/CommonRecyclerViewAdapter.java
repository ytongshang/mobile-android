package cradle.rancune.widget.recyclerview;

import android.text.SpannableStringBuilder;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Rancune@126.com on 2016/4/30.
 */
public abstract class CommonRecyclerViewAdapter<T> extends RecyclerView.Adapter<CommonRecyclerViewAdapter.ViewHolder> {
    private List<T> data;
    private int itemLayoutId;
    private OnItemClickListener clickListener;

    public CommonRecyclerViewAdapter(List<T> data, @LayoutRes int itemLayoutId, OnItemClickListener clickListener) {
        if (data == null) {
            this.data = new ArrayList<>();
        } else {
            this.data = data;
        }
        this.itemLayoutId = itemLayoutId;
        this.clickListener = clickListener;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(getItemLayoutId(viewType), parent, false);
        return new ViewHolder(v, clickListener);
    }


    protected int getItemLayoutId(int viewType) {
        return itemLayoutId;
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
        T item = data.get(position);
        bind(holder, item, position);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        onBindViewHolder(holder, position);
    }

    /**
     * 添加这个方法主要是为了能根据 position 单独刷新一个 item
     * 如果重写这个方法，那么{@link #bind(ViewHolder, Object)} 可以写个空实现
     */
    public void bind(ViewHolder holder, T item, int position) {
        bind(holder, item);
    }

    public abstract void bind(@NonNull ViewHolder holder, @NonNull T item);

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private SparseArray<View> views;
        private OnItemClickListener clickListener;

        public ViewHolder(View itemView, OnItemClickListener clickListener) {
            super(itemView);
            this.clickListener = clickListener;
            views = new SparseArray<>();
            itemView.setOnClickListener(this);
        }

        public <V extends View> V getView(int viewId) {
            View view = views.get(viewId);
            if (view == null) {
                view = itemView.findViewById(viewId);
                views.put(viewId, view);
            }
            //noinspection unchecked
            return (V) view;
        }

        /**
         * 设置文字内容
         */
        public ViewHolder setText(int viewId, CharSequence text) {
            TextView view = getView(viewId);
            if (null != view) {
                view.setText(text);
            }
            return this;
        }

        public ViewHolder setText(int viewId, SpannableStringBuilder text) {
            TextView view = getView(viewId);
            if (null != view) {
                view.setText(text);
            }
            return this;
        }

        public ViewHolder setText(int viewId, @StringRes int textRes) {
            TextView view = getView(viewId);
            if (null != view) {
                view.setText(textRes);
            }
            return this;
        }

        public ViewHolder setTextColor(int viewId, @ColorInt int color) {
            TextView view = getView(viewId);
            if (null != view) {
                view.setTextColor(color);
            }
            return this;
        }

        public ViewHolder setImageResource(int viewId, @DrawableRes int drawableRes) {
            ImageView view = getView(viewId);
            if (null != view) {
                view.setImageResource(drawableRes);
            }
            return this;
        }

        public ViewHolder setVisible(boolean visible, int... ids) {
            for (int id : ids) {
                View v = getView(id);
                if (null != v) {
                    v.setVisibility(visible ? View.VISIBLE : View.GONE);
                }
            }
            return this;
        }

        /**
         * 将viewholder中的某些view的点击事件交给adapter统一处理
         */
        public ViewHolder setOnClickListener(int... ids) {
            for (int i : ids) {
                View v = getView(i);
                if (null != v) {
                    v.setOnClickListener(this);
                }
            }
            return this;
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(v, getLayoutPosition());
        }
    }
}
