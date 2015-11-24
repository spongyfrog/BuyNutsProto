package com.cs4310.epsilon.buynutsproto.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cs4310.epsilon.buynutsproto.R;
import com.cs4310.epsilon.buynutsproto.guiHelpers.MyArrayAdapter;
import com.cs4310.epsilon.buynutsproto.talkToBackend.ListFilteredOffersAsyncTask;
import com.cs4310.epsilon.buynutsproto.talkToBackend.ListOffersAsyncTask;
import com.cs4310.epsilon.nutsinterface.RequestFilteredSellOffer;
import com.cs4310.epsilon.nutsinterface.SellOfferFront;

import java.util.ArrayList;


/**
 * Activity that displays a list of SellOffers in a ListView that occupies
 * most of the screen.
 */
public class NewsActivity extends AppCompatActivity {
    // constants
    /**
     * This is a request code for a search filter, used to notify the
     * NewsActivity which activity is sending it data. For example, this
     * activity starts a new SetSearchFilterActivity when the user clicks
     * "Set Search Filter" (using startActivityForResult), and it sends that
     * activity this request code in an intent. When that activity finishes,
     * the method onActivityResult() will be called, and that method needs some
     * means of figuring out what sequence of events occurred before the start
     * of that method. This request code is used to tell that method that the
     * user clicked on "Set Search Filter" to cause this sequence of events,
     * so the method can respond appropriately to the data passed in the intent.
     */
    static final int REQUEST_CODE_SEARCH_FILTER = 0;
    static final int REQUEST_CODE_MAKE_OFFER = 1;

    // instance data members
    /**
     * The user ID, which is set by the MainLoginActivity when the user logs in.
     * This value is initialized by default to an invalid user ID, so the
     * program will know if the user reached this point in error.
     */
    private long mUid = MainLoginActivity.INVALID_USERID;


    private RequestFilteredSellOffer mFilter;

    /**
     * The object that displays the list of SellOfferFront objects to the user.
     */
    private ListView mListView;
    /**
     * The ArrayAdapter object used to hold a list of SellOfferFront objects
     * in a way that the ListView can interact with them; it allows the ListView
     * to display them, and when a user clicks on an item in the list, this
     * object allows the program to figure out which item the user clicked on.
     */
    private MyArrayAdapter mArrayAdapter;
    /**
     * A list of SellOfferFront objects that the NewsActivity will display in
     * its ListView object
     */
    private ArrayList<SellOfferFront> mSellOffers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        //get mUid from intent
        mUid = this.getIntent().getLongExtra(Constants.USER_ID_KEY, MainLoginActivity.INVALID_USERID);
        Log.i(Constants.TAG, (mUid == MainLoginActivity.INVALID_USERID ? "Didn't receive mUid" : "Received mUid=" + mUid));

        //get mListView
        mListView = (ListView) findViewById(R.id.listView);
        updateListView();

        // set OnClickListeners:
        /**
         * btnMakeNewOffer: launches a new MakeOfferActivity that will allow a
         * user to make a new SellOffer
         */
        Button btnMakeNewOffer = (Button) findViewById(R.id.btnMakeOffer_News);
        btnMakeNewOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(Constants.TAG, "onClick btnMakeOffer_News");

