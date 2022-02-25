package com.syntifi.crypto.key;

import lombok.EqualsAndHashCode;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;

@EqualsAndHashCode(callSuper = true)
public class Ed25519PrivateKey extends AbstractPrivateKey {
    
    private Ed25519PrivateKeyParameters privateKeyParameters;

    /*
     * SEQUENCE (3 elem) INTEGER 0 SEQUENCE (1 elem) OBJECT IDENTIFIER 1.3.101.112
     * curveEd25519 (EdDSA 25519 signature algorithm) OCTET STRING (32 byte)
     * 38AECE974291F14B5FEF97E1B21F684394120B6E7A8AFB04398BBE787E8BC559 OCTET STRING
     * (32 byte) 38AECE974291F14B5FEF97E1B21F684394120B6E7A8AFB04398BBE787E8BC559
     */
    @Override
    public void readPrivateKey(String filename) throws IOException {
        ASN1Primitive key = ASN1Primitive.fromByteArray(PemFileHelper.readPemFile(filename));
        PrivateKeyInfo keyInfo = PrivateKeyInfo.getInstance(key);
        String algoId = keyInfo.getPrivateKeyAlgorithm().getAlgorithm().toString();
        if (algoId.equals(ASN1Identifiers.Ed25519OID.getId())) {
            privateKeyParameters = new Ed25519PrivateKeyParameters(keyInfo.getPrivateKey().getEncoded(), 4);
            setKey(privateKeyParameters.getEncoded());
        }
    }

    @Override
    public void writePrivateKey(String filename) throws IOException {
        DERSequence derPrefix = new DERSequence(ASN1Identifiers.Ed25519OID);
        DEROctetString key = new DEROctetString(new DEROctetString(getKey()));
        ASN1EncodableVector vector = new ASN1EncodableVector();
        vector.add(new ASN1Integer(0));
        vector.add(derPrefix);
        vector.add(key);
        DERSequence derKey = new DERSequence(vector);
        PemFileHelper.writePemFile(filename, derKey.getEncoded(), ASN1Identifiers.PRIVATE_KEY_DER_HEADER);
    }

    @Override
    public String sign(String message) {
        byte[] byteMessage = message.getBytes();
        Signer signer = new Ed25519Signer();
        signer.init(true, privateKeyParameters);
        signer.update(byteMessage, 0, byteMessage.length);
        byte[] signature;
        try {
            signature = signer.generateSignature();
            return Hex.toHexString(signature);
        } catch (DataLengthException | CryptoException e) {
            // TODO: throw new SomeException();
            return null;
        }
    }

    @Override
    public AbstractPublicKey derivePublicKey() {
        return new Ed25519PublicKey(privateKeyParameters.generatePublicKey().getEncoded());
    }
}
