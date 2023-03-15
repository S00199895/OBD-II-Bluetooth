package edu.markc.bluetooth;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class TablayoutFragment extends FragmentPagerAdapter {

    int tabs = 2;
    public TablayoutFragment(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new CurrentJobs();
            case 1:
                return new ResolvedJobs();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabs;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Active";
            case 1:
                return "Resolved";
            default:
                return null;
        }
    }
}