                //create and launch MakeOfferActivity
                Intent intent = new Intent(NewsActivity.this, MakeOfferActivity.class);
                intent.putExtra("mUid", mUid);
                NewsActivity.this.startActivityForResult(intent, REQUEST_CODE_MAKE_OFFER);
            }
        });
        /**
         * btnSetSearch: launches a new SetSearchFilterActivity that allows the
         * user to set search settings
         */
        Button btnSetSearch = (Button) findViewById(R.id.btnSetFilter_News);
        btnSetSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(Constants.TAG, "onClick btnSetFilter_News");

                // create intent for SetSearchFilterActivity
                Intent i = new Intent(NewsActivity.this, SetSearchFilterActivity.class);
                i.putExtra("uid", mUid);

                //create and launch SetSearchFilterActivity
                NewsActivity.this.startActivityForResult(
                        i, REQUEST_CODE_SEARCH_FILTER);
            }
        });

        /**
         * btnRefreshNews: launches a ListOffersAsyncTask that will set
         * mSellOffers and update the ListView with fresh list of SellOffers
         */
        Button btnRefreshNews = (Button) findViewById(R.id.btnRefreshNews_News);
        btnRefreshNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(Constants.TAG, "onClick btnRefreshNews_News");

                setStatusMsg("Requesting SellOffers from server...");

                new ListFilteredOffersAsyncTask(NewsActivity.this).execute(mFilter);

                //new ListOffersAsyncTask(NewsActivity.this).execute(mUid);
            }
        });

        /**
         * mListView: When the user clicks on an item in the ListView, a new
         * ViewSellOfferActivity is launched, which lets the user get a closer
         * look at the SellOffer
         */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //anonymous inner class that implements OnItemClickListener
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                SellOfferFront choice = NewsActivity.this.mArrayAdapter.getItem(position);
                //intent is the only parameter passed to the new activity
                Intent intent = new Intent(NewsActivity.this, ViewSellOfferActivity.class);
                intent.putExtra("SellOffer", choice);

                Log.i(Constants.TAG, "UserFront clicked SellOffer at position=" + position + ", id=" + id);
                //create another activity
                NewsActivity.this.startActivity(intent);
                //refer to parent reference - can't just say "this", that's the inner class
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.i(Constants.TAG, "onActivityResult()");
        if(resultCode != Activity.RESULT_OK) {
            Log.i(Constants.TAG, "didn't get an intent from SetSearchFilterActivity");
        } else if(requestCode == REQUEST_CODE_SEARCH_FILTER) {
            // now we know that a SetSearchFilterActivity has sent us this intent
            Log.i(Constants.TAG, "successfully obtained intent from SetSearchFilterActivity");
            // we can retrieve info from intent 'data' here
            mFilter = data.getParcelableExtra(
                    SetSearchFilterActivity.SEARCH_FILTER_KEY);

            Log.i(Constants.TAG, "NewsActivity received filter: " + mFilter.toString());
            setStatusMsg("Requesting " + mFilter.getCommodity() + " offers from server...");

            new ListFilteredOffersAsyncTask(NewsActivity.this).execute(mFilter);

            //Toast.makeText(this, mFilter.toString(), Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQUEST_CODE_MAKE_OFFER) {
            // The only way to get here is if the user made an offer successfully;
            // otherwise resultCode would not be RESULT_OK or requestCode would be something else

            String commodity = data.getStringExtra("commodity");
            setStatusMsg("New offer for " + commodity +" inserted ok");
        } else {
            // handle other requestCode values here
        }
    }

    /**
     * Used to set the list of SellOfferFront objects that this NewsActivity will
     * display in its ListView.
     * @param sellOffers    ArrayList of SellOfferFront object to display in
     *                      the ListView
     */
    public void setmSellOffers(ArrayList<SellOfferFront> sellOffers){
        this.mSellOffers = sellOffers;
    }

    /**
     * Displays mSellOffers in the mListView.
     */
    public void updateListView() {
        if(mSellOffers != null) {
            mArrayAdapter = new MyArrayAdapter(this,
                    R.layout.list_item_news, mSellOffers);
        } else {
            mArrayAdapter = new MyArrayAdapter(this,
                    R.layout.list_item_news, new ArrayList<SellOfferFront>());
        }
        mListView.setAdapter(mArrayAdapter);
    }

    /**
     * Displays a message in the status bar at the bottom of the screen.
     * @param statusMsg The message to display
     */
    public void setStatusMsg(String statusMsg) {
        TextView tv = (TextView) findViewById(R.id.tvStatus_News);
        tv.setText(statusMsg);
    }
}
