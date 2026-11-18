package com.example.el_mozote.vehicles;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.el_mozote.R;

import java.util.ArrayList;
import java.util.List;

public class VehiclePhotoGalleryAdapter extends RecyclerView.Adapter<VehiclePhotoGalleryAdapter.GalleryViewHolder> {

    private final List<String> photos = new ArrayList<>();

    public VehiclePhotoGalleryAdapter(List<String> initialPhotos) {
        if (initialPhotos != null) {
            this.photos.addAll(initialPhotos);
        }
    }

    public void setPhotos(List<String> newPhotos) {
        this.photos.clear();
        if (newPhotos != null) {
            this.photos.addAll(newPhotos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle_gallery_photo, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        String path = photos.get(position);
        ImageStorageHelper.loadScaledImage(holder.imgPhoto, path, 560, 400, R.drawable.ic_car_placeholder);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPhoto;

        GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.imgGalleryPhoto);
        }
    }
}
