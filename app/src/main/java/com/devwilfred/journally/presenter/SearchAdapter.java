package com.devwilfred.journally.presenter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.devwilfred.journally.R;
import com.devwilfred.journally.model.Thought;
import com.devwilfred.journally.views.ThoughtDetailActivity;
import com.devwilfred.journally.views.UpdateActivity;

import java.util.List;

/**
 * Adapter class for search results
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.Holder> {

   
    private List<Thought> mThoughts;


    public SearchAdapter(List<Thought> pThoughts) {
        mThoughts = pThoughts;
    }
    
    
    @Override
    public int getItemCount() {
        return mThoughts.size();
    }

 
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup pViewGroup, int viewType) {

                return new Holder(LayoutInflater.from(pViewGroup.getContext())
                        .inflate(R.layout.item_thought_no_image, pViewGroup, false));

    }

    @Override
    public void onBindViewHolder(@NonNull final Holder pViewHolder, int pI) {

        pViewHolder.itemView.setTag(mThoughts.get(pI).getIdentifier());

        pViewHolder.mTitle.setText(mThoughts.get(pI).getTitle());
        pViewHolder.mTag.setText(mThoughts.get(pI).getTag());
        pViewHolder.mDescription.setText(mThoughts.get(pI).getDescription());
        pViewHolder.mWhen.setText(DateUtils.getRelativeTimeSpanString(mThoughts.get(pI).getWhen().getTime()));

        pViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View pView) {
                pView.getContext().startActivity(
                        new Intent(pView.getContext(), UpdateActivity.class)
                                .putExtra(UpdateActivity.UPDATE_THOUGHT,
                                        mThoughts.get(pViewHolder.getAdapterPosition())));
                return true;
            }
        });

        pViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                Intent intent = new Intent(pViewHolder.itemView.getContext(),
                        ThoughtDetailActivity.class);
                intent.putExtra(ThoughtDetailActivity.VIEW_THOUGHT_EXTRA,
                        mThoughts.get(pViewHolder.getAdapterPosition()));

                pViewHolder.itemView.getContext().startActivity(intent);
            }
        });
    }


    class Holder extends RecyclerView.ViewHolder {

        TextView mTitle, mDescription, mWhen, mTag;
        ImageView mImageView;

        Holder(@NonNull final View itemView) {
            super(itemView);

            mTag = itemView.findViewById(R.id.thought_tag);
            mDescription = itemView.findViewById(R.id.thought_description);
            mTitle = itemView.findViewById(R.id.thought_title);
            mWhen = itemView.findViewById(R.id.thought_date);
            mImageView = itemView.findViewById(R.id.thought_image);


        }

    }
}
