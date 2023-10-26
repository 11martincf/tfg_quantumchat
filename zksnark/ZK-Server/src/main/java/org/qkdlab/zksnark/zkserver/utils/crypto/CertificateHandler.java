package org.qkdlab.zksnark.zkserver.utils.crypto;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.security.*;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CertificateHandler
 *
 * Valida el certificado proporcionado por el client en la Autenticación
 */
public class CertificateHandler {

    public X509Certificate decodeCertificate(byte[] encodedCert) {
        ByteArrayInputStream bais = new ByteArrayInputStream(encodedCert);
        X509Certificate cert;
        try {
            cert = (X509Certificate)CertificateFactory.getInstance("X.509").generateCertificate(bais);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }

        return cert;
    }

    public boolean validateCertificate(X509Certificate cert) {
        boolean isValid = true;
        try {
            List<X509Certificate> chain = new ArrayList<>();
            chain.add(cert);

            PKIXCertPathValidatorResult pkixCertPathValidatorResult = validatePath(chain);
            //System.out.println(bytesToHex(serverCertificate.getCertificate().getCertificateAt(0).getSerialNumber().toByteArray()));

        }
        catch (Exception e) {
            isValid = false;
        }

        return isValid;
    }

    public static PKIXCertPathValidatorResult validatePath(
            List<X509Certificate> certs) throws GeneralSecurityException {
        return validatePath(certs, getDefaultRootCAs());
    }

    public static PKIXCertPathValidatorResult validatePath(
            List<X509Certificate> certs, Set<TrustAnchor> trustAnchors)
            throws GeneralSecurityException {
        CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
        PKIXParameters params = new PKIXParameters(trustAnchors);
        params.setRevocationEnabled(false);

        CertificateFactory cf = CertificateFactory.getInstance("X509");
        CertPath path = cf.generateCertPath(certs);

        return (PKIXCertPathValidatorResult) cpv.validate(path, params);
    }

    /**
     * Obtains the list of default root CAs installed in the JRE.
     */
    public static Set<TrustAnchor> getDefaultRootCAs()
            throws NoSuchAlgorithmException, KeyStoreException {
        X509TrustManager x509tm = getDefaultX509TrustManager();


        Set<TrustAnchor> rootCAs = new HashSet<>();
        for (X509Certificate c : x509tm.getAcceptedIssuers()) {
            rootCAs.add(new TrustAnchor(c, null));
        }
        return rootCAs;
    }

    /**
     * Loads the system default {@link X509TrustManager}.
     */
    public static X509TrustManager getDefaultX509TrustManager()
            throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null);

        for (TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                return (X509TrustManager) tm;
            }
        }
        throw new IllegalStateException("X509TrustManager is not found");
    }

    public boolean verifySignature(X509Certificate cert, byte[] signature, byte[] commitment) {
        PublicKey publicKey = cert.getPublicKey();
        boolean isValid = true;
        Signature sig;
        try {
            sig = Signature.getInstance("NONEwithRSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        try {
            sig.initVerify(publicKey);
            sig.update(commitment);
            isValid = sig.verify(signature);
        } catch (Exception e) {
            isValid = false;
        }

        return isValid;
    }
}
