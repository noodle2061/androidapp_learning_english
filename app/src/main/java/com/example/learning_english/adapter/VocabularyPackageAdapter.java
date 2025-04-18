package com.example.learning_english.adapter;

import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_english.R;
import com.example.learning_english.db.VocabularyPackage; // Assuming VocabularyPackage entity exists

public class VocabularyPackageAdapter extends ListAdapter<VocabularyPackage, VocabularyPackageAdapter.PackageViewHolder> {

    private final OnPackageInteractionListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION; // Track selected package

    public interface OnPackageInteractionListener {
        void onPackageClicked(VocabularyPackage pkg, int position); // When a package row is clicked
        void onPackageCheckboxChanged(VocabularyPackage pkg, boolean isChecked); // When checkbox state changes
        // Add other interactions like delete if needed
    }

    public VocabularyPackageAdapter(@NonNull DiffUtil.ItemCallback<VocabularyPackage> diffCallback, OnPackageInteractionListener listener) {
        super(diffCallback);
        this.listener = listener;
    }

    @NonNull
    @Override
    public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vocabulary_package, parent, false);
        return new PackageViewHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageViewHolder holder, int position) {
        VocabularyPackage current = getItem(position);
        // Pass the selected state to the ViewHolder
        holder.bind(current, position == selectedPosition);
    }

    // Method to update the selected position and refresh the relevant items
    public void setSelectedPosition(int position) {
        if (position == selectedPosition) return; // No change

        int previousPosition = selectedPosition;
        selectedPosition = position;

        // Notify changes for the previous and new selected items
        if (previousPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousPosition);
        }
        if (selectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPosition);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    // --- ViewHolder Class ---
    class PackageViewHolder extends RecyclerView.ViewHolder {
        private final TextView packageNameView;
        private final CheckBox packageCheckBox;
        private VocabularyPackage currentPackage;
        private final OnPackageInteractionListener interactionListener;

        private PackageViewHolder(View itemView, OnPackageInteractionListener listener) {
            super(itemView);
            this.interactionListener = listener;
            packageNameView = itemView.findViewById(R.id.textview_package_name);
            packageCheckBox = itemView.findViewById(R.id.checkbox_package);

            // Handle clicks on the entire row to select the package
            itemView.setOnClickListener(v -> {
                // *** USE getAdapterPosition() INSTEAD ***
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && interactionListener != null && currentPackage != null) {
                    interactionListener.onPackageClicked(currentPackage, position);
                    // Activity should call setSelectedPosition on the adapter instance
                } else {
                    Log.w("PackageViewHolder", "onClick detected but position invalid or listener/package null");
                }
            });

            // Handle checkbox changes
            packageCheckBox.setOnCheckedChangeListener(null); // Clear listener before binding
            packageCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Only trigger listener if the change is user-initiated
                if (buttonView.isPressed() && interactionListener != null && currentPackage != null) {
                    interactionListener.onPackageCheckboxChanged(currentPackage, isChecked);
                }
            });
        }

        // Bind data and selection state
        public void bind(VocabularyPackage pkg, boolean isSelected) {
            currentPackage = pkg;
            packageNameView.setText(pkg.getName()); // Assuming getName() exists

            // Set checkbox state WITHOUT triggering the listener
            packageCheckBox.setOnCheckedChangeListener(null);
            // TODO: Determine initial checkbox state based on words in the package (complex)
            // For now, let's assume it starts unchecked or reflects a saved state if you implement that.
            // packageCheckBox.setChecked(pkg.isGloballySelectedForReview()); // Example if you add such a field
            packageCheckBox.setChecked(false); // Default to unchecked for simplicity now
            // Re-attach the listener
            packageCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed() && interactionListener != null && currentPackage != null) {
                    interactionListener.onPackageCheckboxChanged(currentPackage, isChecked);
                }
            });

            // Update background based on selection state
            itemView.setSelected(isSelected); // Use standard selected state
            if (isSelected) {
                // Consider using a ColorStateList or a dedicated selector drawable for better theme handling
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.review_highlight_background));
            } else {
                // Use the default selectable item background
                TypedArray typedArray = itemView.getContext().obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground});
                int backgroundResource = typedArray.getResourceId(0, 0);
                itemView.setBackgroundResource(backgroundResource);
                typedArray.recycle();
            }
        }
    }
    // -----------------------

    // --- DiffUtil Callback ---
    public static class PackageDiff extends DiffUtil.ItemCallback<VocabularyPackage> {
        @Override
        public boolean areItemsTheSame(@NonNull VocabularyPackage oldItem, @NonNull VocabularyPackage newItem) {
            return oldItem.getId() == newItem.getId(); // Assuming getId() exists
        }

        @Override
        public boolean areContentsTheSame(@NonNull VocabularyPackage oldItem, @NonNull VocabularyPackage newItem) {
            // Compare relevant fields like name, potentially checkbox state if stored
            return oldItem.equals(newItem); // Assuming equals() is implemented
        }
    }
    // -----------------------
}
