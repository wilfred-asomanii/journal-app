package com.devwilfred.journally;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Date;

public class FilterFragment extends DialogFragment {

    public static final String TAG = "com.devwilfred.filter";


    private RecyclerView filterRecycler;
    private RecieveFilter mRecieveFilter;
    private Query mQuery;
    FirestoreRecyclerOptions<Thought> options;
    private FirebaseFirestore mFirebaseFirestore;
    private String userUid;


    public static FilterFragment newInstance(String userUid) {
        FilterFragment fragment = new FilterFragment();
        Bundle bundle = new Bundle();
        bundle.putString("uuid", userUid);
        fragment.setArguments(bundle);

        return fragment;
    }

    interface RecieveFilter {
        void onRevievedFilter(Thought pFilter);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_filter_picker, null);

        filterRecycler = v.findViewById(R.id.filter_recycler);

        userUid = getArguments().getString("uuid");

        setUpRecycler();

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("Select a Tag Filter")
                .setCancelable(true)
                .create();
    }



    public void setUpRecycler() {

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseFirestore.setFirestoreSettings(new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true).build());

        mQuery = mFirebaseFirestore.collection(userUid).orderBy("tag");

        options = new FirestoreRecyclerOptions.Builder<Thought>()
                .setQuery(mQuery, Thought.class).build();

        filterRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        Adapter adapter = new Adapter(options);
        adapter.startListening();
        filterRecycler.setAdapter(adapter);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mRecieveFilter = (RecieveFilter) context;
    }


    class Adapter extends FirestoreRecyclerAdapter<Thought, Holder> {

        /**
         * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
         * FirestoreRecyclerOptions} for configuration options.
         *
         * @param options
         */
        public Adapter(@NonNull FirestoreRecyclerOptions<Thought> options) {
            super(options);
        }

        @Override
        protected void onBindViewHolder(@NonNull Holder holder, int position, @NonNull final Thought model) {
            holder.tagName.setText(model.getTag());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View pView) {
                    mRecieveFilter.onRevievedFilter(model);
                }
            });
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup pViewGroup, int pI) {

            return new Holder(LayoutInflater.from(pViewGroup.getContext())
                    .inflate(R.layout.item_filter, pViewGroup, false));
        }

    }

    class Holder extends RecyclerView.ViewHolder {
        TextView tagName;

        public Holder(@NonNull View itemView) {
            super(itemView);

            tagName = itemView.findViewById(R.id.filter_tag);
        }
    }
}