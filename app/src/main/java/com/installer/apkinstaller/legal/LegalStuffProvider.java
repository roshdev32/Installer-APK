package com.installer.apkinstaller.legal;

public interface LegalStuffProvider {

    boolean hasPrivacyPolicy();

    String getPrivacyPolicyUrl();

    boolean hasEula();

    String getEulaUrl();

}
