package gor.alaverdyan.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DatabaseReference;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvatarManager {

    private static final String TAG = "AvatarManager";
    private final AppCompatActivity activity;
    private final DatabaseReference userRef;
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    public AvatarManager(AppCompatActivity activity, DatabaseReference userRef, ActivityResultLauncher<PickVisualMediaRequest> pickMedia) {
        this.activity = activity;
        this.userRef = userRef;
        this.pickMedia = pickMedia;
    }

    public void showAvatarSelectionDialog() {
        if (userRef == null) {
            Toast.makeText(activity, R.string.log_in_to_set_avatar, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            BottomSheetDialog dialog = new BottomSheetDialog(activity, R.style.BottomSheetDialogTheme);
            View view = activity.getLayoutInflater().inflate(R.layout.dialog_choose_avatar, null);

            view.findViewById(R.id.btnRemoveAvatar).setOnClickListener(v -> {
                updateProfileAvatar(null, null, activity.getString(R.string.avatar_removed));
                dialog.dismiss();
            });

            view.findViewById(R.id.btnPickGallery).setOnClickListener(v -> {
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
                dialog.dismiss();
            });

            RecyclerView rvEmojis = view.findViewById(R.id.rvEmojiGrid);
            List<String> emojis = Arrays.asList(
                    "😎", "🤓", "🤡", "🤖", "🐱", "🐶", "🦊", "🦁",
                    "🚀", "⭐️", "🔥", "💎", "🎮", "⚽️", "🏀", "🎸",
                    "🍎", "🍕", "🍦", "🛸", "👻", "🦄", "🐼", "🐧"
            );

            rvEmojis.setLayoutManager(new GridLayoutManager(activity, 4));
            EmojiAdapter adapter = new EmojiAdapter(emojis, emoji -> {
                updateProfileAvatar(emoji, null, activity.getString(R.string.avatar_updated_emoji));
                dialog.dismiss();
            });
            rvEmojis.setAdapter(adapter);

            view.findViewById(R.id.btnCloseEmoji).setOnClickListener(v -> dialog.dismiss());

            dialog.setContentView(view);
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing avatar dialog", e);
        }
    }

    public void handleGalleryImage(Uri uri) {
        try {
            InputStream inputStream = activity.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) {
                Toast.makeText(activity, R.string.failed_to_load_image, Toast.LENGTH_SHORT).show();
                return;
            }
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

            updateProfileAvatar(null, base64Image, activity.getString(R.string.avatar_updated_gallery));
        } catch (Exception e) {
            Toast.makeText(activity, R.string.failed_to_load_image, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfileAvatar(String emoji, String base64Image, String successMessage) {
        if (userRef != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("avatarEmoji", emoji);
            updates.put("avatarUrl", base64Image);

            userRef.updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (successMessage != null) {
                        Toast.makeText(activity, successMessage, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                    Toast.makeText(activity, "Update failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private static class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.ViewHolder> {
        private final List<String> emojis;
        private final OnEmojiClickListener listener;

        public interface OnEmojiClickListener {
            void onEmojiClick(String emoji);
        }

        public EmojiAdapter(List<String> emojis, OnEmojiClickListener listener) {
            this.emojis = emojis;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emoji, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String emoji = emojis.get(position);
            holder.text.setText(emoji);
            holder.itemView.setOnClickListener(v -> listener.onEmojiClick(emoji));
        }

        @Override
        public int getItemCount() {
            return emojis.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                text = itemView.findViewById(R.id.tvEmojiItem);
            }
        }
    }
}
