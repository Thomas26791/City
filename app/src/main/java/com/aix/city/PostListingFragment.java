package com.aix.city;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aix.city.core.AIxDataManager;
import com.aix.city.core.Likeable;
import com.aix.city.core.ListingSource;
import com.aix.city.core.ListingSourceType;
import com.aix.city.core.PostListing;
import com.aix.city.core.data.Location;
import com.aix.city.core.data.Post;
import com.aix.city.dummy.DummyContent;
import com.aix.city.view.PostAdapter;

import java.util.Observable;
import java.util.Observer;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class PostListingFragment extends ListFragment implements AbsListView.OnItemClickListener, Observer, View.OnClickListener {
    
    public final static String ARG_POST_LISTING = "listing";
    private TextView mEmptyView;
    private ProgressBar mLoadingPanel;

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String key);
    }

    /**
     * The PostListing-object which contains the posts. It is observed by this fragment.
     */
    private PostListing postListing;

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    private View postCreationView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private PostAdapter mAdapter;

    public static PostListingFragment newInstance(PostListing postListing) {
        PostListingFragment fragment = new PostListingFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_POST_LISTING, postListing);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PostListingFragment() {
    }

    public PostListing getPostListing() {
        return postListing;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListener = (OnFragmentInteractionListener) getActivity();

        //get instance data
        if (getArguments() != null) {
            Object obj = getArguments().getParcelable(ARG_POST_LISTING);
            if(obj != null && obj instanceof PostListing){
                postListing = (PostListing)obj;
            }
        }

        //create postview-adapter
        mAdapter = new PostAdapter(getActivity(), postListing.getPosts());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listing, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mEmptyView = (TextView)view.findViewById(R.id.emptyText);
        mLoadingPanel = (ProgressBar) view.findViewById(R.id.loadingPanel);

        mListView.setAdapter(mAdapter);
        mEmptyView.setVisibility(View.GONE);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);


        //initialize post creation view (text field)
        postCreationView = view.findViewById(R.id.postCreationLayout);
        setPostCreationVisibility(postListing.isEditable());

        final InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        final EditText editText = (EditText)view.findViewById(R.id.postCreationTextField);
        editText.setHorizontallyScrolling(false);
        editText.setMaxLines(5);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Post.MAX_CONTENT_LENGTH)});
        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                createPost(editText.getText().toString());
                editText.setText("");
                inputManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                return true;
            }
        });
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ARG_POST_LISTING, postListing);
        super.onSaveInstanceState(outState);
    }

    public void createPost(String postContent) {
        boolean postCreated = postListing.createPost(postContent);

        final String creationMessage;
        if (postCreated) {
            creationMessage = getResources().getString(R.string.postCreationMessage_successful);
            postListing.refresh();
        }
        else{
            creationMessage = getResources().getString(R.string.postCreationMessage_failed);
        }
        Toast.makeText(getContext(), creationMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();

        postListing.addObserver(this);
        AIxDataManager.getInstance().addObserver(this);

        //load posts
        if(postListing.getPosts().isEmpty()){
            postListing.loadMorePosts();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        postListing.deleteObserver(this);
        AIxDataManager.getInstance().deleteObserver(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.updateVisibleViews();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setPostCreationVisibility(boolean isVisible){
        if(isVisible){
            postCreationView.setVisibility(View.VISIBLE);
        }
        else{
            postCreationView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            //handle multiple view click events
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        String key;
        if(data == null) key = "";
        else key = data.toString();

        switch(key){
            case PostListing.OBSERVER_KEY_CHANGED_DATASET:
                mAdapter.notifyDataSetChanged();
                mLoadingPanel.setVisibility(View.GONE);
                if (!postListing.getPosts().isEmpty()){
                    mEmptyView.setVisibility(View.GONE);
                }
                break;
            case PostListing.OBSERVER_KEY_CHANGED_EDITABILITY:
                setPostCreationVisibility(postListing.isEditable());
                break;
            case Likeable.OBSERVER_KEY_CHANGED_LIKESTATUS:
                mAdapter.updateVisibleViews();
                break;
            case AIxDataManager.OBSERVER_KEY_CHANGED_LOCATIONS:
                mAdapter.updateVisibleViews();
                break;
            case PostListing.OBSERVER_KEY_CHANGED_FINISHED:
                mLoadingPanel.setVisibility(View.GONE);
                if (postListing.getPosts().isEmpty()){
                    mEmptyView.setVisibility(View.VISIBLE);
                }
                break;
        }
    }
}
