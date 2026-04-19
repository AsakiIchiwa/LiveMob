package com.codelab.app.ui.profile;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codelab.app.R;
import com.codelab.app.data.ProfileStore;
import com.codelab.app.data.UserProfile;

public class ProfileEditActivity extends AppCompatActivity {

    private EditText nameField, handleField, bioField;
    private ImageView avatarPreview;
    private LinearLayout avatarPicker;
    private int chosenVariant = 0;

    private static final int[] PALETTE = new int[] {
            Color.parseColor("#22D3EE"), Color.parseColor("#34D399"),
            Color.parseColor("#F87171"), Color.parseColor("#FBBF24"),
            Color.parseColor("#A78BFA"), Color.parseColor("#F472B6")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        nameField = findViewById(R.id.editName);
        handleField = findViewById(R.id.editHandle);
        bioField = findViewById(R.id.editBio);
        avatarPreview = findViewById(R.id.editAvatarPreview);
        avatarPicker = findViewById(R.id.avatarPicker);

        UserProfile p = ProfileStore.get(this).get();
        nameField.setText(p.name);
        handleField.setText(p.handle);
        bioField.setText(p.bio);
        chosenVariant = p.avatarVariant;
        applyAvatarPreview();
        buildAvatarPicker();

        findViewById(R.id.editBack).setOnClickListener(v -> finish());
        findViewById(R.id.editSave).setOnClickListener(v -> save());
    }

    private void applyAvatarPreview() {
        avatarPreview.setColorFilter(PALETTE[Math.floorMod(chosenVariant, PALETTE.length)],
                PorterDuff.Mode.SRC_IN);
    }

    private void buildAvatarPicker() {
        avatarPicker.removeAllViews();
        int sz = (int) (44 * getResources().getDisplayMetrics().density);
        int margin = (int) (6 * getResources().getDisplayMetrics().density);
        int pad = (int) (10 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < PALETTE.length; i++) {
            final int variant = i;
            ImageView swatch = new ImageView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sz, sz);
            lp.setMarginEnd(margin);
            swatch.setLayoutParams(lp);
            swatch.setBackgroundResource(R.drawable.bg_circle);
            swatch.setImageResource(R.drawable.ic_avatar);
            swatch.setPadding(pad, pad, pad, pad);
            swatch.setColorFilter(PALETTE[i], PorterDuff.Mode.SRC_IN);
            swatch.setOnClickListener(v -> {
                chosenVariant = variant;
                applyAvatarPreview();
            });
            avatarPicker.addView(swatch);
        }
    }

    private void save() {
        String name = nameField.getText().toString().trim();
        String handle = handleField.getText().toString().trim();
        String bio = bioField.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!TextUtils.isEmpty(handle) && !handle.startsWith("@")) handle = "@" + handle;

        final String finalHandle = handle;
        ProfileStore.get(this).update(p -> {
            p.name = name;
            p.handle = finalHandle;
            p.bio = bio;
            p.avatarVariant = chosenVariant;
        });
        Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
