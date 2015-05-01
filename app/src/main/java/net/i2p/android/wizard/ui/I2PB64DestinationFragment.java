/*
 * Copyright 2013 str4d
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.i2p.android.wizard.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.i2p.android.router.R;
import net.i2p.android.router.util.Util;
import net.i2p.android.wizard.model.Page;
import net.i2p.android.wizard.model.SingleTextFieldPage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class I2PB64DestinationFragment extends Fragment {
    static final int REQUEST_DESTINATION_FILE = 1;

    private static final String ARG_KEY = "key";

    private PageFragmentCallbacks mCallbacks;
    private SingleTextFieldPage mPage;
    protected TextView mFieldView;
    private TextView mFeedbackView;

    public static I2PB64DestinationFragment create(String key) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        I2PB64DestinationFragment fragment = new I2PB64DestinationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public I2PB64DestinationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        String mKey = args.getString(ARG_KEY);
        mPage = (SingleTextFieldPage) mCallbacks.onGetPage(mKey);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wizard_page_single_text_field_picker, container, false);
        ((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());
        ((TextView) rootView.findViewById(R.id.wizard_text_field_desc)).setText(mPage.getDesc());

        Button b = (Button) rootView.findViewById(R.id.wizard_text_field_pick);
        b.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("text/plain");
                i.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(
                            Intent.createChooser(i,"Select B64 file"),
                            REQUEST_DESTINATION_FILE);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "Please install a File Manager.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mFieldView = ((TextView) rootView.findViewById(R.id.wizard_text_field));
        mFieldView.setHint(mPage.getTitle());
        if (mPage.getData().getString(Page.SIMPLE_DATA_KEY) != null)
            mFieldView.setText(mPage.getData().getString(Page.SIMPLE_DATA_KEY));
        else if (mPage.getDefault() != null) {
            mFieldView.setText(mPage.getDefault());
            mPage.getData().putString(Page.SIMPLE_DATA_KEY, mPage.getDefault());
        }

        mFeedbackView = (TextView) rootView.findViewById(R.id.wizard_text_field_feedback);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof PageFragmentCallbacks)) {
            throw new ClassCastException("Activity must implement PageFragmentCallbacks");
        }

        mCallbacks = (PageFragmentCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFieldView.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                    int i2) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void afterTextChanged(Editable editable) {
                mPage.getData().putString(Page.SIMPLE_DATA_KEY,
                        (editable != null) ? editable.toString() : null);
                mPage.notifyDataChanged();
                if (mPage.showFeedback()) {
                    mFeedbackView.setText(mPage.getFeedback());
                }
            }
        });
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        // In a future update to the support library, this should override setUserVisibleHint
        // instead of setMenuVisibility.
        if (mFieldView != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (!menuVisible) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_DESTINATION_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri result = data.getData();
                BufferedReader br = null;
                try {
                    ParcelFileDescriptor pfd = getActivity().getContentResolver().openFileDescriptor(result, "r");
                    br = new BufferedReader(
                            new InputStreamReader(
                                    new ParcelFileDescriptor.AutoCloseInputStream(pfd)));
                    try {
                        mFieldView.setText(br.readLine());
                    } catch (IOException ioe) {
                        Util.e("Failed to read B64 file", ioe);
                        Toast.makeText(getActivity(), "Failed to read B64 file.",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (FileNotFoundException fnfe) {
                    Util.e("Could not find B64 file", fnfe);
                    Toast.makeText(getActivity(), "Could not find B64 file.",
                            Toast.LENGTH_SHORT).show();
                } finally {
                    if (br != null)
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
        }
    }
}
