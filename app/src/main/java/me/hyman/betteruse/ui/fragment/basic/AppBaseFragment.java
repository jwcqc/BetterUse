package me.hyman.betteruse.ui.fragment.basic;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.hyman.betteruse.ui.activity.basic.BaseAppCompatActivity;
import me.hyman.betteruse.ui.view.MyToolbar;


public class AppBaseFragment extends Fragment implements MyToolbar.onToolbarDoubleClick {

    static final String TAG = AppBaseFragment.class.getSimpleName();

    public AppBaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(getActivity()!= null && getActivity() instanceof BaseAppCompatActivity) {
            ((BaseAppCompatActivity) getActivity()).addFragment(toString(), this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if(getActivity()!= null && getActivity() instanceof BaseAppCompatActivity) {
            ((BaseAppCompatActivity) getActivity()).removeFragment(toString());
        }
    }

    protected void initToolbar(Toolbar toolbar, String title, int icon, int menuId, Toolbar.OnMenuItemClickListener listener) {
        toolbar.setTitle(title);
        toolbar.inflateMenu(menuId);
        toolbar.setOnMenuItemClickListener(listener);
        BaseAppCompatActivity.getRunningActivity().setSupportActionBar(toolbar);
        ActionBar ab = BaseAppCompatActivity.getRunningActivity().getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(icon);
    }

    @Override
    public boolean onToolbarDoubleClick() {
        return false;
    }
}
