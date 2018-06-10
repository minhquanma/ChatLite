package com.mmq.chatlite;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private ImageView imageViewProfile;
    private TextView txtViewProfile;
    private EditText edtDisplayName;
    private EditText edtBirthday;
    private Button buttonSaveProfile;
    private Button buttonLogoutProfile;
    private RadioGroup radioProfileGroup;
    private RadioButton radioMale;
    private RadioButton radioFemale;
    private TextView txtViewPassword;
    private ProgressDialog progressDialog;
    private FirebaseUser firebaseUser;

    private final int REQUEST_CODE = 1;
    private Uri selectedImage;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Mapping
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        imageViewProfile = view.findViewById(R.id.imgViewDlgProfile);
        txtViewProfile = view.findViewById(R.id.txtViewProfile);
        edtDisplayName = view.findViewById(R.id.edtDisplayName);
        edtBirthday = view.findViewById(R.id.edtBirthday);
        buttonSaveProfile = view.findViewById(R.id.buttonSaveProfile);
        buttonLogoutProfile = view.findViewById(R.id.buttonLogoutProfile);
        radioMale = view.findViewById(R.id.radioMale);
        radioFemale = view.findViewById(R.id.radioFemale);
        radioProfileGroup = view.findViewById(R.id.radioProfileGroup);
        txtViewPassword = view.findViewById(R.id.txtViewPassword);

        loadUserProfile(API.currentUser);

        buttonSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = ProgressDialog.show(getContext(), null, "Updating profile...", true);
                // Get the updated user info
                final Map updatedUser = getMapUpdatedUserProfile();

                // Upload image file
                if (selectedImage != null) {
                    API.firebaseStorage.child("USERS/" + API.currentUID + "." + getImageExtension(selectedImage))
                            .putFile(selectedImage)
                            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    // Get the uploaded image URL
                                    String avatarURL = task.getResult().getDownloadUrl().toString();

                                    // Assign into updated user object
                                    updatedUser.put("avatar", avatarURL);

                                    // Update user database
                                    API.firebaseRef.child("USERS")
                                            .child(API.currentUser.getUid())
                                            .updateChildren(updatedUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressDialog.dismiss();
                                            showSnackBar("Profile updated successful!");

                                            // Update the UI
                                            loadUserProfile(getUpdatedUserProfile());
                                        }
                                    });
                                }
                            });
                } else {
                    // Only update user database
                    API.firebaseRef.child("USERS")
                            .child(API.currentUser.getUid())
                            .updateChildren(updatedUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            showSnackBar("Profile updated successful!");

                            // Update the UI
                            loadUserProfile(getUpdatedUserProfile());
                        }
                    });
                }
            }
        });

        buttonLogoutProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                API.firebaseAuth.signOut();
                getActivity().stopService(new Intent(getContext(), MessageNotify.class));
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select image"), REQUEST_CODE);
            }
        });

        txtViewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangePassworDialog();
            }
        });

        // Return the view
        return view;
    }

    private void showSnackBar(String message){
        Snackbar snackbar = Snackbar
                .make(getActivity().findViewById(R.id.activity_main), message, Snackbar.LENGTH_LONG);

        snackbar.show();
    }

    public Users getUpdatedUserProfile() {
        Users profile = new Users();
        profile.setDisplayName(edtDisplayName.getText().toString());
        profile.setBirthDay(edtBirthday.getText().toString());

        if (radioProfileGroup.getCheckedRadioButtonId() == radioMale.getId())
            profile.setGender(true);
        else
            profile.setGender(false);
        return profile;
    }

    public Map<String, Object> getMapUpdatedUserProfile() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("displayName", edtDisplayName.getText().toString());
        map.put("birthDay", edtBirthday.getText().toString());

        if (radioProfileGroup.getCheckedRadioButtonId() == radioMale.getId())
            map.put("gender", true);
        else
            map.put("gender", false);
        return map;
    }

    private void loadUserProfile(Users user)
    {
        // Load avatar
        if (URLUtil.isValidUrl(user.getAvatar()))
            Picasso.with(getContext())
                    .load(user.getAvatar())
                    .into(imageViewProfile);

        if (user.isGender())
            radioMale.setChecked(true);
        else
            radioFemale.setChecked(true);

        txtViewProfile.setText(user.getDisplayName());
        edtDisplayName.setText(user.getDisplayName());
        edtBirthday.setText(user.getBirthDay());
    }

    private void showChangePassworDialog() {
        // Initialize dialog components
        android.support.v7.app.AlertDialog.Builder aBuilder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_password, null);

        // Dialog widgets mapping.
        final EditText oldPassword = dialogView.findViewById(R.id.oldPassword);
        final EditText newPassword = dialogView.findViewById(R.id.newPassword);
        final EditText repeatPassword = dialogView.findViewById(R.id.repeatPassword);
        Button buttonChangePassword = dialogView.findViewById(R.id.buttonChangePassword);

        aBuilder.setView(dialogView);
        aBuilder.setTitle("Change password");
        aBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do nothing.
            }
        });

        final android.support.v7.app.AlertDialog dialog = aBuilder.create();

        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            FirebaseUser user = API.firebaseAuth.getInstance().getCurrentUser();
            @Override
            public void onClick(View view) {
                final String $old = oldPassword.getText().toString();
                final String $new = newPassword.getText().toString();
                final String $repeat = repeatPassword.getText().toString();

                if ($old.length() < 6 || $new.length() < 6  || $repeat.length() < 6) {
                    Toast.makeText(getContext(), "Password must be 6 digits or over", Toast.LENGTH_SHORT).show();
                }
                else {
                    // Check password matching
                    if ($new.equals($repeat)) {
                        progressDialog = ProgressDialog.show(getContext(), null, "Please wait...", true);

                        AuthCredential credential = EmailAuthProvider
                                .getCredential(API.currentUser.getAccount(), $new);
                        // Do re-authentication
                        user.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // After re-auth => change password
                                user.updatePassword($new).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        dialog.cancel();
                                        showSnackBar("Password updated successful!");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        dialog.cancel();
                                        showSnackBar(e.getMessage());
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // If not matching
                        Toast.makeText(getContext(), "Password not matching", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            selectedImage = data.getData();

            try {
                Bitmap bitMap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImage);
                imageViewProfile.setImageBitmap(bitMap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getImageExtension(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
