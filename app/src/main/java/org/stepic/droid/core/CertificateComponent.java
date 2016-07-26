package org.stepic.droid.core;


import org.stepic.droid.view.fragments.CertificateFragment;

import dagger.Subcomponent;

@PerFragment
@Subcomponent(modules = {CertificateModule.class})
public interface CertificateComponent {
    void inject(CertificateFragment certificateFragment);
}
