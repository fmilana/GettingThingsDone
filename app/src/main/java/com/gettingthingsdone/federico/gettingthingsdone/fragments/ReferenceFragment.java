package com.gettingthingsdone.federico.gettingthingsdone.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gettingthingsdone.federico.gettingthingsdone.R;

/**
 * Created by Federico on 07-Nov-17.
 */

public class ReferenceFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reference, container, false);

        getActivity().setTitle(R.string.reference);

        return view;
    }
}
