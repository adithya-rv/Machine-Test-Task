package com.test.machinetesttask.adapter;

import static com.test.machinetesttask.common.Utils.getValidString;

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
import com.test.machinetesttask.R;
import com.test.machinetesttask.common.RecyclerViewClickListener;
import com.test.machinetesttask.model.ContactsListModel;

import java.util.List;

@SuppressWarnings("ALL")
public class ContactsListAdapter extends RecyclerView.Adapter {

    private List<ContactsListModel.Datum> list;
    private Context context;
    private RecyclerViewClickListener recyclerViewClickListener;

    public ContactsListAdapter(List<ContactsListModel.Datum> list, Context context, RecyclerViewClickListener recyclerViewClickListener) {
        this.list = list;
        this.context = context;
        this.recyclerViewClickListener = recyclerViewClickListener;
    }

    @NonNull
    @Override

    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.layout_contacts_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).onBind(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView nameValue, emailValue;
        TextView name, email;
        View profileImageContainer;
        TextView label;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            nameValue = itemView.findViewById(R.id.nameValue);
            emailValue = itemView.findViewById(R.id.emailValue);

            name = itemView.findViewById(R.id.name);
            email = itemView.findViewById(R.id.email);
            profileImageContainer = itemView.findViewById(R.id.profileImageContainer);

            label = itemView.findViewById(R.id.label);
        }

        @SuppressLint("SimpleDateFormat")
        public void onBind(int position) {
            label.setVisibility(View.GONE);

            profileImage.setVisibility(View.VISIBLE);
            profileImageContainer.setVisibility(View.VISIBLE);
            nameValue.setVisibility(View.VISIBLE);
            name.setVisibility(View.VISIBLE);
            email.setVisibility(View.VISIBLE);
            emailValue.setVisibility(View.VISIBLE);

            Glide.with(context).load(list.get(position).getAvatar())
                    .error(context.getResources().getDrawable(R.drawable.icn_profile))
                    .into(profileImage);
            nameValue.setText(String.format("%s %s",
                    getValidString(list.get(position).getFirstName()),
                    getValidString(list.get(position).getLastName())));
            emailValue.setText(getValidString(list.get(position).getEmail()));


            itemView.setOnClickListener(view -> recyclerViewClickListener.onClick(view, position));

        }

    }
}
