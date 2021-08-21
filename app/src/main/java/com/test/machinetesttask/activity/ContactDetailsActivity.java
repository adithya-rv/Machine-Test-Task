package com.test.machinetesttask.activity;

import static com.test.machinetesttask.common.Utils.getValidString;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.test.machinetesttask.R;
import com.test.machinetesttask.model.ContactsListModel;
import com.test.machinetesttask.viewmodel.ContactDetailsViewModel;

@SuppressWarnings("ALL")
public class ContactDetailsActivity extends AppCompatActivity {

    ContactsListModel.Datum data;
    ImageView profileImage;
    TextView nameValue;
    TextView emailValue;
    TextView idValue;
    ContactDetailsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);

        setObservable();
    }

    private void setObservable() {
        viewModel = ViewModelProviders.of(this).get(ContactDetailsViewModel.class);
        viewModel.getConfiguration(getIntent()).observe(this, contactsListModel -> {
            if (contactsListModel != null) {
                data = contactsListModel;
                initView();
                initListeners();
                setValues();
            }
        });
    }

    private void initView() {
        profileImage = findViewById(R.id.profileImage);
        nameValue = findViewById(R.id.nameValue);
        emailValue = findViewById(R.id.emailValue);
        idValue = findViewById(R.id.idValue);
    }

    private void initListeners() {
        emailValue.setOnClickListener(view -> {
            if (!emailValue.getText().toString().equalsIgnoreCase("-")) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + data.getEmail())); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, data.getEmail());
                startActivity(intent);
            }
        });

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setValues() {
        Glide.with(this).load(data.getAvatar())
                .error(getResources().getDrawable(R.drawable.icn_profile))
                .into(profileImage);

        nameValue.setText(String.format("%s %s",
                getValidString(data.getFirstName()),
                getValidString(data.getLastName())));

        emailValue.setText(Html.fromHtml("<u>" + getValidString(data.getEmail()) + "</u>"));
        idValue.setText(getValidString(String.format("%s", data.getId())));
    }

}