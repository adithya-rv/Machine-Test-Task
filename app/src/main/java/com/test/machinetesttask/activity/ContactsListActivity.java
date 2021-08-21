package com.test.machinetesttask.activity;

import static com.test.machinetesttask.common.Utils.convertDpToPixel;
import static com.test.machinetesttask.common.Utils.getValidString;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.test.machinetesttask.R;
import com.test.machinetesttask.adapter.ContactsListAdapter;
import com.test.machinetesttask.common.Constants;
import com.test.machinetesttask.model.ContactsListModel;
import com.test.machinetesttask.viewmodel.ContactsListViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ContactsListActivity extends AppCompatActivity {

    RecyclerView contactsListRcv;
    SwipeRefreshLayout refreshLayout;
    ContactsListAdapter contactsListAdapter;
    List<ContactsListModel.Datum> list = new ArrayList<>();
    List<ContactsListModel.Datum> masterlist = new ArrayList<>();
    ContactsListViewModel viewModel;
    int page = 1;
    int totalPages = 0;
    private Toast toast = null;
    boolean isBottomSheetValueChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);

        initView();
        initListeners();
        setAdapter();
        setSwipe();
        preApi();
        setObservable();
    }

    private void setSwipe() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                switch (direction) {
                    case ItemTouchHelper.LEFT:
                    case ItemTouchHelper.RIGHT:
                        startEditing(viewHolder);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;

                    Paint p = new Paint();
                    p.setColor(getResources().getColor(R.color.purple_700, null));
                    Paint textPaint = new Paint();
                    textPaint.setColor(getResources().getColor(R.color.white, null));
                    textPaint.setAntiAlias(true);
                    textPaint.setTextSize((convertDpToPixel(12.0f, ContactsListActivity.this)));
                    if (dX > 0) {
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                                (float) itemView.getBottom(), p);
                        c.drawText(getString(R.string.swipe_to_edit), itemView.getRight() - convertDpToPixel(100f, ContactsListActivity.this),
                                itemView.getTop() + (itemView.getHeight() >> 1), textPaint);
                    } else {
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                (float) itemView.getRight(), (float) itemView.getBottom(), p);
                        c.drawText(getString(R.string.swipe_to_edit), itemView.getRight() - convertDpToPixel(100f, ContactsListActivity.this),
                                itemView.getTop() + (itemView.getHeight() >> 1), textPaint);
                    }

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(contactsListRcv);
    }

    private void preApi() {
        refreshLayout.setRefreshing(true);
        if (page == 1) {
            list.clear();
        }
    }

    private void setObservable() {
        viewModel = ViewModelProviders.of(this).get(ContactsListViewModel.class);
        viewModel.getConfiguration(page).observe(this, contactsListModel -> {
            if (contactsListModel != null) {
                addToList(contactsListModel);
            } else {
                error();
            }
            refreshLayout.setRefreshing(false);
        });

    }

    private void showToast(String msg) {
        Toast.makeText(ContactsListActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    private void error() {
        showToast(getString(R.string.sww));
        list.clear();
        viewModel.fetchFromDB();
    }

    private void addToList(ContactsListModel contactsListModel) {
        page = contactsListModel.getPage();
        totalPages = contactsListModel.getTotalPages();
        if (page == 1) list.clear();
        if (list.size() < 10 && page < totalPages) {
            refreshLayout.setRefreshing(true);
            viewModel.getList(++page);
        }
        list.addAll(contactsListModel.getData());
        contactsListAdapter.notifyDataSetChanged();
    }

    private void clearMasterList() {
        masterlist.clear();
        masterlist.addAll(list);
    }

    private void setAdapter() {
        contactsListRcv.setLayoutManager(new LinearLayoutManager(this));
        contactsListAdapter = new ContactsListAdapter(list, this, (view, position) -> {
            Intent intent = new Intent(this, ContactDetailsActivity.class);
            Gson gson = new Gson();
            String json = gson.toJson(list.get(position));
            intent.putExtra(Constants.CONTACT_DETAILS, json);
            startActivity(intent);
        });
        contactsListRcv.setAdapter(contactsListAdapter);
    }

    private void startEditing(RecyclerView.ViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        ContactsListModel.Datum deleted = list.get(position);
        list.remove(position);
        contactsListAdapter.notifyItemRemoved(position);
        initBottomSheet(deleted, position);
    }

    private void initBottomSheet(ContactsListModel.Datum data, int position) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.edit_view, null);
        ImageView profileImage;
        TextView nameValue;
        TextView lastNameValue;
        TextView emailValue;
        TextView idValue;
        Button save;
        profileImage = bottomSheetView.findViewById(R.id.profileImage);
        nameValue = bottomSheetView.findViewById(R.id.nameValue);
        lastNameValue = bottomSheetView.findViewById(R.id.lastNameValue);
        emailValue = bottomSheetView.findViewById(R.id.emailValue);
        idValue = bottomSheetView.findViewById(R.id.idValue);
        save = bottomSheetView.findViewById(R.id.save);
        Glide.with(this).load(data.getAvatar())
                .error(getResources().getDrawable(R.drawable.icn_profile))
                .into(profileImage);

        nameValue.setText(String.format("%s",
                getValidString(data.getFirstName())));

        lastNameValue.setText(String.format("%s",
                getValidString(data.getLastName())));

        emailValue.setText(Html.fromHtml("<u>" + getValidString(data.getEmail()) + "</u>"));
        idValue.setText(getValidString(String.format("%s", data.getId())));

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nameValue.getText().toString().trim().length() == 0) {
                    showToast(getString(R.string.invalid_first_name));
                    return;
                }
                if (lastNameValue.getText().toString().trim().length() == 0) {
                    showToast(getString(R.string.invalid_last_name));
                    return;
                }
                if (!Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
                        Pattern.CASE_INSENSITIVE).matcher(emailValue.getText().toString()).find()) {
                    showToast(getString(R.string.invalid_email));
                    return;
                }
                if (nameValue.getText().toString().trim().length() == 0) {
                    showToast(getString(R.string.invalid_id));
                    return;
                }
                data.setFirstName(nameValue.getText().toString());
                data.setLastName(lastNameValue.getText().toString());
                data.setEmail(emailValue.getText().toString());
                try {
                    data.setId(Integer.valueOf(idValue.getText().toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                list.add(position, data);
                contactsListAdapter.notifyItemInserted(position);
            }
        });
        bottomSheetDialog.show();
    }

    private void initView() {
        contactsListRcv = findViewById(R.id.contactsListRcv);
        refreshLayout = findViewById(R.id.refreshLayout);
    }

    private void initListeners() {
        contactsListRcv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1)) {
                    if (page < totalPages) {
                        refreshLayout.setRefreshing(true);
                        viewModel.getList(++page);
                    }
                }
            }
        });

        refreshLayout.setOnRefreshListener(() -> {
            page = 1;
            refreshLayout.setRefreshing(true);
            viewModel.getList(page);
        });
    }
}