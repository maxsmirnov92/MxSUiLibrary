package net.maxsmr.jugglerhelper.fragments.alert.holder;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import net.maxsmr.commonutils.android.AppUtils;
import net.maxsmr.commonutils.data.CompareUtils;
import net.maxsmr.commonutils.data.FileHelper;
import net.maxsmr.jugglerhelper.fragments.alert.AlertDialogFragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

public final class FilePickerDialogFragmentsHolder extends AlertDialogFragmentsHolder {

    private static final String TAG_PICK_FILE_CHOICE = "pick_file_choice";

    @Nullable
    private IFilePickerConfigurator filePickerConfigurator;

    @Nullable
    private File cameraPictureFile;

    @Nullable
    private Activity activity;

    @Nullable
    private Fragment fragment;

    public FilePickerDialogFragmentsHolder() {
        this(null);
    }

    public FilePickerDialogFragmentsHolder(@Nullable Collection<String> tags) {
        super(mergeTags(Collections.singletonList(TAG_PICK_FILE_CHOICE), tags));
    }

    @Nullable
    public Context getContext() {
        if (activity != null) {
            return activity;
        } else if (fragment != null) {
            fragment.getContext();
        }
        return null;
    }

    public void setFilePickerConfigurator(@NotNull FilePickerDialogFragmentsHolder.IFilePickerConfigurator IFilePickerConfigurator) {
        this.filePickerConfigurator = IFilePickerConfigurator;
    }

    public void setActivityAndFragment(@Nullable Activity activity, @Nullable Fragment fragment) {
        this.activity = activity;
        this.fragment = fragment;
    }

