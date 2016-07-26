package org.stepic.droid.view.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.model.CertificateViewItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder> {

    private Context context;
    private List<CertificateViewItem> certificateList;

    public CertificateAdapter(Context context, @NotNull List<CertificateViewItem> certificateList) {
        this.context = context;
        this.certificateList = certificateList;
    }

    @Override
    public CertificateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.certificate_item, null);
        return new CertificateViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CertificateViewHolder holder, int position) {
        CertificateViewItem certificate = certificateList.get(position);
        holder.setData(certificate);
    }

    @Override
    public int getItemCount() {
        return certificateList.size();
    }

    class CertificateViewHolder extends RecyclerView.ViewHolder implements CertificateClickListener {

        @BindView(R.id.certificate_title)
        TextView certificateTitleView;

        CertificateViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CertificateViewHolder.this.onClick(CertificateAdapter.this.certificateList.get(getAdapterPosition()));
                }
            });
        }

        public void setData(CertificateViewItem certificate) {
            certificateTitleView.setText(certificate.getTitle());
        }


        @Override
        public void onClick(CertificateViewItem certificate) {
            // TODO: 26.07.16 open certificate
        }

    }

    private interface CertificateClickListener {
        void onClick(CertificateViewItem certificate);
    }
}
