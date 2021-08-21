package com.test.machinetesttask.activity;

import static com.test.machinetesttask.common.Utils.convertDpToPixel;
import static com.test.machinetesttask.common.Utils.getValidEmail;
import static com.test.machinetesttask.common.Utils.getValidString;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.test.machinetesttask.common.FilePathUri;
import com.test.machinetesttask.model.ContactsListModel;
import com.test.machinetesttask.viewmodel.ContactsListViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ALL")
public class ContactsListActivity extends AppCompatActivity {

    private RecyclerView contactsListRcv;
    private SwipeRefreshLayout refreshLayout;
    private ContactsListAdapter contactsListAdapter;
    private final List<ContactsListModel.Datum> list = new ArrayList<>();
    private ContactsListViewModel viewModel;
    private int page = 1;
    private int totalPages = 0;

    private static final int FILE_SELECT_CODE = 1212;
    private static final int IMAGECPATURE_REQUEST = 1999;
    private static final int ACTION_TAKE_VIDEO = 7272;
    private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;
    public static final int PERMISSION_REQUEST_CODE = 1234;
    public static final String FILES_FOLDER = "/files";

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

    private void startEditing(RecyclerView.ViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        ContactsListModel.Datum deleted = list.get(position);
        list.remove(position);
        contactsListAdapter.notifyItemRemoved(position);
        showToast(getString(R.string.edit_msg));
        initBottomSheet(deleted, position);
    }

