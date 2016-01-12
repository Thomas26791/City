package com.aix.city;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.aix.city.core.AIxDataManager;
import com.aix.city.core.ListingSource;
import com.aix.city.core.PostListing;
import com.aix.city.dummy.DummyContent;
import com.aix.city.view.LocationAdapter;
import com.aix.city.view.TagAdapter;


public class BaseListingActivity extends FragmentActivity implements PostListingFragment.OnFragmentInteractionListener, ListingSourceFragment.OnFragmentInteractionListener, SearchMenuFragment.OnFragmentInteractionListener, UserMenuFragment.OnFragmentInteractionListener {

    public final static String EXTRAS_LISTING_SOURCE = "com.aix.city.ListingSource";

    private ListingSourceFragment listingSourceFragment;
    private PostListingFragment postListingFragment;

    private LinearLayout searchMenu;
    private LinearLayout userMenu;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private RelativeLayout mainLayout;

    public BaseListingActivity(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_base_listing);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        searchMenu = (LinearLayout) findViewById(R.id.fragment_left_menu);
        userMenu = (LinearLayout) findViewById(R.id.fragment_right_menu);

        createMainLayout();
        createSearchMenu();
        createUserMenu();

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, null/*R.drawable.ic_drawer*/, R.string.acc_drawer_open, R.string.acc_drawer_close) {
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (drawerView.getId() == R.id.fragment_right_menu) slideOffset *= -1;
                float moveFactor = (searchMenu.getWidth() * slideOffset);
                mainLayout.setTranslationX(moveFactor);
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
    }

    private void createMainLayout(){
        Intent intent = getIntent();
        ListingSource listingSource = intent.getParcelableExtra(EXTRAS_LISTING_SOURCE);
        if (listingSource == null){
            listingSource = AIxDataManager.getInstance().getCurrentCity();
        }
        PostListing postListing = listingSource.createPostListing();

        //create fragments with data
        postListingFragment = PostListingFragment.newInstance(postListing);
        listingSourceFragment = ListingSourceFragment.newInstance(listingSource);

        // Create transaction
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace fragment container with actual fragment
        transaction.replace(R.id.fragment_container_top, listingSourceFragment);
        transaction.replace(R.id.fragment_container_bottom, postListingFragment);

        // Commit the transaction
        transaction.commit();
    }

    private void createSearchMenu(){

    }

    private void createUserMenu(){
        ListView userMenuList = (ListView) findViewById(R.id.right_menu_list);
        ArrayAdapter<String> rightListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, DummyContent.RIGHT_MENU_ELEMENTS);
        userMenuList.setAdapter(rightListAdapter);
    }

    public ListingSource getListingSource() {
        return listingSourceFragment.getListingSource();
    }

    public PostListing getPostListing(){
        return postListingFragment.getPostListing();
    }

    public void startBaseListingActivity(ListingSource listingSource){
        Intent intent = new Intent(this, BaseListingActivity.class);
        intent.putExtra(BaseListingActivity.EXTRAS_LISTING_SOURCE, listingSource);
        this.startActivity(intent);
    }

    @Override
    public void onFragmentInteraction(String key) {
        if (key != null){
            switch(key){
                case ListingSourceFragment.INTERACTION_KEY_OPEN_LEFT:
                    drawerLayout.openDrawer(GravityCompat.START);
                    break;
                case ListingSourceFragment.INTERACTION_KEY_BACK:
                    Intent intent = new Intent(this, BaseListingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(searchMenu) || drawerLayout.isDrawerOpen(userMenu)){
            drawerLayout.closeDrawers();
        }
        else{
            super.onBackPressed();
        }
    }
}
