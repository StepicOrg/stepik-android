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
import org.stepic.droid.model.CertificateType;
import org.stepic.droid.model.CertificateViewItem;

import java.util.List;

import butterknife.BindString;
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

        @BindView(R.id.certificate_description)
        TextView certificateDescriptionView;

        @BindString(R.string.certificate_result_with_substitution)
        String certificateResultString;

        @BindString(R.string.certificate_distinction_with_substitution)
        String certificateDistinctionString;

        @BindString(R.string.certificate_regular_with_substitution)
        String certificateRegularString;

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

            String certificateDescriptionLocal = null;
            if (certificate.getType() == CertificateType.distinction) {
                certificateDescriptionLocal = String.format(certificateDistinctionString, certificate.getTitle());
            } else if (certificate.getType() == CertificateType.regular) {
                certificateDescriptionLocal = String.format(certificateRegularString, certificate.getTitle());
            }

            if (certificateDescriptionLocal != null) {
                certificateDescriptionView.setText(certificateDescriptionLocal);
            } else {
                certificateDescriptionView.setText("");
            }

            if (certificate.getGrade() != null) {
                certificateGradeView.setText(String.format(certificateResultString, certificate.getGrade()));
            } else {
                certificateGradeView.setText("");
            }
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
