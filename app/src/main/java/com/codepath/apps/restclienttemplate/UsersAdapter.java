package com.codepath.apps.restclienttemplate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.databinding.ItemProfileBinding;
import com.codepath.apps.restclienttemplate.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    Context context;
    List<User> users;
    TwitterClient client;

    ItemProfileBinding binding;

    public static final String TAG = "UserAdapter";

    // Pass in the context and list of users
    public UsersAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
        client = TwitterApp.getRestClient(context);
    }

    // For each row, inflate the layout for a user
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ItemProfileBinding.inflate((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
                , parent, false);
        View view = binding.getRoot();
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    // RecyclerView tells us the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data
        final User user = users.get(position);
        // Bind the User with the ViewHolder
        holder.bind(user);
    }

    @Override
    public int getItemCount() { return users.size(); }

    // Clean all elements of the recycler
    public void clear() {
        users.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<User> userList) {
        users.addAll(userList);
        notifyDataSetChanged();
    }

    // Define a ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProfileImage;
        TextView tvProfileName;
        TextView tvScreenName;

        // itemView is a representation of one row in the RecyclerView (aka one User)
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = binding.ivProfileImage;
            tvProfileName = binding.tvProfileName;
            tvScreenName = binding.tvScreenName;
        }

        @SuppressLint("SetTextI18n")
        public void bind(User user) {
            Glide.with(context).load(user.profileImageUrl).into(ivProfileImage);
            tvProfileName.setText(user.name);
            tvScreenName.setText("@"+user.screenName);
        }

    }

}
