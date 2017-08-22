package hu.ait.missbeauty.adapter;

import hu.ait.missbeauty.data.Product;

/**
 * Created by chaelimseo on 5/22/17.
 */

public interface AdapterCallback {

    void showEditItemActivity(Product product, int position, String key);
}
