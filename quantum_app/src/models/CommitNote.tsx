import * as forge from 'node-forge';
import { HashUtil } from './HashUtil';

class CommitNote {

    protected sigma?: Uint8Array;
    protected publicKey?: Uint8Array;
    protected privateKey?: Uint8Array;

    constructor();
    constructor(sigma: Uint8Array);
    constructor(sigma: Uint8Array, privateKey: Uint8Array, publicKey: Uint8Array);
    constructor(sigma?: Uint8Array, privateKey?: Uint8Array, publicKey?: Uint8Array) {

        if (sigma && privateKey && publicKey) {
            this.sigma = sigma;
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        } else if (sigma) {
            const rsa = forge.pki.rsa;
            const keyPair = rsa.generateKeyPair({ bits: 2048 });
            
            this.sigma = sigma;
            this.publicKey = new Uint8Array(forge.util.binary.raw.decode(forge.asn1.toDer(forge.pki.publicKeyToAsn1(keyPair.publicKey)).getBytes()));
            this.privateKey = new Uint8Array(forge.util.binary.raw.decode(forge.asn1.toDer(forge.pki.privateKeyToAsn1(keyPair.privateKey)).getBytes()));
        }
    }

    getSigma(): Uint8Array | undefined {
        return this.sigma;
    }

    getEncodedPublicKey(): Uint8Array | undefined {
        return this.publicKey;
    }

    getEncodedPrivateKey(): Uint8Array | undefined {
        return this.privateKey;
    }

    getPublicKey(): Uint8Array | undefined {
        return this.publicKey ? HashUtil.hash(this.publicKey) : undefined;
    }

    getPrivateKey(): Uint8Array | undefined {
        return this.privateKey ? HashUtil.hash(this.privateKey) : undefined;
    }
}

export default CommitNote;
