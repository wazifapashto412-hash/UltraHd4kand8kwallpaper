package com.example.ultrahdwallpaper;

import android.app.Activity;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    private final ArrayList<String> wallpapers = new ArrayList<>();
    private final Set<String> favorites = new HashSet<>();
    private LinearLayout listContainer;
    private EditText searchBox;
    private String activeCategory = "All";
    private boolean favoritesOnly = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        favorites.addAll(getPreferences(MODE_PRIVATE).getStringSet("favorites", new HashSet<>()));
        loadWallpaperNames();
        buildScreen();
        renderWallpapers("");
    }

    private void loadWallpaperNames() {
        try {
            String[] files = getAssets().list("wallpapers");
            if (files != null) {
                Arrays.sort(files, String.CASE_INSENSITIVE_ORDER);
                for (String file : files) {
                    String lower = file.toLowerCase(Locale.ROOT);
                    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")) {
                        wallpapers.add(file);
                    }
                }
            }
        } catch (IOException ignored) { }
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(18), dp(16), dp(8));
        root.setBackgroundColor(Color.rgb(17, 18, 25));

        TextView title = text("Ultra HD Wallpapers", 25, Color.WHITE, true);
        root.addView(title);
        TextView subtitle = text("4K • 8K • Carefully selected backgrounds", 13, 0xFFBDB8CC, false);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
        subLp.bottomMargin = dp(14);
        root.addView(subtitle, subLp);

        searchBox = new EditText(this);
        searchBox.setSingleLine(true);
        searchBox.setTextColor(Color.WHITE);
        searchBox.setHintTextColor(0xFFAAA5B5);
        searchBox.setHint("Search wallpapers...");
        searchBox.setPadding(dp(14), 0, dp(14), 0);
        searchBox.setBackground(roundRect(0xFF292735, 14));
        root.addView(searchBox, new LinearLayout.LayoutParams(-1, dp(48)));
        searchBox.addTextChangedListener(new SimpleTextWatcher(s -> renderWallpapers(s)));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button all = button("All");
        Button fav = button("Favorites");
        actions.addView(all, new LinearLayout.LayoutParams(0, dp(42), 1));
        actions.addView(fav, new LinearLayout.LayoutParams(0, dp(42), 1));
        LinearLayout.LayoutParams actLp = new LinearLayout.LayoutParams(-1, -2);
        actLp.topMargin = dp(10);
        root.addView(actions, actLp);
        all.setOnClickListener(v -> { favoritesOnly = false; activeCategory = "All"; renderWallpapers(searchBox.getText().toString()); });
        fav.setOnClickListener(v -> { favoritesOnly = true; renderWallpapers(searchBox.getText().toString()); });

        TextView categories = text("Categories are based on image file names", 12, 0xFFBDB8CC, false);
        LinearLayout.LayoutParams catLp = new LinearLayout.LayoutParams(-1, -2);
        catLp.topMargin = dp(12);
        catLp.bottomMargin = dp(8);
        root.addView(categories, catLp);

        ScrollView scroll = new ScrollView(this);
        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(listContainer);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private void renderWallpapers(String query) {
        if (listContainer == null) return;
        listContainer.removeAllViews();
        ArrayList<String> matches = new ArrayList<>();
        for (String name : wallpapers) {
            String lower = name.toLowerCase(Locale.ROOT);
            if (!lower.contains(query.toLowerCase(Locale.ROOT))) continue;
            if (favoritesOnly && !favorites.contains(name)) continue;
            matches.add(name);
        }
        if (matches.isEmpty()) {
            String message = wallpapers.isEmpty()
                ? "No wallpapers added yet. Add licensed JPG, PNG or WebP images to app/src/main/assets/wallpapers/ and rebuild. Use filenames such as Nature_4K.jpg or Space_8K.jpg."
                : (favoritesOnly ? "You have not saved any favorites yet." : "No wallpapers match your search.");
            TextView empty = text(message, 15, 0xFFE2DEEA, false);
            empty.setPadding(dp(8), dp(24), dp(8), dp(24));
            listContainer.addView(empty);
            return;
        }
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        for (String name : matches) grid.addView(makeCard(name));
        listContainer.addView(grid);
    }

    private View makeCard(String name) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(6), dp(6), dp(6), dp(10));
        card.setBackground(roundRect(0xFF252431, 14));
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = getResources().getDisplayMetrics().widthPixels / 2 - dp(24);
        params.setMargins(dp(4), dp(4), dp(4), dp(8));
        card.setLayoutParams(params);

        ImageView preview = new ImageView(this);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preview.setBackground(roundRect(0xFF393644, 10));
        try (InputStream in = getAssets().open("wallpapers/" + name)) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, opts);
            opts.inSampleSize = sampleSize(opts, dp(170), dp(220));
            opts.inJustDecodeBounds = false;
            try (InputStream in2 = getAssets().open("wallpapers/" + name)) {
                Bitmap bitmap = BitmapFactory.decodeStream(in2, null, opts);
                preview.setImageBitmap(bitmap);
            }
        } catch (Exception ignored) { }
        card.addView(preview, new LinearLayout.LayoutParams(-1, dp(190)));

        TextView label = text(name, 12, Color.WHITE, true);
        label.setMaxLines(2);
        label.setPadding(dp(3), dp(8), dp(3), dp(4));
        card.addView(label);
        Button favorite = button(favorites.contains(name) ? "♥ Saved" : "♡ Favorite");
        card.addView(favorite, new LinearLayout.LayoutParams(-1, dp(38)));
        favorite.setOnClickListener(v -> {
            if (favorites.contains(name)) favorites.remove(name); else favorites.add(name);
            getPreferences(MODE_PRIVATE).edit().putStringSet("favorites", new HashSet<>(favorites)).apply();
            renderWallpapers(searchBox.getText().toString());
        });
        preview.setOnClickListener(v -> showWallpaperActions(name));
        label.setOnClickListener(v -> showWallpaperActions(name));
        return card;
    }

    private void showWallpaperActions(String name) {
        new android.app.AlertDialog.Builder(this)
            .setTitle(name)
            .setMessage("Set this image as your device wallpaper?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Set wallpaper", (d, w) -> setWallpaper(name))
            .show();
    }

    private void setWallpaper(String name) {
        try (InputStream in = getAssets().open("wallpapers/" + name)) {
            WallpaperManager.getInstance(this).setStream(in);
            Toast.makeText(this, "Wallpaper applied successfully", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not set wallpaper: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private int sampleSize(BitmapFactory.Options o, int reqW, int reqH) {
        int size = 1;
        while (o.outHeight / size > reqH * 2 || o.outWidth / size > reqW * 2) size *= 2;
        return size;
    }
    private Button button(String label) {
        Button b = new Button(this); b.setText(label); b.setTextSize(12); b.setAllCaps(false);
        b.setTextColor(Color.WHITE); b.setBackground(roundRect(0xFF51466D, 12)); return b;
    }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return t;
    }
    private GradientDrawable roundRect(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }

    private interface TextChanged { void changed(String value); }
    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final TextChanged callback;
        SimpleTextWatcher(TextChanged callback) { this.callback = callback; }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        public void onTextChanged(CharSequence s, int start, int before, int count) { callback.changed(s.toString()); }
        public void afterTextChanged(android.text.Editable e) { }
    }
}
