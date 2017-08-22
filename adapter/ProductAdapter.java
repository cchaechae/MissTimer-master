package hu.ait.missbeauty.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import hu.ait.missbeauty.AddItemActivity;
import hu.ait.missbeauty.R;
import hu.ait.missbeauty.data.Product;

/**
 * Created by Crystal on 5/19/17.
 */

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private Context context;
    private List<Product> productList;
    private List<String> productKeys;
    private String userId;
    private int lastPosition = -1;
    private DatabaseReference userRef;
    private DatabaseReference productRef;
    private AdapterCallback mAdapterCallback;
    private TextView tvDisDate;
    private boolean isExpired;

    public ProductAdapter(Context context, String userId, Fragment fragment){

        this.context = context;
        this.userId = userId;
        this.productList = new ArrayList<Product>();
        this.productKeys = new ArrayList<String>();

        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        productRef = userRef.child("products");
        try{
            this.mAdapterCallback = ((AdapterCallback)fragment);
        } catch (ClassCastException e){
            throw new ClassCastException("Fragment implement Callback");
        }
    }

    @Override
    public ProductAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    public void addProduct(Product product, String key){

        productList.add(product);
        productKeys.add(key);
        notifyDataSetChanged();
    }

    public void removeProduct(int index){

        productRef.child(productKeys.get(index)).removeValue();
        productList.remove(index);
        productKeys.remove(index);
        notifyItemRemoved(index);
    }

    public void editProduct(int indexSwiped){

        Product product = productList.get(indexSwiped);
        String key = productKeys.get(indexSwiped);

        mAdapterCallback.showEditItemActivity(product, indexSwiped, key);
    }

    public void updateItem(int i, Product product){
        productList.set(i, product);
        notifyItemChanged(i);
    }

    public void addExpiredItem(String userId, String productName, String expDate,
    String opnDate, boolean openStatus, String memo, int isExpired, String spinnerAutoDate, String key){

        if (isExpired()){

            isExpired = 1;

            Product product = new Product(userId, productName, expDate, opnDate, openStatus, memo, isExpired, spinnerAutoDate);

            product.setIsExpired(1);

            productRef.child(key).setValue(product);
        }
    }


    @Override
    public void onBindViewHolder(final ProductAdapter.ViewHolder holder, final int position) {

        final Product tmpProduct = productList.get(position);
        holder.tvName.setText(tmpProduct.getName());
        tvDisDate = holder.tvDate;

        if (!tmpProduct.isOpenStatus()) {
            try {
                if (countIsExpired(changeDateFormet(tmpProduct.getExpDate().toString()))){

                    addExpiredItem(tmpProduct.getUid(), tmpProduct.getName(), tmpProduct.getExpDate(),
                            tmpProduct.getOpnDate(),tmpProduct.isOpenStatus(), tmpProduct.getMemo(),
                            tmpProduct.getIsExpired(), tmpProduct.getSpinnerAutoDate(), productKeys.get(position));
                    tmpProduct.setIsExpired(1);
                }else {
                    tmpProduct.setIsExpired(0);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            holder.linearLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        } else if (tmpProduct.isOpenStatus()) {
            try {
                if (tmpProduct.getSpinnerAutoDate().equals("N/A")) {
                    if (countIsExpired(changeDateFormet(tmpProduct.getExpDate().toString()))){
                        addExpiredItem(tmpProduct.getUid(), tmpProduct.getName(), tmpProduct.getExpDate(),
                                tmpProduct.getOpnDate(),tmpProduct.isOpenStatus(), tmpProduct.getMemo(),
                                tmpProduct.getIsExpired(), tmpProduct.getSpinnerAutoDate(), productKeys.get(position));
                        tmpProduct.setIsExpired(1);
                    } else {
                        tmpProduct.setIsExpired(0);
                    }
                } else if (compareDate(tmpProduct.getExpDate(), generateOpenExpDate(tmpProduct.getSpinnerAutoDate(), tmpProduct.getOpnDate()))) {
                    if (countIsExpired(changeDateFormet(tmpProduct.getExpDate().toString()))){
                        addExpiredItem(tmpProduct.getUid(), tmpProduct.getName(), tmpProduct.getExpDate(),
                                tmpProduct.getOpnDate(),tmpProduct.isOpenStatus(), tmpProduct.getMemo(),
                                tmpProduct.getIsExpired(), tmpProduct.getSpinnerAutoDate(), productKeys.get(position));
                        tmpProduct.setIsExpired(1);
                    }else {
                        tmpProduct.setIsExpired(0);
                    }
                } else {
                    if(countIsExpired(changeDateFormet(generateOpenExpDate(tmpProduct.getSpinnerAutoDate(), tmpProduct.getOpnDate())))){
                        addExpiredItem(tmpProduct.getUid(), tmpProduct.getName(), tmpProduct.getExpDate(),
                                tmpProduct.getOpnDate(),tmpProduct.isOpenStatus(), tmpProduct.getMemo(),
                                tmpProduct.getIsExpired(), tmpProduct.getSpinnerAutoDate(), productKeys.get(position));
                        tmpProduct.setIsExpired(1);
                    }else {
                        tmpProduct.setIsExpired(0);
                    }
                }
            } catch (NullPointerException e) {
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(tmpProduct.getImageURL())) {
            holder.ivProduct.setVisibility(View.VISIBLE);
            Glide.with(context).load(tmpProduct.getImageURL()).into(holder.ivProduct);
        } else {
            holder.ivProduct.setImageResource(R.drawable.applogo);
        }


        setAnimation(holder.itemView, position);
    }

    public boolean countIsExpired(final String expDate) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd");
        Date futureDate = dateFormat.parse(expDate);
        Date currentDate = new Date();
        if (!currentDate.after(futureDate)) {
            long diff = futureDate.getTime()
                    - currentDate.getTime();
            long days = diff / (24 * 60 * 60 * 1000);
            tvDisDate.setText("+" + String.format("%02d", days));
            isExpired=false;
            return isExpired;

        } else {
            long diff = currentDate.getTime()
                    - futureDate.getTime();
            long days = diff / (24 * 60 * 60 * 1000);
            tvDisDate.setText("-" + String.format("%02d", days));
            tvDisDate.setTextColor(Color.parseColor("#FFC94B4D"));
            isExpired=true;
            return isExpired;
        }
    }

    public boolean isExpired(){
        return isExpired;
    }


    public boolean compareDate(String expDateString, String openExpDateString)
            throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd");
        Date expDate = dateFormat.parse(changeDateFormet(expDateString));
        Date openExpDate = dateFormat.parse(changeDateFormet(openExpDateString));
        return openExpDate.after(expDate);
    }



    public String changeDateFormet(String expDate){
        String[] newFormat = expDate.split("/");
        String day = newFormat[0];
        String month = newFormat[1];
        if (month.length()==1){
            month="0"+month;
        }
        String year = newFormat[2];
        return year+"-"+month+"-"+day;
    }

    public String generateOpenExpDate(String openValue, String openDate){
        String[] openValueArray = openValue.split(" ");
        String openValueNum = openValueArray[0];
        int expYear = (int) Math.floor(Integer.parseInt(openValueNum)/12);
        int expMonth = Integer.parseInt(openValueNum)%12;
        String[] openDateArray = openDate.split("/");
        int openYear = Integer.parseInt(openDateArray[2]);
        int openMonth = Integer.parseInt(openDateArray[1]);
        if((openMonth+expMonth)>12){
            expYear= (int) (expYear+Math.floor((openMonth+expMonth)/12));
            expMonth=(openMonth+expMonth)%12;
            if(expMonth==0)
            {
                expMonth=12;
            }
            return Integer.parseInt(openDateArray[0])+"/"+expMonth+"/"+expYear;
        }
        int month = expMonth+openMonth;
        int year = expYear+openYear;
        return Integer.parseInt(openDateArray[0])+"/"+month+"/"+year;
    }

    private void setAnimation(View viewToAnimate, int position){

        if (position>lastPosition){
            Animation animation = AnimationUtils.loadAnimation(context,
                    R.anim.slide);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public void swapPlaces(int oldPosition, int newPosition) {
        if (oldPosition < newPosition) {
            for (int i = oldPosition; i < newPosition; i++) {
                Collections.swap(productList, i, i + 1);
            }
        } else {
            for (int i = oldPosition; i > newPosition; i--) {
                Collections.swap(productList, i, i - 1);
            }
        }
        notifyItemMoved(oldPosition, newPosition);
    }

    public Product getProduct(int i){
        return productList.get(i);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvName;
        public TextView tvDate;
        public ImageView ivProduct;
        public LinearLayout linearLayout;
        public ImageButton btnDelete;

        public ViewHolder(View itemView) {

            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            ivProduct = (ImageView) itemView.findViewById(R.id.ivIcon);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout);
            btnDelete = (ImageButton) itemView.findViewById(R.id.btnDelete);

        }
    }
}
