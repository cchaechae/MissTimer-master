package hu.ait.missbeauty;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hu.ait.missbeauty.data.Product;
import hu.ait.missbeauty.fragment.MyFragmentPager;
import hu.ait.missbeauty.touch.ProductListTouchHelper;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    public static final int REQUEST_NEW_ITEM = 101;
    public static final int REQUEST_EDIT_ITEM = 102;
    public static final String KEY_EDIT = "KEY_EDIT";
    private FragmentPagerAdapter pagerAdapter;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    private int itemToEditPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new MyFragmentPager(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        TextView tvEmail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textView);
        tvEmail.setText(currentUser.getEmail());

        FloatingActionButton fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddItemActivity();
            }
        });


    }


    public void showAddItemActivity(){

        Intent intentAddItem = new Intent(MainActivity.this, AddItemActivity.class);

        startActivityForResult(intentAddItem, REQUEST_NEW_ITEM);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
        }
        if (id == R.id.my_pouch) {
            startActivity(new Intent(MainActivity.this, MainActivity.class));
            finish();
        }
        if (id == R.id.community) {
            startActivity(new Intent(MainActivity.this, PostsActivity.class));
            finish();
        }

        if (id == R.id.my_profile){

            Intent i = new Intent(MainActivity.this, ProfileScreenDesign.class);
            i.putExtra("username",getUserName());
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public String getUserName(){

        String userName = currentUser.getDisplayName();

        return userName;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            FirebaseAuth.getInstance().signOut();
            super.onBackPressed();
        }
    }
}
