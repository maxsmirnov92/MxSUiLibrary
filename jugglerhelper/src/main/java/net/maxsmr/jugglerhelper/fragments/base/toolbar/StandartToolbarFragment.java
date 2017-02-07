package net.maxsmr.jugglerhelper.fragments.base.toolbar;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;

import net.maxsmr.jugglerhelper.R;
import net.maxsmr.jugglerhelper.navigation.NavigationMode;

import static net.maxsmr.jugglerhelper.fragments.base.toolbar.BaseCustomJugglerToolbarFragment.ARG_TOOLBAR_LAYOUT_ID;

public class StandartToolbarFragment extends BaseJugglerToolbarFragment {

    public static StandartToolbarFragment newInstance(@NonNull NavigationMode navigationMode) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_NAVIGATION_MODE, navigationMode);
        StandartToolbarFragment fragment = new StandartToolbarFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static StandartToolbarFragment newInstance(@NonNull NavigationMode navigationMode, @LayoutRes int toolbarLayoutId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_NAVIGATION_MODE, navigationMode);
        args.putInt(ARG_TOOLBAR_LAYOUT_ID, toolbarLayoutId);
        StandartToolbarFragment fragment = new StandartToolbarFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("ResourceType")
    @Override
    protected int getLayoutId() {
        int toolbarId = getArguments() != null? getArguments().getInt(ARG_TOOLBAR_LAYOUT_ID) : 0;
        return toolbarId != 0? toolbarId : R.layout.fragment_standart_toolbar;
    }

    @Override
    protected int getToolbarId() {
        return R.id.toolbar;
    }

}
