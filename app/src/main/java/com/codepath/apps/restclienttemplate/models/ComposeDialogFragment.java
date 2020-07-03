package com.codepath.apps.restclienttemplate.models;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.databinding.FragmentComposeBinding;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

public class ComposeDialogFragment extends DialogFragment {

    private EditText etCompose;
    private TextView tvCount;
    private Button btnTweet;
    private Context context = getContext();
    private FragmentComposeBinding binding;

    int charCount;
    Tweet tweet;

    public static final String TAG = "ComposeDialogFragment";
    public static final Tweet NO_REPLY = null;
    public static final int MAX_TWEET_LENGTH = 280;

    public interface ComposeDialogListener {
        void onFinishComposeDialog(String inputText);
    }

    public ComposeDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static ComposeDialogFragment newInstance(Tweet inReplyTo) {
        ComposeDialogFragment frag = new ComposeDialogFragment();
        Bundle args = new Bundle();
        if (inReplyTo != null) {
            args.putParcelable("tweet", Parcels.wrap(inReplyTo));
        }
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComposeBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        if (getArguments() != null) {
            tweet = Parcels.unwrap(getArguments().getParcelable("tweet"));
        }
        return view;
    }

    // TODO: Make separate for reply and tweet

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        etCompose = view.findViewById(R.id.etCompose);
        tvCount = view.findViewById(R.id.tvCount);
        btnTweet = view.findViewById(R.id.btnTweet);

        tvCount.setText(String.format("%-3d/%d", charCount, MAX_TWEET_LENGTH));

        // Fetch arguments from bundle and set title
        getDialog().setTitle("Tweet");

        // If tweet is a reply
        if (tweet != null) {
            etCompose.setText("@" + tweet.user.screenName + " ");
        }

        // Show soft keyboard automatically and request focus to field
        etCompose.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // Allow character counter to be changed
        etCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @SuppressLint("DefaultLocale")
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                Log.d(TAG, "onTextChanged");
                charCount = charSequence.length();
                tvCount.setText(String.format("%-3d/%d", charCount, MAX_TWEET_LENGTH));
                if (charCount > MAX_TWEET_LENGTH) {
                    tvCount.setTextColor(ContextCompat.getColor(tvCount.getContext(), R.color.accent_red));
                }
                if (charCount <= MAX_TWEET_LENGTH) {
                    tvCount.setTextColor(ContextCompat.getColor(tvCount.getContext(), R.color.primary_text));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etCompose.getText().length() <= MAX_TWEET_LENGTH) {
                    String replyTo = "";
                    if (tweet != null) {
                        replyTo = String.valueOf(tweet.id);
                    }
                    ComposeDialogListener listener = (ComposeDialogListener) getActivity();
                    listener.onFinishComposeDialog(replyTo + "/" + etCompose.getText().toString());
                    // Close the dialog and return back to the parent activity
                    dismiss();
                }
            }
        });
    }

}
