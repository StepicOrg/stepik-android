package org.stepic.droid.ui.adapters;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.App;
import org.stepic.droid.core.presenters.CertificatePresenter;
import org.stepic.droid.model.CertificateViewItem;
import org.stepik.android.model.Certificate;

import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CertificatesAdapter extends RecyclerView.Adapter<CertificatesAdapter.CertificateViewHolder> {

    private CertificatePresenter certificatePresenter;
    private Activity activity;
    private Drawable certificatePlaceholder;

    public CertificatesAdapter(@NotNull CertificatePresenter certificatePresenter, @NotNull Activity activity) {
        this.certificatePresenter = certificatePresenter;
        this.activity = activity;
        certificatePlaceholder = ContextCompat.getDrawable(App.Companion.getAppContext(), R.drawable.general_placeholder);
    }

    @Override
    public CertificateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity).inflate(R.layout.certificate_item, parent, false);
        return new CertificateViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CertificateViewHolder holder, int position) {
        CertificateViewItem certificate = certificatePresenter.get(position);
        holder.setData(certificate);
    }

    @Override
    public int getItemCount() {
        return certificatePresenter.size();
    }

    public void updateCertificates(List<CertificateViewItem> certificateViewItems) {
        notifyDataSetChanged();
    }

    class CertificateViewHolder extends RecyclerView.ViewHolder implements CertificateClickListener {

        @BindView(R.id.certificate_title)
        TextView certificateTitleView;

        @BindView(R.id.certificate_icon)
        ImageView certificateIcon;

        @BindView(R.id.certificate_grade)
        TextView certificateGradeView;

        @BindView(R.id.certificate_description)
        TextView certificateDescriptionView;

        @BindView(R.id.certificate_share_button)
        View certificateShareButton;

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
                    CertificateViewHolder.this.onClick(CertificatesAdapter.this.certificatePresenter.get(getAdapterPosition()));
                }
            });

            certificateShareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CertificateViewItem certificateViewItem = CertificatesAdapter.this.certificatePresenter.get(getAdapterPosition());
                    certificatePresenter.showShareDialogForCertificate(certificateViewItem);
                }
            });
        }

        public void setData(CertificateViewItem certificate) {

            certificateTitleView.setText(certificate.getTitle());

            String certificateDescriptionLocal = null;
            if (certificate.getType() == Certificate.Type.DISTINCTION) {
                certificateDescriptionLocal = String.format(certificateDistinctionString, certificate.getTitle());
            } else if (certificate.getType() == Certificate.Type.REGULAR) {
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

            @NotNull
            final String coverFullPath = certificate.getCoverFullPath() == null ? "" : certificate.getCoverFullPath();
            Glide.with(App.Companion.getAppContext())
                    .load(coverFullPath)
                    .placeholder(certificatePlaceholder)
                    .into(certificateIcon);
        }


        @Override
        public void onClick(CertificateViewItem certificate) {
            certificatePresenter.showCertificateAsPdf(activity, certificate.getFullPath());
        }

    }


    private interface CertificateClickListener {
        void onClick(CertificateViewItem certificate);
    }
}
