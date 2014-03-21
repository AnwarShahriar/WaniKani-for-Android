package tr.xip.wanikani.cards;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.List;

import tr.xip.wanikani.BroadcastIntents;
import tr.xip.wanikani.R;
import tr.xip.wanikani.adapters.RecentUnlocksAdapter;
import tr.xip.wanikani.api.WaniKaniApi;
import tr.xip.wanikani.api.response.RecentUnlocksList;
import tr.xip.wanikani.utils.Utils;

/**
 * Created by xihsa_000 on 3/13/14.
 */
public class RecentUnlocksCard extends Fragment {

    View rootView;

    WaniKaniApi api;
    Utils utils;

    Context mContext;

    TextView mCardTitle;
    ListView mRecentUnlocksList;

    RecentUnlocksAdapter mRecentUnlocksAdapter;

    ViewFlipper mViewFlipper;
    ViewFlipper mConnectionViewFlipper;

    private BroadcastReceiver mDoLoad = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mContext = context;
            new LoadTask().execute();
        }
    };

    @Override
    public void onCreate(Bundle state) {
        api = new WaniKaniApi(getActivity());
        utils = new Utils(getActivity());
        super.onCreate(state);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDoLoad,
                new IntentFilter(BroadcastIntents.SYNC()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.card_recent_unlocks, null);

        mCardTitle = (TextView) rootView.findViewById(R.id.card_recent_unlocks_title);
        mRecentUnlocksList = (ListView) rootView.findViewById(R.id.card_recent_unlocks_list);

        mViewFlipper = (ViewFlipper) rootView.findViewById(R.id.card_recent_unlocks_view_flipper);
        mViewFlipper.setInAnimation(getActivity(), R.anim.abc_fade_in);
        mViewFlipper.setOutAnimation(getActivity(), R.anim.abc_fade_out);

        mConnectionViewFlipper = (ViewFlipper) rootView.findViewById(R.id.card_recent_unlocks_connection_view_flipper);
        mConnectionViewFlipper.setInAnimation(getActivity(), R.anim.abc_fade_in);
        mConnectionViewFlipper.setOutAnimation(getActivity(), R.anim.abc_fade_out);

        return rootView;
    }

    public int setRecentUnlocksHeightBasedOnListView(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter == null) {
            return (int) pxFromDp(550);
        } else {

            int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
            for (int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);
                if (listItem instanceof ViewGroup) {
                    listItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                }
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }

            totalHeight += mCardTitle.getMeasuredHeight();
            totalHeight += pxFromDp(16); // Add the paddings as well

            return totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        }
    }

    private float pxFromDp(float dp) {
        return dp * mContext.getResources().getDisplayMetrics().density;
    }

    private class LoadTask extends AsyncTask<String, Void, List<RecentUnlocksList.UnlockItem>> {

        @Override
        protected List<RecentUnlocksList.UnlockItem> doInBackground(String... strings) {
            List<RecentUnlocksList.UnlockItem> list = null;
            try {
                list = api.getRecentUnlocksList(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<RecentUnlocksList.UnlockItem> result) {
            int height;

            if (result != null) {
                mRecentUnlocksAdapter = new RecentUnlocksAdapter(mContext,
                        R.layout.item_recent_unlock, result);
                mRecentUnlocksList.setAdapter(mRecentUnlocksAdapter);
                if (mConnectionViewFlipper.getDisplayedChild() == 1) {
                    mConnectionViewFlipper.showPrevious();
                }

                height = setRecentUnlocksHeightBasedOnListView(mRecentUnlocksList);
            } else {
                if (mConnectionViewFlipper.getDisplayedChild() == 0) {
                    mConnectionViewFlipper.showNext();
                }

                height = (int) pxFromDp(158);
            }

            if (mViewFlipper.getDisplayedChild() == 0) {
                mViewFlipper.showNext();
            }

            Intent intent = new Intent(BroadcastIntents.FINISHED_SYNC_RECENT_UNLOCKS_CARD());
            intent.putExtra("height", height);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }

}