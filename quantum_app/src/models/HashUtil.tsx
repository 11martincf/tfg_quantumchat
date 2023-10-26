import * as forge from 'node-forge';
import CommitNote from './CommitNote';

class HashUtil {
    
    public static generateCommitment(commitNote: CommitNote): Uint8Array {
        const md = forge.md.sha256.create();
        
        const preimage = HashUtil.concatUint8Arrays(commitNote.getPublicKey()!, commitNote.getSigma()!);
        md.update(HashUtil.uint8ArrayToBinaryString(preimage));
        return forge.util.binary.raw.decode(md.digest().getBytes());
    }

    public static generateNullifier(commitNote: CommitNote): Uint8Array {
        const md = forge.md.sha256.create();
        
        const preimage = HashUtil.concatUint8Arrays(commitNote.getPrivateKey()!, commitNote.getSigma()!);
        md.update(HashUtil.uint8ArrayToBinaryString(preimage));
        return forge.util.binary.raw.decode(md.digest().getBytes());
    }

    public static signCommitment(commitment: Uint8Array, privateKey: forge.pki.rsa.PrivateKey): Uint8Array {
        const signature = privateKey.sign(HashUtil.uint8ArrayToBinaryString(commitment), 'NONE');
        return forge.util.binary.raw.decode(signature);
    }

    public static hash(data: Uint8Array): Uint8Array {
        const md = forge.md.sha256.create();
        md.update(HashUtil.uint8ArrayToBinaryString(data));
        return forge.util.binary.raw.decode(md.digest().getBytes());
    }

    // Helper method to concatenate Uint8Arrays
    private static concatUint8Arrays(...arrays: Uint8Array[]): Uint8Array {
        let totalLength = 0;
        for (const arr of arrays) {
            totalLength += arr.length;
        }
        const result = new Uint8Array(totalLength);
        let offset = 0;
        for (const arr of arrays) {
            result.set(arr, offset);
            offset += arr.length;
        }
        return result;
    }

    // Helper method to convert Uint8Array to a binary string
    private static uint8ArrayToBinaryString(data: Uint8Array): string {
        return Array.from(data).map(byte => String.fromCharCode(byte)).join('');
    }
}

export { HashUtil };
