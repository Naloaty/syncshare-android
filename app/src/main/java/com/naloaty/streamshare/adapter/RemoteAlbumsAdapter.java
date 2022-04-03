package com.naloaty.streamshare.adapter;


import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.naloaty.streamshare.R;
import com.naloaty.streamshare.app.GlideApp;
import com.naloaty.streamshare.communication.CommunicationHelper;
import com.naloaty.streamshare.database.device.NetworkDevice;
import com.naloaty.streamshare.database.media.Album;

import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;

/**
 * Adapter class for RecyclerView that displays a list of albums on the remote device.
 * @see com.naloaty.streamshare.fragment.RemoteAlbumsFragment
 */
public class RemoteAlbumsAdapter extends RecyclerView.Adapter<RemoteAlbumsAdapter.ViewHolder>{

    private static final String TAG = "LocalAlbumsAdapter";

    private List<Album> mList = new ArrayList<>();
    private OnRVClickListener mClickListener;
    private NetworkDevice mNetworkDevice;

    public RemoteAlbumsAdapter(OnRVClickListener clickListener, NetworkDevice networkDevice){
        mClickListener = clickListener;
        mNetworkDevice = networkDevice;
    }

    /**
     * Updates the current list of albums
     * @param albumsList New or updated albums list
     */
    public void setAlbumsList(@NonNull List<Album> albumsList) {
        /*
         * TODO: When adding item to RV it flashes during ViewMessage animation
         */
        if (albumsList.size() == 1 && mList.size() < 2){
            mList = albumsList;
            notifyDataSetChanged();
            return;
        }

        if (mList == null) {
            mList = albumsList;
            notifyItemRangeInserted(0, albumsList.size());
        }
        else
        {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mList.size();
                }

                @Override
                public int getNewListSize() {
                    return albumsList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mList.get(oldItemPosition).getId() ==
                            albumsList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Album newAlbum = albumsList.get(newItemPosition);
                    Album oldAlbum = mList.get(oldItemPosition);

                    return newAlbum.getId() == oldAlbum.getId()
                            && newAlbum.getAlbumId() == oldAlbum.getAlbumId()
                            && TextUtils.equals(newAlbum.getName(), oldAlbum.getName())
                            && TextUtils.equals(newAlbum.getPath(), oldAlbum.getPath())
                            && TextUtils.equals(newAlbum.getLastItemFilename(), oldAlbum.getLastItemFilename())
                            && newAlbum.getItemsCount() == oldAlbum.getItemsCount();

                }
            });

            mList = albumsList;
            result.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.remote_album, parent, false);
        return new ViewHolder(view, mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Album album = mList.get(position);

        holder.albumName.setText(album.getName());
        holder.itemsCount.setText(String.valueOf(album.getItemsCount()));


        RequestOptions options = new RequestOptions()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .centerCrop()
                .placeholder(R.color.colorEmptyThumbnail)
                .error(R.drawable.ic_warning_24dp)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

        String URL = CommunicationHelper.getThumbnailRequestURL(mNetworkDevice) + album.getLastItemFilename();
        DrawableCrossFadeFactory factory = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

        Log.d(TAG, "Album icon url: " + URL);
        Log.d(TAG, "Album lastItemFilename: " + album.getLastItemFilename());

        GlideApp.with(holder.albumPreview.getContext())
                .asBitmap()
                .load(URL)
                .apply(options)
                .transition(withCrossFade(factory))
                .into(holder.albumPreview);
    }



    @Override
    public int getItemCount() {
        return mList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView albumPreview;
        TextView albumName;
        TextView itemsCount;
        ImageView actionButton;
        RelativeLayout layout;

        OnRVClickListener clickListener;

        ViewHolder(@NonNull View itemView, OnRVClickListener clickListener) {
            super(itemView);
            albumPreview = itemView.findViewById(R.id.remote_album_image);
            albumName = itemView.findViewById(R.id.remote_album_name);
            itemsCount = itemView.findViewById(R.id.remote_album_count);
            actionButton = itemView.findViewById(R.id.remote_album_action_btn);
            layout = itemView.findViewById(R.id.remote_album_layout);

            layout.setOnClickListener(this);
            actionButton.setOnClickListener(this);
            this.clickListener = clickListener;
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(getAdapterPosition());
        }
    }
}
