package hu.ait.missbeauty.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


/**
 * Created by ChenChen on 5/18/17.
 */

public class MyFragmentPager  extends FragmentPagerAdapter {
    public MyFragmentPager(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new FragmentOne();
                break;
            case 1:
                fragment = new FragmentTwo();
                break;
            case 2:
                fragment = new FragmentThree();
                break;
            default:
                fragment = new FragmentOne();
                break;
        }

        return fragment;

    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position==0){
            return "In Use";
        }else if(position==1){
            return "Expired";
        }else {
            return "Deleted";
        }
    }
}
