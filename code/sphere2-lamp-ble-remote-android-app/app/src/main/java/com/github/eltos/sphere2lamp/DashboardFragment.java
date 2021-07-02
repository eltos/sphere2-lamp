package com.github.eltos.sphere2lamp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;

import com.github.eltos.sphere2lamp.databinding.FragmentDashboardBinding;
import com.github.eltos.sphere2lamp.properties.Property;
import com.github.eltos.sphere2lamp.properties.RawProperty;

import java.util.ArrayList;
import java.util.Locale;

import eltos.simpledialogfragment.SimpleDialog;

public class DashboardFragment extends Fragment implements Sphere2Lamp.Callback, SimpleDialog.OnDialogResultListener {

    public static final String FILTER_ITEMS = "filter-items";
    private FragmentDashboardBinding binding;
    private GridLayoutManager mLayoutManager;
    private DashboardAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Sphere2Lamp.addCallback(this);
        setHasOptionsMenu(true);

        // Create dashboard views
        mAdapter = new DashboardAdapter();
        mAdapter.registerFragment(this);
        mLayoutManager = new GridLayoutManager(getContext(), 2);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                String uuid = mAdapter.getItem(position).getUuid().toUpperCase(Locale.ROOT);
                if (Property.CHARACTERISTIC_MODE.equals(uuid)) return 2;
                if (mAdapter.getItem(position) instanceof RawProperty) return 2;
                return 1;
            }
        });

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.connectButton.setOnClickListener(view1 -> {
            if (getActivity() instanceof MainActivity){
                ((MainActivity) getActivity()).connect(true);
            }
        });

        binding.dashboardView.setLayoutManager(mLayoutManager);
        binding.dashboardView.setItemAnimator(new DefaultItemAnimator());
        binding.dashboardView.setAdapter(mAdapter);

        mAdapter.replaceProperties(Sphere2Lamp.getProperties());

        updateLoadingIndicator();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        Sphere2Lamp.removeCallback(this);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_dashboard, menu);
        SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
        boolean filter = pref.getBoolean(FILTER_ITEMS, true);
        menu.findItem(R.id.setting_filter_items).setChecked(filter);
        mAdapter.setFilterItems(filter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.setting_filter_items) {
            item.setChecked(!item.isChecked());
            SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
            pref.edit().putBoolean(DashboardFragment.FILTER_ITEMS, item.isChecked()).apply();
            mAdapter.setFilterItems(item.isChecked());
            mAdapter.filterProperties();
            return true;
        } else if (id == R.id.action_refresh) {
            Sphere2Lamp.readAllProperties();
        }

        return super.onOptionsItemSelected(item);
    }

    // callbacks
    @Override
    public void onLampConnectionChange() {
        if (!Sphere2Lamp.isConnected()){
            mAdapter.replaceProperties(new ArrayList<>(0));
        }
        updateLoadingIndicator();
    }

    @Override
    public void onLampError(@StringRes int id) {
        updateLoadingIndicator();
    }

    @Override
    public void onLampPropertiesDiscovered() {
        updateLoadingIndicator();
        mAdapter.replaceProperties(Sphere2Lamp.getProperties());
    }

    @Override
    public void onLampPropertyUpdated(String uuid) {
        mAdapter.updateProperty(Sphere2Lamp.getProperty(uuid));
    }

    private void updateLoadingIndicator(){
        boolean loading = (Sphere2Lamp.isConnecting() || Sphere2Lamp.isConnected()) && !Sphere2Lamp.isReady();
        binding.spinner.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.connectButton.setVisibility(!Sphere2Lamp.isConnected() ? View.VISIBLE : View.GONE);
    }


    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {
        return mAdapter.onResult(dialogTag, which, extras);
    }
}