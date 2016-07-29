package org.stepic.droid.view.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;

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

    public void updateCertificates(List<CertificateViewItem> certificateViewItems) {
        certificateList.clear();
        certificateList.addAll(certificateViewItems);
        notifyDataSetChanged();
    }

    class CertificateViewHolder extends RecyclerView.ViewHolder implements CertificateClickListener {

        @BindView(R.id.certificate_title)
        TextView certificateTitleView;

        @BindView(R.id.certificate_icon)
        DraweeView certificateIcon;

        @BindView(R.id.certificate_grade)
        TextView certificateGradeView;

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
            String prefix = "Сертификат с отличием за курс"; // FIXME: 29.07.16 FROM RESOURCES, resolve distinct

            String certificateName = prefix + " " + certificate.getTitle();
            certificateTitleView.setText(certificate.getTitle());
            certificateGradeView.setText(certificateName + "\n\n Result: 146%"); // FIXME: 29.07.16 throw in certificateviewitem result and show here
//            certificateGradeView.setText("Result: 146%"); // FIXME: 29.07.16 throw in certificateviewitem result and show here
            certificateIcon.setController(getControllerForCertificateCover(certificate.getCoverFullPath()));
        }


        @Override
        public void onClick(CertificateViewItem certificate) {
            // TODO: 26.07.16 open certificate
        }

    }

    private DraweeController getControllerForCertificateCover(String coverFullPath) {
        if (coverFullPath != null) {
            return Fresco.newDraweeControllerBuilder()
                    .setUri(coverFullPath)
                    .setAutoPlayAnimations(true)
                    .build();
        } else {
            //for empty cover:
            Uri uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                    .path(String.valueOf(R.drawable.ic_course_placeholder))
                    .build();

            return Fresco.newDraweeControllerBuilder()
                    .setUri(uri)
                    .setAutoPlayAnimations(true)
                    .build();
        }
    }

    private interface CertificateClickListener {
        void onClick(CertificateViewItem certificate);
    }
}
