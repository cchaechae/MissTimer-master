package hu.ait.missbeauty.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import hu.ait.missbeauty.AddItemActivity;
import hu.ait.missbeauty.R;
import hu.ait.missbeauty.adapter.AdapterCallback;
import hu.ait.missbeauty.adapter.ProductAdapter;
import hu.ait.missbeauty.data.Product;
import hu.ait.missbeauty.touch.ProductListTouchHelper;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static hu.ait.missbeauty.MainActivity.KEY_EDIT;

/**
 * Created by ChenChen on 5/18/17.
 */

public class FragmentTwo extends Fragment implements AdapterCallback {

    public static final String TAG = "Expired";

    public static final int REQUEST_NEW_ITEM = 101;
    public static final int REQUEST_EDIT_ITEM = 102;
    public static final String KEY_ITEM = "KEY_ITEM";

    private int position = -1;
    private ProductAdapter adapter;
    private String dataSnapKey = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.expired_list, container, false);

        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.expired_list);
        rv.setHasFixedSize(true);

        adapter = new ProductAdapter(getContext(), FirebaseAuth.getInstance().getCurrentUser().getUid(), FragmentTwo.this);
        rv.setAdapter(adapter);

        ProductListTouchHelper productCallback = new ProductListTouchHelper(adapter);


        ItemTouchHelper touchHelper = new ItemTouchHelper(productCallback);
        touchHelper.attachToRecyclerView(rv);

        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("products");
        postsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Product newProduct = dataSnapshot.getValue(Product.class);
                if (newProduct.getIsExpired() == 1)
                    adapter.addProduct(newProduct, dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.w(TAG, "fragment two:onCancelled", databaseError.toException());
                Toast.makeText(getContext(), "Failed to load comments.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        return rootView;
    }

    @Override
    public void showEditItemActivity(Product product, int position, String key) {

        Intent intentStart = new Intent(getActivity(), AddItemActivity.class);
        this.position = position;

        intentStart.putExtra(KEY_ITEM, key);
        intentStart.putExtra("name", product.getName());
        intentStart.putExtra("expDate", product.getExpDate());
        intentStart.putExtra("opnDate", product.getOpnDate());
        intentStart.putExtra("memo", product.getMemo());
        intentStart.putExtra("imageURL", product.getImageURL());
        intentStart.putExtra("uid", product.getUid());
        intentStart.putExtra("isOpen", product.isOpenStatus());
        startActivityForResult(intentStart, REQUEST_EDIT_ITEM);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode){
            case RESULT_OK:
                String itemID = data.getStringExtra("KEY_ITEM");

                Product product = adapter.getProduct(position);

                if(requestCode == REQUEST_EDIT_ITEM){

                    adapter.updateItem(position, product);

                }

                break;

            case RESULT_CANCELED:
                //showSnackBarMessage("place adding cancelled");
                break;
        }
    }
}