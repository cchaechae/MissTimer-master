package hu.ait.missbeauty.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import hu.ait.missbeauty.R;
import hu.ait.missbeauty.data.Post;


/**
 * Created by ChenChen on 5/19/17.
 */

public class PostsAdapter
        extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
    private Context context;
    private List<Post> postList;
    private List<String> postKeys;
    private DatabaseReference userRef;

    private String uId;
    private int lastPosition = -1;
    private DatabaseReference postsRef;

    public PostsAdapter(Context context, String uId) {
        this.context = context;
        this.uId = uId;
        this.postList = new ArrayList<Post>();
        this.postKeys = new ArrayList<String>();
        postsRef = FirebaseDatabase.getInstance().getReference("posts");
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_card, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final Post tmpPost = postList.get(position);
        holder.tvName.setText(tmpPost.getName());
        holder.tvCaption.setText(tmpPost.getCaption());
        if (!TextUtils.isEmpty(tmpPost.getImageUrl())) {
            holder.ivPost.setVisibility(View.VISIBLE);
            Glide.with(context).load(tmpPost.getImageUrl()).into(holder.ivPost);
        } else {

        }

//        System.out.println(tmpPost.getCaption().substring(0,3));
        if (tmpPost.getCaption().length()>4) {
            if ((tmpPost.getCaption().substring(0, 4)).equals("http")) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uriUrl = Uri.parse(tmpPost.getCaption());
                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                        context.startActivity(launchBrowser);
                    }
                });
            }
        }
        if(tmpPost.getUid().equals(uId)){
            holder.btnDelete.setVisibility(View.VISIBLE);
        }
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePost(holder.getAdapterPosition());
            }
        });

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public TextView tvCaption;
        public ImageView ivPost;
        public ImageButton btnDelete;


        public ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            ivPost = (ImageView) itemView.findViewById(R.id.ivPost);
            tvCaption= (TextView) itemView.findViewById(R.id.tvCaption);
            btnDelete = (ImageButton) itemView.findViewById(R.id.btnDelete);
        }
    }


    public void addPost(Post place, String key) {
        postList.add(place);
        postKeys.add(key);
        notifyDataSetChanged();
    }

    public void removePost(int index) {
        postsRef.child(postKeys.get(index)).removeValue();
        postList.remove(index);
        postKeys.remove(index);
        notifyItemRemoved(index);
    }

    public void removePostByKey(String key) {
        int index = postKeys.indexOf(key);
        if (index != -1) {
            postList.remove(index);
            postKeys.remove(index);
            notifyItemRemoved(index);
        }
    }

}
