package hu.ait.missbeauty.touch;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import hu.ait.missbeauty.adapter.ProductAdapter;

/**
 * Created by chaelimseo on 5/20/17.
 */

public class ProductListTouchHelper extends ItemTouchHelper.Callback{

    private ProductAdapter productTouchHelperAdapter;

    public ProductListTouchHelper(ProductAdapter productTouchHelperAdapter){

        this.productTouchHelperAdapter = productTouchHelperAdapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

        productTouchHelperAdapter.swapPlaces(viewHolder.getAdapterPosition(),
                target.getAdapterPosition());

        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        //productTouchHelperAdapter.removeProduct(viewHolder.getAdapterPosition());
        productTouchHelperAdapter.editProduct(viewHolder.getAdapterPosition());

    }
}