    private void initBottomSheet(ContactsListModel.Datum data, int position) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(initBottomSheetViews(data, bottomSheetDialog));
        bottomSheetDialog.setOnDismissListener(dialogInterface -> {
            if(data.getEmail()!=null) {
                list.add(position, data);
                contactsListAdapter.notifyItemInserted(position);
            }
        });
        bottomSheetDialog.show();
    }

    private View initBottomSheetViews(ContactsListModel.Datum data, BottomSheetDialog bottomSheetDialog) {
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

        if (data.getEmail() != null) {
            Glide.with(this).load(data.getAvatar())
                    .error(getResources().getDrawable(R.drawable.icn_profile))
                    .into(profileImage);

            nameValue.setText(String.format("%s",
                    getValidString(data.getFirstName())));

            lastNameValue.setText(String.format("%s",
                    getValidString(data.getLastName())));

            emailValue.setText(Html.fromHtml("<u>" + getValidString(data.getEmail()) + "</u>"));
            idValue.setText(getValidString(String.format("%s", data.getId())));
        }
        save.setOnClickListener(view -> {
            if (nameValue.getText().toString().trim().length() == 0) {
                showToast(getString(R.string.invalid_first_name));
                return;
            }
            if (lastNameValue.getText().toString().trim().length() == 0) {
                showToast(getString(R.string.invalid_last_name));
                return;
            }
            if (!getValidEmail(emailValue.getText().toString())) {
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
        });

        return bottomSheetView;
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

    private void requestPermissionForCameraAndMicrophone() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(this,
                    "Permission",
                    Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    CAMERA_MIC_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(reqCode, permissions, grantResults);
        if (reqCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionCallBack.onPermissionGranted();
            } else {
                permissionCallBack.onPermissionRejected();
            }
        }
    }

    public interface PermissionCallback {

        void onPermissionGranted();

        void onPermissionRejected();
    }

    private PermissionCallback permissionCallBack;

    public boolean checkIfPermissionIsGranted(String permission) {

        int permissionCheck = ContextCompat.checkSelfPermission(this, permission);

        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission(String[] permissions, PermissionCallback permissionCallBack) {

        this.permissionCallBack = permissionCallBack;

        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);

    }

    private void checkStorage() {
        if (!checkIfPermissionIsGranted(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                || !checkIfPermissionIsGranted(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                || !checkIfPermissionIsGranted(Manifest.permission.CAMERA)
                || !checkIfPermissionIsGranted(Manifest.permission.RECORD_AUDIO)
        ) {
            requestPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO}, new PermissionCallback() {
                @Override
                public void onPermissionGranted() {
                    try {
                        openImageViewContent();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onPermissionRejected() {
                    showToast("Permission rejected");
                    checkStorage();
                }
            });
        } else {
            try {
                openImageViewContent();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void checkStorageForVideo() {
        if (!checkIfPermissionIsGranted(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                || !checkIfPermissionIsGranted(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                || !checkIfPermissionIsGranted(Manifest.permission.CAMERA)
                || !checkIfPermissionIsGranted(Manifest.permission.RECORD_AUDIO)
        ) {
            requestPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO}, new PermissionCallback() {
                @Override
                public void onPermissionGranted() {
                    try {
                        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        startActivityForResult(takeVideoIntent, FILE_SELECT_CODE);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onPermissionRejected() {
                    showToast("Permission rejected");
                    checkStorageForVideo();
                }
            });
        } else {
            try {
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(takeVideoIntent, FILE_SELECT_CODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkPermissionForCameraAndMicrophone() {
        int resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return resultCamera == PackageManager.PERMISSION_GRANTED &&
                resultMic == PackageManager.PERMISSION_GRANTED;
    }

    public void captureImage(View view) {
        checkStorage();
    }

    public void pinLocation(View view) {
    }

    public void captureVideo(View view) {
        checkStorageForVideo();
    }

    private void openImageViewContent() throws Exception {

        Uri outputFileUri;

        final File root = new File(getRootFolderOfApp(this));
        root.mkdirs();

        final String fname = getAppName(this) + "_" + System.currentTimeMillis()
                + "." + getAppName(this);

        final File sdImageMainDirectory = new File(root.getPath(), fname);

        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        intentList = addIntentsToList(this, intentList, takePhotoIntent);


        if (intentList.size() > 0) {

            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                    ("Pick image From"));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }

        startActivityForResult(chooserIntent, IMAGECPATURE_REQUEST);
    }


    public static String getRootFolderOfApp(Context mContext) throws Exception {
        String appName = getAppName(mContext);
        String dirName = Environment.getExternalStorageDirectory().toString() + File.separator + appName + File.separator;
        isDirectoryExists(dirName);
        return dirName;
    }


    public static boolean isDirectoryExists(String dirName) throws Exception {
        File dirStructure = new File(dirName);
        if (!dirStructure.exists()) {
            dirStructure.mkdirs();
            return true;
        } else {
            return false;
        }
    }

    public static String getAppName(Context mContext) throws Exception {
        return mContext.getResources().getString(R.string.app_name);
    }

    Uri outputFileUri;

    private List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE && (resultCode == Activity.RESULT_OK)) {
            try {
                Uri uri = data.getData();
                String filename;
                int filesize = 1;

                String mimeType = getContentResolver().getType(uri);
                if (mimeType == null) {
                    String path = getPath(this, uri);
                    if (path == null) {
                    } else {
                        File file = new File(path);
                        filename = file.getName();
                    }
                } else {
                    Uri returnUri = data.getData();
                    Cursor returnCursor = getContentResolver().query(returnUri, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                    returnCursor.moveToFirst();
                    filename = returnCursor.getString(nameIndex);
                    String size = Long.toString(returnCursor.getLong(sizeIndex));
                }
                File fileSave = getExternalFilesDir(null);
                String sourcePath = getExternalFilesDir(null).toString();
                File encFile = null;
                File fileDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + FILES_FOLDER);

                if (!fileDir.exists())
                    fileDir.mkdir();

                String newFileName = "video" + "-"
                        + new Date().getTime() / 1000
                        + getFileExtension(data);

                encFile = new File(fileDir, newFileName);
                try {
                    copyFileStream(encFile, uri, this);
                    String size = getFileSize(encFile);
                    showToast("Video saved at" + encFile.getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == IMAGECPATURE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                boolean isCamera;
                String filePath = "";

                if (data != null && data.getExtras() != null) {
                    Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                    Calendar calendar = Calendar.getInstance();
                    File file = new File(getFilesDir(), calendar.getTimeInMillis() + ".jpg");
                    try {
                        try (FileOutputStream out = new FileOutputStream(file)) {
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            filePath = file.getPath();
                        } catch (IOException e) {
                            e.getMessage();
                        }
                    } catch (Exception e) {
                        e.getMessage();
                        try {
                            filePath = getImagePathFromInputStream(getContentResolver().openInputStream(Objects.requireNonNull(data.getData())));
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                    }
                } else if (data != null && data.getData() != null) {

                    try {
                        filePath = getPDFPath(data.getData());
                        if (filePath == null || filePath.length() == 0) {
                            try {
                                filePath = getImagePathFromInputStream(getContentResolver().openInputStream(Objects.requireNonNull(data.getData())));
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        try {
                            filePath = getImagePathFromInputStream(getContentResolver().openInputStream(Objects.requireNonNull(data.getData())));
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                String inputPath = filePath;
                String outputFilePath = "";
                File in = new File(inputPath);
                File encFile = null;
                try {
                    File fileDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + FILES_FOLDER);

                    if (!fileDir.exists())
                        fileDir.mkdir();

                    String newFileName = "image" + "-"
                            + new Date().getTime() / 1000
                            + ".jpg";

                    encFile = new File(fileDir, newFileName);
                    copyFile(inputPath, encFile.getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String displayName = encFile.getName();
                Log.d("Picked", displayName + "   " + encFile.getPath());
                showToast("Image saved at" + encFile.getPath());

            }
        } else if (requestCode == ACTION_TAKE_VIDEO) {
            if (resultCode == Activity.RESULT_OK) {
                Uri videoUri = data.getData();
                File file = new File(videoUri.getPath());
            }
        }
    }

    public static String getPath(Context context, Uri uri) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // DocumentProvider
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private String getFileExtension(Intent data) {
        Uri uri = data.getData();
        String uriString = uri.toString();
        File myFile = new File(uriString);
        String path = myFile.getAbsolutePath();
        String displayName = null;

        if (uriString.startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = this.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        } else if (uriString.startsWith("file://")) {
            displayName = myFile.getName();
        }
        File fileDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + FILES_FOLDER);

        if (!fileDir.exists())
            fileDir.mkdir();
        String newFileName = "image" + "-"
                + new Date().getTime() / 1000
                + displayName;

        File encFile = new File(fileDir, newFileName);
        File file = new File(uri.getPath());
        copy(file, encFile);
        Log.d("Picked", displayName + "   " + path);
        return displayName;
    }

    public static void copy(File src, File dst) {
        try {
            InputStream in = null;

            in = new FileInputStream(src);
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            in.close();
        } catch (Exception e) {
            Log.e("TAG", "copy error\n\n\n\n\n\n");
            Log.e("TAG", e.getMessage());
            e.printStackTrace();
        }
    }

    private void copyFileStream(File dest, Uri uri, Context context)
            throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            is.close();
            os.close();
        }
    }

    private String getFileSize(File file) {
        // Get length of file in bytes
        long fileSizeInBytes = file.length();
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        long fileSizeInKB = fileSizeInBytes / 1024;
        //  Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        long fileSizeInMB = fileSizeInKB / 1024;
        return (fileSizeInMB < 1) ? fileSizeInKB + " KB" : fileSizeInMB + " MB";
    }

    public String getImagePathFromInputStream(InputStream in) throws IOException {
        File file = new File(getFilesDir(), Calendar.getInstance().getTimeInMillis() + ".jpg");
        try (OutputStream output = new FileOutputStream(file)) {
            byte[] buffer = new byte[4 * 1024]; // or other buffer size
            int read;

            while ((read = in.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
        } finally {
            in.close();
        }
        return file.getPath();
    }

    public String getPDFPath(Uri uri) {

        Activity activity = this;

        if (uri.toString().startsWith("file:")) {
            return uri.getPath();
        }

        String id = DocumentsContract.getDocumentId(uri);

        if (!TextUtils.isEmpty(id)) {

            if (id.startsWith("raw:")) {
                return id.replaceFirst("raw:", "");

            }
            try {
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return FilePathUri.getDataColumn(activity, contentUri, null, null);
            } catch (NumberFormatException e) {
                return null;
            }

        }
        return null;

    }

    private void copyFile(String inputPath, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add) {
            showToast(getString(R.string.add_msg));
            initBottomSheet(new ContactsListModel.Datum(), list.size());
        }
        return super.onOptionsItemSelected(item);
    }
}