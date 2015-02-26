/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer.demo;

import com.google.android.exoplayer.MediaCodecUtil;
import com.google.android.exoplayer.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer.demo.Samples.Sample;
import com.google.android.exoplayer.util.MimeTypes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An activity for selecting from a number of samples.
 */
public class SampleChooserActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "SampleChooserActivity";

    private EditText url_input_et;
    private Button start_stream_btn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_chooser_activity);

        url_input_et = (EditText) findViewById(R.id.url_input_et);
        start_stream_btn = (Button) findViewById(R.id.start_stream_btn);
        start_stream_btn.setOnClickListener(this);

        ListView sampleList = (ListView) findViewById(R.id.sample_list);
        final SampleAdapter sampleAdapter = new SampleAdapter(this);

        sampleAdapter.add(new Header("YouTube DASH"));
        sampleAdapter.addAll((Object[]) Samples.YOUTUBE_DASH_MP4);
        sampleAdapter.add(new Header("Widevine GTS DASH"));
        sampleAdapter.addAll((Object[]) Samples.WIDEVINE_GTS);
        sampleAdapter.add(new Header("SmoothStreaming"));
        sampleAdapter.addAll((Object[]) Samples.SMOOTHSTREAMING);
        sampleAdapter.add(new Header("HLS"));
        sampleAdapter.addAll((Object[]) Samples.HLS);
        sampleAdapter.add(new Header("Misc"));
        sampleAdapter.addAll((Object[]) Samples.MISC);

        // Add WebM samples if the device has a VP9 decoder.
        try {
            if (MediaCodecUtil.getDecoderInfo(MimeTypes.VIDEO_VP9, false) != null) {
                sampleAdapter.add(new Header("YouTube WebM DASH (Experimental)"));
                sampleAdapter.addAll((Object[]) Samples.YOUTUBE_DASH_WEBM);
            }
        } catch (DecoderQueryException e) {
            Log.e(TAG, "Failed to query vp9 decoder", e);
        }

        sampleList.setAdapter(sampleAdapter);
        sampleList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = sampleAdapter.getItem(position);
                if (item instanceof Sample) {
                    onSampleSelected((Sample) item);
                }
            }
        });
    }

    private void onSampleSelected(Sample sample) {
        Intent mpdIntent = new Intent(this, PlayerActivity.class)
                .setData(Uri.parse(sample.uri))
                .putExtra(PlayerActivity.CONTENT_ID_EXTRA, sample.contentId)
                .putExtra(PlayerActivity.CONTENT_TYPE_EXTRA, sample.type);
        startActivity(mpdIntent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_stream_btn:
                String uri = url_input_et.getText().toString();
                if(!TextUtils.isEmpty(uri)) {
                    Sample mSample = new Sample("HLS Test", uri, DemoUtil.TYPE_HLS);
                    try {
                        onSampleSelected(mSample);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "URI Maybe unparsable: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter a url 1st", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private static class SampleAdapter extends ArrayAdapter<Object> {

        public SampleAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                int layoutId = getItemViewType(position) == 1 ? android.R.layout.simple_list_item_1
                        : R.layout.sample_chooser_inline_header;
                view = LayoutInflater.from(getContext()).inflate(layoutId, null, false);
            }
            Object item = getItem(position);
            String name = null;
            if (item instanceof Sample) {
                name = ((Sample) item).name;
            } else if (item instanceof Header) {
                name = ((Header) item).name;
            }
            ((TextView) view).setText(name);
            return view;
        }

        @Override
        public int getItemViewType(int position) {
            return (getItem(position) instanceof Sample) ? 1 : 0;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

    }

    private static class Header {

        public final String name;

        public Header(String name) {
            this.name = name;
        }

    }

}
