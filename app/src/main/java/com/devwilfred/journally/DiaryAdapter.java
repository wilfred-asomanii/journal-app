package com.devwilfred.journally;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DiaryAdapter extends FirestoreRecyclerAdapter<Thought, DiaryAdapter.Holder> {

    RecyclerView mRecyclerView;
    TextView mErrorView;
    StorageReference mStorageReference;
    ThoughtClickListener mListener;

    interface  ThoughtClickListener {

        void onThoughtClicked(int pAdapterPosition, Thought pModel, ImageView pImageView);
    }

    DiaryAdapter(@NonNull FirestoreRecyclerOptions<Thought> options,
                 TextView pErrorView, StorageReference pReference, ThoughtClickListener pListener) {
        super(options);
        mErrorView = pErrorView;
        mStorageReference = pReference;
        mListener = pListener;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView pRecyclerView) {
        super.onAttachedToRecyclerView(pRecyclerView);
        this.mRecyclerView = pRecyclerView;
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        if (getItemCount() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            mErrorView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mErrorView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull final Holder holder, int position, @NonNull final Thought model) {
        ViewCompat.setTransitionName(holder.mImageView, model.getIdentifier());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                mListener.onThoughtClicked(holder.getAdapterPosition(), model, holder.mImageView);
            }
        });

        holder.itemView.setTag(model.getIdentifier());
        holder.bind(getItem(position));
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup pViewGroup, int pI) {
        return new Holder(LayoutInflater.from(pViewGroup.getContext())
                .inflate(R.layout.item_thought, pViewGroup, false));
    }

    class Holder extends RecyclerView.ViewHolder {

        TextView mTitle, mDescription, mWhen, mTag;
        ImageView mImageView;
        Thought mThought;

        Holder(@NonNull View itemView) {
            super(itemView);



            mTag = itemView.findViewById(R.id.thought_tag);
            mDescription = itemView.findViewById(R.id.thought_description);
            mTitle = itemView.findViewById(R.id.thought_title);
            mWhen = itemView.findViewById(R.id.thought_date);
            mImageView = itemView.findViewById(R.id.thought_image);



        }

        void bind(Thought pThought) {



            mThought = pThought;
            mTitle.setText(pThought.getTitle());
            mTag.setText(pThought.getTag());
            mDescription.setText(pThought.getDescription());
            mWhen.setText(DateUtils.getRelativeTimeSpanString(pThought.getWhen().getTime()));

            if (pThought.getPhotoUrl() != null) {

                Glide.with(itemView.getContext())
                        .using(new FirebaseImageLoader())
                        .load(mStorageReference.child(pThought.getPhotoUrl()))
                        .into(mImageView);
                mImageView.setVisibility(View.VISIBLE);
            }else {
                mImageView.setVisibility(View.GONE);
            }
        }


    }
}
