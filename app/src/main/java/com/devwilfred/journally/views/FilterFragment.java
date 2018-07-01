package com.devwilfred.journally.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.devwilfred.journally.R;
import com.devwilfred.journally.model.Thought;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;

public class FilterFragment extends DialogFragment {

    public static final String TAG = "com.devwilfred.filter";


    private RecyclerView filterRecycler;
    private ReceiveFilter mReceiveFilter;
    FirestoreRecyclerOptions<Thought> options;
    private String userUid;
    Adapter adapter;



    public static FilterFragment newInstance(String userUid) {
        FilterFragment fragment = new FilterFragment();
        Bundle bundle = new Bundle();
        bundle.putString("uuid", userUid);
        fragment.setArguments(bundle);

        return fragment;
    }

    interface ReceiveFilter {
        void onReceivedFilter(Thought pFilter);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_filter_picker, null);

        filterRecycler = v.findViewById(R.id.filter_recycler);

        if (getArguments() != null) userUid = getArguments().getString("uuid");

        setUpRecycler();

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.filter_dialog_title)
                .setCancelable(true)
                .create();
    }



    public void setUpRecycler() {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.setFirestoreSettings(new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true).build());

        Query query = firebaseFirestore.collection(userUid).orderBy(getString(R.string.field_tag));

        options = new FirestoreRecyclerOptions.Builder<Thought>()
                .setQuery(query, Thought.class).build();

        filterRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new Adapter(options);
        adapter.startListening();
        filterRecycler.setAdapter(adapter);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mReceiveFilter = (ReceiveFilter) context;
    }

    @Override
    public void onStop() {
        super.onStop();

        adapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (adapter != null) {
            adapter.startListening();
        }

    }

    class Adapter extends FirestoreRecyclerAdapter<Thought, Holder> {

        /**
         * Create a new RecyclerView mDiaryAdapter that listens to a Firestore Query.  See {@link
         * FirestoreRecyclerOptions} for configuration mOptions.
         *
         * @param options configurations for firebase; Query, etc
         */
        Adapter(@NonNull FirestoreRecyclerOptions<Thought> options) {
            super(options);
        }

        @Override
        protected void onBindViewHolder(@NonNull Holder holder, int position, @NonNull final Thought model) {
            holder.tagName.setText(model.getTag());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View pView) {
                    mReceiveFilter.onReceivedFilter(model);
                    dismiss();
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

        Holder(@NonNull View itemView) {
            super(itemView);

            tagName = itemView.findViewById(R.id.filter_tag);
        }
    }
}