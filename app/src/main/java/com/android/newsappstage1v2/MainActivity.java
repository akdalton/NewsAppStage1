package com.android.newsappstage1v2;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderCallbacks<List<News>> {

    /**
     * Adapter for the list of newsArticles
     */
    private NewsAdapter newsAdapter;

    /**
     * ImageView that is displayed when the list is empty
     */
    private ImageView noDataFound;

    /**
     * Swipe to reload spinner that is displayed while data is being downloaded
     */
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Lookup the swipe container view
        swipeContainer = findViewById(R.id.swipeContainer);
        swipeContainer.setRefreshing(true);

        // Find a reference to the {@link ListView} in the layout
        ListView newsListView = findViewById(R.id.list_item);

        // Find the feedback_view that is only visible when the list has no items
        noDataFound = findViewById(R.id.no_data_found);
        newsListView.setEmptyView(noDataFound);

        // Create a new adapter that takes an empty list of articles as input
        newsAdapter = new NewsAdapter(this, new ArrayList<News>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        newsListView.setAdapter(newsAdapter);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected article.
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // Find the current article that was clicked on
                News currentNews = newsAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri newsUri = Uri.parse(currentNews.getArticleUrl());

                // Create a new intent to view the article URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });

        // If there is a network connection, fetch data
        loadData();

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Check for internet connection and attempt to load data
                loadData();
            }
        });
    }


    @Override
    public Loader<List<News>> onCreateLoader(int id, Bundle args) {

        // Create a new loader for the given URL
        return new NewsLoader(this);
    }

    /**
     * Called when a previously created loader has finished its load.
     */
    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {

        // Hide swipe to reload spinner
        swipeContainer.setRefreshing(false);

        // Display while refreshing the news articles page
        noDataFound.setImageResource(R.drawable.the_guardian);

        // Clear the adapter of previous news data.
        newsAdapter.clear();

        // If there is a valid list of {@link News}, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (news != null && !news.isEmpty()) {
            newsAdapter.addAll(news);
        } else {
            if (QueryUtils.isConnected(getBaseContext())) {
                // Set empty state to display "No articles found."
                noDataFound.setImageResource(R.drawable.no_data_found);
            } else {
                // Update view with no connection error message
                noDataFound.setImageResource(R.drawable.no_internet_connection);
            }
        }
    }


    /**
     * Called when a previously created loader is being reset,
     * and thus making its data unavailable.
     */
    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        // Loader reset, so clear out existing data.
        newsAdapter.clear();
    }

    /**
     * Loads and reloads the data as requested
     */
    public void loadData() {

        // Display while loading the news articles page
        noDataFound.setImageResource(R.drawable.the_guardian);

        // If there is a network connection, fetch data
        if (QueryUtils.isConnected(getBaseContext())) {

            // destroy old loader to get new info
            getLoaderManager().destroyLoader(1);

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the args. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            getLoaderManager().initLoader(1, null, this);

        } else {
            // Hide loading indicator so error message will be visible
            swipeContainer.setRefreshing(false);

            // Update view with no connection error message
            noDataFound.setImageResource(R.drawable.no_internet_connection);

        }
    }


}