    @Override
    public void onDialogButtonClick(@NotNull AlertDialogFragment fragment, int which) {
        super.onDialogButtonClick(fragment, which);
        if (CompareUtils.stringsEqual(fragment.getTag(), TAG_PICK_FILE_CHOICE, false)) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    pickFromGallery();
                    break;
                case DialogInterface.BUTTON_NEUTRAL:
                    pickFile();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    pickFromCamera();
                    break;
            }
        }
    }

    public String onResult(Context context, int resultCode, int requestCode, @Nullable Intent data) {
        String result = null;
        if (Activity.RESULT_OK == resultCode) {
            if (filePickerConfigurator == null) {
                throw new IllegalStateException(IFilePickerConfigurator.class.getSimpleName() + " is not specified");
            }
            if (requestCode == filePickerConfigurator.getPickFromCameraRequestCode()) {
                if (cameraPictureFile != null) {
                    result = cameraPictureFile.getAbsolutePath();
                }
            } else if (requestCode == filePickerConfigurator.getPickFromGalleryRequestCode() || requestCode == filePickerConfigurator.getPickFileRequestCode()) {
                result = data != null && data.getData() != null ?
                        FileHelper.getPath(context, data.getData()) : null;
            }
        }
        return result;
    }

    @Nullable
    public Uri onResultUri(Context context, int resultCode, int requestCode, Intent data) {
        String result = onResult(context, resultCode, requestCode, data);
        return result != null ? Uri.fromFile(new File(result)) : null;
    }

    public void showPictureModeDialog() {
        final Context context = getContext();
        if (context == null) {
            throw new IllegalStateException(Context.class.getSimpleName() + " is null");
        }
        if (filePickerConfigurator == null) {
            throw new IllegalStateException(IFilePickerConfigurator.class.getSimpleName() + " is not specified");
        }
        showAlert(TAG_PICK_FILE_CHOICE, new AlertDialogFragment.DefaultBuilder()
                .setIconResId(filePickerConfigurator.getAlertIconResId())
                .setMessage(filePickerConfigurator.getAlertMessage(context))
                .setCancelable(filePickerConfigurator.isAlertCancelable())
                .setButtons(filePickerConfigurator.shouldPickFromGallery() ? filePickerConfigurator.getPickFromGalleryAlertyButtonName(context) : null,
                        filePickerConfigurator.shouldPickFromFile() ? filePickerConfigurator.getPickFromFileAlertButtonName(context) : null,
                        filePickerConfigurator.shouldPickFromCamera() ? filePickerConfigurator.getPickFromCameraAlertButtonName(context) : null)
                .build(), false);
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

        if (filePickerConfigurator == null) {
            throw new IllegalStateException(IFilePickerConfigurator.class.getSimpleName() + " is not specified");
        }

        filePickerConfigurator.onBeforePickPictureFromGallery();

        boolean handled = false;
        if (fragment == null) {
            if (activity != null) {
                activity.startActivityForResult(intent, filePickerConfigurator.getPickFromGalleryRequestCode());
                handled = true;
            }
        } else {
            fragment.startActivityForResult(intent, filePickerConfigurator.getPickFromGalleryRequestCode());
            handled = true;
        }
        if (!handled) {
            throw new RuntimeException("Activity or fragment is not specified");
        }
    }

    private void pickFromCamera() {

        Context context = getContext();
        if (context == null) {
            throw new IllegalStateException("Not attached to context");
        }

        if (filePickerConfigurator == null) {
            throw new IllegalStateException(IFilePickerConfigurator.class.getSimpleName() + " is not specified");
        }

        if (filePickerConfigurator.shouldDeletePreviousFile() && FileHelper.isFileExists(cameraPictureFile)) {
            deleteCameraPictureFile();
        }

        cameraPictureFile = filePickerConfigurator.newCameraPictureFile();

        final String fileUriProviderAuthority = filePickerConfigurator.getFileProviderAuthorityPostfix();

        final Uri fileUri;
        if (!TextUtils.isEmpty(fileUriProviderAuthority)) {
            fileUri = FileProvider.getUriForFile(context, context.getPackageName() + "." + fileUriProviderAuthority, cameraPictureFile);
        } else {
            // strict mode must be disabled in that case!
            AppUtils.disableFileUriStrictMode();
            fileUri = Uri.fromFile(cameraPictureFile);
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        FileHelper.createNewFile(cameraPictureFile.getName(), cameraPictureFile.getParent(), true);

        filePickerConfigurator.onBeforePickPictureFromCamera();

        boolean handled = false;
        if (fragment == null) {
            if (activity != null) {
                activity.startActivityForResult(intent, filePickerConfigurator.getPickFromCameraRequestCode());
                handled = true;
            }
        } else {
            fragment.startActivityForResult(intent, filePickerConfigurator.getPickFromCameraRequestCode());
            handled = true;
        }
        if (!handled) {
            throw new RuntimeException("Activity or fragment is not specified");
        }
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");

        if (filePickerConfigurator == null) {
            throw new IllegalStateException(IFilePickerConfigurator.class.getSimpleName() + " is not specified");
        }

        filePickerConfigurator.onBeforePickFile();

        boolean handled = false;
        if (fragment == null) {
            if (activity != null) {
                activity.startActivityForResult(intent, filePickerConfigurator.getPickFileRequestCode());
                handled = true;
            }
        } else {
            fragment.startActivityForResult(intent, filePickerConfigurator.getPickFileRequestCode());
            handled = true;
        }
        if (!handled) {
            throw new RuntimeException("Activity or fragment is not specified");
        }
    }

    private void deleteCameraPictureFile() {
        FileHelper.deleteFile(cameraPictureFile);
        cameraPictureFile = null;
    }

    public interface IFilePickerConfigurator {

        @NotNull
        File newCameraPictureFile();

        boolean shouldDeletePreviousFile();

        String getFileProviderAuthorityPostfix();

        @DrawableRes
        int getAlertIconResId();

        @Nullable
        String getAlertMessage(@NotNull Context context);

        boolean isAlertCancelable();

        @Nullable
        String getPickFromGalleryAlertyButtonName(@NotNull Context context);

        @Nullable
        String getPickFromFileAlertButtonName(@NotNull Context context);

        @Nullable
        String getPickFromCameraAlertButtonName(@NotNull Context context);

        boolean shouldPickFromGallery();

        boolean shouldPickFromFile();

        boolean shouldPickFromCamera();

        int getPickFromGalleryRequestCode();

        int getPickFromCameraRequestCode();

        int getPickFileRequestCode();

        void onBeforePickPictureFromGallery();

        void onBeforePickPictureFromCamera();

        void onBeforePickFile();
    }

}