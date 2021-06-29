package com.github.eltos.sphere2lamp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;

import com.github.eltos.sphere2lamp.databinding.FragmentDashboardBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import eltos.simpledialogfragment.SimpleDialog;

public class DashboardFragment extends Fragment implements Sphere2Lamp.Callback, SimpleDialog.OnDialogResultListener {

    private FragmentDashboardBinding binding;
    private GridLayoutManager mLayoutManager;
    private DashboardPropertiesAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Sphere2Lamp.addCallback(this);

        // Create dashboard views
        mAdapter = new DashboardPropertiesAdapter();
        mAdapter.registerFragment(this);
        mLayoutManager = new GridLayoutManager(getContext(), 2);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                String uuid = mAdapter.getItem(position).characteristic.getUuid().toString().toUpperCase(Locale.ROOT);
                if (Sphere2LampProperties.CHARACTERISTIC_MODE.equals(uuid)) return 2;
                if (mAdapter.getItem(position) instanceof Sphere2LampProperties.RawProperty) return 2;
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

        setProperties(Sphere2Lamp.getProperties());

        updateLoadingIndicator();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        Sphere2Lamp.removeCallback(this);

    }


    private void setProperties(List<Sphere2LampProperties.Property<?>> properties){
        Collections.sort(properties, (o1, o2) -> o1.order - o2.order);
        mAdapter.setData(properties);
        mAdapter.notifyDataSetChanged();
    }



    // callbacks
    @Override
    public void onLampConnectionChange() {
        if (!Sphere2Lamp.isConnected()){
            setProperties(new ArrayList<>(0));
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
        setProperties(Sphere2Lamp.getProperties());
    }

    @Override
    public void onLampPropertiesUpdated() {
        mAdapter.notifyDataSetChanged();
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