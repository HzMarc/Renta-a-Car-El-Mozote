package com.example.el_mozote.vehicles;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.el_mozote.R;

import java.util.ArrayList;
import java.util.List;

public class VehiclePhotoThumbnailAdapter extends RecyclerView.Adapter<VehiclePhotoThumbnailAdapter.PhotoViewHolder> {

    public interface OnPhotoRemoveListener {
        void onRemove(int position, String photoPath);
    }

    private final List<String> photoPaths = new ArrayList<>();
    private final OnPhotoRemoveListener removeListener;

    public VehiclePhotoThumbnailAdapter(List<String> initialPhotos, OnPhotoRemoveListener removeListener) {
        if (initialPhotos != null) {
            this.photoPaths.addAll(initialPhotos);
        }
        this.removeListener = removeListener;
    }

    public void setPhotos(List<String> newPhotos) {
        this.photoPaths.clear();
        if (newPhotos != null) {
            this.photoPaths.addAll(newPhotos);
        }
        notifyDataSetChanged();
    }

    public void addPhotos(List<String> morePhotos) {
        if (morePhotos != null && !morePhotos.isEmpty()) {
            int start = this.photoPaths.size();
            this.photoPaths.addAll(morePhotos);
            notifyItemRangeInserted(start, morePhotos.size());
        }
    }

    public List<String> getPhotos() {
        return new ArrayList<>(photoPaths);
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle_form_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        String path = photoPaths.get(position);
        ImageStorageHelper.loadScaledImage(holder.imgThumbnail, path, 160, 160, R.drawable.ic_car_placeholder);

        holder.btnRemove.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && currentPos < photoPaths.size()) {
                String removedPath = photoPaths.remove(currentPos);
                notifyItemRemoved(currentPos);
                if (removeListener != null) {
                    removeListener.onRemove(currentPos, removedPath);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return photoPaths.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        ImageButton btnRemove;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgFormPhotoThumbnail);
            btnRemove = itemView.findViewById(R.id.btnRemoveFormPhoto);
        }
    }
}
